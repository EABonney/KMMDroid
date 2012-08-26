package com.vanhlebarsoftware.kmmdroid;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.util.AttributeSet;
import android.util.Log;

public class PrefsActivity extends PreferenceActivity
{
	private static final String TAG = PrefsActivity.class.getSimpleName();
	private final String dbTable = "kmmAccounts";
	private final String[] dbColumns = { "accountName", "id AS _id"};
	private final String strSelection = "(parentId='AStd::Asset' OR parentId='AStd::Liability') AND " +
			"(balance != '0/1')";
	private final String strOrderBy = "parentID, accountName ASC";
	KMMDroidApp KMMDapp;
	Cursor cursor;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.prefs);

		// Find our ListPreference for the Home Widget Account to be used.
		ListPreference Accounts = (ListPreference) findPreference("accountUsed");
		
        // Get our application
        KMMDapp = ((KMMDroidApp) getApplication());
        
        // See if the database is already open, if not open it Read/Write.
        if(!KMMDapp.isDbOpen())
        {
        	KMMDapp.openDB();
        }
        
        // Get the accounts to populate the ListPreference with.
		cursor = KMMDapp.db.query(dbTable, dbColumns, strSelection, null, null, null, strOrderBy);
		startManagingCursor(cursor);
		cursor.moveToFirst();
		
		Log.d(TAG, "Cursor size: " + cursor.getCount());
		CharSequence[] entries = new CharSequence[1];
		CharSequence[] entryValues = new CharSequence[1];
		entries[0] = "No accounts found!";
		entryValues[0] = "";
		
		if(cursor.getCount() > 0)
		{
			entries = new CharSequence[cursor.getCount()];
			entryValues = new CharSequence[cursor.getCount()];
			
			int i=0;
			while(!cursor.isAfterLast())
			{
				entries[i] = cursor.getString(0);
				entryValues[i] = cursor.getString(1);
				Log.d(TAG, "Account name: " + cursor.getString(0));
				Log.d(TAG, "Account Id: " + cursor.getString(1));
				cursor.moveToNext();
				i++;
			}
		}
		
		Accounts.setEntries(entries);
		Accounts.setEntryValues(entryValues);
	}
	
	@Override
	protected void onPause() 
	{
		super.onPause();
		
		// First see what we need to do if anything with the HomeWidgets
		String value = KMMDapp.prefs.getString("updateFrequency", "");
		if(value.equals("Auto"))
			KMMDapp.setAutoUpdate(true);
		else
			KMMDapp.setAutoUpdate(false);
		
		if(KMMDapp.getAutoUpdate())
		{
			//Cancel any alarm we might have setup at this point.
			KMMDapp.setRepeatingAlarm("0", null, KMMDroidApp.ALARM_HOMEWIDGET);
			Intent intent = new Intent(KMMDService.DATA_CHANGED);
			sendBroadcast(intent, KMMDService.RECEIVE_HOME_UPDATE_NOTIFICATIONS);			
		}
		else		
			KMMDapp.setRepeatingAlarm(value, null, KMMDroidApp.ALARM_HOMEWIDGET);
		
		// Now see if the user has specified they want to get Schedule Notifications.
		if(KMMDapp.prefs.getBoolean("receiveNotifications", false))
		{
			final Calendar updateTime = Calendar.getInstance();
			//Cancel any alarm we might have setup at this point.
			KMMDapp.setRepeatingAlarm(null, null, KMMDroidApp.ALARM_NOTIFICATIONS);
			//Now set up the correct time that the user has specified.
			KMMDapp.setRepeatingAlarm(null, updateTime, KMMDroidApp.ALARM_NOTIFICATIONS);
		}
		else
			KMMDapp.setRepeatingAlarm(null, null, KMMDroidApp.ALARM_NOTIFICATIONS);
	}
}
