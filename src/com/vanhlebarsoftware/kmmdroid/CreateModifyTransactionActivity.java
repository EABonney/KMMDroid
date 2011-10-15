package com.vanhlebarsoftware.kmmdroid;

import java.util.Calendar;

import com.vanhlebarsoftware.kmmdroid.CreateAccountAccountActivity.AccountOnItemSelectedListener;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;

public class CreateModifyTransactionActivity extends Activity
{
	private static final String TAG = "CreateModifyTransactionActivity";
	private static final int ACTION_NEW = 1;
	private static final int ACTION_EDIT = 2;
	static final String[] FROM = { "name" };
	static final int[] TO = { android.R.id.text1 };
	static final String[] FROM1 = { "accountName" };
	static final int SET_DATE_ID = 0;
	private int intYear;
	private int intMonth;
	private int intDay;
	private int intTransType = 2;
	private int intTransStatus = 2;
	private String strTransPayeeId = null;
	private String strTransCategoryId = null;		// Only used if we have ONLY one category, use splits if we have more than one.
	private static int iNumberofPasses = 0;
	private int Action = ACTION_NEW;
	Spinner spinTransType;
	Spinner spinPayee;
	Spinner spinCategory;
	Spinner spinStatus;
	Button buttonSetDate;
	Button buttonChooseCategory;
	EditText transDate;
	EditText editCategory;
	ArrayAdapter<CharSequence> adapterTransTypes;
	ArrayAdapter<CharSequence> adapterStatus;
	Cursor cursorPayees;
	Cursor cursorCategories;
	SimpleCursorAdapter adapterPayees;
	SimpleCursorAdapter adapterCategories;
	KMMDroidApp KMMDapp;
	
