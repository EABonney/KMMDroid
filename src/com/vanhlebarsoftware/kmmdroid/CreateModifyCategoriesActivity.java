package com.vanhlebarsoftware.kmmdroid;

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
import android.net.Uri;
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
import android.widget.SimpleCursorAdapter;
import android.widget.TabHost;
import android.widget.TabHost.TabContentFactory;

public class CreateModifyCategoriesActivity extends FragmentActivity  implements
									TabHost.OnTabChangeListener,
									CategoriesGeneralActivity.OnSendGeneralDataListener,
									CategoriesHierarchyActivity.OnSendHierarchyDataListener,
									TransactionsTabActivity.OnSendTransactionTabDataListener
{
	private static final String TAG = "CreateModifyCategoriesActivity";
	private static final int ACTION_NEW = 1;
	private static final int ACTION_EDIT = 2;
	private static final int C_PARENTID = 2;
	private static final int C_ACCOUNTTYPE = 7;
	private static final int C_ACCOUNTNAME = 10;
	private static final int C_DESCRIPTION = 11;
	private static final int C_CURRENCYID = 12;
	private static final int C_TRANSACTIONCOUNT = 15;
	private static final String dbTable = "kmmAccounts";
	private int Action = 0;
	private static final int AC_EXPENSE = 13;
	private static final int AC_INCOME = 12;
	private int intType = 0;
	private String strParentId = null;
	private static String strCategoryId = null;
	private static String strCategoryName = null;
	private boolean isParentIdValid = false;
	private boolean returnFromDelete = false;
	private boolean isDirty = false;
	KMMDroidApp KMMDapp;
	SimpleCursorAdapter adapter;
	TabHost tabHost;
	private HashMap<String, TabInfo> mapTabInfo = new HashMap<String, CreateModifyCategoriesActivity.TabInfo>();
	private TabInfo mLastTab = null;
	Account category;
	
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
        
		initialiseTabHost(savedInstanceState);
		if (savedInstanceState != null) 
		{
            tabHost.setCurrentTabByTag(savedInstanceState.getString("tab")); //set the tab as per the saved state
        }
        
        //Resources res = getResources(); // Resource object to get Drawables
        /*tabHost = getTabHost();  // The activity TabHost
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
        
        tabHost.setCurrentTab(0);*/
        
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
				String frag = "#9999";
				Uri u = Uri.withAppendedPath(KMMDProvider.CONTENT_ACCOUNT_URI, strCategoryId + frag);
				u = Uri.parse(u.toString());
				final String[] dbColumns = { "*" };
				Cursor c = getBaseContext().getContentResolver().query(u, dbColumns, null, null, null);
				//cursor = KMMDapp.db.query(dbTable, dbColumns, strSelection, new String[] { strCategoryId  }, null, null, null);
				//startManagingCursor(cursor);	
				
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
				
				c.moveToFirst();
				// Get the account the user wants to edit.
				category = new Account(c, this);
				this.setCategoryType(category.getAccountType());
				this.setParentId(category.getParentId());
				
				// We have the correct category Id, so now popluate the forms fields.
				//getTabHost().setCurrentTab(0);
				//Activity categoryGeneral = this.getCurrentActivity();
				//((CategoriesGeneralActivity) categoryGeneral).putCategoryName(category.getName());
				//switch(category.getAccountType())
				//{
				//	case AC_INCOME:
				//		((CategoriesGeneralActivity) categoryGeneral).putCategoryType(0);
				//		break;
				//	case AC_EXPENSE:
				//		((CategoriesGeneralActivity) categoryGeneral).putCategoryType(1);
				//		break;
				//	default:
				//		((CategoriesGeneralActivity) categoryGeneral).putCategoryType(0);
				//		break;
				//}
				//int pos = 0;
				//pos = getCurrencyPos(category.getCurrencyId());
				//((CategoriesGeneralActivity) categoryGeneral).putCurrency(pos);
				//((CategoriesGeneralActivity) categoryGeneral).putNotes(category.getNotes());
				//((CategoriesGeneralActivity) categoryGeneral).putTransactionCount(String.valueOf(category.getTransactionCount()));
				
				//getTabHost().setCurrentTab(1);
				//Activity categoryHierarchy = this.getCurrentActivity();
				//((CategoriesHierarchyActivity) categoryHierarchy).putParentAccount(category.getParentId());
				// Make sure the 1st tab is displayed to the user.
				//getTabHost().setCurrentTab(0);
				this.isDirty = false;
				
				// Clean up our cursors.
				c.close();
			}
			else
				category = new Account();
		}
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
			if(category.getTransactionCount() > 0)
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
				// Update the category to reflect any changes before saving.
				category.getDataChanges(this);
				
				//String id, parentId, accountType, accountTypeString, accountName;
				//String description, currencyId;
				//int transactionCount = 0;
				
				// Get the Address elements
				//getTabHost().setCurrentTab(0);
				//Activity categoryGeneral = this.getCurrentActivity();
				//accountName = ((CategoriesGeneralActivity) categoryGeneral).getCategoryName();
				// See if the user has at least filled in the Name field, if not then pop up a dialog and do nothing.
				if(category.getName().isEmpty())
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
/*					if(strType.equalsIgnoreCase("Income"))
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
*/					
					// Create the ContentValue pairs and then insert the new account.
					//ContentValues valuesCategory = new ContentValues();
					if(Action == ACTION_NEW)
					{
						category.createAccountId(this);
						category.SaveAccount(this);
					}
					else
						category.UpdateAccount(this);
					
					
					
