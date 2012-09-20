package com.vanhlebarsoftware.kmmdroid;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.DropboxFileInfo;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session.AccessType;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

public class KMMDCloudServicesService extends Service 
{
	private static final String TAG = KMMDCloudServicesService.class.getSimpleName();
    final static private String ACCOUNT_PREFS_NAME = "prefs";
    final static private String ACCESS_KEY_NAME = "ACCESS_KEY";
    final static private String ACCESS_SECRET_NAME = "ACCESS_SECRET";
	public static final int CLOUD_DROPBOX = 0;
	public static final int CLOUD_GOOGLEDRIVE = 1;
	public static final int CLOUD_UBUNTUONE = 2;
	private KMMDDropboxUpdater kmmdDropbox;
	private NotificationManager kmmdNotifcationMgr;
	private Notification kmmdNotification;
	private KMMDroidApp kmmdApp;
	DropboxAPI<AndroidAuthSession> mApi;
	boolean m_LoggedIn = false;
	boolean loggingIn = false;
	boolean loggingOut = false;
	boolean syncing = false;
	
	@Override
	public void onCreate() 
	{
		super.onCreate();
		this.kmmdApp = (KMMDroidApp) getApplication();
		this.kmmdDropbox = new KMMDDropboxUpdater();
		
		// First let get the authorization keys that have been stored.
		String[] token = getKeys();
		AccessTokenPair access;
		// We create a new AuthSession so that we can use the Dropbox API.
		AppKeyPair appKeys = new AppKeyPair(getString(R.string.app_key), getString(R.string.app_secret));
		AndroidAuthSession session = new AndroidAuthSession(appKeys, AccessType.DROPBOX);
		mApi= new DropboxAPI<AndroidAuthSession>(session);
		if(token != null)
		{
			access = new AccessTokenPair(token[0], token[1]);
        	mApi.getSession().setAccessTokenPair(access);
        	m_LoggedIn = true;
		}
	}

	@Override
	public void onDestroy() 
	{
		super.onDestroy();

	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) 
	{
		super.onStartCommand(intent, flags, startId);

		Bundle extras = intent.getExtras();
		//Get the service we are going to login/logout from and/or to sync with
		//along with the actual action we are going to perform.
		int cloudService = extras.getInt("cloudService");
		syncing = extras.getBoolean("syncing");
		
		switch(cloudService)
		{
		case CLOUD_DROPBOX:
			kmmdDropbox.start();
			break;
		case CLOUD_GOOGLEDRIVE:
			break;
		case CLOUD_UBUNTUONE:
			break;
		}

		return START_NOT_STICKY;
	}
	@Override
	public IBinder onBind(Intent intent) 
	{
		// TODO Auto-generated method stub
		return null;
	}

	/****************************************************************************************************************
	 * Helper Functions
	 ***************************************************************************************************************/	
	private void performSync(int service)
	{
		download();
	}
	
	private void upload()
	{
		
	}
	
	private void download()
	{
        SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        Editor edit = prefs.edit();
		Entry info = null;
		List<Entry> contentsDrop = null;
		
		// Get our files from the server to see what needs to be sync'd
		try 
		{
			info = mApi.metadata("/KMMDroid", 50, null, true, null);
			contentsDrop = info.contents;
		}
		catch (DropboxException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Now we need to download the database file from our Dropbox folder.
		FileOutputStream outputStream = null;
		for(int i=0; i<contentsDrop.size(); i++)
		{
			try 
			{
				// create a File object for the parent directory
				File KMMDroidDirectory = new File(Environment.getExternalStorageDirectory(), "/KMMDroid");
				File file = new File(KMMDroidDirectory, contentsDrop.get(i).fileName());
				outputStream = new FileOutputStream(file);
				// First see if this file has already been downloaded to our device and if the rev codes match, if not then we can retrieve it.
				String revCode = prefs.getString(contentsDrop.get(i).fileName(), null);
				if( revCode == null )
				{
					// Get the actual file from the service
					DropboxFileInfo fileInfo = mApi.getFile("/KMMDroid/" + contentsDrop.get(i).fileName(), null, outputStream, null);
					// Now store the file name along with the rev code so we have it for later.
					edit.putString(contentsDrop.get(i).fileName(), fileInfo.getMetadata().rev);
					edit.apply();
					Log.d(TAG, "Sucessfully downloaded: " + fileInfo.getMetadata().fileName() + " from Dropbox!");
					Log.d(TAG, "The file's rev is: " + fileInfo.getMetadata().rev);
				}
				else
				{
					// We don't need to do anything so let's just log it for now.
					Log.d(TAG, "We have the most current version of " + contentsDrop.get(i).fileName());
					break; // do nothing on go back to the next file if any.					
				}
			} 
			catch (DropboxException e) 
			{
				Log.e("DbExampleLog", "Something went wrong while downloading.");
			} 
			catch (FileNotFoundException e) 
			{
				Log.e("DbExampleLog", "File not found.");
			} 
			finally 
			{
				if (outputStream != null) 
				{
					try 
					{
						outputStream.close();
					} 
					catch (IOException e) {}
				}
			}
		}		
	}
	
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
	/**************************************************************************************************************
	 * Thread that will perform the actual syncing with Dropbox
	 *************************************************************************************************************/
	private class KMMDDropboxUpdater extends Thread
	{

		public KMMDDropboxUpdater()
		{
			super("KMMDDropboxUpdater-Updater");
		}
		
		@Override
		public void run()
		{
			performSync(CLOUD_DROPBOX);			
			stopSelf();
		}
	}
}
