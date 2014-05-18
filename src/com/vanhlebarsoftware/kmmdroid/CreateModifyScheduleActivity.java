package com.vanhlebarsoftware.kmmdroid;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.SimpleCursorAdapter;
import android.widget.TabHost;
import android.widget.TabHost.TabContentFactory;

public class CreateModifyScheduleActivity extends FragmentActivity  implements
											TabHost.OnTabChangeListener,
											SchedulePaymentInfoActivity.OnSendPaymentInfoListener,
											ScheduleOptionsActivity.OnSendOptionsListener
{
	private static final String TAG = CreateModifyScheduleActivity.class.getSimpleName();
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
	private static final int ACTION_NEW = 1;
	private static final int ACTION_EDIT = 2;
	private int Action = 0;
	private int numOfSplits = 0;
	private String schId = null;
	private String widgetDatabasePath = null;
	private String widgetId = "9999";
	private boolean isDirty = false;
	private boolean ReturningFromSplits = false;
	private Schedule schedule;
	
	ArrayList<Split> Splits;
	ArrayList<Split> OrigSplits;
	KMMDroidApp KMMDapp;
	Cursor cursor;
	SimpleCursorAdapter adapter;
	TabHost tabHost;
	
	private HashMap<String, TabInfo> mapTabInfo = new HashMap<String, CreateModifyScheduleActivity.TabInfo>();
	private TabInfo mLastTab = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
        setContentView(R.layout.createmod_schedule);
        
        // Get our application
        KMMDapp = ((KMMDroidApp) getApplication());
        
        // Get the Action.
        Bundle extras = getIntent().getExtras();
        Action = extras.getInt("Action");
        widgetDatabasePath = extras.getString("widgetDatabasePath");
        widgetId = extras.getString("widgetId");
        
        // See if we are editing a schedule, if so get the schedule Id we passed in and create the schedule with it.
        if( Action == ACTION_EDIT )
        {
        	schedule = new Schedule(this, extras.getString("scheduleId"), widgetId);
        }
        else
        {
        	schedule = new Schedule(this, null, widgetId);
        }
        
		initialiseTabHost(savedInstanceState);
		if (savedInstanceState != null) 
		{
            tabHost.setCurrentTabByTag(savedInstanceState.getString("tab")); //set the tab as per the saved state
        }
		
        //Resources res = getResources(); // Resource object to get Drawables
/*        tabHost = getTabHost();  // The activity TabHost
        TabHost.TabSpec spec;  // Resusable TabSpec for each tab
        Intent intent;  // Reusable Intent for each tab

        // Create an Intent to launch an Activity for the tab (to be reused)
        intent = new Intent().setClass(this, SchedulePaymentInfoActivity.class);
        
        // Initialize a TabSpec for each tab and add it to the TabHost
        spec = tabHost.newTabSpec("paymentifno").setIndicator(getString(R.string.titleSchedulePaymentInfo))
                      .setContent(intent);
        tabHost.addTab(spec);

        // Do the same for the other tabs
        intent = new Intent().setClass(this, ScheduleOptionsActivity.class);
        spec = tabHost.newTabSpec("options").setIndicator(getString(R.string.titleScheduleOptions))
                      .setContent(intent);
        tabHost.addTab(spec);
        
        tabHost.setCurrentTab(0);
*/
        // See if the database is already open, if not open it Read/Write.
 //       if(!KMMDapp.isDbOpen())
 //       {
 //       	KMMDapp.openDB();
 //       }
        
        // Make sure that the KMMDapp.Splits is empty.
 //       KMMDapp.splitsDestroy();
        
        // Initialize our Splits ArrayList.
 //       Splits = new ArrayList<Split>();
 //       OrigSplits = new ArrayList<Split>();
	}

	@Override
	protected void onDestroy() 
	{
		super.onDestroy();
		
		// clean up the preferences we no longer need.
		SharedPreferences prefs = getPreferences(Activity.MODE_PRIVATE);
		SharedPreferences.Editor edit = prefs.edit();
		edit.clear();
		edit.apply();
	}

	@Override
	protected void onResume() 
	{
		super.onResume();
		
		// See if we are editing and if so, pull the data into the forms.
		if( Action == ACTION_EDIT )
		{
			editSchedule();
			isDirty = false;
		}
		else
		{
			enterSchedule();
			isDirty = false;
		}
		
/*		if( Action == ACTION_EDIT )
		{			
			// So we have all the data for this schedule, now populate the Payment tab
			getTabHost().setCurrentTab(0);
			Activity schedulePayment = this.getCurrentActivity();
			((SchedulePaymentInfoActivity) schedulePayment).setAction(Action);
			((SchedulePaymentInfoActivity) schedulePayment).setScheduleName(schedule.getString(1));
			((SchedulePaymentInfoActivity) schedulePayment).setScheduleFrequency(schedule.getInt(5));
			((SchedulePaymentInfoActivity) schedulePayment).setScheduleFrequencyDescription(schedule.getInt(4));
			((SchedulePaymentInfoActivity) schedulePayment).setSchedulePaymentMethod(schedule.getInt(7));
			((SchedulePaymentInfoActivity) schedulePayment).setScheduleType(schedule.getInt(2));
			((SchedulePaymentInfoActivity) schedulePayment).setAccountTypeId(Splits.get(0).getAccountId());
			((SchedulePaymentInfoActivity) schedulePayment).setPayeeId(Splits.get(0).getPayeeId());
			((SchedulePaymentInfoActivity) schedulePayment).setSplits(Splits);
			((SchedulePaymentInfoActivity) schedulePayment).setCheckNumber(Splits.get(0).getCheckNumber());
			((SchedulePaymentInfoActivity) schedulePayment).setStartDate(schedule.getString(14));
			((SchedulePaymentInfoActivity) schedulePayment).setScheduleAmount(Splits.get(0).getValueFormatted());
			((SchedulePaymentInfoActivity) schedulePayment).setScheduleStatus(Integer.valueOf(Splits.get(0).getReconcileFlag()));
			((SchedulePaymentInfoActivity) schedulePayment).setScheduleMemo(trans.getString(3));
			
			// populate the Options tab
			getTabHost().setCurrentTab(1);
			Activity scheduleOptions = this.getCurrentActivity();
			((ScheduleOptionsActivity) scheduleOptions).setScheduleWeekendOption(schedule.getInt(15));
			((ScheduleOptionsActivity) scheduleOptions).setScheduleIsEstimate(schedule.getString(11));
			((ScheduleOptionsActivity) scheduleOptions).setScheduleAutoEnter(schedule.getString(12));
			((ScheduleOptionsActivity) scheduleOptions).setEndDate(schedule.getString(10));
			
			getTabHost().setCurrentTab(0);			
			editSchedule();
			isDirty = false;
		}
*/		
	}
	
	public void onTabChanged(String tag) 
	{
		TabInfo newTab = this.mapTabInfo.get(tag);
		if (mLastTab != newTab) 
		{
			FragmentTransaction ft = this.getSupportFragmentManager().beginTransaction();
            if (mLastTab != null) 
            {
                if (mLastTab.fragment != null) 
                {
                	// save the tabs UI elements.
                	saveTabUI(mLastTab.fragment);
                	ft.detach(mLastTab.fragment);
                }
            }
            if (newTab != null) 
            {
                if (newTab.fragment == null) 
                {
                    newTab.fragment = Fragment.instantiate(this,
                            newTab.clss.getName(), newTab.args);
                    ft.add(R.id.realtabcontent, newTab.fragment, newTab.tag);
                } 
                else 
                {
                    ft.attach(newTab.fragment);
                }
            }

            mLastTab = newTab;
            ft.commit();
            this.getSupportFragmentManager().executePendingTransactions();
		}		
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
		// We don't want to delete from the edit/create screens.
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
				switch(Action)
				{
					case ACTION_NEW:
						schedule.createScheduleId();
						schedule.getDataChanges(this);
						schedule.Save();
						break;
					case ACTION_EDIT:
						schedule.getDataChanges(this);
						schedule.Update();
						break;
				}
				// ensure that the Splits Array is clean before starting.
				//Splits.clear();
				
				/*************************************************************
				 * The following attributes MUST be valid
				 * 
				 * id = String character is SCH000000 format
				 * name = String
				 * type = integer (Declaration constants defined in Schedule.java)
				 * occurence = integer (Declaration constants defined in Schedule.java)
				 * occurenceMultiplier = integer (Frequency number entered by user, number of periods between entries)
				 * startDate = String YYYY-MM-DD format
				 * fixed = Char(1) Y/N
				 * autoEnter = Char(1) Y/N
				 * weekendOption = integer (Declaration constants defined in Schedule.java)
				 */
				/**** Schedule attributes for kmmSchedules ****/
				//String id = null, scheduleName = null, scheduleOccurenceString = null,
				//		scheduleStartDate = null, scheduleEndDate = null, scheduleFixed = null, scheduleAutoEnter = null, scheduleLastPaymentDate = null; 
				//int nSchType, nSchOccurence, nSchOccurenceMultiplier, nWeekendOption, nTransRemaining, nStatus, nPaymentType;
				//boolean schEnds;
				/**** Transaction attributes for kmmTransactions ****/
				//String accountId = null, payeeId = null, categoryId = null, memo = null, entryDate = null, amount = null, ckNumber = null, bankId = null;
				
				//if( Action == ACTION_NEW )
				//	schedule.createScheduleId();
				//else
				//	id = schId;

				// Get the PaymentInfo elements
				//getTabHost().setCurrentTab(0);
				//Activity schedulePaymentInfo = this.getCurrentActivity();
				//scheduleName = ((SchedulePaymentInfoActivity) schedulePaymentInfo).getScheduleName();
				//nSchOccurenceMultiplier = ((SchedulePaymentInfoActivity) schedulePaymentInfo).getScheduleFrequency();
				//scheduleOccurenceString = ((SchedulePaymentInfoActivity) schedulePaymentInfo).getScheduleFrequencyDescription();
				//nSchOccurence = Schedule.getOccurrenceFromMultiplier(/*nSchOccurenceMultiplier,*/ scheduleOccurenceString);
				//nSchType = ((SchedulePaymentInfoActivity) schedulePaymentInfo).getScheduleType();
				//accountId = ((SchedulePaymentInfoActivity) schedulePaymentInfo).getAccountTypeId();
				//payeeId = ((SchedulePaymentInfoActivity) schedulePaymentInfo).getPayeeId();
				//categoryId = ((SchedulePaymentInfoActivity) schedulePaymentInfo).getCategoryId();
				//memo = ((SchedulePaymentInfoActivity) schedulePaymentInfo).getScheduleMemo();
				//scheduleStartDate = ((SchedulePaymentInfoActivity) schedulePaymentInfo).getStartDate();
				//amount = ((SchedulePaymentInfoActivity) schedulePaymentInfo).getScheduleAmount();
				//nStatus = ((SchedulePaymentInfoActivity) schedulePaymentInfo).getScheduleStatus();
				//ckNumber = ((SchedulePaymentInfoActivity) schedulePaymentInfo).getCheckNumber();
				//nPaymentType = ((SchedulePaymentInfoActivity) schedulePaymentInfo).getSchedulePaymentMethod();

				// Get the PaymentOptions elements
				//getTabHost().setCurrentTab(1);
				//Activity scheduleOptions = this.getCurrentActivity();
				//nWeekendOption = ((ScheduleOptionsActivity) scheduleOptions).getScheduleWeekendOption();
				//scheduleFixed = ((ScheduleOptionsActivity) scheduleOptions).getScheduleEstimate();
				//scheduleAutoEnter = ((ScheduleOptionsActivity) scheduleOptions).getScheduleAutoEnter();
				//schEnds = ((ScheduleOptionsActivity) scheduleOptions).getWillScheduleEnd();
				//nTransRemaining = ((ScheduleOptionsActivity) scheduleOptions).getRemainingTransactions();
				//scheduleEndDate = ((ScheduleOptionsActivity) scheduleOptions).getEndDate();
				
				// Build the ContentValues for the schedule.
				//ContentValues scheduleValues = new ContentValues();
				//scheduleValues.put("id", id);
				//scheduleValues.put("name", scheduleName);
				//scheduleValues.put("type", nSchType);
				//scheduleValues.put("typeString", getTypeDescription(nSchType));
				//scheduleValues.put("occurence", nSchOccurence);
				//scheduleValues.put("occurenceMultiplier", nSchOccurenceMultiplier);
				//scheduleValues.put("occurenceString", Schedule.getOccurrenceToString(nSchOccurenceMultiplier, scheduleOccurenceString, this));
				//scheduleValues.put("paymentType", nSchType);
				//scheduleValues.put("paymentTypeString", getPaymentTypeToString(nPaymentType));
				//scheduleValues.put("startDate", scheduleStartDate);
				//if( schEnds && nTransRemaining > 0)
				//	scheduleValues.put("endDate", getEndDate(nTransRemaining, scheduleStartDate, nSchOccurence));
				//else if( schEnds )
				//	scheduleValues.put("endDate", scheduleEndDate);
				//else
				//{
				//	scheduleEndDate = null;
				//	scheduleValues.put("endDate", scheduleEndDate);					
				//}
				//scheduleValues.put("fixed", scheduleFixed);
				//scheduleValues.put("autoEnter", scheduleAutoEnter);
				//scheduleValues.put("lastPayment", scheduleLastPaymentDate);
				//scheduleValues.put("nextPaymentDue", scheduleStartDate);
				//scheduleValues.put("weekendOption", nWeekendOption);
				//scheduleValues.put("weekendOptionString", getWeekendOptionString(nWeekendOption));
				
				// Build the ContentValues for the transaction
				//ContentValues transactionValues = new ContentValues();
				//transactionValues.put("id", id);
				//transactionValues.put("txType", "S");
				//transactionValues.put("postDate", scheduleStartDate);
				//transactionValues.put("memo", memo);
				//transactionValues.put("entryDate", entryDate);
				//Cursor C = KMMDapp.db.query("kmmFileInfo", new String[] { "baseCurrency" }, null, null, null, null, null);
				//C.moveToFirst();
				//transactionValues.put("currencyId", C.getString(0));
				//transactionValues.put("bankId", bankId);
				
				//int numOfSplits = 2;
				//boolean anySplits = false;
				//if( !KMMDapp.Splits.isEmpty() )
				//{
				//	anySplits = true;
				//	numOfSplits = KMMDapp.Splits.size();
				//}
				
				// Build the the splits
				//for( int i=0; i < numOfSplits; i++)
				//{
				//	String value = null, formatted = null;
				//	if(i == 0)
				//	{
				//		if( nSchType == Schedule.TYPE_BILL )
				//		{
				//			value = "-" + Account.createBalance(Transaction.convertToPennies(amount));
				//			formatted = "-" + amount;
				//		}
				//		else
				//		{
				//			value = Account.createBalance(Transaction.convertToPennies(amount));
				//			formatted = amount;							
				//		}
				//	}
				//	else
				//	{
						// If we have splits grab the relevant information from the KMMDapp.Splits object.
				//		if( anySplits )
				//		{
				//			value = KMMDapp.Splits.get(i-1).getValue();
				//			formatted = KMMDapp.Splits.get(i-1).getValueFormatted();
				//			memo = KMMDapp.Splits.get(i-1).getMemo();
				//			accountId = KMMDapp.Splits.get(i-1).getAccountId();
				//		}
				//		else
				//		{
				//			if( nSchType == Schedule.TYPE_BILL )
				//			{
				//				value = Account.createBalance(Transaction.convertToPennies(amount));
				//				formatted = amount;								
				//			}
				//			else
				//			{
				//				value = "-" + Account.createBalance(Transaction.convertToPennies(amount));
				//				formatted = "-" + amount;								
				//			}
				//			accountId = categoryId;
				//		}
				//	}
				//	// Create the actual split for the transaction to be saved.
				//	if(i > 0)
				//		payeeId = null;
				//	Splits.add(new Split(id, "S", i, payeeId, null, null, String.valueOf(nStatus), value, formatted, value, formatted,
				//			null, null, memo, accountId, ckNumber, scheduleStartDate, null, this.widgetId, getBaseContext()));
				//}
/*				switch (Action)
				{
					case ACTION_NEW:
						KMMDapp.db.insertOrThrow("kmmSchedules", null, scheduleValues);
						KMMDapp.updateFileInfo("hiScheduleId", 1);
						KMMDapp.updateFileInfo("schedules", 1);
						KMMDapp.db.insertOrThrow("kmmTransactions", null, transactionValues);
						KMMDapp.updateFileInfo("hiTransactionId", 1);
						KMMDapp.updateFileInfo("transactions", 1);
						KMMDapp.updateFileInfo("splits", Splits.size());
						break;
					case ACTION_EDIT:
						KMMDapp.db.update("kmmSchedules", scheduleValues, "id=?", new String[] { schId });
						KMMDapp.db.update("kmmTransactions", transactionValues, "id=?", new String[] { schId });
						// Delete all the splits for this transaction first, getting the number or rows deleted.
						int rowsDel = KMMDapp.db.delete("kmmSplits", "transactionId=?", new String[] { schId });
						KMMDapp.updateFileInfo("splits", Splits.size() - rowsDel);
						break;
				}
				// Insert the splits for this transaction
				for(int i=0; i < Splits.size(); i++)
				{
					Split s = Splits.get(i);
					s.commitSplit(false);
				}
*/	
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
				
				// Mark the file as dirty.
				KMMDapp.markFileIsDirty(true, "9999");
				
				// need to close the database as it is keeping it open here and causing issues.
				KMMDapp.closeDB();
				finish();
				break;
			case R.id.itemCancel:
				KMMDapp.splitsDestroy();
				finish();
				break;
		}
		
		return true;
	}
	
	public void onSendPaymentInfo() 
	{
		// Send the payment information to the fragment.
		SchedulePaymentInfoActivity schedulePayment = (SchedulePaymentInfoActivity) this.getSupportFragmentManager().findFragmentByTag("paymentinfo");
		
		schedulePayment.setAction(Action);
		schedulePayment.setScheduleName(schedule.getDescription());
		schedulePayment.setScheduleFrequency(schedule.getOccurenceMultiplier());
		schedulePayment.setScheduleFrequencyDescription(schedule.getOccurence());
		schedulePayment.setSchedulePaymentMethod(schedule.getPaymentType());
		schedulePayment.setScheduleType(schedule.getType());
		schedulePayment.setAccountTypeId(schedule.Transaction.splits.get(0).getAccountId());
		schedulePayment.setPayeeId(schedule.Transaction.splits.get(0).getPayeeId());
		//schedulePayment.setSplits(Splits);
		schedulePayment.setCheckNumber(schedule.Transaction.splits.get(0).getCheckNumber());
		schedulePayment.setStartDate(schedule.convertDate(schedule.getStartDate()));
		schedulePayment.setScheduleAmount(schedule.Transaction.splits.get(0).getValueFormatted());
		schedulePayment.setScheduleStatus(Integer.valueOf(schedule.Transaction.splits.get(0).getReconcileFlag()));
		schedulePayment.setScheduleMemo(schedule.Transaction.getMemo());		
	}
	
	public void onSendOptions() 
	{
		// Send the payment information to the fragment.
		ScheduleOptionsActivity scheduleOptions = (ScheduleOptionsActivity) this.getSupportFragmentManager().findFragmentByTag("options");
		
		scheduleOptions.setScheduleWeekendOption(schedule.getWeekendOption());
		scheduleOptions.setScheduleIsEstimate(schedule.getIsEstimate());
		scheduleOptions.setScheduleAutoEnter(schedule.getAutoEnter());
		scheduleOptions.setEndDate(schedule.getEndDate().toString());		
	}
	// ************************************************************************************************
	// ******************************* Helper Functions ***********************************************
