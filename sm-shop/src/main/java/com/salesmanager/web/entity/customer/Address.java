package com.salesmanager.web.entity.customer;


public class Address {
	

	private String name;
	

	private String company;
	

	private String address;


	private String city;
	

	private String postalCode;
	

	private String stateProvince;
	
	private String zone;//code
	
	private String country;//code

	public void setStateProvince(String stateProvince) {
		this.stateProvince = stateProvince;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getPostalCode() {
		return postalCode;
	}

	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}

	public String getStateProvince() {
		return stateProvince;
	}

	public String getCountry() {
		return country;
	}

	public void setZone(String zone) {
		this.zone = zone;
	}

	public String getZone() {
		return zone;
	}


}
