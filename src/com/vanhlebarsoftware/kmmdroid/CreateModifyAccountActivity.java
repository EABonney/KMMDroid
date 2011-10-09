package com.vanhlebarsoftware.kmmdroid;

import java.util.StringTokenizer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.TabActivity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.SimpleCursorAdapter;
import android.widget.TabHost;
import android.widget.TextView;

public class CreateModifyAccountActivity extends TabActivity
{
	private static final String TAG = "CreateAccounActivity";
	private static final int ACTION_NEW = 1;
	private static final int ACTION_EDIT = 2;
	private static final int A_CHECKING = 1;
	private static final int A_SAVINGS = 2;
	private static final int A_CREDITCARD = 4;
	private static final int A_LOAN = 5;
	private static final int A_INVESTMENT = 7;
	private static final int A_ASSET = 9;
	private static final int A_LIABILITY = 10;
	private static final int A_EQUITY = 16;
	private static final int C_ID = 0;
	private static final int C_INSTITUTIONID = 1;
	private static final int C_PARENTID = 2;
	private static final int C_OPENINGDATE = 5;
	private static final int C_ACCOUNTNUMBER = 6;
	private static final int C_ACCOUNTTYPE = 7;
	private static final int C_ACCOUNTTYPESTRING = 8;
	private static final int C_ACCOUNTNAME = 10;
	private static final int C_CURRENCYID = 12;
	private static final int C_BALANCE = 13;
	private static final int C_BALANCEFORMATTED = 14;	
	private static final int C_TRANSACTIONCOUNT = 15;
	
	private int Action = 0;
	private String accountId = null;
	private boolean returnFromDelete = false;
	KMMDroidApp KMMDapp;
	Cursor cursor;
	TextView payeeName;
	TextView title;
	SimpleCursorAdapter adapter;
	TabHost tabHost;
	
