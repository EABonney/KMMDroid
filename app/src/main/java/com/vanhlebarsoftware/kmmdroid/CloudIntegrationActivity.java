package com.vanhlebarsoftware.kmmdroid;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session.AccessType;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class CloudIntegrationActivity extends FragmentActivity 
{
    private static final String TAG = CloudIntegrationActivity.class.getSimpleName();
    private String APP_KEY;
    private String APP_SECRET;
    
    // Change this to DROPBOX if we need access to the users entire Dropbox structure.
    // Use APP_FOLDER to limit access to just that location under Dropbox.
    final static private AccessType ACCESS_TYPE = AccessType.DROPBOX;
    final static private String ACCOUNT_PREFS_NAME = "prefs";
    final static private String ACCESS_KEY_NAME = "ACCESS_KEY";
    final static private String ACCESS_SECRET_NAME = "ACCESS_SECRET";
    DropboxAPI<AndroidAuthSession> mApi;
    Button btnLogin;
    Button btnLogout;
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cloudintegration);
        
        // Load out private key and secret key.
        APP_KEY = getString(R.string.app_key);
        APP_SECRET = getString(R.string.app_secret);
        
        // Get our views
        btnLogin = (Button) findViewById(R.id.buttonLogin);
        btnLogout = (Button) findViewById(R.id.buttonLogout);
 
        // We create a new AuthSession so that we can use the Dropbox API.
        AppKeyPair appKeys = new AppKeyPair(APP_KEY, APP_SECRET);
        AndroidAuthSession session = new AndroidAuthSession(appKeys, ACCESS_TYPE);
        mApi = new DropboxAPI<AndroidAuthSession>(session);
        
        // Set our OnClickListener events
        btnLogin.setOnClickListener(new View.OnClickListener() 
        {	
			public void onClick(View v)
			{
                // Start the remote authentication
                mApi.getSession().startAuthentication(CloudIntegrationActivity.this);
			}
		});
        
        btnLogout.setOnClickListener(new View.OnClickListener() 
        {	
			public void onClick(View v)
			{
				logOut();
			}
		});
    }
    
    @Override
    protected void onResume()
    {
        super.onResume();

       	//AndroidAuthSession session = mApi.getSession();

       	// The next part must be inserted in the onResume() method of the
       	// activity from which session.startAuthentication() was called, so
       	// that Dropbox authentication completes properly.
       	if (mApi.getSession().authenticationSuccessful())
       	{
       		try
       		{
       			Entry info = null;
       			
       			// Mandatory call to complete the auth
       			mApi.getSession().finishAuthentication();

       			AccessTokenPair tokens = mApi.getSession().getAccessTokenPair();
       			
       			// Store it locally in our app for later use
       			storeKeys(tokens.key, tokens.secret);
       			showToast(getString(R.string.messageLinkSuccessful));
       			
       			// Create our Dropbox folder if it isn't there already.
       			try
       			{
       				info = mApi.createFolder("/KMMDroid");
       			}
       			catch( DropboxException e)
       			{
       				Log.d(TAG, "Error creating our base folder! - " + e.getMessage());
       			}
       			finally
       			{
       				Log.d(TAG, "Successfully created our folder at: " + info.path);
       			}
       		}
       		catch (IllegalStateException e)
       		{
       			showToast(getString(R.string.messageDropboxLinkError) + e.getLocalizedMessage());
       			Log.i(TAG, "Error authenticating", e);
       		}
       	}
    }
    
	// **************************************************************************************************
	// ************************************ Helper methods **********************************************    
    private void logOut()
    {
        // Remove credentials from the session
        mApi.getSession().unlink();

        // Clear our stored keys
        clearKeys();
        showToast(getString(R.string.messageLogOut));
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