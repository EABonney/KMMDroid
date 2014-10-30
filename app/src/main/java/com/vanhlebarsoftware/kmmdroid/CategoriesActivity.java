package com.vanhlebarsoftware.kmmdroid;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class CategoriesActivity extends FragmentActivity implements
									LoaderManager.LoaderCallbacks<List<Account>>
{
	private static final String TAG = CategoriesActivity.class.getSimpleName();
	private static final int CATEGORIES_LOADER = 0x03;
	private String[] mDrawerListItems = {null, null, null, null, null, null};
	private CharSequence mDrawerTitle;
	private CharSequence mTitle;
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;
	private static final int ACTION_NEW = 1;
	private static final int ACTION_EDIT = 2;
	private static String strCategoryId = null;
	private static String strCategoryName = null;
	private String newGroupId = null;
	private String newGroupName = null;
	KMMDroidApp KMMDapp;
	ImageButton btnHome;
	ImageButton btnAccounts;
	ImageButton btnCategories;
	ImageButton btnInstitutions;
	ImageButton btnPayees;
	ImageButton btnSchedules;
	ImageButton btnReports;
	LinearLayout navBar;
	ExpandableListView listCategories;
	//TextView tvGroupHeader;
	KMMDExpandableListAdapter adapter;
	
	/* Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.categories);
        
        // Get our application
        KMMDapp = ((KMMDroidApp) getApplication());
        
        // Set the titles of the Drawer and the ActionBar
        mTitle = mDrawerTitle = getTitle();
        
        // Populate our Drawer items.
        mDrawerListItems[0] = getString(R.string.titleHome);
        mDrawerListItems[1] = getString(R.string.titleAccounts);
        mDrawerListItems[2] = getString(R.string.titleInstitutions);
        mDrawerListItems[3] = getString(R.string.titlePayees);
        mDrawerListItems[4] = getString(R.string.titleSchedules);
        mDrawerListItems[5] = getString(R.string.titleReports);
        
        // Find our views
        listCategories = (ExpandableListView) findViewById(R.id.listCategoriesView);
        btnHome = (ImageButton) findViewById(R.id.buttonHome);
        btnAccounts = (ImageButton) findViewById(R.id.buttonAccounts);
        btnCategories = (ImageButton) findViewById(R.id.buttonCategories);
        btnInstitutions = (ImageButton) findViewById(R.id.buttonInstitutions);
        btnPayees = (ImageButton) findViewById(R.id.buttonPayees);
        btnSchedules = (ImageButton) findViewById(R.id.buttonSchedules);
        btnReports = (ImageButton) findViewById(R.id.buttonReports);
        //tvGroupHeader = (TextView) findViewById(R.id.GroupHeading);
        //navBar = (LinearLayout) findViewById(R.id.navBar);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
        		R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close)
        {
        	// Called when a drawer has settled in a completely closed state.
        	public void onDrawerClosed(View view)
        	{
        		super.onDrawerClosed(view);
        		getActionBar().setTitle(mTitle);
        		invalidateOptionsMenu();	// creates call to onPrepareOptionsMenu()
        	}
        	
        	// Called when a drawer has settled in a completely open state.
        	public void onDrawerOpened(View drawerView)
        	{
        		super.onDrawerOpened(drawerView);
        		getActionBar().setTitle(mDrawerTitle);
        		invalidateOptionsMenu();	// creates call to onPrepareOptionsMenu()
        	}
        };
        
        // Set out onClickListener events.
        // Set the Drawer listeners
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        
        // Set the ActionBar items
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
		registerForContextMenu(listCategories);
		
        // Set out onClickListener events.
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

					//Only create a context menu for child items and isParent() is true
					if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) 
					{
						Account account = (Account) adapter.getChild(group, child);
						if(account.getIsParent())
							menu.add(0, 1, 0, "Edit account");
					}
				}			
		});
		
        // See if the database is already open, if not open it Read/Write.
        if(!KMMDapp.isDbOpen())
        {
        	KMMDapp.openDB();
        }

		Bundle bundle = new Bundle();
        // See if we are coming from a previous list
        Bundle extras = getIntent().getExtras();
        if( extras != null )
        {
        	bundle.putBundle("parentAccount", extras);
        	newGroupId = extras.getString("newGroupId");
            newGroupName = extras.getString("newGroupName");
        }
        
        // Create an empty adapter we will use to display the loaded data.
		adapter = new KMMDExpandableListAdapter(this, new ArrayList<String>(), new ArrayList<ArrayList<Account>>());
        listCategories.setAdapter(adapter);

		// Create the adapter for the navigation DrawerList
		mDrawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list_item, mDrawerListItems));

        // Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
        getSupportLoaderManager().initLoader(CATEGORIES_LOADER, bundle, this);
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
	}
	
	@Override
	protected void onPostCreate(Bundle savedInstanceState)
	{
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);
		mDrawerToggle.onConfigurationChanged(newConfig);
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
		return true;
	}
	
	// Called when an options item is clicked
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Pass the event to ActionBarDrawerToggle, if it returns true, then it has handled the app icon touch event.
		//if(mDrawerToggle.onOptionsItemSelected(item))
		//	return true;
		
		switch (item.getItemId())
		{
			case android.R.id.home:
				Intent intent = new Intent(this, HomeActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				break;
			case R.id.itemPrefs:
				startActivity(new Intent(this, PrefsActivity.class));
				break;
			case R.id.itemNew:
				Intent i = new Intent(this, CreateModifyCategoriesActivity.class);
				i.putExtra("Action", ACTION_NEW);
				startActivity(i);
				break;
			case R.id.itemAbout:
				startActivity(new Intent(this, AboutActivity.class));
				break;
		}
		
		return true;
	}
	
	public Loader<List<Account>> onCreateLoader(int id, Bundle args) 
	{
		setProgressBarIndeterminateVisibility(true);
    	return new CategoriesLoader(this, args);
	}

	public void onLoadFinished(Loader<List<Account>> loader, List<Account> accounts) 
	{
        // Set the new data in the adapter.
    	adapter.setData(accounts);	
    	adapter.notifyDataSetChanged();
    	if(newGroupId != null)
    		listCategories.expandGroup(0);
    	setProgressBarIndeterminateVisibility(false);
	}

	public void onLoaderReset(Loader<List<Account>> accounts) 
	{
        // clear the data in the adapter.
    	adapter.setData(null);	
	}
	
    private class DrawerItemClickListener implements ListView.OnItemClickListener
    {
    	public void onItemClick(AdapterView parent, View view, int position, long id)
    	{
    		selectItem(position);
    	}
    }
    
    private void selectItem(int position)
    {
    	// Make sure to close the Navigation Drawer.
		mDrawerLayout.closeDrawer(mDrawerList);
		
    	// need to start new activities in here.
    	switch(position)
    	{
    	case 0:
    		startActivity(new Intent(this, HomeActivity.class));   		
    		break;
    	case 1:
			startActivity(new Intent(this, AccountsActivity.class));
    		break;    		
    	case 2:
			startActivity(new Intent(this, InstitutionsActivity.class));
    		break;
    	case 3:
			startActivity(new Intent(this, PayeeActivity.class));
    		break;
    	case 4:
			startActivity(new Intent(this, SchedulesActivity.class));
    		break;
    	case 5:
			startActivity(new Intent(this, ReportsActivity.class));
    		break;
    	default:
    		Toast.makeText(this, "We have a major fucking error!!!!", Toast.LENGTH_LONG).show();
    		break;		
    	}
    }
}
