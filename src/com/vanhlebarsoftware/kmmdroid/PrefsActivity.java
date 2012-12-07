package com.vanhlebarsoftware.kmmdroid;

import java.io.File;
import java.util.Calendar;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.exception.DropboxServerException;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session.AccessType;

import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.widget.Toast;

public class PrefsActivity extends PreferenceActivity
{
	private static final String TAG = PrefsActivity.class.getSimpleName();
    private String APP_KEY;
    private String APP_SECRET;
    private Boolean m_LoggedIn = false;
    private Boolean wifiConnected = false;
    private Boolean mobileConnected = false;
    
    // Change this to DROPBOX if we need access to the users entire Dropbox structure.
    // Use APP_FOLDER to limit access to just that location under Dropbox.
    final static private AccessType ACCESS_TYPE = AccessType.APP_FOLDER;
    //final static private String ACCOUNT_PREFS_NAME = "prefs";
    final static private String ACCESS_KEY_NAME = "ACCESS_KEY";
    final static private String ACCESS_SECRET_NAME = "ACCESS_SECRET";
    DropboxAPI<AndroidAuthSession> mApi;
	CheckBoxPreference dropboxSync;
	KMMDroidApp KMMDapp;
	Cursor cursor;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.prefs);
        
        // Get our application
        KMMDapp = ((KMMDroidApp) getApplication());
        
		// Find our ListPreference for the Home Widget Account to be used.
		dropboxSync = (CheckBoxPreference) findPreference("dropboxSync");
		
        // Load out private key and secret key.
        APP_KEY = getString(R.string.app_key);
        APP_SECRET = getString(R.string.app_secret);
        
		// First let get the authorization keys that have been stored.
		String[] token = getKeys();
		AccessTokenPair access;
        // We create a new AuthSession so that we can use the Dropbox API.
        AppKeyPair appKeys = new AppKeyPair(APP_KEY, APP_SECRET);
        AndroidAuthSession session = new AndroidAuthSession(appKeys, ACCESS_TYPE);
        mApi = new DropboxAPI<AndroidAuthSession>(session);
		if(token != null)
		{
			access = new AccessTokenPair(token[0], token[1]);
        	mApi.getSession().setAccessTokenPair(access);
        	m_LoggedIn = true;
		}
		
        // Set our OnClickListener events
		dropboxSync.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
		{
			
			public boolean onPreferenceClick(Preference preference)
			{				
				if(dropboxSync.isChecked())
				{   
					// We need to see if we have an internet connection.
					updateConnectedFlags();
					if( wifiConnected || mobileConnected )
					{
						// Start the remote authentication
						KMMDCloudThread kmmdCloudThread = new KMMDCloudThread(KMMDDropboxService.CLOUD_DROPBOX, KMMDCloudThread.AUTHENTICATE);
						kmmdCloudThread.run();
					}
					else
					{
						showToast("Sorry but we don't have an internet connection, please turn one on and try again.");
						dropboxSync.setChecked(false);
					}
				}
				else
				{
					logOut();
					m_LoggedIn = false;
				}
				
				return false;
			}
		});
        
        // See if the database is already open, if not open it Read/Write.
        if(!KMMDapp.isDbOpen())
        {
        	KMMDapp.openDB();
        }
	}
	
	@Override
	public void onResume()
	{
		super.onResume();
		
		updateConnectedFlags();
       	// The next part must be inserted in the onResume() method of the
       	// activity from which session.startAuthentication() was called, so
       	// that Dropbox authentication completes properly.
		if( !m_LoggedIn && (wifiConnected || mobileConnected) )
		{
			if (mApi.getSession().authenticationSuccessful())
			{
				try
				{       			
					// Mandatory call to complete the auth
					mApi.getSession().finishAuthentication();

					AccessTokenPair tokens = mApi.getSession().getAccessTokenPair();
       			
					// Store it locally in our app for later use
					storeKeys(tokens.key, tokens.secret);
					showToast("Your account has been successfully linked!");
				}
				catch (IllegalStateException e)
				{
					showToast("Couldn't authenticate with Dropbox:" + e.getLocalizedMessage());
					Log.i(TAG, "Error authenticating", e);
				}

				// create a File object for the parent directory
				File KMMDroidDirectory = new File(Environment.getExternalStorageDirectory(), "/KMMDroid");
				// have the object build the directory structure, if needed.
				KMMDroidDirectory.mkdirs();
			}
		}
		else if( (!wifiConnected && !mobileConnected) )
		{
			showToast("Sorry but we don't have an internet connection, please turn one on and try again.");
		}
	}
	
	@Override
	protected void onPause() 
	{
		super.onPause();
		
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
        //SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        String key = KMMDapp.prefs.getString(ACCESS_KEY_NAME, null);
        String secret = KMMDapp.prefs.getString(ACCESS_SECRET_NAME, null);
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
        Editor edit = KMMDapp.prefs.edit();
        edit.putString(ACCESS_KEY_NAME, key);
        edit.putString(ACCESS_SECRET_NAME, secret);
        edit.commit();
    }

    private void clearKeys() 
    {
        Editor edit = KMMDapp.prefs.edit();
        edit.clear();
        edit.commit();
    }
    
    // Check the networks connect and set the wifiConnected and mobileConnected variables accordingly.
    public void updateConnectedFlags()
    {
    	ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    	
    	NetworkInfo activeInfo = connMgr.getActiveNetworkInfo();
    	
    	if( activeInfo != null && activeInfo.isConnected() )
    	{
    		wifiConnected = activeInfo.getType() == ConnectivityManager.TYPE_WIFI;
    		mobileConnected = activeInfo.getType() == ConnectivityManager.TYPE_MOBILE;
    	}
    	else
    	{
    		wifiConnected = false;
    		mobileConnected = false;
    	}
    }
    /**************************************************************************************************************
	 * Thread that will perform the actual authentication with the various services and perform server setup.
	 *************************************************************************************************************/
	private class KMMDCloudThread extends Thread
	{
		private final static int AUTHENTICATE = 0;
		private final static int SETUPSERVER = 1;
		private final int option;
		private final int service;
		
		public KMMDCloudThread(int service, int option)
		{
			super("KMMDCloudThread-Updater");
			this.service = service;
			this.option = option;
		}
		
		@Override
		public void run()
		{
			switch(this.option)
			{
			case AUTHENTICATE:
				doAuthentication(this.service);
				break;
			case SETUPSERVER:
				doSetup(this.service);
				break;
			}
		}
		
		private void doAuthentication(int service)
		{
			switch(service)
			{
				case KMMDDropboxService.CLOUD_DROPBOX:
	                mApi.getSession().startAuthentication(PrefsActivity.this);
	                doSetup(this.service);
					break;
				case KMMDDropboxService.CLOUD_GOOGLEDRIVE:
					break;
				case KMMDDropboxService.CLOUD_UBUNTUONE:
						break;
			}
		}
		
		private void doSetup(int service)
		{
			switch(service)
			{
				case KMMDDropboxService.CLOUD_DROPBOX:
			    	Entry info = null;
			    	
					// Create our Dropbox folder if it isn't there already.
					try
					{
						info = mApi.metadata("/", 0, null, false, null);    				
						if( !info.isDir )
						{
							info = mApi.createFolder("");
						}
					}
					catch( DropboxServerException e)
					{
						Log.d(TAG, "Server error: " + e.getMessage());
						e.printStackTrace();
					}
					catch( DropboxException e)
					{
						Log.d(TAG, "Error creating our base folder!");
						e.printStackTrace();
					}
					break;
				case KMMDDropboxService.CLOUD_GOOGLEDRIVE:
					break;
				case KMMDDropboxService.CLOUD_UBUNTUONE:
						break;
			}			
		}
	}
}