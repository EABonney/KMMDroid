package com.vanhlebarsoftware.kmmdroid;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class InvestmentActivity extends FragmentActivity
{
	private static final String TAG = InvestmentActivity.class.getSimpleName();
	private static final int INVESTMENTS_LOADER = 0x31;
	private String[] mDrawerListItems = {null, null, null, null, null, null};
	private CharSequence mDrawerTitle;
	private CharSequence mTitle;
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;
	ListView listInvestments;
	InvestmentsAdapter adapterInvestments;
	InvestmentsLoaderCallbacks investmentloaderCallbacks;

	/* (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle extras) 
	{
		// TODO Auto-generated method stub
		super.onCreate(extras);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.investment);
        
        Bundle ext = getIntent().getExtras();
		if(ext.isEmpty())
			Log.d(TAG, "We didn't pass any extras to the activity!");
		else
		{
			Log.d(TAG, "AccountId: " + ext.getString("AccountId"));
			Log.d(TAG, "Account Name: " + ext.getString("AccountName"));
		}
		
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
        listInvestments = (ListView) findViewById(R.id.listInvestmentView);
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
        listInvestments.setOnItemClickListener(mMessageClickedHandler);
        
        // Create an empty adapter we will use to display the loaded data.
        adapterInvestments = new InvestmentsAdapter(this);
		listInvestments.setAdapter(adapterInvestments);
		
		// Create the adapter for the navigation DrawerList
		mDrawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list_item, mDrawerListItems));
		
        // Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
		investmentloaderCallbacks = new InvestmentsLoaderCallbacks(this);
        getSupportLoaderManager().initLoader(INVESTMENTS_LOADER, ext, investmentloaderCallbacks);
	}


	/* (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onDestroy()
	 */
	@Override
	protected void onDestroy() 
	{
		// TODO Auto-generated method stub
		super.onDestroy();
	}


	/* (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onResume()
	 */
	@Override
	protected void onResume() 
	{
		// TODO Auto-generated method stub
		super.onResume();
	}
	
	// Message Handler for our listAccounts List View clicks
	private OnItemClickListener mMessageClickedHandler = new OnItemClickListener() {
	    public void onItemClick(AdapterView<?> parent, View v, int position, long id)
	    {
	    	Account acct = adapterInvestments.getItem(position); //accounts.get(position);
	    	Intent i = new Intent(getBaseContext(), LedgerActivity.class);
	    	i.putExtra("AccountId", acct.getId());
	    	i.putExtra("AccountName", acct.getName());
	    	i.putExtra("Balance", acct.getBalance());
	    	startActivity(i);
	    }
	};
	
	private class InvestmentsAdapter extends ArrayAdapter<Account>
	{
		private final LayoutInflater mInflater;
		
		public InvestmentsAdapter(Context context)
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
				view = mInflater.inflate(R.layout.investment_row, parent, false);
			}
			else
				view = convertView;
			
			Account item = getItem(position);
			
			// Load the items into the view.
			if(item != null)
			{
				TextView desc = (TextView) view.findViewById(R.id.irInvestmentName);
				TextView shares = (TextView) view.findViewById(R.id.irInvestmentShares);
				TextView cost = (TextView) view.findViewById(R.id.irInvestmentCost);
				TextView value = (TextView) view.findViewById(R.id.irInvestmentValue);
				
				desc.setText(item.getName());
				shares.setText(item.getBalance());
				cost.setText(item.getStockCost());
				value.setText(item.getStockValue());
			}
			else
				Log.d(TAG, "Never got an Account!");

			return view;
		}
	}
	
	private class InvestmentsLoaderCallbacks implements LoaderManager.LoaderCallbacks<List<Account>>
	{
		Context context;
		
		public InvestmentsLoaderCallbacks(InvestmentActivity investmentActivity)
		{
			this.context = investmentActivity;
		}
		
		public Loader<List<Account>> onCreateLoader(int id, Bundle args) 
		{
        	setProgressBarIndeterminateVisibility(true);
        	return new InvestmentsLoader(context, args);
		}

		public void onLoadFinished(Loader<List<Account>> loader, List<Account> accounts) 
		{
            // Set the new data in the adapter.
        	adapterInvestments.setData(accounts);
        	setProgressBarIndeterminateVisibility(false);
		}

		public void onLoaderReset(Loader<List<Account>> loader) 
		{
            // clear the data in the adapter.
        	adapterInvestments.setData(null);			
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
