package com.vanhlebarsoftware.kmmdroid;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

public class WelcomeActivity extends FragmentActivity
{
	private static final String TAG = "WelcomeActivity";
	/*********************************************************************************************************************
	 * Parameters used for querying the schedules table
	 ********************************************************************************************************************/
	private static final String schedulesTable = "kmmSchedules, kmmSplits";
	private static final String[] schedulesColumns = { "kmmSchedules.id AS _id", "kmmSchedules.name AS Description", "occurence", "occurenceString", "occurenceMultiplier",
												"nextPaymentDue", "startDate", "endDate", "lastPayment", "valueFormatted", "autoEnter" };
	private static final String schedulesSelection = "kmmSchedules.id = kmmSplits.transactionId AND nextPaymentDue > 0" + 
												" AND ((occurence = 1 AND lastPayment IS NULL) OR occurence != 1)" +
												" AND kmmSplits.splitId = 0 AND kmmSplits.accountId=";
	private static final String schedulesOrderBy = "nextPaymentDue ASC";
	boolean closedDB = false;
	Context context;
	KMMDroidApp KMMDapp;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        // Get our application
        KMMDapp = ((KMMDroidApp) getApplication());
        
        Bundle extras = getIntent().getExtras();
        String lostPath = null;
        String fromWidgetId = null;
        boolean Closing = false;
        
        if( extras != null)
        {
        	if(!extras.isEmpty())
        	{
        		Closing = extras.getBoolean("Closing");
        		lostPath = extras.getString("lostPath");
        		fromWidgetId = extras.getString("fromWidgetId");
        	}
        }
        else
        {
        	fromWidgetId = "9999";
        	Log.d(TAG, "No extras passed to WelcomeActivity!");
        }
        
