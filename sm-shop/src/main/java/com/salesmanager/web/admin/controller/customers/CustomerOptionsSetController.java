package com.salesmanager.web.admin.controller.customers;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
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

import com.salesmanager.core.business.customer.model.attribute.CustomerOption;
import com.salesmanager.core.business.customer.model.attribute.CustomerOptionDescription;
import com.salesmanager.core.business.customer.model.attribute.CustomerOptionSet;
import com.salesmanager.core.business.customer.model.attribute.CustomerOptionValue;
import com.salesmanager.core.business.customer.model.attribute.CustomerOptionValueDescription;
import com.salesmanager.core.business.customer.service.attribute.CustomerOptionService;
import com.salesmanager.core.business.customer.service.attribute.CustomerOptionValueService;
import com.salesmanager.core.business.merchant.model.MerchantStore;
import com.salesmanager.core.business.reference.language.model.Language;
import com.salesmanager.core.business.reference.language.service.LanguageService;
import com.salesmanager.core.utils.ajax.AjaxResponse;
import com.salesmanager.web.admin.controller.ControllerConstants;
import com.salesmanager.web.admin.entity.web.Menu;
import com.salesmanager.web.constants.Constants;
import com.salesmanager.web.utils.LabelUtils;

@Controller
public class CustomerOptionsSetController {
	
	@Autowired
	private LanguageService languageService;
	
	@Autowired
	private CustomerOptionService customerOptionService;
	
	@Autowired
	private CustomerOptionValueService customerOptionValueService;
	
	@Autowired
	private LabelUtils messages;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(CustomerOptionsSetController.class);
	
	
	@Secured("CUSTOMER")
	@RequestMapping(value="/admin/customers/optionsset/list.html", method=RequestMethod.GET)
	public String displayOptions(Model model, HttpServletRequest request, HttpServletResponse response, Locale locale) throws Exception {
		
		Language language = languageService.toLanguage(locale);
		
		
		this.setMenu(model, request);
		MerchantStore store = (MerchantStore)request.getAttribute(Constants.ADMIN_STORE);

		
		//get options 
		List<CustomerOption> options = customerOptionService.listByStore(store, language);
		
		
		//get values
		List<CustomerOptionValue> optionsValues = customerOptionValueService.listByStore(store, language);

		
		CustomerOptionSet optionSet = new CustomerOptionSet();
		
		model.addAttribute("optionSet", optionSet);
		model.addAttribute("options", options);
		model.addAttribute("optionsValues", optionsValues);
		return ControllerConstants.Tiles.Customer.optionsSet;
		

	}
	
	
	@Secured("CUSTOMER")
	@RequestMapping(value="/admin/customers/optionsset/save.html", method=RequestMethod.POST)
	public String saveOption(@Valid @ModelAttribute("optionSet") CustomerOptionSet optionSet, BindingResult result, Model model, HttpServletRequest request, Locale locale) throws Exception {
		

		//display menu
		setMenu(model,request);
		
		Language language = languageService.toLanguage(locale);
		
		MerchantStore store = (MerchantStore)request.getAttribute(Constants.ADMIN_STORE);
		
		
		/** reference objects **/
		
		//get options 
		List<CustomerOption> options = customerOptionService.listByStore(store, language);
		
		
		//get values
		List<CustomerOptionValue> optionsValues = customerOptionValueService.listByStore(store, language);


		model.addAttribute("options", options);
		model.addAttribute("optionsValues", optionsValues);

		
		
		//see if association already exist
		CustomerOption option =	null;	

		//get from DB
		option = customerOptionService.getById(optionSet.getPk().getCustomerOption().getId());
			
		if(option==null) {
				return "redirect:/admin/customers/optionsset/list.html";
		}

		CustomerOptionValue optionValue = customerOptionValueService.getById(optionSet.getPk().getCustomerOptionValue().getId());
			
		if(optionValue==null) {
			return "redirect:/admin/customers/optionsset/list.html";
		}
		
		
		List<CustomerOptionSet> optionsSet = customerOptionService.listCustomerOptionSetByStore(store, language);
		
		if(optionsSet!=null && optionsSet.size()>0) {
			
			for(CustomerOptionSet optSet : optionsSet) {
				
				CustomerOption opt = optSet.getPk().getCustomerOption();
				CustomerOptionValue optValue = optSet.getPk().getCustomerOptionValue();
				
				if(opt.getId().longValue()==optionSet.getPk().getCustomerOption().getId().longValue() 
						&& optValue.getId().longValue() == optionSet.getPk().getCustomerOptionValue().getId().longValue()) {
						model.addAttribute("errorMessage",messages.getMessage("message.region.null", locale));
						ObjectError error = new ObjectError("region",messages.getMessage("message.region.exists", locale));
						result.addError(error);
						break;
				}
			}
		}
		
		if (result.hasErrors()) {
			return ControllerConstants.Tiles.Customer.optionsSet;
		}
		
		
		optionSet.getPk().setCustomerOption(option);
		optionSet.getPk().setCustomerOptionValue(optionValue);
		customerOptionService.addCustomerOptionSet(optionSet, option);

		


		model.addAttribute("success","success");
		return ControllerConstants.Tiles.Customer.optionsSet;
	}

	
	
