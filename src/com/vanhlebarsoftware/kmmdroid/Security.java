package com.vanhlebarsoftware.kmmdroid;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;


public class Security
{
	String id;
	String name;
	String symbol;
	int type;
	String typeString;
	String smallestAccountFraction;
	String tradingMarket;
	String tradingCurrency;
	String widgetId;
	Context context;
	ArrayList<Price> prices;
	
	Security(String sId, String sName, String sSymbol, int sType, String sTypeString, String sSmallestAcctFrac, String sTradingMarket,
			 String sTradingCur, String widgetId, Context c)
	{
		this.id = sId;
		this.name = sName;
		this.symbol = sSymbol;
		this.type = sType;
		this.typeString = sTypeString;
		this.smallestAccountFraction = sSmallestAcctFrac;
		this.tradingMarket = sTradingMarket;
		this.tradingCurrency = sTradingCur;
		this.widgetId = widgetId;
		this.context = c;
		this.prices = new ArrayList<Price>();
		this.prices = getPrices(sId, widgetId);
	}
	
	Security(Cursor security, String widgetId, Context c)
	{
		security.moveToFirst();
		this.id = security.getString(security.getColumnIndexOrThrow("id"));
		this.name = security.getString(security.getColumnIndexOrThrow("name"));
		this.symbol = security.getString(security.getColumnIndexOrThrow("symbol"));
		this.type = security.getInt(security.getColumnIndexOrThrow("type"));
		this.typeString = security.getString(security.getColumnIndexOrThrow("typeString"));
		this.smallestAccountFraction = security.getString(security.getColumnIndexOrThrow("smallestAccountFraction"));
		this.tradingMarket = security.getString(security.getColumnIndexOrThrow("tradingMarket"));
		this.tradingCurrency = security.getString(security.getColumnIndexOrThrow("tradingCurrency"));
		this.widgetId = widgetId;
		this.context = c;
		this.prices = new ArrayList<Price>();
		this.prices = getPrices(this.id, widgetId);
		security.close();
	}
	
	private ArrayList<Price> getPrices(String fromId, String wId)
	{
		ArrayList<Price> pr = new ArrayList<Price>();
		
		// Get the prices for this security
		Uri u = Uri.withAppendedPath(KMMDProvider.CONTENT_PRICES, fromId + "#" + wId);
		u = Uri.parse(u.toString());
		Cursor curPrices = this.context.getContentResolver().query(u, null, null, null, null);
		
		for(int i=0; i<curPrices.getCount(); i++)
			pr.add(new Price(curPrices, i, wId, this.context));
		
		return pr;
	}
}