        // If the user has lostPath, somehow they lost their saved database, just pop and alert, show them the lost path and then skip
        // everything else until they open up a new database.
        if( lostPath != null )
        {
        	String msg = "Sorry but somehow you have lost the database that you had opened. Please open another one to continue.";
        	msg = msg + "\n" + "Missing database was at: " + lostPath;
			AlertDialog.Builder alertDel = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogNoTitle));
			alertDel.setTitle(R.string.lostDatabase);
			alertDel.setMessage(msg);

			alertDel.setPositiveButton(getString(R.string.titleButtonOK), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {					
				}
			});				
			alertDel.show();        	
        }
        else
        {
        	// See if the user has a home widget in use and a preferance set for the update interval of home widgets, if so set it.
        	if(KMMDapp.prefs.getBoolean("homeWidgetSetup", false))
        	{
        		if(!KMMDapp.getAutoUpdate())
        		{
        			String value = KMMDapp.prefs.getString("updateFrequency", "0");
        			KMMDapp.setRepeatingAlarm(value, null, KMMDroidApp.ALARM_HOMEWIDGET);
        		}
        	}
        
        	// See if the user wants to get notifications of schedules that are due Today or past due.
        	if(KMMDapp.prefs.getBoolean("receiveNotifications", false))
        	{
        		// Check to see if the alarm is already set, if not then set it.
        		if( !KMMDapp.isNotificationAlarmSet() )
        		{
        			Log.d(TAG, "First time we have run and we need to setup the Notifications for the user.");
        			final Calendar updateTime = Calendar.getInstance();
        			int intHour = KMMDapp.prefs.getInt("notificationTime.hour", 0);
        			int intMin = KMMDapp.prefs.getInt("notificationTime.minute", 0);
        			updateTime.set(Calendar.HOUR_OF_DAY, intHour);
        			updateTime.set(Calendar.MINUTE, intMin);
        			updateTime.set(Calendar.SECOND, 0);
        			KMMDapp.setRepeatingAlarm(null, updateTime, KMMDroidApp.ALARM_NOTIFICATIONS);
        		}
        		else
        			Log.d(TAG, "Nofications alreadyd set up, no need to reset them......");
        	}
        
        	// See if the user wants us to check for schedules that need to be automatically entered at startup. If so then enter them.
        	if(KMMDapp.prefs.getBoolean("checkSchedulesStartup", false) && KMMDapp.prefs.getBoolean("autoEnterScheduleStartup", false))
        	{
        		// See if the user has set the preference to start up with the last used database
        		if( KMMDapp.prefs.getBoolean("openLastUsed", false) )
        		{
        			KMMDapp.setFullPath(KMMDapp.prefs.getString("Full Path", ""));
        			KMMDapp.openDB();
        		}
        	
        		Cursor c = null;
        		String accountUsed = KMMDapp.prefs.getString("accountUsed", "");
        		
        		// Get our active schedules from the database.
        		String selection = schedulesSelection + "'" + accountUsed + "'";
        		c = KMMDapp.db.query(schedulesTable, schedulesColumns, selection, null, null, null, schedulesOrderBy);

        		GregorianCalendar calToday = new GregorianCalendar();
        		GregorianCalendar calYesterday = new GregorianCalendar();
        		calYesterday = (GregorianCalendar) calToday.clone();
        		calYesterday.add(Calendar.DAY_OF_MONTH, -1);
        		String strToday = String.valueOf(calToday.get(Calendar.YEAR)) + "-" + String.valueOf(calToday.get(Calendar.MONTH)+ 1) + "-"
        				+ String.valueOf(calToday.get(Calendar.DAY_OF_MONTH));
        		String strYesterday = String.valueOf(calYesterday.get(Calendar.YEAR)) + "-" + String.valueOf(calYesterday.get(Calendar.MONTH)+ 1) + "-"
        				+ String.valueOf(calYesterday.get(Calendar.DAY_OF_MONTH));
    		
        		// We have our open schedules from the database, now create the user defined period of cash flow.
        		ArrayList<Schedule> Schedules = new ArrayList<Schedule>();
    		
        		Schedules = Schedule.BuildCashRequired(c, Schedule.padFormattedDate(strYesterday), Schedule.padFormattedDate(strToday), 
        												Transaction.convertToPennies("0.00"), getBaseContext(), fromWidgetId);

        		// Get a list of all schedules that are due today AND are setup for autoEntry.
        		ArrayList<String> autoEnterSchedules = new ArrayList<String>();
        		for(int i=0; i < Schedules.size(); i++)
        		{
        			if( Schedules.get(i).isDueToday() && Schedules.get(i).getAutoEnter())
        				autoEnterSchedules.add(Schedules.get(i).getId());
        		}
    		
        		// Take the list of schedules that need to be entered and create the transactions from the scheduleId and enter it into the database.
        		Schedule schedule = null;
        		for(String scheduleId : autoEnterSchedules)
        		{
        			// Get the schedule from the supplied id
        			schedule = getSchedule(scheduleId, fromWidgetId);
        			Transaction transaction = schedule.convertToTransaction(createTransId());
        			transaction.setEntryDate(calToday);
        			transaction.Save();
        			schedule = null;
        			
        			// Need to repull in the information for the schedule as the transactionId is changed above and stays on the transaction not the
        			// schedule. Not sure why...
        			schedule = getSchedule(scheduleId, fromWidgetId);
        			//Need to advance the schedule to the next date and update the lastPayment and startDate dates to the recorded date of the transaction.
        			schedule.advanceDueDate(/*Schedule.getOccurence(schedule.getOccurence(), schedule.getOccurenceMultiplier())*/);
        			ContentValues values = new ContentValues();
        			values.put("nextPaymentDue", schedule.getDatabaseFormattedString());
        			values.put("startDate", schedule.getDatabaseFormattedString());
        			values.put("lastPayment", transaction.formatEntryDateString());
        			KMMDapp.db.update("kmmSchedules", values, "id=?", new String[] { schedule.getId() });
        			//Need to update the schedules splits in the kmmsplits table as this is where the upcoming bills in desktop comes from.
        			for(int i=0; i < schedule.Splits.size(); i++)
        			{
        				Split s = schedule.Splits.get(i);
        				s.setPostDate(schedule.getDatabaseFormattedString());
        				s.commitSplit(true);
        				s = null;
        			}		
        			//Need to update the schedule in kmmTransactions postDate to match the splits and the actual schedule for the next payment due date.
        			values.clear();
        			values.put("postDate", schedule.getDatabaseFormattedString());
        			KMMDapp.db.update("kmmTransactions", values, "id=?", new String[] { schedule.getId() });
        			
        			//Now update the kmmFileInfo row for the entered items.
        			KMMDapp.updateFileInfo("hiTransactionId", 1);
        			KMMDapp.updateFileInfo("transactions", 1);
        			KMMDapp.updateFileInfo("splits", transaction.splits.size());
        			KMMDapp.updateFileInfo("lastModified", 0);
        			transaction = null;
        			schedule = null;
        		}	
    		
        		// If the user has the preference item of updateFrequency = Auto fire off a Broadcast
        		if(KMMDapp.getAutoUpdate())
        		{
        			Intent intent = new Intent(KMMDService.DATA_CHANGED);
        			sendBroadcast(intent, KMMDService.RECEIVE_HOME_UPDATE_NOTIFICATIONS);
        		}	
			
        		// Just do a toast for now to see if we are getting results correctly.
        		Toast.makeText(this, "Number of schedules that where auto-entered: " + autoEnterSchedules.size(), Toast.LENGTH_SHORT).show();
        	}
        
    		Editor edit = KMMDapp.prefs.edit();
        	if( !Closing )
        	{
        		// See if we are starting from a home widget, if so use that database.
        		if(fromWidgetId != null && fromWidgetId != "9999")
        		{
        			//See if the database is already open, if so, close it.
        			if(KMMDapp.isDbOpen())
        				KMMDapp.closeDB();
        			
        			KMMDapp.setFullPath(KMMDapp.prefs.getString("widgetDatabasePath" + fromWidgetId, ""));
            		// Start the HomeActivity.
        			Log.d(TAG, "Starting from home widget: " + fromWidgetId);
            		startActivity(new Intent(this, HomeActivity.class));
            		finish();
        		}
        		
        		// See if the user has set the preference to start up with the last used database
        		if( KMMDapp.prefs.getBoolean("openLastUsed", false) )
        		{
        			KMMDapp.setFullPath(KMMDapp.prefs.getString("Full Path", ""));
            		// Start the HomeActivity.
            		startActivity(new Intent(this, HomeActivity.class));
            		finish();
        		}
        		
        		// Save the currently opened database in the preferences.
        		edit.putString("currentOpenedDatabase", KMMDapp.getFullPath());
        		edit.apply();
        	}
        	else
        	{
        		//Remove the entry for currentOpenedDatabase from preferences.
        		edit.remove("currentOpenedDatabase");
        		edit.apply();
        	}
        }
        // Find our views
        setContentView(R.layout.welcome);
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
    
	// Called first time the user clicks on the menu button
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.welcome_menu, menu);
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
		
		// remove the New and Recent menu items for now.
		menu.findItem(R.id.itemNew).setVisible(false);
		menu.findItem(R.id.itemRecent).setVisible(false);
		
		return true;
	}
	// Called when an options item is clicked
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
    	Intent i = null;
    	
		switch (item.getItemId())
		{
			case R.id.itemNew:
    			i = new Intent(this, NewDatabaseActivity.class);
    			startActivityForResult(i, 0);
				break;
			case R.id.itemOpen:
    			i = new Intent(this, FileChooser.class);
    			startActivityForResult(i, 0);
				break;
			case R.id.itemRecent:
				break;
			case R.id.syncDropbox:
				i = new Intent(this, KMMDDropboxService.class);
				i.putExtra("cloudService", KMMDDropboxService.CLOUD_DROPBOX);
				startService(i);
				break;
		}
		
		return true;
	}
    @Override
    protected void onActivityResult(int pRequestCode, int resultCode, Intent data)
    {
    	Intent i = null;
    	
    	if( resultCode != -1)
    	{
    		String fromActivity = data.getStringExtra("FromActivity");
    		Log.d(TAG, "result: " + fromActivity.toString());
    		
    		if( fromActivity.equalsIgnoreCase("FileChooser") )
    		{
    			String path = data.getStringExtra("FullPath");
    			Log.d(TAG, "Full Path: " + path);
    			KMMDapp.setFullPath(path);
        		// Save the currently opened database in the preferences.
    			Editor edit = KMMDapp.prefs.edit();
        		edit.putString("currentOpenedDatabase", KMMDapp.getFullPath());
        		edit.apply();
    			i = new Intent(this, HomeActivity.class);
    			startActivity(i);
    			finish();
    		}
    		
    		if( fromActivity.equalsIgnoreCase("NewDatabase") )
    		{
    			String dbName = data.getStringExtra("DatabaseName");
    			DbHelper dbHelper = new DbHelper(this, dbName);
    			SQLiteDatabase db = dbHelper.getReadableDatabase();
    			db.close();
    			Toast.makeText(this, "New Database created: " + dbName, Toast.LENGTH_SHORT).show();
    		}
    	}
    }

	// **************************************************************************************************
	// ************************************ Helper methods **********************************************
    
	private Schedule getSchedule(String schId, String fromWidgetId)
	{
		Log.d(TAG, "schId: " + schId);
		Cursor schedule = KMMDapp.db.query("kmmschedules",new String[] { "*" }, "id=?", new String[] { schId }, null, null, null);
		Cursor splits = KMMDapp.db.query("kmmsplits", new String[] { "*" }, "transactionId=?", new String[] { schId }, null, null, "splitId");
		Cursor transaction = KMMDapp.db.query("kmmTransactions", new String[] { "*" }, "id=?", new String[] { schId }, null, null, null);

		Log.d(TAG, "Number of transactions returned: " + transaction.getCount());
		return new Schedule(schedule, splits, transaction, getBaseContext(), fromWidgetId);
	}

	private String createTransId()
	{
		final String[] dbColumns = { "hiTransactionId"};
		final String strOrderBy = "hiTransactionId DESC";
		// Run a query to get the Transaction ids so we can create a new one.
		Cursor cursor = KMMDapp.db.query("kmmFileInfo", dbColumns, null, null, null, null, strOrderBy);
		startManagingCursor(cursor);
		
		cursor.moveToFirst();

		// Since id is in T000000000000000000 format, we need to pick off the actual number then increase by 1.
		int lastId = cursor.getInt(0);
		lastId = lastId +1;
		String nextId = Integer.toString(lastId);
		
		// Need to pad out the number so we get back to our P000000 format
		String newId = "T";
		for(int i= 0; i < (18 - nextId.length()); i++)
		{
			newId = newId + "0";
		}
		
		// Tack on the actual number created.
		newId = newId + nextId;
		
		return newId;
	}
}