/*					valuesCategory.put("id", id);
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
							Log.d(TAG, "Did NOT update categoryId: " + id);
						}
							break;
					}*/
				}
				KMMDapp.updateFileInfo("lastModified", 0);
				
				//Mark file as dirty
				KMMDapp.markFileIsDirty(true, "9999");
				// Get the loader to refresh.
				sendCategoryChangeMsg();
				
				finish();
				break;
			case R.id.itemDelete:
				int rows = KMMDapp.db.delete(dbTable, "id=?", new String[] { strCategoryId });
				KMMDapp.updateFileInfo("accounts", -1);
				if( rows != 1)
				{
					Log.d(TAG, "There was an error deleting your category!");
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
	
	public void onSendGeneralData() 
	{
		// Send the General information to the fragment.
		Fragment general = this.getSupportFragmentManager().findFragmentByTag("general");
		//Activity categoryGeneral = this.getCurrentActivity();
		((CategoriesGeneralActivity) general).putCategoryName(category.getName());
		((CategoriesGeneralActivity) general).putCategoryType(category.getAccountType());
		((CategoriesGeneralActivity) general).putCurrency(category.getCurrencyId());
		((CategoriesGeneralActivity) general).putNotes(category.getNotes());
		((CategoriesGeneralActivity) general).putTransactionCount(String.valueOf(category.getTransactionCount()));		
	}
	
	public void onSendHierarchyData() 
	{
		// Send the Hierarchy data to the fragment.
		Fragment hierarchy = this.getSupportFragmentManager().findFragmentByTag("hierarchy");
		((CategoriesHierarchyActivity) hierarchy).putParentAccount(category.getParentId());		
	}

	public void onSendTransactionTabData() 
	{
		// Send in our CategoryId for the transactions to be loaded.
		Fragment transactions = this.getSupportFragmentManager().findFragmentByTag("transactions");
		((TransactionsTabActivity) transactions).putCategoryInfo(category.getId(), category.getName());
	}
	// ************************************************************************************************
	// ******************************* Helper Functions ***********************************************
/*	private int getCurrencyPos(String id)
	{
		int i = 0;
		String frag = "#9999";
		Uri u = Uri.withAppendedPath(KMMDProvider.CONTENT_CURRENCY_URI, frag);
		u = Uri.parse(u.toString());
		Cursor c = getBaseContext().getContentResolver().query(u, new String[] { "name", "ISOcode" }, null, null, null);
		//Cursor c = KMMDapp.db.query("kmmCurrencies", new String[] { "name", "ISOcode" },
		//							null, null,	null, null, "name ASC");
		//startManagingCursor(c);	
		if(c.getCount() > 0)
		{
			c.moveToFirst();
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
		
		// Clean up our cursor.
		c.close();
		
		return i;
	}*/
	
/*	private String createAccountId()
	{
		String frag = "#9999";
		Uri u = Uri.withAppendedPath(KMMDProvider.CONTENT_FILEINFO_URI, frag);
		u = Uri.parse(u.toString());
		final String[] dbColumns = { "hiAccountId"};
		final String strOrderBy = "hiAccountId DESC";
		// Run a query to get the Acount ids so we can create a new one.
		Cursor cursor = getBaseContext().getContentResolver().query(u, dbColumns, null, null, strOrderBy);
		//cursor = KMMDapp.db.query("kmmFileInfo", dbColumns, null, null, null, null, strOrderBy);
		//startManagingCursor(cursor);
		
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
		
		// Clean up our cursor.
		cursor.close();
		
		return newId;
	}*/
	
/*	private void increaseAccountId()
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
	}*/
	
	public void setCategoryType(int type)
	{
		this.intType = type;
	}
	
	public void setParentId(String pId)
	{
		this.strParentId = pId;
	}
	
	public void setIsParentInvalid(boolean flag)
	{
		this.isParentIdValid = flag;
	}
	
	public void setIsDirty(boolean flag)
	{
		this.isDirty = flag;
	}
	
	public int getCategoryType()
	{
		return this.intType;
	}
	
	public String getParentId()
	{
		return this.strParentId;
	}
	
	public boolean getIsParentInvalid()
	{
		return this.isParentIdValid;
	}
	
	public boolean getIsDirty()
	{
		return this.isDirty;
	}
	
	/**
	 * Initialise the Tab Host
	 */
	private void initialiseTabHost(Bundle args) 
	{
		tabHost = (TabHost)findViewById(android.R.id.tabhost);
        tabHost.setup();
        TabInfo tabInfo = null;

        // Add the fragment for General
        CreateModifyCategoriesActivity.addTab(this, this.tabHost, 
        		this.tabHost.newTabSpec("general").setIndicator(getString(R.string.CategoriesTabGeneral)),
        		( tabInfo = new TabInfo("general", CategoriesGeneralActivity.class, args)));
        this.mapTabInfo.put(tabInfo.tag, tabInfo);
        
        // Add the fragment for the Hierarchy
        CreateModifyCategoriesActivity.addTab(this, this.tabHost,
        		this.tabHost.newTabSpec("hierarchy").setIndicator(getString(R.string.CategoriesTabHierarchy)),
        		( tabInfo = new TabInfo("hierarchy", CategoriesHierarchyActivity.class, args)));
        this.mapTabInfo.put(tabInfo.tag, tabInfo);
        
        if( Action == ACTION_EDIT )
        {
        	Bundle extras = new Bundle();
        	extras.putString("CategoryName", strCategoryName);
        	extras.putString("CategoryId", strCategoryId);
        	
            // Add the fragment for the Transactions
            CreateModifyCategoriesActivity.addTab(this, this.tabHost,
            		this.tabHost.newTabSpec("transactions").setIndicator(getString(R.string.TabTransactions)),
            		( tabInfo = new TabInfo("transactions", TransactionsTabActivity.class, extras)));
            this.mapTabInfo.put(tabInfo.tag, tabInfo);
        }
        
        // Default to first tab
        this.onTabChanged("general");
        
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
	private static void addTab(CreateModifyCategoriesActivity activity, TabHost tabHost, TabHost.TabSpec tabSpec, TabInfo tabInfo) 
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
		
		if(tag.equalsIgnoreCase("general"))
		{
			edit.putString("Name", ((CategoriesGeneralActivity) tab).getCategoryName());
			edit.putString("Currency", ((CategoriesGeneralActivity) tab).getCurrency());
			edit.putString("Notes", ((CategoriesGeneralActivity) tab).getNotes());
			edit.putBoolean("needUpdateParent", false);
			switch(((CategoriesGeneralActivity) tab).getCategoryType())
			{
				case Account.ACCOUNT_EXPENSE:
					edit.putInt("TypePos", 1);
					break;
				case Account.ACCOUNT_INCOME:
				default:
					edit.putInt("TypePos", 0);
					break;
			}
		}
		else if(tag.equalsIgnoreCase("hierarchy"))
		{
			edit.putString("Parent", ((CategoriesHierarchyActivity) tab).getParentAccount());
		}
		
		edit.apply();
	}
	
	private void sendCategoryChangeMsg()
	{
		Log.d(TAG, "Sending Categories-Change event.");
		Intent category = new Intent(CategoriesLoader.ACCOUNTSCHANGED);
		
		// Notify our Categories to update.
		LocalBroadcastManager.getInstance(this).sendBroadcast(category);
	}
	
	public void ReloadHierarchyLoader() 
	{
		Fragment hierarchy = this.getSupportFragmentManager().findFragmentByTag("hierarchy");
		// if the tab hasn't been instantiated yet, just skip the reload.
		if( hierarchy != null )
			((CategoriesHierarchyActivity) hierarchy).reloadLoader();
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