	/* Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
        setContentView(R.layout.create_accounts);
 
        // Get the Activity and account id.
        Bundle extras = getIntent().getExtras();
        Action = extras.getInt("Action");
        
        // If we are editing then we need to retrieve the payeeId
        if (Action == ACTION_EDIT)
        {
        	accountId = extras.getString("AccountId");
        	//find our view and update it for the correct title.
        	title = (TextView) findViewById(R.id.titleCreateModAccount);
        	title.setText(R.string.titleEditModAccount);
        }
        
        //Resources res = getResources(); // Resource object to get Drawables
        tabHost = getTabHost();  // The activity TabHost
        TabHost.TabSpec spec;  // Resusable TabSpec for each tab
        Intent intent;  // Reusable Intent for each tab

        // Create an Intent to launch an Activity for the tab (to be reused)
        intent = new Intent().setClass(this, CreateAccountInstitutionActivity.class);

        // Initialize a TabSpec for each tab and add it to the TabHost
        spec = tabHost.newTabSpec("institution").setIndicator("Institution")
                      .setContent(intent);
        tabHost.addTab(spec);

        // Do the same for the other tabs
        intent = new Intent().setClass(this, CreateAccountAccountActivity.class);
        spec = tabHost.newTabSpec("account").setIndicator("Account")
                      .setContent(intent);
        tabHost.addTab(spec);

        // Do the same for the other tabs
        intent = new Intent().setClass(this, CreateAccountParentActivity.class);
        spec = tabHost.newTabSpec("parent").setIndicator("Subaccount")
                      .setContent(intent);
        tabHost.addTab(spec);
        
        tabHost.setCurrentTab(0);
        
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
				cursor = KMMDapp.db.query("kmmAccounts", dbColumns, strSelection, new String[] { accountId  }, null, null, null);
				startManagingCursor(cursor);	
				
				cursor.moveToFirst();
				// Check to see if we are editing one of the currently supported account types. If not tell the user and then return to the
				// account lists.
				if( cursor.getInt(C_ACCOUNTTYPE) == A_CREDITCARD || cursor.getInt(C_ACCOUNTTYPE) == A_LOAN ||
						cursor.getInt(C_ACCOUNTTYPE) == A_INVESTMENT )
				{
					AlertDialog.Builder alert = new AlertDialog.Builder(this);

					alert.setTitle(getString(R.string.error));
					alert.setMessage(getString(R.string.accountNotSupported));

					alert.setPositiveButton(getString(R.string.titleButtonOK), new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							finish();
						}
					});
					alert.show();					
				}
				
				// If we returned anything other than just one record we have issues.
				if ( cursor.getCount() == 0 )
				{
					AlertDialog.Builder alert = new AlertDialog.Builder(this);

					alert.setTitle(getString(R.string.error));
					alert.setMessage(getString(R.string.categoryNotFound));

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
					alert.setMessage(getString(R.string.categoryNotFound));

					alert.setPositiveButton(getString(R.string.titleButtonOK), new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							finish();
						}
					});
					alert.show();
				}

				// Get the KeyValuePairs for this id.
				Cursor kmmKVP = KMMDapp.db.query("kmmKeyValuePairs", new String[] { "kvpData" },
						"kvpId=? AND kvpType='ACCOUNT' AND kvpKey='IBAN'", new String[] { cursor.getString(C_ID) }, null, null, null);
				startManagingCursor(kmmKVP);
				
				// We have the correct account Id, so now popluate the forms fields.
				getTabHost().setCurrentTab(0);
				Activity accountInst = this.getCurrentActivity();
				((CreateAccountInstitutionActivity) accountInst).putAccountNumber(cursor.getString(C_ACCOUNTNUMBER));
				// if we have an institudtionID then we need to populate the fields.
				if(cursor.getString(C_INSTITUTIONID) != null)
				{
					((CreateAccountInstitutionActivity) accountInst).putUseInstitution(false);
					((CreateAccountInstitutionActivity) accountInst).putInstitutionId(cursor.getString(C_INSTITUTIONID));
					Log.d(TAG, "institutionId: " + cursor.getString(C_INSTITUTIONID));
				}
				else
				{
					((CreateAccountInstitutionActivity) accountInst).putUseInstitution(true);
				}
				if( kmmKVP.getCount() > 0)
				{
					kmmKVP.moveToFirst();
					((CreateAccountInstitutionActivity) accountInst).putIBAN(kmmKVP.getString(0));
				}
				kmmKVP.close();
				getTabHost().setCurrentTab(1);
				Activity accountAccount = this.getCurrentActivity();
				((CreateAccountAccountActivity) accountAccount).putAccountName(cursor.getString(C_ACCOUNTNAME));
				((CreateAccountAccountActivity) accountAccount).putAccountType(cursor.getInt(C_ACCOUNTTYPE));
				((CreateAccountAccountActivity) accountAccount).putAccountTypeString(cursor.getString(C_ACCOUNTTYPESTRING));
				((CreateAccountAccountActivity) accountAccount).putCurrency(cursor.getString(C_CURRENCYID));
				((CreateAccountAccountActivity) accountAccount).putOpeningDate(cursor.getString(C_OPENINGDATE));
				((CreateAccountAccountActivity) accountAccount).putOpeningBalance(cursor.getString(C_BALANCEFORMATTED));
				// Get the KeyValuePairs for this id.
				kmmKVP = KMMDapp.db.query("kmmKeyValuePairs", new String[] { "kvpData" },
						"kvpId=? AND kvpType='ACCOUNT' AND kvpKey='PreferredAccount'", new String[] { cursor.getString(C_ID) },
						null, null, null);
				startManagingCursor(kmmKVP);
				kmmKVP.moveToFirst();
				if( kmmKVP.getCount() > 0 )
				{
					((CreateAccountAccountActivity) accountAccount).putPreferredAccount(true);
				}
				else
					((CreateAccountAccountActivity) accountAccount).putPreferredAccount(false);
				kmmKVP.close();
				getTabHost().setCurrentTab(2);
				Activity accountParent = this.getCurrentActivity();
				((CreateAccountParentActivity) accountParent).putParentId(cursor.getString(C_PARENTID));
				// Make sure the 1st tab is displayed to the user.
				getTabHost().setCurrentTab(0);		
				Log.d(TAG, "reached the end of onResume for editing");
			}
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
	
	@Override
	public boolean onPrepareOptionsMenu (Menu menu)
	{
		if( Action == ACTION_EDIT )
		{
			if(cursor.getInt(C_TRANSACTIONCOUNT) > 0)
			{
				menu.getItem(1).setVisible(false);
			}
			else
			{
				menu.getItem(1).setVisible(true);
			}
		}
		else
			menu.getItem(1).setVisible(false);
	    return true;
	}
	
	// Called when an options item is clicked
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.itemsave:
				String instId, accountNumber, accountIBAN, parentId, accountType, accountTypeString, accountName;
				String currencyId, balance, openDate, preferredAcct = "No";
				int transactionCount = 0;
				
				// Get the Institution elements
				getTabHost().setCurrentTab(0);
				Activity accountInst = this.getCurrentActivity();
				boolean useInst = ((CreateAccountInstitutionActivity) accountInst).getUseInstitution();
				if(useInst)
					instId = ((CreateAccountInstitutionActivity) accountInst).getInstitutionId();
				else
					instId = "";
				accountNumber = ((CreateAccountInstitutionActivity) accountInst).getAccountNumber();
				accountIBAN = ((CreateAccountInstitutionActivity) accountInst).getIBAN();
				
				// Get the general Account elements
				getTabHost().setCurrentTab(1);
				Activity accountAcct = this.getCurrentActivity();
				accountName = ((CreateAccountAccountActivity) accountAcct).getAccountName();
				if(accountName.isEmpty())
				{
					AlertDialog.Builder alert = new AlertDialog.Builder(this);
				
					alert.setTitle(getString(R.string.error));
					alert.setMessage(getString(R.string.accountNameNotEntered));

					alert.setPositiveButton(getString(R.string.titleButtonOK), new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
						}
					});
					alert.show();
				}
				else
				{
					accountType = String.valueOf(((CreateAccountAccountActivity) accountAcct).getAccountType());
					accountTypeString = ((CreateAccountAccountActivity) accountAcct).getAccountTypeString();
					currencyId = ((CreateAccountAccountActivity) accountAcct).getCurrency();
					openDate = ((CreateAccountAccountActivity) accountAcct).getOpeningDate();
					balance = ((CreateAccountAccountActivity) accountAcct).getOpeningBalance();
					if( ((CreateAccountAccountActivity) accountAcct).getPreferredAccount() )
						preferredAcct = "Yes";
					
					// Get the Parent account
					getTabHost().setCurrentTab(2);
					Activity accountParent = this.getCurrentActivity();
					parentId = ((CreateAccountParentActivity) accountParent).getParentId();
				
					// Create the ContentValue pairs and then insert the new account.
					ContentValues valuesAccount = new ContentValues();
					if(Action == ACTION_NEW)
						accountId = createAccountId();

					valuesAccount.put("id", accountId);
					Log.d(TAG, "id: " + accountId);
					valuesAccount.put("institutionId", instId);
					Log.d(TAG, "instId: " + instId);
					valuesAccount.put("parentId", parentId);
					Log.d(TAG, "parentId: " + parentId);
					valuesAccount.put("openingDate", openDate);
					Log.d(TAG, "openDate: " + openDate);
					valuesAccount.put("accountNumber", accountNumber);
					Log.d(TAG, "accountNumber: " + accountNumber);
					valuesAccount.put("accountType", accountType);
					Log.d(TAG, "accountType" + String.valueOf(accountType));
					valuesAccount.put("accountTypeString", accountTypeString);
					Log.d(TAG, "accountTypeString: " + accountTypeString);
					valuesAccount.put("accountName", accountName);
					Log.d(TAG, "accountName: " + accountName);
					valuesAccount.put("description", "");
					valuesAccount.put("currencyId", currencyId);
					Log.d(TAG, "currencyId: " + currencyId);
					valuesAccount.put("balance", createBalance(balance));
					Log.d(TAG, "balance: " + createBalance(balance));
					valuesAccount.put("balanceFormatted", balance);
					Log.d(TAG, "balanceFormatted: " + balance);
					
					switch(Action)
					{
						case ACTION_NEW:
							valuesAccount.put("transactionCount", transactionCount);
							valuesAccount.put("isStockAccount", "N");
							valuesAccount.put("lastReconciled", "");
							valuesAccount.put("lastModified", "");

							try 
							{
								KMMDapp.db.insertOrThrow("kmmAccounts", null, valuesAccount);
							} 
							catch (SQLException e)
							{
								// TODO Auto-generated catch block
								Log.d(TAG, "error: " + e.getMessage());
							}
							increaseAccountId();
							// We need to put the additional information in the kmmKeyValuePairs table for this account.
							String kvpType = "ACCOUNT";
							String kvpId = accountId;
							ContentValues valuesKVP = new ContentValues();
							valuesKVP.put("kvpType", kvpType);
							valuesKVP.put("kvpId", kvpId);
							valuesKVP.put("kvpKey", "IBAN");
							valuesKVP.put("kvpData", accountIBAN);
							KMMDapp.db.insert("kmmKeyValuePairs", null, valuesKVP);
							valuesKVP.clear();
							if( preferredAcct.equalsIgnoreCase("Yes") )
							{
								valuesKVP.put("kvpType", kvpType);
								valuesKVP.put("kvpId", kvpId);
								valuesKVP.put("kvpKey", "PreferredAccount");
								valuesKVP.put("kvpData", "Yes");
								KMMDapp.db.insert("kmmKeyValuePairs", null, valuesKVP);
							}
							break;
						case ACTION_EDIT:
							KMMDapp.db.update("kmmAccounts", valuesAccount, "id=?", new String[] { accountId });
							// We need to put the additional information in the kmmKeyValuePairs table for this account.
							kvpType = "ACCOUNT";
							kvpId = accountId;
							valuesKVP = new ContentValues();
							valuesKVP.put("kvpType", kvpType);
							valuesKVP.put("kvpId", kvpId);
							valuesKVP.put("kvpKey", "IBAN");
							valuesKVP.put("kvpData", accountIBAN);
							KMMDapp.db.update("kmmKeyValuePairs", valuesKVP, "kvpId=?", new String[] { accountId });
							valuesKVP.clear();
							if( preferredAcct.equalsIgnoreCase("Yes") )
							{
								valuesKVP.put("kvpType", kvpType);
								valuesKVP.put("kvpId", kvpId);
								valuesKVP.put("kvpKey", "PreferredAccount");
								valuesKVP.put("kvpData", "Yes");
								KMMDapp.db.update("kmmKeyValuePairs", valuesKVP, "kvpId=?", new String[] { accountId } );
							}
							else
							{
								// See if the account was previously setup as a preferred account, if so remove it.
								Cursor c = KMMDapp.db.query("kmmKeyValuePairs", new String[] { "kvpData" },
										"kvpId=? AND kvpType='ACCOUNT' AND kvpKey='PreferredAccount'", new String[] { cursor.getString(C_ID) },
										null, null, null);
								startManagingCursor(c);
								if(c.getCount() > 0)
									KMMDapp.db.delete("kmmKeyValuePairs", "kvpId=? AND kvpType='ACCOUNT' AND kvpKey='PreferredAccount'", 
											new String[] { accountId });
							}
							break;
					}
				}
				finish();
				break;
			case R.id.itemDelete:
				KMMDapp.db.delete("kmmKeyValuePairs", "kvpId=?", new String [] { accountId });
				int rows = KMMDapp.db.delete("kmmAccounts", "id=?", new String[] { accountId });
				if( rows != 1 )
				{
					Log.d(TAG, "There was an error deleting your category!");
					// TODO Auto-generated catch block
					AlertDialog.Builder alert = new AlertDialog.Builder(this);
					alert.setTitle(getString(R.string.error));
					alert.setMessage(getString(R.string.unableToDelete) + "rows deleted=" + 
							String.valueOf(rows));

					alert.setPositiveButton(getString(R.string.titleButtonOK), new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							finish();
						}
					});
					alert.show();							
				}
				else
				{
					returnFromDelete = true;
					finish();
				}
				break;
		}
		return true;
	}
	
	// *****************************************************************************************************************************
	// ********************************************** Helper Functions *************************************************************
	
	private String createAccountId()
	{
		final String[] dbColumns = { "hiAccountId"};
		final String strOrderBy = "hiAccountId DESC";
		// Run a query to get the Acount ids so we can create a new one.
		cursor = KMMDapp.db.query("kmmFileInfo", dbColumns, null, null, null, null, strOrderBy);
		startManagingCursor(cursor);
		
		cursor.moveToFirst();

		// Since id is in A000000 format, we need to pick off the actual number then increase by 1.
		int lastId = cursor.getInt(0);
		lastId = lastId +1;
		String nextId = Integer.toString(lastId);
		
		// Need to pad out the number so we get back to our P000000 format
		String newId = "A";
		for(int i= 0; i < (6 - nextId.length()); i++)
		{
			newId = newId + "0";
		}
		
		// Tack on the actual number created.
		newId = newId + nextId;
		
		return newId;
	}
	
	private void increaseAccountId()
	{
		final String[] dbColumns = { "hiAccountId"};
		final String strOrderBy = "hiAccountId DESC";
		// Run a query to get the Account ids so we can create a new one.
		cursor = KMMDapp.db.query("kmmFileInfo", dbColumns, null, null, null, null, strOrderBy);
		startManagingCursor(cursor);
		
		cursor.moveToFirst();
		int lastId = cursor.getInt(0);	
		lastId = lastId + 1;
		
		ContentValues values = new ContentValues();
		values.put("hiAccountId", lastId);
		KMMDapp.db.update("kmmFileInfo", values, null, null);
	}
	
	private String createBalance(String formattedValue)
	{
		StringTokenizer split = new StringTokenizer(formattedValue, ".");
		String dollars = split.nextToken();
		String cents = split.nextToken();
		String balance = dollars + cents;
		String denominator = "/100";
		
		return balance + denominator;
	}
}
