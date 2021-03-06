package com.salesmanager.web.entity.catalog.product;

import java.io.Serializable;

import org.hibernate.validator.constraints.NotEmpty;

import com.salesmanager.web.entity.ShopEntity;

public class ProductReviewEntity extends ShopEntity implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@NotEmpty
	private String description;
	private Long productId;
	private String date;
	
	private Double rating;
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public Long getProductId() {
		return productId;
	}
	public void setProductId(Long productId) {
		this.productId = productId;
	}

	public Double getRating() {
		return rating;
	}
	public void setRating(Double rating) {
		this.rating = rating;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}


}
