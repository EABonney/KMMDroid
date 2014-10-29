package com.vanhlebarsoftware.kmmdroid;

import android.content.Context;
import android.database.Cursor;

public class Price 
{
	String fromId;
	String toId;
	String priceDate;
	String price;
	String priceFormatted;
	String priceSource;
	String widgetId;
	Context context;
	
	Price(String pfromId, String ptoId, String pDate, String pPrice, String pFormatted, String pSource, String wId, Context c)
	{
		this.fromId = pfromId;
		this.toId = ptoId;
		this.priceDate = pDate;
		this.price = pPrice;
		this.priceFormatted = pFormatted;
		this.priceSource = pSource;
		this.widgetId = wId;
		this.context = c;
	}
	
	Price (Cursor price, int element, String wId, Context c)
	{
		price.moveToPosition(element);
		
		this.fromId = price.getString(price.getColumnIndexOrThrow("fromId"));
		this.toId = price.getString(price.getColumnIndexOrThrow("toId"));
		this.priceDate = price.getString(price.getColumnIndexOrThrow("priceDate"));
		this.price = price.getString(price.getColumnIndexOrThrow("price"));
		this.priceFormatted = price.getString(price.getColumnIndexOrThrow("priceFormatted"));
		this.priceSource = price.getString(price.getColumnIndexOrThrow("priceSource"));
		this.widgetId = wId;
		this.context = c;
	}
	
	public String getPrice()
	{
		return this.price;
	}
}
