package com.vanhlebarsoftware.kmmdroid;

import java.util.ArrayList;
import java.util.Calendar;

import android.app.Application;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.util.Log;

@SuppressWarnings("unused")
public class KMMDroidApp extends Application implements OnSharedPreferenceChangeListener
{
	private static final String TAG = KMMDroidApp.class.getSimpleName();
	public SharedPreferences prefs;
	SQLiteDatabase db;
	private String fullPath = null;
	private boolean dbOpen = false;
	public ArrayList<Split> Splits;	
	public long flSplitsTotal = 0;
	
	@Override
	public void onCreate()
	{
		super.onCreate();
		this.prefs = PreferenceManager.getDefaultSharedPreferences(this);
		this.prefs.registerOnSharedPreferenceChangeListener((OnSharedPreferenceChangeListener) this);
		Splits = new ArrayList<Split>();
		Splits.clear();
	}
	
	@Override
	public void onTerminate()
	{
		super.onTerminate();
		
		// Make sure the database is closed.
		db.close();
		dbOpen = false;
	}
	
	public synchronized void onSharedPreferenceChanged( SharedPreferences sharedPreferences, String key)
	{
		SharedPreferences.Editor prefEditor = prefs.edit();
		
		// If the user wants to open the last used database when we start up, store the location.
		if (prefs.getBoolean("openLastUsed", false))
		{
			prefEditor.putString("Full Path", fullPath);
			prefEditor.commit();
		}
		else
		{
			prefEditor.putString("Full Path", "");
			prefEditor.commit();
		}
	}
	
	public void openDB()
	{
		db = SQLiteDatabase.openDatabase(fullPath, null, 0);
		dbOpen = true;
	}
	
	public void setFullPath(String path)
	{
		fullPath = path;
	}
	
	public boolean isDbOpen()
	{
		return dbOpen;
	}
	
	public void splitsInit()
	{
		Splits = new ArrayList<Split>();
	}
	
	public void splitsDestroy()
	{
		Splits.clear();
	}
	
	public ArrayList<Split> getSplits()
	{
		return Splits;
	}
	
	public void updateFileInfo(String updateColumn, int nChange)
	{
		Cursor cursor;
		ContentValues values = new ContentValues();
		
		if( updateColumn.equals("lastModified") )
		{
	        // get the current date
	        Calendar c = Calendar.getInstance();
	        int intYear = c.get(Calendar.YEAR);
	        int intMonth = c.get(Calendar.MONTH);
	        int intDay = c.get(Calendar.DAY_OF_MONTH);
	        String date = new StringBuilder()
			.append(intYear).append("-")
			.append(intMonth + 1).append("-")
			.append(intDay).toString();
	        
	        values.put("lastModified", date);
		}
		else if ( updateColumn.equals("institutions") )
		{
			final String[] dbColumns = { "institutions" };
			
			cursor = db.query("kmmFileInfo", dbColumns, null, null, null, null, null);
			cursor.moveToFirst();
			
			int id = cursor.getInt(0);
			id = id + nChange;
	
			values.put("institutions", id);			
		}
		else if ( updateColumn.equals("accounts") )
		{
			final String[] dbColumns = { "accounts" };
			
			cursor = db.query("kmmFileInfo", dbColumns, null, null, null, null, null);
			cursor.moveToFirst();
			
			int id = cursor.getInt(0);
			id = id + nChange;
	
			values.put("accounts", id);			
		}
		else if ( updateColumn.equals("payees") )
		{
			final String[] dbColumns = { "payees" };
			
			cursor = db.query("kmmFileInfo", dbColumns, null, null, null, null, null);
			cursor.moveToFirst();
			
			int id = cursor.getInt(0);
			id = id + nChange;
	
			values.put("payees", id);			
		}
		else if ( updateColumn.equals("transactions") )
		{
			final String[] dbColumns = { "transactions" };
			
			cursor = db.query("kmmFileInfo", dbColumns, null, null, null, null, null);
			cursor.moveToFirst();
			
			int id = cursor.getInt(0);
			id = id + nChange;
	
			values.put("transactions", id);			
		}
		else if ( updateColumn.equals("splits") )
		{
			final String[] dbColumns = { "splits" };
			
			cursor = db.query("kmmFileInfo", dbColumns, null, null, null, null, null);
			cursor.moveToFirst();
			
			int id = cursor.getInt(0);
			id = id + nChange;
	
			values.put("splits", id);			
		}
		else if ( updateColumn.equals("hiInstitutionsId") )
		{
			final String[] dbColumns = { "hiInstitutionId" };
			
			cursor = db.query("kmmFileInfo", dbColumns, null, null, null, null, null);
			cursor.moveToFirst();
			
			int id = cursor.getInt(0);
			id = id + 1;
	
			values.put("hiInstitutionId", id);			
		}
		else if ( updateColumn.equals("hiPayeeId") )
		{
			final String[] dbColumns = { "hiPayeeId" };
			
			cursor = db.query("kmmFileInfo", dbColumns, null, null, null, null, null);
			cursor.moveToFirst();
			
			int id = cursor.getInt(0);
			id = id + 1;
	
			values.put("hiPayeeId", id);	
		}
		else if ( updateColumn.equals("hiAccountId") )
		{
			final String[] dbColumns = { "hiAccountId" };
			
			cursor = db.query("kmmFileInfo", dbColumns, null, null, null, null, null);
			cursor.moveToFirst();
			
			int id = cursor.getInt(0);
			id = id + 1;
	
			values.put("hiAccountId", id);			
		}
		else if ( updateColumn.equals("hiTransactionId") )
		{
			final String[] dbColumns = { "hiTransactionId" };
			
			cursor = db.query("kmmFileInfo", dbColumns, null, null, null, null, null);
			cursor.moveToFirst();
			
			int id = cursor.getInt(0);
			id = id + 1;
	
			values.put("hiTransactionId", id);			
		}
		
		db.update("kmmFileInfo", values, null, null);
	}
}