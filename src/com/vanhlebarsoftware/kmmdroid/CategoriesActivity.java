package com.vanhlebarsoftware.kmmdroid;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
//import android.widget.AdapterView.OnItemClickListener;
//import android.widget.SimpleCursorAdapter.ViewBinder;
//import android.view.*;

public class CategoriesActivity extends Activity
{
	@SuppressWarnings("unused")
	private static final String TAG = "CategoriesActivity";
	private static final int ACTION_NEW = 1;
	private static final int ACTION_EDIT = 2;
	private static final int C_ACCOUNTNAME = 0;
	private static final int C_BALANCE = 1;
	private static final int C_ID = 2;
	private static final String dbTable = "kmmAccounts";
	private static final String[] dbColumns = { "accountName", "balance", "id AS _id", "parentId" };
	private static final String strSelectionExp = "(parentId='AStd::Expense')"; /* + " OR accountType=" + Account.ACCOUNT_INCOME +
			") AND (balance != '0/1')*/
	private static final String strSelectionInc = "(parentId='AStd::Income')"; /* + " OR accountType=" + Account.ACCOUNT_INCOME +
	") AND (balance != '0/1')*/	
	private static final String strOrderBy = "accountName ASC";
	static final String[] FROM = { "accountName", "balance" };
	static final int[] TO = { R.id.crAccountName, R.id.crAccountBalance };
	private static String strCategoryId = null;
	private static String strCategoryName = null;
	private String accountType = null;
	private String newGroupId = null;
	private String newGroupName = null;
	KMMDroidApp KMMDapp;
	Cursor cursorExp;
	Cursor cursorInc;
	ImageButton btnHome;
	ImageButton btnAccounts;
	ImageButton btnCategories;
	ImageButton btnInstitutions;
	ImageButton btnPayees;
	ImageButton btnSchedules;
	ImageButton btnReports;
	LinearLayout navBar;
	ExpandableListView listCategories;
	TextView tvGroupHeader;
	KMMDExpandableListAdapter adapter;
	
