package com.vanhlebarsoftware.kmmdroid;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AlphabetIndexer;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SimpleCursorAdapter;

public class PayeeActivity extends FragmentActivity implements
									LoaderManager.LoaderCallbacks<Cursor>
{
	private static final String TAG = "PayeeActivity";
	private static final int PAYEES_LOADER = 0x05;
	private String[] mDrawerListItems = {null, null, null, null, null, null};
	private CharSequence mDrawerTitle;
	private CharSequence mTitle;
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;
	private static final int ACTION_NEW = 1;
	private static final int ACTION_EDIT = 2;
	private static final String[] dbColumns = { "name", "id AS _id"};
	private static final String strOrderBy = "name ASC";
	static final String[] FROM = { "name" };
	static final int[] TO = { R.id.prPayeeName };
	private String selectedPayeeId = null;
	private String selectedPayeeName = null;
	KMMDroidApp KMMDapp;
	Cursor cursor;
	ImageButton btnHome;
	ImageButton btnAccounts;
	ImageButton btnCategories;
	ImageButton btnInstitutions;
	ImageButton btnPayees;
	ImageButton btnSchedules;
	ImageButton btnReports;
	LinearLayout navBar;
	ListView listPayees;
	PayeesAdapter adapterPayees;
	
	/* Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.payee);
        
        // Get our application
        KMMDapp = ((KMMDroidApp) getApplication());
        
        // Set the titles of the Drawer and the ActionBar
        mTitle = mDrawerTitle = getTitle();
        
        // Populate our Drawer items.
        mDrawerListItems[0] = getString(R.string.titleHome);
        mDrawerListItems[1] = getString(R.string.titleAccounts);
        mDrawerListItems[2] = getString(R.string.titleCategories);
        mDrawerListItems[3] = getString(R.string.titleInstitutions);
        mDrawerListItems[4] = getString(R.string.titleSchedules);
        mDrawerListItems[5] = getString(R.string.titleReports);
        
        // Find our views
        listPayees = (ListView) findViewById(R.id.listPayeesView);
        btnHome = (ImageButton) findViewById(R.id.buttonHome);
        btnAccounts = (ImageButton) findViewById(R.id.buttonAccounts);
        btnCategories = (ImageButton) findViewById(R.id.buttonCategories);
        btnInstitutions = (ImageButton) findViewById(R.id.buttonInstitutions);
        btnPayees = (ImageButton) findViewById(R.id.buttonPayees);
        btnSchedules = (ImageButton) findViewById(R.id.buttonSchedules);
        btnReports = (ImageButton) findViewById(R.id.buttonReports);
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
        
        // Set out onClickListener events.
        
		listPayees.setFastScrollEnabled(true);
		
    	// Now hook into listAccounts ListView and set its onItemClickListener member
    	// to our class handler object.
        listPayees.setOnItemClickListener(mMessageClickedHandler);

        // See if the database is already open, if not open it Read/Write.
        if(!KMMDapp.isDbOpen())
        {
        	KMMDapp.openDB();
        }
        
        // Create an empty adapter we will use to display the loaded data.
        adapterPayees = new PayeesAdapter(this,R.layout.payee_row, null, FROM, TO, 0);
		listPayees.setAdapter(adapterPayees);

		// Create the adapter for the navigation DrawerList
		mDrawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list_item, mDrawerListItems));

        // Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
        getSupportLoaderManager().initLoader(PAYEES_LOADER, null, this);
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
		
		// Make sure the edit and delete buttons are not visible and no payee is selected.
		// This is to control the menu items.
		selectedPayeeId = null;
		selectedPayeeName = null;
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
	
	// Message Handler for our listAccounts List View clicks
	private OnItemClickListener mMessageClickedHandler = new OnItemClickListener() {
	    public void onItemClick(AdapterView<?> parent, View v, int position, long id)
	    {
			Cursor c = (Cursor) parent.getAdapter().getItem(position);
			selectedPayeeId = c.getString(c.getColumnIndex("_id"));
			selectedPayeeName = c.getString(c.getColumnIndex("name"));
	    	Intent i = new Intent(getBaseContext(), CreateModifyPayeeActivity.class);
			i.putExtra("Activity", ACTION_EDIT);
			i.putExtra("PayeeId", selectedPayeeId);
			i.putExtra("PayeeName", selectedPayeeName);
			startActivity(i);
	    }
	};
	
	// Called first time the user clicks on the menu button
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.payees_menu, menu);
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
				Intent i = new Intent(getBaseContext(), CreateModifyPayeeActivity.class);
				i.putExtra("Activity", ACTION_NEW);
				startActivity(i);
				break;	
			case R.id.itemAbout:
				startActivity(new Intent(this, AboutActivity.class));
				break;
		}
		return true;
	}
	
	class PayeesAdapter extends SimpleCursorAdapter implements SectionIndexer
	{
		AlphabetIndexer alphaIndexer;
		
		public PayeesAdapter(Context context, int layout, Cursor c,
				String[] from, int[] to, int observer)
		{
			super(context, layout, c, from, to, observer);			
		}


		@Override
		public Cursor swapCursor(Cursor c) 
		{
			if( c != null)
				alphaIndexer = new AlphabetIndexer(c, c.getColumnIndex("name"), " ABCDEFGHIJKLMNOPQRSTUVWXYZ");
			return super.swapCursor(c);
		}


		public View getView(int position, View convertView, ViewGroup parent)
		{
			View view = convertView;
			if(view == null)
			{
				LayoutInflater inflater = (LayoutInflater) getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				view = inflater.inflate(R.layout.payee_row, null);
			}
			else
				view = convertView;
			
			this.mCursor.moveToPosition(position);
			
			// Load the items into the view now for this schedule.
			if(this.mCursor != null)
			{
				TextView row = (TextView) view.findViewById(R.id.prPayeeName);
				row.setText(this.mCursor.getString(this.mCursor.getColumnIndex("name")));
				if( position % 2 == 0)
					row.setBackgroundColor(Color.rgb(0x62, 0xB1, 0xF6));
				else
					row.setBackgroundColor(Color.rgb(0x62, 0xa1, 0xc6));
			}
			else
				Log.d(TAG, "Never got an Payee!");

			return view;
		}
		
		public int getPositionForSection(int section)
		{
			return alphaIndexer.getPositionForSection(section);
		}

		public int getSectionForPosition(int position)
		{
			return alphaIndexer.getSectionForPosition(position);
		}

		public Object[] getSections() 
		{
			return alphaIndexer.getSections();
		}
	}

	public Loader<Cursor> onCreateLoader(int id, Bundle args) 
	{
		String frag = "#9999";
		Uri u = Uri.withAppendedPath(KMMDProvider.CONTENT_PAYEE_URI, frag);
		u = Uri.parse(u.toString());
		setProgressBarIndeterminateVisibility(true);
		return new CursorLoader(PayeeActivity.this, u, dbColumns, null, null, strOrderBy);
	}

	public void onLoadFinished(Loader<Cursor> loader, Cursor payees) 
	{
		adapterPayees.swapCursor(payees);
		setProgressBarIndeterminateVisibility(false);
	}

	public void onLoaderReset(Loader<Cursor> payees) 
	{
		adapterPayees.swapCursor(null);
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
    		startActivity(new Intent(this, AccountsActivity.class));   		
    		break;
    	case 1:
			startActivity(new Intent(this, CategoriesActivity.class));
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
