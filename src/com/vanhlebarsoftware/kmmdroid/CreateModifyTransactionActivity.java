package com.vanhlebarsoftware.kmmdroid;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.StringTokenizer;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
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
import android.widget.ImageButton;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;

public class CreateModifyTransactionActivity extends Activity
{
	private static final String TAG = "CreateModifyTransactionActivity";
	private static final int ACTION_NEW = 1;
	private static final int ACTION_EDIT = 2;
	private static final int ACTION_ENTER_SCHEDULE = 3;
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
	private static int T_POSTDATE = 2;
	private static int T_MEMO = 3;
	private static int WITHDRAW = 2;
	private static int DEPOSIT = 0;
	private static int TRANSFER = 1;
	static final String[] FROM = { "name" };
	static final int[] TO = { android.R.id.text1 };
	static final String[] FROM1 = { "accountName" };
	static final int SET_DATE_ID = 0;
	private int intYear;
	private int intMonth;
	private int intDay;
	private int intTransType = WITHDRAW;
	private int intTransStatus = 0;
	private String strTransPayeeId = null;
	private String strTransCategoryId = null;		// Only used if we have ONLY one category, use splits if we have more than one.
	private static int iNumberofPasses = 0;
	private int Action = ACTION_NEW;
	private String transId = null;
	private boolean anySplits = false;
	private String accountUsed = null;
	private int numOfSplits;
	private Schedule scheduleToEnter = null;
	ArrayList<Split> Splits;
	ArrayList<Split> OrigSplits;
	Spinner spinTransType;
	Spinner spinPayee;
	Spinner spinCategory;
	Spinner spinStatus;
	ImageButton buttonSetDate;
	Button buttonChooseCategory;
	ImageButton buttonSplits;
	EditText transDate;
	EditText editCategory;
	EditText editMemo;
	EditText editAmount;
	EditText editCkNumber;
	ArrayAdapter<CharSequence> adapterTransTypes;
	ArrayAdapter<CharSequence> adapterStatus;
	Cursor cursorPayees;
	Cursor cursorCategories;
	SimpleCursorAdapter adapterPayees;
	SimpleCursorAdapter adapterCategories;
	boolean fromHomeWidget = false;
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
        editMemo = (EditText) findViewById(R.id.memo);
        editAmount = (EditText) findViewById(R.id.amount);
        editCkNumber = (EditText) findViewById(R.id.checkNumber);
        buttonSetDate = (ImageButton) findViewById(R.id.buttonSetDate);
        buttonChooseCategory = (Button) findViewById(R.id.buttonChooseCategory);
        buttonSplits = (ImageButton) findViewById(R.id.buttonSplit);
        
        // Make sure that the KMMDapp.Splits is empty.
        KMMDapp.splitsDestroy();
        
        // Get the action the user is doing.
        Bundle extras = getIntent().getExtras();
        Log.d(TAG, "Size of extras: " + extras.size());
        Action = extras.getInt("Action");
        fromHomeWidget = extras.getBoolean("fromHome");
        
        if( Action == ACTION_NEW )
        {
        	Log.d(TAG, "From homeWidget: " + String.valueOf(fromHomeWidget));
        	// if we are coming from the home screen widget make sure to open the database used with the actual widget.
        	if( fromHomeWidget )
        	{
        		KMMDapp.setFullPath(KMMDapp.prefs.getString("widgetDatabasePath", ""));
        		KMMDapp.openDB();
        	}
        	accountUsed = extras.getString("accountUsed");
        }
        else if( Action == ACTION_EDIT )
        	transId = extras.getString("transId");
        else if( Action == ACTION_ENTER_SCHEDULE )
        {
        	// Need to get the specified schedule and all it's splits and other information.
        	scheduleToEnter = getSchedule(extras.getString("scheduleId"));
        }
        
        // See if the database is already open, if not open it Read/Write.
        if(!KMMDapp.isDbOpen())
        {
        	KMMDapp.openDB();
        }
        
