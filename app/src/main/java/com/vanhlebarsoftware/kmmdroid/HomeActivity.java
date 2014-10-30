package com.vanhlebarsoftware.kmmdroid;

import java.util.List;

import android.os.Bundle;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.DrawerLayout;
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
import android.widget.Toast;

public class HomeActivity extends FragmentActivity /*implements
						  LoaderManager.LoaderCallbacks<List<Account>>*/
{

	private static final String TAG = HomeActivity.class.getSimpleName();
	private static final int HOMEACCOUNTS_LOADER = 0x01;
	private String[] mDrawerListItems = {null, null, null, null, null, null};
	private CharSequence mDrawerTitle;
	private CharSequence mTitle;
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;
	KMMDroidApp KMMDapp;
	//LinearLayout navBar;
	ListView listAccounts;
	AccountsAdapter adapterAccounts;
	HomeLoaderCallbacks homeloaderCallback;
	
	/* Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.home);

        // Get our application
        KMMDapp = ((KMMDroidApp) getApplication());
        
        // Set the titles of the Drawer and the ActionBar
        mTitle = mDrawerTitle = getTitle();
        
        // Populate our Drawer items.
        mDrawerListItems[0] = getString(R.string.titleAccounts);
        mDrawerListItems[1] = getString(R.string.titleCategories);
        mDrawerListItems[2] = getString(R.string.titleInstitutions);
        mDrawerListItems[3] = getString(R.string.titlePayees);
        mDrawerListItems[4] = getString(R.string.titleSchedules);
        mDrawerListItems[5] = getString(R.string.titleReports);
        
        // Find our views
        listAccounts = (ListView) findViewById(R.id.listHomeView);
        //navBar = (LinearLayout) findViewById(R.id.navBar);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
        		R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close)
        {
        	// Called when a drawer has settled in a completely closed state.
        	public void onDrawerClosed(View view)
        	{
        		super.onDrawerClosed(view);
        		getActionBar().setTitle(mTitle);
        		invalidateOptionsMenu();	// creates call to onPrepareOptionsMenu()
        	}
        	
        	// Called when a drawer has settled in a completely open state.
        	public void onDrawerOpened(View drawerView)
        	{
        		super.onDrawerOpened(drawerView);
        		getActionBar().setTitle(mDrawerTitle);
        		invalidateOptionsMenu();	// creates call to onPrepareOptionsMenu()
        	}
        };
        
        // Set out onClickListener events.
        // Set the Drawer listeners
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        
        // Set the ActionBar items
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
        
    	// Now hook into listAccounts ListView and set its onItemClickListener member
    	// to our class handler object.
        listAccounts.setOnItemClickListener(mMessageClickedHandler);
        

        // See if the database is already open, if not open it Read/Write.
        if(!KMMDapp.isDbOpen())
        {
        	KMMDapp.openDB();
        }
		
		// Let's display the currently opened file in the label
		//setTitle(KMMDapp.getFullPath());
		
        // Create an empty adapter we will use to display the loaded data.
        adapterAccounts = new AccountsAdapter(this);
		listAccounts.setAdapter(adapterAccounts);
		
		// Create the adapter for the navigation DrawerList
		mDrawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list_item, mDrawerListItems));
		
        // Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
		homeloaderCallback = new HomeLoaderCallbacks(this);
        getSupportLoaderManager().initLoader(HOMEACCOUNTS_LOADER, null, homeloaderCallback);
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
	}
	
	@Override
	protected void onPostCreate(Bundle savedInstanceState)
	{
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);
		mDrawerToggle.onConfigurationChanged(newConfig);
	}
	
	// Message Handler for our listAccounts List View clicks
	private OnItemClickListener mMessageClickedHandler = new OnItemClickListener() {
	    public void onItemClick(AdapterView<?> parent, View v, int position, long id)
	    {
	    	Account acct = adapterAccounts.getItem(position); //accounts.get(position);
	    	
	    	// See if we have an invesetment account or a non-investment account.
	    	if(acct.getAccountType() != Account.ACCOUNT_INVESTMENT )
	    	{
	    		Intent i = new Intent(getBaseContext(), LedgerActivity.class);
	    		i.putExtra("AccountId", acct.getId());
	    		i.putExtra("AccountName", acct.getName());
	    		i.putExtra("Balance", acct.getBalance());
	    		startActivity(i);
	    	}
	    	else
	    	{
	    		Intent i = new Intent(getBaseContext(), InvestmentActivity.class);
	    		i.putExtra("AccountId", acct.getId());
	    		i.putExtra("AccountName", acct.getName());
	    		startActivity(i);
	    	}
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
			
			// Load the items into the view.
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
		
		return true;
	}
	
	// Called when an options item is clicked
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Pass the event to ActionBarDrawerToggle, if it returns true, then it has handled the app icon touch event.
		if(mDrawerToggle.onOptionsItemSelected(item))
			return true;
		
		switch (item.getItemId())
		{
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
/*    public Loader<List<Account>> onCreateLoader(int id, Bundle args) 
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
*/
    private class HomeLoaderCallbacks implements LoaderManager.LoaderCallbacks<List<Account>>
    {  
    	Context context;
    	
    	public HomeLoaderCallbacks(HomeActivity homeActivity) 
    	{
    		context = homeActivity;
		}

		// LoaderManager.LoaderCallbacks<List<Accounts> methods:
        public Loader<List<Account>> onCreateLoader(int id, Bundle args) 
        {
        	setProgressBarIndeterminateVisibility(true);
        	return new HomeLoader(context, args);
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
    
    private class DrawerItemClickListener implements ListView.OnItemClickListener
    {
    	public void onItemClick(AdapterView parent, View view, int position, long id)
    	{
    		selectItem(position);
    	}
    }
    
    private void selectItem(int position)
    {
    	// Make sure to close the Navigation Drawer.
		mDrawerLayout.closeDrawer(mDrawerList);
		
    	// need to start new activities in here.
    	switch(position)
    	{
    	case 0:
    		startActivity(new Intent(this, AccountsActivity.class));   		
    		break;
    	case 1:
			startActivity(new Intent(this, CategoriesActivity.class));
    		break;    		
    	case 2:
			startActivity(new Intent(this, InstitutionsActivity.class));
    		break;
    	case 3:
			startActivity(new Intent(this, PayeeActivity.class));
    		break;
    	case 4:
			startActivity(new Intent(this, SchedulesActivity.class));
    		break;
    	case 5:
			startActivity(new Intent(this, ReportsActivity.class));
    		break;
    	default:
    		Toast.makeText(this, "We have a major fucking error!!!!", Toast.LENGTH_LONG).show();
    		break;		
    	}
    }
}