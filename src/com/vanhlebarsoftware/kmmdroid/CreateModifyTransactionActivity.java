package com.vanhlebarsoftware.kmmdroid;

import java.util.Calendar;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
//import android.widget.DatePicker;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.*;

public class CreateModifyTransactionActivity extends FragmentActivity implements
												CategoryFragment.OnSplitsClickedListener,
												CategoryFragment.OnSendWidgetIdListener,
												PayeeFragment.OnSendWidgetIdListener
{
	private static final String TAG = CreateModifyTransactionActivity.class.getSimpleName();
	private static final String URI_SCHEME = "com.vanhlebarsoftware.kmmdroid";
	private static final int ACTION_NEW = 1;
	private static final int ACTION_EDIT = 2;
	private static final int ACTION_ENTER_SCHEDULE = 3;
	static final String[] FROM = { "name" };
	static final int[] TO = { android.R.id.text1 };
	static final String[] FROM1 = { "accountName" };
	static final int SET_DATE_ID = 0;
	private int intYear;
	private int intMonth;
	private int intDay;
	private int intTransType = Transaction.WITHDRAW;
	private int intTransStatus = 0;
	private String strTransPayeeId = null;
	private String strTransCategoryId = null;		// Only used if we have ONLY one category, use splits if we have more than one.
	private static int iNumberofPasses = 0;
	private int Action = ACTION_NEW;
	private String transId = null;
	private boolean anySplits = false;
	private boolean ReturningFromSplits = false;
	private String accountUsed = null;
	private int numOfSplits;
	private Schedule scheduleToEnter = null;
	boolean fromHomeWidget = false;
	boolean fromScheduleActions = false;
	private String widgetDatabasePath = null;
	private String fromWidgetId = "9999";
	private boolean isDirty = false;
	CategoryFragment catFrag;
	PayeeFragment payeeFrag;
	Spinner spinTransType;
	Spinner spinStatus;
	ImageButton buttonSetDate;
	EditText transDate;
	EditText editMemo;
	EditText editAmount;
	EditText editCkNumber;
	ArrayAdapter<CharSequence> adapterTransTypes;
	ArrayAdapter<CharSequence> adapterStatus;
	Transaction transaction;
	KMMDroidApp KMMDapp;
	
	/* Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
        setContentView(R.layout.createmod_transaction);
        
        // Get our application
        KMMDapp = ((KMMDroidApp) getApplication()); 
        
        // See if the database is already open, if not open it Read/Write.
        if(!KMMDapp.isDbOpen())
        {
        	KMMDapp.openDB();
        }
        
        // Find our views
        spinTransType = (Spinner) findViewById(R.id.transactionType);
        spinStatus = (Spinner) findViewById(R.id.status);
        transDate = (EditText) findViewById(R.id.date);
        editMemo = (EditText) findViewById(R.id.memo);
        editAmount = (EditText) findViewById(R.id.amount);
        editCkNumber = (EditText) findViewById(R.id.checkNumber);
        buttonSetDate = (ImageButton) findViewById(R.id.buttonSetDate);
        catFrag = (CategoryFragment) getSupportFragmentManager().findFragmentById(R.id.categoryFragment);
        payeeFrag = (PayeeFragment) getSupportFragmentManager().findFragmentById(R.id.payeeFragment);
        
        // Make sure that the KMMDapp.Splits is empty.
        KMMDapp.splitsDestroy();
        
        // Get the action the user is doing.
        Bundle extras = getIntent().getExtras();
        Log.d(TAG, "Size of extras: " + extras.size());
        Action = extras.getInt("Action");
        fromHomeWidget = extras.getBoolean("fromHome");
        fromScheduleActions = extras.getBoolean("fromScheduleActions");
        widgetDatabasePath = extras.getString("widgetDatabasePath");
        fromWidgetId = extras.getString("fromWidgetId");
        
        // Make sure that our KMMDApp is pointing to the correct preference area, main app or from a specified widgetId.
        if(fromWidgetId != null)
        {
        	KMMDapp.updatePrefs(fromWidgetId);
        }
        
        if( Action == ACTION_NEW )
        {
        	Log.d(TAG, "From homeWidget: " + String.valueOf(fromHomeWidget));
        	transaction = new Transaction(getBaseContext(), null, this.fromWidgetId);
        	accountUsed = extras.getString("accountUsed");
        }
        else if( Action == ACTION_EDIT )
        	transaction = new Transaction(getBaseContext(), extras.getString("transId"), this.fromWidgetId); 

        else if( Action == ACTION_ENTER_SCHEDULE )
        {
        	// Need to get the specified schedule and all it's splits and other information.
        	transaction = new Transaction(getBaseContext(), null, this.fromWidgetId);
        	scheduleToEnter = getSchedule(extras.getString("scheduleId"));
        }
        
        // Set up the other keyListener's for the various editText items.
        editCkNumber.addTextChangedListener(new TextWatcher()
        {

			public void afterTextChanged(Editable s) 
			{
				isDirty = true;
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {}
        });
        
        editMemo.addTextChangedListener(new TextWatcher()
        {

			public void afterTextChanged(Editable s) 
			{
				isDirty = true;
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {}
        });
        
        editAmount.addTextChangedListener(new TextWatcher()
        {

			public void afterTextChanged(Editable s) 
			{
				isDirty = true;
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {}
        });
        
        // Set our OnClickListener events
        buttonSetDate.setOnClickListener(new View.OnClickListener() 
        {	
			public void onClick(View v)
			{
				DialogFragment dateFrag = new KMMDDatePickerFragment(transDate);
				dateFrag.show(getSupportFragmentManager(), "datePicker");
				isDirty = true;
			}
		});
        
        // Set the OnItemSelectedListeners for the spinners.
        spinTransType.setOnItemSelectedListener(new AccountOnItemSelectedListener());
        spinStatus.setOnItemSelectedListener(new AccountOnItemSelectedListener());
        
        // get the current date
        final Calendar c = Calendar.getInstance();
        intYear = c.get(Calendar.YEAR);
        intMonth = c.get(Calendar.MONTH);
        intDay = c.get(Calendar.DAY_OF_MONTH);
        
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

		adapterTransTypes = ArrayAdapter.createFromResource(this, R.array.TransactionTypes, android.R.layout.simple_spinner_item);
		adapterTransTypes.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
		spinTransType.setAdapter(adapterTransTypes);
		adapterStatus = ArrayAdapter.createFromResource(this, R.array.TransactionStatus, android.R.layout.simple_spinner_item);
		adapterStatus.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
		spinStatus.setAdapter(adapterStatus);
		
		if( Action == ACTION_EDIT )
		{
			editTransaction();
			if(!this.ReturningFromSplits)
				isDirty = false;
		}
		else if( Action == ACTION_ENTER_SCHEDULE )
		{
			enterSchedule();
		}
		else
		{
			iNumberofPasses = 0;
			
			// See if we have any splits from the Split Entry screen.
			if( this.numOfSplits == 0)
			{
				catFrag.setCategoryId(null);
				numOfSplits = 0;
				anySplits = false;
			}
		}	
		
		// See if the splits are dirty, if so mark the transaction as dirty.
		if( KMMDapp.getSplitsAreDirty() )
		{
			Log.d(TAG, "Splits are dirty!!");
			isDirty = true;
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
		// Only display the save item if we have made changes.
		if( isDirty )
			menu.findItem(R.id.itemsave).setVisible(true);
		else
			menu.findItem(R.id.itemsave).setVisible(false);
		
		// We don't need the "Open", "Close" or "Delete" items at all.
		menu.findItem(R.id.itemOpenAcct).setVisible(false);
		menu.findItem(R.id.itemClose).setVisible(false);
		menu.findItem(R.id.itemDelete).setVisible(false);		
		return true;
	}
	
	// Called when an options item is clicked
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.itemsave:				
				switch( Action )
				{
				case ACTION_NEW:					
					transaction.createId();
					transaction.getDataChanges(this);
					transaction.Save();
					break;
				case ACTION_EDIT:
					transaction.getDataChanges(this);
					transaction.Update();
					break;
				case ACTION_ENTER_SCHEDULE:
					transaction.createId();
					transaction = scheduleToEnter.convertToTransaction(transaction.getTransId());
					transaction.getDataChanges(this);
					transaction.Save();
					//Need to advance the schedule to the next date and update the lastPayment and
					//startDate dates to the recorded date of the transaction.
					scheduleToEnter.setTransDate(scheduleToEnter.convertDate(transaction.getDate()));
					scheduleToEnter.advanceDueDate(/*Schedule.getOccurence(scheduleToEnter.getOccurence(), scheduleToEnter.getOccurenceMultiplier())*/);					
					break;
				}

				KMMDapp.updateFileInfo("lastModified", 0);

				// If the user has the preference item of updateFrequency = Auto fire off a Broadcast
				if(KMMDapp.getAutoUpdate())
				{
					// We have an issue here, we don't really want to update ALL widgets as that is ineffecient, BUT
					// we might have more than just the current widget effected by an update that needs to be refreshed.
					// We either need to update ALL widgets OR figure out a routine to update only the effected widgets.
					KMMDFunctions.updateSchedulesWidgets(getBaseContext());
					KMMDFunctions.updatePreferredAccountsWidgets(getBaseContext());
				}
				else
					Log.d(TAG, "No need to fire update broadcast for this event!");
				
				// Mark the file as dirty
				KMMDapp.markFileIsDirty(true, fromWidgetId);
				
				// Clear the SplitsDirty flag.
				KMMDapp.setSplitsAryDirty(false);
				
				// Send off the Transactions changed message locally.
				sendTransChangedMsg();
				finish();
				break;
			case R.id.itemCancel:
				Log.d(TAG, "CreateModifyTransactionActivity itemCancelled!");
				if( isDirty )
				{
					AlertDialog.Builder alertDel = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogNoTitle));
					alertDel.setTitle(R.string.BackActionWarning);
					alertDel.setMessage(getString(R.string.titleBackActionWarning));

					alertDel.setPositiveButton(getString(R.string.titleButtonOK), new DialogInterface.OnClickListener()
					{
						public void onClick(DialogInterface dialog, int whichButton)
						{
							// Clear the SplitsDirty flag.
							KMMDapp.setSplitsAryDirty(false);
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
					KMMDapp.splitsDestroy();
					// Clear the SplitsDirty flag.
					KMMDapp.setSplitsAryDirty(false);
					finish();
				}
				break;
		}
		return true;
	}
	
	public void onSplitsClicked(String categoryId) 
	{
		// First populate our transaction with the current date from the form.
		transaction.getDataChanges(this);
		
		// We need to write out the current splits to cache so that we can pick them up later in the modify splits activity.
		transaction.cacheTransaction();
		Intent i = new Intent(getBaseContext(), CreateModifySplitsActivity.class);
		i.putExtra("Action", Action);
		i.putExtra("TransAmount", editAmount.getText().toString());
		i.putExtra("Memo", editMemo.getText().toString());
		i.putExtra("CategoryId", categoryId);
		i.putExtra("transType", intTransType);
		i.putExtra("date", transDate.getText().toString());
		i.putExtra("payeeid", payeeFrag.getPayeeId());
		i.putExtra("transAction", getTranAction());
		i.putExtra("transStatus", intTransStatus);
		i.putExtra("checkNumber", this.editCkNumber.getText().toString());
		if( Action == ACTION_EDIT)
			i.putExtra("transactionId", transaction.getTransId());
		startActivityForResult(i, 0);		
	}
	
	public void onSendWidgetId(int fromFragment) 
	{
		switch( fromFragment )
		{
		case PayeeFragment.PAYEE_LOADER:
			payeeFrag.setWidgetId(this.fromWidgetId);
			break;
		case CategoryFragment.WITHDRAW_CATEGORY_LOADER:
			catFrag.setWidgetId(this.fromWidgetId);
			break;
		default:
			break;
		}
	}
	
	@Override
	public void onBackPressed()
	{
		Log.d(TAG, "User clicked the back button");
		if( isDirty )
		{
			AlertDialog.Builder alertDel = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogNoTitle));
			alertDel.setTitle(R.string.BackActionWarning);
			alertDel.setMessage(getString(R.string.titleBackActionWarning));

			alertDel.setPositiveButton(getString(R.string.titleButtonOK), new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int whichButton)
				{
					// Clear the SplitsDirty flag.
					KMMDapp.setSplitsAryDirty(false);
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
			// Clear the SplitsDirty flag.
			KMMDapp.setSplitsAryDirty(false);
			finish();
		}
	}
	
    @Override
    protected void onActivityResult(int pRequestCode, int resultCode, Intent data)
    {    	
    	if( resultCode != -1)
    	{
    		this.numOfSplits = data.getIntExtra("NumberOfSplits", -1);
    		this.ReturningFromSplits = true;
    		long splitsTotal = data.getLongExtra("splitsTotal", 0);
  		
    		if( this.numOfSplits > 0 )
    		{
    			// Pull the cached splits into our current transactions.
    			transaction.getcachedTransaction();
    			
    			// Setup the correct Transaction amount from the splits and then make this non-editable, if we don't have splits, turn it
    			// bank on.
    			if( splitsTotal < 0 )
    				splitsTotal = splitsTotal * -1;
    			editAmount.setText(Transaction.convertToDollars((splitsTotal), true, false));
    			editAmount.setFocusable(false);
			
    			// We need to set up the display of split transaction and make it so the user can't use the category select button.
    			catFrag.setCategoryId(getString(R.string.splitTransaction));
    			
    			isDirty = true;
    		}
    		else
    			editAmount.setFocusableInTouchMode(true);
    	}
    }
    
	public class AccountOnItemSelectedListener implements OnItemSelectedListener
	{
		public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
		{
			Log.d(TAG, "Inside onItemSelected");
			if( iNumberofPasses > 1 )
			{
				switch( parent.getId())
				{
					case R.id.transactionType:
						Log.d(TAG, "Inside transactionType: " + String.valueOf(parent.getId()));
						String str = parent.getAdapter().getItem(pos).toString();
						if( str.matches("Deposit") )
						{
							intTransType = 0;
							catFrag.setLoaderType(CategoryFragment.WITHDRAW_CATEGORY_LOADER);
						}
						if( str.matches("Transfer") )
						{
							intTransType = 1;
							catFrag.setLoaderType(CategoryFragment.TRANSFER_CATEGORY_LOADER);
						}
						if( str.matches("Withdrawal") )
						{
							intTransType = 2;
							catFrag.setLoaderType(CategoryFragment.WITHDRAW_CATEGORY_LOADER);
						}
						isDirty = true;
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
						isDirty = true;
						break;
					default:
						Log.d(TAG, "parentId: " + String.valueOf(parent.getId()));
						break;
				}
			}
			if( iNumberofPasses < 2 )
				iNumberofPasses = iNumberofPasses + 1;
		}

		public void onNothingSelected(AdapterView<?> arg0) {
			// do nothing.
		}		
	}
	
	// **************************************************************************************************
	// ************************************ Helper methods **********************************************	
	public String getTranAction()
	{
		return spinTransType.getItemAtPosition(intTransType).toString();
	}
	
	public Calendar getPostDate()
	{
		// Date is stored MM-DD-YYYY
		String[] date = this.transDate.getText().toString().split("-");
		Calendar cal = Calendar.getInstance();
		cal.set(Integer.valueOf(date[2]), Integer.valueOf(date[0]) - 1, Integer.valueOf(date[1]));
		
		return cal;
	}
	
	public int getTransactionType()
	{
		return this.intTransType;
	}
	
	public int getTransactionStatus()
	{
		return this.intTransStatus;
	}
	
	public int getNumberOfSplits()
	{
		return this.numOfSplits;
	}
	
	public String getAccountUsed()
	{
		return this.accountUsed;
	}
	
	public String getCheckNumber()
	{
		return this.editCkNumber.getText().toString();
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

	private void convertDate(Calendar date)
	{	
		intYear = date.get(Calendar.YEAR);
		intMonth = date.get(Calendar.MONTH);
		intDay = date.get(Calendar.DAY_OF_MONTH);	
	}

	private Schedule getSchedule(String schId)
	{
		Log.d(TAG, "Using widgetId: " + this.fromWidgetId);
		Log.d(TAG, "Getting schedule number: " + schId);
		Uri u = Uri.withAppendedPath(KMMDProvider.CONTENT_SCHEDULE_URI, schId + "#" + this.fromWidgetId);
		u = Uri.parse(u.toString());
		Cursor schedule = getBaseContext().getContentResolver().query(u, null, null, null, null);
		u = Uri.withAppendedPath(KMMDProvider.CONTENT_SPLIT_URI, "#" + this.fromWidgetId);
		u = Uri.parse(u.toString());
		Cursor splits = getBaseContext().getContentResolver().query(u, new String[] { "*" }, "transactionId=?", 
																	new String[] { schId }, "splitId ASC");

		return new Schedule(schedule, splits, getBaseContext(), this.fromWidgetId);
	}
	
	private void editTransaction()
	{
		// If we are coming back from splits entry screen follow this path.
		if( this.ReturningFromSplits )
		{
			iNumberofPasses = 0;
		}
		else
		{	
			// load the transaction details into the form.
			editMemo.setText(transaction.getMemo());
			convertDate(transaction.getDate());
			editCkNumber.setText(transaction.splits.get(0).getCheckNumber());
			payeeFrag.setPayeeId(transaction.splits.get(0).getPayeeId());
		
			// See if we have only used one category or if we have multiple.
			if( transaction.splits.size() == 2 )
			{
				catFrag.setCategoryId(transaction.splits.get(1).getAccountId());
				iNumberofPasses = 0;
				intTransStatus = Integer.valueOf(transaction.splits.get(0).getReconcileFlag());
				numOfSplits = 2;
				anySplits = false;
				
				// Populate the category used for this split only.
				strTransCategoryId = transaction.splits.get(1).getAccountId();
			}
			else
			{		
				iNumberofPasses = 0;
				catFrag.setCategoryId(getString(R.string.splitTransaction));
				
				// Make it so the user can't edit the amount.
				editAmount.setFocusable(false);
				numOfSplits = transaction.splits.size();
				anySplits = true;
			}
		
			float amount = Float.valueOf(Transaction.convertToDollars(Account.convertBalance(transaction.splits.get(0).getValue()), false, false));
			if( amount < 0 )
			{
				intTransType = Transaction.WITHDRAW;
				amount = amount * -1;		//change the sign of the amount for the form only.
			}
			else
				intTransType = Transaction.DEPOSIT;
			
			editAmount.setText(String.valueOf(amount));
			
			// Need to populate the Account used for this transaction.
			accountUsed = transaction.splits.get(0).getAccountId();
		
			updateDisplay();
		}
	}
	
	private void enterSchedule()
	{
		// If we are coming back from splits entry screen follow this path.
		if( this.ReturningFromSplits )
		{
			//setupSplitInfo();
			//editCategory.setText(R.string.splitTransaction);
			//buttonChooseCategory.setEnabled(false);
			//spinPayee.setSelection(setPayee(strTransPayeeId));
			iNumberofPasses = 0;
		}
		else
		{	
			// load the transaction details into the form.
			editMemo.setText(scheduleToEnter.Splits.get(0).getMemo());
//**************************** THIS NEEDS TO BE FIXED!!!!!!!!! ****************************************************
			Log.d(TAG, "Schedule due date: " + scheduleToEnter.getDatabaseFormattedString());
			transDate.setText(scheduleToEnter.getDatabaseFormattedString());
			String[] dueDate = scheduleToEnter.getDatabaseFormattedString().split("-");
			intDay = Integer.valueOf(dueDate[2]);
			intMonth = Integer.valueOf(dueDate[1]) - 1;
			intYear = Integer.valueOf(dueDate[0]);
//*****************************************************************************************************************
			editCkNumber.setText(scheduleToEnter.Splits.get(0).getCheckNumber());
			strTransPayeeId = scheduleToEnter.Splits.get(0).getPayeeId();
			payeeFrag.setPayeeId(strTransPayeeId);
			//spinPayee.setSelection(setPayee(strTransPayeeId));
		
			// See if we have only used one category or if we have multiple.
			if( scheduleToEnter.Splits.size() == 2 )
			{
				//Uri u = Uri.withAppendedPath(KMMDProvider.CONTENT_ACCOUNT_URI,scheduleToEnter.Splits.get(1).getAccountId() +
				//		"#" + this.fromWidgetId);
				//u = Uri.parse(u.toString());
				//Cursor c = getBaseContext().getContentResolver().query(u, null, null, null, null);
						//KMMDapp.db.query("kmmAccounts", new String[] { "accountName" }, "id=?",
						//				new String[] { scheduleToEnter.Splits.get(1).getAccountId() }, null, null, null);
				//startManagingCursor(c);
				//c.moveToFirst();
				catFrag.setCategoryId(scheduleToEnter.Splits.get(1).getAccountId());
				//editCategory.setText(c.getString(0));
				iNumberofPasses = 0;
				intTransStatus = Integer.valueOf(scheduleToEnter.Splits.get(0).getReconcileFlag());

				//c.close();
				numOfSplits = 2;
				anySplits = false;
				
				// Populate the category used for this split only.
				strTransCategoryId = scheduleToEnter.Splits.get(1).getAccountId();
			}
			else
			{
				// need to put the splits into the KMMDapp.Splits object so user may edit the split details.
				//KMMDapp.splitsInit();
				//for(int i = 1; i < scheduleToEnter.Splits.size(); i++)
				//	KMMDapp.Splits.add(scheduleToEnter.Splits.get(i));
			
				//for(int i = 0; i < KMMDapp.Splits.size(); i++)
				//	 KMMDapp.Splits.get(i).dump();
				iNumberofPasses = 0;
				//editCategory.setText(R.string.splitTransaction);
				catFrag.setCategoryId(getString(R.string.splitTransaction));
				//buttonChooseCategory.setEnabled(false);
				numOfSplits = scheduleToEnter.Splits.size();
				anySplits = true;
			}
		
			float amount = Float.valueOf(scheduleToEnter.Splits.get(0).getValueFormatted());
			if( amount < 0 )
			{
				intTransType = Transaction.WITHDRAW;
				amount = amount * -1;		//change the sign of the amount for the form only.
			}
			else
				intTransType = Transaction.DEPOSIT;
			
			editAmount.setText(String.valueOf(amount));
			
			// Need to populate the Account used for this transaction.
			accountUsed = scheduleToEnter.Splits.get(0).getAccountId();
		
			// Make a copy of the original transactions split for later use if we modify anything.
			//for(int i=0; i < scheduleToEnter.Splits.size(); i++)
			//	OrigSplits.add(scheduleToEnter.Splits.get(i));
			
			updateDisplay();
		}		
	}
	
	public boolean getHasSplits()
	{
		return this.ReturningFromSplits;
	}
	
	private void sendTransChangedMsg()
	{
		Log.d(TAG, "Sending Transactions-Change event.");
		Intent category = new Intent(TransactionsLoader.TRANSCHANGED);
		Intent home = new Intent(HomeLoader.HOMECHANGED);
		
		// Notify our Transactions to update.
		LocalBroadcastManager.getInstance(this).sendBroadcast(category);
		
		// Notify our Home screen to update.
		LocalBroadcastManager.getInstance(this).sendBroadcast(home);
	}
}
