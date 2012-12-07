package com.vanhlebarsoftware.kmmdroid;

public class Institution 
{
	private String id;
	private String name;
	private String manager;
	private String routingCode;
	private String addressStreet;
	private String addressCity;
	private String addressZipcode;
	private String telephone;
	
	Institution(String id, String name, String manager, String routingCode, String addressStreet,
			    String addressCity, String addressZipcode, String telephone)
	{
		this.id = id;
		this.name = name;
		this.manager = manager;
		this.routingCode = routingCode;
		this.addressStreet = addressStreet;
		this.addressCity = addressCity;
		this.addressZipcode = addressZipcode;
		this.telephone = telephone;
	}
	
	public String getId()
	{
		return this.id;
	}
	
	public String getName()
	{
		return this.name;
	}
	
	public String getManager()
	{
		return this.manager;
	}
	
	public String getRoutingCode()
	{
		return this.routingCode;
	}
	
	public String getStreet()
	{
		return this.addressStreet;
	}
	
	public String getCity()
	{
		return this.addressCity;
	}
	
	public String getZipcode()
	{
		return this.addressZipcode;
	}
	
	public String getTelephone()
	{
		return this.telephone;
	}
}
