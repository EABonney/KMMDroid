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

public class CreateModifyCategoriesActivity extends TabActivity
{
	private static final String TAG = "CreateModifyCategoriesActivity";
	private static final int ACTION_NEW = 1;
	private static final int ACTION_EDIT = 2;
	private static final int C_ID = 0;
	private static final int C_INSTITUTIONID = 1;
	private static final int C_PARENTID = 2;
	private static final int C_LASTRECONCILED = 3;
	private static final int C_LASTMODIFIED = 4;
	private static final int C_OPENINGDATE = 5;
	private static final int C_ACCOUNTNUMBER = 6;
	private static final int C_ACCOUNTTYPE = 7;
	private static final int C_ACCOUNTTYPESTRING = 8;
	private static final int C_ISSTOCKACCOUNT = 9;
	private static final int C_ACCOUNTNAME = 10;
	private static final int C_DESCRIPTION = 11;
	private static final int C_CURRENCYID = 12;
	private static final int C_BALANCE = 13;
	private static final int C_BALANCEFORMATTED = 14;
	private static final int C_TRANSACTIONCOUNT = 15;
	private static final String dbTable = "kmmAccounts";
	private int Action = 0;
	private static final int AC_EXPENSE = 13;
	private static final int AC_INCOME = 12;
	public static String strType = null;
	private static String strCategoryId = null;
	private static String strCategoryName = null;
	public static boolean inValidateParentId = false;
	private boolean returnFromDelete = false;
	private boolean isDirty = false;
	KMMDroidApp KMMDapp;
	Cursor cursor;
	SimpleCursorAdapter adapter;
	TabHost tabHost;
	
