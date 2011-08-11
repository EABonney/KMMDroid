package com.vanhlebarsoftware.kmmdroid;

import android.app.Application;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

@SuppressWarnings("unused")
public class KMMDroidApp extends Application
{
	private static final String TAG = KMMDroidApp.class.getSimpleName();
	SQLiteDatabase db;
	Cursor cursor;
	private String fullPath = null;
	private boolean dbOpen = false;
	
	@Override
	public void onCreate()
	{
		super.onCreate();
	}
	
	@Override
	public void onTerminate()
	{
		super.onTerminate();
		
		// Make sure the database is closed.
		db.close();
		dbOpen = false;
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
}