	/* Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
        setContentView(R.layout.categories);
        
        // Get our application
        KMMDapp = ((KMMDroidApp) getApplication());
        
        // Find our views
        listCategories = (ExpandableListView) findViewById(R.id.listCategoriesView);
        btnHome = (ImageButton) findViewById(R.id.buttonHome);
        btnAccounts = (ImageButton) findViewById(R.id.buttonAccounts);
        btnCategories = (ImageButton) findViewById(R.id.buttonCategories);
        btnInstitutions = (ImageButton) findViewById(R.id.buttonInstitutions);
        btnPayees = (ImageButton) findViewById(R.id.buttonPayees);
        btnSchedules = (ImageButton) findViewById(R.id.buttonSchedules);
        btnReports = (ImageButton) findViewById(R.id.buttonReports);
        tvGroupHeader = (TextView) findViewById(R.id.GroupHeading);
        navBar = (LinearLayout) findViewById(R.id.navBar);
		
		registerForContextMenu(listCategories);
		
        // Set out onClickListener events.
        btnHome.setOnClickListener(new View.OnClickListener()
        {
			public void onClick(View arg0)
			{
				startActivity(new Intent(getBaseContext(), HomeActivity.class));
				finish();
			}
		});
        
        btnAccounts.setOnClickListener(new View.OnClickListener()
        {
			public void onClick(View arg0)
			{
				startActivity(new Intent(getBaseContext(), AccountsActivity.class));
				finish();
			}
		});
        
        btnCategories.setVisibility(View.GONE);
        
        btnInstitutions.setOnClickListener(new View.OnClickListener()
        {
			public void onClick(View arg0)
			{
				startActivity(new Intent(getBaseContext(), InstitutionsActivity.class));
				finish();
			}
		});
        
        btnPayees.setOnClickListener(new View.OnClickListener()
        {
			public void onClick(View arg0)
			{
				startActivity(new Intent(getBaseContext(), PayeeActivity.class));
				finish();
			}
		});
        
        btnSchedules.setOnClickListener(new View.OnClickListener()
        {
			public void onClick(View arg0)
			{
				startActivity(new Intent(getBaseContext(), SchedulesActivity.class));
				finish();
			}
		});
        
        btnReports.setOnClickListener(new View.OnClickListener()
        {
			public void onClick(View arg0)
			{
				startActivity(new Intent(getBaseContext(), ReportsActivity.class));
			}
		});

    	// Now hook into listAccounts ListView and set its onItemClickListener member
    	// to our class handler object.
        listCategories.setOnChildClickListener(new OnChildClickListener()
        {
            public boolean onChildClick(ExpandableListView parent, View view, int groupPosition, int childPosition, long id)
            {
            	Account account = (Account) adapter.getChild(groupPosition, childPosition);
            	if( !account.getIsParent() )
            	{
            		strCategoryId = account.getId();
            		strCategoryName = account.getName();
            		Intent i = new Intent(getBaseContext(), CreateModifyCategoriesActivity.class);
            		i.putExtra("Action", ACTION_EDIT);
            		i.putExtra("categoryId", strCategoryId);
            		i.putExtra("categoryName", strCategoryName);
            		startActivity(i);
            	}
            	else
            	{
            		Intent i = new Intent(getBaseContext(), CategoriesActivity.class);
            		i.putExtra("newGroupId", account.getId());
            		i.putExtra("newGroupName", account.getName());
            		startActivity(i);    		
            	}
            	
                return false;
            }
        });
		
		listCategories.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener()
		{
				// Create the context menu for the long click on child items.
				public void onCreateContextMenu(ContextMenu menu, View v,ContextMenuInfo menuInfo)
				{
					//super.onCreateContextMenu(menu, v, menuInfo);
					ExpandableListView.ExpandableListContextMenuInfo info =
						(ExpandableListView.ExpandableListContextMenuInfo) menuInfo;
					int type =
						ExpandableListView.getPackedPositionType(info.packedPosition);
					int group =
						ExpandableListView.getPackedPositionGroup(info.packedPosition);
					int child =
						ExpandableListView.getPackedPositionChild(info.packedPosition);					
					Account account = (Account) adapter.getChild(group, child);
					//Only create a context menu for child items and isParent() is true
					if (type == 1 && account.getIsParent()) 
					{
						menu.add(0, 1, 0, "Edit account");
					}
				}			
		});
		
        // See if the database is already open, if not open it Read/Write.
        if(!KMMDapp.isDbOpen())
        {
        	KMMDapp.openDB();
        }
        
        // See if we are coming from a previous list
        Bundle extras = getIntent().getExtras();
        if( extras != null )
        {
        	newGroupId = extras.getString("newGroupId");
            newGroupName = extras.getString("newGroupName");
        }
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem menuItem)
	{
		ExpandableListContextMenuInfo info =
			(ExpandableListContextMenuInfo) menuItem.getMenuInfo();
		int groupPos = 0, childPos = 0;
		int type = ExpandableListView.getPackedPositionType(info.packedPosition);
		if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD)
		{
			groupPos = ExpandableListView.getPackedPositionGroup(info.packedPosition);
			childPos = ExpandableListView.getPackedPositionChild(info.packedPosition);
		}
		
		Account account = (Account) adapter.getChild(groupPos, childPos);
		switch(menuItem.getItemId())
		{
			case 1:
				strCategoryId = account.getId();
				strCategoryName = account.getName();
				Intent i = new Intent(getBaseContext(), CreateModifyCategoriesActivity.class);
				i.putExtra("Action", ACTION_EDIT);
				i.putExtra("categoryId", strCategoryId);
				i.putExtra("categoryName", strCategoryName);
				startActivity(i);
				return true;
			default:
				Log.d(TAG, "We reached the defualt spot somehow.");
				return false;
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
		
		// Initialize the adapter with a blanck groups and children.
		adapter = new KMMDExpandableListAdapter(this, new ArrayList<String>(), new ArrayList<ArrayList<Account>>(), KMMDapp);
		//adapter.setViewBinder(VIEW_BINDER);
		listCategories.setAdapter(adapter);
		
		//Get all the accounts to be displayed OR just get the sub-accounts for the passed in ParentId.
		if( newGroupId == null )
		{
			cursorExp = KMMDapp.db.query(dbTable, dbColumns, strSelectionExp, null, null, null, strOrderBy);
			cursorInc = KMMDapp.db.query(dbTable, dbColumns, strSelectionInc, null, null, null, strOrderBy);
			
			// Add the items from our expense cursor
			cursorExp.moveToFirst();	
			for(int i=0; i < cursorExp.getCount(); i++)
			{
				Account account = new Account(cursorExp);
				adapter.addItem(getString(R.string.Expense), account);
				cursorExp.moveToNext();
			}
			
			// Add the items from our income cursor
			cursorInc.moveToFirst();
			for(int i=0; i < cursorInc.getCount(); i++)
			{
				Account account = new Account(cursorInc);
				adapter.addItem(getString(R.string.Income), account);
				cursorInc.moveToNext();
			}
		}
		else
		{
			Cursor cur = KMMDapp.db.query(dbTable, dbColumns, "parentId=?", new String[] { newGroupId }, null, null, strOrderBy);
			
			if( cur.getCount() > 0 )
			{
				cur.moveToFirst();
				for(int i=0; i < cur.getCount(); i++)
				{
					Account account = new Account(cur);
					adapter.addItem(newGroupName, account);
					cur.moveToNext();
				}
			}
			listCategories.expandGroup(0);
		}
		
		// See if the user has requested the navigation bar.
		if(!KMMDapp.prefs.getBoolean("navBar", false))
			navBar.setVisibility(View.GONE);
		else
			navBar.setVisibility(View.VISIBLE);
	}
	
	// Called first time the user clicks on the menu button
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.categories_menu, menu);
		return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu (Menu menu)
	{	
		// See if the user wants us to navigation menu items.
		if( !KMMDapp.prefs.getBoolean("navMenu", true))
		{
			menu.findItem(R.id.itemAccounts).setVisible(false);
			menu.findItem(R.id.itemHome).setVisible(false);
			menu.findItem(R.id.itemPayees).setVisible(false);
			menu.findItem(R.id.itemInstitutions).setVisible(false);
			menu.findItem(R.id.itemSchedules).setVisible(false);
			menu.findItem(R.id.itemReports).setVisible(false);
		}
		else
		{
			menu.findItem(R.id.itemAccounts).setVisible(true);
			menu.findItem(R.id.itemHome).setVisible(true);
			menu.findItem(R.id.itemPayees).setVisible(true);
			menu.findItem(R.id.itemInstitutions).setVisible(true);
			menu.findItem(R.id.itemSchedules).setVisible(true);
			menu.findItem(R.id.itemReports).setVisible(true);
		}
		
		return true;
	}
	
	// Called when an options item is clicked
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.itemAccounts:
				startActivity(new Intent(this, AccountsActivity.class));
				break;
			case R.id.itemInstitutions:
				startActivity(new Intent(this, InstitutionsActivity.class));
				break;
			case R.id.itemPayees:
				startActivity(new Intent(this, PayeeActivity.class));
				break;
			case R.id.itemHome:
				startActivity(new Intent(this, HomeActivity.class));
				break;
			case R.id.itemPrefs:
				startActivity(new Intent(this, PrefsActivity.class));
				break;
			case R.id.itemNew:
				Intent i = new Intent(this, CreateModifyCategoriesActivity.class);
				i.putExtra("Action", ACTION_NEW);
				startActivity(i);
				break;
			case R.id.itemSchedules:
				startActivity(new Intent(this, SchedulesActivity.class));
				break;
			case R.id.itemReports:
				startActivity(new Intent(this, ReportsActivity.class));
				break;
			case R.id.itemAbout:
				startActivity(new Intent(this, AboutActivity.class));
				break;
		}
		
		return true;
	}
	
// *********************************************************************************************
// ************************************ Helper Functions ***************************************
	public void putAccountType(String name)
	{
		accountType = name;
	}
	
	public String getAccountType()
	{
		return accountType;
	}
}
