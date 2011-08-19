package com.vanhlebarsoftware.kmmdroid;

import android.app.Activity;
import android.app.TabActivity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TabHost;
import android.widget.TextView;
import android.util.Log;

public class CreateModifyPayeeActivity extends TabActivity
{
	private static final String TAG = "CreateModifyPayeeActivity";
	private static final int ACTION_NEW = 1;
	private static final int ACTION_EDIT = 2;
	private static final int ACTION_DELETE = 3;
	private static final String dbTable = "kmmPayees";
	private static final String[] dbColumns = { "id AS _id"};
	private static final String strOrderBy = "id DESC";
	private int Action = 0;
	KMMDroidApp KMMDapp;
	Cursor cursor;
	TextView payeeName;
	SimpleCursorAdapter adapter;
	TabHost tabHost;
	
	/* Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
        setContentView(R.layout.createmod_payee);

        // Find our views
        payeeName = (TextView) findViewById(R.id.titleCreateModPayee);
        
        // Get the Activity and payee name.
        Bundle extras = getIntent().getExtras();
        Action = extras.getInt("Activity");
        payeeName.setText(extras.getString("PayeeName"));
        
        //Resources res = getResources(); // Resource object to get Drawables
        tabHost = getTabHost();  // The activity TabHost
        TabHost.TabSpec spec;  // Resusable TabSpec for each tab
        Intent intent;  // Reusable Intent for each tab

        // Create an Intent to launch an Activity for the tab (to be reused)
        intent = new Intent().setClass(this, PayeeAddressActivity.class);

        // Initialize a TabSpec for each tab and add it to the TabHost
        spec = tabHost.newTabSpec("payeeaddress").setIndicator("Address")
                      .setContent(intent);
        tabHost.addTab(spec);

        // Do the same for the other tabs
        intent = new Intent().setClass(this, PayeeDefaultAccountActivity.class);
        spec = tabHost.newTabSpec("payeedefault").setIndicator("Default")
                      .setContent(intent);
        tabHost.addTab(spec);

        intent = new Intent().setClass(this, PayeeMatchingActivity.class);
        spec = tabHost.newTabSpec("payeematching").setIndicator("Matching")
                      .setContent(intent);
        tabHost.addTab(spec);

        if( Action == ACTION_EDIT )
        {
        	intent = new Intent().setClass(this, PayeeTransactionsActivity.class);
        	spec = tabHost.newTabSpec("payeetransactions").setIndicator("Transactions")
                      .setContent(intent);
        	tabHost.addTab(spec);

        	tabHost.setCurrentTab(0);
        }
        
        // Get our application
        KMMDapp = ((KMMDroidApp) getApplication());

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
		
	}
	
	// Called first time the user clicks on the menu button
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.save_menu, menu);
		return true;
	}
	
	// Called when an options item is clicked
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.itemsave:
				String name, address, postalcode, phone, email, notes;
				boolean usedefault, useInc, useExp;
				long accountInc, accountExp;
				int matchingType;
				
				// Get the Address elements
				getTabHost().setCurrentTab(0);
				Activity payeeAddress = this.getCurrentActivity();
				name = payeeName.getText().toString();
				address = ((PayeeAddressActivity) payeeAddress).getPayeeAddress();
				postalcode = ((PayeeAddressActivity) payeeAddress).getPayeePostalCode();
				phone = ((PayeeAddressActivity) payeeAddress).getPayeePhone();
				email = ((PayeeAddressActivity) payeeAddress).getPayeeEmail();
				notes = ((PayeeAddressActivity) payeeAddress).getPayeeNotes();
				
				// Get the Default Account elements
				getTabHost().setCurrentTab(1);
				Activity payeeDefaults = this.getCurrentActivity();
				usedefault = ((PayeeDefaultAccountActivity) payeeDefaults).getUseDefaults();
				useInc = ((PayeeDefaultAccountActivity) payeeDefaults).getUseIncome();
				useExp = ((PayeeDefaultAccountActivity) payeeDefaults).getUseExpense();
				accountInc = ((PayeeDefaultAccountActivity) payeeDefaults).getIncomeAccount();
				accountExp = ((PayeeDefaultAccountActivity) payeeDefaults).getExpenseAccount();
				
				// Get the matching type elements
				getTabHost().setCurrentTab(2);
				Activity payeeMatching = this.getCurrentActivity();
				matchingType = ((PayeeMatchingActivity) payeeMatching).getMatchingType();
				
				String id = createPayeeId();
				
				// Create the ContentValue pairs for the insert query.
				ContentValues valuesPayee = new ContentValues();
				valuesPayee.put("id", id);
				valuesPayee.put("name", name);
				valuesPayee.put("reference", "");
				valuesPayee.put("email", email);
				valuesPayee.put("addressStreet", address);
				valuesPayee.put("addressCity", "");
				valuesPayee.put("addressState", "");
				valuesPayee.put("addressZipcode", postalcode);
				valuesPayee.put("telephone", phone);
				valuesPayee.put("notes", notes);
				switch ( matchingType )
				{
					case R.id.payeeMatchonName:
						valuesPayee.put("matchData", 1);
						break;
					case R.id.payeeNoMatching:
						valuesPayee.put("matchData", 0);
						break;
				}
				if ( usedefault )
				{
					if ( useInc )
						valuesPayee.put("defaultAccountId", accountInc);
					if ( useExp )
						valuesPayee.put("defaultAccountId", accountExp);
				}
				valuesPayee.put("matchIgnoreCase", "Y");
				valuesPayee.put("matchKeys", "");
				
				// Attempt to insert the newly created Payee.
				try {
					KMMDapp.db.insertOrThrow(dbTable, null, valuesPayee);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					Log.d(TAG, "Insert error: " + e.getMessage());
				}
				finish();
				break;
			case R.id.itemCancel:
				finish();
				break;
		}
		return true;
	}
	
	private String createPayeeId()
	{
		// Run a query to get the Payee ids so we can create a new one.
		cursor = KMMDapp.db.query(dbTable, dbColumns, null, null, null, null, strOrderBy);
		startManagingCursor(cursor);
		
		// We need to skip over the "USER" payee.
		cursor.moveToPosition(1);

		// Since id is in P000000 format, we need to pick off the actual number then increase by 1.
		int lastId = Integer.parseInt(cursor.getString(0).substring(1).toString());
		lastId = lastId +1;
		String nextId = Integer.toString(lastId);
		
		// Need to pad out the number so we get back to our P000000 format
		String newId = "P";
		for(int i= 0; i < (6 - nextId.length()); i++)
		{
			newId = newId + "0";
		}
		
		// Tack on the actual number created.
		newId = newId + nextId;
		
		return newId;
	}
}
