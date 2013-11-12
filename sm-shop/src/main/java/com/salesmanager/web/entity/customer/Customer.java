package com.salesmanager.web.entity.customer;

import java.io.Serializable;

import com.salesmanager.core.business.common.model.Billing;
import com.salesmanager.web.entity.ShopEntity;

public class Customer extends ShopEntity implements Serializable {
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String userName;
	private String password;
	private String storeCode;
	
	private String emailAddress;
	private String phone;
	private Billing billing;
	
	
	
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getStoreCode() {
		return storeCode;
	}
	public void setStoreCode(String storeCode) {
		this.storeCode = storeCode;
	}
	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}
	public String getEmailAddress() {
		return emailAddress;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public String getPhone() {
		return phone;
	}
	public void setBilling(Billing billing) {
		this.billing = billing;
	}
	public Billing getBilling() {
		return billing;
	}

}
