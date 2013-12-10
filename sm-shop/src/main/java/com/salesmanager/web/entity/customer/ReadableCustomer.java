package com.salesmanager.web.entity.customer;

import java.io.Serializable;
import java.util.List;

import com.salesmanager.web.entity.customer.attribute.ReadableCustomerAttribute;

public class ReadableCustomer extends CustomerEntity implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private List<ReadableCustomerAttribute> attributes;
	public void setAttributes(List<ReadableCustomerAttribute> attributes) {
		this.attributes = attributes;
	}
	public List<ReadableCustomerAttribute> getAttributes() {
		return attributes;
	}

}
