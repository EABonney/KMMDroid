package com.vanhlebarsoftware.kmmdroid;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class ScheduleActionsActivity extends FragmentActivity
{
	private static final String TAG = ScheduleActionsActivity.class.getSimpleName();
	private static final int ACTION_EDIT = 2;
	private static int C_TRANSACTIONID = 0;
	private static int C_TXTYPE = 1;
	private static int C_SPLITID = 2;
	private static int C_PAYEEID = 3;
	private static int C_RECONCILEDATE = 4;
	private static int C_ACTION = 5;
	private static int C_RECONCILEFLAG = 6;
	private static int C_VALUE = 7;
	private static int C_VALUEFORMATTED = 8;
	private static int C_SHARES = 9;
	private static int C_SHARESFORMATTED = 10;
	private static int C_PRICE = 11;
	private static int C_PRICEFORMATTED = 12;
	private static int C_MEMO = 13;
	private static int C_ACCOUNTID = 14;
	private static int C_CHECKNUMBER = 15;
	private static int C_POSTDATE = 16;
	private static int C_BANKID = 17;
	private String scheduleId = null;
	private String scheduleDesc = null;
	private int Action = 0;
	private String widgetDatabasePath = null;
	private String widgetId = null;
	ArrayList<Split> Splits;
	KMMDroidApp KMMDapp;
	
	Button buttonEnter;
	Button buttonSkip;
	Button buttonEdit;
	Button buttonDelete;
	TextView tvScheduleDescription;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
        setContentView(R.layout.dialogscheduleactions);
        
        // Get our application
        KMMDapp = ((KMMDroidApp) getApplication());
        
        // Get the scheduleId the user clicked on in the widget.
        Bundle extras = getIntent().getExtras();
        scheduleId = extras.getString("scheduleId");
        scheduleDesc = extras.getString("scheduleDescription");
        Action = extras.getInt("Action");
        widgetDatabasePath = extras.getString("widgetDatabasePath");
        widgetId = extras.getString("widgetId");
        Log.d(TAG, "widgetId: " + widgetId);

        // If the widgetDatabasePath is empty then we came from inside the app and don't need to worry
        // about the database path, since it is already open.
        if(widgetDatabasePath != null)
        {	
        	// Ensure that we open the correct database for this item.
        	KMMDapp.setFullPath(widgetDatabasePath);
        }
        
        // See if the database is already open, if not open it Read/Write.
        if(!KMMDapp.isDbOpen())
        {
        	KMMDapp.openDB();
    		// Set the currently opened database.
    		Editor edit = KMMDapp.prefs.edit();
    		edit.putString("currentOpenedDatabase", widgetDatabasePath);
    		edit.apply();
        }
        
        // Find our views
        buttonEnter = (Button) findViewById(R.id.btnEnterSchedule);
        buttonSkip = (Button) findViewById(R.id.btnSkipSchedule);
        buttonEdit = (Button) findViewById(R.id.btnEditSchedule);
        buttonDelete = (Button) findViewById(R.id.btnDeleteSchedule);
        tvScheduleDescription = (TextView) findViewById(R.id.scheduleDescription);
        
        // Update the description of the TextView to include the actual description of the schedule the user wants to operate on.
        String str = tvScheduleDescription.getText().toString() + "\n" + scheduleDesc;
        tvScheduleDescription.setText(str);
        
        // Set our onClickListener events.
        buttonEnter.setOnClickListener(new View.OnClickListener()
        {
			public void onClick(View arg0)
			{
				Intent i = new Intent(getBaseContext(), CreateModifyTransactionActivity.class);
				i.putExtra("scheduleId", scheduleId);
				i.putExtra("Action", Action);
				i.putExtra("widgetDatabasePath", widgetDatabasePath);
				i.putExtra("fromScheduleActions", true);
				i.putExtra("fromWidgetId", widgetId);
				startActivity(i);
				finish();
			}
		});

        buttonSkip.setOnClickListener(new View.OnClickListener()
        {

			public void onClick(View arg0)
			{					
				AlertDialog.Builder alertDel = new AlertDialog.Builder(arg0.getContext());
				alertDel.setTitle(R.string.skip);
				alertDel.setMessage(getString(R.string.titleSkipSchedule));

				alertDel.setPositiveButton(getString(R.string.titleButtonYes), new DialogInterface.OnClickListener() 
				{
					public void onClick(DialogInterface dialog, int whichButton)
					{					
						Intent intent = new Intent(getBaseContext(), KMMDService.class);
						intent.putExtra("skipScheduleId", scheduleId);
						if(widgetId == null)
							widgetId = "9999";
						intent.putExtra("widgetId", widgetId);
						startService(intent);
						finish();
					}
				});
				alertDel.setNegativeButton(getString(R.string.titleButtonNo), new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int whichButton) 
					{
						// Canceled.
						finish();
					}
				});				
				alertDel.show();
			}
		});
        
        buttonEdit.setOnClickListener(new View.OnClickListener()
        {
			public void onClick(View arg0)
			{
				Intent i = new Intent(getBaseContext(), CreateModifyScheduleActivity.class);
				i.putExtra("scheduleId", scheduleId);
				i.putExtra("Action", ACTION_EDIT);
				i.putExtra("widgetDatabasePath", widgetDatabasePath);
				i.putExtra("fromScheduleActions", true);
				startActivity(i);
				finish();
			}
		});
        
        buttonDelete.setOnClickListener(new View.OnClickListener()
        {
        	public void onClick(View arg0)
        	{
        		Context base = arg0.getContext();
				AlertDialog.Builder alertDel = new AlertDialog.Builder(new ContextThemeWrapper(base, R.style.AlertDialogNoTitle));
				alertDel.setTitle(R.string.delete);
				alertDel.setMessage(getString(R.string.titleDeleteTransaction));

				alertDel.setPositiveButton(getString(R.string.titleButtonOK), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {					
						// Get our splits details.
						Splits = getSplits(scheduleId);
											
						// Delete the schedule 
						KMMDapp.db.delete("kmmSchedules", "id=?", new String[] { scheduleId });
						
						// Delete the transaction from the transactions table.
						KMMDapp.db.delete("kmmTransactions", "id=?", new String[] { scheduleId });
						
						// Delete the splits for the selected transaction from the splits table
						int splitsDeleted = KMMDapp.db.delete("kmmSplits", "transactionId=?", new String[] { scheduleId });						
						
						// Update the number of splits inside kmmFileInfo table.
						Cursor c = KMMDapp.db.query("kmmFileInfo", new String[] { "transactions", "splits", "schedules" }, null, null, null, null, null);
						startManagingCursor(c);
						c.moveToFirst();
						int trans = c.getInt(0);
						int splits = c.getInt(1);
						int schedules = c.getInt(2);

						KMMDapp.updateFileInfo("schedules", (schedules - 1));
						KMMDapp.updateFileInfo("transactions", (trans - 1));
						KMMDapp.updateFileInfo("splits", (splits - splitsDeleted));

						c.close();

						// If the user has the preference item of updateFrequency = Auto fire off a Broadcast
						if(KMMDapp.getAutoUpdate())
						{
							Intent intent = new Intent(KMMDService.DATA_CHANGED);
							sendBroadcast(intent, KMMDService.RECEIVE_HOME_UPDATE_NOTIFICATIONS);
						}
						finish();
					}
				});
				alertDel.setNegativeButton(getString(R.string.titleButtonCancel), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// Canceled.
					}
					});				
				alertDel.show();
        	}
        });
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
	}

	@Override
	protected void onResume()
	{
		super.onResume();

	}

	// **************************************************************************************************
	// ************************************ Helper methods **********************************************	
	private ArrayList<Split> getSplits(String transId)
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
								 cursor.getString(C_POSTDATE), cursor.getString(C_BANKID), this.widgetId, getBaseContext() ));
			cursor.moveToNext();
		}
		
		cursor.close();
		return splits;
	}
}
