package com.vanhlebarsoftware.kmmdroid;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.StringTokenizer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.SQLException;
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
	ArrayList<Split> Splits;
	Spinner spinTransType;
	Spinner spinPayee;
	Spinner spinCategory;
	Spinner spinStatus;
	Button buttonSetDate;
	Button buttonChooseCategory;
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
        buttonSetDate = (Button) findViewById(R.id.buttonSetDate);
        buttonChooseCategory = (Button) findViewById(R.id.buttonChooseCategory);
       
        // Get the action the user is doing.
        Bundle extras = getIntent().getExtras();
        Action = extras.getInt("Action");
        
        if( Action == ACTION_NEW )
        	accountUsed = extras.getString("accountUsed");
                
        if( Action == ACTION_EDIT )
        	transId = extras.getString("transId");
        
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
        
        // Initialize our Splits ArrayList.
        Splits = new ArrayList<Split>();
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
			Cursor transaction = KMMDapp.db.query("kmmTransactions", new String[] { "*" }, "id=?", new String[] { transId }, null, null, null);
			startManagingCursor(transaction);
			transaction.moveToFirst();
			Splits = getSplits(transId);
			
			// load the transaction details into the form.
			editMemo.setText(transaction.getString(T_MEMO));
			convertDate(transaction.getString(T_POSTDATE));
			editCkNumber.setText(Splits.get(0).checkNumber);
			spinPayee.setSelection(setPayee(Splits.get(0).payeeId));
			
			// See if we have only used one category or if we have multiple.
			if( Splits.size() == 2 )
			{
				Cursor c = KMMDapp.db.query("kmmAccounts", new String[] { "accountName" }, "id=?",
											new String[] { Splits.get(1).accountId }, null, null, null);
				startManagingCursor(c);
				c.moveToFirst();
				editCategory.setText(c.getString(0));
				iNumberofPasses = 0;
				intTransStatus = Integer.valueOf(Splits.get(0).reconcileFlag);

				c.close();
			}
			
			float amount = Float.valueOf(Splits.get(0).valueFormatted);
			if( amount < 0 )
			{
				intTransType = WITHDRAW;
				amount = amount * -1;		//change the sign of the amount for the form only.
			}
			else
				intTransType = DEPOSIT;
			editAmount.setText(String.valueOf(amount));
			
			transaction.close();
	        updateDisplay();
		}
		else
			iNumberofPasses = 0;
			
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
				if (Action == ACTION_NEW)
					id = createId();
				else
					id = transId;
				valuesTrans.put("id", id);
				
				// Create the splits information to be entered.
				int numOfSplits = 0;
				if( !anySplits )
					numOfSplits = 2;
				
				for( int i=0; i < numOfSplits; i++)
				{
					String value = null, formatted = null;
					if( (intTransType == 2 && i == 0) || (intTransType == 0 && i > 0) )
					{
						value = "-" + createBalance(editAmount.getText().toString());
						formatted = "-" + editAmount.getText().toString();
					}
					else
					{
						value = createBalance(editAmount.getText().toString());
						formatted = editAmount.getText().toString();
					}
						
					Splits.add(new Split(id, "N", i, strTransPayeeId, "", "", "0", value, formatted, value, formatted,
										 "", "", editMemo.getText().toString(), accountUsed, editCkNumber.getText().toString(),
										 formatDate(transDate.getText().toString()), ""));
					accountUsed = strTransCategoryId;
				}
				switch (Action)
				{
					case ACTION_NEW:
						KMMDapp.db.insertOrThrow("kmmTransactions", null, valuesTrans);
						increaseId();
						// Insert the splits for this transaction
						for(int i=0; i < Splits.size(); i++)
						{
							Split s = Splits.get(i);
							s.dump();
							s.commitSplit(false);
						}
						increaseSplits(Splits.size());
						updateAccount( Splits.get(0).accountId, Splits.get(0).valueFormatted );
						break;
					case ACTION_EDIT:
						KMMDapp.db.update("kmmInstitutions", valuesTrans, "id=?", new String[] { transId });
						break;
				}
				finish();
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
				if( iNumberofPasses < 3 )
					iNumberofPasses = iNumberofPasses + 1;
		}

		public void onNothingSelected(AdapterView<?> arg0) {
			// TODO Auto-generated method stub
			// do nothing.
		}		
	}
	
	private class Split
	{
		private String transactionId;
		private String txType;
		private int splitId;
		private String payeeId;
		private String reconcileDate;
		private String action;
		private String reconcileFlag;
		private String value;
		private String valueFormatted;
		private String shares;
		private String sharesFormatted;
		private String price;
		private String priceFormatted;
		private String memo;
		private String accountId;
		private String checkNumber;
		private String postDate;
		private String bankId;
		
		// Constructor used for creating new Splits.
		Split(String tId, String tType, int sId, String pId, String rDate, String a, String rFlag,
				String v, String vFormatted, String s, String sFormatted, String p, String pFormatted,
				String m, String aId, String ckNumber, String pDate, String bId)
		{
			this.transactionId = tId;
			this.txType = tType;
			this.splitId = sId;
			this.payeeId = pId;
			this.reconcileDate = rDate;
			this.action = a;
			this.reconcileFlag = rFlag;
			this.value = v;
			this.valueFormatted = vFormatted;
			this.shares = s;
			this.sharesFormatted = sFormatted;
			this.price = p;
			this.priceFormatted = pFormatted;
			this.memo = m;
			this.accountId = aId;
			this.checkNumber = ckNumber;
			this.postDate = pDate;
			this.bankId = bId;
		}
		
		public boolean commitSplit(boolean updating)
		{
			// create the ContentValue pairs
			ContentValues valuesSplit = new ContentValues();
			valuesSplit.put("transactionId", transactionId);
			valuesSplit.put("txType", txType);
			valuesSplit.put("splitId", splitId);
			valuesSplit.put("payeeId", payeeId);
			valuesSplit.put("reconcileDate", reconcileDate);
			valuesSplit.put("action", action);
			valuesSplit.put("reconcileFlag", reconcileFlag);
			valuesSplit.put("value", value);
			valuesSplit.put("valueFormatted", valueFormatted);
			valuesSplit.put("shares", shares);
			valuesSplit.put("sharesFormatted", sharesFormatted);
			valuesSplit.put("price", price);
			valuesSplit.put("priceFormatted", priceFormatted);
			valuesSplit.put("memo", memo);
			valuesSplit.put("accountId", accountId);
			valuesSplit.put("checkNumber", checkNumber);
			valuesSplit.put("postDate", postDate);
			valuesSplit.put("bankId", bankId);
			
			if( updating )
			{
				KMMDapp.db.update("kmmSplits", valuesSplit, "transactionId=? AND splitId=?", new String[] { transactionId, String.valueOf(splitId) });
			}
			else
			{
				KMMDapp.db.insertOrThrow("kmmSplits", null, valuesSplit);
			}
			
			return true;
		}
		
	
		public void dump()
		{
			Log.d(TAG, "transactionId: " + transactionId);
			Log.d(TAG, "txType: " + txType);
			Log.d(TAG, "splitId: " + splitId);
			Log.d(TAG, "payeeId: " + payeeId);
			Log.d(TAG, "reconcileDate: " + reconcileDate);
			Log.d(TAG, "value: " + value);
			Log.d(TAG, "valueFormatted: " + valueFormatted);
			Log.d(TAG, "shares: " + shares);
			Log.d(TAG, "sharesFormatted: " + sharesFormatted);
			Log.d(TAG, "price: " + price);
			Log.d(TAG, "priceFormatted: " + priceFormatted);
			Log.d(TAG, "memo: " + memo);
			Log.d(TAG, "accountId: " + accountId);
			Log.d(TAG, "checkNumber: " + checkNumber);
			Log.d(TAG, "postDate: " + postDate);
			Log.d(TAG, "bankId: " + bankId);
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
	
	private void increaseId()
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
	
	private String createBalance(String formattedValue)
	{
		StringTokenizer split = new StringTokenizer(formattedValue, ".");
		String dollars = split.nextToken();
		String cents = split.nextToken();
		String balance = dollars + cents;
		String denominator = "/100";
		return balance + denominator;
	}
	
	private void increaseSplits(int numOfSplits)
	{
		ContentValues values = new ContentValues();
		values.put("splits", numOfSplits);
		
		KMMDapp.db.update("kmmFileInfo", values, null, null);
	}
	
	private void updateAccount( String accountId, String transValue)
	{
		Cursor c = KMMDapp.db.query("kmmAccounts", new String[] { "balanceFormatted" }, "id=?", new String[] { accountId }, null, null, null);
		startManagingCursor(c);
		c.moveToFirst();
		float balance = Float.valueOf(c.getString(0));
		float tValue = Float.valueOf(transValue);
		float newBalance = balance + tValue;

		ContentValues values = new ContentValues();
		values.put("balanceFormatted", String.valueOf(newBalance));
		values.put("balance", createBalance(String.valueOf(newBalance)));
		
		KMMDapp.db.update("kmmAccounts", values, "id=?", new String[] { accountId });
	}
	
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
	
	private int setCategory(String categoryId)
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
}
