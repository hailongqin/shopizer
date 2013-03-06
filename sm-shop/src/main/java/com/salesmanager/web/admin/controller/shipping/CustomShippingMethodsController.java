package com.salesmanager.web.admin.controller.shipping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.salesmanager.core.business.merchant.model.MerchantStore;
import com.salesmanager.core.business.reference.country.model.Country;
import com.salesmanager.core.business.reference.country.service.CountryService;
import com.salesmanager.core.business.reference.language.model.Language;
import com.salesmanager.core.business.shipping.model.ShippingConfiguration;
import com.salesmanager.core.business.shipping.model.ShippingType;
import com.salesmanager.core.business.shipping.service.ShippingService;
import com.salesmanager.core.business.system.model.IntegrationConfiguration;
import com.salesmanager.core.modules.integration.IntegrationException;
import com.salesmanager.core.modules.integration.shipping.model.CustomShippingQuotesConfiguration;
import com.salesmanager.core.modules.integration.shipping.model.CustomShippingQuotesRegion;
import com.salesmanager.core.utils.ajax.AjaxResponse;
import com.salesmanager.web.admin.controller.ControllerConstants;
import com.salesmanager.web.admin.entity.web.Menu;
import com.salesmanager.web.constants.Constants;
import com.salesmanager.web.utils.LabelUtils;

@Controller
public class CustomShippingMethodsController {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(CustomShippingMethodsController.class);
	

	public final static String WEIGHT_BASED_SHIPPING_METHOD = "weightBased";
	
	@Autowired
	private ShippingService shippingService;
	
	@Autowired
	private CountryService countryService;
	
	@Autowired
	LabelUtils messages;
	

	@Secured("SHIPPING")
	@RequestMapping(value="/admin/shipping/weightBased.html", method=RequestMethod.GET)
	public String getWeightBasedShippingMethod(Model model, HttpServletRequest request, HttpServletResponse response) throws Exception {


		this.setMenu(model, request);

		populateModel(model, request, response);

		return ControllerConstants.Tiles.Shipping.shippingMethod;
		
		
	}
	
	
	@Secured("SHIPPING")
	@RequestMapping(value="/admin/shipping/addCustomRegion.html", method=RequestMethod.POST)
	public String addCustomRegion(@ModelAttribute("region") String region, BindingResult result, Model model, HttpServletRequest request, HttpServletResponse response, Locale locale) throws Exception {

		this.setMenu(model, request);
		populateModel(model, request, response);
		MerchantStore store = (MerchantStore)request.getAttribute(Constants.ADMIN_STORE);
		CustomShippingQuotesConfiguration customConfiguration = (CustomShippingQuotesConfiguration)shippingService.getCustomShippingConfiguration(WEIGHT_BASED_SHIPPING_METHOD, store);

		List<CustomShippingQuotesRegion> regions = customConfiguration.getRegions();
		
		
		for(CustomShippingQuotesRegion customRegion : regions) {
			if(customRegion.equals(region)) {
				ObjectError error = new ObjectError("region",messages.getMessage("message.region.exists", locale));
				result.addError(error);
				break;
			}
		}
		
		if (result.hasErrors()) {
			return ControllerConstants.Tiles.Shipping.shippingMethod;
		}
		
		
		CustomShippingQuotesRegion quoteRegion = new CustomShippingQuotesRegion();
		quoteRegion.setCustomRegionName(region);
		
		model.addAttribute("customConfiguration", customConfiguration);
		model.addAttribute("success","success");
		
		return ControllerConstants.Tiles.Shipping.shippingMethod;
	
	}
	
	@Secured("SHIPPING")
	@RequestMapping(value="/admin/shipping/addCountryToRegion.html", method=RequestMethod.POST)
	public String addCountryToCustomRegion(@ModelAttribute("customRegion") CustomShippingQuotesRegion customRegion, BindingResult result, Model model, HttpServletRequest request, HttpServletResponse response, Locale locale) throws Exception {

		this.setMenu(model, request);
		populateModel(model, request, response);
		MerchantStore store = (MerchantStore)request.getAttribute(Constants.ADMIN_STORE);
		CustomShippingQuotesConfiguration customConfiguration = (CustomShippingQuotesConfiguration)shippingService.getCustomShippingConfiguration(WEIGHT_BASED_SHIPPING_METHOD, store);

		List<CustomShippingQuotesRegion> regions = customConfiguration.getRegions();
		
		
		for(CustomShippingQuotesRegion region : regions) {
			if(region.equals(customRegion)) {
				ObjectError error = new ObjectError("region",messages.getMessage("mmessage.region.exists", locale));
				result.addError(error);
				break;
			}
		}
		
		if (result.hasErrors()) {
			return ControllerConstants.Tiles.Shipping.shippingMethod;
		}
		
		regions.add(customRegion);
		model.addAttribute("customConfiguration", customConfiguration);
		model.addAttribute("success","success");
		
		return ControllerConstants.Tiles.Shipping.shippingMethod;
	
	}
	
