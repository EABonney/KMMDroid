package com.vanhlebarsoftware.kmmdroid;

import android.content.Context;
import android.content.Intent;
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
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;

public class PayeeActivity extends FragmentActivity implements
									LoaderManager.LoaderCallbacks<Cursor>
{
	private static final String TAG = "PayeeActivity";
	private static final int PAYEES_LOADER = 0x05;
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
        
        // Find our views
        listPayees = (ListView) findViewById(R.id.listPayeesView);
        btnHome = (ImageButton) findViewById(R.id.buttonHome);
        btnAccounts = (ImageButton) findViewById(R.id.buttonAccounts);
        btnCategories = (ImageButton) findViewById(R.id.buttonCategories);
        btnInstitutions = (ImageButton) findViewById(R.id.buttonInstitutions);
        btnPayees = (ImageButton) findViewById(R.id.buttonPayees);
        btnSchedules = (ImageButton) findViewById(R.id.buttonSchedules);
        btnReports = (ImageButton) findViewById(R.id.buttonReports);
        navBar = (LinearLayout) findViewById(R.id.navBar);
        
        // Set out onClickListener events.
        btnHome.setOnClickListener(new View.OnClickListener()
        {
			public void onClick(View arg0)
			{
				startActivity(new Intent(getBaseContext(), HomeActivity.class));
			}
		});
        
        btnAccounts.setOnClickListener(new View.OnClickListener()
        {
			public void onClick(View arg0)
			{
				startActivity(new Intent(getBaseContext(), AccountsActivity.class));
			}
		});
        
        btnCategories.setOnClickListener(new View.OnClickListener()
        {
			public void onClick(View arg0)
			{
				startActivity(new Intent(getBaseContext(), CategoriesActivity.class));
			}
		});
        
        btnInstitutions.setOnClickListener(new View.OnClickListener()
        {
			public void onClick(View arg0)
			{
				startActivity(new Intent(getBaseContext(), InstitutionsActivity.class));
			}
		});
        
        btnPayees.setVisibility(View.GONE);
        
        btnSchedules.setOnClickListener(new View.OnClickListener()
        {
			public void onClick(View arg0)
			{
				startActivity(new Intent(getBaseContext(), SchedulesActivity.class));
			}
		});
        
        btnReports.setOnClickListener(new View.OnClickListener()
        {
			public void onClick(View arg0)
			{
				startActivity(new Intent(getBaseContext(), ReportsActivity.class));
			}
		});
        
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
		
		// See if the user has requested the navigation bar.
		if(!KMMDapp.prefs.getBoolean("navBar", false))
			navBar.setVisibility(View.GONE);
		else
			navBar.setVisibility(View.VISIBLE);
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
		// See if the user wants us to navigation menu items.
		if( !KMMDapp.prefs.getBoolean("navMenu", true))
		{
			menu.findItem(R.id.itemAccounts).setVisible(false);
			menu.findItem(R.id.itemCategories).setVisible(false);
			menu.findItem(R.id.itemHome).setVisible(false);
			menu.findItem(R.id.itemInstitutions).setVisible(false);
			menu.findItem(R.id.itemSchedules).setVisible(false);
			menu.findItem(R.id.itemReports).setVisible(false);
		}
		else
		{
			menu.findItem(R.id.itemAccounts).setVisible(true);
			menu.findItem(R.id.itemCategories).setVisible(true);
			menu.findItem(R.id.itemHome).setVisible(true);
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
			case R.id.itemHome:
				startActivity(new Intent(this, HomeActivity.class));
				break;
			case R.id.itemAccounts:
				startActivity(new Intent(this, AccountsActivity.class));
				break;
			case R.id.itemCategories:
				startActivity(new Intent(this, CategoriesActivity.class));
				break;
			case R.id.itemInstitutions:
				startActivity(new Intent(this, InstitutionsActivity.class));
				break;
			case R.id.itemPrefs:
				startActivity(new Intent(this, PrefsActivity.class));
				break;
			case R.id.itemNew:
				Intent i = new Intent(getBaseContext(), CreateModifyPayeeActivity.class);
				i.putExtra("Activity", ACTION_NEW);
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
}
