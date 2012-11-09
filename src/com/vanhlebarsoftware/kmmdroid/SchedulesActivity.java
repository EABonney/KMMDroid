package com.vanhlebarsoftware.kmmdroid;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.SimpleCursorAdapter.ViewBinder;

public class SchedulesActivity extends Activity
{
	private static final String TAG = SchedulesActivity.class.getSimpleName();
	private static final int ACTION_NEW = 1;
	private static final int ACTION_EDIT = 2;
	private static final String dbTable = "kmmSchedules, kmmSplits, kmmPayees";
	private static final String[] dbColumns = { "kmmSchedules.id AS _id", "kmmSchedules.name AS Description", "occurenceString", "nextPaymentDue", 
												"endDate", "lastPayment", "valueFormatted", "kmmPayees.name AS Payee" };
	private static final String strSelection = "kmmSchedules.id = kmmSplits.transactionId AND kmmSplits.payeeId = kmmPayees.id AND nextPaymentDue > 0" + 
												" AND ((occurenceString = 'Once' AND lastPayment IS NULL) OR occurenceString != 'Once')";
	private static final String strOrderBy = "nextPaymentDue ASC";
	static final String[] FROM = { "Description", "occurenceString", "nextPaymentDue", "valueFormatted", "Payee" };
	static final int[] TO = { R.id.srDescription, R.id.srFrequency, R.id.srNextDueDate, R.id.srAmount, R.id.srPayee };
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
	ListView listSchedules;
	ScheduleCursorAdapter adapter;
	
	/* Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
        setContentView(R.layout.schedules);

        // Get our application
        KMMDapp = ((KMMDroidApp) getApplication());
        
        // Find our views
        listSchedules = (ListView) findViewById(R.id.listSchedules);
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
        
        btnCategories.setOnClickListener(new View.OnClickListener()
        {
			public void onClick(View arg0)
			{
				startActivity(new Intent(getBaseContext(), CategoriesActivity.class));
				finish();
			}
		});
        
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
        
        /*btnSchedules.setOnClickListener(new View.OnClickListener()
        {
			public void onClick(View arg0)
			{
				Toast.makeText(getBaseContext(), "Just a holder for now", Toast.LENGTH_SHORT).show();
			}
		});*/
        btnSchedules.setVisibility(View.GONE);
        
        btnReports.setOnClickListener(new View.OnClickListener()
        {
			public void onClick(View arg0)
			{
				startActivity(new Intent(getBaseContext(), ReportsActivity.class));
			}
		});
        
    	// Now hook into listTransactions ListView and set its onItemClickListener member
    	// to our class handler object.
        listSchedules.setOnItemClickListener(mMessageClickedHandler);
        
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
		
        // See if the database is already open, if not open it Read/Write.
        if(!KMMDapp.isDbOpen())
        {
        	KMMDapp.openDB();
        }
        
		//Run the query on the database to get the transactions.
		cursor = KMMDapp.db.query(dbTable, dbColumns, strSelection, null, null, null, strOrderBy, null);
		startManagingCursor(cursor);
		
		// Set up the adapter
		adapter = new ScheduleCursorAdapter(this, R.layout.schedules_rows, cursor, FROM, TO);
		adapter.setViewBinder(VIEW_BINDER);
		listSchedules.setAdapter(adapter);
		
