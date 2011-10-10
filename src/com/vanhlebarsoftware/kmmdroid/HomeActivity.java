package com.vanhlebarsoftware.kmmdroid;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class HomeActivity extends Activity
{

	@SuppressWarnings("unused")
	private static final String TAG = "HomeActivity";
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
	KMMDroidApp KMMDapp;
	Cursor cursor;
	TextView tvAccountName;
	TextView tvAccountBalance;
	ListView listAccounts;
	SimpleCursorAdapter adapter;
	
	/* Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
        setContentView(R.layout.home);
		Log.d(TAG, "onCreate");
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
		adapter = new SimpleCursorAdapter(this, R.layout.home_row, cursor, FROM, TO);
		adapter.setViewBinder(VIEW_BINDER);
		listAccounts.setAdapter(adapter);
	}
	
	// Message Handler for our listAccounts List View clicks
	private OnItemClickListener mMessageClickedHandler = new OnItemClickListener() {
	    public void onItemClick(AdapterView<?> parent, View v, int position, long id)
	    {
	    	cursor.moveToPosition(position);
	    	Intent i = new Intent(getBaseContext(), LedgerActivity.class);
	    	i.putExtra("AccountId", cursor.getString(C_ID));
	    	i.putExtra("AccountName", cursor.getString(C_ACCOUNTNAME));
	    	i.putExtra("Balance", cursor.getString(C_BALANCE));
	    	startActivity(i);
	    }
	};
	
	// View binder to do formatting of the string values to numbers with commas and parenthesis
	static final ViewBinder VIEW_BINDER = new ViewBinder() {
		public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
			if(view.getId() != R.id.hrAccountBalance)
				return false;
			
			// Format the Amount properly.
			((TextView) view).setText(String.format("%,(.2f", Float.valueOf(cursor.getString(columnIndex))));
			
			return true;
		}
	};
	
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
		    	//Intent i = new Intent(getBaseContext(), WelcomeActivity.class);
		    	//i.putExtra("ClosedDB", true);
				//startActivity(i);
				KMMDapp.db.close();
				finish();
				break;
		}
		
		return true;
	}
}
