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
import android.database.SQLException;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.SimpleCursorAdapter;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.TabHost.TabContentFactory;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

public class CreateModifyPayeeActivity extends FragmentActivity implements
												TabHost.OnTabChangeListener,
												PayeeAddressActivity.OnSendPayeeAddressListener,
												TransactionsTabActivity.OnSendTransactionTabDataListener,
												PayeeDefaultAccountActivity.OnUseDefaultCheckedListener,
												PayeeDefaultAccountActivity.OnSendDefaultDataListener,
												PayeeDefaultIncFragment.OnUseDefaultIncomeListener,
												PayeeDefaultExpFragment.OnUseDefaultExpenseListener,
												PayeeMatchingActivity.OnSendMatchingDataListener
{
	private static final String TAG = "CreateModifyPayeeActivity";
	private static final int ACTION_NEW = 1;
	private static final int ACTION_EDIT = 2;
	private int Action = 0;
	private String payeeId = null;
	private String payeeName = null;
	private boolean returnFromDelete = false;
	private boolean isDirty = false;
	private HashMap<String, TabInfo> mapTabInfo = new HashMap<String, CreateModifyPayeeActivity.TabInfo>();
	private TabInfo mLastTab = null;
	Payee payee = null;
	KMMDroidApp KMMDapp;
	SimpleCursorAdapter adapter;
	TabHost tabHost;
	
	/* Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
        setContentView(R.layout.createmod_payee);
        
        // Get the Activity and payee name.
        Bundle extras = getIntent().getExtras();
        Action = extras.getInt("Activity");
        
        // If we are editing then we need to retrieve the payeeId
        if (Action == ACTION_EDIT)
        {
        	payee = new Payee(this, extras.getString("PayeeId"));
        	payeeName = extras.getString("PayeeName");
        }
        else
        	payee = new Payee(this, null);
        
		initialiseTabHost(savedInstanceState);
		if (savedInstanceState != null) 
		{
            tabHost.setCurrentTabByTag(savedInstanceState.getString("tab")); //set the tab as per the saved state
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
				// If we returned anything other than just one record we have issues.
				if ( payee.getId() == null )
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
			
				isDirty = false;
			}
		}
		else
		{
			returnFromDelete = false;
			
			//Mark file as dirty
			KMMDapp.markFileIsDirty(true, "9999");
			
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
	
	// Called when an options item is clicked
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.itemsave:
				// Get our changes.
				payee.getDataChanges();

				if (Action == ACTION_NEW)
				{
					payee.createPayeeId();
					payee.Save();
				}
				else
					payee.Update();
				
				KMMDapp.updateFileInfo("lastModified", 0);
				
				//Mark file as dirty
				KMMDapp.markFileIsDirty(true, "9999");
				
				finish();
				break;
			case R.id.itemDelete:
				AlertDialog.Builder alertDel = new AlertDialog.Builder(this);
				alertDel.setTitle(R.string.delete);
				alertDel.setMessage(getString(R.string.deletemsg) + " " + payeeName + "?");

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
	
	public void onSendPayeeAddressData() 
	{
		// Send the payee address information to the fragment.
		Fragment address = this.getSupportFragmentManager().findFragmentByTag("address");
		((PayeeAddressActivity) address).putPayeeName(payee.getName());
		((PayeeAddressActivity) address).putPayeeAddress(payee.getStreet());
		((PayeeAddressActivity) address).putPayeePostalCode(payee.getZipCode());
		((PayeeAddressActivity) address).putPayeePhone(payee.getTelephone());
		((PayeeAddressActivity) address).putPayeeEmail(payee.getEmail());
		((PayeeAddressActivity) address).putPayeeNotes(payee.getNotes());
	}
	
	public void onSendDefaultData() 
	{
		PayeeDefaultAccountActivity defaultAccount = (PayeeDefaultAccountActivity) this.getSupportFragmentManager()
				.findFragmentByTag("default");
		if(payee.getDefualtAccountId() != null )
		{
			defaultAccount.putUseDefaults(true);
			defaultAccount.putDefaultAccountId(payee.getDefualtAccountId());
		}
		else
			defaultAccount.putUseDefaults(false);
	}
	
	public void onSendTransactionTabData() 
	{
		// Send in our CategoryId for the transactions to be loaded.
		Fragment transactions = this.getSupportFragmentManager().findFragmentByTag("transactions");
		((TransactionsTabActivity) transactions).putPayeeInfo(payee.getId(), payee.getName());		
	}

	public void onUseDefaultChecked(boolean flag) 
	{
		PayeeDefaultAccountActivity defaultAccount = (PayeeDefaultAccountActivity) this.getSupportFragmentManager()
				.findFragmentByTag("default");
		PayeeDefaultIncFragment incFrag = (PayeeDefaultIncFragment) defaultAccount.getChildFragmentManager()
				.findFragmentByTag("incomeFragment");
		PayeeDefaultExpFragment expFrag = (PayeeDefaultExpFragment) defaultAccount.getChildFragmentManager()
				.findFragmentByTag("expenseFragment");

		// If the Income Fragment is checked, then we must not enable the Expense Fragment.
		if( !incFrag.checkboxInc.isChecked() )
		{
			expFrag.checkboxExp.setEnabled(flag);
			expFrag.spinExpense.setEnabled(flag);			
		}
		
		if( !expFrag.checkboxExp.isChecked() )
		{
			incFrag.checkboxInc.setEnabled(flag);
			incFrag.spinIncome.setEnabled(flag);
		}
	}
	
	public void onUseDefaultIncome(boolean flag) 
	{
		// We need to do the opposite of what the user has selected in the Income Fragment
		// to the Expense Fragment.
		PayeeDefaultAccountActivity defaultAccount = (PayeeDefaultAccountActivity) this.getSupportFragmentManager()
				.findFragmentByTag("default");
		PayeeDefaultExpFragment expFrag = (PayeeDefaultExpFragment) defaultAccount.getChildFragmentManager()
				.findFragmentByTag("expenseFragment");
		expFrag.checkboxExp.setEnabled(!flag);
		expFrag.spinExpense.setEnabled(!flag);		
	}
	
	public void onUseDefaultExpense(boolean flag) 
	{
		// We need to do the opposite of what the user has selected in the Expense Fragment
		// to the Income Fragment.
		PayeeDefaultAccountActivity defaultAccount = (PayeeDefaultAccountActivity) this.getSupportFragmentManager()
				.findFragmentByTag("default");
		PayeeDefaultIncFragment incFrag = (PayeeDefaultIncFragment) defaultAccount.getChildFragmentManager()
				.findFragmentByTag("incomeFragment");
		incFrag.checkboxInc.setEnabled(!flag);
		incFrag.spinIncome.setEnabled(!flag);		
	}
	
	public void onSendMatchingData() 
	{
		PayeeMatchingActivity matching = (PayeeMatchingActivity) this.getSupportFragmentManager()
				.findFragmentByTag("matching");
		matching.putMatchingType(payee.getMatchData());
	}
	// ************************************************************************************************
	// ******************************* Helper Functions ***********************************************
	public void setIsDirty(boolean flag)
	{
		this.isDirty = flag;
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
        CreateModifyPayeeActivity.addTab(this, this.tabHost, 
        		this.tabHost.newTabSpec("address").setIndicator(getString(R.string.PayeeTabAddress)),
        		( tabInfo = new TabInfo("address", PayeeAddressActivity.class, args)));
        this.mapTabInfo.put(tabInfo.tag, tabInfo);
        
        CreateModifyPayeeActivity.addTab(this, this.tabHost,
        		this.tabHost.newTabSpec("default").setIndicator(getString(R.string.PayeeTabDefault)),
        		( tabInfo = new TabInfo("default", PayeeDefaultAccountActivity.class, args)));
        this.mapTabInfo.put(tabInfo.tag, tabInfo);
        
        CreateModifyPayeeActivity.addTab(this, this.tabHost,
        		this.tabHost.newTabSpec("matching").setIndicator(getString(R.string.PayeeTabMatching)),
        		( tabInfo = new TabInfo("matching", PayeeMatchingActivity.class, args)));
        this.mapTabInfo.put(tabInfo.tag, tabInfo);
        
        if( Action == ACTION_EDIT )
        {
           	Bundle extras = new Bundle();
            extras.putString("PayeeId", payeeId);
            extras.putString("PayeeName", extras.getString("PayeeName"));
        	
            // Add the fragment for the Transactions
            CreateModifyPayeeActivity.addTab(this, this.tabHost,
            		this.tabHost.newTabSpec("transactions").setIndicator(getString(R.string.TabTransactions)),
            		( tabInfo = new TabInfo("transactions", TransactionsTabActivity.class, extras)));
            this.mapTabInfo.put(tabInfo.tag, tabInfo);
        }
        
        // Default to first tab
        this.onTabChanged("address");
        
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
	private static void addTab(CreateModifyPayeeActivity activity, TabHost tabHost, TabHost.TabSpec tabSpec, TabInfo tabInfo) 
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
		
		if( tag.equalsIgnoreCase("address") )
		{
			edit.putString("Name", ((PayeeAddressActivity) tab).getPayeeName());
			edit.putString("Address", ((PayeeAddressActivity) tab).getPayeeAddress());
			edit.putString("PostalCode", ((PayeeAddressActivity) tab).getPayeePostalCode());
			edit.putString("Telephone", ((PayeeAddressActivity) tab).getPayeePhone());
			edit.putString("Email", ((PayeeAddressActivity) tab).getPayeeEmail());
			edit.putString("Notes", ((PayeeAddressActivity) tab).getPayeeNotes());
		}
		else if( tag.equalsIgnoreCase("default") )
		{
			edit.putInt("UseDefault", ((PayeeDefaultAccountActivity) tab).getUseDefaults() ? 1 : 0);
			edit.putInt("UseIncome", ((PayeeDefaultAccountActivity) tab).getUseIncome() ? 1 : 0);
			edit.putString("IncId", ((PayeeDefaultAccountActivity) tab).getIncomeAccount());
			edit.putInt("UseExpense", ((PayeeDefaultAccountActivity) tab).getUseExpense() ? 1 : 0);
			edit.putString("ExpId", ((PayeeDefaultAccountActivity) tab).getExpenseAccount());
		}
		else if( tag.equalsIgnoreCase("matching") )
		{
			edit.putInt("MatchType", ((PayeeMatchingActivity) tab).getMatchingType());
		}
		
		edit.apply();
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
