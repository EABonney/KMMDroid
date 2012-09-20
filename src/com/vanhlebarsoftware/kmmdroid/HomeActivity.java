package com.vanhlebarsoftware.kmmdroid;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.DropboxFileInfo;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.exception.DropboxUnlinkedException;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session.AccessType;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class HomeActivity extends Activity
{

	private static final String TAG = "HomeActivity";
    final static private String ACCOUNT_PREFS_NAME = "prefs";
    final static private String ACCESS_KEY_NAME = "ACCESS_KEY";
    final static private String ACCESS_SECRET_NAME = "ACCESS_SECRET";
	private static final int C_ACCOUNTNAME = 0;
	private static final int C_BALANCE = 1;
	private static final int C_ID = 2;
	private static final String dbTable = "kmmAccounts";
	private static final String[] dbColumns = { "accountName", "balanceFormatted", "id AS _id"};
	private static final String strSelection = "(parentId='AStd::Asset' OR parentId='AStd::Liability') AND " +
			"(balance != '0/1')";
	private static final String strOrderBy = "parentID, accountName ASC";
	static final String[] FROM = { "accountName", "balanceFormatted" };
	static final int[] TO = { R.id.hrAccountName, R.id.hrAccountBalance };
	DropboxAPI<AndroidAuthSession> mApi;
	KMMDroidApp KMMDapp;
	Cursor cursor;
	ListView listAccounts;
	ArrayList<Accounts> accounts = new ArrayList<Accounts>();
	AccountsAdapter adapterAccounts;
	private boolean m_LoggedIn = false;
	
	/* Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        // Get our application
        KMMDapp = ((KMMDroidApp) getApplication());
        
        // Find our views
        listAccounts = (ListView) findViewById(R.id.listHomeView);

    	// Now hook into listAccounts ListView and set its onItemClickListener member
    	// to our class handler object.
        listAccounts.setOnItemClickListener(mMessageClickedHandler);

        // See if the database is already open, if not open it Read/Write.
        if(!KMMDapp.isDbOpen())
        {
        	KMMDapp.openDB();
        }
        
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
	protected void onResume()
	{
		super.onResume();

		
		// Make sure our ArrayLists are clear.
		accounts.clear();
		
		// Make sure the database is open and ready.
		if(!KMMDapp.isDbOpen())
			KMMDapp.openDB();
		
		//Get all the accounts to be displayed.
		cursor = KMMDapp.db.query(dbTable, dbColumns, strSelection, null, null, null, strOrderBy);
		cursor.moveToFirst();
		
		// Loop over the cursor to build the accounts ArrayList and adjust each account for possible furture transactions.
		for(int i=0; i < cursor.getCount(); i++)
		{
			accounts.add(new Accounts(cursor.getString(C_ID), cursor.getString(C_ACCOUNTNAME),
					Account.adjustForFutureTransactions(cursor.getString(C_ID), cursor.getString(C_BALANCE), KMMDapp.db)));
			cursor.moveToNext();
		}
		
		// close the cursor since we don't need it anymore
		cursor.close();
		
		// Set up the adapter
		adapterAccounts = new AccountsAdapter(this, R.layout.home_row, accounts);
		listAccounts.setAdapter(adapterAccounts);
		
/*		Entry info = null;
		List<Entry> contentsDrop = null;
		if( m_LoggedIn )
		{
			//Just for a test see if we can get a list of all the files in our KMyMoney Dropbox folder.
			try
			{
				//File file = new File("/mnt/sdcard/tmp/kmmdroid.txt");
				//outputStream = new FileOutputStream(file);
				info = mApi.metadata("/KMMDroid", 50, null, true, null);
				contentsDrop = info.contents;
				Log.d(TAG, "Path from Dropbox:" + info.path);
				for(int i=0; i<contentsDrop.size(); i++)
					Log.d(TAG, "File found: " + contentsDrop.get(i).fileName());
			}
			catch (DropboxException e)
			{
				Log.d(TAG, "Something went wrong while downloading.");
				Log.d(TAG, "DropboxException: " + e.getMessage());
			}
			// Now let's try to upload the current database from the root of the sdcard but only if the
			// hash is different OR if the file doesn't even exist on the server.
			// Uploading content.
			FileInputStream inputStream = null;
			SharedPreferences.Editor editor = KMMDapp.prefs.edit();
			try {
				String filePath = KMMDapp.prefs.getString("Full Path", null);
				String[] fileName = filePath.split("/");
			    File file = new File(filePath);
			    inputStream = new FileInputStream(file);
			    String savedRev = KMMDapp.prefs.getString(fileName[fileName.length-1], null);
			    Entry newEntry = mApi.putFile("/KMMDroid/" + fileName[fileName.length-1], inputStream,
			            file.length(), savedRev, null);
			    Log.i(TAG, "The uploaded file's rev is: " + newEntry.rev);
			    // Need to store the key value pair of the file name and rev hash in the prefences so that we can use it later
			    // to see if the file has changed or not to see if we need to redownload or upload the file.
				editor.putString(newEntry.fileName(), newEntry.rev);
				editor.apply();
			} 
			catch (DropboxUnlinkedException e) 
			{
			    // User has unlinked, ask them to link again here.
			    Log.e("DbExampleLog", "User has unlinked.");
			} 
			catch (DropboxException e) 
			{
			    Log.e("DbExampleLog", "Something went wrong while uploading.");
			} 
			catch (FileNotFoundException e) 
			{
			    Log.e("DbExampleLog", "File not found.");
			} 
			finally 
			{
			    if (inputStream != null) 
			    {
			        try 
			        {
			            inputStream.close();
			        } 
			        catch (IOException e) {}
			    }
			}
		}
		*/
	}
	
	// Message Handler for our listAccounts List View clicks
	private OnItemClickListener mMessageClickedHandler = new OnItemClickListener() {
	    public void onItemClick(AdapterView<?> parent, View v, int position, long id)
	    {
	    	Accounts acct = accounts.get(position);
	    	Intent i = new Intent(getBaseContext(), LedgerActivity.class);
	    	i.putExtra("AccountId", acct.getId());
	    	i.putExtra("AccountName", acct.getName());
	    	i.putExtra("Balance", acct.getBalance());
	    	startActivity(i);
	    }
	};
	
	private class AccountsAdapter extends ArrayAdapter<Accounts>
	{
		private ArrayList<Accounts> items;
		private Context context;
		
		public AccountsAdapter(Context context, int textViewResourceId, ArrayList<Accounts> items)
		{
			super(context, textViewResourceId, items);
			this.context = context;
			this.items = items;
		}
		
		public View getView(int position, View convertView, ViewGroup parent)
		{
			View view = convertView;
			if(view == null)
			{
				LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				view = inflater.inflate(R.layout.home_row, null);
			}
			
			Accounts item = items.get(position);
			// Load the items into the view now for this schedule.
			if(item != null)
			{
				TextView desc = (TextView) view.findViewById(R.id.hrAccountName);
				TextView bal = (TextView) view.findViewById(R.id.hrAccountBalance);
				
				desc.setText(item.getName());
				bal.setText(item.getBalance());
			}
			else
				Log.d(TAG, "Never got a Schedule!");			
			return view;
		}
	}
	
	// Called first time the user clicks on the menu button
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.home_menu, menu);
		return true;
	}
	
	// Called when an options item is clicked
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.itemAccounts:
				startActivity(new Intent(this, AccountsActivity.class));
				break;
			case R.id.itemPayees:
				startActivity(new Intent(this, PayeeActivity.class));
				break;
			case R.id.itemCategories:
				startActivity(new Intent(this, CategoriesActivity.class));
				break;
			case R.id.itemInstitutions:
				startActivity(new Intent(this, InstitutionsActivity.class));
				break;
			case R.id.itemPrefs:
				startActivity(new Intent(this, PrefsActivity.class));
				break;
			case R.id.close:
				KMMDapp.closeDB();
		    	Intent i = new Intent(getBaseContext(), WelcomeActivity.class);
				i.putExtra("Closing", true);
		    	startActivity(i);
				finish();
				break;
			case R.id.itemSchedules:
				startActivity(new Intent(this, SchedulesActivity.class));
				break;
			case R.id.itemCashRequirments:
				startActivity(new Intent(this, CashRequirementsOptionsActivity.class));
				break;
			case R.id.itemAbout:
				startActivity(new Intent(this, AboutActivity.class));
				break;
		}
		
		return true;
	}
	
	/***********************************************************************************
	 * 
	 * 									Helper Functions
	 */

	private class Accounts extends ArrayList<Object>
	{
		private static final long serialVersionUID = -826625428929646643L;
		private String id;
		private String Name;
		private String Balance;
		
		Accounts(String accountId, String accountName, String accountBal)
		{
			this.id = accountId;
			this.Name = accountName;
			this.Balance = accountBal;
		}
		
		public String getId()
		{
			return this.id;
		}
		
		public String getName()
		{
			return this.Name;
		}
		
		public String getBalance()
		{
			return this.Balance;
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
}
