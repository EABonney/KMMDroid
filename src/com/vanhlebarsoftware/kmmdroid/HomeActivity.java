package com.vanhlebarsoftware.kmmdroid;

import java.util.List;

import android.os.Bundle;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class HomeActivity extends FragmentActivity implements
						  LoaderManager.LoaderCallbacks<List<Account>>
{

	private static final String TAG = HomeActivity.class.getSimpleName();
	private static final int HOMEACCOUNTS_LOADER = 0x01;
	KMMDroidApp KMMDapp;
	ImageButton btnHome;
	ImageButton btnAccounts;
	ImageButton btnCategories;
	ImageButton btnInstitutions;
	ImageButton btnPayees;
	ImageButton btnSchedules;
	ImageButton btnReports;
	LinearLayout navBar;
	ListView listAccounts;
	AccountsAdapter adapterAccounts;
	
	/* Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.home);

        // Get our application
        KMMDapp = ((KMMDroidApp) getApplication());
        
        // Find our views
        listAccounts = (ListView) findViewById(R.id.listHomeView);
        btnHome = (ImageButton) findViewById(R.id.buttonHome);
        btnAccounts = (ImageButton) findViewById(R.id.buttonAccounts);
        btnCategories = (ImageButton) findViewById(R.id.buttonCategories);
        btnInstitutions = (ImageButton) findViewById(R.id.buttonInstitutions);
        btnPayees = (ImageButton) findViewById(R.id.buttonPayees);
        btnSchedules = (ImageButton) findViewById(R.id.buttonSchedules);
        btnReports = (ImageButton) findViewById(R.id.buttonReports);
        navBar = (LinearLayout) findViewById(R.id.navBar);
        
        // Set out onClickListener events.
        btnHome.setVisibility(View.GONE);
        
        btnAccounts.setOnClickListener(new View.OnClickListener()
        {
			public void onClick(View arg0)
			{
				startActivity(new Intent(getBaseContext(), AccountsActivity.class));
			}
		});
        
        btnCategories.setOnClickListener(new View.OnClickListener()
        {
			public void onClick(View arg0)
			{
				startActivity(new Intent(getBaseContext(), CategoriesActivity.class));
			}
		});
        
        btnInstitutions.setOnClickListener(new View.OnClickListener()
        {
			public void onClick(View arg0)
			{
				startActivity(new Intent(getBaseContext(), InstitutionsActivity.class));
			}
		});
        
        btnPayees.setOnClickListener(new View.OnClickListener()
        {
			public void onClick(View arg0)
			{
				startActivity(new Intent(getBaseContext(), PayeeActivity.class));
			}
		});
        
        btnSchedules.setOnClickListener(new View.OnClickListener()
        {
			public void onClick(View arg0)
			{
				startActivity(new Intent(getBaseContext(), SchedulesActivity.class));
			}
		});
        
        btnReports.setOnClickListener(new View.OnClickListener()
        {
			public void onClick(View arg0)
			{
				startActivity(new Intent(getBaseContext(), ReportsActivity.class));
			}
		});

    	// Now hook into listAccounts ListView and set its onItemClickListener member
    	// to our class handler object.
        listAccounts.setOnItemClickListener(mMessageClickedHandler);
        

        // See if the database is already open, if not open it Read/Write.
        if(!KMMDapp.isDbOpen())
        {
        	KMMDapp.openDB();
        }
		
		// Let's display the currently opened file in the label
		setTitle(KMMDapp.getFullPath());
		
        // Create an empty adapter we will use to display the loaded data.
        adapterAccounts = new AccountsAdapter(this);
		listAccounts.setAdapter(adapterAccounts);
		
        // Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
        getSupportLoaderManager().initLoader(HOMEACCOUNTS_LOADER, null, this);
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

		//We need to ensure that the user's database is still valid.
		String path = KMMDapp.getFullPath();
		Log.d(TAG, "path: " + path);
		Log.d(TAG, "Did file exist: " + KMMDapp.fileExists(path));
		if( !KMMDapp.fileExists(path) )
		{
			Log.d(TAG, "User lost file, going back to Welcome");
			// clear the users preferences in settings.
			Editor edit = KMMDapp.prefs.edit();
			edit.putBoolean("openLastUsed", false);
			edit.putString("Full Path", "");
			edit.apply();
		
			// make sure that we no longer show the database open or have a path set for it.
			KMMDapp.setFullPath(null);
			KMMDapp.closeDB();
		
			// Send the user to Welcome.
			Intent i = new Intent(this, WelcomeActivity.class);
			i.putExtra("lostPath", path);
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(i);
			finish();
		}
		else
		{		
			// Make sure the database is open and ready.
			if(!KMMDapp.isDbOpen())
				KMMDapp.openDB();
		}
		
		// See if the user has requested the navigation bar.
		if(!KMMDapp.prefs.getBoolean("navBar", false))
			navBar.setVisibility(View.GONE);
		else
			navBar.setVisibility(View.VISIBLE);
	}
	
	// Message Handler for our listAccounts List View clicks
	private OnItemClickListener mMessageClickedHandler = new OnItemClickListener() {
	    public void onItemClick(AdapterView<?> parent, View v, int position, long id)
	    {
	    	Account acct = adapterAccounts.getItem(position); //accounts.get(position);
	    	Intent i = new Intent(getBaseContext(), LedgerActivity.class);
	    	i.putExtra("AccountId", acct.getId());
	    	i.putExtra("AccountName", acct.getName());
	    	i.putExtra("Balance", acct.getBalance());
	    	startActivity(i);
	    }
	};
	
	private class AccountsAdapter extends ArrayAdapter<Account>
	{
		private final LayoutInflater mInflater;
		
		public AccountsAdapter(Context context)
		{
			super(context, R.layout.home_row);
			mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		
		public void setData(List<Account> data) 
	    {
	        clear();
	        if (data != null) 
	        {
	        	for(Account account : data)
	        	{
	        		add(account);
	        	}
	        }
	    }
	    
		public View getView(int position, View convertView, ViewGroup parent)
		{
			View view = convertView;
			if(view == null)
			{
				view = mInflater.inflate(R.layout.home_row, parent, false);
			}
			else
				view = convertView;
			
			Account item = getItem(position);
			
			// Load the items into the view now for this schedule.
			if(item != null)
			{
				TextView desc = (TextView) view.findViewById(R.id.hrAccountName);
				TextView bal = (TextView) view.findViewById(R.id.hrAccountBalance);
				
				desc.setText(item.getName());
				bal.setText(item.getBalance());
			}
			else
				Log.d(TAG, "Never got an Account!");

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
	
	@Override
	public boolean onPrepareOptionsMenu (Menu menu)
	{
		// Hide the sync menu if the user is not logged into at least one of the services.
		if( !KMMDapp.prefs.getBoolean("dropboxSync", false) )
			menu.findItem(R.id.Sync).setVisible(false);
		else
			menu.findItem(R.id.Sync).setVisible(true);
		
		// See if the user wants us to navigation menu items.
		if( !KMMDapp.prefs.getBoolean("navMenu", true))
		{
			menu.findItem(R.id.itemAccounts).setVisible(false);
			menu.findItem(R.id.itemCategories).setVisible(false);
			menu.findItem(R.id.itemPayees).setVisible(false);
			menu.findItem(R.id.itemInstitutions).setVisible(false);
			menu.findItem(R.id.itemSchedules).setVisible(false);
			menu.findItem(R.id.itemReports).setVisible(false);
		}
		else
		{
			menu.findItem(R.id.itemAccounts).setVisible(true);
			menu.findItem(R.id.itemCategories).setVisible(true);
			menu.findItem(R.id.itemPayees).setVisible(true);
			menu.findItem(R.id.itemInstitutions).setVisible(true);
			menu.findItem(R.id.itemSchedules).setVisible(true);
			menu.findItem(R.id.itemReports).setVisible(true);
		}
		
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
			case R.id.itemReports:
				startActivity(new Intent(this, ReportsActivity.class));
				break;
			case R.id.itemAbout:
				startActivity(new Intent(this, AboutActivity.class));
				break;
			case R.id.syncDropbox:
				i = new Intent(this, KMMDDropboxService.class);
				i.putExtra("cloudService", KMMDDropboxService.CLOUD_DROPBOX);
				startService(i);
				break;
		}
		
		return true;
	}
	
	// LoaderManager.LoaderCallbacks<List<Accounts> methods:
    public Loader<List<Account>> onCreateLoader(int id, Bundle args) 
    {
    	setProgressBarIndeterminateVisibility(true);
    	return new HomeLoader(this, args);
    }
    
    public void onLoadFinished(Loader<List<Account>> loader, List<Account> accounts) 
    {
        // Set the new data in the adapter.
    	adapterAccounts.setData(accounts);
    	setProgressBarIndeterminateVisibility(false);
    }
    
    public void onLoaderReset(Loader<List<Account>> loader) 
    {
        // clear the data in the adapter.
    	adapterAccounts.setData(null);
    }
}
