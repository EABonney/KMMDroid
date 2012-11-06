package com.vanhlebarsoftware.kmmdroid;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.TabActivity;
import android.content.ContentValues;
import android.content.DialogInterface;
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
	private static final int PAYEE_ID = 0;
	private static final int PAYEE_NAME = 1;
	private static final int PAYEE_REFERENCE = 2;
	private static final int PAYEE_EMAIL = 3;
	private static final int PAYEE_STREET = 4;
	private static final int PAYEE_CITY = 5;
	private static final int PAYEE_ZIPCODE = 6;
	private static final int PAYEE_STATE = 7;
	private static final int PAYEE_PHONE = 8;
	private static final int PAYEE_NOTES = 9;
	private static final int PAYEE_DEFAULTACCOUNTID = 10;
	private static final int PAYEE_MATCHDATA = 11;
	private static final int PAYEE_MATCHIGNORECASE = 12;
	private static final int PAYEE_MATCHKEYS = 13;
	private static final int AC_EXPENSE = 13;
	private static final int AC_INCOME = 12;
	private static final String dbTable = "kmmPayees";
	private int Action = 0;
	private String payeeId = null;
	private boolean returnFromDelete = false;
	private boolean isDirty = false;
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
        
        // If we are editing then we need to retrieve the payeeId
        if (Action == ACTION_EDIT)
        	payeeId = extras.getString("PayeeId");
        
        //Resources res = getResources(); // Resource object to get Drawables
        tabHost = getTabHost();  // The activity TabHost
        TabHost.TabSpec spec;  // Resusable TabSpec for each tab
        Intent intent;  // Reusable Intent for each tab

        // Create an Intent to launch an Activity for the tab (to be reused)
        intent = new Intent().setClass(this, PayeeAddressActivity.class);

        // Initialize a TabSpec for each tab and add it to the TabHost
        spec = tabHost.newTabSpec("payeeaddress").setIndicator(getString(R.string.PayeeTabAddress))
                      .setContent(intent);
        tabHost.addTab(spec);

        // Do the same for the other tabs
        intent = new Intent().setClass(this, PayeeDefaultAccountActivity.class);
        spec = tabHost.newTabSpec("payeedefault").setIndicator(getString(R.string.PayeeTabDefault))
                      .setContent(intent);
        tabHost.addTab(spec);

        intent = new Intent().setClass(this, PayeeMatchingActivity.class);
        spec = tabHost.newTabSpec("payeematching").setIndicator(getString(R.string.PayeeTabMatching))
                      .setContent(intent);
        tabHost.addTab(spec);

        if( Action == ACTION_EDIT )
        {
        	intent = new Intent().setClass(this, TransactionsTabActivity.class);
        	intent.putExtra("PayeeId", payeeId);
        	intent.putExtra("PayeeName", extras.getString("PayeeName"));
        	spec = tabHost.newTabSpec("payeetransactions").setIndicator(getString(R.string.TabTransactions))
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
		
		if(!returnFromDelete)
		{
			// See if we are editing and if so pull the data into the forms.
			if ( Action == ACTION_EDIT )
			{
				final String[] dbColumns = { "*" };
				final String strSelection = "id=?";
				cursor = KMMDapp.db.query(dbTable, dbColumns, strSelection, new String[] { payeeId }, null, null, null);
				startManagingCursor(cursor);
			
				// If we returned anything other than just one record we have issues.
				if ( cursor.getCount() == 0 )
				{
					AlertDialog.Builder alert = new AlertDialog.Builder(this);

					alert.setTitle(getString(R.string.error));
					alert.setMessage(getString(R.string.payeeNotFound));

					alert.setPositiveButton(getString(R.string.titleButtonOK), new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							finish();
						}
					});
					alert.show();
				}
			
				if ( cursor.getCount() > 1)
				{
					AlertDialog.Builder alert = new AlertDialog.Builder(this);
					
					alert.setTitle(getString(R.string.error));
					alert.setMessage(getString(R.string.payeeNotFound));

					alert.setPositiveButton(getString(R.string.titleButtonOK), new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							finish();
						}
					});
					alert.show();
				}
			
				cursor.moveToFirst();
				// We have the correct payeeId to edit. Populate the fields now.
				// Populate the Address elements
				getTabHost().setCurrentTab(0);
				Activity payeeAddress = this.getCurrentActivity();
				((PayeeAddressActivity) payeeAddress).putPayeeName(cursor.getString(PAYEE_NAME));
				((PayeeAddressActivity) payeeAddress).putPayeeAddress(cursor.getString(PAYEE_STREET));
				((PayeeAddressActivity) payeeAddress).putPayeePostalCode(cursor.getString(PAYEE_ZIPCODE));
				((PayeeAddressActivity) payeeAddress).putPayeePhone(cursor.getString(PAYEE_PHONE));
				((PayeeAddressActivity) payeeAddress).putPayeeEmail(cursor.getString(PAYEE_EMAIL));
				((PayeeAddressActivity) payeeAddress).putPayeeNotes(cursor.getString(PAYEE_NOTES));

				// Populate the default account elements.
				getTabHost().setCurrentTab(1);
				Activity payeeDefaults = this.getCurrentActivity();
				int pos = 0;
				switch( getAccountType( cursor.getString(PAYEE_DEFAULTACCOUNTID)) )
				{
					case AC_INCOME:
						Log.d(TAG, "AC_INCOME");
						((PayeeDefaultAccountActivity) payeeDefaults).putUseDefaults(true);
						pos = getListViewPos(cursor.getString(PAYEE_DEFAULTACCOUNTID), AC_INCOME);
						((PayeeDefaultAccountActivity) payeeDefaults).putUseIncome(true);
						((PayeeDefaultAccountActivity) payeeDefaults).putIncomeAccount(pos);
						((PayeeDefaultAccountActivity) payeeDefaults).putUseExpense(false);
						break;
					case AC_EXPENSE:
						Log.d(TAG, "AC_EXPENSE");
						((PayeeDefaultAccountActivity) payeeDefaults).putUseDefaults(true);
						pos = getListViewPos(cursor.getString(PAYEE_DEFAULTACCOUNTID), AC_EXPENSE);
						((PayeeDefaultAccountActivity) payeeDefaults).putUseExpense(true);
						((PayeeDefaultAccountActivity) payeeDefaults).putExpenseAccount(pos);
						((PayeeDefaultAccountActivity) payeeDefaults).putUseIncome(false);
						break;
					default:
						Log.d(TAG, "Default");
						((PayeeDefaultAccountActivity) payeeDefaults).putUseDefaults(false);
						((PayeeDefaultAccountActivity) payeeDefaults).putUseExpense(false);
						((PayeeDefaultAccountActivity) payeeDefaults).putUseIncome(false);
						break;
				}
			
				// Popluate the default matching tab.
				getTabHost().setCurrentTab(2);
				Activity payeeMatching = this.getCurrentActivity();
				((PayeeMatchingActivity) payeeMatching).putMatchingType(cursor.getInt(PAYEE_MATCHDATA));
				
				// Make sure the 1st tab is displayed to the user.
				getTabHost().setCurrentTab(0);
				isDirty = false;
			}
		}
		else
		{
			returnFromDelete = false;
			finish();
		}
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
				String name, address, postalcode, phone, email, notes, accountInc, accountExp;
				boolean usedefault, useInc, useExp;
				int matchingType;
				
				// Get the Address elements
				getTabHost().setCurrentTab(0);
				Activity payeeAddress = this.getCurrentActivity();
				//name = payeeName.getText().toString();
				name = ((PayeeAddressActivity) payeeAddress).getPayeeName();
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
				
				String id = null;
				if (Action == ACTION_NEW)
					id = createPayeeId();
				else
					id = payeeId;
				
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
					
					Log.d(TAG, "accontInc: " + accountInc + 
							" accountExp: " + accountExp);
				}
				valuesPayee.put("matchIgnoreCase", "Y");
				valuesPayee.put("matchKeys", "");
				
				switch (Action)
				{
					case ACTION_NEW:
						// Attempt to insert the newly created Payee.
						try 
						{
							KMMDapp.db.insertOrThrow(dbTable, null, valuesPayee);
							KMMDapp.updateFileInfo("hiPayeeId", 1);
							KMMDapp.updateFileInfo("payees", 1);
						} 
						catch (SQLException e) 
						{
							Log.d(TAG, "Insert error: " + e.getMessage());
						}
						increasePayeeId();
						break;
					case ACTION_EDIT:
						KMMDapp.db.update(dbTable, valuesPayee, "id=?", new String[] { payeeId });
						break;
				}
				KMMDapp.updateFileInfo("lastModified", 0);
				finish();
				break;
			case R.id.itemDelete:
				AlertDialog.Builder alertDel = new AlertDialog.Builder(this);
				alertDel.setTitle(R.string.delete);
				alertDel.setMessage(getString(R.string.deletemsg) + " " + payeeName.getText().
						toString() + "?");

				alertDel.setPositiveButton(getString(R.string.titleButtonOK), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						Intent i = new Intent(getBaseContext(), PayeeReassignActivity.class);
						i.putExtra("PayeeToDelete", payeeId);
						returnFromDelete = true;
						startActivity(i);							
					}
				});
				alertDel.setNegativeButton(getString(R.string.titleButtonCancel), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// Canceled.
						Log.d(TAG, "User cancelled delete.");
					}
					});				
				alertDel.show();
				break;
			case R.id.itemCancel:
				finish();
				break;
		}
		return true;
	}
	
