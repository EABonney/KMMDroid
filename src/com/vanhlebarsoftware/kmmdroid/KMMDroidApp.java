package com.vanhlebarsoftware.kmmdroid;

import android.app.Application;
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
	Cursor cursor;
	private String fullPath = null;
	private boolean dbOpen = false;
	
	@Override
	public void onCreate()
	{
		super.onCreate();
		this.prefs = PreferenceManager.getDefaultSharedPreferences(this);
		this.prefs.registerOnSharedPreferenceChangeListener((OnSharedPreferenceChangeListener) this);
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
		db = SQLiteDatabase.openDatabase(fullPath, null, 1);
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
}