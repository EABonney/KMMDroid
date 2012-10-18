package com.vanhlebarsoftware.kmmdroid;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class SchedulesNotificationsActivity extends Activity
{
	private static final String TAG = SchedulesNotificationsActivity.class.getSimpleName();
	private static final String schedulesTable = "kmmSchedules, kmmSplits";
	private static final String[] schedulesColumns = { "kmmSchedules.id AS _id", "kmmSchedules.name AS Description", "occurence", "occurenceString", "occurenceMultiplier",
												"nextPaymentDue", "startDate", "endDate", "lastPayment", "valueFormatted", "autoEnter" };
	private static final String schedulesSelection = "kmmSchedules.id = kmmSplits.transactionId AND nextPaymentDue > 0" + 
												" AND ((occurenceString = 'Once' AND lastPayment IS NULL) OR occurenceString != 'Once')" +
												" AND kmmSplits.splitId = 0 AND kmmSplits.accountId=";
	private static final String schedulesOrderBy = "nextPaymentDue ASC";
	
	String accountUsed = null;
	String dbSelection = null;
	LinearLayout pastDueLayout;
	LinearLayout dueTodayLayout;
	ListView listpastDue;
	ListView listdueToday;
	ListView listautoEntered;
	TextView textPastDue;
	TextView textDueToday;
	TextView textAutoEntered;
	ArrayList<Schedule> Schedules = new ArrayList<Schedule>();
	ArrayList<Schedule> pastDueSchedules = new ArrayList<Schedule>();
	ArrayList<Schedule> dueTodaySchedules = new ArrayList<Schedule>();
	ArrayList<Schedule> autoEnteredSchedules = new ArrayList<Schedule>();
	ArrayList<String> autoEnteredScheduleIds = new ArrayList<String>();
	ArrayList<String> autoTransactionIds = new ArrayList<String>();
	SchedulesAdapter adapterPastDue;
	SchedulesAdapter adapterDueToday;
	SchedulesAdapter adapterAutoEntered;
	KMMDroidApp KMMDapp;
	
	/* Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
        setContentView(R.layout.schedule_notifications);

        // Get our application
        KMMDapp = ((KMMDroidApp) getApplication());
        
        // Find our views
        //pastDueLayout = (LinearLayout) findViewById(R.id.pastDue);
        //dueTodayLayout = (LinearLayout) findViewById(R.id.dueToday);
        listpastDue = (ListView) findViewById(R.id.listPastDueTransactions);
        listdueToday = (ListView) findViewById(R.id.listDueTodayTransactions);
        listautoEntered = (ListView) findViewById(R.id.listAutoEntered);
        textPastDue = (TextView) findViewById(R.id.titlePastDue);
        textDueToday = (TextView) findViewById(R.id.titleDueToday);
        textAutoEntered = (TextView) findViewById(R.id.titleAutoEntered);
        
    	// Now hook into ListViews and set its onItemClickListener member
    	// to our class handler object.
        listpastDue.setOnItemClickListener(mMessageClickedHandler);
        listdueToday.setOnItemClickListener(mMessageClickedHandler);
        listautoEntered.setOnItemClickListener(mMessageClickedHandler);
        
        // See if the database is already open, if not open it Read/Write.
        if(!KMMDapp.isDbOpen())
        {
        	KMMDapp.openDB();
        }
        
        // Get the AccountId
        Bundle extras = getIntent().getExtras();
        accountUsed = extras.getString("accountUsed");
        dbSelection = schedulesSelection + "'" + accountUsed + "'";
        autoEnteredScheduleIds = extras.getStringArrayList("autoEnteredScheduleIds");
        autoTransactionIds = extras.getStringArrayList("newTransactionIds");
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
	}
	
	@Override
	public void onResume()
	{
		super.onResume();
		
		Cursor cursor = KMMDapp.db.query(schedulesTable, schedulesColumns, dbSelection, null, null, null, schedulesOrderBy);
		
		GregorianCalendar calToday = new GregorianCalendar();
		GregorianCalendar calYesterday = new GregorianCalendar();
		calYesterday = (GregorianCalendar) calToday.clone();
		calYesterday.add(Calendar.DAY_OF_MONTH, -1);
		String strToday = String.valueOf(calToday.get(Calendar.YEAR)) + "-" + String.valueOf(calToday.get(Calendar.MONTH)+ 1) + "-"
				+ String.valueOf(calToday.get(Calendar.DAY_OF_MONTH));
		String strYesterday = String.valueOf(calYesterday.get(Calendar.YEAR)) + "-" + String.valueOf(calYesterday.get(Calendar.MONTH)+ 1) + "-"
				+ String.valueOf(calYesterday.get(Calendar.DAY_OF_MONTH));
		
		// Make sure all the ArrayLists are clear.
		Schedules.clear();
		pastDueSchedules.clear();
		dueTodaySchedules.clear();
		autoEnteredSchedules.clear();
		
		// We have our open schedules from the database, now create the user defined period of cash flow.
		Schedules = Schedule.BuildCashRequired(cursor, Schedule.padFormattedDate(strYesterday), Schedule.padFormattedDate(strToday), Transaction.convertToPennies("0.00"));

		// Seperate out the schedules for use in the adapters.
		for(int i=0; i < Schedules.size(); i++)
		{
			if(Schedules.get(i).isPastDue())
				pastDueSchedules.add(Schedules.get(i));
			else if(Schedules.get(i).isDueToday())
				dueTodaySchedules.add(Schedules.get(i));
		}
		
		// Need to get the schedules that where entered based on auto enter preferences.
		Cursor cur = null;
		String selection = dbSelection + " AND id=?";
		for(int i=0; i<autoEnteredScheduleIds.size(); i++)
		{
			cur = KMMDapp.db.query(schedulesTable, schedulesColumns, selection, new String[] { autoEnteredScheduleIds.get(i) }, null, null, null);
			cur.moveToFirst();
			Calendar today = new GregorianCalendar();
			autoEnteredSchedules.add(new Schedule(cur.getString(1), today, cur.getString(9)));
			cur.close();
		}
		
		// Set up the adapters
		adapterPastDue = new SchedulesAdapter(this, R.layout.schedule_notifications_row, pastDueSchedules);
		adapterDueToday = new SchedulesAdapter(this, R.layout.schedule_notifications_row, dueTodaySchedules);
		adapterAutoEntered = new SchedulesAdapter(this, R.layout.schedule_notifications_row, autoEnteredSchedules);
		listpastDue.setAdapter(adapterPastDue);
		listdueToday.setAdapter(adapterDueToday);
		listautoEntered.setAdapter(adapterAutoEntered);

		
		// See if the user only wants to display one of the two types of schedule due.
		if(!KMMDapp.prefs.getBoolean("overdueSchedules", false) || !(pastDueSchedules.size() > 0))
		{
			textPastDue.setVisibility(View.GONE);
			listpastDue.setVisibility(View.GONE);
		}
		if(!KMMDapp.prefs.getBoolean("dueTodaySchedules", false) || !(dueTodaySchedules.size() > 0))
		{
			textDueToday.setVisibility(View.GONE);
			listdueToday.setVisibility(View.GONE);
		}
		
		// If there where no schedules auto entered then hide that listview and textview.
		if(autoEnteredSchedules.size() > 0)
		{
			textAutoEntered.setVisibility(View.VISIBLE);
			listautoEntered.setVisibility(View.VISIBLE);
		}
		
		// Close the cursor to free up memory.
		cursor.close();
	}
	
	// Message Handler for our ListView clicks
	private OnItemClickListener mMessageClickedHandler = new OnItemClickListener() 
	{
	    public void onItemClick(AdapterView<?> parent, View v, int position, long id)
	    {
	    	Log.d(TAG, "OnItemClickListener()");
	    	
	    	switch( parent.getId() )
	    	{
	    	case R.id.listPastDueTransactions:
	    		Intent i = new Intent(getBaseContext(), ScheduleActionsActivity.class);
	    		Schedule sch = pastDueSchedules.get(position);
	    		i.putExtra("scheduleId", sch.getId());	
	    		i.putExtra("scheduleDescription", sch.getDescription());
	    		i.putExtra("Action", 3);
	    		startActivity(i);	    		
	    		break;
	    	case R.id.listDueTodayTransactions:
		    	i = new Intent(getBaseContext(), ScheduleActionsActivity.class);
		    	sch = dueTodaySchedules.get(position);
		    	i.putExtra("scheduleId", sch.getId());	
		    	i.putExtra("scheduleDescription", sch.getDescription());
		    	i.putExtra("Action", 3);
		    	startActivity(i);
		    	break;
	    	case R.id.listAutoEntered:
	    		i = new Intent(getBaseContext(), ViewTransactionActivity.class);
	    		i.putExtra("transactionId", autoTransactionIds.get(position));
	    		startActivity(i);
	    		break;
	    	}
	    }
	};

	// Message Handler for our listTransactions List View clicks
	/*private OnItemClickListener mMessageDueTodayClickedHandler = new OnItemClickListener() 
	{
	    public void onItemClick(AdapterView<?> parent, View v, int position, long id)
	    {
	    	Intent i = new Intent(getBaseContext(), ScheduleActionsActivity.class);
	    	Schedule sch = dueTodaySchedules.get(position);
	    	i.putExtra("scheduleId", sch.getId());	
	    	i.putExtra("scheduleDescription", sch.getDescription());
	    	i.putExtra("Action", 3);
	    	startActivity(i);
	    }
	};*/
	
	private class SchedulesAdapter extends ArrayAdapter<Schedule>
	{
		private ArrayList<Schedule> items;
		private Context context;
		
		public SchedulesAdapter(Context context, int textViewResourceId, ArrayList<Schedule> items)
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
				view = inflater.inflate(R.layout.schedule_notifications_row, null);
			}
			
			Schedule item = items.get(position);
			// Load the items into the view now for this schedule.
			if(item != null)
			{
				TextView DatePaid = (TextView) view.findViewById(R.id.scheduleDate);
				TextView Desc = (TextView) view.findViewById(R.id.scheduleName);
				TextView Amount = (TextView) view.findViewById(R.id.scheduleAmount);
				DatePaid.setText(item.formatDateString());
				Desc.setText(item.getDescription());
				Amount.setText(Transaction.convertToDollars(item.getAmount(), true));
			}
			else
				Log.d(TAG, "Never got a Schedule!");			
			return view;
		}
	}
}
