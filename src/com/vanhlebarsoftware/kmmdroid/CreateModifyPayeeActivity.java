package com.vanhlebarsoftware.kmmdroid;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TabHost;
import android.widget.TextView;

public class CreateModifyPayeeActivity extends TabActivity
{
	private static final int ACTION_NEW = 1;
	private static final int ACTION_EDIT = 2;
	private static final int ACTION_DELETE = 3;

	private int Action = 0;
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
        setContentView(R.layout.createmod_payee);

        // Get the AccountId
        Bundle extras = getIntent().getExtras();
        Action = extras.getInt("AccountId");
        
        //Resources res = getResources(); // Resource object to get Drawables
        TabHost tabHost = getTabHost();  // The activity TabHost
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
}