		// See if the user has requested the navigation bar.
		if(!KMMDapp.prefs.getBoolean("navBar", false))
			navBar.setVisibility(View.GONE);
		else
			navBar.setVisibility(View.VISIBLE);
	}
	
	// Message Handler for our listTransactions List View clicks
	private OnItemClickListener mMessageClickedHandler = new OnItemClickListener() {
	    public void onItemClick(AdapterView<?> parent, View v, int position, long id)
	    {
	    	Intent i = new Intent(getBaseContext(), ScheduleActionsActivity.class);
	    	Cursor sch = (Cursor) parent.getAdapter().getItem(position);
	    	i.putExtra("scheduleId", sch.getString(0));	
	    	i.putExtra("scheduleDescription", sch.getString(1));
	    	i.putExtra("Action", 3);
	    	startActivity(i);
	    }
	};
	
	// View binder to do alternating of background colors.
	static final ViewBinder VIEW_BINDER = new ViewBinder() 
	{
		public boolean setViewValue(View view, Cursor cursor, int columnIndex) 
		{
			TextView amount = (TextView) view.findViewById(R.id.srAmount);
			TextView desc = (TextView) view.findViewById(R.id.srDescription);
			TextView payee = (TextView) view.findViewById(R.id.srPayee);
			TextView freq = (TextView) view.findViewById(R.id.srFrequency);
			TextView next = (TextView) view.findViewById(R.id.srNextDueDate);
			Log.d(TAG, "Cursor: " + cursor.getPosition());
			if( cursor.getPosition() % 2 == 0)
			{
				amount.setBackgroundColor(Color.rgb(0x62, 0xB1, 0xF6));
				desc.setBackgroundColor(Color.rgb(0x62, 0xB1, 0xF6));
				payee.setBackgroundColor(Color.rgb(0x62, 0xB1, 0xF6));
				freq.setBackgroundColor(Color.rgb(0x62, 0xB1, 0xF6));
				next.setBackgroundColor(Color.rgb(0x62, 0xB1, 0xF6));
			}
			else
			{
				amount.setBackgroundColor(Color.rgb(0x62, 0xa1, 0xc6));
				desc.setBackgroundColor(Color.rgb(0x62, 0xa1, 0xc6));
				payee.setBackgroundColor(Color.rgb(0x62, 0xa1, 0xc6));
				freq.setBackgroundColor(Color.rgb(0x62, 0xa1, 0xc6));
				next.setBackgroundColor(Color.rgb(0x62, 0xa1, 0xc6));
			}
	
			return true;
		}
	};
	
	// Called first time the user clicks on the menu button
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.schedules_menu, menu);
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
			menu.findItem(R.id.itemPayees).setVisible(false);
			menu.findItem(R.id.itemInstitutions).setVisible(false);
			menu.findItem(R.id.itemHome).setVisible(false);
			menu.findItem(R.id.itemReports).setVisible(false);
		}
		else
		{
			menu.findItem(R.id.itemAccounts).setVisible(true);
			menu.findItem(R.id.itemCategories).setVisible(true);
			menu.findItem(R.id.itemPayees).setVisible(true);
			menu.findItem(R.id.itemInstitutions).setVisible(true);
			menu.findItem(R.id.itemHome).setVisible(true);
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
			case R.id.itemNew:
				Intent i = new Intent(this, CreateModifyScheduleActivity.class);
				i.putExtra("Action", ACTION_NEW);
				startActivity(i);
				break;
			case R.id.itemHome:
				startActivity(new Intent(this, HomeActivity.class));
				break;
			case R.id.itemAccounts:
				startActivity(new Intent(this, AccountsActivity.class));
				break;
			case R.id.itemInstitutions:
				startActivity(new Intent(this, InstitutionsActivity.class));
				break;
			case R.id.itemPayees:
				startActivity(new Intent(this, PayeeActivity.class));
				break;
			case R.id.itemCategories:
				startActivity(new Intent(this, CategoriesActivity.class));
				break;
			case R.id.itemReports:
				startActivity(new Intent(this, ReportsActivity.class));
				break;
			case R.id.itemPrefs:
				startActivity(new Intent(this, PrefsActivity.class));
				break;
			case R.id.itemAbout:
				startActivity(new Intent(this, AboutActivity.class));
				break;
		}
		
		return true;
	}
	
	public class ScheduleCursorAdapter extends SimpleCursorAdapter
	{
		private Cursor c;
		private Context context;
		
		public ScheduleCursorAdapter(Context context, int layout, Cursor c,
				String[] from, int[] to)
		{
			super(context, layout, c, from, to);
			this.c = c;
			this.context = context;
		}
		
		public View getView(int pos, View inView, ViewGroup parent)
		{
			View view = inView;
			TextView txtDesc;
			TextView txtOccurence;
			TextView txtNextPaymentDue;
			TextView txtAmount;
			TextView txtPayee;
			
			if(view == null)
			{
				LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				view = inflater.inflate(R.layout.schedules_rows, null);
			}
			
			this.c.moveToPosition(pos);
			
			// Find our views
			txtDesc = (TextView) view.findViewById(R.id.srDescription);
			txtOccurence = (TextView) view.findViewById(R.id.srFrequency);
			txtNextPaymentDue = (TextView) view.findViewById(R.id.srNextDueDate);
			txtAmount = (TextView) view.findViewById(R.id.srAmount);
			txtPayee = (TextView) view.findViewById(R.id.srPayee);
			
			// load up the current record.
			txtDesc.setText(this.c.getString(1));
			txtOccurence.setText(this.c.getString(2));
			txtNextPaymentDue.setText(this.c.getString(3));
			txtAmount.setText(Transaction.convertToDollars(Transaction.convertToPennies(this.c.getString(6)), true));
			txtPayee.setText(this.c.getString(7));
			
			return view;
		}
	}
}