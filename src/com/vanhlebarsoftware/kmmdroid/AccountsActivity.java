package com.vanhlebarsoftware.kmmdroid;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.util.Log;

public class AccountsActivity extends Activity
{
	private static final String TAG = "AccountsActivity";
	private static final int ACTION_NEW = 1;
	private static final int ACTION_EDIT = 2;
	// Define our Account Type Constants
	private static final int AT_CHECKING = 1;
	private static final int AT_SAVINGS = 2;
	private static final int AT_CREDITCARD = 4;
	private static final int AT_INVESTMENT = 7;
	private static final int AT_LIABILITY = 10;
	private static final int AT_INCOME = 12;
	private static final int AT_EXPENSE = 13;
	private static final int AT_EQUITY = 16;
	private static final String dbTable = "kmmAccounts";
	private static final String[] dbColumns = { "accountName", "balance", "accountTypeString",
												"accountType", "id AS _id"};
	private static final String strSelection = "(accountType != ? AND accountType != ?)";
	private static final String strOrderBy = "accountName ASC";
	static final String[] FROM = { "accountName", "accountTypeString", "balance", "accountType" };
	static final int[] TO = { R.id.arAccountName, R.id.arAccountType, R.id.arAccountBalance, R.id.arIcon };
	private String strAccountId = null;
	KMMDroidApp KMMDapp;
	Cursor cursor;
	ImageButton btnHome;
	ImageButton btnAccounts;
	ImageButton btnCategories;
	ImageButton btnInstitutions;
	ImageButton btnPayees;
	ImageButton btnSchedules;
	ImageButton btnReports;
	LinearLayout navBar;
	ListView listAccounts;
	SimpleCursorAdapter adapter;
	
	/* Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
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
		
		//Get all the accounts to be displayed.
		cursor = KMMDapp.db.query(dbTable, dbColumns, strSelection, new String[] { String.valueOf(Account.ACCOUNT_EXPENSE), String.valueOf(Account.ACCOUNT_INCOME) },
									null, null, strOrderBy);
		startManagingCursor(cursor);
		
		// Set up the adapter
		adapter = new SimpleCursorAdapter(this, R.layout.accounts_row, cursor, FROM, TO);
		adapter.setViewBinder(VIEW_BINDER);
		listAccounts.setAdapter(adapter);
		
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
	    	cursor.moveToPosition(position);
	    	strAccountId = cursor.getString(4);
			Intent i = new Intent(getBaseContext(), CreateModifyAccountActivity.class);
			i.putExtra("Action", ACTION_EDIT);
			i.putExtra("AccountId", strAccountId);
			startActivity(i);
	    }
	};
	
	// View binder to do formatting of the string values to numbers with commas and parenthesis
	static final ViewBinder VIEW_BINDER = new ViewBinder() 
	{
		public boolean setViewValue(View view, Cursor cursor, int columnIndex) 
		{
			LinearLayout row = (LinearLayout) view.getRootView().findViewById(R.id.accountRow);
			if( cursor.getPosition() % 2 == 0)
				row.setBackgroundColor(Color.rgb(0x62, 0xB1, 0xF6));
			else
				row.setBackgroundColor(Color.rgb(0x62, 0xa1, 0xc6));
			
			//if(view.getId() != R.id.arAccountBalance)
				//return false;
			switch (view.getId())
			{
				case R.id.arAccountBalance:
					// Format the Amount properly.
					((TextView) view).setText(Transaction.convertToDollars(Account.convertBalance(cursor.getString(columnIndex)), true));
					break;
				case R.id.arIcon:
					// Set the correct icon based on accountType
					switch (cursor.getInt(columnIndex))
					{
						/*case AT_ASSET:
							((ImageView) view).setImageResource(R.drawable.account_types_asset);
							break;	
						case AT_CASH:
							((ImageView) view).setImageResource(R.drawable.account_types_cash);
							break;*/
						case AT_CHECKING:
							((ImageView) view).setImageResource(R.drawable.account_types_checking);
							break;
						case AT_CREDITCARD:
							((ImageView) view).setImageResource(R.drawable.account_types_credit_card);
							break;
						case AT_INVESTMENT:
							((ImageView) view).setImageResource(R.drawable.account_types_investments);
							break;
						case AT_LIABILITY:
							((ImageView) view).setImageResource(R.drawable.account_types_liability);
							break;
						/*case AT_LOAN:
							((ImageView) view).setImageResource(R.drawable.account_types_loan);
							break;*/
						case AT_SAVINGS:
							((ImageView) view).setImageResource(R.drawable.account_types_savings);
							break;
						/*case AT_EQUITY:
							((ImageView) view).setImageResource(R.drawable.account_types_equity);
							break;*/
					}
					break;
				default:
					return false;
			}

			return true;
		}
	};
	
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
}
