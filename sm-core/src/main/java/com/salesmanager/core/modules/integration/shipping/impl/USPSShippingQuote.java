package com.salesmanager.core.modules.integration.shipping.impl;

import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.digester.Digester;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.salesmanager.core.business.customer.model.Delivery;
import com.salesmanager.core.business.merchant.model.MerchantStore;
import com.salesmanager.core.business.reference.country.model.Country;
import com.salesmanager.core.business.shipping.model.PackageDetails;
import com.salesmanager.core.business.shipping.model.ShippingConfiguration;
import com.salesmanager.core.business.shipping.model.ShippingDescription;
import com.salesmanager.core.business.shipping.model.ShippingOption;
import com.salesmanager.core.business.system.model.IntegrationConfiguration;
import com.salesmanager.core.business.system.model.IntegrationModule;
import com.salesmanager.core.business.system.model.MerchantLog;
import com.salesmanager.core.business.system.model.ModuleConfig;
import com.salesmanager.core.business.system.service.MerchantLogService;
import com.salesmanager.core.constants.MeasureUnit;
import com.salesmanager.core.modules.integration.IntegrationException;
import com.salesmanager.core.modules.integration.shipping.model.ShippingQuoteModule;
import com.salesmanager.core.utils.DataUtils;
import com.salesmanager.core.constants.Constants;

/**
 * Integrates with USPS online API
 * @author casams1
 *
 */
public class USPSShippingQuote implements ShippingQuoteModule {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(USPSShippingQuote.class);
	
	@Autowired
	private MerchantLogService merchantLogService;

	@Override
	public void validateModuleConfiguration(
			IntegrationConfiguration integrationConfiguration,
			MerchantStore store) throws IntegrationException {
		
		
		List<String> errorFields = null;
		
		//validate integrationKeys['account']
		Map<String,String> keys = integrationConfiguration.getIntegrationKeys();
		if(keys==null || StringUtils.isBlank(keys.get("account"))) {
			errorFields = new ArrayList<String>();
			errorFields.add("identifier");
		}

		//validate at least one integrationOptions['packages']
		Map<String,List<String>> options = integrationConfiguration.getIntegrationOptions();
		if(options==null) {
			errorFields = new ArrayList<String>();
			errorFields.add("identifier");
		}
		
		List<String> packages = options.get("packages");
		if(packages==null || packages.size()==0) {
			if(errorFields==null) {
				errorFields = new ArrayList<String>();
			}
			errorFields.add("packages");
		}
		
		List<String> services = options.get("services");
		if(services==null || services.size()==0) {
			if(errorFields==null) {
				errorFields = new ArrayList<String>();
			}
			errorFields.add("services");
		}
		
		if(services!=null && services.size()>3) {
			if(errorFields==null) {
				errorFields = new ArrayList<String>();
			}
			errorFields.add("services");
		}
		
		if(errorFields!=null) {
			IntegrationException ex = new IntegrationException(IntegrationException.ERROR_VALIDATION_SAVE);
			ex.setErrorFields(errorFields);
			throw ex;
			
		}
		
		

	}

