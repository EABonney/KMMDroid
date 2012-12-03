package com.vanhlebarsoftware.kmmdroid;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;
import android.widget.ExpandableListView.OnChildClickListener;

public class SchedulesNotificationsActivity extends Activity
{
	private static final String TAG = SchedulesNotificationsActivity.class.getSimpleName();
	private static final String schedulesTable = "kmmSchedules, kmmSplits";
	private static final String[] schedulesColumns = { "kmmSchedules.id AS _id", "kmmSchedules.name AS Description", "occurence", "occurenceString", "occurenceMultiplier",
												"nextPaymentDue", "startDate", "endDate", "lastPayment", "valueFormatted", "autoEnter" };
	private static final String schedulesSelection = "kmmSchedules.id = kmmSplits.transactionId AND nextPaymentDue > 0" + 
												" AND ((occurence = 1 AND lastPayment IS NULL) OR occurence != 1)" +
												" AND kmmSplits.splitId = 0";
	private static final String schedulesOrderBy = "nextPaymentDue ASC";
	private static final int ENTER = 1001;
	private static final int SKIP = 1003;
	private static final int ACTION_ENTERSCHEDULE = 3;
	String accountUsed = null;
	String dbSelection = null;
	LinearLayout pastDueLayout;
	LinearLayout dueTodayLayout;
	ExpandableListView listScheduledTrans;
	TextView textPastDue;
	TextView textDueToday;
	TextView textAutoEntered;
	ArrayList<Schedule> Schedules = new ArrayList<Schedule>();
	ArrayList<Schedule> pastDueSchedules = new ArrayList<Schedule>();
	ArrayList<Schedule> dueTodaySchedules = new ArrayList<Schedule>();
	ArrayList<Schedule> autoEnteredSchedules = new ArrayList<Schedule>();
	ArrayList<String> autoEnteredScheduleIds = new ArrayList<String>();
	ArrayList<String> autoTransactionIds = new ArrayList<String>();
	ArrayList<Split> Splits;
	KMMDExpandableListAdapterSchedules adapter;
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
        listScheduledTrans = (ExpandableListView) findViewById(R.id.listTransactions);     
        
        // Get out context menu
        registerForContextMenu(listScheduledTrans);
        
        listScheduledTrans.setOnChildClickListener(new OnChildClickListener()
        {
            public boolean onChildClick(ExpandableListView parent, View view, int groupPosition, int childPosition, long id)
            {
            	Schedule schedule = (Schedule) adapter.getChild(groupPosition, childPosition);
				Log.d(TAG, "Descripition: " + schedule.getDescription());
				Log.d(TAG, "groupPos: " + groupPosition + " childPos: " + childPosition);         	
                return false;
            }
        });
        