// ************************************************************************************************
// ******************************* Helper Functions ***********************************************
	
	private String createPayeeId()
	{
		final String[] dbColumns = { "hiPayeeId"};
		final String strOrderBy = "hiPayeeId DESC";
		// Run a query to get the Payee ids so we can create a new one.
		cursor = KMMDapp.db.query("kmmFileInfo", dbColumns, null, null, null, null, strOrderBy);
		startManagingCursor(cursor);
		
		cursor.moveToFirst();

		// Since id is in P000000 format, we need to pick off the actual number then increase by 1.
		int lastId = cursor.getInt(0);
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
	
	private void increasePayeeId()
	{
		final String[] dbColumns = { "hiPayeeId" };
		final String strOrderBy = "hiPayeeId DESC";
		
		cursor = KMMDapp.db.query("kmmFileInfo", dbColumns, null, null, null, null, strOrderBy);
		startManagingCursor(cursor);
		cursor.moveToFirst();
		
		int id = cursor.getInt(0);
		id = id + 1;
		
		ContentValues values = new ContentValues();
		values.put("hiPayeeId", id);
		
		KMMDapp.db.update("kmmFileInfo", values, null, null);
	}
	
	private int getAccountType(String account)
	{
		Log.d(TAG, "getAccountType, account: " + account);
		if( account == null )
			return 0;
		else
		{
			Cursor c = KMMDapp.db.query("kmmAccounts", new String[] { "accountType" }, "id=?", 
				new String[] { account }, null, null, null);
			startManagingCursor(c);
			c.moveToFirst();

			Log.d(TAG, "getAcountType Return: " + c.getString(0));
			return Integer.parseInt(c.getString(0));

		}
	}
	
	private int getListViewPos(String name, int type)
	{
		Log.d(TAG, "getListViewPos: " + name);
		int i = 0;
		String strType = null;
		switch (type)
		{
			case AC_INCOME:
				strType = "Income";
				break;
			case AC_EXPENSE:
				strType = "Expense";
				break;
		}
		Cursor c = KMMDapp.db.query("kmmAccounts", new String[] { "accountName", "id" }, 
									"accountTypeString=? AND (balance != '0/1')", new String[] { strType },
									null, null, "accountName ASC");
		startManagingCursor(c);
		c.moveToFirst();
		
		if(c.getCount() > 0)
		{
			Log.d(TAG, "name: " + name + " cursorName: " + c.getString(1));
			while(!name.equals(c.getString(1)))
			{
				Log.d(TAG, "name: " + name + " cursorName: " + c.getString(1));
				c.moveToNext();
				i++;
			}
		}
		else
		{
			Log.d(TAG, "getListViewPos query returned no accounts!");
			i = 0;
		}
		
		Log.d(TAG, "getListViewPos returning: " + String.valueOf(i));
		return i;
	}
	
	public void setIsDirty(boolean flag)
	{
		this.isDirty = flag;
	}
	
	public boolean getIsDirty()
	{
		return this.isDirty;
	}
}
