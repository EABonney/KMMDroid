package com.vanhlebarsoftware.kmmdroid;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
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
import android.widget.AdapterView.OnItemClickListener;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

public class AccountsActivity extends FragmentActivity implements
								LoaderManager.LoaderCallbacks<List<Account>>
{
	private static final String TAG = AccountsActivity.class.getSimpleName();
	private static final int ACCOUNTS_LOADER = 0x02;
	private static final int ACTION_NEW = 1;
	private static final int ACTION_EDIT = 2;
	// Define our Account Type Constants
	private static final int AT_CHECKING = 1;
	private static final int AT_SAVINGS = 2;
	private static final int AT_CREDITCARD = 4;
	private static final int AT_INVESTMENT = 7;
	private static final int AT_LIABILITY = 10;
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
        setContentView(R.layout.accounts);
        
        // Get our application
        KMMDapp = ((KMMDroidApp) getApplication());
        
        // Find our views
        listAccounts = (ListView) findViewById(R.id.listAccountsView);
        btnHome = (ImageButton) findViewById(R.id.buttonHome);
        btnAccounts = (ImageButton) findViewById(R.id.buttonAccounts);
        btnCategories = (ImageButton) findViewById(R.id.buttonCategories);
        btnInstitutions = (ImageButton) findViewById(R.id.buttonInstitutions);
        btnPayees = (ImageButton) findViewById(R.id.buttonPayees);
        btnSchedules = (ImageButton) findViewById(R.id.buttonSchedules);
        btnReports = (ImageButton) findViewById(R.id.buttonReports);
        navBar = (LinearLayout) findViewById(R.id.navBar);
        
        // Set out onClickListener events.
        btnHome.setOnClickListener(new View.OnClickListener()
        {
			public void onClick(View arg0)
			{
				startActivity(new Intent(getBaseContext(), HomeActivity.class));
			}
		});
        
        btnAccounts.setVisibility(View.GONE);
        
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
        
        // Create an empty adapter we will use to display the loaded data.
        adapterAccounts = new AccountsAdapter(this);
		listAccounts.setAdapter(adapterAccounts);
		
        // Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
		Bundle bundle = new Bundle();
		bundle.putInt("activity", 1);
        getSupportLoaderManager().initLoader(ACCOUNTS_LOADER, bundle, this);
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
	    	Account account = adapterAccounts.getItem(position);
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
			View view = convertView;
			if(view == null)
			{
				view = mInflater.inflate(R.layout.accounts_row, parent, false);
			}
			else
				view = convertView;
			
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
		// See if the user wants us to navigation menu items.
		if( !KMMDapp.prefs.getBoolean("navMenu", true))
		{
			menu.findItem(R.id.itemHome).setVisible(false);
			menu.findItem(R.id.itemCategories).setVisible(false);
			menu.findItem(R.id.itemPayees).setVisible(false);
			menu.findItem(R.id.itemInstitutions).setVisible(false);
			menu.findItem(R.id.itemSchedules).setVisible(false);
			menu.findItem(R.id.itemReports).setVisible(false);
		}
		else
		{
			menu.findItem(R.id.itemHome).setVisible(true);
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
			case R.id.itemHome:
				startActivity(new Intent(this, HomeActivity.class));
				break;
			case R.id.itemInstitutions:
				startActivity(new Intent(this, InstitutionsActivity.class));
				break;
			case R.id.itemPayees:
				startActivity(new Intent(this, PayeeActivity.class));
				break;
			case R.id.itemCategories:
				startActivity(new Intent(this, CategoriesActivity.class));
				break;
			case R.id.itemPrefs:
				startActivity(new Intent(this, PrefsActivity.class));
				break;
			case R.id.itemNew:
				Intent i = new Intent(getBaseContext(), CreateModifyAccountActivity.class);
				i.putExtra("Action", ACTION_NEW);
				startActivity(i);
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
		setProgressBarIndeterminateVisibility(false);
	}

	public void onLoaderReset(Loader<List<Account>> accounts) 
	{
        // clear the data in the adapter.
    	adapterAccounts.setData(null);		
	}
}
