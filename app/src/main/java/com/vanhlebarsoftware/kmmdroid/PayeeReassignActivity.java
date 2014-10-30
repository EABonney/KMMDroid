package com.vanhlebarsoftware.kmmdroid;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;

public class PayeeReassignActivity extends FragmentActivity implements OnItemSelectedListener,
	OnClickListener
{
	private static final String TAG = "PayeeReassignActivity";
	private static final String dbTable = "kmmPayees";
	private static final String[] dbColumns = { "name", "id AS _id"};
	private static final String dbSelection = "id != ?";
	private static final String strOrderBy = "name ASC";
	static final String[] FROM = { "name" };
	static final int[] TO = { android.R.id.text1 };
	String payeeToDeleteId = null;
	boolean firstRun = true;
	KMMDroidApp KMMDapp;
	Cursor cursor;
	Spinner spinAvailPayee;
	SimpleCursorAdapter adapter;
	Button btnOk;
	Button btnCancel;
	
	/* Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
        setContentView(R.layout.payee_reassign);

        // Get the Payee to delete id.
        Bundle extras = getIntent().getExtras();
        payeeToDeleteId = extras.getString("PayeeToDelete");
        
        // Get our application
        KMMDapp = ((KMMDroidApp) getApplication());

        // See if the database is already open, if not open it Read/Write.
        if(!KMMDapp.isDbOpen())
        {
        	KMMDapp.openDB();
        }
     
		// See if we have used this payee in transactions, if not just delete the payee.
        if( !checkPayeeUsed(payeeToDeleteId) )
        {
        	deletePayee();
        	finish();
        }
        
        // Find our views
        spinAvailPayee = (Spinner) findViewById(R.id.payeeAvailable);
        btnOk = (Button) findViewById(R.id.buttonOk);
        btnCancel = (Button) findViewById(R.id.buttonCancel);

        // Set the OnItemSelectedListener for the spinner.
        // Set the OnClickListener's for the buttons.
        spinAvailPayee.setOnItemSelectedListener(this);
        btnOk.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
	}
	
	@Override
	protected void onResume()
	{
		super.onResume();
		
		//Get all the accounts to be displayed.
		cursor = KMMDapp.db.query(dbTable, dbColumns, dbSelection, new String[] { payeeToDeleteId }, null, null, strOrderBy);
		startManagingCursor(cursor);
		
		// Set up the adapter
		adapter = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, cursor, FROM, TO);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		
		spinAvailPayee.setAdapter(adapter);
	}

	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3)
	{
		if( !firstRun )
		{	
			btnOk.setEnabled(true);
			firstRun = false;
		}
		else
			firstRun = false;
	}

	public void onNothingSelected(AdapterView<?> arg0)
	{
		btnOk.setEnabled(false);
	}

	public void onClick(View v)
	{
		switch (v.getId())
		{
			case R.id.buttonOk:
				// First we need to update the kmmSplits database for all transactions with the old
				// payeeId to use the new payeeId.
				ContentValues values = new ContentValues();
				values.put("payeeId", String.valueOf(spinAvailPayee.getSelectedItemId()));
				int rows = KMMDapp.db.update("kmmSplits", values, "payeeId=?", new String[] { payeeToDeleteId });
				
				// If no rows where affected we didn't update correctly, warn user and don't delete.
				if ( rows == 0 )
				{
					AlertDialog.Builder alertDel = new AlertDialog.Builder(this);
					alertDel.setTitle(R.string.error);
					alertDel.setMessage(R.string.unableToReassign);

					alertDel.setPositiveButton(getString(R.string.titleButtonOK), new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							finish();
						}
					});
					alertDel.show();
				}
				// We must have affected some rows so go ahead and delete the payee now.
				else
				{
					deletePayee();
				}
				KMMDapp.updateFileInfo("lastModified", 0);
				finish();
				break;
			case R.id.buttonCancel:
				finish();
				break;
		}
		
	}
	
	private boolean checkPayeeUsed(String id)
	{
		String[] dbcolumns = { "transactionId" };
		String selection = "payeeId=?";
		Cursor cur;
		
		// Run the query to see if the Payee has been used in any transactions.
		cur = KMMDapp.db.query("kmmSplits", dbcolumns, selection, new String[] { id }, null, null, null);
		startManagingCursor(cur);
		
		if (cur.getCount() > 0)
			return true;			// return payee is in use.
		else
			return false;			// return payee is not used.
	}
	
	private void deletePayee()
	{
		try 
		{
			KMMDapp.db.delete(dbTable, "id=?", new String[] { payeeToDeleteId });
			KMMDapp.updateFileInfo("payees", -1);
			adapter.notifyDataSetInvalidated();
		}
		catch (Exception e)
		{
			AlertDialog.Builder alert = new AlertDialog.Builder(this);
			alert.setTitle(getString(R.string.error));
			alert.setMessage(getString(R.string.unableToDelete) + e.getMessage());

			alert.setPositiveButton(getString(R.string.titleButtonOK), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					finish();
				}
			});
			alert.show();						
		}
	}
}
