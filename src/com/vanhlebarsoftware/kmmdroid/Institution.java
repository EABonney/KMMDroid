package com.vanhlebarsoftware.kmmdroid;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class Institution implements Parcelable
{
	private static final String TAG = Institution.class.getSimpleName();
	private String id;
	private String name;
	private String manager;
	private String routingCode;
	private String addressStreet;
	private String addressCity;
	private String addressZipcode;
	private String telephone;
	private Context context;
	
	Institution(String id, String name, String manager, String routingCode, String addressStreet,
			    String addressCity, String addressZipcode, String telephone, Context c)
	{
		this.id = id;
		this.name = name;
		this.manager = manager;
		this.routingCode = routingCode;
		this.addressStreet = addressStreet;
		this.addressCity = addressCity;
		this.addressZipcode = addressZipcode;
		this.telephone = telephone;
		this.context = c;
	}
	
	Institution(Context context, String id)
	{
		Cursor c = null;
		
		if( id != null )
		{
			String frag = "#9999";
			Uri u = Uri.withAppendedPath(KMMDProvider.CONTENT_INSTITUTION_URI,id + frag);
			u = Uri.parse(u.toString());
			c = context.getContentResolver().query(u, new String[] { "*" }, null, null, null);
		}
		
		if( c != null )
		{
			c.moveToFirst();
			this.id = id;
			this.name = c.getString(c.getColumnIndex("name"));
			this.manager = c.getString(c.getColumnIndex("manager"));
			this.routingCode = c.getString(c.getColumnIndex("routingCode"));
			this.addressStreet = c.getString(c.getColumnIndex("addressStreet"));
			this.addressCity = c.getString(c.getColumnIndex("addressCity"));
			this.addressZipcode = c.getString(c.getColumnIndex("addressZipcode"));
			this.telephone = c.getString(c.getColumnIndex("telephone"));
			
			// Clean up our cursor.
			c.close();
		}
		else
		{
			// Create an empty institution with all null values instead.
			this.id = null;
			this.name = null;
			this.manager = null;
			this.routingCode = null;
			this.addressStreet = null;
			this.addressCity = null;
			this.addressZipcode = null;
			this.telephone = null;			
		}
		
		this.context = context;
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
	
	public void createId()
	{
		String frag = "#9999";
		Uri u = Uri.withAppendedPath(KMMDProvider.CONTENT_FILEINFO_URI, frag);
		u = Uri.parse(u.toString());
		final String[] dbColumns = { "hiInstitutionId"};
		final String strOrderBy = "hiInstitutionId DESC";
		// Run a query to get the Institution ids so we can create a new one.
		Cursor cursor = this.context.getContentResolver().query(u, dbColumns, null, null, strOrderBy);
		
		cursor.moveToFirst();

		// Since id is in I000000 format, we need to pick off the actual number then increase by 1.
		int lastId = cursor.getInt(0);
		lastId = lastId +1;
		String nextId = Integer.toString(lastId);
		
		// Need to pad out the number so we get back to our P000000 format
		String newId = "I";
		for(int i= 0; i < (6 - nextId.length()); i++)
		{
			newId = newId + "0";
		}
		
		// Tack on the actual number created.
		newId = newId + nextId;
		
		// Clean up our cursor.
		cursor.close();

		this.id = newId;
	}
	
	public void getDataChanges()
	{
		this.name = ((CreateModifyInstitutionActivity) this.context).instName.getText().toString();
		this.manager = ((CreateModifyInstitutionActivity) this.context).instBIC.getText().toString();
		this.routingCode = ((CreateModifyInstitutionActivity) this.context).instRoutingNumber.getText().toString();
		this.addressStreet = ((CreateModifyInstitutionActivity) this.context).instStreet.getText().toString();
		this.addressCity = ((CreateModifyInstitutionActivity) this.context).instCity.getText().toString();
		this.addressZipcode = ((CreateModifyInstitutionActivity) this.context).instPostalCode.getText().toString();
		this.telephone = ((CreateModifyInstitutionActivity) this.context).instPhone.getText().toString();
	}
	
	public void Save()
	{
		// We can only save if we actually have a context, if not just error out for now.
		if( this.context != null )
		{
			// create the ContentValue pairs
			ContentValues valuesInst = new ContentValues();
			valuesInst.put("id", this.getId());
			valuesInst.put("name", this.getName());
			valuesInst.put("routingCode", this.getRoutingCode());
			valuesInst.put("addressStreet", this.getStreet());
			valuesInst.put("addressCity", this.getCity());
			valuesInst.put("addressZipcode", this.getZipcode());
			valuesInst.put("telephone", this.getTelephone());
			valuesInst.put("manager", this.getManager());
		
			try 
			{
				String frag = "#9999";
				Uri u = Uri.withAppendedPath(KMMDProvider.CONTENT_INSTITUTION_URI, frag);
				u = Uri.parse(u.toString());
				context.getContentResolver().insert(u,valuesInst);
				frag = "#9999";
				u = Uri.withAppendedPath(KMMDProvider.CONTENT_FILEINFO_URI, frag);
				u = Uri.parse(u.toString());
				context.getContentResolver().update(u, null, "hiInstitutionId", new String[] { "1" });
				context.getContentResolver().update(u, null, "institutions", new String[] { "1" });
			} 
			catch (SQLException e)
			{
				Log.d(TAG, "error: " + e.getMessage());
			}
		}
		else
			Log.d(TAG, "We didn't have a context so we could not save!!!!");
	}
	
	public void Update()
	{
		// We can only save if we actually have a context, if not just error out for now.
		if( this.context != null )
		{
			// create the ContentValue pairs
			ContentValues valuesInst = new ContentValues();
			valuesInst.put("name", this.getName());
			valuesInst.put("routingCode", this.getRoutingCode());
			valuesInst.put("addressStreet", this.getStreet());
			valuesInst.put("addressCity", this.getCity());
			valuesInst.put("addressZipcode", this.getZipcode());
			valuesInst.put("telephone", this.getTelephone());
			valuesInst.put("manager", this.getManager());
		
			// Actually update the institution now.
			String frag = "#9999";
			Uri u = Uri.withAppendedPath(KMMDProvider.CONTENT_INSTITUTION_URI, this.getId() + frag);
			u = Uri.parse(u.toString());
			context.getContentResolver().update(u, valuesInst, null, null);
		}
		else
			Log.d(TAG, "We didn't have a context so we could not update!!!!");
	}

	/***********************************************************************************************
	 * Required methods to make Institution parcelable to pass between activities
	 * 
	 * Any time we are using this parcel to get Institution we MUST use the setContext() method to set the
	 * context of the actual Institution as we can not pass this as part of the Parcel. Failing to do this
	 * will cause context to be null and crash!
	 **********************************************************************************************/
	public void setContext(Context c)
	{
		this.context = c;
	}
	
	public int describeContents() 
	{
		return 0;
	}

	public void writeToParcel(Parcel dest, int flags) 
	{	
		dest.writeString(id);
		dest.writeString(name);
		dest.writeString(manager);
		dest.writeString(routingCode);
		dest.writeString(addressStreet);
		dest.writeString(addressCity);
		dest.writeString(addressZipcode);
		dest.writeString(telephone);
	}
}
