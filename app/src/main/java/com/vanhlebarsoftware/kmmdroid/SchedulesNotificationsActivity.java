package com.vanhlebarsoftware.kmmdroid;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;
import android.widget.ExpandableListView.OnChildClickListener;

public class SchedulesNotificationsActivity extends FragmentActivity  implements
											LoaderManager.LoaderCallbacks<List<Schedule>>
{
	private static final String TAG = SchedulesNotificationsActivity.class.getSimpleName();
	private static final int SCHEDULES_NOTIFICATIONS_LOADER = 0x08;
	//private static final String schedulesTable = "kmmSchedules, kmmSplits";
	//private static final String[] schedulesColumns = { "kmmSchedules.id AS _id", "kmmSchedules.name AS Description", "occurence", "occurenceString", "occurenceMultiplier",
	//											"nextPaymentDue", "startDate", "endDate", "lastPayment", "valueFormatted", "autoEnter" };
	//private static final String schedulesSelection = "kmmSchedules.id = kmmSplits.transactionId AND nextPaymentDue > 0" + 
	//											" AND ((occurence = 1 AND lastPayment IS NULL) OR occurence != 1)" +
	//											" AND kmmSplits.splitId = 0";
	//private static final String schedulesOrderBy = "nextPaymentDue ASC";
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
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
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
        //dbSelection = schedulesSelection;// + "'" + accountUsed + "'";
        autoEnteredScheduleIds = extras.getStringArrayList("autoEnteredScheduleIds");
        autoTransactionIds = extras.getStringArrayList("newTransactionIds");
        
        // Add in our preferences for showing dueToday and overDue schedules.
        extras.putBoolean("showOverDue", KMMDapp.prefs.getBoolean("overdueSchedules", false));
        extras.putBoolean("showDueToday", KMMDapp.prefs.getBoolean("dueTodaySchedules", false));
        
		// Set up the adapters
		// Initialize the adapter with a blanck groups and children.
		adapter = new KMMDExpandableListAdapterSchedules(this, new ArrayList<String>(), new ArrayList<ArrayList<Schedule>>(), KMMDapp);
		listScheduledTrans.setAdapter(adapter);
		
        // Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
		getSupportLoaderManager().initLoader(SCHEDULES_NOTIFICATIONS_LOADER, extras, this);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem menuItem)
	{
		ExpandableListContextMenuInfo info =
			(ExpandableListContextMenuInfo) menuItem.getMenuInfo();
		//int type = ExpandableListView.getPackedPositionType(info.packedPosition);
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
						//Schedule schedule = (Schedule) adapter.getChild(groupPos, childPos);
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
		
		//Cursor cursor = KMMDapp.db.query(schedulesTable, schedulesColumns, dbSelection, null, null, null, schedulesOrderBy);
		
		//GregorianCalendar calToday = new GregorianCalendar();
		//GregorianCalendar calYesterday = new GregorianCalendar();
		//calYesterday = (GregorianCalendar) calToday.clone();
		//calYesterday.add(Calendar.DAY_OF_MONTH, -1);
		//String strToday = String.valueOf(calToday.get(Calendar.YEAR)) + "-" + String.valueOf(calToday.get(Calendar.MONTH)+ 1) + "-"
		//		+ String.valueOf(calToday.get(Calendar.DAY_OF_MONTH));
		//String strYesterday = String.valueOf(calYesterday.get(Calendar.YEAR)) + "-" + String.valueOf(calYesterday.get(Calendar.MONTH)+ 1) + "-"
		//		+ String.valueOf(calYesterday.get(Calendar.DAY_OF_MONTH));
		
		// Make sure all the ArrayLists are clear.
		//Schedules.clear();
		//pastDueSchedules.clear();
		//dueTodaySchedules.clear();
		//autoEnteredSchedules.clear();
		
		// We have our open schedules from the database, now create the user defined period of cash flow.
		//Schedules = Schedule.BuildCashRequired(cursor, Schedule.padFormattedDate(strYesterday), Schedule.padFormattedDate(strToday), Transaction.convertToPennies("0.00"));

		// Seperate out the schedules for use in the adapters.
		//for(int i=0; i < Schedules.size(); i++)
		//{
		//	if(Schedules.get(i).isPastDue())
		//		pastDueSchedules.add(Schedules.get(i));
		//	else if(Schedules.get(i).isDueToday())
		//		dueTodaySchedules.add(Schedules.get(i));
		//}
		
		// Need to get the schedules that where entered based on auto enter preferences.
		//Cursor cur = null;
		//String selection = dbSelection + " AND id=?";
		//for(int i=0; i<autoEnteredScheduleIds.size(); i++)
		//{
		//	cur = KMMDapp.db.query(schedulesTable, schedulesColumns, selection, new String[] { autoEnteredScheduleIds.get(i) }, null, null, null);
		//	cur.moveToFirst();
		//	Calendar today = new GregorianCalendar();
		//	autoEnteredSchedules.add(new Schedule(cur.getString(1), today, cur.getString(9)));
		//	cur.close();
		//}
		
		// Set up the adapters
		// Initialize the adapter with a blanck groups and children.
		//adapter = new KMMDExpandableListAdapterSchedules(this, new ArrayList<String>(), new ArrayList<ArrayList<Schedule>>(), KMMDapp);
		//listScheduledTrans.setAdapter(adapter);
		
		// Add the items that are past due.
		//if(KMMDapp.prefs.getBoolean("overdueSchedules", false))
		//{
		//	for(int i=0; i<pastDueSchedules.size(); i++)
		//		adapter.addItem(getString(R.string.titlePastDue), pastDueSchedules.get(i));
		//}
		
		// Add the items that are due today.
		//if(KMMDapp.prefs.getBoolean("dueTodaySchedules", false))
		//{
		//	for(int i=0; i<dueTodaySchedules.size(); i++)
		//		adapter.addItem(getString(R.string.titleDueToday), dueTodaySchedules.get(i));
		//}
		
		// Add the items that where auto entered.
		//for(int i=0; i<autoEnteredSchedules.size(); i++)
		//	adapter.addItem(getString(R.string.titleAutoEntered), autoEnteredSchedules.get(i));
		
		// Close the cursor to free up memory.
		//cursor.close();
	}

	public Loader<List<Schedule>> onCreateLoader(int id, Bundle args) 
	{
		setProgressBarIndeterminateVisibility(true);
		return new SchedulesLoader(this, args);
	}

	public void onLoadFinished(Loader<List<Schedule>> loader, List<Schedule> schedules) 
	{
		adapter.setData(schedules);
    	adapter.notifyDataSetChanged();
		setProgressBarIndeterminateVisibility(false);
	}

	public void onLoaderReset(Loader<List<Schedule>> loader) 
	{
		adapter.setData(null);
	}

	// **************************************************************************************************
	// ************************************ Helper methods **********************************************	
}