	@SuppressWarnings("unchecked")
	@Secured("CUSTOMER")
	@RequestMapping(value="/admin/customers/optionsset/paging.html", method=RequestMethod.POST, produces="application/json")
	public @ResponseBody String pageOptions(HttpServletRequest request, HttpServletResponse response) {

		AjaxResponse resp = new AjaxResponse();

		
		try {
			
			
			Language language = (Language)request.getAttribute("LANGUAGE");	
			MerchantStore store = (MerchantStore)request.getAttribute(Constants.ADMIN_STORE);
			List<CustomerOption> options = null;
				
			List<CustomerOptionSet> optionSet = customerOptionService.listCustomerOptionSetByStore(store, language);
			//for(CustomerOption option : options) {
				
				
				//Set<CustomerOptionSet> optionSet = option.getCustomerOptions();
				
				if(optionSet!=null && optionSet.size()>0) {
					
					for(CustomerOptionSet optSet : optionSet) {
						
						CustomerOption customerOption = optSet.getPk().getCustomerOption();
						CustomerOptionValue customerOptionValue = optSet.getPk().getCustomerOptionValue();
						
						@SuppressWarnings("rawtypes")
						Map entry = new HashMap();
						entry.put("id", optSet.getId());
						
						CustomerOptionDescription description = customerOption.getDescriptionsList().get(0);
						CustomerOptionValueDescription valueDescription = customerOptionValue.getDescriptionsList().get(0);
						
						entry.put("optionCode", customerOption.getCode());
						entry.put("optionName", description.getName());
						entry.put("optionValueCode", customerOptionValue.getCode());
						entry.put("optionValueName", valueDescription.getName());
						entry.put("order", customerOption.getSortOrder());
						resp.addDataEntry(entry);
					
					}
				
				}
				
				
			//}
			
			resp.setStatus(AjaxResponse.RESPONSE_STATUS_SUCCESS);
			

		
		} catch (Exception e) {
			LOGGER.error("Error while paging options", e);
			resp.setStatus(AjaxResponse.RESPONSE_STATUS_FAIURE);
		}
		
		String returnString = resp.toJSONString();
		
		return returnString;
		
		
	}
	
	

	
	private void setMenu(Model model, HttpServletRequest request) throws Exception {
		
		//display menu
		Map<String,String> activeMenus = new HashMap<String,String>();
		activeMenus.put("customer", "customer");
		activeMenus.put("customer-options", "customer-options");
		
		@SuppressWarnings("unchecked")
		Map<String, Menu> menus = (Map<String, Menu>)request.getAttribute("MENUMAP");
		
		Menu currentMenu = (Menu)menus.get("customer");
		model.addAttribute("currentMenu",currentMenu);
		model.addAttribute("activeMenus",activeMenus);
		//
		
	}
	
	@Secured("CUSTOMER")
	@RequestMapping(value="/admin/customers/optionsset/remove.html", method=RequestMethod.POST, produces="application/json")
	public @ResponseBody String deleteOption(HttpServletRequest request, HttpServletResponse response, Locale locale) {
		String sid = request.getParameter("id");

		MerchantStore store = (MerchantStore)request.getAttribute(Constants.ADMIN_STORE);
		AjaxResponse resp = new AjaxResponse();

		
		try {
			
			Long id = Long.parseLong(sid);
			
			CustomerOptionSet entity = customerOptionService.getCustomerOptionSetById(id);
			if(entity==null || entity.getPk().getCustomerOption().getMerchantStore().getId().intValue()!=store.getId().intValue()) {

				resp.setStatusMessage(messages.getMessage("message.unauthorized", locale));
				resp.setStatus(AjaxResponse.RESPONSE_STATUS_FAIURE);			
				
			} else {
				
				customerOptionService.removeCustomerOptionSet(entity);
				resp.setStatus(AjaxResponse.RESPONSE_OPERATION_COMPLETED);
				
			}
		
		
		} catch (Exception e) {
			LOGGER.error("Error while deleting option", e);
			resp.setStatus(AjaxResponse.RESPONSE_STATUS_FAIURE);
			resp.setErrorMessage(e);
		}
		
		String returnString = resp.toJSONString();
		
		return returnString;
	}
	
	

	@Secured("CUSTOMER")
	@RequestMapping(value="/admin/customers/optionsset/update.html", method=RequestMethod.POST, produces="application/json")
	public @ResponseBody String updateCountry(HttpServletRequest request, HttpServletResponse response) {
		String values = request.getParameter("_oldValues");
		String order = request.getParameter("order");

		AjaxResponse resp = new AjaxResponse();

		try {
			
			/**
			 * Values
			 */
			ObjectMapper mapper = new ObjectMapper();
			@SuppressWarnings("rawtypes")
			Map conf = mapper.readValue(values, Map.class);
			
			String sid = (String)conf.get("id");

			Long id = Long.parseLong(sid);
			
			CustomerOptionSet entity = customerOptionService.getCustomerOptionSetById(id);
			
			
			if(entity!=null) {
				
				entity.setSortOrder(Integer.parseInt(order));
				customerOptionService.updateCustomerOptionSet(entity);
				resp.setStatus(AjaxResponse.RESPONSE_OPERATION_COMPLETED);
				
			}

		
		} catch (Exception e) {
			LOGGER.error("Error while paging shipping countries", e);
			resp.setStatus(AjaxResponse.RESPONSE_STATUS_FAIURE);
		}
		
		String returnString = resp.toJSONString();
		
		return returnString;
	}
	
	

}
