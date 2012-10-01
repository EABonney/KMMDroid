package com.vanhlebarsoftware.kmmdroid;

import java.io.File;
import java.util.Calendar;
import java.util.List;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.exception.DropboxServerException;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session.AccessType;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class PrefsActivity extends PreferenceActivity
{
	private static final String TAG = PrefsActivity.class.getSimpleName();
    private String APP_KEY;
    private String APP_SECRET;
    
    // Change this to DROPBOX if we need access to the users entire Dropbox structure.
    // Use APP_FOLDER to limit access to just that location under Dropbox.
    final static private AccessType ACCESS_TYPE = AccessType.APP_FOLDER;
    final static private String ACCOUNT_PREFS_NAME = "prefs";
    final static private String ACCESS_KEY_NAME = "ACCESS_KEY";
    final static private String ACCESS_SECRET_NAME = "ACCESS_SECRET";
    DropboxAPI<AndroidAuthSession> mApi;
	private final String dbTable = "kmmAccounts";
	private final String[] dbColumns = { "accountName", "id AS _id"};
	private final String strSelection = "(parentId='AStd::Asset' OR parentId='AStd::Liability') AND " +
			"(balance != '0/1')";
	private final String strOrderBy = "parentID, accountName ASC";
	CheckBoxPreference dropboxSync;
	KMMDroidApp KMMDapp;
	Cursor cursor;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.prefs);
        
		// Find our ListPreference for the Home Widget Account to be used.
		ListPreference Accounts = (ListPreference) findPreference("accountUsed");
		dropboxSync = (CheckBoxPreference) findPreference("dropboxSync");
		
        // Load out private key and secret key.
        APP_KEY = getString(R.string.app_key);
        APP_SECRET = getString(R.string.app_secret);
        
        // We create a new AuthSession so that we can use the Dropbox API.
        AppKeyPair appKeys = new AppKeyPair(APP_KEY, APP_SECRET);
        AndroidAuthSession session = new AndroidAuthSession(appKeys, ACCESS_TYPE);
        mApi = new DropboxAPI<AndroidAuthSession>(session);
        
        // Set our OnClickListener events
		dropboxSync.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
		{
			
			public boolean onPreferenceClick(Preference preference)
			{				
				// TODO Auto-generated method stub
				if(dropboxSync.isChecked())
				{   
	                // Start the remote authentication
	                mApi.getSession().startAuthentication(PrefsActivity.this);
				}
				else
				{
					logOut();
				}
				
				return false;
			}
		});

        
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
	public void onResume()
	{
		super.onResume();
		
		boolean needToSync = false;
       	// The next part must be inserted in the onResume() method of the
       	// activity from which session.startAuthentication() was called, so
       	// that Dropbox authentication completes properly.
       	if (mApi.getSession().authenticationSuccessful())
       	{
       		try
       		{
       			Entry info = null;
       			List<Entry> contentsDrop = null;
       			
       			// Mandatory call to complete the auth
       			mApi.getSession().finishAuthentication();

       			AccessTokenPair tokens = mApi.getSession().getAccessTokenPair();
       			
       			// Store it locally in our app for later use
       			//TokenPair tokens = session.getAccessTokenPair();
       			storeKeys(tokens.key, tokens.secret);
       			showToast("Your account has been successfully linked!");
       			
       			// Create our Dropbox folder if it isn't there already.
       			try
       			{
       				//info = mApi.metadata("/Apps/KMMDroid", 0, null, true, null);    				
       				//if( !info.isDir )
       					info = mApi.createFolder("/KMMDroid");
       				//else
       					needToSync = true;
       			}
       			catch( DropboxException e)
       			{
       				needToSync = true;
       				Log.d(TAG, "Error creating our base folder!");
       				e.printStackTrace();
       			}
       		}
       		catch (IllegalStateException e)
       		{
       			showToast("Couldn't authenticate with Dropbox:" + e.getLocalizedMessage());
       			Log.i(TAG, "Error authenticating", e);
       		}
       		
       		// See if we need to now sync our folder from the Cloud
       		if( needToSync )
       		{
				// create a File object for the parent directory
				File KMMDroidDirectory = new File(Environment.getExternalStorageDirectory(), "/KMMDroid");
				// have the object build the directory structure, if needed.
				KMMDroidDirectory.mkdirs();
				
				// Now retrieve any data that we might need.
				Intent i = new Intent(this, KMMDCloudServicesService.class);
				i.putExtra("cloudService", KMMDCloudServicesService.CLOUD_DROPBOX);
				i.putExtra("syncing", true);
				startService(i);
       		}
       	}
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
			final Calendar now = Calendar.getInstance();
    		int intHour = KMMDapp.prefs.getInt("notificationTime.hour", 0);
    		int intMin = KMMDapp.prefs.getInt("notificationTime.minute", 0);
    		updateTime.set(Calendar.HOUR_OF_DAY, intHour);
    		updateTime.set(Calendar.MINUTE, intMin);
    		updateTime.set(Calendar.SECOND, 0);
    		
    		//If the user set a time to before now, set the alarm to go off tomorrow.
    		if(updateTime.before(now))
    			updateTime.add(Calendar.DAY_OF_MONTH, 1);
    		
			//Cancel any alarm we might have setup at this point.
			KMMDapp.setRepeatingAlarm(null, null, KMMDroidApp.ALARM_NOTIFICATIONS);
			//Now set up the correct time that the user has specified.
			KMMDapp.setRepeatingAlarm(null, updateTime, KMMDroidApp.ALARM_NOTIFICATIONS);
		}
		else
			KMMDapp.setRepeatingAlarm(null, null, KMMDroidApp.ALARM_NOTIFICATIONS);
	}
	
	// **************************************************************************************************
	// ************************************ Helper methods ********************************************** 
    private void logOut()
    {
        // Remove credentials from the session
        mApi.getSession().unlink();

        // Clear our stored keys
        clearKeys();
        showToast("Logging out!");
    }
    
    private void showToast(String msg)
    {
        Toast error = Toast.makeText(this, msg, Toast.LENGTH_LONG);
        error.show();
    }

    /**
     * Shows keeping the access keys returned from Trusted Authenticator in a local
     * store, rather than storing user name & password, and re-authenticating each
     * time (which is not to be done, ever).
     *
     * @return Array of [access_key, access_secret], or null if none stored
     */
    private String[] getKeys()
    {
        SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        String key = prefs.getString(ACCESS_KEY_NAME, null);
        String secret = prefs.getString(ACCESS_SECRET_NAME, null);
        if (key != null && secret != null)
        {
        	String[] ret = new String[2];
        	ret[0] = key;
        	ret[1] = secret;
        	return ret;
        }
        else
        {
        	return null;
        }
    }
    
    /**
     * Shows keeping the access keys returned from Trusted Authenticator in a local
     * store, rather than storing user name & password, and re-authenticating each
     * time (which is not to be done, ever).
     */
    private void storeKeys(String key, String secret) 
    {
        // Save the access key for later
        SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        Editor edit = prefs.edit();
        edit.putString(ACCESS_KEY_NAME, key);
        edit.putString(ACCESS_SECRET_NAME, secret);
        edit.commit();
    }

    private void clearKeys() 
    {
        SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        Editor edit = prefs.edit();
        edit.clear();
        edit.commit();
    }
}
