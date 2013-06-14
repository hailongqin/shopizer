package com.salesmanager.core.business.catalog.product.model.file;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import com.salesmanager.core.business.catalog.product.model.Product;
import com.salesmanager.core.business.generic.model.SalesManagerEntity;
import com.salesmanager.core.constants.SchemaConstant;


/**
 * Representation of a digital product
 * @author csamson777
 *
 */
@Entity
@Table(name = "PRODUCT_DIGITAL", schema=SchemaConstant.SALESMANAGER_SCHEMA)
public class DigitalProduct extends SalesManagerEntity<Long, DigitalProduct> {


	private static final long serialVersionUID = 1L;
	
	
	@Id
	@Column(name = "PRODUCT_DIGITAL_ID")
	@TableGenerator(name = "TABLE_GEN", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME", valueColumnName = "SEQ_COUNT", pkColumnValue = "PRODUCT_DGT_SEQ_NEXT_VAL")
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "TABLE_GEN")
	private Long id;
	
	
	@ManyToOne(targetEntity = Product.class)
	@JoinColumn(name = "PRODUCT_ID", nullable = false)
	private Product product;


	@Column(name="FILE_NAME",nullable=false, unique=true)
	private String productFileName;
	

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	public String getProductFileName() {
		return productFileName;
	}

	public void setProductFileName(String productFileName) {
		this.productFileName = productFileName;
	}
	
	public Product getProduct() {
		return product;
	}

	public void setProduct(Product product) {
		this.product = product;
	}

	
}