	/* Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
        setContentView(R.layout.createmod_category);
        
        // Get the Activity and payee name.
        Bundle extras = getIntent().getExtras();
        Action = extras.getInt("Action");
        
        // If we are editing then we need to retrieve the payeeId
        if (Action == ACTION_EDIT)
        {
        	strCategoryId = extras.getString("categoryId");
        	strCategoryName = extras.getString("categoryName");
        }
        
        //Resources res = getResources(); // Resource object to get Drawables
        tabHost = getTabHost();  // The activity TabHost
        TabHost.TabSpec spec;  // Resusable TabSpec for each tab
        Intent intent;  // Reusable Intent for each tab

        // Create an Intent to launch an Activity for the tab (to be reused)
        intent = new Intent().setClass(this, CategoriesGeneralActivity.class);

        // Initialize a TabSpec for each tab and add it to the TabHost
        spec = tabHost.newTabSpec("general").setIndicator(getString(R.string.CategoriesTabGeneral))
                      .setContent(intent);
        tabHost.addTab(spec);

        // Do the same for the other tabs
        intent = new Intent().setClass(this, CategoriesHierarchyActivity.class);
        spec = tabHost.newTabSpec("hierarchy").setIndicator(getString(R.string.CategoriesTabHierarchy))
                      .setContent(intent);
        tabHost.addTab(spec);

        if( Action == ACTION_EDIT )
        {
        	intent = new Intent().setClass(this, TransactionsTabActivity.class);
        	intent.putExtra("CategoryName", strCategoryName);
        	intent.putExtra("CategoryId", strCategoryId);
        	spec = tabHost.newTabSpec("payeetransactions").setIndicator(getString(R.string.TabTransactions))
                      .setContent(intent);
        	tabHost.addTab(spec);
        }
        
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
				cursor = KMMDapp.db.query(dbTable, dbColumns, strSelection, new String[] { strCategoryId  }, null, null, null);
				startManagingCursor(cursor);	
				
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
				
				cursor.moveToFirst();
				// We have the correct category Id, so now popluate the forms fields.
				getTabHost().setCurrentTab(0);
				Activity categoryGeneral = this.getCurrentActivity();
				((CategoriesGeneralActivity) categoryGeneral).putCategoryName(cursor.getString(C_ACCOUNTNAME));
				switch(cursor.getInt(C_ACCOUNTTYPE))
				{
					case AC_INCOME:
						((CategoriesGeneralActivity) categoryGeneral).putCategoryType(0);
						break;
					case AC_EXPENSE:
						((CategoriesGeneralActivity) categoryGeneral).putCategoryType(1);
						break;
					default:
						((CategoriesGeneralActivity) categoryGeneral).putCategoryType(0);
						break;
				}
				int pos = 0;
				pos = getCurrencyPos(cursor.getString(C_CURRENCYID));
				((CategoriesGeneralActivity) categoryGeneral).putCurrency(pos);
				((CategoriesGeneralActivity) categoryGeneral).putNotes(cursor.getString(C_DESCRIPTION));
				((CategoriesGeneralActivity) categoryGeneral).putTransactionCount(String.valueOf(cursor.getInt(C_TRANSACTIONCOUNT)));
				
				getTabHost().setCurrentTab(1);
				Activity categoryHierarchy = this.getCurrentActivity();
				((CategoriesHierarchyActivity) categoryHierarchy).putParentAccount(cursor.getString(C_PARENTID));
				Log.d(TAG, "parentId: " + cursor.getString(C_PARENTID));
				// Make sure the 1st tab is displayed to the user.
				getTabHost().setCurrentTab(0);
				this.isDirty = false;
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
				String id, parentId, accountType, accountTypeString, accountName;
				String description, currencyId, balance, balanceFormatted;
				int transactionCount = 0;
				
				// Get the Address elements
				getTabHost().setCurrentTab(0);
				Activity categoryGeneral = this.getCurrentActivity();
				accountName = ((CategoriesGeneralActivity) categoryGeneral).getCategoryName();
				// See if the user has at least filled in the Name field, if not then pop up a dialog and do nothing.
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
				// Else we will save the newly created category
				else
				{
					if(strType.equalsIgnoreCase("Income"))
					{
						accountType = String.valueOf(AC_INCOME);
						accountTypeString = getString(R.string.Income);
					}
					else
					{
						accountType = String.valueOf(AC_EXPENSE);
						accountTypeString = getString(R.string.Expense);
					}
					
					currencyId = ((CategoriesGeneralActivity) categoryGeneral).getCategoryCurrency();
					description = ((CategoriesGeneralActivity) categoryGeneral).getNotes();
					
					// Get the parent Id of this account.
					getTabHost().setCurrentTab(1);
					Activity categoryHierarchy = this.getCurrentActivity();
					parentId = ((CategoriesHierarchyActivity) categoryHierarchy).getParentAccount();
					
					// Create the ContentValue pairs and then insert the new account.
					ContentValues valuesCategory = new ContentValues();
					if(Action == ACTION_NEW)
						id = createAccountId();
					else
						id = strCategoryId;
					
					valuesCategory.put("id", id);
					valuesCategory.put("parentId", parentId);
					valuesCategory.put("accountType", accountType);
					valuesCategory.put("accountTypeString", accountTypeString);
					valuesCategory.put("accountName", accountName);
					valuesCategory.put("description", description);
					valuesCategory.put("currencyId", currencyId);
					
					switch(Action)
					{
						case ACTION_NEW:
							valuesCategory.put("balance", "0/100");
							valuesCategory.put("balanceFormatted", "0.00");
							valuesCategory.put("transactionCount", transactionCount);
							valuesCategory.put("isStockAccount", "N");
							valuesCategory.put("lastReconciled", "");
							valuesCategory.put("lastModified", "");
							valuesCategory.put("openingDate", "");
							valuesCategory.put("accountNumber", "");
							valuesCategory.put("institutionId", "");
							try 
							{
								KMMDapp.db.insertOrThrow(dbTable, null, valuesCategory);
								KMMDapp.updateFileInfo("hiAccountId", 1);
								KMMDapp.updateFileInfo("accounts", 1);
							} 
							catch (SQLException e)
							{
								// TODO Auto-generated catch block
								Log.d(TAG, "error: " + e.getMessage());
							}
							increaseAccountId();
							break;
						case ACTION_EDIT:
						try {
							int effected = 0;
							effected = KMMDapp.db.update(dbTable, valuesCategory, "id=?", new String[] { id });
							Log.d(TAG, "rows effected: " + String.valueOf(effected));
						} catch (Exception e) {
							// TODO Auto-generated catch block
							Log.d(TAG, "Did NOT update categoryId: " + id);
						}
							break;
					}
				}
				KMMDapp.updateFileInfo("lastModified", 0);
				
				//Mark file as dirty
				KMMDapp.markFileIsDirty(true, "9999");
				
				finish();
				break;
			case R.id.itemDelete:
				int rows = KMMDapp.db.delete(dbTable, "id=?", new String[] { strCategoryId });
				KMMDapp.updateFileInfo("accounts", -1);
				if( rows != 1)
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
					KMMDapp.updateFileInfo("lastModified", 0);
					
					//Mark file as dirty
					KMMDapp.markFileIsDirty(true, "9999");
					finish();
				}
				break;
		}
		return true;
	}
	// ************************************************************************************************
	// ******************************* Helper Functions ***********************************************
	private int getCurrencyPos(String id)
	{
		int i = 0;
		
		Cursor c = KMMDapp.db.query("kmmCurrencies", new String[] { "name", "ISOcode" },
									null, null,	null, null, "name ASC");
		startManagingCursor(c);
		c.moveToFirst();
		
		if(c.getCount() > 0)
		{
			while(!id.equals(c.getString(1)))
			{
				c.moveToNext();
				i++;
			}
		}
		else
		{
			Log.d(TAG, "getCurrencyPos query returned no accounts!");
			i = 0;
		}
		
		return i;
	}
	
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
	
	public void setIsDirty(boolean flag)
	{
		this.isDirty = flag;
	}
	
	public boolean getIsDirty()
	{
		return this.isDirty;
	}
}
