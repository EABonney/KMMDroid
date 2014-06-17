package com.vanhlebarsoftware.kmmdroid;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class CashRequirementsActivity extends FragmentActivity
{
	public static final String TAG = "CashRequirementsActivity";
	private static final String dbTable = "kmmSchedules, kmmSplits";
	private static final String[] dbColumns = { "kmmSchedules.id AS _id", "kmmSchedules.name AS Description", "occurence", "occurenceString", "occurenceMultiplier",
												"nextPaymentDue", "startDate", "endDate", "lastPayment", "valueFormatted", "autoEnter" };
	private static final String strSelection = "kmmSchedules.id = kmmSplits.transactionId AND nextPaymentDue > 0" + 
												" AND ((occurenceString = 'Once' AND lastPayment IS NULL) OR occurenceString != 'Once')" +
												" AND kmmSplits.splitId = 0 AND kmmSplits.accountId=?";
	private static final String strOrderBy = "nextPaymentDue ASC";
	private String strStartDate = null;
	private String strEndDate = null;
	private String strAccountId = null;
	private long nBegBalance = 0;
	boolean bChangeBackground = false;
	ArrayList<Schedule> Schedules;
	KMMDroidApp KMMDapp;
	Cursor cursor;
	ListView listSchedules;
	TextView noSchedules;
	ScheduleAdapter adapter;
	
	/* Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
        setContentView(R.layout.cashrequirements);

        // Get our application
        KMMDapp = ((KMMDroidApp) getApplication());
        
        // Find our views
        listSchedules = (ListView) findViewById(R.id.listCashRequirements);
        noSchedules = (TextView) findViewById(R.id.titleNoSchedules);
        
        // Get the action the user is doing.
        Bundle extras = getIntent().getExtras();
        strAccountId = extras.getString("Account");
        nBegBalance = extras.getLong("AccountBalance");
        strStartDate = extras.getString("BegDate");
        strEndDate = extras.getString("EndDate");
        
        // Make sure our dates are in YYYY-MM-DD format
        strStartDate = formatDate(strStartDate);
        strEndDate = formatDate(strEndDate);
        
        // See if the database is already open, if not open it Read/Write.
        if(!KMMDapp.isDbOpen())
        {
        	KMMDapp.openDB();
        }
        
        Schedules = new ArrayList<Schedule>();
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
		
		//Run the query on the database to get the transactions.
		cursor = KMMDapp.db.query(dbTable, dbColumns, strSelection, new String[] { strAccountId }, null, null, strOrderBy, null);
		startManagingCursor(cursor);
		
		Schedules = Schedule.BuildCashRequired(cursor, strStartDate, strEndDate, nBegBalance, getBaseContext(), "9999");
		
		if(Schedules.size() > 0)
		{
			listSchedules.setVisibility(View.VISIBLE);
			noSchedules.setVisibility(View.GONE);
			// Set up the adapter
			adapter = new ScheduleAdapter(this, R.layout.cashrequirements_row, Schedules);
			listSchedules.setAdapter(adapter);
		}
		else
		{
			listSchedules.setVisibility(View.GONE);
			noSchedules.setVisibility(View.VISIBLE);
			Log.d(TAG, "We didn't get any schedules!");
		}
	}
	
	// Called first time the user clicks on the menu button
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.schedules_menu, menu);
		return true;
	}
	
	// Called when an options item is clicked
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.itemPrefs:
				startActivity(new Intent(this, PrefsActivity.class));
				break;
			case R.id.itemAbout:
				startActivity(new Intent(this, AboutFragment.class));
				break;
		}
		
		return true;
	}
	
	// **************************************************************************************************
	// ************************************ Helper methods **********************************************
		
	private String formatDate(String date)
	{
		// We need to reverse the order of the date to be YYYY-MM-DD for SQL
		String dates[] = date.split("-");
		
		return new StringBuilder()
		.append(dates[2]).append("-")
		.append(dates[0]).append("-")
		.append(dates[1]).toString();
	}

	private class ScheduleAdapter extends ArrayAdapter<Schedule>
	{
		private ArrayList<Schedule> items;
		private Context context;
		
		public ScheduleAdapter(Context context, int textViewResourceId, ArrayList<Schedule> items)
		{
			super(context, textViewResourceId, items);
			this.context = context;
			this.items = items;
		}
		
		public View getView(int position, View convertView, ViewGroup parent)
		{
			View view = convertView;
			if(view == null)
			{
				LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				view = inflater.inflate(R.layout.cashrequirements_row, null);
			}
			
			Schedule item = items.get(position);
			// Load the items into the view now for this schedule.
			if(item != null)
			{
				TextView DueDate = (TextView) view.findViewById(R.id.crDueDate);
				TextView Description = (TextView) view.findViewById(R.id.crDescription);
				TextView Amount = (TextView) view.findViewById(R.id.crAmount);
				TextView Balance = (TextView) view.findViewById(R.id.crBalance);
				LinearLayout row = (LinearLayout) view.findViewById(R.id.cashReqRow);
				
				if(bChangeBackground)
				{
					row.setBackgroundColor(Color.rgb(0x62, 0xB1, 0xF6));
					bChangeBackground = false;
				}
				else
				{
					row.setBackgroundColor(Color.rgb(0x62, 0xa1, 0xc6));
					bChangeBackground = true;
				}
				
				DueDate.setText(item.formatDateString());
				Description.setText(item.getDescription());
				Log.d(TAG, item.getDescription() + ": " + String.valueOf(item.getAmount()));
				Amount.setText(item.convertToDollars(item.getAmount()));
				Balance.setText(item.convertToDollars(item.getBalance()));
			}
			else
				Log.d(TAG, "Never got a Schedule!");
			
			return view;
		}
	}
}