	/* Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
        setContentView(R.layout.createmod_transaction);
        
        // Get our application
        KMMDapp = ((KMMDroidApp) getApplication());
        
        // Find our views
        spinTransType = (Spinner) findViewById(R.id.transactionType);
        spinPayee = (Spinner) findViewById(R.id.payee);
        spinCategory = (Spinner) findViewById(R.id.category);
        spinStatus = (Spinner) findViewById(R.id.status);
        transDate = (EditText) findViewById(R.id.date);
        editCategory = (EditText) findViewById(R.id.editCategory);
        buttonSetDate = (Button) findViewById(R.id.buttonSetDate);
        buttonChooseCategory = (Button) findViewById(R.id.buttonChooseCategory);
       
        // Make it so the user is not able to edit the Category selected without using the Spinner.
        editCategory.setKeyListener(null);
        // Set our OnClickListener events
        buttonSetDate.setOnClickListener(new View.OnClickListener() 
        {	
			public void onClick(View v)
			{
				// TODO Auto-generated method stub
				showDialog(SET_DATE_ID);
			}
		});
        
        buttonChooseCategory.setOnClickListener(new View.OnClickListener()
        {			
			public void onClick(View arg0)
			{
				// TODO Auto-generated method stub
				spinCategory.setVisibility(0);
				spinCategory.performClick();
				buttonChooseCategory.setVisibility(4);
			}
		});
        
        // Set the OnItemSelectedListeners for the spinners.
        spinTransType.setOnItemSelectedListener(new AccountOnItemSelectedListener());
        spinPayee.setOnItemSelectedListener(new AccountOnItemSelectedListener());
        spinCategory.setOnItemSelectedListener(new AccountOnItemSelectedListener());
        spinStatus.setOnItemSelectedListener(new AccountOnItemSelectedListener());
        
        // get the current date
        final Calendar c = Calendar.getInstance();
        intYear = c.get(Calendar.YEAR);
        intMonth = c.get(Calendar.MONTH);
        intDay = c.get(Calendar.DAY_OF_MONTH);
        
        // See if the database is already open, if not open it Read/Write.
        if(!KMMDapp.isDbOpen())
        {
        	KMMDapp.openDB();
        }
        
        // display the current date
        updateDisplay();
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

		cursorPayees = KMMDapp.db.query("kmmpayees", new String[] { "name", "id AS _id" }, 
				null, null, null, null, "name ASC");
		startManagingCursor(cursorPayees);
		cursorCategories = KMMDapp.db.query("kmmAccounts", new String[] { "accountName", "id AS _id" },
				"(accountTypeString='Expense' OR accountTypeString='Income')", null, null, null, "accountName ASC");
		startManagingCursor(cursorCategories);
		
		// Set up the adapters
		adapterPayees = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, cursorPayees, FROM, TO);
		adapterPayees.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
		spinPayee.setAdapter(adapterPayees);
		adapterCategories = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, cursorCategories, FROM1, TO);
		adapterCategories.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
		spinCategory.setAdapter(adapterCategories);
		adapterTransTypes = ArrayAdapter.createFromResource(this, R.array.TransactionTypes, android.R.layout.simple_spinner_item);
		adapterTransTypes.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
		spinTransType.setAdapter(adapterTransTypes);
		adapterStatus = ArrayAdapter.createFromResource(this, R.array.TransactionStatus, android.R.layout.simple_spinner_item);
		adapterStatus.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
		spinStatus.setAdapter(adapterStatus);
		
		// Set the default items for the type and status spinners.
		spinTransType.setSelection(intTransType);
		spinStatus.setSelection(intTransStatus);
	}
	
	// Called first time the user clicks on the menu button
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.save_menu, menu);
		return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu (Menu menu)
	{
		int rows = 0;
		
		// Can't delete if we are creating a new item.
		if( Action == ACTION_NEW )
			menu.getItem(1).setVisible(false);
		
		return true;
	}
	
	// Called when an options item is clicked
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.itemsave:
				// create the ContentValue pairs
				ContentValues valuesTrans = new ContentValues();
				valuesTrans.put("txType", "N");
				valuesTrans.put("postDate", formatDate(transDate.getText().toString()));
				break;
			case R.id.itemDelete:
				break;
			case R.id.itemCancel:
				finish();
				break;
		}
		return true;
	}
	// the callback received with the user "sets" the opening date in the dialog
	private DatePickerDialog.OnDateSetListener mDateSetListener = 
			new DatePickerDialog.OnDateSetListener() 
			{				
				public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) 
				{
					// TODO Auto-generated method stub
					intYear = year;
					intMonth = monthOfYear;
					intDay = dayOfMonth;
					updateDisplay();
				}
			};
			
	@Override
	protected Dialog onCreateDialog(int id)
	{
		switch(id)
		{
			case SET_DATE_ID:
				return new DatePickerDialog(this, mDateSetListener, intYear, intMonth, intDay);
		}
		return null;
	}
	
	public class AccountOnItemSelectedListener implements OnItemSelectedListener
	{
		public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
		{
			Log.d(TAG, "Inside onItemSelected");
			if( iNumberofPasses > 2 )
			{
				switch( parent.getId())
				{
					case R.id.transactionType:
						Log.d(TAG, "Inside transactionType: " + String.valueOf(parent.getId()));
						String str = parent.getAdapter().getItem(pos).toString();
						if( str.matches("Deposit") )
							intTransType = 0;
						if( str.matches("Transfer") )
							intTransType = 1;
						if( str.matches("Withdrawal") )
							intTransType = 2;
						break;
					case R.id.payee:
						Log.d(TAG, "Inside payee: " + String.valueOf(parent.getId()));
						break;
					case R.id.category:
						Log.d(TAG, "Inside category: " + String.valueOf(parent.getId()));
						Cursor c = (Cursor) parent.getAdapter().getItem(pos);
						editCategory.setText(c.getString(0).toString());
						strTransCategoryId = c.getString(1).toString();
						spinCategory.setVisibility(4);
						buttonChooseCategory.setVisibility(0);
						break;
					case R.id.status:
						Log.d(TAG, "Inside status: " + String.valueOf(parent.getId()));
						str = parent.getAdapter().getItem(pos).toString();
						if( str.matches( "Reconciled" ) )
							intTransStatus = 0;
						if( str.matches( "Cleared" ) )
							intTransStatus = 1;
						if( str.matches( "Not reconciled" ) )
							intTransStatus = 2;
						break;
					default:
						Log.d(TAG, "parentId: " + String.valueOf(parent.getId()));
						break;
				}
			}
				if( iNumberofPasses < 3 )
					iNumberofPasses = iNumberofPasses + 1;
		}

		public void onNothingSelected(AdapterView<?> arg0) {
			// TODO Auto-generated method stub
			// do nothing.
		}		
	}
	// **************************************************************************************************
	// ************************************ Helper methods **********************************************
	private String createId()
	{
		final String[] dbColumns = { "hiTransactionId"};
		final String strOrderBy = "hiTransaction DESC";
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
	
	private void updateDisplay()
	{
		transDate.setText(
				new StringBuilder()
					// Month is 0 based so add 1
					.append(intMonth + 1).append("-")
					.append(intDay).append("-")
					.append(intYear));
	}
	
	private String formatDate(String date)
	{
		// We need to reverse the order of the date to be YYYY-MM-DD for SQL
		String dates[] = date.split("-");
		
		//tmp = String.valueOf(dates[2]) + "-" + String.valueOf(dates[0]) + "-" + String.valueOf(dates[1]);
		return new StringBuilder()
		.append(dates[2]).append("-")
		.append(dates[0]).append("-")
		.append(dates[1]).toString();
	}
}
