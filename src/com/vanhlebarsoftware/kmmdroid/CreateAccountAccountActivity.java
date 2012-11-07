package com.vanhlebarsoftware.kmmdroid;

import java.util.Calendar;

import com.vanhlebarsoftware.kmmdroid.PayeeDefaultAccountActivity.PayeeDefaultOnItemSelectedListener;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.TextView;

public class CreateAccountAccountActivity extends Activity implements OnCheckedChangeListener
{
	private static final String TAG = "CreateAccountAccountActivity";
	static final String[] FROM = { "name" };
	static final int[] TO = { android.R.id.text1 };
	static final int SET_DATE_ID = 0;
	private static final int A_CHECKING = 1;
	private static final int A_SAVINGS = 2;
	private static final int A_CREDITCARD = 4;
	private static final int A_LOAN = 5;
	private static final int A_INVESTMENT = 7;
	private static final int A_ASSET = 9;
	private static final int A_LIABILITY = 10;
	private static final int A_EQUITY = 16;
	private int intYear;
	private int intMonth;
	private int intDay;
	private int TypeSelected = 0;
	private int numberOfPasses = 0;
	private String strTypeSelected = null;
	private String currencySelected = null;
	private CreateModifyAccountActivity parentTabHost;
	Button buttonDate;
	EditText accountName;
	EditText openDate;
	EditText openBalance;
	Spinner spinType;
	Spinner spinCurrency;
	CheckBox checkPreferred;
	TextView txtTotTrans;
	Cursor cursorCurrency;
	SimpleCursorAdapter adapterCurrency;
	ArrayAdapter<CharSequence> adapterTypes;
	KMMDroidApp KMMDapp;
	
	@Override
    public void onCreate(Bundle savedInstanceState) 
	{
        super.onCreate(savedInstanceState);
        setContentView(R.layout.createaccount_account);
        
        // Get our application
        KMMDapp = ((KMMDroidApp) getApplication());
        
        // Get the activity for the tabHost.
        parentTabHost = ((CreateModifyAccountActivity) this.getParent());
        
        // Find our views
        spinCurrency = (Spinner) findViewById(R.id.accountCurrency);
        spinType = (Spinner) findViewById(R.id.accountType);
        accountName = (EditText) findViewById(R.id.accountName);
        openDate = (EditText) findViewById(R.id.accountOpenDate);
        openBalance = (EditText) findViewById(R.id.accountOpenBalance);
        checkPreferred = (CheckBox) findViewById(R.id.checkboxAccountPreferred);
        buttonDate = (Button) findViewById(R.id.buttonSetDate);
        txtTotTrans = (TextView) findViewById(R.id.titleAccountTransactions);
        
        // Set our OnClickListener events
        buttonDate.setOnClickListener(new View.OnClickListener() 
        {	
			public void onClick(View v)
			{
				showDialog(SET_DATE_ID);
				parentTabHost.setIsDirty(true);
			}
		});
        
        // Set the OnItemSelectedListeners for the spinners.
        spinType.setOnItemSelectedListener(new AccountOnItemSelectedListener());
        spinCurrency.setOnItemSelectedListener(new AccountOnItemSelectedListener());
        
        // Set up the other keyListener's for the various editText items.
        accountName.addTextChangedListener(new TextWatcher()
        {

			public void afterTextChanged(Editable s) 
			{
				parentTabHost.setIsDirty(true);
				Log.d(TAG, "changing isDirty from accountName!");
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {}
        });
        
        openBalance.addTextChangedListener(new TextWatcher()
        {

			public void afterTextChanged(Editable s) 
			{
				parentTabHost.setIsDirty(true);
				Log.d(TAG, "changing isDirty from openBalance!");
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {}
        });
        
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
        
        // Set the base currency from the file.
        currencySelected = getBaseCurrency();
        
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
		
		//Get all the currencies to be displayed.
		cursorCurrency = KMMDapp.db.query("kmmCurrencies", new String[] { "ISOCode AS _id", "name" }, null, null, null, null, null);
		startManagingCursor(cursorCurrency);
		
		// Set up the adapters
		adapterCurrency = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, cursorCurrency, FROM, TO);
		adapterCurrency.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
		spinCurrency.setAdapter(adapterCurrency);
		adapterTypes = ArrayAdapter.createFromResource(this, R.array.arrayAccountTypes, android.R.layout.simple_spinner_item);
		adapterTypes.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
		spinType.setAdapter(adapterTypes);
		
		// Set the account type and currency type selected.
		spinType.setSelection(TypeSelected);
		spinCurrency.setSelection(setCurrency(currencySelected));
		
		// Update the date EditBox.
		updateDisplay();
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
			if( numberOfPasses > 1)
			{
				switch( parent.getId())
				{
					case R.id.accountType:
						strTypeSelected = parent.getAdapter().getItem(pos).toString();
						Log.d(TAG, "itemSelected: " + strTypeSelected);
			
						if( strTypeSelected.matches(getString(R.string.Asset)) )
						{	
							CreateAccountParentActivity.setSelected("id='AStd::Asset' OR (parentId='AStd::Asset'" +
								" AND balance !='0/1')");
							TypeSelected = 0;
						}
						else if( strTypeSelected.matches(getString(R.string.Checking)) )
						{
							CreateAccountParentActivity.setSelected("id='AStd::Asset' OR (parentId='AStd::Asset'" +
									" AND balance !='0/1')");
							TypeSelected = 1;
						}
						else if( strTypeSelected.matches(getString(R.string.Equity)) )
						{
							CreateAccountParentActivity.setSelected("id='AStd::Equity' OR (parentId='AStd::Equity'" +
									" AND balance !='0/1')");
							TypeSelected = 2;
						}
						else if( strTypeSelected.matches(getString(R.string.Liability)) )
						{	
							CreateAccountParentActivity.setSelected("id='AStd::Liability' OR (parentId='AStd::Liability'" +
								" AND balance !='0/1')");
							TypeSelected = 3;
						}
						else if( strTypeSelected.matches(getString(R.string.Savings)) )
						{
							CreateAccountParentActivity.setSelected("id='AStd::Asset' OR (parentId='AStd::Asset'" +
								" AND balance !='0/1')");
							TypeSelected = 4;
						}
						else
							Log.d(TAG, "ERROR!!!");
						Log.d(TAG, "Number of passes: " + numberOfPasses);
						parentTabHost.setIsDirty(true);
						break;
					case R.id.accountCurrency:
						Cursor c = (Cursor) parent.getAdapter().getItem(pos);
						currencySelected = c.getString(0);
						Log.d(TAG, "Number of passes: " + numberOfPasses);
						parentTabHost.setIsDirty(true);
						Log.d(TAG, "currencyId: " + currencySelected);
						break;
				}
			}
			else
			{
				numberOfPasses++;
				Log.d(TAG, "Number of passes: " + numberOfPasses);
			}
		}

		public void onNothingSelected(AdapterView<?> arg0) {
			// do nothing.
		}		
	}


	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
	{
		switch( buttonView.getId() )
		{
			case R.id.checkboxAccountPreferred:
				parentTabHost.setIsDirty(true);
				break;
		}
	}
	
