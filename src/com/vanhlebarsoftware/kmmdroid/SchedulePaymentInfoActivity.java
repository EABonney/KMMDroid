package com.vanhlebarsoftware.kmmdroid;

import java.util.ArrayList;
import java.util.Calendar;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
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

public class SchedulePaymentInfoActivity extends Activity 
{
	private static final String TAG = SchedulePaymentInfoActivity.class.getSimpleName();
	private static final int ACTION_NEW = 1;
	private static final int ACTION_EDIT = 2;
	static final String[] FROM = { "name" };
	static final String[] FROM1 = { "accountName" };
	static final int[] TO = { android.R.id.text1 };
	static final int SET_DATE_ID = 0;
	private static int WITHDRAW = 2;
	private static int DEPOSIT = 0;
	private static int TRANSFER = 1;
	private int Action = ACTION_NEW;
	private int intYear;
	private int intMonth;
	private int intDay;
	/***** variables used for keeping track of each spinner position *****/
	private int intSchPaymentMethodPos = 0;
	private int intSchTypePos = 2;
	private int intSchAccountPos = 0;
	private int intSchPayeePos = 0;
	private int intSchStatusPos = 0;
	private int intSchCategoryPos = 0;
	
	private int intSchType = WITHDRAW;
	private int intSchStatus = 0;
	private int intSchFreq = 0;
	private int intSchFreqDesc = 0;
	private int intSchPaymentMethod = 0;
	private int intSchOccurence = Schedule.OCCUR_ONCE;
	private String strSchAccountId = null;
	private String strSchPayeeId = null;
	private String strSchCategoryId = null;		// Only used if we have ONLY one category, use splits if we have more than one.
	private String strSchFreqDesc = null;
	private String strSchPaymentMethod = null;
	private String strCategoryName = null;
	private static int iNumberofPasses = 0;
	private int numOfSplits;
	private boolean anySplits = false;
	ArrayList<Split> Splits;
	ArrayList<Split> OrigSplits;
	Button btnCategory;
	EditText editSchName;
	EditText editCategory;
	EditText editCheckNum;
	EditText editDate;
	EditText editAmount;
	EditText editMemo;
	ImageButton btnSplits;
	ImageButton btnSetDate;
	Spinner spinFreqNum;
	Spinner spinFreqDesc;
	Spinner spinPaymentMethod;
	Spinner spinSchType;
	Spinner spinSchAccount;
	Spinner spinPayee;
	Spinner spinCategory;
	Spinner spinStatus;
	Cursor cursorPayees;
	Cursor cursorCategories;
	Cursor cursorAccounts;
	ArrayAdapter<CharSequence> adapterSchTypes;
	ArrayAdapter<CharSequence> adapterStatus;
	ArrayAdapter<CharSequence> adapterSchFreqNum;
	ArrayAdapter<CharSequence> adapterSchFreqDesc;
	ArrayAdapter<CharSequence> adapterSchPaymentMethod;
	SimpleCursorAdapter adapterPayees;
	SimpleCursorAdapter adapterCategories;
	SimpleCursorAdapter adapterSchAccounts;
	KMMDroidApp KMMDapp;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
        setContentView(R.layout.schedule_paymentinfo);
        
        // Get our application
        KMMDapp = ((KMMDroidApp) getApplication());
        
        // See if the database is already open, if not open it Read/Write.
        if(!KMMDapp.isDbOpen())
        {
        	KMMDapp.openDB();
        }
        
        // Make sure that the KMMDapp.Splits is empty.
        KMMDapp.splitsDestroy();
        
        // Find our views.
        btnCategory = (Button) findViewById(R.id.buttonChooseCategory);
        editSchName = (EditText) findViewById(R.id.editScheduleName);
        editCategory = (EditText) findViewById(R.id.editCategory);
        editCheckNum = (EditText) findViewById(R.id.checkNumber);
        editDate = (EditText) findViewById(R.id.date);
        editAmount = (EditText) findViewById(R.id.amount);
        editMemo = (EditText) findViewById(R.id.memo);
        btnSplits = (ImageButton) findViewById(R.id.buttonSplit);
        btnSetDate = (ImageButton) findViewById(R.id.buttonSetDate);
        spinFreqNum = (Spinner) findViewById(R.id.scheduleFrequencyNumber);
        spinFreqDesc = (Spinner) findViewById(R.id.scheduleFrequencyDescription);
        spinSchType = (Spinner) findViewById(R.id.scheduleType);
        spinSchAccount = (Spinner) findViewById(R.id.scheduleAccount);
        spinPayee = (Spinner) findViewById(R.id.payee);
        spinCategory = (Spinner) findViewById(R.id.category);
        spinStatus = (Spinner) findViewById(R.id.status);
        spinPaymentMethod = (Spinner) findViewById(R.id.schedulePaymentMethod);
        
