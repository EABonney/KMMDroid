package com.vanhlebarsoftware.kmmdroid;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.SimpleCursorAdapter.ViewBinder;

public class ViewScheduleActivity extends FragmentActivity 
{
	private static final String TAG = ViewScheduleActivity.class.getSimpleName();
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
	private static final String dbTable = "kmmSplits, kmmAccounts, kmmPayees";
	private static final String[] dbColumns = { "splitId", "transactionId AS _id", "valueFormatted", "memo",
												"accountId", "id", "checkNumber", "accountName", "kmmPayees.name AS payeeName" };
	private static final String strSelection = "(accountId = id) AND transactionId = ? AND splitId <> 0 AND kmmPayees.id = kmmSplits.payeeId";
	private static final String strOrderBy = "splitId ASC";
	static final String[] FROM = { "accountName", "memo", "valueFormatted" };
	static final int[] TO = { R.id.splitsAccountName, R.id.splitsMemo, R.id.splitsAmount  };
	String schId = null;
	ArrayList<Split> Splits;
	KMMDroidApp KMMDapp;
	Cursor cursor;
	SimpleCursorAdapter adapter;
	ListView listSplits;
	TextView textTitleViewSch;
	TextView textDate;
	TextView textChkNum;
	TextView textAmount;
	TextView textMemo;
	TextView textDescription;
	TextView textStatus;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
        //setContentView(R.layout.view_schedule);

        // Get our application
        KMMDapp = ((KMMDroidApp) getApplication());
        
        // Find our views
        listSplits = (ListView) findViewById(R.id.listSplits);
        textDate = (TextView) findViewById(R.id.vtDate);
        textChkNum = (TextView) findViewById(R.id.vtNumber);
        textAmount = (TextView) findViewById(R.id.vtAmount);
        textMemo = (TextView) findViewById(R.id.vtMemo);
        textDescription = (TextView) findViewById(R.id.vtDescription);
        textStatus = (TextView) findViewById(R.id.vtStatus);
        
        // See if the database is already open, if not open it Read/Write.
        if(!KMMDapp.isDbOpen())
        {
        	KMMDapp.openDB();
        }
        
        // Set the Description, Amount, Date and Memo fields.
        Bundle extras = getIntent().getExtras();
        schId = extras.getString("id");
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
		
		// Get the schedule's base information
		Cursor sch = KMMDapp.db.query("kmmSchedules", new String[] { "*" }, "id=?", new String[] { schId }, null, null, null);
		sch.moveToFirst();
		textTitleViewSch.setText(sch.getString(Schedule.COL_DESCRIPTION));
		textDate.setText(sch.getString(Schedule.COL_NEXTPAYMENTDUE));
		
		// Put the transactionId into a String array
		String[] selectionArgs = { schId };
		
		//Run the query on the database to get the schedule's split information.
		cursor = KMMDapp.db.query(dbTable, dbColumns, strSelection, selectionArgs, null, null, strOrderBy);
		cursor.moveToFirst();
		textChkNum.setText(cursor.getString(6));
		
		startManagingCursor(cursor);
		
		// Set up the adapter
		adapter = new SimpleCursorAdapter(this, R.layout.splits_row, cursor, FROM, TO);
		adapter.setViewBinder(VIEW_BINDER);
		listSplits.setAdapter(adapter); 
		

	}

	// Called first time the user clicks on the menu button
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.edit_menu, menu);
		return true;
	}
	
	// Called when an options item is clicked
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.itemEdit:
				Intent i = new Intent(getBaseContext(), CreateModifyTransactionActivity.class);
				i.putExtra("Action", ACTION_EDIT);
				i.putExtra("scheduleId", schId);
				startActivity(i);
				finish();
				break;
			case R.id.itemDelete:
				AlertDialog.Builder alertDel = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogNoTitle));
				alertDel.setTitle(R.string.delete);
				alertDel.setMessage(getString(R.string.titleDeleteTransaction));

				alertDel.setPositiveButton(getString(R.string.titleButtonOK), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {					
						// Get our splits details.
						Splits = getSplits(schId);
											
						// Delete the schedule 
						KMMDapp.db.delete("kmmSchedules", "id=?", new String[] { schId });
						
						// Delete the transaction from the transactions table.
						KMMDapp.db.delete("kmmTransactions", "id=?", new String[] { schId });
						
						// Delete the splits for the selected transaction from the splits table
						int splitsDeleted = KMMDapp.db.delete("kmmSplits", "transactionId=?", new String[] { schId });						
						
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
				break;
			case R.id.itemCancel:
				finish();
				break;
		}
		return true;
	}
	// View binder to do formatting of the string values to numbers with commas and parenthesis
	static final ViewBinder VIEW_BINDER = new ViewBinder() {
		public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
			if(view.getId() != R.id.splitsAmount)
				return false;
			
			// Format the Amount properly.
			((TextView) view).setText(String.format("%,(.2f", Float.valueOf(cursor.getString(columnIndex))));
			
			return true;
		}
	};
	
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
								 cursor.getString(C_POSTDATE), cursor.getString(C_BANKID), "9999", getBaseContext()));
			cursor.moveToNext();
		}
		
		cursor.close();
		return splits;
	}
}
