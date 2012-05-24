package com.vanhlebarsoftware.kmmdroid;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

public class KMMDProvider extends ContentProvider 
{
	private static final String TAG = KMMDProvider.class.getSimpleName();
	public static final Uri CONTENT_URI = Uri.parse("content://com.vanhlebarsoftware.kmmdroid.kmmdprovider/account");
	public static final String SINGLE_RECORD_MIME_TYPE = "vnd.android.cursor.item/vnd.vanhlebarsoftware.kmmdroid.account";
	public static final String MULTIPLE_RECORDS_MIME_TYPE = "vnd.android.cursor.dir/vnd.vanhlebarsoftware.com.kmmdroid.accounts";
	private static final String dbTable = "kmmAccounts";
	private static final String[] dbColumns = { "accountName", "balanceFormatted", "id AS _id"};
	private static final String strSelection = "(parentId='AStd::Asset' OR parentId='AStd::Liability') AND " +
			"(balance != '0/1')";
	private static final String strOrderBy = "parentID, accountName ASC";
	public SharedPreferences prefs;
	SQLiteDatabase db;
	
	@Override
	public int delete(Uri arg0, String arg1, String[] arg2) 
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getType(Uri uri) 
	{
		return this.getId(uri) < 0 ? MULTIPLE_RECORDS_MIME_TYPE : SINGLE_RECORD_MIME_TYPE;
	}

	@Override
	public Uri insert(Uri arg0, ContentValues arg1) 
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean onCreate() 
	{  
		Log.d(TAG, "onCreate() entered");
		// Need to get the prefs for our application.
		Context context = getContext();
		Context cont = null;
		try 
		{
			cont = context.createPackageContext("com.vanhlebarsoftware.kmmdroid", Context.CONTEXT_IGNORE_SECURITY);
		} 
		catch (NameNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		SharedPreferences prefs = cont.getSharedPreferences("com.vanhlebarsoftware.kmmdroid_preferences", Context.MODE_WORLD_READABLE);
		
		// Get the path to the database the user wants to use for this widget.
		String path = prefs.getString("Full Path", "");
		Log.d(TAG, "Database path: " + path);
		
		// Hack - if database path is empty do nothing.
		if(!path.isEmpty())
		{
			Log.d(TAG, "Attempting to open database.");
			db = SQLiteDatabase.openDatabase(path, null, 0);
		}
		else
			Log.d(TAG, "There is no database to be opened!");
        
		return false;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) 
	{
		long id = this.getId(uri);
		
		if(id < 0)
		{
			if(db.isOpen())
			{
				Log.d(TAG, "Running query for all accounts");
				//return db.query("kmmAccounts", projection, selection, selectionArgs, null, null, sortOrder);
				return db.query(dbTable, dbColumns, strSelection, null, null, null, strOrderBy);
			}
			else
			{	
				Log.d(TAG, "Database is not open!");
				return null;
			}
		}
		else
			return db.query("kmmAccounts=" + id, projection, selection, null, null, null, null);
	}

	@Override
	public int update(Uri arg0, ContentValues arg1, String arg2, String[] arg3) 
	{
		// TODO Auto-generated method stub
		return 0;
	}

	private long getId(Uri uri)
	{
		String lastPathSegment = uri.getLastPathSegment();
		
		if(lastPathSegment != null)
		{
			try
			{
				return Long.parseLong(lastPathSegment);
			}
			catch(NumberFormatException e)
			{
				// at least we tried
			}
		}
		return -1;
	}
}