/*	private String createScheduleId()
	{
		final String[] dbColumns = { "hiScheduleId"};
		final String strOrderBy = "hiAccountId DESC";
		// Run a query to get the hi schedule id so we can create a new one.
		cursor = KMMDapp.db.query("kmmFileInfo", dbColumns, null, null, null, null, strOrderBy);
		startManagingCursor(cursor);
		
		cursor.moveToFirst();

		// Since id is in SCH000000 format, we need to pick off the actual number then increase by 1.
		int lastId = cursor.getInt(0);
		lastId = lastId +1;
		String nextId = Integer.toString(lastId);
		
		// Need to pad out the number so we get back to our SCH000000 format
		String newId = "SCH";
		for(int i= 0; i < (6 - nextId.length()); i++)
		{
			newId = newId + "0";
		}
		
		// Tack on the actual number created.
		newId = newId + nextId;
		
		return newId;
	}
*/
	private String getTypeDescription(int type)
	{
		switch(type)
		{
			case Schedule.TYPE_ANY:
				return "Any";
			case Schedule.TYPE_BILL:
				return "Bill";
			case Schedule.TYPE_DEPOSIT:
				return "Deposit";
			case Schedule.TYPE_TRANSFER:
				return "Transfer";
			case Schedule.TYPE_LOANPAYMENT:
				return "Loan payment";
		}
		
		return null;
	}
	
	private String getPaymentTypeToString(int type)
	{
		switch(type)
		{
			case Schedule.PAYMENT_TYPE_ANY:
				return "Any (Error)";
			case Schedule.PAYMENT_TYPE_BANKTRANSFER:
				return "Bank transfer";
			case Schedule.PAYMENT_TYPE_DIRECTDEBIT:
				return "Direct debit";
			case Schedule.PAYMENT_TYPE_DIRECTDEPOSIT:
				return "Direct deposit";
			case Schedule.PAYMENT_TYPE_MANUALDEPOSIT:
				return "Manual deposit";
			case Schedule.PAYMENT_TYPE_OTHER:
				return "Other";
			case Schedule.PAYMENT_TYPE_STANDINGORDER:
				return "Standing order";
			case Schedule.PAYMENT_TYPE_WRITECHECK:
				return "Write check";
			default:
				return "Any (Error)";
		}
	}
	
	private String getEndDate(int numOfTrans, String startDate, int Frequency)
	{
		String strDates[] = startDate.split("-");
		GregorianCalendar calDate = new GregorianCalendar();
		calDate.set(Integer.valueOf(strDates[0]), Integer.valueOf(strDates[1]) - 1, Integer.valueOf(strDates[2]));
		
		switch (Frequency)
		{
			case Schedule.OCCUR_ONCE:
				break;
			case Schedule.OCCUR_DAILY:
				calDate.add(Calendar.DAY_OF_MONTH, (1 * Frequency));
				break;
			case Schedule.OCCUR_WEEKLY:
				calDate.add(Calendar.DAY_OF_MONTH, (7 * Frequency));
				break;
			case Schedule.OCCUR_FORTNIGHTLY:
			case Schedule.OCCUR_EVERYOTHERWEEK:
				calDate.add(Calendar.DAY_OF_MONTH, (14 * Frequency));
				break;
			case Schedule.OCCUR_EVERYTHREEWEEKS:
					calDate.add(Calendar.DAY_OF_MONTH, (21 * Frequency));
				break;
			case Schedule.OCCUR_EVERYTHIRTYDAYS:
				calDate.add(Calendar.DAY_OF_MONTH, (30 * Frequency));
				break;
			case Schedule.OCCUR_MONTHLY:
				calDate.add(Calendar.MONTH, (1 * Frequency));
				break;
			case Schedule.OCCUR_EVERYFOURWEEKS:
				calDate.add(Calendar.DAY_OF_MONTH, (28 * Frequency));
				break;
			case Schedule.OCCUR_EVERYEIGHTWEEKS:
				calDate.add(Calendar.DAY_OF_MONTH, (56 * Frequency));				
				break;
			case Schedule.OCCUR_EVERYOTHERMONTH:
				calDate.add(Calendar.MONTH, (2 * Frequency));
				break;
			case Schedule.OCCUR_EVERYTHREEMONTHS:
			case Schedule.OCCUR_QUARTERLY:
				calDate.add(Calendar.MONTH, (3 * Frequency));
				break;
			case Schedule.OCCUR_TWICEYEARLY:
				calDate.add(Calendar.MONTH, (6 * Frequency));
				break;
			case Schedule.OCCUR_EVERYOTHERYEAR:
				calDate.add(Calendar.YEAR, (2 * Frequency));				
				break;
			case Schedule.OCCUR_EVERYFOURMONTHS:
				calDate.add(Calendar.MONTH, (4 * Frequency));
				break;
			case Schedule.OCCUR_YEARLY:
				calDate.add(Calendar.YEAR, (1 * Frequency));
				break;
			default:
				break;
		}
		
		return formatDateforDatabase(calDate.get(Calendar.YEAR), calDate.get(Calendar.MONTH) + 1, calDate.get(Calendar.DAY_OF_MONTH));
	}
	
	private String formatDateforDatabase(int Year, int Month, int Day)
	{
		String strDay = null;
		String strMonth = null;
		switch(Day)
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
				strDay = "0" + String.valueOf(Day);
				break;
			default:
				strDay = String.valueOf(Day);
			break;
		}
		
		switch(Month)
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
				strMonth = "0" + String.valueOf(Month);
				break;
			default:
				strMonth = String.valueOf(Month);
				break;
		}
		
		return new StringBuilder()
					// Month is 0 based so add 1
					.append(strMonth).append("-")
					.append(strDay).append("-")
					.append(Year).toString();
	}
	
	private String getWeekendOptionString(int option)
	{
		switch(option)
		{
		case Schedule.MOVE_AFTER:
			return "Change the date to the next processing day";
		case Schedule.MOVE_BEFORE:
			return "Change the date to the previous pocessing day";
		case Schedule.MOVE_NONE:
		default:
			return "Do Nothing";
		}
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
								 cursor.getString(C_POSTDATE), cursor.getString(C_BANKID), this.widgetId, getBaseContext()) );
			cursor.moveToNext();
		}
		
		cursor.close();
		return splits;
	}
	
	public void setIsDirty(boolean flag)
	{
		this.isDirty = flag;
	}
	
	public Boolean getIsDirty()
	{
		return this.isDirty;
	}
	
	/**
	 * Initialise the Tab Host
	 */
	private void initialiseTabHost(Bundle args) 
	{
		tabHost = (TabHost)findViewById(android.R.id.tabhost);
        tabHost.setup();
        TabInfo tabInfo = null;

        // Add the fragment for PaymentInfo
        CreateModifyScheduleActivity.addTab(this, this.tabHost, 
        		this.tabHost.newTabSpec("paymentinfo").setIndicator(getString(R.string.titleSchedulePaymentInfo)),
        		( tabInfo = new TabInfo("paymentinfo", SchedulePaymentInfoActivity.class, args)));
        this.mapTabInfo.put(tabInfo.tag, tabInfo);
        
        // Add the fragment for the Options
        CreateModifyScheduleActivity.addTab(this, this.tabHost,
        		this.tabHost.newTabSpec("options").setIndicator(getString(R.string.titleScheduleOptions)),
        		( tabInfo = new TabInfo("options", ScheduleOptionsActivity.class, args)));
        this.mapTabInfo.put(tabInfo.tag, tabInfo);
        
        // Default to first tab
        this.onTabChanged("paymentinfo");
        
        // Set the listener for the tab host.
        tabHost.setOnTabChangedListener(this);
	}
	
	/**
	 * @param activity
	 * @param tabHost
	 * @param tabSpec
	 * @param clss
	 * @param args
	 */
	private static void addTab(CreateModifyScheduleActivity activity, TabHost tabHost, TabHost.TabSpec tabSpec, TabInfo tabInfo) 
	{
		// Attach a Tab view factory to the spec
		tabSpec.setContent(activity.new TabFactory(activity));
        String tag = tabSpec.getTag();

        // Check to see if we already have a fragment for this tab, probably
        // from a previously saved state.  If so, deactivate it, because our
        // initial state is that a tab isn't shown.
        tabInfo.fragment = activity.getSupportFragmentManager().findFragmentByTag(tag);
        if (tabInfo.fragment != null && !tabInfo.fragment.isDetached()) 
        {
            FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();
            ft.detach(tabInfo.fragment);
            ft.commit();
            activity.getSupportFragmentManager().executePendingTransactions();
        }

        tabHost.addTab(tabSpec);
	}
	
	private void saveTabUI(Fragment tab)
	{
		String tag = tab.getTag();
		SharedPreferences prefs = getPreferences(Activity.MODE_PRIVATE);
		SharedPreferences.Editor edit = prefs.edit();
		
	}
	
	public void editSchedule() 
	{
		// If we are coming back from splits entry screen follow this path.
		if( this.ReturningFromSplits )
		{
			SchedulePaymentInfoActivity schedulePayment = (SchedulePaymentInfoActivity) this.getSupportFragmentManager().findFragmentByTag("paymentinfo");

			String name = getResources().getString(R.string.splitTransaction);
			schedulePayment.catFrag.setCategoryName(name);
			//setupSplitInfo();
			//editCategory.setText(R.string.splitTransaction);
			//btnCategory.setEnabled(false);
			//spinPayee.setSelection(setPayee(strSchPayeeId));
			//iNumberofPasses = 0;
		}
/*		else
		{	
			// load the schedule details into the form.
			// set the spinner for the frequency and freq description of the schedule.
			spinFreqNum.setSelection(intSchFreq);
			spinFreqDesc.setSelection(intSchFreqDesc);
			
			// set the spinner for payment method
			spinPaymentMethod.setSelection(intSchPaymentMethodPos);
			
			// set the spinner for the transaction type
			spinSchType.setSelection(intSchTypePos);
			
			//intSchPayeePos = setPayee(strSchPayeeId);
			payeeFrag.setPayeeId(strSchPayeeId);
			//spinPayee.setSelection(intSchPayeePos);
		
			// See if we have only used one category or if we have multiple.
			if( Splits.size() == 2 )
			{
				Cursor c = KMMDapp.db.query("kmmAccounts", new String[] { "accountName" }, "id=?",
										new String[] { Splits.get(1).getAccountId() }, null, null, null);
				startManagingCursor(c);
				c.moveToFirst();
				editCategory.setText(c.getString(0));
				iNumberofPasses = 0;
				intSchStatus = Integer.valueOf(Splits.get(0).getReconcileFlag());

				c.close();
				numOfSplits = 2;
				anySplits = false;
				
				// Populate the category used for this split only.
				strSchCategoryId = Splits.get(1).getAccountId();
				//spinCategory.setSelection(setCategoryUsed(strSchCategoryId));
			}
			else
			{
				// need to put the splits into the KMMDapp.Splits object so user may edit the split details.
				KMMDapp.splitsInit();
				for(int i = 1; i < Splits.size(); i++)
					KMMDapp.Splits.add(Splits.get(i));
			
				for(int i = 0; i < KMMDapp.Splits.size(); i++)
					 KMMDapp.Splits.get(i).dump();
				iNumberofPasses = 0;
				editCategory.setText(R.string.splitTransaction);
				//btnCategory.setEnabled(false);
				numOfSplits = Splits.size();
				anySplits = true;
			}
		
			float amount = Float.valueOf(Splits.get(0).getValueFormatted());
			if( amount < 0 )
			{
				intSchType = Schedule.TYPE_BILL;
				amount = amount * -1;		//change the sign of the amount for the form only.
			}
			else
				intSchType = Schedule.TYPE_DEPOSIT;
			
			editAmount.setText(String.valueOf(amount));
			
			// Need to populate the Account used for this transaction.
			//intSchAccountPos = setAccountUsed(strSchAccountId);
			accountFrag.setAccountId(strSchAccountId);
			//spinSchAccount.setSelection(intSchAccountPos);
		
			// Make a copy of the original transactions split for later use if we modify anything.
			for(int i=0; i < Splits.size(); i++)
				OrigSplits.add(Splits.get(i));
			
			updateDisplay();
		}
*/
	}
	
	private void enterSchedule()
	{
		
	}
	
	public int getNumberOfSplits()
	{
		return this.numOfSplits;
	}
	
	public boolean getHasSplits()
	{
		return this.ReturningFromSplits;
	}
	// *****************************************************************************************************************************
	// ********************************************** Helper Classes ***************************************************************

	/**
	 * 
	 * @author mwho
	 *
	 */
	private class TabInfo 
	{
		 private String tag;
         private Class<?> clss;
         private Bundle args;
         private Fragment fragment;
         
         TabInfo(String tag, Class<?> clazz, Bundle args) 
         {
        	 this.tag = tag;
        	 this.clss = clazz;
        	 this.args = args;
         }
	}
	
	/**
	 * 
	 * @author mwho
	 *
	 */
	class TabFactory implements TabContentFactory 
	{
		private final Context mContext;

	    /**
	     * @param context
	     */
	    public TabFactory(Context context) 
	    {
	        mContext = context;
	    }

	    /** (non-Javadoc)
	     * @see android.widget.TabHost.TabContentFactory#createTabContent(java.lang.String)
	     */
	    public View createTabContent(String tag) 
	    {
	        View v = new View(mContext);
	        v.setMinimumWidth(0);
	        v.setMinimumHeight(0);
	        return v;
	    }
	}
}
