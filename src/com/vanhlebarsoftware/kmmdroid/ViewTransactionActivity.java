package com.vanhlebarsoftware.kmmdroid;

import java.util.StringTokenizer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.SimpleCursorAdapter.ViewBinder;

@SuppressWarnings("unused")
public class ViewTransactionActivity extends Activity
{
	private static final String TAG = "ViewTransactionActivity";
	private static final int ACTION_NEW = 1;
	private static final int ACTION_EDIT = 2;
	private static final String dbTable = "kmmSplits, kmmAccounts";
	private static final String[] dbColumns = { "splitId", "transactionId AS _id", "valueFormatted", "memo",
												"accountId", "id", "checkNumber", "accountName" };
	private static final String strSelection = "(accountId = id) AND transactionId = ? AND splitId <> 0";
	private static final String strOrderBy = "splitId ASC";
	static final String[] FROM = { "accountName", "memo", "valueFormatted" };
	static final int[] TO = { R.id.splitsAccountName, R.id.splitsMemo, R.id.splitsAmount  };
	String Description = null;
	String TransID = null;
	String strStatus = null;
	KMMDroidApp KMMDapp;
	Cursor cursor;
	SimpleCursorAdapter adapter;
	ListView listSplits;
	TextView textTitleViewTrans;
	TextView textDate;
	TextView textAmount;
	TextView textMemo;
	TextView textDescription;
	TextView textStatus;
	
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
        textStatus = (TextView) findViewById(R.id.vtStatus);
        
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
        strStatus = extras.getString("Status");
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
		
		// Set the status of the transaction
		Log.d(TAG, "Status: " + strStatus);
		if(strStatus.contentEquals("0"))
			textStatus.setText(R.string.notreconciled);
		else if(strStatus.contentEquals("1"))
			textStatus.setText(R.string.cleared);
		else if(strStatus.contentEquals("2"))
			textStatus.setText(R.string.reconciled);
		else
			textStatus.setText("Error!!");
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
				i.putExtra("transId", TransID);
				startActivity(i);
				finish();
				break;
			case R.id.itemDelete:
				AlertDialog.Builder alertDel = new AlertDialog.Builder(this);
				alertDel.setTitle(R.string.delete);
				alertDel.setMessage(getString(R.string.titleDeleteTransaction));

				alertDel.setPositiveButton(getString(R.string.titleButtonOK), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// Update the accounts balance.
						Cursor cur = KMMDapp.db.query("kmmSplits", new String[] { "accountId", "valueFormatted" }, "transactionId=? AND splitId=0",
								new String[] { TransID }, null, null, null);
						startManagingCursor(cur);
						cur.moveToFirst();
						updateAccount(cur.getString(0), cur.getString(1));
						
						// Delete the transaction from the transactions table.
						KMMDapp.db.delete("kmmTransactions", "id=?", new String[] { TransID });
						
						// Delete the splits for the selected transaction from the splits table
						int splitsDeleted = KMMDapp.db.delete("kmmSplits", "transactionId=?", new String[] { TransID });						
						
						// Update the number of splits inside kmmFileInfo table.
						Cursor c = KMMDapp.db.query("kmmFileInfo", new String[] { "transactions", "splits" }, null, null, null, null, null);
						startManagingCursor(c);
						c.moveToFirst();
						int trans = c.getInt(0);
						int splits = c.getInt(1);
						ContentValues values = new ContentValues();
						values.put("transactions", (trans - 1));
						values.put("splits", (splits - splitsDeleted));
						KMMDapp.db.update("kmmFileInfo", values, null, null);
						
						// Update the number of transactions for the accounts used.
						c.close();
						cur.close();
						finish();
					}
				});
				alertDel.setNegativeButton(getString(R.string.titleButtonCancel), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// Canceled.
						Log.d(TAG, "User cancelled delete.");
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
	private String createBalance(String formattedValue)
	{
		StringTokenizer split = new StringTokenizer(formattedValue, ".");
		String dollars = split.nextToken();
		String cents = split.nextToken();
		String balance = dollars + cents;
		String denominator = "/100";
		return balance + denominator;
	}
	
	private void updateAccount( String accountId, String transValue)
	{
		Cursor c = KMMDapp.db.query("kmmAccounts", new String[] { "balanceFormatted" }, "id=?", new String[] { accountId }, null, null, null);
		startManagingCursor(c);
		c.moveToFirst();
		float balance = Float.valueOf(c.getString(0));
		float tValue = Float.valueOf(transValue);
		float newBalance = balance - tValue;

		ContentValues values = new ContentValues();
		values.put("balanceFormatted", String.valueOf(newBalance));
		values.put("balance", createBalance(String.valueOf(newBalance)));
		
		KMMDapp.db.update("kmmAccounts", values, "id=?", new String[] { accountId });
	}
}