        // Make it so the user is not able to edit the Category selected without using the Spinner.
        editCategory.setKeyListener(null);
        
        // Set our OnClickListener events
        btnSetDate.setOnClickListener(new View.OnClickListener() 
        {	
			public void onClick(View v)
			{
				showDialog(SET_DATE_ID);
			}
		});

        btnSplits.setOnClickListener(new View.OnClickListener() 
        {	
			public void onClick(View v)
			{
				Intent i = new Intent(getBaseContext(), CreateModifySplitsActivity.class);
				i.putExtra("Action", Action);
				i.putExtra("TransAmount", editAmount.getText().toString());
				i.putExtra("Memo", editMemo.getText().toString());
				i.putExtra("CategoryId", strSchCategoryId);
				i.putExtra("transType", intSchType);
				startActivity(i);
			}
		});
        
        btnCategory.setOnClickListener(new View.OnClickListener()
        {			
			public void onClick(View arg0)
			{
				spinCategory.setVisibility(0);
				spinCategory.performClick();
				btnCategory.setVisibility(4);
			}
		});
        
        // Set the OnItemSelectedListeners for the spinners.
        spinSchType.setOnItemSelectedListener(new AccountOnItemSelectedListener());
        spinPayee.setOnItemSelectedListener(new AccountOnItemSelectedListener());
        spinCategory.setOnItemSelectedListener(new AccountOnItemSelectedListener());
        spinStatus.setOnItemSelectedListener(new AccountOnItemSelectedListener());
        spinSchAccount.setOnItemSelectedListener(new AccountOnItemSelectedListener());
        spinFreqNum.setOnItemSelectedListener(new AccountOnItemSelectedListener());
        spinFreqDesc.setOnItemSelectedListener(new AccountOnItemSelectedListener());
        spinPaymentMethod.setOnItemSelectedListener(new AccountOnItemSelectedListener());
        
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
        
        // Get the Action.
        Bundle extras = getIntent().getExtras();
        //Action = extras.getInt("Action");
	}
	@Override
	protected void onDestroy() 
	{
		// TODO Auto-generated method stub
		super.onDestroy();
	}
	@Override
	protected void onResume() 
	{
		// TODO Auto-generated method stub
		super.onResume();
	
		cursorPayees = KMMDapp.db.query("kmmpayees", new String[] { "name", "id AS _id" }, 
				null, null, null, null, "name ASC");
		startManagingCursor(cursorPayees);
		
		cursorCategories = KMMDapp.db.query("kmmAccounts", new String[] { "accountName", "id AS _id" },
				"(accountTypeString='Expense' OR accountTypeString='Income')", null, null, null, "accountName ASC");
		startManagingCursor(cursorCategories);
		
		cursorAccounts = KMMDapp.db.query("kmmAccounts", new String[] { "accountName", "id AS _id" },
				"(accountTypeString='Checking' OR accountTypeString='Savings' OR accountTypeString='Liability' OR " +
						"accountTypeString='Credit Card') AND (balance != '0/1')", null, null, null, "accountName ASC");
		startManagingCursor(cursorAccounts);
		
		// Set up the adapters
		adapterPayees = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, cursorPayees, FROM, TO);
		adapterPayees.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
		spinPayee.setAdapter(adapterPayees);
		adapterCategories = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, cursorCategories, FROM1, TO);
		adapterCategories.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
		spinCategory.setAdapter(adapterCategories);
		adapterSchAccounts = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, cursorAccounts, FROM1, TO);
		adapterSchAccounts.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
		spinSchAccount.setAdapter(adapterSchAccounts);
		adapterSchTypes = ArrayAdapter.createFromResource(this, R.array.TransactionTypes, android.R.layout.simple_spinner_item);
		adapterSchTypes.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
		spinSchType.setAdapter(adapterSchTypes);
		adapterStatus = ArrayAdapter.createFromResource(this, R.array.TransactionStatus, android.R.layout.simple_spinner_item);
		adapterStatus.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
		spinStatus.setAdapter(adapterStatus);
		adapterSchFreqNum = ArrayAdapter.createFromResource(this, R.array.scheduleFrequency, android.R.layout.simple_spinner_item);
		adapterSchFreqNum.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
		spinFreqNum.setAdapter(adapterSchFreqNum);
		adapterSchFreqDesc = ArrayAdapter.createFromResource(this, R.array.scheduleFreqDescription, android.R.layout.simple_spinner_item);
		adapterSchFreqDesc.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
		spinFreqDesc.setAdapter(adapterSchFreqDesc);
		adapterSchPaymentMethod = ArrayAdapter.createFromResource(this, R.array.SchedulePaymentMethod, android.R.layout.simple_spinner_item);
		adapterSchPaymentMethod.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
		spinPaymentMethod.setAdapter(adapterSchPaymentMethod);
		
		if( Action == ACTION_EDIT )
		{
//			editTransaction();
		}
