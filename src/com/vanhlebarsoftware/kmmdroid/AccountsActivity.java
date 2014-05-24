package com.vanhlebarsoftware.kmmdroid;

import java.util.List;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.DrawerLayout;

public class AccountsActivity extends FragmentActivity implements
								LoaderManager.LoaderCallbacks<List<Account>>
{
	private static final String TAG = AccountsActivity.class.getSimpleName();
	private static final int ACCOUNTS_LOADER = 0x02;
	private String[] mDrawerListItems = {null, null, null, null, null, null};
	private CharSequence mDrawerTitle;
	private CharSequence mTitle;
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;
	private ActionBar mActionBar;
	private static final int ACTION_NEW = 1;
	private static final int ACTION_EDIT = 2;
	// Define our Account Type Constants
	private static final int AT_CHECKING = 1;
	private static final int AT_SAVINGS = 2;
	private static final int AT_CREDITCARD = 4;
	private static final int AT_INVESTMENT = 7;
	private static final int AT_LIABILITY = 10;
	private boolean hideClosed = false;
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
	AccountsAdapter adapterNoClosed;
	
	/* Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.accounts);
        
        // Get our application
        KMMDapp = ((KMMDroidApp) getApplication());
        
        // Set the attributes of the ActionBar
        mActionBar = getActionBar();
        mActionBar.setBackgroundDrawable((new ColorDrawable(Color.parseColor("#62B1F6"))));
        
        // Set the titles of the Drawer and the ActionBar
        mTitle = mDrawerTitle = getTitle();
        
        // Populate our Drawer items.
        mDrawerListItems[0] = getString(R.string.titleHome);
        mDrawerListItems[1] = getString(R.string.titleCategories);
        mDrawerListItems[2] = getString(R.string.titleInstitutions);
        mDrawerListItems[3] = getString(R.string.titlePayees);
        mDrawerListItems[4] = getString(R.string.titleSchedules);
        mDrawerListItems[5] = getString(R.string.titleReports);
        
        // Find our views
        listAccounts = (ListView) findViewById(R.id.listAccountsView);
        btnHome = (ImageButton) findViewById(R.id.buttonHome);
        btnAccounts = (ImageButton) findViewById(R.id.buttonAccounts);
        btnCategories = (ImageButton) findViewById(R.id.buttonCategories);
        btnInstitutions = (ImageButton) findViewById(R.id.buttonInstitutions);
        btnPayees = (ImageButton) findViewById(R.id.buttonPayees);
        btnSchedules = (ImageButton) findViewById(R.id.buttonSchedules);
        btnReports = (ImageButton) findViewById(R.id.buttonReports);
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
        
        // Set out onClickListener events.
    	// Now hook into listAccounts ListView and set its onItemClickListener member
    	// to our class handler object.
        listAccounts.setOnItemClickListener(mMessageClickedHandler);

        // See if the database is already open, if not open it Read/Write.
        if(!KMMDapp.isDbOpen())
        {
        	KMMDapp.openDB();
        }
        
        // Create an empty adapter we will use to display the loaded data.
        adapterAccounts = new AccountsAdapter(this);
        adapterNoClosed = new AccountsAdapter(this);
		listAccounts.setAdapter(adapterAccounts);

		// Create the adapter for the navigation DrawerList
		mDrawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list_item, mDrawerListItems));

        // Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
		Bundle bundle = new Bundle();
		bundle.putInt("activity", 1);
        getSupportLoaderManager().initLoader(ACCOUNTS_LOADER, bundle, this);
        
        // Get the user's preference on hiding closed accounts.
        hideClosed = KMMDapp.prefs.getBoolean("hideClosed", false);
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
	    	Account account = null;
	    	if(hideClosed)
	    		account = adapterNoClosed.getItem(position);
	    	else
	    		account = adapterAccounts.getItem(position);
			Intent i = new Intent(getBaseContext(), CreateModifyAccountActivity.class);
			i.putExtra("Action", ACTION_EDIT);
			i.putExtra("AccountId", account.getId());
			startActivity(i);
	    }
	};
	
	private class AccountsAdapter extends ArrayAdapter<Account>
	{
		private final LayoutInflater mInflater;
		
		public AccountsAdapter(Context context)
		{
			super(context, R.layout.accounts_row);
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
			View view = mInflater.inflate(R.layout.accounts_row, parent, false);
			
			Account item = getItem(position);
			
			// Load the items into the view now for this schedule.
			if(item != null)
			{
				LinearLayout row = (LinearLayout) view.getRootView().findViewById(R.id.accountRow);
				if( position % 2 == 0)
					row.setBackgroundColor(Color.rgb(0x62, 0xB1, 0xF6));
				else
					row.setBackgroundColor(Color.rgb(0x62, 0xa1, 0xc6));
				
				
				TextView name = (TextView) view.findViewById(R.id.arAccountName);
				TextView balance = (TextView) view.findViewById(R.id.arAccountBalance);
				TextView type = (TextView) view.findViewById(R.id.arAccountType);
				ImageView icon = (ImageView) view.findViewById(R.id.arIcon);

				// if the account is closed, show it with strike thru text.
				if(item.getIsClosed())
				{
					name.setPaintFlags(name.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
					balance.setPaintFlags(balance.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
					type.setPaintFlags(type.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
				}
				else
				{
					name.setPaintFlags(name.getPaintFlags() &(~ Paint.STRIKE_THRU_TEXT_FLAG));
					balance.setPaintFlags(balance.getPaintFlags() &(~ Paint.STRIKE_THRU_TEXT_FLAG));
					type.setPaintFlags(type.getPaintFlags() &(~ Paint.STRIKE_THRU_TEXT_FLAG));
				}	
			
				name.setText(item.getName());
				balance.setText(item.getBalance());
				type.setText(item.getAccountTypeString());
				
				// Set the correct icon based on the account type.
				switch (item.getAccountType())
				{
					/*case AT_ASSET:
						icon.setImageResource(R.drawable.account_types_asset);
						break;	
					case AT_CASH:
						icon.setImageResource(R.drawable.account_types_cash);
						break;*/
					case AT_CHECKING:
						icon.setImageResource(R.drawable.account_types_checking);
						break;
					case AT_CREDITCARD:
						icon.setImageResource(R.drawable.account_types_credit_card);
						break;
					case AT_INVESTMENT:
						icon.setImageResource(R.drawable.account_types_investments);
						break;
					case AT_LIABILITY:
						icon.setImageResource(R.drawable.account_types_liability);
						break;
					/*case AT_LOAN:
						icon.setImageResource(R.drawable.account_types_loan);
						break;*/
					case AT_SAVINGS:
						icon.setImageResource(R.drawable.account_types_savings);
						break;
					/*case AT_EQUITY:
						icon.setImageResource(R.drawable.account_types_equity);
						break;*/
				}
			}
			else
				Log.d(TAG, "Never got an Account!");

			return view;
		}
		
		public AccountsAdapter getOpenAccounts()
		{
			AccountsAdapter tmp = new AccountsAdapter(getBaseContext());
			for(int i=0; i<this.getCount(); i++)
			{
				if( !this.getItem(i).getIsClosed() )
					tmp.add(this.getItem(i));
			}
			
			return tmp;
		}
	}
	// Called first time the user clicks on the menu button
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.accounts_menu, menu);

		return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu (Menu menu)
	{	
		// See if the user wants to hide closed accounts.
		if( hideClosed )
		{
			menu.findItem(R.id.itemHideShowClosed).setTitle(R.string.titleShowClosed);	
			menu.findItem(R.id.itemHideShowClosed).setChecked(true);
		}
		else
		{
			menu.findItem(R.id.itemHideShowClosed).setTitle(R.string.titleHideClosed);
			menu.findItem(R.id.itemHideShowClosed).setChecked(false);
		}
		
		return true;
	}

	// Called when an options item is clicked
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Pass the event to ActionBarDrawerToggle, if it returns true, then it has handled the app icon touch event.
		//if(mDrawerToggle.onOptionsItemSelected(item))
		//	return true;
		
		switch (item.getItemId())
		{
			case android.R.id.home:
				Intent intent = new Intent(this, HomeActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				break;
			case R.id.itemPrefs:
				startActivity(new Intent(this, PrefsActivity.class));
				break;
			case R.id.itemNew:
				Intent i = new Intent(getBaseContext(), CreateModifyAccountActivity.class);
				i.putExtra("Action", ACTION_NEW);
				startActivity(i);
				break;
			case R.id.itemAbout:
				startActivity(new Intent(this, AboutActivity.class));
				break;
			case R.id.itemHideShowClosed:
				Editor edit = KMMDapp.prefs.edit();
				if( hideClosed )
				{
					item.setTitle(R.string.titleHideClosed);
					item.setChecked(false);
					listAccounts.setAdapter(adapterAccounts);
					hideClosed = false;
				}
				else
				{
					item.setTitle(R.string.titleShowClosed);
					item.setChecked(true);
					listAccounts.setAdapter(adapterNoClosed);
					hideClosed = true;
				}
				edit.putBoolean("hideClosed", this.hideClosed);
				edit.apply();
				listAccounts.setVisibility(View.INVISIBLE);
				listAccounts.setVisibility(View.VISIBLE);
				break;
		}
		
		return true;
	}

	public Loader<List<Account>> onCreateLoader(int id, Bundle args) 
	{
		setProgressBarIndeterminateVisibility(true);
    	return new AccountsLoader(this, args);
	}

	public void onLoadFinished(Loader<List<Account>> loader, List<Account> accounts) 
	{
        // Set the new data in the adapter.
    	adapterAccounts.setData(accounts);
    	
		// let's create a 2nd adapter without the closed accounts.
		adapterNoClosed = adapterAccounts.getOpenAccounts();
		
		if( hideClosed )
		{
			// Set the adapter for no closed accounts.
			listAccounts.setAdapter(adapterNoClosed);
		}	
		
    	setProgressBarIndeterminateVisibility(false);
	}

	public void onLoaderReset(Loader<List<Account>> accounts) 
	{
        // clear the data in the adapter.
    	adapterAccounts.setData(null);	
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
    		startActivity(new Intent(this, HomeActivity.class));   		
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
