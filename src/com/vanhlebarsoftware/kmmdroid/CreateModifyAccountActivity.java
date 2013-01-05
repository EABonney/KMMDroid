package com.vanhlebarsoftware.kmmdroid;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.StringTokenizer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.SimpleCursorAdapter;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.TabHost.TabContentFactory;
import android.net.*;

public class CreateModifyAccountActivity extends FragmentActivity implements
								TabHost.OnTabChangeListener,
								CreateAccountInstitutionActivity.OnInstitutionSelectedListener,
								CreateAccountInstitutionActivity.OnNoInstitutionCheckedListener,
								CreateAccountInstitutionActivity.OnNewInstitutionClickedListener,
								CreateAccountInstitutionActivity.OnSendInstitutionData,
								CreateAccountAccountActivity.OnAccountPreferredCheckedListener,
								CreateAccountAccountActivity.OnSendAccountDataListener,
								CreateAccountParentActivity.OnSendParentDataListener
{
	private static final String TAG = CreateModifyAccountActivity.class.getSimpleName();
	private static final int ACTION_NEW = 1;
	private static final int ACTION_EDIT = 2;
	private static final int A_CREDITCARD = 4;
	private static final int A_LOAN = 5;
	private static final int A_INVESTMENT = 7;
	private static final int C_ID = 0;
	private static final int C_INSTITUTIONID = 1;
	private static final int C_PARENTID = 2;
	private static final int C_OPENINGDATE = 5;
	private static final int C_ACCOUNTNUMBER = 6;
	private static final int C_ACCOUNTTYPE = 7;
	private static final int C_ACCOUNTTYPESTRING = 8;
	private static final int C_ACCOUNTNAME = 10;
	private static final int C_CURRENCYID = 12;
	private static final int C_BALANCEFORMATTED = 14;	
	private static final int C_TRANSACTIONCOUNT = 15;
	
	private int Action = 0;
	private String accountId = null;
	private String parentAccountSelection = null;
	private boolean returnFromDelete = false;
	private boolean isDirty = false;
	private boolean firstRun = true;
	KMMDroidApp KMMDapp;
	TextView payeeName;
	TextView title;
	//SimpleCursorAdapter adapter;
	TabHost tabHost;
	
	private HashMap<String, TabInfo> mapTabInfo = new HashMap<String, CreateModifyAccountActivity.TabInfo>();
	private TabInfo mLastTab = null;
	
	/********************************************************************************************************
	 * Class variables to hold the Institution data - used to pass down to the fragment and to save in db.
	 *******************************************************************************************************/
	String InstId = null;
	String AccountNum = null;
	String IBAN = null;
	boolean useInst = false;
	
	String AccountName = null;
	int AccountType = 0;
	String AccountTypeString = null;
	String CurrencyId = null;
	String OpeningDate = null;
	String OpeningBal = null;
	String TransCount = null;
	boolean PreferredAcct = false;
	
	String ParentId = null;
	
	/* Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
        setContentView(R.layout.create_accounts);
        
		initialiseTabHost(savedInstanceState);
		if (savedInstanceState != null) 
		{
            tabHost.setCurrentTabByTag(savedInstanceState.getString("tab")); //set the tab as per the saved state
        }
 
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
		
		// clean up the preferences we no longer need.
		SharedPreferences prefs = getPreferences(Activity.MODE_PRIVATE);
		SharedPreferences.Editor edit = prefs.edit();
		edit.clear();
		edit.apply();
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
				String frag = "#9999";
				Uri u = Uri.withAppendedPath(KMMDProvider.CONTENT_ACCOUNT_URI, accountId + frag);
				u = Uri.parse(u.toString());
				Cursor c = getBaseContext().getContentResolver().query(u, dbColumns, null, null, null);
				
				c.moveToFirst();
				// Check to see if we are editing one of the currently supported account types. If not tell the user and then return to the
				// account lists.
				if( c.getInt(C_ACCOUNTTYPE) == A_CREDITCARD || c.getInt(C_ACCOUNTTYPE) == A_LOAN ||
						c.getInt(C_ACCOUNTTYPE) == A_INVESTMENT )
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
				if ( c.getCount() == 0 )
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
				
				if ( c.getCount() > 1)
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
				String strColumns[] = { "kvpData" };
				String strSelection = "kvpId=? AND kvpType='ACCOUNT' AND kvpKey='IBAN'";
				String selectionArgs[] = { c.getString(c.getColumnIndex("id")) };
				frag = "#9999";
				u = Uri.withAppendedPath(KMMDProvider.CONTENT_KVP_URI, frag);
				u = Uri.parse(u.toString());
				Cursor kmmKVP = getContentResolver().query(u, strColumns, strSelection, selectionArgs, null);
				
				// We have the correct account Id, so now popluate the forms fields.
				AccountNum = c.getString(c.getColumnIndex("accountNumber"));
				// if we have an institudtionID then we need to populate the fields.
				if(c.getString(c.getColumnIndex("institutionId")) != null)
				{
					useInst = true;
					InstId = c.getString(c.getColumnIndex("institutionId"));
				}
				else
				{
					useInst = false;
				}
				if( kmmKVP.getCount() > 0)
				{
					kmmKVP.moveToFirst();
					IBAN = kmmKVP.getString(kmmKVP.getColumnIndex("kvpData"));
				}
				kmmKVP.close();

				ParentId = c.getString(c.getColumnIndex("parentId"));
				
				AccountName = c.getString(c.getColumnIndex("accountName"));
				AccountType = c.getInt(c.getColumnIndex("accountType"));
				AccountTypeString = c.getString(c.getColumnIndex("accountTypeString"));
				CurrencyId = c.getString(c.getColumnIndex("currencyId"));
				// We actually need to pull the correct balance information for this account, if query returns null, then opening bal was zero.
				frag = "#9999";
				u = Uri.withAppendedPath(KMMDProvider.CONTENT_SPLIT_URI, frag);
				u = Uri.parse(u.toString());
				Cursor bal = getContentResolver().query(u, new String[] { "valueFormatted", "postDate" }, "accountId=? AND splitId='0' AND "
														+ "(payeeId IS NULL OR payeeId='')", new String[] { accountId }, null);
				if( bal.getCount() > 0 )
				{
					bal.moveToFirst();
					OpeningDate = bal.getString(bal.getColumnIndex("postDate"));
					OpeningBal = bal.getString(bal.getColumnIndex("valueFormatted"));
				}
				else
				{
					OpeningDate = c.getString(c.getColumnIndex("openingDate"));
					OpeningBal = "";
				}
				bal.close();
				TransCount = String.valueOf(c.getInt(c.getColumnIndex("transactionCount")));

				// Get the KeyValuePairs for this id.
				frag = "#9999";
				u = Uri.withAppendedPath(KMMDProvider.CONTENT_KVP_URI, frag);
				u = Uri.parse(u.toString());
				strSelection = "kvpId=? AND kvpType='ACCOUNT' AND kvpKey='PreferredAccount'";
				kmmKVP = getBaseContext().getContentResolver().query(u, strColumns, strSelection, selectionArgs, null);
				if( kmmKVP.getCount() > 0 )
					PreferredAcct = true;
				else
					PreferredAcct = false;
				
				// Close our cursors.
				c.close();
				kmmKVP.close();     						
			}
		}
	}
	
	/** (non-Javadoc)
     * @see android.support.v4.app.FragmentActivity#onSaveInstanceState(android.os.Bundle)
     */
    protected void onSaveInstanceState(Bundle outState) 
    {
        outState.putString("tab", tabHost.getCurrentTabTag()); //save the tab selected
        super.onSaveInstanceState(outState);
    }
	
	public void onBackPressed()
	{				
		if( getIsDirty() )
		{
			AlertDialog.Builder alertDel = new AlertDialog.Builder(new ContextThemeWrapper(this,
																	R.style.AlertDialogNoTitle));
			alertDel.setTitle(R.string.BackActionWarning);
			alertDel.setMessage(getString(R.string.titleBackActionWarning));

			alertDel.setPositiveButton(getString(R.string.titleButtonOK), new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int whichButton)
				{
					finish();
				}
			});
			
			alertDel.setNegativeButton(getString(R.string.titleButtonCancel), new DialogInterface.OnClickListener() 
			{
				public void onClick(DialogInterface dialog, int whichButton) 
				{
					// Canceled.
					Log.d(TAG, "User cancelled back action.");
				}
			});				
			alertDel.show();
		}
		else
		{
			finish();
		}
	}
	
	public void onTabChanged(String tag) 
	{
		TabInfo newTab = this.mapTabInfo.get(tag);
		if (mLastTab != newTab) 
		{
			FragmentTransaction ft = this.getSupportFragmentManager().beginTransaction();
            if (mLastTab != null) 
            {
                if (mLastTab.fragment != null) 
                {
                	// save the tabs UI elements.
                	if( !firstRun )
                		saveTabUI(mLastTab.fragment);
                	ft.detach(mLastTab.fragment);
                }
            }
            if (newTab != null) 
            {
                if (newTab.fragment == null) 
                {
                    newTab.fragment = Fragment.instantiate(this,
                            newTab.clss.getName(), newTab.args);
                    ft.add(R.id.realtabcontent, newTab.fragment, newTab.tag);
                } 
                else 
                {
                    ft.attach(newTab.fragment);
                }
            }

            mLastTab = newTab;
            ft.commit();
            this.getSupportFragmentManager().executePendingTransactions();
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
			final String[] dbColumns = { "*" };
			String frag = "#9999";
			Uri u = Uri.withAppendedPath(KMMDProvider.CONTENT_ACCOUNT_URI, accountId + frag);
			u = Uri.parse(u.toString());
			Cursor c = getBaseContext().getContentResolver().query(u, dbColumns, null, null, null);
			
			// Get if the account is open or closed.
			frag = "#9999";
			u = Uri.withAppendedPath(KMMDProvider.CONTENT_KVP_URI, frag);
			u = Uri.parse(u.toString());
			Cursor closed = getBaseContext().getContentResolver().query(u, new String[] { "kvpKey", "kvpData" }, "kvpId=? AND kvpKey='mm-closed'", 
																		 new String[] { accountId }, null);
			c.moveToFirst();
			// If the account balance is zero AND we only have 1 transaction, then we can delete the account.
			// Otherwise, we can't so don't show the user that option.
			if(c.getInt(c.getColumnIndex("transactionCount")) > 1)
				menu.findItem(R.id.itemDelete).setVisible(false);
			else if(c.getInt(c.getColumnIndex("transactionCount")) == 1 && Account.convertBalance(c.getString(c.getColumnIndex("balance"))) != 0)
				menu.findItem(R.id.itemDelete).setVisible(false);
			else
				menu.findItem(R.id.itemDelete).setVisible(true);
			
			// If the account balance is not zero then we can't close this account nor can we open it as it is already open.
			if(Account.convertBalance(c.getString(c.getColumnIndex("balance"))) != 0)
			{
				menu.findItem(R.id.itemClose).setVisible(false);
				menu.findItem(R.id.itemOpenAcct).setVisible(false);
			}
			
			// If the account is closed, give user option to open it again and we can't close it again of course.
			if( closed.getCount() > 0 )
			{
				menu.findItem(R.id.itemOpenAcct).setVisible(true);
				menu.findItem(R.id.itemClose).setVisible(false);
			}
			else
				menu.findItem(R.id.itemOpenAcct).setVisible(false);
			
			// close our cursors.
			closed.close();
			c.close();
		}
		else
		{
			menu.findItem(R.id.itemClose).setVisible(false);
			menu.findItem(R.id.itemDelete).setVisible(false);
			menu.findItem(R.id.itemOpenAcct).setVisible(false);
		}
		
	    return true;
	}
	
	// Called when an options item is clicked
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.itemsave:
				Log.d(TAG, "Saving acccount....");
				String instId, accountNumber, accountIBAN, parentId, accountType, accountTypeString, accountName;
				String currencyId, balance, openDate, preferredAcct = "No";
				int transactionCount = 0;
				
				// Get the Institution elements
				Fragment accountInst = this.getSupportFragmentManager().findFragmentByTag("institution");
				boolean useInst = ((CreateAccountInstitutionActivity) accountInst).getUseInstitution();
				if(useInst)
					instId = ((CreateAccountInstitutionActivity) accountInst).getInstitutionId();
				else
					instId = "";
				accountNumber = ((CreateAccountInstitutionActivity) accountInst).getAccountNumber();
				accountIBAN = ((CreateAccountInstitutionActivity) accountInst).getIBAN();
				
				// Get the general Account elements
				Fragment accountAcct = this.getSupportFragmentManager().findFragmentByTag("account");
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
					Fragment accountParent = this.getSupportFragmentManager().findFragmentByTag("parent");
					parentId = ((CreateAccountParentActivity) accountParent).getParentId();
				
					// Create the ContentValue pairs and then insert the new account.
					ContentValues valuesAccount = new ContentValues();
					if(Action == ACTION_NEW)
						accountId = createAccountId();

					valuesAccount.put("id", accountId);
					valuesAccount.put("institutionId", instId);
					valuesAccount.put("parentId", parentId);
					valuesAccount.put("openingDate", openDate);
					Log.d(TAG, "accountNumber: " + accountNumber);
					valuesAccount.put("accountNumber", accountNumber);
					valuesAccount.put("accountType", accountType);
					valuesAccount.put("accountTypeString", accountTypeString);
					valuesAccount.put("accountName", accountName);
					valuesAccount.put("description", "");
					valuesAccount.put("currencyId", currencyId);
					
					switch(Action)
					{
						case ACTION_NEW:
							valuesAccount.put("transactionCount", transactionCount);
							valuesAccount.put("isStockAccount", "N");
							valuesAccount.put("lastReconciled", "");
							valuesAccount.put("lastModified", "");
							// We initially set the balance and balanceFormatted to zero because they are updated to the actual amounts
							// when the splits are posted later on in createTransaction().
							valuesAccount.put("balance", "0/1");
							valuesAccount.put("balanceFormatted", "0.00");

							try 
							{
								String frag = "#9999";
								Uri u = Uri.withAppendedPath(KMMDProvider.CONTENT_ACCOUNT_URI, frag);
								u = Uri.parse(u.toString());
								getBaseContext().getContentResolver().insert(u,valuesAccount);
								frag = "#9999";
								u = Uri.withAppendedPath(KMMDProvider.CONTENT_FILEINFO_URI, frag);
								u = Uri.parse(u.toString());
								getBaseContext().getContentResolver().update(u, null, "transactions", new String[] { "1" });
								getBaseContext().getContentResolver().update(u, null, "hiAccountId", new String[] { "1" });
								getBaseContext().getContentResolver().update(u, null, "accounts", new String[] { "1" });
							} 
							catch (SQLException e)
							{
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
							String frag = "#9999";
							Uri u = Uri.withAppendedPath(KMMDProvider.CONTENT_KVP_URI, frag);
							u = Uri.parse(u.toString());
							getBaseContext().getContentResolver().insert(u, valuesKVP);
							valuesKVP.clear();
							if( preferredAcct.equalsIgnoreCase("Yes") )
							{
								valuesKVP.put("kvpType", kvpType);
								valuesKVP.put("kvpId", kvpId);
								valuesKVP.put("kvpKey", "PreferredAccount");
								valuesKVP.put("kvpData", "Yes");
								getBaseContext().getContentResolver().insert(u, valuesKVP);
							}
							// Create the transaction and splits for any opening balance that was entered by the user.
							createTransaction(openDate, currencyId, balance, accountId);
							break;
						case ACTION_EDIT:
							// First we need to get the old account balance and make any necessary adjustments to this balance based on
							// the changes the user may have made to our Opening balance amount for this account.
							// Now we need to adjust any Splits/Transactions we originally had for this account.
							adjustOpenTrans(accountId, balance, openDate, currencyId);
							// Actually update the account now.
							frag = "#9999";
							u = Uri.withAppendedPath(KMMDProvider.CONTENT_ACCOUNT_URI, accountId + frag);
							u = Uri.parse(u.toString());
							getBaseContext().getContentResolver().update(u, valuesAccount, null, null);
							// We need to put the additional information in the kmmKeyValuePairs table for this account.
							kvpType = "ACCOUNT";
							kvpId = accountId;
							valuesKVP = new ContentValues();
							valuesKVP.put("kvpType", kvpType);
							valuesKVP.put("kvpId", kvpId);
							valuesKVP.put("kvpKey", "IBAN");
							valuesKVP.put("kvpData", accountIBAN);
							frag = "#9999";
							u = Uri.withAppendedPath(KMMDProvider.CONTENT_KVP_URI, frag);
							u = Uri.parse(u.toString());
							getBaseContext().getContentResolver().update(u, valuesKVP, "kvpId=? AND kvpType=? AND kvpKey=?", 
																		 new String[] { accountId, kvpType, "IBAN" });
							valuesKVP.clear();
							// See if the account was previously setup as a preferred account.
							Cursor c = getBaseContext().getContentResolver().query(u, new String[] { "kvpData" },
									"kvpId=? AND kvpType='ACCOUNT' AND kvpKey='PreferredAccount'", new String[] { accountId }, null);
							if( preferredAcct.equalsIgnoreCase("Yes") )
							{
								valuesKVP.put("kvpType", kvpType);
								valuesKVP.put("kvpId", kvpId);
								valuesKVP.put("kvpKey", "PreferredAccount");
								valuesKVP.put("kvpData", "Yes");
								if( c.getCount() > 0)
									getBaseContext().getContentResolver().update(u, valuesKVP, "kvpId=? AND kvpType=? AND kvpKey=?",
																			 new String[] { accountId, kvpType, "PreferredAccount" } );
								else
									getBaseContext().getContentResolver().insert(u, valuesKVP);
							}
							else
							{
								if(c.getCount() > 0)
									getBaseContext().getContentResolver().delete(u, "kvpId=? AND kvpType='ACCOUNT' AND kvpKey='PreferredAccount'",
																				 new String[] {accountId});
							}
							c.close();
							break;
					}
				}
				String frag = "#9999";
				Uri u = Uri.withAppendedPath(KMMDProvider.CONTENT_FILEINFO_URI, frag);
				u = Uri.parse(u.toString());
				getBaseContext().getContentResolver().update(u, null, "lastModified", new String[] { "0" });
				
				//Mark file as dirty
				KMMDapp.markFileIsDirty(true, "9999");
				// Get the loader to refresh.
				sendAccountsChangeMsg();				
				finish();
				break;
			case R.id.itemClose:
				// Mark this account as closed.
				String kvpType = "ACCOUNT";
				String kvpId = accountId;
				ContentValues valuesKVP = new ContentValues();
				valuesKVP.put("kvpType", kvpType);
				valuesKVP.put("kvpId", kvpId);
				valuesKVP.put("kvpKey", "mm-closed");
				valuesKVP.put("kvpData", "yes");
				frag = "#9999";
				u = Uri.withAppendedPath(KMMDProvider.CONTENT_KVP_URI, frag);
				u = Uri.parse(u.toString());
				getBaseContext().getContentResolver().insert(u, valuesKVP);
				sendAccountsChangeMsg();
				finish();
				break;
			case R.id.itemOpenAcct:
				// Re-open this account.
				frag = "#9999";
				u = Uri.withAppendedPath(KMMDProvider.CONTENT_KVP_URI, frag);
				u = Uri.parse(u.toString());
				getBaseContext().getContentResolver().delete(u, "kvpId=? AND kvpKey='mm-closed'", new String[] { accountId });
				sendAccountsChangeMsg();
				finish();
				break;
			case R.id.itemDelete:
				// Need to find our transactionId that belongs to this account so we can delete it.
				/*frag = "#9999";
				u = Uri.withAppendedPath(KMMDProvider.CONTENT_SPLIT_URI, frag);
				u = Uri.parse(u.toString());
				Cursor c = getBaseContext().getContentResolver().query(u, new String[] { "transactionId" }, "accountId=? AND splitId='0'" +
																	   " AND (payeeid IS NULL OR payeeId='')", new String[] { accountId }, null);
				c.moveToFirst();
				String transId = c.getString(c.getColumnIndex("transactionId"));
				c.close();
				frag = "#9999";
				u = Uri.withAppendedPath(KMMDProvider.CONTENT_SPLIT_URI, frag);
				u = Uri.parse(u.toString());
				getBaseContext().getContentResolver().delete(u, "transactionId=?", new String[] { transId });
				frag = "#9999";
				u = Uri.withAppendedPath(KMMDProvider.CONTENT_TRANSACTION_URI, frag);
				u = Uri.parse(u.toString());
				getBaseContext().getContentResolver().delete(u, "id=?", new String[] { transId });*/
				frag = "#9999";
				u = Uri.withAppendedPath(KMMDProvider.CONTENT_KVP_URI, frag);
				u = Uri.parse(u.toString());
				getBaseContext().getContentResolver().delete(u, "kvpId=?", new String [] { accountId });
				frag = "#9999";
				u = Uri.withAppendedPath(KMMDProvider.CONTENT_ACCOUNT_URI, frag);
				u = Uri.parse(u.toString());				
				int rows = getBaseContext().getContentResolver().delete(u, "id=?", new String[] { accountId });
				frag = "#9999";
				u = Uri.withAppendedPath(KMMDProvider.CONTENT_FILEINFO_URI, frag);
				u = Uri.parse(u.toString());
				getBaseContext().getContentResolver().update(u, null, "accounts", new String[] { "-1" });
				getBaseContext().getContentResolver().update(u, null, "transactions", new String[] { "-1" });
				getBaseContext().getContentResolver().update(u,	null, "splits", new String[] { "-2" });
				if( rows != 1 )
				{
					Log.d(TAG, "There was an error deleting your category!");
					AlertDialog.Builder alert = new AlertDialog.Builder(this);
					alert.setTitle(getString(R.string.error));
					alert.setMessage(getString(R.string.unableToDelete) + "rows deleted = " + 
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
					frag = "#9999";
					u = Uri.withAppendedPath(KMMDProvider.CONTENT_FILEINFO_URI, frag);
					u = Uri.parse(u.toString());
					getBaseContext().getContentResolver().update(u, null, "lastModified", new String[] { "0" });
					
					//Mark file as dirty
					KMMDapp.markFileIsDirty(true, "9999");
					//Get the loader to refresh.
					sendAccountsChangeMsg();
					finish();
				}
				break;
		}
		return true;
	}
	
	public void onAccountPreferredChecked(CompoundButton btn, boolean arg1) 
	{
		// TODO Auto-generated method stub
		
	}

	public void onNoInstitutionChecked(CompoundButton btn, boolean arg1) 
	{
		// TODO Auto-generated method stub
		
	}

	public void onInstitutionSelected(AdapterView<?> parent, View view,
			int pos, long id) 
	{
		// TODO Auto-generated method stub
		
	}

	public void onNewInstitutionClicked(View view) 
	{
		// TODO Auto-generated method stub
		
	}
	
	public void onSendInstitutionData() 
	{
		// Send the Institution data back to the fragment.
		Fragment Inst = this.getSupportFragmentManager().findFragmentByTag("institution");
		((CreateAccountInstitutionActivity) Inst).putAccountNumber(AccountNum);
		((CreateAccountInstitutionActivity) Inst).putIBAN(IBAN);
		((CreateAccountInstitutionActivity) Inst).putInstitutionId(InstId);
		((CreateAccountInstitutionActivity) Inst).putUseInstitution(useInst);
	}
	
	public void onSendParentData() 
	{
		// Send the Parent data back to the fragment.
		Fragment Parent = this.getSupportFragmentManager().findFragmentByTag("parent");		
		((CreateAccountParentActivity) Parent).putParentId(ParentId);
	}

	public void onSendAccountData() 
	{
		// Send the account data back to the fragment.
		Fragment Account = this.getSupportFragmentManager().findFragmentByTag("account");
		((CreateAccountAccountActivity) Account).putAccountName(AccountName);
		((CreateAccountAccountActivity) Account).putAccountType(AccountType);
		((CreateAccountAccountActivity) Account).putAccountTypeString(AccountTypeString);
		((CreateAccountAccountActivity) Account).putCurrency(CurrencyId);
		((CreateAccountAccountActivity) Account).putOpeningDate(OpeningDate);
		((CreateAccountAccountActivity) Account).putOpeningBalance(OpeningBal);
		((CreateAccountAccountActivity) Account).putTransactionCount(TransCount);
		((CreateAccountAccountActivity) Account).putPreferredAccount(PreferredAcct);
	}
	// *****************************************************************************************************************************
	// ********************************************** Helper Functions *************************************************************
	
	private String createAccountId()
	{
		final String[] dbColumns = { "hiAccountId"};
		final String strOrderBy = "hiAccountId DESC";
		String frag = "#9999";
		Uri u = Uri.withAppendedPath(KMMDProvider.CONTENT_FILEINFO_URI, frag);
		u = Uri.parse(u.toString());
		// Run a query to get the Acount ids so we can create a new one.
		Cursor c = getBaseContext().getContentResolver().query(u, dbColumns, null, null, strOrderBy);
		
		c.moveToFirst();

		// Since id is in A000000 format, we need to pick off the actual number then increase by 1.
		int lastId = c.getInt(0);
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
		
		// Close our cursor.
		c.close();
		
		return newId;
	}
	
	private String createTransactionId()
	{
		final String[] dbColumns = { "hiTransactionId"};
		// Run a query to get the Transaction ids so we can create a new one.
		String frag = "#9999";
		Uri u = Uri.withAppendedPath(KMMDProvider.CONTENT_FILEINFO_URI, frag);
		u = Uri.parse(u.toString());
		Cursor cursor = getContentResolver().query(u, dbColumns, null, null, null);
		
		cursor.moveToFirst();

		// Since id is in T000000000000000000 format, we need to pick off the actual number then increase by 1.
		int lastId = cursor.getInt(0);
		lastId = lastId +1;
		String nextId = Integer.toString(lastId);
		
		// Need to pad out the number so we get back to our P000000 format
		String newId = "T";
		for(int i= 0; i < (18 - nextId.length()); i++)
		{
			newId = newId + "0";
		}
		
		// Tack on the actual number created.
		newId = newId + nextId;
		
		// close our cursor
		cursor.close();
		
		return newId;
	}
	
	private void increaseAccountId()
	{
		final String[] dbColumns = { "hiAccountId"};
		final String strOrderBy = "hiAccountId DESC";
		String frag = "#9999";
		Uri u = Uri.withAppendedPath(KMMDProvider.CONTENT_FILEINFO_URI, frag);
		u = Uri.parse(u.toString());
		// Run a query to get the Account ids so we can create a new one.
		Cursor cursor = getContentResolver().query(u, dbColumns, null, null, strOrderBy);
		
		cursor.moveToFirst();
		int lastId = cursor.getInt(0);	
		lastId = lastId + 1;
		
		getBaseContext().getContentResolver().update(u, null, "hiAccountId", new String[] { "0" });

		// close our cursor.
		cursor.close();
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
	
	private String updateBalance(String newOpenBal)
	{
		long longNewOpenBal = Transaction.convertToPennies(newOpenBal);
		long longOldAcctBal = 0;
		long longOldOpenBal = 0;
		long longDifference = 0;
		String newBal = null;
		String frag = "#9999";
		Uri u = Uri.withAppendedPath(KMMDProvider.CONTENT_ACCOUNT_URI,accountId + frag);
		u = Uri.parse(u.toString());
		Cursor acct = getContentResolver().query(u, null, null, null, null);
		if( acct.getCount() == 1 )
		{
			acct.moveToFirst();
			longOldAcctBal = Transaction.convertToPennies(acct.getString(acct.getColumnIndex("balanceFormatted")));
		}
		else
		{
			longOldAcctBal = (long) 0;
		}
		
		frag = "#9999";
		u = Uri.withAppendedPath(KMMDProvider.CONTENT_SPLIT_URI, frag);
		u = Uri.parse(u.toString());
		Cursor bal = getContentResolver().query(u, new String[] { "valueFormatted", "postDate" }, "accountId=? AND splitId='0' AND "
												+ "(payeeId IS NULL OR payeeId='')", new String[] { accountId }, null);
		if( bal.getCount() > 0 )
		{
			bal.moveToFirst();
			longOldOpenBal = Transaction.convertToPennies(bal.getString(bal.getColumnIndex("valueFormatted")));
			longDifference = longNewOpenBal - longOldOpenBal;
			newBal = Transaction.convertToDollars(longOldAcctBal + longDifference, false);
		}
		else
			newBal = Transaction.convertToDollars(longOldAcctBal + longNewOpenBal, false);
		
		// Clean up our cursors.
		acct.close();
		bal.close();
		
		Log.d(TAG, "newOpenBal: " + newOpenBal);
		Log.d(TAG, "longNewOpenBal: " + longNewOpenBal);
		Log.d(TAG, "longOldAcctBal: " + longOldAcctBal);
		Log.d(TAG, "longOldOpenBal: " + longOldOpenBal);
		Log.d(TAG, "longDifference: " + longDifference);
		Log.d(TAG, "newBal: " + newBal);
		
		return newBal;
	}
	
	private void adjustOpenTrans(String acctId, String openBalance, String openDate, String baseCurId)
	{
		// Run a query to get the accountId for "Opening Balances".
		String frag = "#9999";
		Uri u = Uri.withAppendedPath(KMMDProvider.CONTENT_ACCOUNT_URI, frag);
		u = Uri.parse(u.toString());
		String Columns[] = { "id" };
		String Selection = "accountName='Opening Balances'";
		Cursor openBalanceCursor = getContentResolver().query(u, Columns, Selection, null, null);
		openBalanceCursor.moveToFirst();
		String OpeningBalancesId = openBalanceCursor.getString(openBalanceCursor.getColumnIndex("id"));
		// clean up our cursor.
		openBalanceCursor.close();
		long longOpenBal = Transaction.convertToPennies(openBalance);
		// First let's see if this account even has an opening balance transaction to begin with.
		frag = "#9999";
		u = Uri.withAppendedPath(KMMDProvider.CONTENT_SPLIT_URI, frag);
		u = Uri.parse(u.toString());
		Cursor splits = getContentResolver().query(u, new String[] { "*" }, "accountId=? AND splitId='0' AND "
												+ "(payeeId IS NULL OR payeeId='')", new String[] { acctId }, null);

		// If query returns null then we didn't have any previous opening balance transactions.
		// If we have an opening balance entry now, we need to create the transaction and the splits.
		if( splits.getCount() == 0 )
		{
			Log.d(TAG, "We didn't have any previous opening balance, so we are creating the transaction now.");
			if( longOpenBal != 0 )
				createTransaction(openDate, baseCurId, openBalance, acctId);
		}
		else if( splits.getCount() > 0 )
		{
			frag = "#9999";
			u = Uri.withAppendedPath(KMMDProvider.CONTENT_ACCOUNT_URI, accountId + frag);
			u = Uri.parse(u.toString());
			String newBalance = updateBalance(openBalance);
			ContentValues cv = new ContentValues();
			cv.put("balance", createBalance(newBalance));
			cv.put("balanceFormatted", newBalance);
			getBaseContext().getContentResolver().update(u, cv, null, null);
			
			// If account had an opening transaction AND our current Opening Balance is not zero, update previous transaction with 
			// current value.
			splits.moveToFirst();
			if( longOpenBal != 0 )
			{
				Log.d(TAG, "We had a previous opening balance AND the user changed it to a non-zero amount, updating splits.");
				frag = "#9999";
				u = Uri.withAppendedPath(KMMDProvider.CONTENT_SPLIT_URI, frag);
				u = Uri.parse(u.toString());				
				cv.clear();
				cv.put("value", createBalance(openBalance));
				cv.put("valueFormatted", openBalance);
				cv.put("shares", createBalance(openBalance));
				cv.put("sharesFormatted", openBalance);
				getContentResolver().update(u, cv, "transactionId=? AND accountId=?", 
						new String[] { splits.getString(splits.getColumnIndex("transactionId")), acctId });
				cv.clear();
				// Now adjust the other side of the transaction, just using the negative.
				String revBal = Transaction.convertToDollars(longOpenBal * -1, false);
				cv.put("value", createBalance(revBal));
				cv.put("valueFormatted", revBal);
				cv.put("shares", createBalance(revBal));
				cv.put("sharesFormatted", revBal);
				getContentResolver().update(u, cv, "transactionId=? AND accountId=?", 
						new String[] { splits.getString(splits.getColumnIndex("transactionId")), OpeningBalancesId });
			}
			else if( longOpenBal == 0 )
			{
				Log.d(TAG, "We had a previous opening balance but the user has changed it to zero, deleting previous splits and transaction");
				// If account had an opening transaction AND our current Opening Balance is zero, we need to delete previous transaction
				frag = "#9999";
				u = Uri.withAppendedPath(KMMDProvider.CONTENT_SPLIT_URI, frag);
				u = Uri.parse(u.toString());	
				getContentResolver().delete(u, "transactionId=?", new String[] { splits.getString(splits.getColumnIndex("transactionId")) });
				frag = "#9999";
				u = Uri.withAppendedPath(KMMDProvider.CONTENT_TRANSACTION_URI,splits.getString(splits.getColumnIndex("transactionId")) + frag);
				u = Uri.parse(u.toString());	
				getContentResolver().delete(u, null, null);
				// Reduce the number of transactions and splits in kmmFileInfo table.
				frag = "#9999";
				u = Uri.withAppendedPath(KMMDProvider.CONTENT_FILEINFO_URI, frag);
				u = Uri.parse(u.toString());
				getBaseContext().getContentResolver().update(u, null, "transactions", new String[] { "-1" });
				getBaseContext().getContentResolver().update(u, null, "splits", new String[] { "-2" });
				// Reduce the number of transactions listed in kmmAccounts table for this account.
				frag = "#9999";
				u = Uri.withAppendedPath(KMMDProvider.CONTENT_ACCOUNT_URI, frag);
				u = Uri.parse(u.toString());				
				Cursor acct = getBaseContext().getContentResolver().query(u, new String[] { "transactionCount" },
						"id=?", new String[] { acctId }, null);
				acct.moveToFirst();
				cv.clear();
				int count = acct.getInt(acct.getColumnIndex("transactionCount"));
				cv.put("transactionCount", count - 1);
				getBaseContext().getContentResolver().update(u, cv, "id=?", new String[] { acctId });
				// clean up our cursor.
				acct.close();
			}
		}
		else
			Log.d(TAG, "Some how we didn't catch our splits/transactions update!");
		
		// clean up our cursor.
		splits.close();
	}
	
	public void setIsDirty(boolean flag)
	{
		this.isDirty = flag;
	}
	
	public boolean getIsDirty()
	{
		return this.isDirty;
	}
	
	private void createTransaction(String openDate, String baseCurId, String openBal, String acctId)
	{
		// Run a query to get the accountId for "Opening Balances".
		String frag = "#9999";
		Uri u = Uri.withAppendedPath(KMMDProvider.CONTENT_ACCOUNT_URI, frag);
		u = Uri.parse(u.toString());
		String Columns[] = { "id" };
		String Selection = "accountName='Opening Balances'";
		Cursor openBalanceCursor = getContentResolver().query(u, Columns, Selection, null, null);
		openBalanceCursor.moveToFirst();
		String OpeningBalancesId = openBalanceCursor.getString(openBalanceCursor.getColumnIndex("id"));
		openBalanceCursor.close();
		
		// create the ContentValue pairs
		ContentValues valuesTrans = new ContentValues();
		
		valuesTrans.put("txType", "N");
		valuesTrans.put("postDate", openDate);
		valuesTrans.put("memo", "");
		
        // get the current date
        final Calendar c = Calendar.getInstance();
        valuesTrans.put("entryDate", new StringBuilder()
			// Month is 0 based so add 1
			.append(c.get(Calendar.YEAR)).append("-")
			.append(c.get(Calendar.MONTH) + 1).append("-")
			.append(c.get(Calendar.DAY_OF_MONTH)).toString());
        
		valuesTrans.put("currencyId", baseCurId);
		valuesTrans.put("bankId", "");
		// Need to create the transaction id.
		String id = createTransactionId();
		valuesTrans.put("id", id);
		
		// Enter the transaction into the kmmTransactions table.
		frag = "#9999";
		u = Uri.withAppendedPath(KMMDProvider.CONTENT_TRANSACTION_URI, frag);
		u = Uri.parse(u.toString());
		getBaseContext().getContentResolver().insert(u, valuesTrans);
		
		// We need to take our editAmount string which "may" contain a '.' as the decimal and replace it with the localized seperator.
		DecimalFormat decimal = new DecimalFormat();
		char decChar = decimal.getDecimalFormatSymbols().getDecimalSeparator();
		String strAmount = openBal.replace('.', decChar);
		
		// Create the splits information to be saved.
		// Take the amount entered as is, use it for the account's balance and then use the negative of that amount as the offset to
		// Open Balances.
		ArrayList<Split> splits = new ArrayList<Split>();
		String value = null, formatted = null;
		value = Account.createBalance(Transaction.convertToPennies(openBal));
		formatted = Transaction.convertToDollars(Account.convertBalance(value), false);
		splits.add(new Split(id, "N", 0, "", "", "", "0", value, formatted, value, formatted,
				 "", "", "", acctId, "", openDate, ""));
		value = Account.createBalance(Transaction.convertToPennies(openBal) * -1);
		formatted = Transaction.convertToDollars(Account.convertBalance(value), false);
		splits.add(new Split(id, "N", 1, "", "", "", "0", value, formatted, value, formatted,
				 "", "", "", OpeningBalancesId, "", openDate, ""));		
		
		// Actually enter the transaction and splits into the database, update the fileInfo table and the account with the # of transactions.
		frag = "#9999";
		u = Uri.withAppendedPath(KMMDProvider.CONTENT_FILEINFO_URI, frag);
		u = Uri.parse(u.toString());
		getBaseContext().getContentResolver().update(u, null, "hiTransactionId", new String[] { "1" });
		getBaseContext().getContentResolver().update(u, null, "transactions", new String[] { "1" });
		getBaseContext().getContentResolver().update(u, null, "splits", new String[] { String.valueOf(splits.size()) });

		for(int i=0; i < splits.size(); i++)
		{
			Split s = splits.get(i);
			s.commitSplit(false, KMMDapp.db);
			Account.updateAccount(KMMDapp.db, s.getAccountId(), s.getValueFormatted(), 1);
		}
		getBaseContext().getContentResolver().update(u, null, "lastModified", new String[] { "0" });
	}

	/**
	 * Initialise the Tab Host
	 */
	private void initialiseTabHost(Bundle args) 
	{
		tabHost = (TabHost)findViewById(android.R.id.tabhost);
        tabHost.setup();
        TabInfo tabInfo = null;

        // Add the fragment for Institution
        CreateModifyAccountActivity.addTab(this, this.tabHost, 
        		this.tabHost.newTabSpec("institution").setIndicator(getString(R.string.AccountTabInstitution)),
        		( tabInfo = new TabInfo("institution", CreateAccountInstitutionActivity.class, args)));
        this.mapTabInfo.put(tabInfo.tag, tabInfo);
        
        // Add the fragment for the Account
        CreateModifyAccountActivity.addTab(this, this.tabHost,
        		this.tabHost.newTabSpec("account").setIndicator(getString(R.string.AccountTabAccount)),
        		( tabInfo = new TabInfo("account", CreateAccountAccountActivity.class, args)));
        this.mapTabInfo.put(tabInfo.tag, tabInfo);
        
        // Add the fragment for the Parent
        CreateModifyAccountActivity.addTab(this, this.tabHost,
        		this.tabHost.newTabSpec("parent").setIndicator(getString(R.string.AccountTabSubAccount)),
        		( tabInfo = new TabInfo("parent", CreateAccountParentActivity.class, args)));
        this.mapTabInfo.put(tabInfo.tag, tabInfo);
                     
        // Cycle through the tabs to populate all fields correctly.
        //this.onTabChanged("institution");
        //this.onTabChanged("account");
        //this.onTabChanged("parent");
        this.firstRun = false;
        
        // Default to first tab
        this.onTabChanged("institution");
        
        // Set the listener for the tab host.
        tabHost.setOnTabChangedListener(this);
	}
	
	/**
	 * @param activity
	 * @param tabHost
	 * @param tabSpec
	 * @param clss
	 * @param args
	 */
	private static void addTab(CreateModifyAccountActivity activity, TabHost tabHost, TabHost.TabSpec tabSpec, TabInfo tabInfo) 
	{
		// Attach a Tab view factory to the spec
		tabSpec.setContent(activity.new TabFactory(activity));
        String tag = tabSpec.getTag();

        // Check to see if we already have a fragment for this tab, probably
        // from a previously saved state.  If so, deactivate it, because our
        // initial state is that a tab isn't shown.
        tabInfo.fragment = activity.getSupportFragmentManager().findFragmentByTag(tag);
        if (tabInfo.fragment != null && !tabInfo.fragment.isDetached()) 
        {
            FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();
            ft.detach(tabInfo.fragment);
            ft.commit();
            activity.getSupportFragmentManager().executePendingTransactions();
        }

        tabHost.addTab(tabSpec);
	}
	
	private void saveTabUI(Fragment tab)
	{
		String tag = tab.getTag();
		SharedPreferences prefs = getPreferences(Activity.MODE_PRIVATE);
		SharedPreferences.Editor edit = prefs.edit();
		
		if(tag.equalsIgnoreCase("institution"))
		{
			edit.putString("AccountNumber", ((CreateAccountInstitutionActivity) tab).accountNumber.getText().toString());
			edit.putString("IBAN", ((CreateAccountInstitutionActivity) tab).accountIBAN.getText().toString());
			edit.putBoolean("UseInstitution", !((CreateAccountInstitutionActivity) tab).checkboxNoInstitution.isChecked());
			if(!((CreateAccountInstitutionActivity) tab).checkboxNoInstitution.isChecked())
				edit.putString("InstitutionId", ((CreateAccountInstitutionActivity) tab).getInstitutionId());
		}
		else if(tag.equalsIgnoreCase("account"))
		{
			edit.putString("AccountName", ((CreateAccountAccountActivity) tab).accountName.getText().toString());
			edit.putInt("AccountType", ((CreateAccountAccountActivity) tab).getAccountType());
			edit.putString("CurrencyId", ((CreateAccountAccountActivity) tab).getCurrency());
			edit.putString("OpenDate", ((CreateAccountAccountActivity) tab).openDate.getText().toString());
			edit.putString("OpenBalance", ((CreateAccountAccountActivity) tab).openBalance.getText().toString());
			edit.putBoolean("PreferredAccount", ((CreateAccountAccountActivity) tab).checkPreferred.isChecked());
		}
		else if(tag.equalsIgnoreCase("parent"))
		{
			edit.putString("ParentId", ((CreateAccountParentActivity) tab).getParentId());
		}
		
		edit.apply();
	}
	
	private void sendAccountsChangeMsg()
	{
		Log.d(TAG, "Sending Accounts-Change event.");
		Intent accounts = new Intent(AccountsLoader.ACCOUNTSCHANGED);
		Intent home = new Intent(HomeLoader.HOMECHANGED);
		
		// Notify our Activities to update.
		accounts.putExtra("activity", AccountsLoader.ACTIVITY_ACCOUNTS);
		LocalBroadcastManager.getInstance(this).sendBroadcast(accounts);
		LocalBroadcastManager.getInstance(this).sendBroadcast(home);
	}
	// *****************************************************************************************************************************
	// ********************************************** Helper Classes ***************************************************************

	/**
	 * 
	 * @author mwho
	 *
	 */
	private class TabInfo 
	{
		 private String tag;
         private Class<?> clss;
         private Bundle args;
         private Fragment fragment;
         
         TabInfo(String tag, Class<?> clazz, Bundle args) 
         {
        	 this.tag = tag;
        	 this.clss = clazz;
        	 this.args = args;
         }
	}
	
	/**
	 * 
	 * @author mwho
	 *
	 */
	class TabFactory implements TabContentFactory 
	{
		private final Context mContext;

	    /**
	     * @param context
	     */
	    public TabFactory(Context context) 
	    {
	        mContext = context;
	    }

	    /** (non-Javadoc)
	     * @see android.widget.TabHost.TabContentFactory#createTabContent(java.lang.String)
	     */
	    public View createTabContent(String tag) 
	    {
	        View v = new View(mContext);
	        v.setMinimumWidth(0);
	        v.setMinimumHeight(0);
	        return v;
	    }
	}
}
