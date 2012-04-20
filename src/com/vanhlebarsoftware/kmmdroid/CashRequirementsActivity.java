package com.vanhlebarsoftware.kmmdroid;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.lang.Math;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class CashRequirementsActivity extends Activity
{
	public static final String TAG = "CashRequirementsActivity";
	private static final int OCCUR_ONCE = 1;
	private static final int OCCUR_DAILY = 2;
	private static final int OCCUR_WEEKLY = 4;
	private static final int OCCUR_FORTNIGHTLY = 8;
	private static final int OCCUR_EVERYOTHERWEEK = 16;
	private static final int OCCUR_EVERYTHREEWEEKS = 20;
	private static final int OCCUR_EVERYTHIRTYDAYS = 30;
	private static final int OCCUR_MONTHLY = 32;
	private static final int OCCUR_EVERYFOURWEEKS = 64;
	private static final int OCCUR_EVERYEIGHTWEEKS = 126;
	private static final int OCCUR_EVERYOTHERMONTH = 128;
	private static final int OCCUR_EVERYTHREEMONTHS = 256;
	private static final int OCCUR_TWICEYEARLY = 1024;
	private static final int OCCUR_EVERYOTHERYEAR = 2048;
	private static final int OCCUR_QUARTERLY = 4096;
	private static final int OCCUR_EVERYFOURMONTHS = 8192;
	private static final int OCCUR_YEARLY = 16384;
	private static final int C_DESCRIPTION = 1;
	private static final int C_OCCURENCE = 2;
	private static final int C_OCCURENCESTRING = 3;
	private static final int C_OCCURENCEMULTIPLIER = 4;
	private static final int C_NEXTPAYMENTDUE = 5;
	private static final int C_STARTDATE = 6;
	private static final int C_ENDDATE = 7;
	private static final int C_LASTPAYMENT = 8;
	private static final int C_VALUEFORMATTED = 9;
	private static final String dbTable = "kmmSchedules, kmmSplits";
	private static final String[] dbColumns = { "kmmSchedules.id AS _id", "kmmSchedules.name AS Description", "occurence", "occurenceString", "occurenceMultiplier",
												"nextPaymentDue", "startDate", "endDate", "lastPayment", "valueFormatted" };
	private static final String strSelection = "kmmSchedules.id = kmmSplits.transactionId AND nextPaymentDue > 0" + 
												" AND ((occurenceString = 'Once' AND lastPayment IS NULL) OR occurenceString != 'Once')" +
												" AND kmmSplits.splitId = 0 AND kmmSplits.accountId=?";
	private static final String strOrderBy = "nextPaymentDue ASC";
	private int nAccountBalance = 0;
	private String strStartDate = null;
	private String strEndDate = null;
	private String strAccountId = null;
	private long nBegBalance = 0;
	ArrayList<Schedule> Schedules;
	KMMDroidApp KMMDapp;
	Cursor cursor;
	ListView listSchedules;
	TextView noSchedules;
	ScheduleAdapter adapter;
	
	/* Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
        setContentView(R.layout.cashrequirements);

        // Get our application
        KMMDapp = ((KMMDroidApp) getApplication());
        
        // Find our views
        listSchedules = (ListView) findViewById(R.id.listCashRequirements);
        noSchedules = (TextView) findViewById(R.id.titleNoSchedules);
        
        // Get the action the user is doing.
        Bundle extras = getIntent().getExtras();
        strAccountId = extras.getString("Account");
        nBegBalance = extras.getLong("AccountBalance");
        strStartDate = extras.getString("BegDate");
        strEndDate = extras.getString("EndDate");
        
        // Make sure our dates are in YYYY-MM-DD format
        strStartDate = formatDate(strStartDate);
        strEndDate = formatDate(strEndDate);
        
        // See if the database is already open, if not open it Read/Write.
        if(!KMMDapp.isDbOpen())
        {
        	KMMDapp.openDB();
        }
        
        Schedules = new ArrayList<Schedule>();
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
		
		//Run the query on the database to get the transactions.
		cursor = KMMDapp.db.query(dbTable, dbColumns, strSelection, new String[] { strAccountId }, null, null, strOrderBy, null);
		startManagingCursor(cursor);
		
		BuildCashRequired(cursor);
		
		if(Schedules.size() > 0)
		{
			listSchedules.setVisibility(View.VISIBLE);
			noSchedules.setVisibility(View.GONE);
			// Set up the adapter
			adapter = new ScheduleAdapter(this, R.layout.cashrequirements_row, Schedules);
			listSchedules.setAdapter(adapter);
		}
		else
		{
			listSchedules.setVisibility(View.GONE);
			noSchedules.setVisibility(View.VISIBLE);
			Log.d(TAG, "We didn't get any schedules!");
		}
	}
	
	// Called first time the user clicks on the menu button
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.schedules_menu, menu);
		return true;
	}
	
	// Called when an options item is clicked
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.itemHome:
				startActivity(new Intent(this, HomeActivity.class));
				break;
			case R.id.itemAccounts:
				startActivity(new Intent(this, AccountsActivity.class));
				break;
			case R.id.itemInstitutions:
				startActivity(new Intent(this, InstitutionsActivity.class));
				break;
			case R.id.itemPayees:
				startActivity(new Intent(this, PayeeActivity.class));
				break;
			case R.id.itemCategories:
				startActivity(new Intent(this, CategoriesActivity.class));
				break;
			case R.id.itemPrefs:
				startActivity(new Intent(this, PrefsActivity.class));
				break;
			case R.id.itemAbout:
				startActivity(new Intent(this, AboutActivity.class));
				break;
		}
		
		return true;
	}
	
	// **************************************************************************************************
	// ************************************ Helper methods **********************************************
	
	/* Take a look at the mymoneyschedule.cpp for the following routines:
	 * 		paymentDates() - returns a list of payment dates between two given dates.
	 * 		transactionsRemaining() - calculates how many transactions are left if a schedule has an endDate
	 * 		isFinished()
	 * 		isOverDue()
	 */
	private void BuildCashRequired(Cursor c)
	{
		Schedule schd = null;
		ArrayList<Calendar> dueDates = new ArrayList<Calendar>();
		// Make sure we are at the beginning of the dataset
		c.moveToFirst();
		
		// Loop over the dataset and expand each of the cases out the date the user entered.
		for(int i=0; i < c.getCount(); i++)
		{
			dueDates = paymentDates(strStartDate, strEndDate, getOccurence(c.getInt(C_OCCURENCE), c.getInt(C_OCCURENCEMULTIPLIER)), c.getString(C_STARTDATE), c.getString(C_NEXTPAYMENTDUE));

			// We now have all our payment dates for this schedule between the dates the user supplied.
			// Let's create the Schedule class and add it to the ArrayList.
			for(int d=0; d < dueDates.size(); d++)
			{
				schd = new Schedule(c.getString(C_DESCRIPTION), dueDates.get(d), c.getString(C_VALUEFORMATTED));
				Schedules.add(schd);
				schd = null;
			}
			
			// Get the next record.
			c.moveToNext();
		}
		
		// Need to bubble sort here based on the due date.
		ScheduleComparator comparator = new ScheduleComparator();
		Collections.sort(Schedules, comparator);
		
		// Need to add a routine to add in the balance after each transaction is to the ArrayList.
		for(int i = 0; i < Schedules.size(); i++)
		{
			nBegBalance = Schedules.get(i).calcBalance(nBegBalance);
		}
	}
	
	private ArrayList<Calendar> paymentDates(String strStart, String strEnd, int occurence, String scheduleStartDate, String nextPaymentDate)
	{
		ArrayList<Calendar> Dates = new ArrayList<Calendar>();
		Dates.clear();
		String strDates[] = null;
		
		// Setup our Dates.
		// Since the Month of a Calendar object is zero based, ie January = 0, we need to substract one from the SQL date returned.
		GregorianCalendar calStart = new GregorianCalendar();
		GregorianCalendar calEnd = new GregorianCalendar();
		GregorianCalendar calScheduleStart = new GregorianCalendar();
		GregorianCalendar calNextPaymentDate = new GregorianCalendar();
		strDates = strStart.split("-");
		calStart.set(Integer.valueOf(strDates[0]), Integer.valueOf(strDates[1]) - 1, Integer.valueOf(strDates[2]));
		strDates = strEnd.split("-");
		calEnd.set(Integer.valueOf(strDates[0]), Integer.valueOf(strDates[1]) - 1, Integer.valueOf(strDates[2]));
		strDates = scheduleStartDate.split("-");
		calScheduleStart.set(Integer.valueOf(strDates[0]), Integer.valueOf(strDates[1]) - 1, Integer.valueOf(strDates[2]));
		strDates = nextPaymentDate.split("-");
		calNextPaymentDate.set(Integer.valueOf(strDates[0]), Integer.valueOf(strDates[1]) - 1, Integer.valueOf(strDates[2]));
		GregorianCalendar date = (GregorianCalendar) calNextPaymentDate.clone();
		calNextPaymentDate.setLenient(false);
		
		switch (occurence)
		{
			case OCCUR_ONCE:
				if(calScheduleStart.before(calEnd))
					Dates.add(calScheduleStart);
				break;
			case OCCUR_DAILY:
				while(calNextPaymentDate.before(calEnd))
				{
					Dates.add(date);
					calNextPaymentDate.add(Calendar.DAY_OF_MONTH, 1);
					date = (GregorianCalendar) calNextPaymentDate.clone();
				}
				break;
			case OCCUR_WEEKLY:
				while(calNextPaymentDate.before(calEnd))
				{
					Dates.add(date);
					calNextPaymentDate.add(Calendar.DAY_OF_MONTH, 7);
					date = (GregorianCalendar) calNextPaymentDate.clone();
				}			
				break;
			case OCCUR_FORTNIGHTLY:
			case OCCUR_EVERYOTHERWEEK:
				while(calNextPaymentDate.before(calEnd))
				{
					Dates.add(date);
					calNextPaymentDate.add(Calendar.DAY_OF_MONTH, 14);
					date = (GregorianCalendar) calNextPaymentDate.clone();
				}
				break;
			case OCCUR_EVERYTHREEWEEKS:
				while(calNextPaymentDate.before(calEnd))
				{
					Dates.add(date);
					calNextPaymentDate.add(Calendar.DAY_OF_MONTH, 21);
					date = (GregorianCalendar) calNextPaymentDate.clone();
				}
				break;
			case OCCUR_EVERYTHIRTYDAYS:
				while(calNextPaymentDate.before(calEnd))
				{
					Dates.add(date);
					calNextPaymentDate.add(Calendar.DAY_OF_MONTH, 30);
					date = (GregorianCalendar) calNextPaymentDate.clone();
				}
				break;
			case OCCUR_MONTHLY:
				while(calNextPaymentDate.before(calEnd))
				{
					Dates.add(date);
					calNextPaymentDate.add(Calendar.MONTH, 1);
					date = (GregorianCalendar) calNextPaymentDate.clone();
				}
				break;
			case OCCUR_EVERYFOURWEEKS:
				while(calNextPaymentDate.before(calEnd))
				{
					Dates.add(date);
					calNextPaymentDate.add(Calendar.DAY_OF_MONTH, 28);
					date = (GregorianCalendar) calNextPaymentDate.clone();
				}
				break;
			case OCCUR_EVERYEIGHTWEEKS:
				while(calNextPaymentDate.before(calEnd))
				{
					Dates.add(date);
					calNextPaymentDate.add(Calendar.DAY_OF_MONTH, 56);
					date = (GregorianCalendar) calNextPaymentDate.clone();
				}				
				break;
			case OCCUR_EVERYOTHERMONTH:
				while(calNextPaymentDate.before(calEnd))
				{
					Dates.add(date);
					calNextPaymentDate.add(Calendar.MONTH, 2);
					date = (GregorianCalendar) calNextPaymentDate.clone();
				}
				break;
			case OCCUR_EVERYTHREEMONTHS:
			case OCCUR_QUARTERLY:
				while(calNextPaymentDate.before(calEnd))
				{
					Dates.add(date);
					calNextPaymentDate.add(Calendar.MONTH, 3);
					date = (GregorianCalendar) calNextPaymentDate.clone();
				}
				break;
			case OCCUR_TWICEYEARLY:
				while(calNextPaymentDate.before(calEnd))
				{
					Dates.add(date);
					calNextPaymentDate.add(Calendar.MONTH, 6);
					date = (GregorianCalendar) calNextPaymentDate.clone();
				}
				break;
			case OCCUR_EVERYOTHERYEAR:
				while(calNextPaymentDate.before(calEnd))
				{
					Dates.add(date);
					calNextPaymentDate.add(Calendar.YEAR, 2);
					date = (GregorianCalendar) calNextPaymentDate.clone();
				}				
				break;
			case OCCUR_EVERYFOURMONTHS:
				while(calNextPaymentDate.before(calEnd))
				{
					Dates.add(date);
					calNextPaymentDate.add(Calendar.MONTH, 4);
					date = (GregorianCalendar) calNextPaymentDate.clone();
				}
				break;
			case OCCUR_YEARLY:
				while(calNextPaymentDate.before(calEnd))
				{
					Dates.add(date);
					calNextPaymentDate.add(Calendar.YEAR, 1);
					date = (GregorianCalendar) calNextPaymentDate.clone();
				}
				break;
			default:
				break;
		}
		return Dates;
	}
	
	private int getOccurence(int nOccurence, int nOccurenceMultiple)
	{
		switch (nOccurence)
		{
			case OCCUR_ONCE:
				return OCCUR_ONCE;
			case OCCUR_DAILY:
				switch(nOccurenceMultiple)
				{
					case 1:
						return OCCUR_DAILY;
					case 30:
						return OCCUR_EVERYTHIRTYDAYS;
				}
			case OCCUR_WEEKLY:
				switch(nOccurenceMultiple)
				{
					case 1:
						return OCCUR_WEEKLY;
					case 2:
						return OCCUR_EVERYOTHERWEEK;
					case 3:
						return OCCUR_EVERYTHREEWEEKS;
					case 4:
						return OCCUR_EVERYFOURWEEKS;
					case 8:
						return OCCUR_EVERYEIGHTWEEKS;
				}
			case OCCUR_MONTHLY:
				switch(nOccurenceMultiple)
				{
					case 1:
						return OCCUR_MONTHLY;
					case 2:
						return OCCUR_EVERYOTHERMONTH;
					case 3:
						return OCCUR_QUARTERLY;
					case 4:
						return OCCUR_EVERYFOURMONTHS;
				}
			case OCCUR_YEARLY:
				switch(nOccurenceMultiple)
				{
					case 1:
						return OCCUR_YEARLY;
					case 2:
						return OCCUR_TWICEYEARLY;
				}
		}
		return 0;
	}
	
	private String formatDate(String date)
	{
		// We need to reverse the order of the date to be YYYY-MM-DD for SQL
		String dates[] = date.split("-");
		
		return new StringBuilder()
		.append(dates[2]).append("-")
		.append(dates[0]).append("-")
		.append(dates[1]).toString();
	}
	
	private class Schedule
	{
		private String Description;
		private Calendar DueDate;
		private long nAmount;		//Holds the amount of this transaction in pennies.
		private long nBalance;		//Hold the balance AFTER this transactions occurs. This is calculated and only for the Cash Required report.
									//Held in pennies.
		
		// Constructor for a Schedule
		Schedule(String Desc, Calendar dueDate, String strAmt)
		{
			this.Description = Desc;
			this.DueDate = dueDate;
			this.nAmount = convertToPennies(strAmt);
			this.nBalance = 0;
		}
		
		/********************************************************************************************
		* Adapted from code found at currency : Java Glossary
		* website: http://mindprod.com/jgloss/currency.html
		********************************************************************************************/
		private long convertToPennies(String numStr)
		{
			numStr = numStr.trim ();
			// strip commas, spaces, + etc
			StringBuffer b = new StringBuffer( numStr.length() );
			boolean negative = false;
			int decpl = -1;
			for ( int i=0; i<numStr.length(); i++ )
			{
				char c = numStr.charAt( i );
			    switch ( c )
			    {
			    	case '-' :
			    		negative = true;
			            break;
			        case '.' :
			        	if ( decpl == -1 )
			            {
			               decpl = 0;
			            }
			            else
			            {
			               throw new NumberFormatException( "more than one decimal point" );
			            }
			            break;
			        case '0' :
			        case '1' :
			        case '2' :
			        case '3' :
			        case '4' :
			        case '5' :
			        case '6' :
			        case '7' :
			        case '8' :
			        case '9' :
			        	if ( decpl != -1 )
			            {
			               decpl++;
			            }
			            b.append(c);
			            break;
			        default:
			        	// ignore junk chars
			            break;
			    }
			    // end switch
			}
			// end for
			if ( numStr.length() != b.length() )
			{
				numStr = b.toString();
			}
			if ( numStr.length() == 0 )
			{
				return 0;
			}
			long num = Long.parseLong( numStr );
			if ( decpl == -1 || decpl == 0 )
			{
				num *= 100;
			}
			else if ( decpl == 1 )
			{
				num *= 10;
			}
			else if ( decpl == 2 )
			{
				/* it is fine as is */
			}
			else
			{
				throw new NumberFormatException( "wrong number of decimal places." );
			}
			if ( negative )
			{
				num = -num;
			}
			return num;
		}

		/********************************************************************************************
		* Adapted from code found at currency : Java Glossary
		* website: http://mindprod.com/jgloss/currency.html
		********************************************************************************************/
		private String convertToDollars(long pennies)
		{
			boolean negative;
			if ( pennies < 0 )
			{
				pennies = -pennies;
			    negative = true;
			}
			else
				negative = false;

			String s = Long.toString( pennies );
			String strNumber = String.format("%,d", (pennies / 100));
			int len = s.length();
			switch ( len )
			{
				case 1:
			        s = "0.0" + s;
			        break;
			    case 2:
			        s = "0." + s;
			        break;
			    default:
			    	strNumber = strNumber + "." + s.substring(len-2, len);
			        break;
			} // end switch
			
			if ( negative )
				strNumber = "(" + strNumber + ")";

			return strNumber;
		}
		
		private long calcBalance(long nPrevBal)
		{
			this.nBalance = nPrevBal + this.nAmount;
			
			return this.nBalance; 
		}
		
		private String formatDateString()
		{
			Log.d(TAG, "Date: " + DueDate.toString());
			return String.valueOf(this.DueDate.get(Calendar.MONTH) + 1) + "/" + String.valueOf(this.DueDate.get(Calendar.DAY_OF_MONTH)) +
					"/" + String.valueOf(this.DueDate.get(Calendar.YEAR));
		}
	}
	
	public class ScheduleComparator implements Comparator<Schedule>
	{
		public int compare(Schedule arg0, Schedule arg1) 
		{
			// TODO Auto-generated method stub
			return arg0.DueDate.compareTo(arg1.DueDate);
		}
	}
	
	private class ScheduleAdapter extends ArrayAdapter<Schedule>
	{
		private ArrayList<Schedule> items;
		private Context context;
		
		public ScheduleAdapter(Context context, int textViewResourceId, ArrayList<Schedule> items)
		{
			super(context, textViewResourceId, items);
			this.context = context;
			this.items = items;
		}
		
		public View getView(int position, View convertView, ViewGroup parent)
		{
			View view = convertView;
			if(view == null)
			{
				LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				view = inflater.inflate(R.layout.cashrequirements_row, null);
			}
			
			Schedule item = items.get(position);
			// Load the items into the view now for this schedule.
			if(item != null)
			{
				TextView DueDate = (TextView) view.findViewById(R.id.crDueDate);
				TextView Description = (TextView) view.findViewById(R.id.crDescription);
				TextView Amount = (TextView) view.findViewById(R.id.crAmount);
				TextView Balance = (TextView) view.findViewById(R.id.crBalance);
				
				DueDate.setText(item.formatDateString());
				Description.setText(item.Description);
				Log.d(TAG, item.Description + ": " + String.valueOf(item.nAmount));
				Amount.setText(item.convertToDollars(item.nAmount));
				Balance.setText(item.convertToDollars(item.nBalance));
			}
			else
				Log.d(TAG, "Never got a Schedule!");
			
			return view;
		}
	}
}