        // Make it so the user is not able to edit the Category selected without using the Spinner.
        editCategory.setKeyListener(null);
        
        // Set our OnClickListener events
        buttonSetDate.setOnClickListener(new View.OnClickListener() 
        {	
			public void onClick(View v)
			{
				showDialog(SET_DATE_ID);
			}
		});
        
        buttonChooseCategory.setOnClickListener(new View.OnClickListener()
        {			
			public void onClick(View arg0)
			{
				spinCategory.setVisibility(0);
				spinCategory.performClick();
				buttonChooseCategory.setVisibility(4);
			}
		});
        
        buttonSplits.setOnClickListener(new View.OnClickListener()
        {
			public void onClick(View arg0)
			{
				Intent i = new Intent(getBaseContext(), CreateModifySplitsActivity.class);
				i.putExtra("Action", Action);
				i.putExtra("TransAmount", editAmount.getText().toString());
				i.putExtra("Memo", editMemo.getText().toString());
				i.putExtra("CategoryId", strTransCategoryId);
				i.putExtra("transType", intTransType);
				startActivity(i);
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
        
        // display the current date
        updateDisplay();
        
        // Initialize our Splits ArrayList.
        Splits = new ArrayList<Split>();
        OrigSplits = new ArrayList<Split>();
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
		
		if( Action == ACTION_EDIT )
		{
			editTransaction();
		}
		else if( Action == ACTION_ENTER_SCHEDULE )
		{
			enterSchedule();
		}
		else
		{
			iNumberofPasses = 0;
			
			// See if we have any splits from the Split Entry screen.
			if(!KMMDapp.Splits.isEmpty())
			{
				setupSplitInfo();
			}
			else
			{
				editCategory.setText("");
				buttonChooseCategory.setEnabled(true);
				numOfSplits = 2;
				anySplits = false;
			}
		}	
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
				// ensure that the Splits Array is clean before starting.
				Splits.clear();
				
				// create the ContentValue pairs
				ContentValues valuesTrans = new ContentValues();
				valuesTrans.put("txType", "N");
				valuesTrans.put("postDate", formatDate(transDate.getText().toString()));
				valuesTrans.put("memo", editMemo.getText().toString());
		        // get the current date
		        final Calendar c = Calendar.getInstance();
		        valuesTrans.put("entryDate", new StringBuilder()
					// Month is 0 based so add 1
					.append(c.get(Calendar.YEAR)).append("-")
					.append(c.get(Calendar.MONTH) + 1).append("-")
					.append(c.get(Calendar.DAY_OF_MONTH)).toString());
				Cursor C = KMMDapp.db.query("kmmFileInfo", new String[] { "baseCurrency" }, null, null, null, null, null);
				C.moveToFirst();
				valuesTrans.put("currencyId", C.getString(0));
				valuesTrans.put("bankId", "");
				String id = null;
				if (Action == ACTION_NEW || Action == ACTION_ENTER_SCHEDULE)
					id = createId();
				else
					id = transId;
				valuesTrans.put("id", id);
				
				// Create the splits information to be saved.			
				for( int i=0; i < numOfSplits; i++)
				{
					String value = null, formatted = null, memo = null;
					if(i == 0)
					{
						if( intTransType == WITHDRAW )
						{
							value = "-" + Account.createBalance(Transaction.convertToPennies(editAmount.getText().toString()));
							formatted = "-" + editAmount.getText().toString();
						}
						else
						{
							value = Account.createBalance(Transaction.convertToPennies(editAmount.getText().toString()));
							formatted = editAmount.getText().toString();							
						}
						memo = editMemo.getText().toString();
					}
					else
					{
						// If we have splits grab the relevant information from the KMMDapp.Splits object.
						if( anySplits )
						{
							value = Account.createBalance(Transaction.convertToPennies(KMMDapp.Splits.get(i-1).getValueFormatted()));
							formatted = KMMDapp.Splits.get(i-1).getValueFormatted();
							memo = KMMDapp.Splits.get(i-1).getMemo();
							accountUsed = KMMDapp.Splits.get(i-1).getAccountId();
						}
						else
						{
							if( intTransType == WITHDRAW )
							{
								value = Account.createBalance(Transaction.convertToPennies(editAmount.getText().toString()));
								formatted = editAmount.getText().toString();								
							}
							else
							{
								value = "-" + Account.createBalance(Transaction.convertToPennies(editAmount.getText().toString()));
								formatted = "-" + editAmount.getText().toString();								
							}
							memo = editMemo.getText().toString();
							accountUsed = strTransCategoryId;
						}
					}
					// Create the actual split for the transaction to be saved.
					Splits.add(new Split(id, "N", i, strTransPayeeId, "", "", "0", value, formatted, value, formatted,
										 "", "", memo, accountUsed, editCkNumber.getText().toString(),
										 formatDate(transDate.getText().toString()), ""));
				}
				switch (Action)
				{
					case ACTION_NEW:
						KMMDapp.db.insertOrThrow("kmmTransactions", null, valuesTrans);
						KMMDapp.updateFileInfo("hiTransactionId", 1);
						KMMDapp.updateFileInfo("transactions", 1);
						KMMDapp.updateFileInfo("splits", Splits.size());
						break;
					case ACTION_EDIT:
						KMMDapp.db.update("kmmTransactions", valuesTrans, "id=?", new String[] { transId });
						// Delete all the splits for this transaction first, getting the number or rows deleted.
						int rowsDel = KMMDapp.db.delete("kmmSplits", "transactionId=?", new String[] { transId });
						KMMDapp.updateFileInfo("splits", Splits.size() - rowsDel);
						// Need to update the account by pulling out all the Original Splits information.
						for(int i=0; i < OrigSplits.size(); i++)
							Account.updateAccount(KMMDapp.db, OrigSplits.get(i).getAccountId(), OrigSplits.get(i).getValueFormatted(), -1);
						break;
					case ACTION_ENTER_SCHEDULE:
						KMMDapp.db.insertOrThrow("kmmTransactions", null, valuesTrans);
						KMMDapp.updateFileInfo("hiTransactionId", 1);
						KMMDapp.updateFileInfo("transactions", 1);
						KMMDapp.updateFileInfo("splits", Splits.size());
						//Need to advance the schedule to the next date and update the lastPayment and startDate dates to the recorded date of the transaction.
						scheduleToEnter.advanceDueDate(Schedule.getOccurence(scheduleToEnter.getOccurence(), scheduleToEnter.getOccurenceMultiplier()));
						ContentValues values = new ContentValues();
						values.put("nextPaymentDue", scheduleToEnter.getDatabaseFormattedString());
						values.put("startDate", scheduleToEnter.getDatabaseFormattedString());
						values.put("lastPayment", formatDate(transDate.getText().toString()));
						KMMDapp.db.update("kmmSchedules", values, "id=?", new String[] { scheduleToEnter.getId() });
						//Need to update the schedules splits in the kmmsplits table as this is where the upcoming bills in deskktop comes from.
						for(int i=0; i < scheduleToEnter.Splits.size(); i++)
						{
							Split s = scheduleToEnter.Splits.get(i);
							s.setPostDate(scheduleToEnter.getDatabaseFormattedString());
							s.commitSplit(true, KMMDapp.db);
						}	
						//Need to update the schedule in kmmTransactions postDate to match the splits and the actual schdule for the next payment due date.
						values.clear();
						values.put("postDate", scheduleToEnter.getDatabaseFormattedString());
						KMMDapp.db.update("kmmTransactions", values, "id=?", new String[] { scheduleToEnter.getId() });						
						break;
				}		
				// Insert the splits for this transaction
				for(int i=0; i < Splits.size(); i++)
				{
					Split s = Splits.get(i);
					s.commitSplit(false, KMMDapp.db);
					Account.updateAccount(KMMDapp.db, s.getAccountId(), s.getValueFormatted(), 1);
				}
				KMMDapp.updateFileInfo("lastModified", 0);
				// Need to clean up the OrigSplits and Splits arrays for future use.
				Splits.clear();
				OrigSplits.clear();
				// If the user has the preference item of updateFrequency = Auto fire off a Broadcast
				if(KMMDapp.getAutoUpdate())
				{
					Intent intent = new Intent(KMMDService.DATA_CHANGED);
					sendBroadcast(intent, KMMDService.RECEIVE_HOME_UPDATE_NOTIFICATIONS);
				}
				// need to close the database as it is keeping it open here and causing issues.
				//KMMDapp.closeDB();
				// If we are coming from the home widget, we need to close the db.
				if( fromHomeWidget )
					KMMDapp.closeDB();
				finish();
				break;
			case R.id.itemCancel:
				KMMDapp.splitsDestroy();
				Log.d(TAG, "CreateModifyTransactionActivity itemCancelled!");
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
			if( iNumberofPasses > 3 )
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
						Cursor c = (Cursor) parent.getAdapter().getItem(pos);
						strTransPayeeId = c.getString(1).toString();
						Log.d(TAG, "Inside payee: " + strTransPayeeId);
						break;
					case R.id.category:
						Log.d(TAG, "Inside category: " + String.valueOf(parent.getId()));
						c = (Cursor) parent.getAdapter().getItem(pos);
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
				if( iNumberofPasses < 4 )
					iNumberofPasses = iNumberofPasses + 1;
		}

		public void onNothingSelected(AdapterView<?> arg0) {
			// do nothing.
		}		
	}
	
	// **************************************************************************************************
	// ************************************ Helper methods **********************************************
	private String createId()
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
	
	private void updateDisplay()
	{
		String strDay = null;
		String strMonth = null;
		switch(intDay)
		{
			case 0:
			case 1:
			case 2:
			case 3:
			case 4:
			case 5:
			case 6:
			case 7:
			case 8:
			case 9:
				strDay = "0" + String.valueOf(intDay);
				break;
			default:
				strDay = String.valueOf(intDay);
			break;
		}
		
		switch(intMonth)
		{
			case 0:
			case 1:
			case 2:
			case 3:
			case 4:
			case 5:
			case 6:
			case 7:
			case 8:
			case 9:
				strMonth = "0" + String.valueOf(intMonth + 1);
				break;
			default:
				strMonth = String.valueOf(intMonth + 1);
				break;
		}
		
		transDate.setText(
				new StringBuilder()
					// Month is 0 based so add 1
					.append(strMonth).append("-")
					.append(strDay).append("-")
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
	
/*	private void increaseId()
	{
		final String[] dbColumns = { "hiTransactionId" };
		final String strOrderBy = "hiTransactionId DESC";
		
		Cursor cursor = KMMDapp.db.query("kmmFileInfo", dbColumns, null, null, null, null, strOrderBy);
		startManagingCursor(cursor);
		cursor.moveToFirst();
		
		int id = cursor.getInt(0);
		id = id + 1;
		
		ContentValues values = new ContentValues();
		values.put("hiTransactionId", id);
		values.put("transactions", id);
		
		KMMDapp.db.update("kmmFileInfo", values, null, null);		
	}
*/	
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
								 cursor.getString(C_POSTDATE), cursor.getString(C_BANKID)) );
			cursor.moveToNext();
		}
		
