package com.vanhlebarsoftware.kmmdroid;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

public class ReportsActivity extends FragmentActivity 
{
	private final static String TAG = ReportsActivity.class.getSimpleName();
	private String[] mDrawerListItems = {null, null, null, null, null, null};
	private CharSequence mDrawerTitle;
	private CharSequence mTitle;
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;
	ListView listReports;
	KMMDroidApp KMMDapp;
	
	/* Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
        setContentView(R.layout.reports);
        
        // Get our application
        KMMDapp = ((KMMDroidApp) getApplication());
        
        // Set the titles of the Drawer and the ActionBar
        mTitle = mDrawerTitle = getTitle();
        
        // Populate our Drawer items.
        mDrawerListItems[0] = getString(R.string.titleHome);
        mDrawerListItems[1] = getString(R.string.titleAccounts);
        mDrawerListItems[2] = getString(R.string.titleCategories);
        mDrawerListItems[3] = getString(R.string.titleInstitutions);
        mDrawerListItems[4] = getString(R.string.titlePayees);
        mDrawerListItems[5] = getString(R.string.titleSchedules);        
        
        // Find our views
        listReports = (ListView) findViewById(R.id.listReports);
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
        
		// Create the adapter for the navigation DrawerList
		mDrawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list_item, mDrawerListItems));
        
    	// Now hook into listReports ListView and set its onItemClickListener member
    	// to our class handler object.
        listReports.setOnItemClickListener(mMessageClickedHandler);
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
	
	// Message Handler for our listReports List View clicks
	private OnItemClickListener mMessageClickedHandler = new OnItemClickListener() {
	    public void onItemClick(AdapterView<?> parent, View v, int position, long id)
	    {
	    	String selection = (String) parent.getItemAtPosition(position);
	    	
	    	if( selection.equalsIgnoreCase(getString(R.string.titleCashRequirements)) )
	    	{
		    	Intent i = new Intent(getBaseContext(), CashRequirementsOptionsActivity.class);
		    	startActivity(i);
		    	finish();	    		
	    	}
	    	else
	    	{
		    	Toast.makeText(getBaseContext(), "Can't run the selected report!", Toast.LENGTH_SHORT).show();
	    	}
	    }
	};
	
	// Called first time the user clicks on the menu button
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.reports_menu, menu);
		return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu (Menu menu)
	{
		// Hide the sync menu if the user is not logged into at least one of the services.
		if( !KMMDapp.prefs.getBoolean("dropboxSync", false) )
			menu.findItem(R.id.Sync).setVisible(false);
		else
			menu.findItem(R.id.Sync).setVisible(true);
		
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
			case R.id.close:
				KMMDapp.closeDB();
		    	Intent i = new Intent(getBaseContext(), WelcomeActivity.class);
				i.putExtra("Closing", true);
		    	startActivity(i);
				finish();
				break;
			case R.id.itemAbout:
				startActivity(new Intent(this, AboutActivity.class));
				break;
			case R.id.syncDropbox:
				i = new Intent(this, KMMDDropboxService.class);
				i.putExtra("cloudService", KMMDDropboxService.CLOUD_DROPBOX);
				startService(i);
				break;
		}
		
		return true;
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
			startActivity(new Intent(this, CategoriesActivity.class));
    		break;
    	case 3:
			startActivity(new Intent(this, InstitutionsActivity.class));
    		break;
    	case 4:
			startActivity(new Intent(this, PayeeActivity.class));
    		break;
    	case 5:
			startActivity(new Intent(this, SchedulesActivity.class));
    		break;
    	default:
    		Toast.makeText(this, "We have a major fucking error!!!!", Toast.LENGTH_LONG).show();
    		break;		
    	}
    }
}
