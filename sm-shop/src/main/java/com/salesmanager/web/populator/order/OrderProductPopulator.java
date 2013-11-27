package com.salesmanager.web.populator.order;

import org.apache.commons.lang.Validate;

import com.salesmanager.core.business.catalog.product.model.Product;
import com.salesmanager.core.business.catalog.product.model.file.DigitalProduct;
import com.salesmanager.core.business.catalog.product.model.price.FinalPrice;
import com.salesmanager.core.business.catalog.product.model.price.ProductPrice;
import com.salesmanager.core.business.catalog.product.service.ProductService;
import com.salesmanager.core.business.catalog.product.service.file.DigitalProductService;
import com.salesmanager.core.business.generic.exception.ConversionException;
import com.salesmanager.core.business.generic.exception.ServiceException;
import com.salesmanager.core.business.merchant.model.MerchantStore;
import com.salesmanager.core.business.order.model.orderproduct.OrderProduct;
import com.salesmanager.core.business.order.model.orderproduct.OrderProductDownload;
import com.salesmanager.core.business.order.model.orderproduct.OrderProductPrice;
import com.salesmanager.core.business.reference.language.model.Language;
import com.salesmanager.core.business.shoppingcart.model.ShoppingCartItem;
import com.salesmanager.core.utils.AbstractDataPopulator;

public class OrderProductPopulator extends
		AbstractDataPopulator<ShoppingCartItem, OrderProduct> {
	
	private ProductService productService;
	private DigitalProductService digitalProductService;


	public DigitalProductService getDigitalProductService() {
		return digitalProductService;
	}

	public void setDigitalProductService(DigitalProductService digitalProductService) {
		this.digitalProductService = digitalProductService;
	}

	/**
	 * Converts a ShoppingCartItem carried in the ShoppingCart to an OrderProduct
	 * that will be saved in the system
	 */
	@Override
	public OrderProduct populate(ShoppingCartItem source, OrderProduct target,
			MerchantStore store, Language language) throws ConversionException {
		
		Validate.notNull(productService,"productService must be set");
		Validate.notNull(digitalProductService,"digitalProductService must be set");

		
		try {
			Product modelProduct = productService.getById(source.getProductId());
			if(modelProduct==null) {
				throw new ConversionException("Cannot get product with id (productId) " + source.getProductId());
			}
			
			if(modelProduct.getMerchantStore().getId().intValue()!=store.getId().intValue()) {
				throw new ConversionException("Invalid product id " + source.getProductId());
			}
		
		
			DigitalProduct digitalProduct = digitalProductService.getByProduct(store, modelProduct);
			
			if(digitalProduct!=null) {
				OrderProductDownload orderProductDownload = new OrderProductDownload();	
				orderProductDownload.setOrderProductFilename(digitalProduct.getProductFileName());
				orderProductDownload.setOrderProduct(target);
				target.getDownloads().add(orderProductDownload);
			}
			

	
			target.setOneTimeCharge(source.getItemPrice());	
			target.setProductName(source.getProduct().getDescriptions().iterator().next().getName());
			target.setProductQuantity(source.getQuantity());
			target.setSku(source.getProduct().getSku());
			
			FinalPrice finalPrice = source.getFinalPrice();
			if(finalPrice==null) {
				throw new ConversionException("Object final price not populated in shoppingCartItem (source)");
			}
			ProductPrice productPrice = finalPrice.getProductPrice();
			OrderProductPrice orderProductPrice = new OrderProductPrice();
			orderProductPrice.setDefaultPrice(productPrice.isDefaultPrice());
			orderProductPrice.setOrderProduct(target);
			orderProductPrice.setProductPrice(source.getItemPrice());
			orderProductPrice.setProductPriceCode(productPrice.getCode());
			orderProductPrice.setProductPriceName(productPrice.getDescriptions().iterator().next().getName());
			if(finalPrice.isDiscounted()) {
				orderProductPrice.setProductPriceSpecial(productPrice.getProductPriceSpecialAmount());
				orderProductPrice.setProductPriceSpecialStartDate(productPrice.getProductPriceSpecialStartDate());
				orderProductPrice.setProductPriceSpecialEndDate(productPrice.getProductPriceSpecialEndDate());
			}


			//target.getPrices().add(oproductprice ) ;
			
			//oproductprice.setOrderProduct(oproduct);		
			//orderProductDownload.setOrderProduct(oproduct);
			//order.getOrderProducts().add(oproduct);
			
		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return null;
	}

	@Override
	protected OrderProduct createTarget() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setProductService(ProductService productService) {
		this.productService = productService;
	}

	public ProductService getProductService() {
		return productService;
	}


}
