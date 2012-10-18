package com.vanhlebarsoftware.kmmdroid;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.SimpleCursorAdapter.ViewBinder;

public class CategoriesActivity extends Activity
{
	@SuppressWarnings("unused")
	private static final String TAG = "CategoriesActivity";
	private static final int ACTION_NEW = 1;
	private static final int ACTION_EDIT = 2;
	private static final int C_ACCOUNTNAME = 0;
	private static final int C_BALANCE = 1;
	private static final int C_ID = 2;
	private static final String dbTable = "kmmAccounts";
	private static final String[] dbColumns = { "accountName", "balance", "id AS _id" };
	private static final String strSelection = "(accountType=" + Account.ACCOUNT_EXPENSE + " OR accountType=" + Account.ACCOUNT_INCOME +
			") AND (balance != '0/1')";
	private static final String strOrderBy = "accountName ASC";
	static final String[] FROM = { "accountName", "balance" };
	static final int[] TO = { R.id.crAccountName, R.id.crAccountBalance };
	private static String strCategoryId = null;
	private static String strCategoryName = null;
	private String accountType = null;
	KMMDroidApp KMMDapp;
	Cursor cursor;
	ListView listCategories;
	SimpleCursorAdapter adapter;
	
	/* Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
        setContentView(R.layout.categories);
        
        // Get our application
        KMMDapp = ((KMMDroidApp) getApplication());
        
        // Find our views
        listCategories = (ListView) findViewById(R.id.listCategoriesView);

    	// Now hook into listAccounts ListView and set its onItemClickListener member
    	// to our class handler object.
        listCategories.setOnItemClickListener(mMessageClickedHandler);

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
		cursor = KMMDapp.db.query(dbTable, dbColumns, strSelection, null, null, null, strOrderBy);
		startManagingCursor(cursor);
		
		// Set up the adapter
		adapter = new SimpleCursorAdapter(this, R.layout.categories_row, cursor, FROM, TO);
		adapter.setViewBinder(VIEW_BINDER);
		listCategories.setAdapter(adapter);
	}
	
	// Message Handler for our listAccounts List View clicks
	private OnItemClickListener mMessageClickedHandler = new OnItemClickListener() {
	    public void onItemClick(AdapterView<?> parent, View v, int position, long id)
	    {
	    	cursor.moveToPosition(position);
	    	strCategoryId = cursor.getString(C_ID);
	    	strCategoryName = cursor.getString(C_ACCOUNTNAME);
			Intent i = new Intent(getBaseContext(), CreateModifyCategoriesActivity.class);
			i.putExtra("Action", ACTION_EDIT);
			i.putExtra("categoryId", strCategoryId);
			i.putExtra("categoryName", strCategoryName);
			startActivity(i);
	    }
	};
	
	// View binder to do formatting of the string values to numbers with commas and parenthesis
	// and to do the alternating of background colors for rows.
	static final ViewBinder VIEW_BINDER = new ViewBinder() 
	{
		public boolean setViewValue(View view, Cursor cursor, int columnIndex) 
		{
			LinearLayout row = (LinearLayout) view.getRootView().findViewById(R.id.crRow);
			if( cursor.getPosition() % 2 == 0)
				row.setBackgroundColor(Color.rgb(0x62, 0xB1, 0xF6));
			else
				row.setBackgroundColor(Color.rgb(0x62, 0xa1, 0xc6));
			
			if(view.getId() != R.id.crAccountBalance)
				return false;
			
			// Format the Amount properly.
			((TextView) view).setText(String.format(Transaction.convertToDollars(Account.convertBalance(cursor.getString(columnIndex)), true)));
			
			return true;
		}
	};
	
	// Called first time the user clicks on the menu button
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.categories_menu, menu);
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
			case R.id.itemInstitutions:
				startActivity(new Intent(this, InstitutionsActivity.class));
				break;
			case R.id.itemPayees:
				startActivity(new Intent(this, PayeeActivity.class));
				break;
			case R.id.itemHome:
				startActivity(new Intent(this, HomeActivity.class));
				break;
			case R.id.itemPrefs:
				startActivity(new Intent(this, PrefsActivity.class));
				break;
			case R.id.itemNew:
				Intent i = new Intent(this, CreateModifyCategoriesActivity.class);
				i.putExtra("Action", ACTION_NEW);
				startActivity(i);
				break;
			case R.id.itemAbout:
				startActivity(new Intent(this, AboutActivity.class));
				break;
		}
		
		return true;
	}
	
// *********************************************************************************************
// ************************************ Helper Functions ***************************************
	public void putAccountType(String name)
	{
		accountType = name;
	}
	
	public String getAccountType()
	{
		return accountType;
	}
}
