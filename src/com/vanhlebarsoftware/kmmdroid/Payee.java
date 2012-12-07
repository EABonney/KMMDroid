package com.vanhlebarsoftware.kmmdroid;

public class Payee 
{
	private String id;
	private String name;
	private String reference;
	private String email;
	private String addressStreet;
	private String addressCity;
	private String addressState;
	private String addressZipcode;
	private String telephone;
	private String notes;
	private String defaultAccountId;
	private int matchData;
	private String matchIgnoreCase;
	private String matchKeys;
	
	Payee(String id, String name, String reference, String email, String street, String city, String state,String zipcode,
		  String phone, String notes, String defaultAcctId, int matchData, String matchIgnoreCase, String matchkeys)
	{
		this.id = id;
		this.name = name;
		this.reference = reference;
		this.email = email;
		this.addressStreet = street;
		this.addressCity = city;
		this.addressState = state;
		this.addressZipcode = zipcode;
		this.telephone = phone;
		this.notes = notes;
		this.defaultAccountId = defaultAcctId;
		this.matchData = matchData;
		this.matchIgnoreCase = matchIgnoreCase;
		this.matchKeys = matchkeys;
	}
	
	public String getId()
	{
		return this.id;
	}
	
	public String getName()
	{
		return this.name;
	}
	
	public String getReference()
	{
		return this.reference;
	}
	
	public String getEmail()
	{
		return this.email;
	}
	
	public String getStreet()
	{
		return this.addressStreet;
	}
	
	public String getCity()
	{
		return this.addressCity;
	}
	
	public String getState()
	{
		return this.addressState;
	}
	
	public String getZipCode()
	{
		return this.addressZipcode;
	}
	
	public String getTelephone()
	{
		return this.telephone;
	}
	
	public String getNotes() 
	{
		return this.notes;
	}
	
	public String getDefualtAccountId()
	{
		return this.defaultAccountId;
	}
	
	public int getMatchData()
	{
		return this.matchData;
	}
	
	public String getMatchIgnoreCase()
	{
		return this.matchIgnoreCase;
	}
	
	public String getKeys()
	{
		return this.matchKeys;
	}
}