        // Set up our context menu for entering, skipping, editing or deleting the schedule or transaction.
        listScheduledTrans.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener()
		{
			// Create the context menu for the long click on child items.
			public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
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
					menu.add(0, ENTER, 0, getString(R.string.buttonEnter));
					menu.add(0, SKIP, 0, getString(R.string.buttonSkip));
				}
			}			
		});
        
        // See if the database is already open, if not open it Read/Write.
        if(!KMMDapp.isDbOpen())
        {
        	KMMDapp.openDB();
        }
        
        // Get the AccountId
        Bundle extras = getIntent().getExtras();
        accountUsed = extras.getString("accountUsed");
        dbSelection = schedulesSelection;// + "'" + accountUsed + "'";
        autoEnteredScheduleIds = extras.getStringArrayList("autoEnteredScheduleIds");
        autoTransactionIds = extras.getStringArrayList("newTransactionIds");
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem menuItem)
	{
		ExpandableListContextMenuInfo info =
			(ExpandableListContextMenuInfo) menuItem.getMenuInfo();
		//int groupPos = 0, childPos = 0;
		int type = ExpandableListView.getPackedPositionType(info.packedPosition);
		final int groupPos = ExpandableListView.getPackedPositionGroup(info.packedPosition);
		final int childPos = ExpandableListView.getPackedPositionChild(info.packedPosition);
		
		Schedule schedule = (Schedule) adapter.getChild(groupPos, childPos);
		final String schedId = schedule.getId();
		switch(menuItem.getItemId())
		{
			case ENTER:
				Intent i = new Intent(getBaseContext(), CreateModifyTransactionActivity.class);
				i.putExtra("scheduleId", schedId);
				i.putExtra("Action", ACTION_ENTERSCHEDULE);
				i.putExtra("widgetDatabasePath", KMMDapp.getFullPath());
				i.putExtra("fromScheduleActions", true);
				startActivity(i);
				return true;
			case SKIP:
				AlertDialog.Builder alertSkip = new AlertDialog.Builder(this);
				alertSkip.setTitle(R.string.skip);
				alertSkip.setMessage(getString(R.string.titleSkipSchedule));

				alertSkip.setPositiveButton(getString(R.string.titleButtonYes), new DialogInterface.OnClickListener() 
				{
					public void onClick(DialogInterface dialog, int whichButton)
					{					
						Intent intent = new Intent(getBaseContext(), KMMDService.class);
						intent.putExtra("skipScheduleId", schedId);
						intent.putExtra("widgetId", "9999");
						startService(intent);
						Schedule schedule = (Schedule) adapter.getChild(groupPos, childPos);
						Log.d(TAG, "Descripition: " + schedule.getDescription());
						Log.d(TAG, "groupPos: " + groupPos + " childPos: " + childPos);
						adapter.removeItem(groupPos, childPos);
						adapter.notifyDataSetChanged();
					}
				});
				alertSkip.setNegativeButton(getString(R.string.titleButtonNo), new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int whichButton) 
					{
						// Canceled.
					}
				});				
				alertSkip.show();
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
		// Initialize the adapter with a blanck groups and children.
		adapter = new KMMDExpandableListAdapterSchedules(this, new ArrayList<String>(), new ArrayList<ArrayList<Schedule>>(), KMMDapp);
		listScheduledTrans.setAdapter(adapter);
		
		// Add the items that are past due.
		if(KMMDapp.prefs.getBoolean("overdueSchedules", false))
		{
			for(int i=0; i<pastDueSchedules.size(); i++)
				adapter.addItem(getString(R.string.titlePastDue), pastDueSchedules.get(i));
		}
		
		// Add the items that are due today.
		if(KMMDapp.prefs.getBoolean("dueTodaySchedules", false))
		{
			for(int i=0; i<dueTodaySchedules.size(); i++)
				adapter.addItem(getString(R.string.titleDueToday), dueTodaySchedules.get(i));
		}
		
		// Add the items that where auto entered.
		for(int i=0; i<autoEnteredSchedules.size(); i++)
			adapter.addItem(getString(R.string.titleAutoEntered), autoEnteredSchedules.get(i));
		
		// Close the cursor to free up memory.
		cursor.close();
	}

	// **************************************************************************************************
	// ************************************ Helper methods **********************************************	
	/*private ArrayList<Split> getSplits(String transId)
	{
		ArrayList<Split> splits = new ArrayList<Split>();
		
		Cursor cursor = KMMDapp.db.query("kmmSplits", new String[] { "*" }, "transactionId=?", new String[] { transId }, null, null, "splitId ASC");
		startManagingCursor(cursor);
		cursor.moveToFirst();
		
		// put all the splits information into the ArrayList and then return that as a single object
		while( !cursor.isAfterLast() )
		{
			splits.add(new Split(cursor.getString(C_TRANSACTIONID), cursor.getString(C_TXTYPE),
								 cursor.getInt(C_SPLITID), cursor.getString(C_PAYEEID),
								 cursor.getString(C_RECONCILEDATE), cursor.getString(C_ACTION),
								 cursor.getString(C_RECONCILEFLAG), cursor.getString(C_VALUE),
								 cursor.getString(C_VALUEFORMATTED), cursor.getString(C_SHARES),
								 cursor.getString(C_SHARESFORMATTED), cursor.getString(C_PRICE),
								 cursor.getString(C_PRICEFORMATTED), cursor.getString(C_MEMO),
								 cursor.getString(C_ACCOUNTID), cursor.getString(C_CHECKNUMBER),
								 cursor.getString(C_POSTDATE), cursor.getString(C_BANKID)) );
			cursor.moveToNext();
		}
		
		cursor.close();
		return splits;
	}*/
}