	@Secured("SHIPPING")
	@RequestMapping(value="/admin/shipping/saveWeightBasedShippingMethod.html", method=RequestMethod.POST)
	public String saveShippingMethod(@ModelAttribute("configuration") CustomShippingQuotesConfiguration configuration, BindingResult result, Model model, HttpServletRequest request, HttpServletResponse response, Locale locale) throws Exception {


		this.setMenu(model, request);
		MerchantStore store = (MerchantStore)request.getAttribute(Constants.ADMIN_STORE);
		
		String moduleCode = configuration.getModuleCode();
		LOGGER.debug("Saving module code " + moduleCode);
		
		List<String> environments = new ArrayList<String>();
		environments.add(Constants.TEST_ENVIRONMENT);
		environments.add(Constants.PRODUCTION_ENVIRONMENT);

		model.addAttribute("environments", environments);
		model.addAttribute("configuration", configuration);

		try {
			shippingService.saveShippingQuoteModuleConfiguration(configuration, store);
			//TODO
			shippingService.saveCustomShippingConfiguration(WEIGHT_BASED_SHIPPING_METHOD, configuration, store);

			
			
		} catch (Exception e) {
			if(e instanceof IntegrationException) {
				if(((IntegrationException)e).getErrorCode()==IntegrationException.ERROR_VALIDATION_SAVE) {
					
					List<String> errorCodes = ((IntegrationException)e).getErrorFields();
					for(String errorCode : errorCodes) {
						model.addAttribute(errorCode,messages.getMessage("message.fielderror", locale));
					}
					return ControllerConstants.Tiles.Shipping.shippingMethod;
				}
			} else {
				throw new Exception(e);
			}
		}
		

		model.addAttribute("success","success");
		return ControllerConstants.Tiles.Shipping.shippingMethod;
		
		
	}
	

	@Secured("SHIPPING")
	@RequestMapping(value="/admin/shipping/deleteWeightBasedShippingMethod.html", method=RequestMethod.POST)
	public String deleteShippingMethod(BindingResult result, Model model, HttpServletRequest request, HttpServletResponse response, Locale locale) throws Exception {
		
		this.setMenu(model, request);
		MerchantStore store = (MerchantStore)request.getAttribute(Constants.ADMIN_STORE);
		shippingService.removeCustomShippingQuoteModuleConfiguration(WEIGHT_BASED_SHIPPING_METHOD, store);
		
		
		return ControllerConstants.Tiles.Shipping.shippingMethods;
		
	}
	
	/**
	 * Check if a region code already exist with the same name
	 * @param request
	 * @param response
	 * @param locale
	 * @return
	 */
	@Secured("SHIPPING")
	@RequestMapping(value="/admin/shipping/checkRegionCode.html", method=RequestMethod.POST, produces="application/json")
	public @ResponseBody String checkRegionCode(HttpServletRequest request, HttpServletResponse response, Locale locale) {
		String code = request.getParameter("code");


		AjaxResponse resp = new AjaxResponse();
		MerchantStore store = (MerchantStore)request.getAttribute(Constants.ADMIN_STORE);
		
		try {
			
			if(StringUtils.isBlank(code)) {
				resp.setStatus(AjaxResponse.CODE_ALREADY_EXIST);
				return resp.toJSONString();
			}
			
			CustomShippingQuotesConfiguration customConfiguration = (CustomShippingQuotesConfiguration)shippingService.getCustomShippingConfiguration(WEIGHT_BASED_SHIPPING_METHOD, store);

			if(customConfiguration!=null) {
				List<CustomShippingQuotesRegion> regions =  customConfiguration.getRegions();
				for(CustomShippingQuotesRegion region : regions) {
					
					if(code.equals(region.getCustomRegionName())) {
						resp.setStatus(AjaxResponse.CODE_ALREADY_EXIST);
						return resp.toJSONString();
					}
					
				}
			}

			resp.setStatus(AjaxResponse.RESPONSE_OPERATION_COMPLETED);

		} catch (Exception e) {
			LOGGER.error("Error while getting user", e);
			resp.setStatus(AjaxResponse.RESPONSE_STATUS_FAIURE);
			resp.setErrorMessage(e);
		}
		
		String returnString = resp.toJSONString();
		
		return returnString;
	}
	
