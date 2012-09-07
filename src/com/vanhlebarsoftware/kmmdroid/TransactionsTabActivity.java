package com.vanhlebarsoftware.kmmdroid;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.SimpleCursorAdapter.ViewBinder;

public class TransactionsTabActivity extends Activity
{
	private static final String TAG = "PayeeActivity";
	private static final String dbTable = "kmmSplits, kmmAccounts";
	private static final String[] dbColumns = { "splitId", "transactionId AS _id", "valueFormatted",
												"accountId", "postDate", "id", "accountName", "memo" };
	private static final String strSelectionPayee = "(accountId = id) AND payeeId = ? AND splitId = 0 AND txType = 'N'";
	private static final String strSelectionCategory = "(accountId = id) AND accountId = ? AND txType = 'N'";
	private static final String strOrderBy = "postDate ASC";
	static final String[] FROM = { "postDate", "accountName", "valueFormatted", "memo" };
	static final int[] TO = { R.id.ptrDate, R.id.ptrAccount, R.id.ptrAmount, R.id.ptrDetails };
	String PayeeId = null;
	String PayeeName = null;
	String CategoryId = null;
	String CategoryName = null;
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
        CategoryId = extras.getString("CategoryId");
        CategoryName = extras.getString("CategoryName");
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
	
		//Get all the accounts to be displayed.
		if(PayeeId != null)
		{
			// Put the PayeeId into a String array
			String[] selectionArgs = { PayeeId };
			cursor = KMMDapp.db.query(dbTable, dbColumns, strSelectionPayee, selectionArgs, null, null, strOrderBy);
		}
		else if(CategoryId != null)
		{
			// Put the PayeeId into a String array
			String[] selectionArgs = { CategoryId };
			cursor = KMMDapp.db.query(dbTable, dbColumns, strSelectionCategory, selectionArgs, null, null, strOrderBy);
		}
		
		startManagingCursor(cursor);
		
		// Set up the adapter
		adapter = new SimpleCursorAdapter(this, R.layout.payee_transactions_row, cursor, FROM, TO);
		adapter.setViewBinder(VIEW_BINDER);
		listPayeeTrans.setAdapter(adapter);
	}
	
	// View binder to do formatting of the string values to numbers with commas and parenthesis
	static final ViewBinder VIEW_BINDER = new ViewBinder() 
	{
		public boolean setViewValue(View view, Cursor cursor, int columnIndex) 
		{
			Log.d(TAG, "Cursor row: " + cursor.getPosition() + " of " + cursor.getCount());
			LinearLayout row = (LinearLayout) view.getRootView().findViewById(R.id.payeeTransRow);
			
			if( row != null)
			{
				if( cursor.getPosition() % 2 == 0)
					row.setBackgroundColor(Color.rgb(0x62, 0xB1, 0xF6));
				else
					row.setBackgroundColor(Color.rgb(0x62, 0xa1, 0xc6));
			}
			
			switch(view.getId())
			{
			case R.id.ptrDate:
				Log.d(TAG, "ViewBinder: Date");
				return false;
			case R.id.ptrAccount:
				Log.d(TAG, "ViewBinder: Account name");
				return false;
			case R.id.ptrDetails:
				Log.d(TAG, "ViewBinder: Details");
				((TextView) view).setText(cursor.getString(columnIndex));
				return true;
			case R.id.ptrAmount:
				Log.d(TAG, "ViewBinder: Amount");
				// Format the Amount properly.
				((TextView) view).setText(Transaction.convertToDollars(Transaction.convertToPennies(cursor.getString(columnIndex))));
				return true;
			default:
				return false;
			}
		}
	};
}
