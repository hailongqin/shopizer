package com.salesmanager.web.shop.controller.product;

import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.salesmanager.core.business.catalog.category.model.Category;
import com.salesmanager.core.business.catalog.product.model.Product;
import com.salesmanager.core.business.catalog.product.service.ProductService;
import com.salesmanager.core.business.merchant.model.MerchantStore;
import com.salesmanager.core.business.reference.language.model.Language;
import com.salesmanager.core.utils.CacheUtils;
import com.salesmanager.web.constants.Constants;
import com.salesmanager.web.entity.shop.PageInformation;
import com.salesmanager.web.shop.controller.ControllerConstants;
import com.salesmanager.web.utils.CatalogUtils;

@Controller
public class ShopProductController {
	
	@Autowired
	ProductService productService;
	
	@Autowired
	CatalogUtils catalogUtils;
	
	

	@RequestMapping("/shop/product/{friendlyUrl}.html")
	public String displayCategory(@PathVariable final String friendlyUrl, Model model, HttpServletRequest request, HttpServletResponse response, Locale locale) throws Exception {
		
		
		
		
		MerchantStore store = (MerchantStore)request.getAttribute(Constants.MERCHANT_STORE);

		Language language = (Language)request.getAttribute("LANGUAGE");
		
		Product product = productService.getBySeUrl(store, friendlyUrl, locale);
		
		if(product==null) {
			//TODO product is not found page
			return "redirect:/shop";
		}
		
		com.salesmanager.web.entity.catalog.Product productProxy = catalogUtils.buildProxyProduct(product, store, locale);
		

		//meta information
		PageInformation pageInformation = new PageInformation();
		pageInformation.setPageDescription(productProxy.getMetaDescription());
		pageInformation.setPageKeywords(productProxy.getKeyWords());
		pageInformation.setPageTitle(productProxy.getTitle());
		pageInformation.setPageUrl(productProxy.getFriendlyUrl());
		
		request.setAttribute(Constants.REQUEST_PAGE_INFORMATION, pageInformation);
		
		//related items
		
		//reviews
		
		
			
		model.addAttribute("product", product);

		
		/** template **/
		StringBuilder template = new StringBuilder().append(ControllerConstants.Tiles.Product.product).append(".").append(store.getStoreTemplate());

		return template.toString();
	}

}