		cursor.close();
		return splits;
	}
	
	private void convertDate(String date)
	{
		date = date.trim();
		String dates[] = date.split("-");
		
		// Date was stored YYYY-MM-DD
		intYear = Integer.valueOf(dates[0]);
		// Since updateDisplay uses a zero based month we need to subract one now.
		intMonth = Integer.valueOf(dates[1]) - 1;
		intDay = Integer.valueOf(dates[2]);		
	}
	
	private int setPayee(String payeeId)
	{
		int i = 0;
		cursorPayees.moveToFirst();
		
		if( payeeId != null )
		{
			while(!payeeId.equals(cursorPayees.getString(1)))
			{
				cursorPayees.moveToNext();
			
				//check to see if we have moved past the last item in the cursor, if so return current i.
				if(cursorPayees.isAfterLast())
					return i;
			
				i++;
			}
		}
		return i;
	}
	
/*	private int setCategory(String categoryId)
	{
		int i = 0;
		cursorCategories.moveToFirst();
		
		if( categoryId != null )
		{
			while(!categoryId.equals(cursorCategories.getString(1)))
			{
				cursorCategories.moveToNext();
			
				//check to see if we have moved past the last item in the cursor, if so return current i.
				if(cursorCategories.isAfterLast())
					return i;
			
				i++;
			}
		}
		return i;
	}
*/	
	private void setupSplitInfo()
	{
		// need to take into account the actual accounts entry as well as the splits.
		numOfSplits = KMMDapp.Splits.size() + 1;
		anySplits = true;
		if( KMMDapp.flSplitsTotal != 0  )
		{
			long tmp = 0;
			if( KMMDapp.flSplitsTotal < 0 )
				tmp = KMMDapp.flSplitsTotal * -1;
			else
				tmp = KMMDapp.flSplitsTotal;
			editAmount.setText(Transaction.convertToDollars((tmp)));
		}
		
		// Clear the Splits ArrayList out.
		Splits.clear();
	}
	
	private Schedule getSchedule(String schId)
	{
		Cursor schedule = KMMDapp.db.query("kmmschedules",new String[] { "*" }, "id=?", new String[] { schId }, null, null, null);
		Cursor splits = KMMDapp.db.query("kmmsplits", new String[] { "*" }, "transactionId=?", new String[] { schId }, null, null, "splitId");

		return new Schedule(schedule, splits);
	}
	
	private void editTransaction()
	{
		// If we are coming back from splits entry screen follow this path.
		if(!KMMDapp.Splits.isEmpty())
		{
			setupSplitInfo();
			editCategory.setText(R.string.splitTransaction);
			buttonChooseCategory.setEnabled(false);
			spinPayee.setSelection(setPayee(strTransPayeeId));
			iNumberofPasses = 0;
			Log.d(TAG, "OnResume Action == ACTION_EDIT");
		}
		else
		{
			// if we are not coming back from splits entry screen follow this path.
			Cursor transaction = KMMDapp.db.query("kmmTransactions", new String[] { "*" }, "id=?", new String[] { transId }, null, null, null);
			startManagingCursor(transaction);
			transaction.moveToFirst();
			Splits = getSplits(transId);
		
			// load the transaction details into the form.
			editMemo.setText(transaction.getString(T_MEMO));
			convertDate(transaction.getString(T_POSTDATE));
			editCkNumber.setText(Splits.get(0).getCheckNumber());
			strTransPayeeId = Splits.get(0).getPayeeId();
			spinPayee.setSelection(setPayee(strTransPayeeId));
		
			// See if we have only used one category or if we have multiple.
			if( Splits.size() == 2 )
			{
				Cursor c = KMMDapp.db.query("kmmAccounts", new String[] { "accountName" }, "id=?",
										new String[] { Splits.get(1).getAccountId() }, null, null, null);
				startManagingCursor(c);
				c.moveToFirst();
				editCategory.setText(c.getString(0));
				iNumberofPasses = 0;
				intTransStatus = Integer.valueOf(Splits.get(0).getReconcileFlag());

				c.close();
				numOfSplits = 2;
				anySplits = false;
				
				// Populate the category used for this split only.
				strTransCategoryId = Splits.get(1).getAccountId();
			}
			else
			{
				// need to put the splits into the KMMDapp.Splits object so user may edit the split details.
				KMMDapp.splitsInit();
				for(int i = 1; i < Splits.size(); i++)
					KMMDapp.Splits.add(Splits.get(i));
			
				iNumberofPasses = 0;
				editCategory.setText(R.string.splitTransaction);
				buttonChooseCategory.setEnabled(false);
				numOfSplits = Splits.size();
				anySplits = true;
			}
		
			float amount = Float.valueOf(Splits.get(0).getValueFormatted());
			if( amount < 0 )
			{
				intTransType = WITHDRAW;
				amount = amount * -1;		//change the sign of the amount for the form only.
			}
			else
				intTransType = DEPOSIT;
			
			editAmount.setText(String.valueOf(amount));
			
			// Need to populate the Account used for this transaction.
			accountUsed = Splits.get(0).getAccountId();
		
			// Make a copy of the original transactions split for later use if we modify anything.
			for(int i=0; i < Splits.size(); i++)
				OrigSplits.add(Splits.get(i));
			
			transaction.close();
			updateDisplay();
		}
	}
	
	private void enterSchedule()
	{
		// If we are coming back from splits entry screen follow this path.
		if(!KMMDapp.Splits.isEmpty())
		{
			setupSplitInfo();
			editCategory.setText(R.string.splitTransaction);
			buttonChooseCategory.setEnabled(false);
			spinPayee.setSelection(setPayee(strTransPayeeId));
			iNumberofPasses = 0;
			Log.d(TAG, "OnResume Action == ACTION_ENTER_SCHEDULE");
		}
		else
		{	
			// load the transaction details into the form.
			editMemo.setText(scheduleToEnter.Splits.get(0).getMemo());
			convertDate(scheduleToEnter.getDatabaseFormattedString());
			editCkNumber.setText(scheduleToEnter.Splits.get(0).getCheckNumber());
			strTransPayeeId = scheduleToEnter.Splits.get(0).getPayeeId();
			spinPayee.setSelection(setPayee(strTransPayeeId));
		
			// See if we have only used one category or if we have multiple.
			if( scheduleToEnter.Splits.size() == 2 )
			{
				Cursor c = KMMDapp.db.query("kmmAccounts", new String[] { "accountName" }, "id=?",
										new String[] { scheduleToEnter.Splits.get(1).getAccountId() }, null, null, null);
				startManagingCursor(c);
				c.moveToFirst();
				editCategory.setText(c.getString(0));
				iNumberofPasses = 0;
				intTransStatus = Integer.valueOf(scheduleToEnter.Splits.get(0).getReconcileFlag());

				c.close();
				numOfSplits = 2;
				anySplits = false;
				
				// Populate the category used for this split only.
				strTransCategoryId = scheduleToEnter.Splits.get(1).getAccountId();
			}
			else
			{
				// need to put the splits into the KMMDapp.Splits object so user may edit the split details.
				KMMDapp.splitsInit();
				for(int i = 1; i < scheduleToEnter.Splits.size(); i++)
					KMMDapp.Splits.add(scheduleToEnter.Splits.get(i));
			
				for(int i = 0; i < KMMDapp.Splits.size(); i++)
					 KMMDapp.Splits.get(i).dump();
				iNumberofPasses = 0;
				editCategory.setText(R.string.splitTransaction);
				buttonChooseCategory.setEnabled(false);
				numOfSplits = scheduleToEnter.Splits.size();
				anySplits = true;
			}
		
			float amount = Float.valueOf(scheduleToEnter.Splits.get(0).getValueFormatted());
			if( amount < 0 )
			{
				intTransType = WITHDRAW;
				amount = amount * -1;		//change the sign of the amount for the form only.
			}
			else
				intTransType = DEPOSIT;
			
			editAmount.setText(String.valueOf(amount));
			
			// Need to populate the Account used for this transaction.
			accountUsed = scheduleToEnter.Splits.get(0).getAccountId();
		
			// Make a copy of the original transactions split for later use if we modify anything.
			for(int i=0; i < scheduleToEnter.Splits.size(); i++)
				OrigSplits.add(scheduleToEnter.Splits.get(i));
			
			updateDisplay();
		}		
	}
}