	@Secured("SHIPPING")
	@RequestMapping(value = "/admin/shipping/weightBased/page.html", method = RequestMethod.POST, produces = "application/json")
	public @ResponseBody
	String pageStores(HttpServletRequest request,
			HttpServletResponse response) {

		AjaxResponse resp = new AjaxResponse();


		try {
			MerchantStore store = (MerchantStore)request.getAttribute(Constants.ADMIN_STORE);
			CustomShippingQuotesConfiguration customConfiguration = (CustomShippingQuotesConfiguration)shippingService.getCustomShippingConfiguration(WEIGHT_BASED_SHIPPING_METHOD, store);

			List<CustomShippingQuotesRegion> quotes = customConfiguration.getRegions();
			for (CustomShippingQuotesRegion quote : quotes) {
					List<String> countries = quote.getCountries();
					for(String country : countries) {
						Map<String,String> entry = new HashMap<String,String> ();
						entry.put("region", quote.getCustomRegionName());
						entry.put("country", country);
						resp.addDataEntry(entry);
					}
			}

			resp.setStatus(AjaxResponse.RESPONSE_STATUS_SUCCESS);

		} catch (Exception e) {
			LOGGER.error("Error while paging products", e);
			resp.setStatus(AjaxResponse.RESPONSE_STATUS_FAIURE);
		}

		String returnString = resp.toJSONString();

		return returnString;
	}
	
	/**
	 * Edit custom region
	 * @param region
	 * @param model
	 * @param request
	 * @param response
	 * @param locale
	 * @return
	 * @throws Exception
	 */
	@Secured("SHIPPING")
	@RequestMapping(value="/admin/shipping/weightBased/edit.html", method=RequestMethod.GET)
	public String displayMerchantStore(@ModelAttribute("region") String region, Model model, HttpServletRequest request, HttpServletResponse response, Locale locale) throws Exception {
		
		setMenu(model,request);
		MerchantStore store = (MerchantStore)request.getAttribute(Constants.ADMIN_STORE);
		CustomShippingQuotesConfiguration customConfiguration = (CustomShippingQuotesConfiguration)shippingService.getCustomShippingConfiguration(WEIGHT_BASED_SHIPPING_METHOD, store);
		CustomShippingQuotesRegion aRegion = null;

		List<CustomShippingQuotesRegion> regions = customConfiguration.getRegions();
		for(CustomShippingQuotesRegion customRegion : regions) {
			if(customRegion.getCustomRegionName().equals(region)) {
				aRegion = customRegion;
				break;
			}
		}
		
		model.addAttribute("customRegion", aRegion);


		return ControllerConstants.Tiles.Shipping.customShippingWeightBased;
	}
	

	private void populateModel(Model model, HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		
		MerchantStore store = (MerchantStore)request.getAttribute(Constants.ADMIN_STORE);
		Language language = (Language)request.getAttribute("LANGUAGE");
		
		ShippingConfiguration shippingConfiguration =  shippingService.getShippingConfiguration(store);
		
		if(shippingConfiguration==null) {
			shippingConfiguration = new ShippingConfiguration();
			shippingConfiguration.setShippingType(ShippingType.INTERNATIONAL);
		}
		

		//get configured shipping modules
		Map<String,IntegrationConfiguration> configuredModules = shippingService.getShippingModulesConfigured(store);
		IntegrationConfiguration configuration = new IntegrationConfiguration();
		if(configuredModules!=null) {
			for(String key : configuredModules.keySet()) {
				if(key.equals(WEIGHT_BASED_SHIPPING_METHOD)) {
					configuration = configuredModules.get(key);
					break;
				}
			}
		}
		configuration.setModuleCode(WEIGHT_BASED_SHIPPING_METHOD);
		
		//get custom information
		CustomShippingQuotesConfiguration customConfiguration = (CustomShippingQuotesConfiguration)shippingService.getCustomShippingConfiguration(WEIGHT_BASED_SHIPPING_METHOD, store);

		//get supported countries
		List<String> includedCountries = shippingService.getSupportedCountries(store);
		List<Country> shippingCountries = new ArrayList<Country>();
		if(shippingConfiguration.getShippingType().equals(ShippingType.INTERNATIONAL.name())){
			Map<String,Country> countries = countryService.getCountriesMap(language);
			for(String key : countries.keySet()) {
				Country country = (Country)countries.get(key);
				if(!includedCountries.contains(key)) {
					shippingCountries.add(country);
				}
			}
		} else {//if national only store country
			if(!includedCountries.contains(store.getCountry().getIsoCode())) {
				shippingCountries.add(store.getCountry());
			}
		}
		
		
		List<String> environments = new ArrayList<String>();
		environments.add(Constants.TEST_ENVIRONMENT);
		environments.add(Constants.PRODUCTION_ENVIRONMENT);
		
		model.addAttribute("configuration", configuration);
		model.addAttribute("customConfiguration", customConfiguration);
		model.addAttribute("shippingCountries", shippingCountries);

		
	}
	
	private void setMenu(Model model, HttpServletRequest request) throws Exception {
		
		//display menu
		Map<String,String> activeMenus = new HashMap<String,String>();
		activeMenus.put("shipping", "shipping");
		activeMenus.put("shipping-methods", "shipping-methods");
		
		@SuppressWarnings("unchecked")
		Map<String, Menu> menus = (Map<String, Menu>)request.getAttribute("MENUMAP");
		
		Menu currentMenu = (Menu)menus.get("shipping");
		model.addAttribute("currentMenu",currentMenu);
		model.addAttribute("activeMenus",activeMenus);
		//
		
	}


}