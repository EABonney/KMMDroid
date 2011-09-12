package com.vanhlebarsoftware.kmmdroid;

import android.app.TabActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.SimpleCursorAdapter;
import android.widget.TabHost;
import android.widget.TextView;

public class CreateAccountActivity extends TabActivity
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
	private static final int C_TRANSACTIONCOUNT = 15;
	private int Action = 0;
	private String payeeId = null;
	private boolean returnFromDelete = false;
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
        setContentView(R.layout.create_accounts);
  
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
}
