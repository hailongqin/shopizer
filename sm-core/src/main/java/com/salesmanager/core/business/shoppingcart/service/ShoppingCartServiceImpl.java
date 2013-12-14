package com.salesmanager.core.business.shoppingcart.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.salesmanager.core.business.catalog.product.model.Product;
import com.salesmanager.core.business.catalog.product.model.attribute.ProductAttribute;
import com.salesmanager.core.business.catalog.product.model.price.FinalPrice;
import com.salesmanager.core.business.catalog.product.service.PricingService;
import com.salesmanager.core.business.catalog.product.service.ProductService;
import com.salesmanager.core.business.customer.model.Customer;
import com.salesmanager.core.business.generic.exception.ServiceException;
import com.salesmanager.core.business.generic.service.SalesManagerEntityServiceImpl;
import com.salesmanager.core.business.merchant.model.MerchantStore;
import com.salesmanager.core.business.shipping.model.ShippingProduct;
import com.salesmanager.core.business.shoppingcart.dao.ShoppingCartDao;
import com.salesmanager.core.business.shoppingcart.dao.ShoppingCartItemDao;
import com.salesmanager.core.business.shoppingcart.model.ShoppingCart;
import com.salesmanager.core.business.shoppingcart.model.ShoppingCartAttributeItem;
import com.salesmanager.core.business.shoppingcart.model.ShoppingCartItem;

@Service("shoppingCartService")
public class ShoppingCartServiceImpl extends SalesManagerEntityServiceImpl<Long, ShoppingCart> implements ShoppingCartService {


	private ShoppingCartDao shoppingCartDao;
	
	@Autowired
	private ProductService productService;
	
	@Autowired
	private ShoppingCartItemDao shoppingCartItemDao;
	