//		else if( Action == ACTION_ENTER_SCHEDULE )
//		{
//			enterSchedule();
//		}
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
				if( strCategoryName == null )
					editCategory.setText("");
				else
					editCategory.setText(strCategoryName);
				btnCategory.setEnabled(true);
				numOfSplits = 2;
				anySplits = false;
			}
		}	
		
		// Set the default items for the type, status, freq, freq desc, account, category and payment method spinners.
		spinSchType.setSelection(intSchTypePos);
		spinStatus.setSelection(intSchStatusPos);
		spinFreqNum.setSelection(intSchFreq);
		spinFreqDesc.setSelection(intSchFreqDesc);
		spinPaymentMethod.setSelection(intSchPaymentMethodPos);
		//spinCategory.setSelection(intSchCategoryPos);
		spinSchAccount.setSelection(intSchAccountPos);
		spinPayee.setSelection(intSchPayeePos);
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
			if( iNumberofPasses > 7 )
			{
				switch( parent.getId())
				{
					case R.id.scheduleAccount:
						intSchAccountPos = parent.getSelectedItemPosition();
						Cursor c = (Cursor) parent.getAdapter().getItem(pos);
						strSchAccountId = c.getString(1).toString();
						Log.d(TAG, "User changed the default account: " + strSchAccountId);
						break;
					case R.id.scheduleFrequencyNumber:
						intSchFreq = parent.getSelectedItemPosition();	
						Log.d(TAG, "User changed the Schedule Frequency Number: " + parent.getAdapter().getItem(pos).toString());
						break;
					case R.id.scheduleFrequencyDescription:
						strSchFreqDesc = parent.getAdapter().getItem(pos).toString();
						intSchFreqDesc = parent.getSelectedItemPosition();
						Log.d(TAG, "User changed Schedule Frequency Description: " + strSchFreqDesc);
						break;
					case R.id.schedulePaymentMethod:
						intSchPaymentMethodPos = parent.getSelectedItemPosition();
						strSchPaymentMethod = parent.getAdapter().getItem(pos).toString();
						Log.d(TAG, "User changed Schedule Payment Method: " + strSchPaymentMethod);
						break;						
					case R.id.scheduleType:
						intSchTypePos = parent.getSelectedItemPosition();
						String str = parent.getAdapter().getItem(pos).toString();
						Log.d(TAG, "User changed Schedule Type: " + str);
						if( str.matches("Deposit") )
							intSchType = Schedule.TYPE_DEPOSIT;
						if( str.matches("Transfer") )
							intSchType = Schedule.TYPE_TRANSFER;
						if( str.matches("Withdrawal") )
							intSchType = Schedule.TYPE_BILL;
						break;
					case R.id.payee:
						intSchPayeePos = parent.getSelectedItemPosition();
						c = (Cursor) parent.getAdapter().getItem(pos);
						strSchPayeeId = c.getString(1).toString();
						break;
					case R.id.category:
						//intSchCategoryPos = parent.getSelectedItemPosition();
						c = (Cursor) parent.getAdapter().getItem(pos);
						strCategoryName = c.getString(0).toString();
						editCategory.setText(strCategoryName);
						strSchCategoryId = c.getString(1).toString();
						spinCategory.setVisibility(4);
						btnCategory.setVisibility(0);
						break;
					case R.id.status:
						intSchStatusPos = parent.getSelectedItemPosition();
						str = parent.getAdapter().getItem(pos).toString();
						if( str.matches( "Reconciled" ) )
							intSchStatus = 0;
						if( str.matches( "Cleared" ) )
							intSchStatus = 1;
						if( str.matches( "Not reconciled" ) )
							intSchStatus = 2;
						break;
					default:
						Log.d(TAG, "parentId: " + String.valueOf(parent.getId()));
						break;
				}
			}
				if( iNumberofPasses < 8 )
					iNumberofPasses = iNumberofPasses + 1;
		}

		public void onNothingSelected(AdapterView<?> arg0) {
			// TODO Auto-generated method stub
			// do nothing.
		}		
	}
	// **************************************************************************************************
	// ************************************ Helper methods **********************************************
	public String getScheduleName()
	{
		return this.editSchName.getText().toString();
	}
	
	public int getScheduleFrequency()
	{
		// Since we are storing the actual array index value we need to increase by one for database storage.
		return this.intSchFreq + 1;
	}
	
	public String getScheduleFrequencyDescription()
	{
		return this.strSchFreqDesc;
	}
	
	public int getSchedulePaymentMethod()
	{
		switch(intSchPaymentMethodPos)
		{
		case 0:
			return Schedule.PAYMENT_TYPE_OTHER;
		case 1:
			return Schedule.PAYMENT_TYPE_BANKTRANSFER;
		case 2:
			return Schedule.PAYMENT_TYPE_STANDINGORDER;
		case 3:
			return Schedule.PAYMENT_TYPE_MANUALDEPOSIT;
		case 4:
			return Schedule.PAYMENT_TYPE_DIRECTDEPOSIT;
		default:
			return Schedule.PAYMENT_TYPE_ANY;
		}
	}
	
	public int getScheduleType()
	{
		return this.intSchType;
	}
	
	public String getAccountTypeId()
	{
		return this.strSchAccountId;
	}
	
	public String getPayeeId()
	{
		return this.strSchPayeeId;
	}
	
	public String getCategoryId()
	{
		return this.strSchCategoryId;
	}
	
	public String getCheckNumber()
	{
		return this.editCheckNum.getText().toString();
	}
	
	public String getStartDate()
	{
		// Need to re-format the date to YYY-MM-DD
		String str[] = this.editDate.getText().toString().split("-");
		return new StringBuilder()
		// Month is 0 based so add 1
		.append(str[2]).append("-")
		.append(str[0]).append("-")
		.append(str[1]).toString();
	}
	
	public String getScheduleAmount()
	{
		return this.editAmount.getText().toString();
	}
	
	public int getScheduleStatus()
	{
		return this.intSchStatus;
	}
	
	public String getScheduleMemo()
	{
		return this.editMemo.getText().toString();
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
		
		editDate.setText(
				new StringBuilder()
					// Month is 0 based so add 1
					.append(strMonth).append("-")
					.append(strDay).append("-")
					.append(intYear));
	}
	
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
	
	// Take the occurence value and return the index to our internal array for the description.
	private int getOccurenceDescFromOccurence(int occ)
	{
		switch(occ)
		{
		case Schedule.OCCUR_ANY:
			return 0;
		case Schedule.OCCUR_ONCE:
			return 0;
		case Schedule.OCCUR_DAILY:
			return 1;
		case Schedule.OCCUR_WEEKLY:
		case Schedule.OCCUR_FORTNIGHTLY:
		case Schedule.OCCUR_EVERYOTHERWEEK:
		case Schedule.OCCUR_EVERYTHREEWEEKS:
		case Schedule.OCCUR_EVERYTHIRTYDAYS:
			return 2;
		case Schedule.OCCUR_MONTHLY:
		case Schedule.OCCUR_EVERYFOURWEEKS:
		case Schedule.OCCUR_EVERYEIGHTWEEKS:
		case Schedule.OCCUR_EVERYOTHERMONTH:
		case Schedule.OCCUR_EVERYTHREEMONTHS:
		case Schedule.OCCUR_TWICEYEARLY:
		case Schedule.OCCUR_QUARTERLY:
		case Schedule.OCCUR_EVERYFOURMONTHS:
			return 4;
		case Schedule.OCCUR_EVERYOTHERYEAR:
		case Schedule.OCCUR_YEARLY:
			return 5;
		default:
			return 0;
		}
	}
}
