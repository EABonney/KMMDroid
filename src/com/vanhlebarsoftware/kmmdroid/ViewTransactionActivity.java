package com.vanhlebarsoftware.kmmdroid;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.SimpleCursorAdapter.ViewBinder;

@SuppressWarnings("unused")
public class ViewTransactionActivity extends Activity
{
	private static final String TAG = "ViewTransactionActivity";
	private static final String dbTable = "kmmSplits, kmmAccounts";
	private static final String[] dbColumns = { "splitId", "transactionId AS _id", "valueFormatted", "memo",
												"accountId", "id", "checkNumber", "accountName" };
	private static final String strSelection = "(accountId = id) AND transactionId = ? AND splitId <> 0";
	private static final String strOrderBy = "splitId ASC";
	static final String[] FROM = { "accountName", "memo", "valueFormatted" };
	static final int[] TO = { R.id.splitsAccountName, R.id.splitsMemo, R.id.splitsAmount  };
	String Description = null;
	String TransID = null;
	KMMDroidApp KMMDapp;
	Cursor cursor;
	SimpleCursorAdapter adapter;
	ListView listSplits;
	TextView textTitleViewTrans;
	TextView textDate;
	TextView textAmount;
	TextView textMemo;
	TextView textDescription;
	
	/* Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
        setContentView(R.layout.view_transaction);

        // Get our application
        KMMDapp = ((KMMDroidApp) getApplication());
        
        // Find our views
        listSplits = (ListView) findViewById(R.id.listSplits);
        textTitleViewTrans = (TextView) findViewById(R.id.titleViewTransaction);
        textDate = (TextView) findViewById(R.id.vtDate);
        textAmount = (TextView) findViewById(R.id.vtAmount);
        textMemo = (TextView) findViewById(R.id.vtMemo);
        textDescription = (TextView) findViewById(R.id.vtDescription);
        
    	// Now hook into listTransactions ListView and set its onItemClickListener member
    	// to our class handler object.
        //listTransactions.setOnItemClickListener(mMessageClickedHandler);
        
        // See if the database is already open, if not open it Read/Write.
        if(!KMMDapp.isDbOpen())
        {
        	KMMDapp.openDB();
        }
        
        // Set the Description, Amount, Date and Memo fields.
        Bundle extras = getIntent().getExtras();
        textDescription.setText(extras.getString("Description"));
        textDate.setText(extras.getString("Date"));
		textAmount.setText(String.format("%,(.2f", Float.valueOf(extras.getString("Amount"))));
        textMemo.setText(extras.getString("Memo"));
        TransID = extras.getString("TransID");
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

		// Put the transactionId into a String array
		String[] selectionArgs = { TransID };
		
		//Run the query on the database to get the transactions.
		cursor = KMMDapp.db.query(dbTable, dbColumns, strSelection, selectionArgs, null, null, strOrderBy);
		startManagingCursor(cursor);
		
		// Set up the adapter
		adapter = new SimpleCursorAdapter(this, R.layout.splits_row, cursor, FROM, TO);
		adapter.setViewBinder(VIEW_BINDER);
		listSplits.setAdapter(adapter); 
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
}