	@Autowired
	private PricingService pricingService;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ShoppingCartServiceImpl.class);
	
	@Autowired
	public ShoppingCartServiceImpl(
			ShoppingCartDao shoppingCartDao) {
		super(shoppingCartDao);
		this.shoppingCartDao = shoppingCartDao;

	}
	
	
	/**
	 * Retrieve a {@link ShoppingCart} cart for a given customer
	 */
	@Transactional
	public ShoppingCart getShoppingCart(Customer customer) throws ServiceException {

		try {

			ShoppingCart shoppingCart = shoppingCartDao.getByCustomer(customer);
			populateShoppingCart(shoppingCart);
			if(shoppingCart.isObsolete()) {
				delete(shoppingCart);
				return null;
			} else {
				return shoppingCart;
			}

		
		} catch (Exception e) {
			throw new ServiceException(e);
		}
		
	}
	
	/**
	 * Save or update a {@link ShoppingCart} for a given customer
	 */
	public void saveOrUpdate(ShoppingCart shoppingCart) throws ServiceException {
		if(shoppingCart.getId()==null || shoppingCart.getId().longValue()==0) {
			super.create(shoppingCart);
		} else {
			super.update(shoppingCart);
		}
	}
	
	/**
	 * Get a {@link ShoppingCart} for a given id and MerchantStore. Will update the shopping cart
	 * prices and items based on the actual inventory.
	 */
	@Override
	@Transactional
	public ShoppingCart getById(Long id, MerchantStore store) throws ServiceException {

		try {
			ShoppingCart shoppingCart = shoppingCartDao.getById(id, store);
			populateShoppingCart(shoppingCart);
			if(shoppingCart.isObsolete()) {
				delete(shoppingCart);
				return null;
			} else {
				return shoppingCart;
			}

		
		} catch (Exception e) {
			throw new ServiceException(e);
		}
		
		
	}
	
	@Override
	public ShoppingCart getById(Long id) {

			ShoppingCart shoppingCart = shoppingCartDao.getById(id);
			try {
				populateShoppingCart(shoppingCart);

			if(shoppingCart.isObsolete()) {
				delete(shoppingCart);
				return null;
			} else {
				return shoppingCart;
			}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;

	}
	
	@Override
	@Transactional
	public ShoppingCart getByCode(String code, MerchantStore store) throws ServiceException {

		try {
			ShoppingCart shoppingCart = shoppingCartDao.getByCode(code, store);
			populateShoppingCart(shoppingCart);
			
			if(shoppingCart.isObsolete()) {
				delete(shoppingCart);
				return null;
			} else {
				return shoppingCart;
			}

		
		} catch (RuntimeException e) {
			throw new ServiceException(e);
		} catch (Exception ee) {
			throw new ServiceException(ee);
		}
		
		
	}
	
	@Override
	public void delete(ShoppingCart shoppingCart) throws ServiceException {
		shoppingCart = this.getById(shoppingCart.getId());
		super.delete(shoppingCart);
	}
	
	@Override
	public ShoppingCart getByCustomer(Customer customer) throws ServiceException {
		
		try {
			ShoppingCart shoppingCart = shoppingCartDao.getByCustomer(customer);
			return populateShoppingCart(shoppingCart);

		
		} catch (Exception e) {
			throw new ServiceException(e);
		}
	}
	
	@Transactional(noRollbackFor={org.springframework.dao.EmptyResultDataAccessException.class})
	private ShoppingCart populateShoppingCart(ShoppingCart shoppingCart) throws Exception {
		
		try {

			boolean cartIsObsolete = true;
			if(shoppingCart!=null) {
				
				Set<ShoppingCartItem> items = shoppingCart.getLineItems();
				if(items==null || items.size()==0) {
					shoppingCart.setObsolete(true);
					return shoppingCart;
					
				}
				
				//Set<ShoppingCartItem> shoppingCartItems = new HashSet<ShoppingCartItem>();
				for(ShoppingCartItem item : items) {
					LOGGER.debug("Populate item " + item.getId());
					this.populateItem(item);
					LOGGER.debug("Obsolete item ? " + item.isObsolete());
					if(item.isObsolete()) {
					} else {
						cartIsObsolete = false;
					}
					//if(item.getProduct()==null) {//product has been removed
						//LOGGER.debug("Removing shopping cart item for product id " + item.getProductId());
						//shoppingCartItemDao.delete(item);
					//} else {
					//	shoppingCartItems.add(item);
					//}
				}
				
				//shoppingCart.setLineItems(shoppingCartItems);
				boolean refreshCart = false;
                Set<ShoppingCartItem> refreshedItems = new HashSet<ShoppingCartItem>();
                for(ShoppingCartItem item : items) {
                	if(!item.isObsolete()) {
                		refreshedItems.add(item);
                	} else {
                		refreshCart = true;
                	}
                }
                
                if(refreshCart) {
                	shoppingCart.setLineItems(refreshedItems);
                	this.update(shoppingCart);
                }
				
				if(cartIsObsolete) {
					shoppingCart.setObsolete(true);
				}
				return shoppingCart;
			}
		
		} catch (Exception e) {
			throw new ServiceException(e);
		}
		
		return shoppingCart;
		
	}
	
	@Override
	public ShoppingCartItem populateShoppingCartItem(Product product) throws ServiceException {
		Validate.notNull(product, "Product should not be null");
		Validate.notNull(product.getMerchantStore(), "Product.merchantStore should not be null");

		
		ShoppingCartItem item = new ShoppingCartItem(product);

		Set<ProductAttribute> productAttributes = product.getAttributes();
		Set<ShoppingCartAttributeItem> attributesList = new HashSet<ShoppingCartAttributeItem>();
		if(!CollectionUtils.isEmpty(productAttributes)) {

			for(ProductAttribute productAttribute : productAttributes) {
					ShoppingCartAttributeItem attributeItem = new ShoppingCartAttributeItem();
					attributeItem.setShoppingCartItem(item);
					attributeItem.setProductAttribute(productAttribute);
					attributeItem.setProductAttributeId(productAttribute.getId());
					attributesList.add(attributeItem);

			}
			
			item.setAttributes(attributesList);
		}
		
		//set item price
		FinalPrice price = pricingService.calculateProductPrice(product);
		item.setItemPrice(price.getFinalPrice());
		return item;
		
		
	}
	

	private void populateItem(ShoppingCartItem item) throws Exception {
		
		Product product = null;
		//try {
			

			Long productId = item.getProductId();
			product = productService.getById(productId);
			
			if(product==null) {
				item.setObsolete(true);
				return;
			}
		
		//} catch(Exception e) {
			//if(e instanceof org.springframework.dao.EmptyResultDataAccessException) {

			//}
		//}

		

		
		item.setProduct(product);
		
		Set<ShoppingCartAttributeItem> attributes = item.getAttributes();
		Set<ProductAttribute> productAttributes = product.getAttributes();
		List<ProductAttribute> attributesList = new ArrayList<ProductAttribute>();
		if(productAttributes!=null && productAttributes.size()>0 && attributes!=null && attributes.size()>0) {
			for(ShoppingCartAttributeItem attribute : attributes) {
				long attributeId = attribute.getProductAttributeId().longValue();
				for(ProductAttribute productAttribute : productAttributes) {
					
					if(productAttribute.getId().longValue()==attributeId) {
						attribute.setProductAttribute(productAttribute);
						attributesList.add(productAttribute);
						break;
					}
					
				}
				
			}
		} else {
			
			if(productAttributes!=null && productAttributes.size()>0) {
				LOGGER.debug("Removing attributes for shopping cart item " + item.getId());
				item.setAttributes(null);//TODO check should update shopping cart
			}
			
		}
		
		//set item price
		FinalPrice price = pricingService.calculateProductPrice(product, attributesList);
		item.setItemPrice(price.getFinalPrice());
		item.setFinalPrice(price);

		

		BigDecimal subTotal = item.getItemPrice().multiply(new BigDecimal(item.getQuantity().intValue()));
		item.setSubTotal(subTotal);
		
		
	}
	
	@Override
	public List<ShippingProduct> createShippingProduct(ShoppingCart cart) throws ServiceException {
		/**
		 * Determines if products are virtual
		 */
		Set<ShoppingCartItem> items = cart.getLineItems();
		List<ShippingProduct> shippingProducts = null;
		for(ShoppingCartItem item : items) {
			Product product = item.getProduct();
			if(!product.isProductVirtual() && product.isProductShipeable()) {
				if(shippingProducts==null) {
					shippingProducts = new ArrayList<ShippingProduct>();
				}
				ShippingProduct shippingProduct = new ShippingProduct(product);
				shippingProduct.setQuantity(item.getQuantity());
			}
		}
		
		return shippingProducts;
		
	}

	
	@Override
	public boolean isFreeShoppingCart(ShoppingCart cart) throws ServiceException {
		/**
		 * Determines if products are free
		 */
		Set<ShoppingCartItem> items = cart.getLineItems();
		for(ShoppingCartItem item : items) {
			Product product = item.getProduct();
			FinalPrice finalPrice = pricingService.calculateProductPrice(product);
			if(finalPrice.getFinalPrice().longValue()>0) {
				return false;
			}
		}
		return true;
		
	}
	



}
