package com.salesmanager.web.shop.controller.customer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.salesmanager.core.business.customer.model.Customer;
import com.salesmanager.core.business.customer.service.CustomerService;
import com.salesmanager.core.business.merchant.model.MerchantStore;
import com.salesmanager.core.business.merchant.service.MerchantStoreService;
import com.salesmanager.core.business.reference.country.model.Country;
import com.salesmanager.core.business.reference.country.service.CountryService;
import com.salesmanager.core.business.reference.language.model.Language;
import com.salesmanager.core.business.reference.language.service.LanguageService;
import com.salesmanager.core.business.reference.zone.service.ZoneService;
import com.salesmanager.web.constants.Constants;
import com.salesmanager.web.shop.controller.category.ShoppingCategoryController;
import com.salesmanager.web.utils.CatalogUtils;
import com.salesmanager.web.utils.LocaleUtils;

@Controller
@RequestMapping("/shop/services/customers")
public class CustomerRESTController {

	@Autowired
	private CustomerService customerService;
	
	@Autowired
	private MerchantStoreService merchantStoreService;
	
	@Autowired
	private LanguageService languageService;
	
	@Autowired
	private CatalogUtils catalogUtils;
	
	@Autowired
	private CountryService countryService;
	
	@Autowired
	private ZoneService zoneService;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ShoppingCategoryController.class);
	
	
	/**
	 * Returns a single customer for a given MerchantStore
	 */
	@RequestMapping( value="/{store}/{language}/{id}", method=RequestMethod.GET)
	@ResponseBody
	public com.salesmanager.web.entity.customer.Customer getCustomer(@PathVariable final String store, @PathVariable final String language, @PathVariable Long id, Model model, HttpServletRequest request, HttpServletResponse response) throws Exception {
		Map<String,Language> langs = languageService.getLanguagesMap();
		Language lang = langs.get(language);
		if(lang==null) {
			lang = languageService.getByCode(Constants.DEFAULT_LANGUAGE);
		}
		
		MerchantStore merchantStore = (MerchantStore)request.getAttribute(Constants.MERCHANT_STORE);
		if(merchantStore!=null) {
			if(!merchantStore.getCode().equals(store)) {
				merchantStore = null;
			}
		}
		
		if(merchantStore== null) {
			merchantStore = merchantStoreService.getByCode(store);
		}
		
		if(merchantStore==null) {
			LOGGER.error("Merchant store is null for code " + store);
			response.sendError(503, "Merchant store is null for code " + store);
			return null;
		}
		
		Customer customer = customerService.getById(id);
		com.salesmanager.web.entity.customer.Customer customerProxy;
		if(customer != null){
			customerProxy = catalogUtils.buildProxyCustomer(customer, merchantStore, LocaleUtils.getLocale(lang));
		}else{
			response.sendError(404, "No Customer found with id : " + id);
			return null;
		}
		
		
		return customerProxy;
	}
	
	
	/**
	 * Returns all customers for a given MerchantStore
	 */
	@RequestMapping( value="/{store}/{language}", method=RequestMethod.GET)
	@ResponseBody
	public List<com.salesmanager.web.entity.customer.Customer> getCustomers(@PathVariable final String store, @PathVariable final String language, Model model, HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		Map<String,Language> langs = languageService.getLanguagesMap();
		Language lang = langs.get(language);
		if(lang==null) {
			lang = languageService.getByCode(Constants.DEFAULT_LANGUAGE);
		}
		
		MerchantStore merchantStore = (MerchantStore)request.getAttribute(Constants.MERCHANT_STORE);
		if(merchantStore!=null) {
			if(!merchantStore.getCode().equals(store)) {
				merchantStore = null;
			}
		}
		
		if(merchantStore== null) {
			merchantStore = merchantStoreService.getByCode(store);
		}
		
		if(merchantStore==null) {
			LOGGER.error("Merchant store is null for code " + store);
			response.sendError(503, "Merchant store is null for code " + store);
			return null;
		}
		
		List<Customer> customers = customerService.listByStore(merchantStore);
		List<com.salesmanager.web.entity.customer.Customer> returnCustomers = new ArrayList<com.salesmanager.web.entity.customer.Customer>();
		for(Customer customer : customers) {
			com.salesmanager.web.entity.customer.Customer customerProxy = catalogUtils.buildProxyCustomer(customer, merchantStore, LocaleUtils.getLocale(lang));
			returnCustomers.add(customerProxy);
		}
		
		return returnCustomers;
	}
	
	
	/**
	 * Updates a customer for a given MerchantStore
	 */
	@RequestMapping( value="/{store}/{language}/{id}", method=RequestMethod.PUT)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Secured("CUSTOMER")
	public void updateCustomer(@PathVariable final String store, @PathVariable final String language, @PathVariable Long id, @Valid @RequestBody Customer customer, Model model, HttpServletRequest request, HttpServletResponse response) throws Exception {
		Map<String,Language> langs = languageService.getLanguagesMap();
		Language lang = langs.get(language);
		if(lang==null) {
			lang = languageService.getByCode(Constants.DEFAULT_LANGUAGE);
		}
		
		MerchantStore merchantStore = (MerchantStore)request.getAttribute(Constants.MERCHANT_STORE);
		if(merchantStore!=null) {
			if(!merchantStore.getCode().equals(store)) {
				merchantStore = null;
			}
		}
		
		if(merchantStore== null) {
			merchantStore = merchantStoreService.getByCode(store);
		}
		
		if(merchantStore==null) {
			LOGGER.error("Merchant store is null for code " + store);
			response.sendError(503, "Merchant store is null for code " + store);
		}
		
		Customer oldCustomer = customerService.getById(id);
		if(oldCustomer != null){
			customer.setId(id);
			Country country = (customer.getCountry() != null)?countryService.getByCode(customer.getCountry().getIsoCode().toUpperCase()):null;
			if(country != null){
				customer.setCountry(country);
			}
			
			Country billCountry = (customer.getBilling() != null)?countryService.getByCode(customer.getBilling().getCountry().getIsoCode().toUpperCase()):null;
			if(billCountry != null){
				customer.getBilling().setCountry(billCountry);
			}
			
			Country delCountry = (customer.getDelivery() != null)?countryService.getByCode(customer.getDelivery().getCountry().getIsoCode().toUpperCase()):null;
			if(delCountry != null){
				customer.getDelivery().setCountry(delCountry);
			}
			
			if(customer.getZone() != null){
				customer.setZone(zoneService.getByCode(customer.getZone().getCountry().getIsoCode().toUpperCase()));
				Country zoneCountry = countryService.getByCode(customer.getZone().getCountry().getIsoCode().toUpperCase());
				if(zoneCountry != null){
					customer.getZone().setCountry(zoneCountry);
				}
			}
			
			customer.setMerchantStore(merchantStore);
			customer.setDefaultLanguage(lang);
			
			customerService.saveOrUpdate(customer);
		}else{
			response.sendError(404, "No Customer found for ID : " + id);
		}
	}
	
	
	/**
	 * Deletes a customer for a given MerchantStore
	 */
	@RequestMapping( value="/{store}/{language}/{id}", method=RequestMethod.DELETE)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Secured("CUSTOMER")
	public void deleteCustomer(@PathVariable final String store, @PathVariable final String language, @PathVariable Long id, HttpServletRequest request, HttpServletResponse response) throws Exception {
		boolean isDeleted = customerService.deleteById(id);
	    if(!isDeleted){
	        response.sendError(404, "No Customer found for ID : " + id);
	    }
	    
	    // Due to some transaction issue below code is not working, hence moved retrieve and delete logic to a single service method
		/*Customer customer = customerService.getById(id);
		if(customer != null && customer.getMerchantStore().getCode().equalsIgnoreCase(store)){
			customerService.delete(customer);
		}else{
			response.sendError(404, "No Customer found for ID : " + id);
		}*/
	}
	
	
	/**
	 * Create new customer for a given MerchantStore
	 */
	@RequestMapping( value="/{store}/{language}", method=RequestMethod.POST)
	@ResponseStatus(HttpStatus.CREATED)
	@Secured("CUSTOMER")
	@ResponseBody
	public com.salesmanager.web.entity.customer.Customer createCustomer(@PathVariable final String store, @PathVariable final String language, @Valid @RequestBody Customer customer, Model model, HttpServletRequest request, HttpServletResponse response) throws Exception {
		Map<String,Language> langs = languageService.getLanguagesMap();
		Language lang = langs.get(language);
		if(lang==null) {
			lang = languageService.getByCode(Constants.DEFAULT_LANGUAGE);
		}
		
		MerchantStore merchantStore = (MerchantStore)request.getAttribute(Constants.MERCHANT_STORE);
		if(merchantStore!=null) {
			if(!merchantStore.getCode().equals(store)) {
				merchantStore = null;
			}
		}
		
		if(merchantStore== null) {
			merchantStore = merchantStoreService.getByCode(store);
		}
		
		if(merchantStore==null) {
			LOGGER.error("Merchant store is null for code " + store);
			response.sendError(503, "Merchant store is null for code " + store);
			return null;
		}
		
		customer.setMerchantStore(merchantStore);
		customer.setDefaultLanguage(lang);
		
		Country country = (customer.getCountry() != null)?countryService.getByCode(customer.getCountry().getIsoCode().toUpperCase()):null;
		if(country != null){
			customer.setCountry(country);
		}else{
			response.sendError(503, "Customer Country is a amandatory field!");
			return null;
		}
		
		Country billCountry = (customer.getBilling() != null)?countryService.getByCode(customer.getBilling().getCountry().getIsoCode().toUpperCase()):null;
		if(billCountry != null){
			customer.getBilling().setCountry(billCountry);
		}else{
			response.sendError(503, "Billing Country is a amandatory field!");
			return null;
		}
		
		Country delCountry = (customer.getDelivery() != null)?countryService.getByCode(customer.getDelivery().getCountry().getIsoCode().toUpperCase()):null;
		if(delCountry != null){
			customer.getDelivery().setCountry(delCountry);
		}else{
			response.sendError(503, "Delivery Country is a amandatory field!");
			return null;
		}
		
		if(customer.getZone() != null){
			customer.setZone(zoneService.getByCode(customer.getZone().getCountry().getIsoCode().toUpperCase()));
			Country zoneCountry = countryService.getByCode(customer.getZone().getCountry().getIsoCode().toUpperCase());
			if(zoneCountry != null){
				customer.getZone().setCountry(zoneCountry);
			}
		}
		
		customerService.saveOrUpdate(customer);
		
		com.salesmanager.web.entity.customer.Customer customerProxy = catalogUtils.buildProxyCustomer(customer, merchantStore, LocaleUtils.getLocale(lang));
		return customerProxy;
	}
	
}
