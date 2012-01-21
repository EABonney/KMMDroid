package com.vanhlebarsoftware.kmmdroid;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.android.AuthActivity;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.TokenPair;
import com.dropbox.client2.session.Session.AccessType;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class DropboxActivity extends Activity 
{
    private static final String TAG = "DropboxActivity";
    private String APP_KEY;
    private String APP_SECRET;
    
    // Change this to DROPBOX if we need access to the users entire Dropbox structure.
    // Use APP_FOLDER to limit access to just that location under Dropbox.
    final static private AccessType ACCESS_TYPE = AccessType.APP_FOLDER;
    final static private String ACCOUNT_PREFS_NAME = "prefs";
    final static private String ACCESS_KEY_NAME = "ACCESS_KEY";
    final static private String ACCESS_SECRET_NAME = "ACCESS_SECRET";
    DropboxAPI<AndroidAuthSession> mApi;
    private boolean mLoggedIn = false;
    Button btnLogin;
    Button btnLogout;
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dropbox);
        
        // Load out private key and secret key.
        APP_KEY = getString(R.string.app_key);
        APP_SECRET = getString(R.string.app_secret);
        
        // Get our views
        btnLogin = (Button) findViewById(R.id.buttonLogin);
        btnLogout = (Button) findViewById(R.id.buttonLogout);
 
        // We create a new AuthSession so that we can use the Dropbox API.
        AndroidAuthSession session = buildSession();
        mApi = new DropboxAPI<AndroidAuthSession>(session);
        checkAppKeySetup();
        
        // Set our OnClickListener events
        btnLogin.setOnClickListener(new View.OnClickListener() 
        {	
			public void onClick(View v)
			{
				// TODO Auto-generated method stub
                // Start the remote authentication
                mApi.getSession().startAuthentication(DropboxActivity.this);
		        mLoggedIn = true;
			}
		});
        
        btnLogout.setOnClickListener(new View.OnClickListener() 
        {	
			public void onClick(View v)
			{
				// TODO Auto-generated method stub
				logOut();
				mLoggedIn = false;
			}
		});
    }
    
    @Override
    protected void onResume()
    {
        super.onResume();

       	AndroidAuthSession session = mApi.getSession();

       	// The next part must be inserted in the onResume() method of the
       	// activity from which session.startAuthentication() was called, so
       	// that Dropbox authentication completes properly.
       	if (session.authenticationSuccessful())
       	{
       		try
       		{
       			// Mandatory call to complete the auth
       			session.finishAuthentication();

       			// Store it locally in our app for later use
       			TokenPair tokens = session.getAccessTokenPair();
       			storeKeys(tokens.key, tokens.secret);
       			showToast("Your account has been successfully linked!");
       		}
       		catch (IllegalStateException e)
       		{
       			showToast("Couldn't authenticate with Dropbox:" + e.getLocalizedMessage());
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
        showToast("Logging out!");
    }

    private void checkAppKeySetup()
    {
        // Check if the app has set up its manifest properly.
        Intent testIntent = new Intent(Intent.ACTION_VIEW);
        String scheme = "db-" + APP_KEY;
        String uri = scheme + "://" + AuthActivity.AUTH_VERSION + "/test";
        testIntent.setData(Uri.parse(uri));
        PackageManager pm = getPackageManager();
        if (0 == pm.queryIntentActivities(testIntent, 0).size())
        {
            showToast("URL scheme in your app's " +
                    "manifest is not set up correctly. You should have a " +
                    "com.dropbox.client2.android.AuthActivity with the " +
                    "scheme: " + scheme);
            finish();
        }
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

    private AndroidAuthSession buildSession() 
    {
        AppKeyPair appKeyPair = new AppKeyPair(APP_KEY, APP_SECRET);
        AndroidAuthSession session;

        String[] stored = getKeys();
        if (stored != null) 
        {
            AccessTokenPair accessToken = new AccessTokenPair(stored[0], stored[1]);
            session = new AndroidAuthSession(appKeyPair, ACCESS_TYPE, accessToken);
        } 
        else 
        {
            session = new AndroidAuthSession(appKeyPair, ACCESS_TYPE);
        }

        return session;
    }
}