	@Override
	public void onBackPressed()
	{
		Log.d(TAG, "User clicked the back button");
		if( parentTabHost.getIsDirty() )
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
					Log.d(TAG, "User cancelled back action.");
				}
			});				
			alertDel.show();
		}
		else
		{
			finish();
		}
	}
	
	// **************************************************************************************************
	// ************************************ Helper methods **********************************************
	
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
			//case 9:
				strMonth = "0" + String.valueOf(intMonth + 1);
				break;
			default:
				strMonth = String.valueOf(intMonth + 1);
				break;
		}
		
		openDate.setText(
				new StringBuilder()
					// Month is 0 based so add 1
					.append(strMonth).append("-")
					.append(strDay).append("-")
					.append(intYear));
	}
	
	private String getBaseCurrency()
	{
		Cursor c = KMMDapp.db.query("kmmFileInfo", new String[] { "baseCurrency" }, null, null, null, null, null);
		c.moveToFirst();
		String currency = c.getString(0);
		
		return currency;
	}
	
	private int setCurrency(String baseCur)
	{
		int i = 0;
		cursorCurrency.moveToFirst();
		
		if( baseCur != null )
		{
			while(!baseCur.equals(cursorCurrency.getString(0)))
			{
				cursorCurrency.moveToNext();
			
				//check to see if we have moved past the last item in the cursor, if so return current i.
				if(cursorCurrency.isAfterLast())
					return i;
			
				i++;
			}
		}
		return i;
	}
	
	public String getAccountName()
	{
		return accountName.getText().toString();
	}
	
	public int getAccountType()
	{
		switch ( TypeSelected )
		{
			case 0:
				return 9;
			case 1:
				return 1;
			case 2:
				return 16;
			case 3:
				return 10;
			case 4:
				return 2;
			default:
				return 0;
		}
	}
	
	public String getAccountTypeString()
	{
		return strTypeSelected;
	}
	
	public String getCurrency()
	{
		return currencySelected;
	}
	
	public String getOpeningDate()
	{
		// We need to reverse the order of the date to be YYYY-MM-DD for SQL
		String tmp = openDate.getText().toString();
		String dates[] = tmp.split("-");
		
		//tmp = String.valueOf(dates[2]) + "-" + String.valueOf(dates[0]) + "-" + String.valueOf(dates[1]);
		return new StringBuilder()
		.append(dates[2]).append("-")
		.append(dates[0]).append("-")
		.append(dates[1]).toString();
	}
	
	public String getOpeningBalance()
	{
		return openBalance.getText().toString();
	}
	
	public boolean getPreferredAccount()
	{
		return checkPreferred.isChecked();
	}
	
	public void putAccountName(String name)
	{
		accountName.setText(name);
	}
	
	public void putAccountType(int type)
	{
		switch ( type )
		{
			case 1:
				TypeSelected = 1;
				break;
			case 2:
				TypeSelected = 4;
				break;
			case 9:
				TypeSelected = 0;
				break;
			case 10:
				TypeSelected = 3;
				break;
			case 16:
				TypeSelected = 2;
				break;
		}
	}
	
	public void putAccountTypeString(String type)
	{
		strTypeSelected = type;
	}
	
	public void putCurrency(String currency)
	{
		currencySelected = currency;
	}
	
	public void putOpeningDate(String date)
	{
		Log.d(TAG, "putOpeningDate date: " + date);
		date = date.trim();
		String dates[] = date.split("-");
		
		// Date was stored YYYY-MM-DD
		intYear = Integer.valueOf(dates[0]);
		// Since updateDisplay uses a zero based month we need to subract one now.
		intMonth = Integer.valueOf(dates[1]) - 1;
		intDay = Integer.valueOf(dates[2]);		
	}
	
	public void putOpeningBalance(String balance)
	{
		openBalance.setText(balance);
	}
	
	public void putPreferredAccount(boolean preferred)
	{
		checkPreferred.setChecked(preferred);
	}
	
	public void putTransactionCount(String strCount)
	{
		txtTotTrans.setText(txtTotTrans.getText().toString() + " " + strCount);
	}
}