	@Override
	public List<ShippingOption> getShippingQuotes(
			List<PackageDetails> packages, BigDecimal orderTotal,
			Delivery delivery, MerchantStore store,
			IntegrationConfiguration configuration, IntegrationModule module,
			ShippingConfiguration shippingConfiguration, Locale locale)
			throws IntegrationException {

		
		
		BigDecimal total = orderTotal;

		if (packages == null) {
			return null;
		}
		
		List<ShippingOption> options = null;

		// only applies to Canada and US
		Country country = delivery.getCountry();
		if(!country.getIsoCode().equals("US") || !country.equals("CA")) {
			throw new IntegrationException("Canadapost Not configured for shipping in country " + country.getIsoCode());
		}

		// supports en and fr
		String language = locale.getLanguage();
		if (!language.equals(Locale.FRENCH.getLanguage())
				&& !language.equals(Locale.ENGLISH.getLanguage())) {
			language = Locale.ENGLISH.getLanguage();
		}
		

		// if store is not CAD /** maintained in the currency **/
/*		if (!store.getCurrency().equals(Constants.CURRENCY_CODE_CAD)) {
			total = CurrencyUtil.convertToCurrency(total, store.getCurrency(),
					Constants.CURRENCY_CODE_CAD);
		}*/

		
		GetMethod httpget = null;

		String pack = configuration.getIntegrationOptions().get("packages").get(0);

		try {
			
		
			
			Map<String,String> keys = configuration.getIntegrationKeys();
			if(keys==null || StringUtils.isBlank(keys.get("account"))) {
				throw new IntegrationException("Canadapost missing configuration key account");
			}

			
			String host = null;
			String protocol = null;
			String port = null;
			String url = null;
		
			
			
			//against which environment are we using the service
			String env = configuration.getEnvironment();

			Country originCountry = store.getCountry();

			Map<String, ModuleConfig> moduleConfigsMap = module.getModuleConfigs();
			for(String key : moduleConfigsMap.keySet()) {
				
				ModuleConfig moduleConfig = (ModuleConfig)moduleConfigsMap.get(key);
				if(moduleConfig.getEnv().equals(env)) {
					host = moduleConfig.getHost();
					protocol = moduleConfig.getScheme();
					port = moduleConfig.getPort();
					url = moduleConfig.getUri();
				}
			}
			

			StringBuilder xmlheader = new StringBuilder();
			if(store.getCountry().getIsoCode().equals(delivery.getCountry().getIsoCode())) {
				xmlheader.append("<RateV3Request USERID=\"").append(keys.get("account")).append("\">");
			} else {
				xmlheader.append("<IntlRateRequest USERID=\"").append(keys.get("account")).append("\">");
			}



			StringBuilder xmldatabuffer = new StringBuilder();

			Country customerCountry = delivery.getCountry();

		
			double totalW = 0;
			double totalH = 0;
			double totalL = 0;
			double totalG = 0;
			double totalP = 0;

			for (PackageDetails detail : packages) {


				// need size in inch
				double w = DataUtils.getMeasure(detail.getShippingWidth(),
						store, MeasureUnit.IN.name());
				double h = DataUtils.getMeasure(detail.getShippingHeight(),
						store, MeasureUnit.IN.name());
				double l = DataUtils.getMeasure(detail.getShippingLength(),
						store, MeasureUnit.IN.name());
	
				totalW = totalW + w;
				totalH = totalH + h;
				totalL = totalL + l;
	
				// Girth = Length + (Width x 2) + (Height x 2)
				double girth = l + (w * 2) + (h * 2);
		
				totalG = totalG + girth;
	
				// need weight in pounds
				double p = DataUtils.getWeight(detail.getShippingWeight(), store, MeasureUnit.LB.name());
	
				totalP = totalP + p;

			}

/*			BigDecimal convertedOrderTotal = CurrencyUtil.convertToCurrency(
					orderTotal, store.getCurrency(),
					Constants.CURRENCY_CODE_USD);*/

			// calculate total shipping volume

			// ship date is 3 days from here

			Calendar c = Calendar.getInstance();
			c.setTime(new Date());
			c.add(Calendar.DATE, 3);
			Date newDate = c.getTime();
			
			SimpleDateFormat format = new SimpleDateFormat(Constants.DEFAULT_DATE_FORMAT);
			String shipDate = format.format(newDate);
			


			int i = 1;

			// need pounds and ounces
			int pounds = (int) totalP;
			String ouncesString = String.valueOf(totalP - pounds);
			int ouncesIndex = ouncesString.indexOf(".");
			String ounces = "00";
			if (ouncesIndex > -1) {
				ounces = ouncesString.substring(ouncesIndex + 1);
			}

			String size = "REGULAR";
		
			if (totalL + totalG <= 64) {
				size = "REGULAR";
			} else if (totalL + totalG <= 108) {
				size = "LARGE";
			} else {
				size = "OVERSIZE";
			}

			/**
			 * Domestic <Package ID="1ST"> <Service>ALL</Service>
			 * <ZipOrigination>90210</ZipOrigination>
			 * <ZipDestination>96698</ZipDestination> <Pounds>8</Pounds>
			 * <Ounces>32</Ounces> <Container/> <Size>REGULAR</Size>
			 * <Machinable>true</Machinable> </Package>
			 * 
			 * //MAXWEIGHT=70 lbs
			 * 
			 * 
			 * //domestic container default=VARIABLE whiteSpace=collapse
			 * enumeration=VARIABLE enumeration=FLAT RATE BOX enumeration=FLAT
			 * RATE ENVELOPE enumeration=LG FLAT RATE BOX
			 * enumeration=RECTANGULAR enumeration=NONRECTANGULAR
			 * 
			 * //INTL enumeration=Package enumeration=Postcards or aerogrammes
			 * enumeration=Matter for the blind enumeration=Envelope
			 * 
			 * Size May be left blank in situations that do not Size. Defined as
			 * follows: REGULAR: package plus girth is 84 inches or less; LARGE:
			 * package length plus girth measure more than 84 inches not more
			 * than 108 inches; OVERSIZE: package length plus girth is more than
			 * 108 but not 130 inches. For example: <Size>REGULAR</Size>
			 * 
			 * International <Package ID="1ST"> <Machinable>true</Machinable>
			 * <MailType>Envelope</MailType> <Country>Canada</Country>
			 * <Length>0</Length> <Width>0</Width> <Height>0</Height>
			 * <ValueOfContents>250</ValueOfContents> </Package>
			 * 
			 * <Package ID="2ND"> <Pounds>4</Pounds> <Ounces>3</Ounces>
			 * <MailType>Package</MailType> <GXG> <Length>46</Length>
			 * <Width>14</Width> <Height>15</Height> <POBoxFlag>N</POBoxFlag>
			 * <GiftFlag>N</GiftFlag> </GXG>
			 * <ValueOfContents>250</ValueOfContents> <Country>Japan</Country>
			 * </Package>
			 */

			xmldatabuffer.append("<Package ID=\"").append(i).append("\">");


			if(store.getCountry().getIsoCode().equals(delivery.getCountry().getIsoCode())) {

				xmldatabuffer.append("<Service>");
				xmldatabuffer.append("ALL");
				xmldatabuffer.append("</Service>");
				xmldatabuffer.append("<ZipOrigination>");
				xmldatabuffer.append(DataUtils
						.trimPostalCode(store.getStorepostalcode()));
				xmldatabuffer.append("</ZipOrigination>");
				xmldatabuffer.append("<ZipDestination>");
				xmldatabuffer.append(DataUtils
						.trimPostalCode(delivery.getPostalCode()));
				xmldatabuffer.append("</ZipDestination>");
				xmldatabuffer.append("<Pounds>");
				xmldatabuffer.append(pounds);
				xmldatabuffer.append("</Pounds>");
				xmldatabuffer.append("<Ounces>");
				xmldatabuffer.append(ounces);
				xmldatabuffer.append("</Ounces>");
				xmldatabuffer.append("<Container>");
				xmldatabuffer.append(pack);
				xmldatabuffer.append("</Container>");
				xmldatabuffer.append("<Size>");
				xmldatabuffer.append(size);
				xmldatabuffer.append("</Size>");
				xmldatabuffer.append("<ShipDate>");
				xmldatabuffer.append(shipDate);
				xmldatabuffer.append("</ShipDate>");
			} else {
				// if international
				xmldatabuffer.append("<Pounds>");
				xmldatabuffer.append(pounds);
				xmldatabuffer.append("</Pounds>");
				xmldatabuffer.append("<Ounces>");
				xmldatabuffer.append(ounces);
				xmldatabuffer.append("</Ounces>");
				xmldatabuffer.append("<MailType>");
				xmldatabuffer.append("Package");
				xmldatabuffer.append("</MailType>");
				xmldatabuffer.append("<ValueOfContents>");
				xmldatabuffer.append(orderTotal);
				xmldatabuffer.append("</ValueOfContents>");
				xmldatabuffer.append("<Country>");
				xmldatabuffer.append(delivery.getCountry().getName());
				xmldatabuffer.append("</Country>");
			}

			// if international & CXG
			/*
			 * xmldatabuffer.append("<CXG>"); xmldatabuffer.append("<Length>");
			 * xmldatabuffer.append(""); xmldatabuffer.append("</Length>");
			 * xmldatabuffer.append("<Width>"); xmldatabuffer.append("");
			 * xmldatabuffer.append("</Width>");
			 * xmldatabuffer.append("<Height>"); xmldatabuffer.append("");
			 * xmldatabuffer.append("</Height>");
			 * xmldatabuffer.append("<POBoxFlag>"); xmldatabuffer.append("");
			 * xmldatabuffer.append("</POBoxFlag>");
			 * xmldatabuffer.append("<GiftFlag>"); xmldatabuffer.append("");
			 * xmldatabuffer.append("</GiftFlag>");
			 * xmldatabuffer.append("</CXG>");
			 */
		
			/*
			 * xmldatabuffer.append("<Width>"); xmldatabuffer.append(totalW);
			 * xmldatabuffer.append("</Width>");
			 * xmldatabuffer.append("<Length>"); xmldatabuffer.append(totalL);
			 * xmldatabuffer.append("</Length>");
			 * xmldatabuffer.append("<Height>"); xmldatabuffer.append(totalH);
			 * xmldatabuffer.append("</Height>");
			 * xmldatabuffer.append("<Girth>"); xmldatabuffer.append(totalG);
			 * xmldatabuffer.append("</Girth>");
			 */

			xmldatabuffer.append("</Package>");

			String xmlfooter = "</RateRequest>";
			if(!store.getCountry().getIsoCode().equals(delivery.getCountry().getIsoCode())) {
				xmlfooter = "</IntlRateRequest>";
			}

			StringBuilder xmlbuffer = new StringBuilder().append(xmlheader.toString()).append(
					xmldatabuffer.toString()).append(xmlfooter.toString());

			LOGGER.debug("USPS QUOTE REQUEST " + xmlbuffer.toString());

			String data = "";


			HttpClient client = new HttpClient();
		
			@SuppressWarnings("deprecation")
			String encoded = java.net.URLEncoder.encode(xmlbuffer.toString());
		
			String completeUri = url + "?API=RateV3&XML=" + encoded;
			if(!store.getCountry().getIsoCode().equals(delivery.getCountry().getIsoCode())) {
				completeUri = url + "?API=IntlRate&XML=" + encoded;
			}
		
			// ?API=RateV3
		
			httpget = new GetMethod(protocol + "://" + host + ":" + port
					+ completeUri);
			// RequestEntity entity = new
			// StringRequestEntity(xmlbuffer.toString(),"text/plain","UTF-8");
			// httpget.setRequestEntity(entity);
		
			int result = client.executeMethod(httpget);
			if (result != 200) {
				LOGGER.error("Communication Error with usps quote " + result + " "
						+ protocol + "://" + host + ":" + port + url);
				throw new Exception("USPS quote communication error " + result);
			}
			data = httpget.getResponseBodyAsString();
			LOGGER.debug("usps quote response " + data);

			USPSParsedElements parsed = new USPSParsedElements();

			/**
			 * <RateV3Response> <Package ID="1ST">
			 * <ZipOrigination>44106</ZipOrigination>
			 * <ZipDestination>20770</ZipDestination>
			 */

			Digester digester = new Digester();
			digester.push(parsed);

			if(store.getCountry().getIsoCode().equals(delivery.getCountry().getIsoCode())) {

				digester.addCallMethod("RateV3Response/Package/Error",
						"setError", 0);
				digester
						.addObjectCreate(
								"RateV3Response/Package/Postage",
								ShippingOption.class);
				digester.addSetProperties("RateV3Response/Package/Postage",
						"CLASSID", "optionId");
				digester.addCallMethod(
						"RateV3Response/Package/Postage/MailService",
						"optionName", 0);
				digester.addCallMethod(
						"RateV3Response/Package/Postage/MailService",
						"optionCode", 0);
				digester.addCallMethod("RateV3Response/Package/Postage/Rate",
						"optionPrice", 0);
				digester
						.addCallMethod(
								"RateV3Response/Package/Postage/Commitment/CommitmentDate",
								"estimatedNumberOfDays", 0);
				digester.addSetNext("RateV3Response/Package/Postage",
						"addOption");

			} else {
	
				digester.addCallMethod("IntlRateResponse/Package/Error",
						"setError", 0);
				digester
						.addObjectCreate(
								"IntlRateResponse/Package/Service",
								ShippingOption.class);
				digester.addSetProperties("IntlRateResponse/Package/Service",
						"ID", "optionId");
				digester.addCallMethod(
						"IntlRateResponse/Package/Service/SvcDescription",
						"setOptionName", 0);
				digester.addCallMethod(
						"IntlRateResponse/Package/Service/SvcDescription",
						"setOptionCode", 0);
				digester.addCallMethod(
						"IntlRateResponse/Package/Service/Postage",
						"setOptionPriceText", 0);
				digester.addCallMethod(
						"IntlRateResponse/Package/Service/SvcCommitments",
						"setEstimatedNumberOfDays", 0);
				digester.addSetNext("IntlRateResponse/Package/Service",
						"addOption");
	
			}

			// <?xml
			// version="1.0"?><AddressValidationResponse><Response><TransactionReference><CustomerContext>SalesManager
			// Data</CustomerContext><XpciVersion>1.0</XpciVersion></TransactionReference><ResponseStatusCode>0</ResponseStatusCode><ResponseStatusDescription>Failure</ResponseStatusDescription><Error><ErrorSeverity>Hard</ErrorSeverity><ErrorCode>10002</ErrorCode><ErrorDescription>The
			// XML document is well formed but the document is not
			// valid</ErrorDescription><ErrorLocation><ErrorLocationElementName>AddressValidationRequest</ErrorLocationElementName></ErrorLocation></Error></Response></AddressValidationResponse>

			Reader xmlreader = new StringReader(data);
			digester.parse(xmlreader);

			if (!StringUtils.isBlank(parsed.getErrorCode())) {
				LOGGER.error("Can't process USPS statusCode="
						+ parsed.getErrorCode() + " message= "
						+ parsed.getError());
				return null;
			}
			if (!StringUtils.isBlank(parsed.getStatusCode())
					&& !parsed.getStatusCode().equals("1")) {
				merchantLogService.save(new MerchantLog(store,
						"Can't process USPS statusCode="
								+ parsed.getStatusCode() + " message= "
								+ parsed.getError()));
				LOGGER.error("Can't process USPS statusCode="
						+ parsed.getStatusCode() + " message= "
						+ parsed.getError());
				return null;
			}
		
			if (parsed.getOptions() == null || parsed.getOptions().size() == 0) {
				LOGGER.warn("No options returned from UPS");
				return null;
			}

			
			
			
			
/*			String carrier = getShippingMethodDescription(locale);
			// cost is in USD, need to do conversion
		
			MerchantConfiguration rtdetails = config
					.getMerchantConfiguration(ShippingConstants.MODULE_SHIPPING_DISPLAY_REALTIME_QUOTES);
			int displayQuoteDeliveryTime = ShippingConstants.NO_DISPLAY_RT_QUOTE_TIME;
			if (rtdetails != null) {
		
				if (!StringUtils.isBlank(rtdetails.getConfigurationValue1())) {// display
																				// or
																				// not
																				// quotes
					try {
						displayQuoteDeliveryTime = Integer.parseInt(rtdetails
								.getConfigurationValue1());
		
					} catch (Exception e) {
						log.error("Display quote is not an integer value ["
								+ rtdetails.getConfigurationValue1() + "]");
					}
				}
			}
		
			LabelUtil labelUtil = LabelUtil.getInstance();
			// Map serviceMap =
			// com.salesmanager.core.util.ShippingUtil.buildServiceMap("usps",locale);
		
			List options = parsed.getOptions();
		
			Collection returnColl = null;
		
			if (options != null && options.size() > 0) {
		
				returnColl = new ArrayList();
				// Map selectedintlservices =
				// (Map)config.getConfiguration("service-global-usps");
				// need to create a Map of LABEL - LABLEL
				// Iterator servicesIterator =
				// selectedintlservices.keySet().iterator();
				// Map services = new HashMap();
		
				// ResourceBundle bundle = ResourceBundle.getBundle("usps",
				// locale);
		
				// while(servicesIterator.hasNext()) {
				// String key = (String)servicesIterator.next();
				// String value =
				// bundle.getString("shipping.quote.services.label." + key);
				// services.put(value, key);
				// }
		
				Iterator it = options.iterator();
				while (it.hasNext()) {
					ShippingOption option = (ShippingOption) it.next();
					option.setCurrency(Constants.CURRENCY_CODE_USD);
		
					StringBuffer description = new StringBuffer();
					description.append(option.getOptionName());
					if (displayQuoteDeliveryTime == ShippingConstants.DISPLAY_RT_QUOTE_TIME) {
						if (!StringUtils.isBlank(option
								.getEstimatedNumberOfDays())) {
							description.append(" (").append(
									option.getEstimatedNumberOfDays()).append(
									" ").append(
									labelUtil.getText(locale,
											"label.generic.days.lowercase"))
									.append(")");
						}
					}
					option.setDescription(description.toString());
		
					// get currency
					if (!option.getCurrency().equals(store.getCurrency())) {
						option.setOptionPrice(CurrencyUtil.convertToCurrency(
								option.getOptionPrice(), option.getCurrency(),
								store.getCurrency()));
					}
		
					// if(!services.containsKey(option.getOptionCode())) {
					// if(returnColl==null) {
					// returnColl = new ArrayList();
					// }
					// returnColl.add(option);
					// }
					returnColl.add(option);
				}
		
				// if(options.size()==0) {
				// CommonService.logServiceMessage(store.getMerchantId(),
				// " none of the service code returned by UPS [" +
				// selectedintlservices.keySet().toArray(new
				// String[selectedintlservices.size()]) +
				// "] for this shipping is in your selection list");
				// }
		
			}
		
			return returnColl;
		
		} catch (Exception e1) {
			log.error(e1);
			return null;
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (Exception ignore) {
				}
			}
			if (httpget != null) {
				httpget.releaseConnection();
			}
		}
					
					
	*/				
					
					
					
					
					
			
		} catch(Exception e) {}
			
	
		

		return null;
		
	}

}


class USPSParsedElements {

	private String statusCode;
	private String statusMessage;
	private String error = "";
	private String errorCode = "";
	private List<ShippingOption> options = new ArrayList<ShippingOption>();

	public void addOption(ShippingOption option) {
		options.add(option);
	}

	public List getOptions() {
		return options;
	}

	public String getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(String statusCode) {
		this.statusCode = statusCode;
	}

	public String getStatusMessage() {
		return statusMessage;
	}

	public void setStatusMessage(String statusMessage) {
		this.statusMessage = statusMessage;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

}
