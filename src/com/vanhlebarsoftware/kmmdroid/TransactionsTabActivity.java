package com.vanhlebarsoftware.kmmdroid;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.SimpleCursorAdapter.ViewBinder;

public class TransactionsTabActivity extends FragmentActivity
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
	private boolean fromPayee = false;
	CreateModifyPayeeActivity payeeTabHost;
	CreateModifyCategoriesActivity categoryTabHost;
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
        
        // Set the AccountId selected fields.
        Bundle extras = getIntent().getExtras();
        PayeeId = extras.getString("PayeeId");
        PayeeName = extras.getString("PayeeName");
        CategoryId = extras.getString("CategoryId");
        CategoryName = extras.getString("CategoryName");
        
        if( PayeeId != null )
        	fromPayee = true;
    
        // Find our views
        listPayeeTrans = (ListView) findViewById(R.id.listPayeeTransView);

        // See if the database is already open, if not open it Read/Write.
        if(!KMMDapp.isDbOpen())
        {
        	KMMDapp.openDB();
        }
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
	
	@Override
	public void onBackPressed()
	{
		boolean isDirty = false;
		
        // Get the correct tabHost based on the parent.
        if( fromPayee )
        {
        	payeeTabHost = ((CreateModifyPayeeActivity) this.getParent());
        	isDirty = payeeTabHost.getIsDirty();
        }
        else
        {
        	categoryTabHost = ((CreateModifyCategoriesActivity) this.getParent());
        	isDirty = categoryTabHost.getIsDirty();
        }
        
		if( isDirty )
		{
			AlertDialog.Builder alertDel = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogNoTitle));
			alertDel.setTitle(R.string.BackActionWarning);
			alertDel.setMessage(getString(R.string.titleBackActionWarning));

			alertDel.setPositiveButton(getString(R.string.titleButtonOK), new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int whichButton)
				{
					finish();
				}
			});
			
			alertDel.setNegativeButton(getString(R.string.titleButtonCancel), new DialogInterface.OnClickListener() 
			{
				public void onClick(DialogInterface dialog, int whichButton) 
				{
					// Canceled.
				}
			});				
			alertDel.show();
		}
		else
		{
			finish();
		}
	}
	
	// View binder to do formatting of the string values to numbers with commas and parenthesis
	static final ViewBinder VIEW_BINDER = new ViewBinder() 
	{
		public boolean setViewValue(View view, Cursor cursor, int columnIndex) 
		{
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
				return false;
			case R.id.ptrAccount:
				return false;
			case R.id.ptrDetails:
				((TextView) view).setText(cursor.getString(columnIndex));
				return true;
			case R.id.ptrAmount:
				// Format the Amount properly.
				((TextView) view).setText(Transaction.convertToDollars(Transaction.convertToPennies(cursor.getString(columnIndex)), true));
				return true;
			default:
				return false;
			}
		}
	};
}
