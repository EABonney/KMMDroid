package com.vanhlebarsoftware.kmmdroid;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class Payee implements Parcelable
{
	private static final String TAG = Payee.class.getSimpleName();
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
	private CreateModifyPayeeActivity context;
	
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
	
	Payee(Context cont, String id)
	{
		if( id == null )
		{
			this.id = null;
			this.name = null;
			this.reference = null;
			this.email = null;
			this.addressStreet = null;
			this.addressCity = null;
			this.addressState = null;
			this.addressZipcode = null;
			this.telephone = null;
			this.notes = null;
			this.defaultAccountId = null;
			this.matchData = 0;
			this.matchIgnoreCase = null;
			this.matchKeys = null;
		}
		else
		{
			String frag = "#9999";
			Uri u = Uri.withAppendedPath(KMMDProvider.CONTENT_PAYEE_URI,id + frag);
			u = Uri.parse(u.toString());
			Cursor c = cont.getContentResolver().query(u, new String[] { "*" }, null, null, null);
			c.moveToFirst();
			this.id = c.getString(c.getColumnIndex("id"));
			this.name = c.getString(c.getColumnIndex("name"));
			this.reference = c.getString(c.getColumnIndex("reference"));
			this.email = c.getString(c.getColumnIndex("email"));
			this.addressStreet = c.getString(c.getColumnIndex("addressStreet"));
			this.addressCity = c.getString(c.getColumnIndex("addressCity"));
			this.addressState = c.getString(c.getColumnIndex("addressState"));
			this.addressZipcode = c.getString(c.getColumnIndex("addressZipcode"));
			this.telephone = c.getString(c.getColumnIndex("telephone"));
			this.notes = c.getString(c.getColumnIndex("notes"));
			this.defaultAccountId = c.getString(c.getColumnIndex("defaultAccountId"));
			this.matchData = c.getInt(c.getColumnIndex("matchData"));
			this.matchIgnoreCase = c.getString(c.getColumnIndex("matchIgnoreCase"));
			this.matchKeys = c.getString(c.getColumnIndex("matchKeys"));
			c.close();
		}
		
		this.context = (CreateModifyPayeeActivity) cont;
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
	
	public void createPayeeId()
	{
		String frag = "#9999";
		Uri u = Uri.withAppendedPath(KMMDProvider.CONTENT_FILEINFO_URI, frag);
		u = Uri.parse(u.toString());
		final String[] dbColumns = { "hiPayeeId"};
		final String strOrderBy = "hiPayeeId DESC";
		// Run a query to get the Payee ids so we can create a new one.
		Cursor cursor = this.context.getContentResolver().query(u, dbColumns, null, null, strOrderBy);
		
		cursor.moveToFirst();

		// Since id is in P000000 format, we need to pick off the actual number then increase by 1.
		int lastId = cursor.getInt(0);
		lastId = lastId +1;
		String nextId = Integer.toString(lastId);
		
		// Need to pad out the number so we get back to our P000000 format
		String newId = "P";
		for(int i= 0; i < (6 - nextId.length()); i++)
		{
			newId = newId + "0";
		}
		
		// Tack on the actual number created.
		newId = newId + nextId;
		
		// clean up our cursor
		cursor.close();
		
		this.id = newId;
	}
	
	public void getDataChanges()
	{
		PayeeAddressActivity address = (PayeeAddressActivity) this.context.getSupportFragmentManager().findFragmentByTag("address");
		this.name = address.getPayeeName();
		this.email = address.getPayeeEmail();
		this.addressStreet = address.getPayeeAddress();
		this.addressZipcode = address.getPayeePostalCode();
		this.telephone = address.getPayeePhone();
		this.notes = address.getPayeeNotes();
		
		PayeeDefaultAccountActivity def = (PayeeDefaultAccountActivity) this.context.getSupportFragmentManager().findFragmentByTag("default");
		if( def != null )
		{
			if( def.getUseDefaults() )
			{
				if( def.getUseIncome() )
					this.defaultAccountId = def.getIncomeAccount();
				else if( def.getUseExpense() )
					this.defaultAccountId = def.getExpenseAccount();
				else
					this.defaultAccountId = null;
			}
			else
				this.defaultAccountId = null;
		}
		
		PayeeMatchingActivity match = (PayeeMatchingActivity) this.context.getSupportFragmentManager().findFragmentByTag("matching");
		if( match != null )
		{
			this.matchData = match.getMatchingType();
		}
	}
	
	public void Save()
	{
		// Create the ContentValue pairs for the insert query.
		ContentValues valuesPayee = new ContentValues();
		valuesPayee.put("id", this.id);
		valuesPayee.put("name", this.name);
		valuesPayee.put("reference", "");
		valuesPayee.put("email", this.email);
		valuesPayee.put("addressStreet", this.addressStreet);
		valuesPayee.put("addressCity", "");
		valuesPayee.put("addressState", "");
		valuesPayee.put("addressZipcode", this.addressZipcode);
		valuesPayee.put("telephone", this.telephone);
		valuesPayee.put("notes", notes);
		valuesPayee.put("defaultAccountId", this.defaultAccountId);
		valuesPayee.put("matchData", this.matchData);
		valuesPayee.put("matchIgnoreCase", "Y");
		valuesPayee.put("matchKeys", "");
		
		try 
		{
			String frag = "#9999";
			Uri u = Uri.withAppendedPath(KMMDProvider.CONTENT_PAYEE_URI, frag);
			u = Uri.parse(u.toString());
			this.context.getContentResolver().insert(u,valuesPayee);
			frag = "#9999";
			u = Uri.withAppendedPath(KMMDProvider.CONTENT_FILEINFO_URI, frag);
			u = Uri.parse(u.toString());
			this.context.getContentResolver().update(u, null, "hiPayeeId", new String[] { "1" });
			this.context.getContentResolver().update(u, null, "payees", new String[] { "1" });
		} 
		catch (SQLException e)
		{
			Log.d(TAG, "error: " + e.getMessage());
		}
	}
	
	public void Update()
	{
		// Create the ContentValue pairs for the insert query.
		ContentValues valuesPayee = new ContentValues();
		valuesPayee.put("id", this.id);
		valuesPayee.put("name", this.name);
		valuesPayee.put("reference", "");
		valuesPayee.put("email", this.email);
		valuesPayee.put("addressStreet", this.addressStreet);
		valuesPayee.put("addressCity", "");
		valuesPayee.put("addressState", "");
		valuesPayee.put("addressZipcode", this.addressZipcode);
		valuesPayee.put("telephone", this.telephone);
		valuesPayee.put("notes", notes);
		valuesPayee.put("defaultAccountId", this.defaultAccountId);
		valuesPayee.put("matchData", this.matchData);
		valuesPayee.put("matchIgnoreCase", "Y");
		valuesPayee.put("matchKeys", "");

		String frag = "#9999";
		Uri u = Uri.withAppendedPath(KMMDProvider.CONTENT_PAYEE_URI,getId() + frag);
		u = Uri.parse(u.toString());
		this.context.getContentResolver().update(u,valuesPayee, null, null);
	}

	/***********************************************************************************************
	 * Required methods to make Payee parcelable to pass between activities
	 **********************************************************************************************/
	public int describeContents() 
	{
		return 0;
	}

	public void writeToParcel(Parcel dest, int flags) 
	{	
		dest.writeString(id);
		dest.writeString(name);
		dest.writeString(reference);
		dest.writeString(email);
		dest.writeString(addressStreet);
		dest.writeString(addressCity);
		dest.writeString(addressState);
		dest.writeString(addressZipcode);
		dest.writeString(telephone);
		dest.writeString(notes);
		dest.writeString(defaultAccountId);
		dest.writeInt(matchData);
		dest.writeString(matchIgnoreCase);
		dest.writeString(matchKeys);
	}
}
