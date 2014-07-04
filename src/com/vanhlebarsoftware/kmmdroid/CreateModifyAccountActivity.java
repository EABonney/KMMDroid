package com.vanhlebarsoftware.kmmdroid;

import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
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
	private int Action = 0;
	private String accountId = null;
	private String parentAccountSelection = null;
	private boolean returnFromDelete = false;
	private boolean isDirty = false;
	private boolean firstRun = true;
	private boolean bAccountDirty = false;
	private boolean bInstitutionDirty = false;
	private boolean bParentDirty = false;
	KMMDroidApp KMMDapp;
	TextView payeeName;
	//TextView title;
	TabHost tabHost;
	
	private HashMap<String, TabInfo> mapTabInfo = new HashMap<String, CreateModifyAccountActivity.TabInfo>();
	private TabInfo mLastTab = null;
	boolean useInst = false;	
	Account account;
	
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
        
        // If we are editing we need to pull the account in now then, or create a new empty account.
        if (Action == ACTION_EDIT)
        {
        	accountId = extras.getString("AccountId");
        	account = Account.getAccount(this, accountId);
        	account.logAccount();
        	//find our view and update it for the correct title.
        	//title = (TextView) findViewById(R.id.titleCreateModAccount);
        	//title.setText(R.string.titleEditModAccount);
        }
        else
        	account = new Account(this);
        
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
				// Check to see if we are editing one of the currently supported account types. If not tell the user and then return to the
				// account lists.
				if(account.getAccountType() == Account.ACCOUNT_CREDITCARD || 
						account.getAccountType() == Account.ACCOUNT_LOAN ||
						account.getAccountType() == Account.ACCOUNT_INVESTMENT )
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

				if( account.getInstitutionId() != null)
					useInst = true;
				else
					useInst = false;
			}
			else
				account = new Account(this.getBaseContext());
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

            // Mark the tab we are moving to as dirty.
            if(tag.equalsIgnoreCase("institution"))
            	this.bInstitutionDirty = true;
            if(tag.equalsIgnoreCase("account"))
            	this.bAccountDirty = true;
            if(tag.equalsIgnoreCase("parent"))
            	this.bParentDirty = true;
            
            Log.d(TAG, "Changing tabs now...to tab: " + tag);
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
			case R.id.itemCancel:
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
				break;
			case R.id.itemsave:	
				//this.onTabChanged("account");
				Log.d(TAG, "accountTypeString: " + account.getAccountTypeString());
				// Update the account object to reflect any changes before saving.
				Fragment fragAcct = getSupportFragmentManager().findFragmentByTag("account");
				Fragment fragInst = getSupportFragmentManager().findFragmentByTag("institution");
				Fragment fragParent = getSupportFragmentManager().findFragmentByTag("parent");
				
				Bundle bdlAcct = null;
				Bundle bdlInst = null;
				Bundle bdlParent = null;
				
				if(bAccountDirty)
					bdlAcct = ((CreateAccountAccountActivity) fragAcct).getAccountBundle();
				if(bInstitutionDirty)
					bdlInst = ((CreateAccountInstitutionActivity) fragInst).getInstitutionBunde();
				if(bParentDirty)
					bdlParent = ((CreateAccountParentActivity) fragParent).getParentBundle();
				
				fillAccountData(bdlAcct, bdlInst, bdlParent);
				//account.getDataChanges(this);

				if(account.getName().isEmpty())
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
					if(Action == ACTION_NEW)
						account.createAccountId(this);
					
					switch(Action)
					{
						case ACTION_NEW:
							account.logAccount();
							account.SaveAccount(this);
							break;
						case ACTION_EDIT:
							account.logAccount();
							account.UpdateAccount(this);
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
		((CreateAccountInstitutionActivity) Inst).putAccountNumber(account.getAccountNumber());
		((CreateAccountInstitutionActivity) Inst).putIBAN(account.getIBAN());
		((CreateAccountInstitutionActivity) Inst).putInstitutionId(account.getInstitutionId());
		((CreateAccountInstitutionActivity) Inst).putUseInstitution(useInst);
	}
	
	public void onSendParentData() 
	{
		// Send the Parent data back to the fragment.
		Fragment Parent = this.getSupportFragmentManager().findFragmentByTag("parent");	
		if(account.getParentId() != null)
			((CreateAccountParentActivity) Parent).putParentId(account.getParentId());
	}

	public void onSendAccountData() 
	{
		// Send the account data back to the fragment.
		Fragment Account = this.getSupportFragmentManager().findFragmentByTag("account");
		((CreateAccountAccountActivity) Account).putAccountName(account.getName());
		((CreateAccountAccountActivity) Account).putAccountType(account.getAccountType());
		((CreateAccountAccountActivity) Account).putAccountTypeString(account.getAccountTypeString());
		((CreateAccountAccountActivity) Account).putCurrency(account.getCurrencyId());
		((CreateAccountAccountActivity) Account).putOpeningDate(account.getOpenDate());
		((CreateAccountAccountActivity) Account).putOpeningBalance(account.getBalance());
		((CreateAccountAccountActivity) Account).putTransactionCount(String.valueOf(account.getTransactionCount()));
		((CreateAccountAccountActivity) Account).putPreferredAccount(account.getIsPreferred());
	}
	// *****************************************************************************************************************************
	// ********************************************** Helper Functions *************************************************************	
	public void setIsDirty(boolean flag)
	{
		this.isDirty = flag;
	}
	
	public void setIsInstitutionDirty(boolean flag)
	{
		this.bInstitutionDirty = flag;
	}
	
	public void setIsAccountDirty(boolean flag)
	{
		this.bAccountDirty = flag;
	}
	
	public void setIsParentDirty(boolean flag)
	{
		this.bParentDirty = flag;
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
			edit.putString("AccountTypeString", ((CreateAccountAccountActivity) tab).getAccountTypeString());
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
	
	private void fillAccountData(Bundle bdlAccount, Bundle bdlInstitution, Bundle bdlParent)
	{
		// Populate the account information from the supplied bundle
		if(bdlAccount != null)
		{
			account.setIsPreferred(bdlAccount.getBoolean("preferred"));
			account.setAccountName(bdlAccount.getString("name"));
			account.setOpenDate(bdlAccount.getString("openDate"));
			account.setOpenBalance(bdlAccount.getString("openBalance"));
			account.setCurrency(bdlAccount.getString("currencySelected"));
			account.setAccountType(bdlAccount.getInt("intTypeSelected"));
			account.setAccountType(bdlAccount.getString("accountTypeString"));
		}
		
		// Populate the Institution information from the supplied bundle
		if(bdlInstitution != null)
		{
			account.setInstitutionId(bdlInstitution.getString("institutionId"));
			account.setIBAN(bdlInstitution.getString("strIBAN"));
			account.setAccountNumber(bdlInstitution.getString("strAccountNumber"));
		}
		
		// Populate the Parent information from the supplied bundle
		if(bdlParent != null)
			account.setParentId(bdlParent.getString("parentId"));
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
