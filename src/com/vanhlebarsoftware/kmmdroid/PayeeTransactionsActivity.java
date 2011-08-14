package com.vanhlebarsoftware.kmmdroid;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.SimpleCursorAdapter.ViewBinder;

public class PayeeTransactionsActivity extends Activity
{
	private static final String TAG = "PayeeActivity";
	private static final String dbTable = "kmmSplits, kmmAccounts";
	private static final String[] dbColumns = { "splitId", "transactionId AS _id", "valueFormatted",
												"accountId", "postDate", "id", "accountName" };
	private static final String strSelection = "(accountId = id) AND payeeId = ? AND splitId = 0";
	private static final String strOrderBy = "postDate ASC";
	static final String[] FROM = { "postDate", "accountName", "valueFormatted" };
	static final int[] TO = { R.id.ptrDate, R.id.ptrAccount, R.id.ptrAmount };
	String PayeeId = null;
	String PayeeName = null;
	KMMDroidApp KMMDapp;
	Cursor cursor;
	ListView listPayeeTrans;
	SimpleCursorAdapter adapter;
	
	/* Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
        setContentView(R.layout.payee_transactions);
        
        // Get our application
        KMMDapp = ((KMMDroidApp) getApplication());
        
        // Find our views
        listPayeeTrans = (ListView) findViewById(R.id.listPayeeTransView);

    	// Now hook into listAccounts ListView and set its onItemClickListener member
    	// to our class handler object.
        //listPayeeTrans.setOnItemClickListener(mMessageClickedHandler);

        // See if the database is already open, if not open it Read/Write.
        if(!KMMDapp.isDbOpen())
        {
        	KMMDapp.openDB();
        }
        
        // Set the AccountId selected fields.
        Bundle extras = getIntent().getExtras();
        PayeeId = extras.getString("PayeeId");
        PayeeName = extras.getString("PayeeName");
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

		// Put the PayeeId into a String array
		String[] selectionArgs = { PayeeId };
		
		//Get all the accounts to be displayed.
		cursor = KMMDapp.db.query(dbTable, dbColumns, strSelection, selectionArgs, null, null, strOrderBy);
		startManagingCursor(cursor);
		
		// Set up the adapter
		adapter = new SimpleCursorAdapter(this, R.layout.payee_transactions_row, cursor, FROM, TO);
		adapter.setViewBinder(VIEW_BINDER);
		listPayeeTrans.setAdapter(adapter);
	}
	
	// View binder to do formatting of the string values to numbers with commas and parenthesis
	static final ViewBinder VIEW_BINDER = new ViewBinder() {
		public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
			if(view.getId() != R.id.ptrAmount)
				return false;
			
			// Format the Amount properly.
			((TextView) view).setText(String.format("%,(.2f", Float.valueOf(cursor.getString(columnIndex))));
			
			return true;
		}
	};
}
