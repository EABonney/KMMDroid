package com.vanhlebarsoftware.kmmdroid;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class Schedule 
{
	private static final String TAG = Schedule.class.getSimpleName();
	
	/******** Occurence constants ***********/
	public static final int OCCUR_ANY = 0;
	public static final int OCCUR_ONCE = 1;
	public static final int OCCUR_DAILY = 2;
	public static final int OCCUR_WEEKLY = 4;
	public static final int OCCUR_FORTNIGHTLY = 8;
	public static final int OCCUR_EVERYOTHERWEEK = 16;
	public static final int OCCUR_EVERYHALFMONTH = 18;
	public static final int OCCUR_EVERYTHREEWEEKS = 20;
	public static final int OCCUR_EVERYTHIRTYDAYS = 30;
	public static final int OCCUR_MONTHLY = 32;
	public static final int OCCUR_EVERYFOURWEEKS = 64;
	public static final int OCCUR_EVERYEIGHTWEEKS = 126;
	public static final int OCCUR_EVERYOTHERMONTH = 128;
	public static final int OCCUR_EVERYTHREEMONTHS = 256;
	public static final int OCCUR_TWICEYEARLY = 1024;
	public static final int OCCUR_EVERYOTHERYEAR = 2048;
	public static final int OCCUR_QUARTERLY = 4096;
	public static final int OCCUR_EVERYFOURMONTHS = 8192;
	public static final int OCCUR_YEARLY = 16384;
	
	/********** Schedule type constants **************/
	public static final int TYPE_ANY = 0;
	public static final int TYPE_BILL = 1;
	public static final int TYPE_DEPOSIT = 2;
	public static final int TYPE_TRANSFER = 3;
	public static final int TYPE_LOANPAYMENT = 4;
	
	/********** Schedule Payment Type constants ********/
	public static final int PAYMENT_TYPE_ANY = 0;
	public static final int PAYMENT_TYPE_DIRECTDEBIT = 1;
	public static final int PAYMENT_TYPE_DIRECTDEPOSIT = 2;
	public static final int PAYMENT_TYPE_MANUALDEPOSIT = 4;
	public static final int PAYMENT_TYPE_OTHER = 8;
	public static final int PAYMENT_TYPE_WRITECHECK = 16;
	public static final int PAYMENT_TYPE_STANDINGORDER = 32;
	public static final int PAYMENT_TYPE_BANKTRANSFER = 64;
	
	/********* Schedule Weekend Option constants *******/
	public static final int MOVE_BEFORE = 0;
	public static final int MOVE_AFTER = 1;
	public static final int MOVE_NONE = 2;
	
	public static final int C_ID = 0;
	public static final int C_DESCRIPTION = 1;
	public static final int C_OCCURENCE = 2;
	public static final int C_OCCURENCESTRING = 3;
	public static final int C_OCCURENCEMULTIPLIER = 4;
	public static final int C_NEXTPAYMENTDUE = 5;
	public static final int C_STARTDATE = 6;
	public static final int C_ENDDATE = 7;
	public static final int C_LASTPAYMENT = 8;
	public static final int C_VALUEFORMATTED = 9;
	public static final int C_AUTOENTER = 10;
	
	/***** Additional Constants to refer to ALL columns of a schedule cursor *****/
	public static final int COL_ID = 0;
	public static final int COL_DESCRIPTION = 1;
	public static final int COL_TYPE = 2;
	public static final int COL_TYPESTRING = 3;
	public static final int COL_OCCURENCE = 4;
	public static final int COL_OCCURENCEMULTIPLIER = 5;
	public static final int COL_OCCURENCESTRING = 6;
	public static final int COL_PAYMENTTYPE = 7;
	public static final int COL_PAYMENTTYPESTRING = 8;
	public static final int COL_STARTDATE = 9;
	public static final int COL_ENDDATE = 10;
	public static final int COL_FIXED = 11;
	public static final int COL_AUTOENTER = 12;
	public static final int COL_LASTPAYMENT = 13;
	public static final int COL_NEXTPAYMENTDUE = 14;
	public static final int COL_WEEKENDOPTION = 15;
	public static final int COL_WEEKENDOPTIONSTRING = 16;
	
	/***** Elements of a specific Schedule *****/
	private String id;
	private String Description;
	private int Type;
	private String TypeString;
	private int occurence;
	private int occurenceMultiplier;
	private String occurenceString;
	private int paymentType;
	private String paymentTypeString;
	private Calendar StartDate;
	private Calendar EndDate;
	private String Fixed;
	private String AutoEnter;
	private Calendar LastPaymentDate;
	private Calendar DueDate;
	private int WeekendOption;
	private String WeekendOptionString;
	private long nAmount;		//Holds the amount of this transaction in pennies.
	private long nBalance;		//Hold the balance AFTER this transactions occurs. This is calculated and only for the Cash Required report.
								//Held in pennies.
	private String title;
	private String transDate = null;
	ArrayList<Split> Splits;	// All the actual details of a particular schedule
	Transaction Transaction;
	Context context;

	
	// Constructor for a Schedule
	Schedule(String Desc, Calendar dueDate, String strAmt, Context cont)
	{
		this.id = null;
		this.Description = Desc;
		this.Type = 0;
		this.TypeString = null;
		this.occurence = 0;
		this.occurenceMultiplier = 0;
		this.occurenceString = null;
		this.paymentType = 0;
		this.paymentTypeString = null;
		this.StartDate = null;
		this.EndDate = null;
		this.Fixed = null;
		this.AutoEnter = null;
		this.LastPaymentDate = null;
		this.DueDate = dueDate;
		this.WeekendOption = 0;
		this.WeekendOptionString = null;

		this.nAmount = convertToPennies(strAmt);
		this.nBalance = 0;
		this.Splits = null;
		this.Transaction = null;
		this.title = null;
		this.context = cont;
	}
	
	Schedule(Cursor c, Context cont)
	{
		String[] date = {null, null, null};
		c.moveToFirst();
		
		this.id = c.getString(C_ID);
		this.Description = c.getString(C_DESCRIPTION);
		this.Type = 0;
		this.TypeString = null;
		this.occurence = c.getInt(C_OCCURENCE);
		this.occurenceMultiplier = c.getInt(C_OCCURENCEMULTIPLIER);
		this.occurenceString = null;
		this.paymentType = 0;
		this.paymentTypeString = null;
		this.StartDate = null;
		if(c.getString(C_ENDDATE) != null)
		{
			this.EndDate = Calendar.getInstance();
			date = c.getString(C_ENDDATE).split("-");
			this.EndDate.set(Integer.valueOf(date[0]), Integer.valueOf(date[1]) - 1, Integer.valueOf(date[2]));
		}
		this.Fixed = null;
		if(c.getString(C_AUTOENTER) != null)
			this.AutoEnter = c.getString(C_AUTOENTER);
		else
			this.AutoEnter = null;
		this.LastPaymentDate = null;
		if(c.getString(C_NEXTPAYMENTDUE) != null)
		{
			this.DueDate = Calendar.getInstance();
			date = c.getString(C_NEXTPAYMENTDUE).split("-");			
			this.DueDate.set(Integer.valueOf(date[0]), Integer.valueOf(date[1]) - 1, Integer.valueOf(date[2]));
			date[0] = date[1] = date[2] = null;
		}
		this.WeekendOption = 0;
		this.WeekendOptionString = null;
		this.nAmount = convertToPennies(c.getString(C_VALUEFORMATTED));
		this.nBalance = 0;
		this.Splits = null;
		this.Transaction = null;
		this.title = null;
		this.context = cont;
	}
	
	Schedule(Cursor curSchedule, Cursor curSplits, Context c)
	{
		// First poplulate the actual schedule details.
		String[] date = {null, null, null};
		curSchedule.moveToFirst();
		
		this.id = curSchedule.getString(COL_ID);
		this.Description = curSchedule.getString(COL_DESCRIPTION);
		this.Type = curSchedule.getInt(COL_TYPE);
		this.TypeString = curSchedule.getString(COL_TYPESTRING);
		this.occurence = curSchedule.getInt(COL_OCCURENCE);
		this.occurenceMultiplier = curSchedule.getInt(COL_OCCURENCEMULTIPLIER);
		this.occurenceString = curSchedule.getString(COL_OCCURENCESTRING);
		this.paymentType = curSchedule.getInt(COL_PAYMENTTYPE);
		this.paymentTypeString = curSchedule.getString(COL_PAYMENTTYPESTRING);
		if(curSchedule.getString(COL_STARTDATE) != null)
		{
			this.StartDate = Calendar.getInstance();
			date = curSchedule.getString(COL_STARTDATE).split("-");
			this.StartDate.set(Integer.valueOf(date[0]),  Integer.valueOf(date[1]) - 1, Integer.valueOf(date[2]));
			date[0] = date[1] = date[2] = null;
		}
		else
			this.StartDate = null;
		if(curSchedule.getString(COL_ENDDATE) != null)
		{
			this.EndDate = Calendar.getInstance();
			date = curSchedule.getString(COL_ENDDATE).split("-");
			this.EndDate.set(Integer.valueOf(date[0]), Integer.valueOf(date[1]) - 1, Integer.valueOf(date[2]));
			date[0] = date[1] = date[2] = null;
		}
		else
			this.EndDate = null;
		this.Fixed = curSchedule.getString(COL_FIXED);
		this.AutoEnter = curSchedule.getString(COL_AUTOENTER);
		if(curSchedule.getString(COL_LASTPAYMENT) != null)
		{
			this.LastPaymentDate = Calendar.getInstance();
			date = curSchedule.getString(COL_LASTPAYMENT).split("-");
			this.LastPaymentDate.set(Integer.valueOf(date[0]), Integer.valueOf(date[1]) - 1, Integer.valueOf(date[2]));
			date[0] = date[1] = date[2] = null;		
		}
		else
			this.LastPaymentDate = null;
		if(curSchedule.getString(COL_NEXTPAYMENTDUE) != null)
		{
			this.DueDate = Calendar.getInstance();
			date = curSchedule.getString(COL_NEXTPAYMENTDUE).split("-");			
			this.DueDate.set(Integer.valueOf(date[0]), Integer.valueOf(date[1]) - 1, Integer.valueOf(date[2]));
			date[0] = date[1] = date[2] = null;
		}
		else
			this.DueDate = null;
		this.WeekendOption = curSchedule.getInt(COL_WEEKENDOPTION);
		this.WeekendOptionString = curSchedule.getString(COL_WEEKENDOPTIONSTRING);
		
		// We should only be using this particular Constructor for a "single" schedule instance, so nAmount and nBalance don't matter.
		this.nAmount = 0;
		this.nBalance = 0;
		
		// Now populate the Splits ArrayList from the supplied cursor.
		this.Splits = new ArrayList<Split>();
		//curSplits.moveToFirst();
		for(int i=0; i < curSplits.getCount(); i++)
			this.Splits.add(new Split(curSplits, i, "9999", c));
		
		this.Transaction = null;
		this.title = null;
		this.context = c;
	}
	
	Schedule(Cursor curSchedule, Cursor curSplits, Cursor curTransaction, Context c)
	{
		// First poplulate the actual schedule details.
		String[] date = {null, null, null};
		curSchedule.moveToFirst();
		
		Log.d(TAG, "ScheduleId: " + curSchedule.getString(COL_ID));
		this.id = curSchedule.getString(COL_ID);
		Log.d(TAG, "Description: " + curSchedule.getString(COL_DESCRIPTION));
		this.Description = curSchedule.getString(COL_DESCRIPTION);
		Log.d(TAG, "Type: " + curSchedule.getString(COL_TYPE));
		this.Type = curSchedule.getInt(COL_TYPE);
		Log.d(TAG, "Type String: " + curSchedule.getString(COL_TYPESTRING));
		this.TypeString = curSchedule.getString(COL_TYPESTRING);
		Log.d(TAG, "Occurence: " + curSchedule.getString(COL_OCCURENCE));
		this.occurence = curSchedule.getInt(COL_OCCURENCE);
		Log.d(TAG, "Occurence Multiplier: " + curSchedule.getInt(COL_OCCURENCEMULTIPLIER));
		this.occurenceMultiplier = curSchedule.getInt(COL_OCCURENCEMULTIPLIER);
		Log.d(TAG, "Occurence String: " + curSchedule.getString(COL_OCCURENCESTRING));
		this.occurenceString = curSchedule.getString(COL_OCCURENCESTRING);
		Log.d(TAG, "Payment Type: " + curSchedule.getInt(COL_PAYMENTTYPE));
		this.paymentType = curSchedule.getInt(COL_PAYMENTTYPE);
		Log.d(TAG, "Payment Type String: " + curSchedule.getString(COL_PAYMENTTYPESTRING));
		this.paymentTypeString = curSchedule.getString(COL_PAYMENTTYPESTRING);
		Log.d(TAG, "StartDate: " + curSchedule.getString(COL_STARTDATE));
		if(curSchedule.getString(COL_STARTDATE) != null)
		{
			this.StartDate = Calendar.getInstance();
			date = curSchedule.getString(COL_STARTDATE).split("-");
			this.StartDate.set(Integer.valueOf(date[0]),  Integer.valueOf(date[1]) - 1, Integer.valueOf(date[2]));
			date[0] = date[1] = date[2] = null;
		}
		else
			this.StartDate = null;
		if(curSchedule.getString(COL_ENDDATE) != null)
		{
			this.EndDate = Calendar.getInstance();
			date = curSchedule.getString(COL_ENDDATE).split("-");
			this.EndDate.set(Integer.valueOf(date[0]), Integer.valueOf(date[1]) - 1, Integer.valueOf(date[2]));
			date[0] = date[1] = date[2] = null;
		}
		else
			this.EndDate = null;
		this.Fixed = curSchedule.getString(COL_FIXED);
		this.AutoEnter = curSchedule.getString(COL_AUTOENTER);
		if(curSchedule.getString(COL_LASTPAYMENT) != null)
		{
			this.LastPaymentDate = Calendar.getInstance();
			date = curSchedule.getString(COL_LASTPAYMENT).split("-");
			this.LastPaymentDate.set(Integer.valueOf(date[0]), Integer.valueOf(date[1]) - 1, Integer.valueOf(date[2]));
			date[0] = date[1] = date[2] = null;		
		}
		else
			this.LastPaymentDate = null;
		if(curSchedule.getString(COL_NEXTPAYMENTDUE) != null)
		{
			this.DueDate = Calendar.getInstance();
			date = curSchedule.getString(COL_NEXTPAYMENTDUE).split("-");			
			this.DueDate.set(Integer.valueOf(date[0]), Integer.valueOf(date[1]) - 1, Integer.valueOf(date[2]));
			date[0] = date[1] = date[2] = null;
		}
		else
			this.DueDate = null;
		this.WeekendOption = curSchedule.getInt(COL_WEEKENDOPTION);
		this.WeekendOptionString = curSchedule.getString(COL_WEEKENDOPTIONSTRING);
		
		// We should only be using this particular Constructor for a "single" schedule instance, so nAmount and nBalance don't matter.
		this.nAmount = 0;
		this.nBalance = 0;
		
		// Now populate the Splits ArrayList from the supplied cursor.
		this.Splits = new ArrayList<Split>();
		//curSplits.moveToFirst();
		for(int i=0; i < curSplits.getCount(); i++)
			this.Splits.add(new Split(curSplits, i, "9999", c));
		
		// Now populate the transaction for this schedule
		this.Transaction = new Transaction(curTransaction, "9999", c);
		this.title = null;
		this.context = c;
	}
	
	public String getDescription()
	{
		return Description;
	}
	
	public Calendar getDueDate()
	{
		return DueDate;
	}
	
	public long getAmount()
	{
		return nAmount;
	}
	
	public long getBalance()
	{
		return nBalance;
	}
	
	public void setId(String id)
	{
		this.id = id;
	}
	
	public String getId()
	{
		return this.id;
	}
	
	public void setOccurence(int occurence)
	{
		this.occurence = occurence;
	}
	
	public int getOccurence()
	{
		return this.occurence;
	}
	
	public void setOccurenceMultiplier(int multiplier)
	{
		this.occurenceMultiplier = multiplier;
	}
	
	public int getOccurenceMultiplier()
	{
		return this.occurenceMultiplier;
	}
	
	public String getTitle()
	{
		return this.title;
	}
	
	
	public void setTitle(String t)
	{
		this.title = t;
	}
	
	public void setEndDate(String end)
	{	
		if(end != null)
		{
			this.EndDate = Calendar.getInstance();
			String[] date = end.split("-");
			this.EndDate.set(Integer.valueOf(date[0]), Integer.valueOf(date[1]) - 1, Integer.valueOf(date[2]));
		}
	}
	
	public Calendar getEndDate()
	{
		return this.EndDate;
	}
	
	public void setAutoEnter(String auto)
	{
		this.AutoEnter = auto;
	}
	
	public boolean getAutoEnter()
	{
		if(this.AutoEnter.equals("Y"))
			return true;
		else
			return false;
	}
	
	/********************************************************************************************
	* Adapted from code found at currency : Java Glossary
	* website: http://mindprod.com/jgloss/currency.html
	********************************************************************************************/
	public long convertToPennies(String numStr)
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
		    	case '(':
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
	public String convertToDollars(long pennies)
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
	
	public long calcBalance(long nPrevBal)
	{
		this.nBalance = nPrevBal + this.nAmount;
		
		return this.nBalance; 
	}
	
	public String formatDateString()
	{
		return String.valueOf(this.DueDate.get(Calendar.MONTH) + 1) + "/" + String.valueOf(this.DueDate.get(Calendar.DAY_OF_MONTH)) +
				"/" + String.valueOf(this.DueDate.get(Calendar.YEAR));
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
	
	static public ArrayList<Schedule> BuildCashRequired(Cursor c, String strStartDate, String strEndDate, long nBegBalance, Context cont)
	{
		Schedule schd = null;
		ArrayList<Calendar> dueDates = new ArrayList<Calendar>();
		ArrayList<Schedule> Schedules = new ArrayList<Schedule>();
		
		// Make sure we are at the beginning of the dataset
		c.moveToFirst();
		
		// Loop over the dataset and expand each of the cases out the date the user entered.
		for(int i=0; i < c.getCount(); i++)
		{
			dueDates = Schedule.paymentDates(strStartDate, strEndDate, Schedule.getOccurence(c.getInt(C_OCCURENCE), c.getInt(C_OCCURENCEMULTIPLIER)), c.getString(C_STARTDATE), c.getString(C_NEXTPAYMENTDUE));

			// We now have all our payment dates for this schedule between the dates the user supplied.
			// Let's create the Schedule class and add it to the ArrayList.
			for(int d=0; d < dueDates.size(); d++)
			{
				schd = new Schedule(c.getString(C_DESCRIPTION), dueDates.get(d), c.getString(C_VALUEFORMATTED), cont);
				// Add the id, occurence, occurenceMultiplier, autoEnter and enddate for this schedule
				schd.setId(c.getString(C_ID));
				schd.setOccurence(c.getInt(C_OCCURENCE));
				schd.setOccurenceMultiplier(c.getInt(C_OCCURENCEMULTIPLIER));
				schd.setEndDate(c.getString(C_ENDDATE));
				schd.setAutoEnter(c.getString(C_AUTOENTER));
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
		
		// Finally return the Schedules ArrayList
		return Schedules;
	}
	
	static public ArrayList<Calendar> paymentDates(String strStart, String strEnd, int occurence, String scheduleStartDate, String nextPaymentDate)
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
				if(isDueToday(calNextPaymentDate, calEnd))
					Dates.add(date);
				break;
			case OCCUR_DAILY:
				while(calNextPaymentDate.before(calEnd))
				{
					Dates.add(date);
					calNextPaymentDate.add(Calendar.DAY_OF_MONTH, 1);
					date = (GregorianCalendar) calNextPaymentDate.clone();
				}
				if(isDueToday(calNextPaymentDate, calEnd))
					Dates.add(date);
				break;
			case OCCUR_WEEKLY:
				while(calNextPaymentDate.before(calEnd))
				{
					Dates.add(date);
					calNextPaymentDate.add(Calendar.DAY_OF_MONTH, 7);
					date = (GregorianCalendar) calNextPaymentDate.clone();
				}	
				if(isDueToday(calNextPaymentDate, calEnd))
					Dates.add(date);
				break;
			case OCCUR_FORTNIGHTLY:
			case OCCUR_EVERYOTHERWEEK:
				while(calNextPaymentDate.before(calEnd))
				{
					Dates.add(date);
					calNextPaymentDate.add(Calendar.DAY_OF_MONTH, 14);
					date = (GregorianCalendar) calNextPaymentDate.clone();
				}
				if(isDueToday(calNextPaymentDate, calEnd))
					Dates.add(date);
				break;
			case OCCUR_EVERYHALFMONTH:
				while(calNextPaymentDate.before(calEnd))
				{
					Dates.add(date);
					calNextPaymentDate.add(Calendar.DAY_OF_MONTH, 15);
					date = (GregorianCalendar) calNextPaymentDate.clone();
				}
				if(isDueToday(calNextPaymentDate, calEnd))
					Dates.add(date);
				break;
			case OCCUR_EVERYTHREEWEEKS:
				while(calNextPaymentDate.before(calEnd))
				{
					Dates.add(date);
					calNextPaymentDate.add(Calendar.DAY_OF_MONTH, 21);
					date = (GregorianCalendar) calNextPaymentDate.clone();
				}
				if(isDueToday(calNextPaymentDate, calEnd))
					Dates.add(date);
				break;
			case OCCUR_EVERYTHIRTYDAYS:
				while(calNextPaymentDate.before(calEnd))
				{
					Dates.add(date);
					calNextPaymentDate.add(Calendar.DAY_OF_MONTH, 30);
					date = (GregorianCalendar) calNextPaymentDate.clone();
				}
				if(isDueToday(calNextPaymentDate, calEnd))
					Dates.add(date);
				break;
			case OCCUR_MONTHLY:
				while(calNextPaymentDate.before(calEnd))
				{
					Dates.add(date);
					calNextPaymentDate.add(Calendar.MONTH, 1);
					date = (GregorianCalendar) calNextPaymentDate.clone();
				}
				if(isDueToday(calNextPaymentDate, calEnd))
					Dates.add(date);
				break;
			case OCCUR_EVERYFOURWEEKS:
				while(calNextPaymentDate.before(calEnd))
				{
					Dates.add(date);
					calNextPaymentDate.add(Calendar.DAY_OF_MONTH, 28);
					date = (GregorianCalendar) calNextPaymentDate.clone();
				}
				if(isDueToday(calNextPaymentDate, calEnd))
					Dates.add(date);
				break;
			case OCCUR_EVERYEIGHTWEEKS:
				while(calNextPaymentDate.before(calEnd))
				{
					Dates.add(date);
					calNextPaymentDate.add(Calendar.DAY_OF_MONTH, 56);
					date = (GregorianCalendar) calNextPaymentDate.clone();
				}				
				if(isDueToday(calNextPaymentDate, calEnd))
					Dates.add(date);
				break;
			case OCCUR_EVERYOTHERMONTH:
				while(calNextPaymentDate.before(calEnd))
				{
					Dates.add(date);
					calNextPaymentDate.add(Calendar.MONTH, 2);
					date = (GregorianCalendar) calNextPaymentDate.clone();
				}
				if(isDueToday(calNextPaymentDate, calEnd))
					Dates.add(date);
				break;
			case OCCUR_EVERYTHREEMONTHS:
			case OCCUR_QUARTERLY:
				while(calNextPaymentDate.before(calEnd))
				{
					Dates.add(date);
					calNextPaymentDate.add(Calendar.MONTH, 3);
					date = (GregorianCalendar) calNextPaymentDate.clone();
				}
				if(isDueToday(calNextPaymentDate, calEnd))
					Dates.add(date);
				break;
			case OCCUR_TWICEYEARLY:
				while(calNextPaymentDate.before(calEnd))
				{
					Dates.add(date);
					calNextPaymentDate.add(Calendar.MONTH, 6);
					date = (GregorianCalendar) calNextPaymentDate.clone();
				}
				if(isDueToday(calNextPaymentDate, calEnd))
					Dates.add(date);
				break;
			case OCCUR_EVERYOTHERYEAR:
				while(calNextPaymentDate.before(calEnd))
				{
					Dates.add(date);
					calNextPaymentDate.add(Calendar.YEAR, 2);
					date = (GregorianCalendar) calNextPaymentDate.clone();
				}				
				if(isDueToday(calNextPaymentDate, calEnd))
					Dates.add(date);
				break;
			case OCCUR_EVERYFOURMONTHS:
				while(calNextPaymentDate.before(calEnd))
				{
					Dates.add(date);
					calNextPaymentDate.add(Calendar.MONTH, 4);
					date = (GregorianCalendar) calNextPaymentDate.clone();
				}
				if(isDueToday(calNextPaymentDate, calEnd))
					Dates.add(date);
				break;
			case OCCUR_YEARLY:
				while(calNextPaymentDate.before(calEnd))
				{
					Dates.add(date);
					calNextPaymentDate.add(Calendar.YEAR, 1);
					date = (GregorianCalendar) calNextPaymentDate.clone();
				}
				if(isDueToday(calNextPaymentDate, calEnd))
					Dates.add(date);
				break;
			default:
				break;
		}
		return Dates;
	}
	
	static public int getOccurence(int nOccurence, int nOccurenceMultiple)
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
			case OCCUR_EVERYHALFMONTH:
				return OCCUR_EVERYHALFMONTH;
			case OCCUR_MONTHLY:
				switch(nOccurenceMultiple)
				{
					case 1:
						return OCCUR_MONTHLY;
					case 2:
						return OCCUR_EVERYOTHERMONTH;
					case 3:
						return OCCUR_EVERYTHREEMONTHS;
					case 4:
						return OCCUR_EVERYFOURMONTHS;
					case 6:
						return OCCUR_TWICEYEARLY;
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
	
	static public int getOccurrenceFromMultiplier(/*int multiplier,*/ String strDesc)
	{
		if( strDesc.equals("Once") )
			return OCCUR_ONCE;
		else if ( strDesc.equals("Day") )
		{
			/*switch( multiplier) 
			{
			case 1:
				return OCCUR_DAILY;
			case 30:
				return OCCUR_EVERYTHIRTYDAYS;
			default:
				return OCCUR_ANY;
			}*/
			return OCCUR_DAILY;
		}
		else if ( strDesc.equals("Week") )
		{
			/*switch ( multiplier )
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
			default:
				return OCCUR_ANY;
			}*/
			return OCCUR_WEEKLY;
		}
		else if ( strDesc.equals("Month") )
		{
			/*switch ( multiplier) 
			{
			case 1:
				return OCCUR_MONTHLY;
			case 2:
				return OCCUR_EVERYOTHERMONTH;
			case 3:
				return OCCUR_EVERYTHREEMONTHS;
			case 4:
				return OCCUR_EVERYFOURMONTHS;
			case 6:
				return OCCUR_TWICEYEARLY;
			default:
				return OCCUR_ANY;	
			}*/
			return OCCUR_MONTHLY;
		}
		else if ( strDesc.equals("Half-month") )
		{
			return OCCUR_EVERYHALFMONTH;
		}
		else if ( strDesc.equals("Year") )
		{
			/*switch( multiplier )
			{
			case 1:
				return OCCUR_YEARLY;
			case 2:
				return OCCUR_EVERYOTHERYEAR;
			default:
				return OCCUR_ANY;
			}*/
			return OCCUR_YEARLY;
		}
		else
			return OCCUR_ANY;
	}
	
	static public String getOccurrenceToString(int multiplier, String strDesc, Context context)
	{
		if( strDesc.equals(context.getString(R.string.freqOnce)) )
		{
			switch( multiplier )
			{
			case 1:
				return context.getString(R.string.freqOnce);
			default:
				return String.format("%1d " + context.getString(R.string.freqTimes), multiplier);
			}
		}
		else if( strDesc.equals(context.getString(R.string.freqDay)) )
		{
			switch( multiplier )
			{
			case 1:
				return context.getString(R.string.freqDaily);
			case 30:
				return context.getString(R.string.freq30Days);
			default:
				return String.format(context.getString(R.string.freqEvery) + " %1d " + context.getString(R.string.freqDays), multiplier);
			}
		}
		else if( strDesc.equals(context.getString(R.string.freqWeek)) )
		{
			switch( multiplier )
			{
			case 1:
				return context.getString(R.string.freqWeekly);
			case 2:
				return context.getString(R.string.freq2Weeks);
			case 3:
				return context.getString(R.string.freq3Weeks);
			case 4:
				return context.getString(R.string.freq4Weeks);
			case 8:
				return context.getString(R.string.freq8Weeks);
			default:
				return String.format(context.getString(R.string.freqEvery) + " %1d " + context.getString(R.string.freqWeeks), multiplier);
			}	
		}
		else if( strDesc.equals(context.getString(R.string.freqHalfMonth)) )
		{
			switch( multiplier )
			{
			case 1:
				return context.getString(R.string.freqHalfMonths);
			default:
				return String.format(context.getString(R.string.freqEvery) + " %1d " + context.getString(R.string.freqHalfMonths2), multiplier);
			}
		}
		else if( strDesc.equals(context.getString(R.string.freqMonth)) )
		{
			switch( multiplier )
			{
			case 1:
				return context.getString(R.string.freqMonthly);
			case 2:
				return context.getString(R.string.freq2Months);
			case 3:
				return context.getString(R.string.freq3Months);
			case 4:
				return context.getString(R.string.freq4Months);
			case 6:
				return context.getString(R.string.freq2Yearly);
			default:
				return String.format(context.getString(R.string.freqEvery) + " %1d " + context.getString(R.string.freqMonths), multiplier);
			}
		}
		else if( strDesc.equals(context.getString(R.string.freqYear)) )
		{
			switch( multiplier )
			{
			case 1:
				return context.getString(R.string.freqYearly);
			default:
				return String.format(context.getString(R.string.freqEvery) + " %1d " + context.getString(R.string.freqYears), multiplier);
			}
		}
		else
			return context.getString(R.string.freqAny);
	}
	
	public boolean isPastDue()
	{
		GregorianCalendar calToday = new GregorianCalendar();
		
		if(this.DueDate.get(Calendar.DAY_OF_YEAR) < calToday.get(Calendar.DAY_OF_YEAR))
			return true;
		else
			return false;
	}
	
	public boolean isDueToday()
	{
		GregorianCalendar calToday = new GregorianCalendar();
		
		if(this.DueDate.get(Calendar.DAY_OF_YEAR) == calToday.get(Calendar.DAY_OF_YEAR))
			return true;
		else
			return false;
	}
	
	static public boolean isDueToday(Calendar date1, Calendar date2)
	{
		if(date1.get(Calendar.DAY_OF_YEAR) == date2.get(Calendar.DAY_OF_YEAR))
			return true;
		else
			return false;
	}
	
	public void skipSchedule()
	{
		this.advanceDueDate();
		//this.advanceDueDate(Schedule.getOccurence(this.occurence, this.occurenceMultiplier));
	}
	
	public void advanceDueDate(/*int occurenceRate*/)
	{
		
		switch (this.getOccurence(this.occurence, this.occurenceMultiplier))
		{
			case OCCUR_ONCE:
				//if(this.DueDate.before(this.EndDate) || this.EndDate == null)
					//this.DueDate = this.EndDate;
				break;
			case OCCUR_DAILY:
				if(this.DueDate.before(this.EndDate) || this.EndDate == null)
					this.DueDate.add(Calendar.DAY_OF_MONTH, 1);
				break;
			case OCCUR_WEEKLY:
				if(this.DueDate.before(this.EndDate) || this.EndDate == null)
					this.DueDate.add(Calendar.DAY_OF_MONTH, 7);			
				break;
			case OCCUR_FORTNIGHTLY:
			case OCCUR_EVERYOTHERWEEK:
				if(this.DueDate.before(this.EndDate) || this.EndDate == null)
					this.DueDate.add(Calendar.DAY_OF_MONTH, 14);
				break;
			case OCCUR_EVERYHALFMONTH:
				if(this.DueDate.before(this.EndDate) || this.EndDate == null)
					this.DueDate.add(Calendar.DAY_OF_MONTH, 15);
				break;
			case OCCUR_EVERYTHREEWEEKS:
				if(this.DueDate.before(this.EndDate) || this.EndDate == null)
					this.DueDate.add(Calendar.DAY_OF_MONTH, 21);
				break;
			case OCCUR_EVERYTHIRTYDAYS:
				if(this.DueDate.before(this.EndDate) || this.EndDate == null)
					this.DueDate.add(Calendar.DAY_OF_MONTH, 30);
				break;
			case OCCUR_MONTHLY:
				if(this.DueDate.before(this.EndDate) || this.EndDate == null)
					this.DueDate.add(Calendar.MONTH, 1);
				break;
			case OCCUR_EVERYFOURWEEKS:
				if(this.DueDate.before(this.EndDate) || this.EndDate == null)
					this.DueDate.add(Calendar.DAY_OF_MONTH, 28);
				break;
			case OCCUR_EVERYEIGHTWEEKS:
				if(this.DueDate.before(this.EndDate) || this.EndDate == null)
					this.DueDate.add(Calendar.DAY_OF_MONTH, 56);				
				break;
			case OCCUR_EVERYOTHERMONTH:
				if(this.DueDate.before(this.EndDate) || this.EndDate == null)
					this.DueDate.add(Calendar.MONTH, 2);
				break;
			case OCCUR_EVERYTHREEMONTHS:
			case OCCUR_QUARTERLY:
				if(this.DueDate.before(this.EndDate) || this.EndDate == null)
					this.DueDate.add(Calendar.MONTH, 3);
				break;
			case OCCUR_TWICEYEARLY:
				if(this.DueDate.before(this.EndDate) || this.EndDate == null)
					this.DueDate.add(Calendar.MONTH, 6);
				break;
			case OCCUR_EVERYOTHERYEAR:
				if(this.DueDate.before(this.EndDate) || this.EndDate == null)
					this.DueDate.add(Calendar.YEAR, 2);				
				break;
			case OCCUR_EVERYFOURMONTHS:
				if(this.DueDate.before(this.EndDate) || this.EndDate == null)
					this.DueDate.add(Calendar.MONTH, 4);
				break;
			case OCCUR_YEARLY:
				if(this.DueDate.before(this.EndDate) || this.EndDate == null)
					this.DueDate.add(Calendar.YEAR, 1);
				break;
			default:
				break;
		}		
		
		// Now that the schedule is advanced we need to update the various tables with the correct information.
		// kmmSchedules for the schedule information
		// kmmTransactions for the "transaction" part of the schedule
		// kmmSplits for the dates of the next upcoming split that is part of the schedule.
		ContentValues values = new ContentValues();
		values.put("nextPaymentDue", this.getDatabaseFormattedString());
		values.put("startDate", this.getDatabaseFormattedString());
		values.put("lastPayment", formatDate( this.transDate ));
		Uri u = Uri.withAppendedPath(KMMDProvider.CONTENT_SCHEDULE_URI,this.getId() );
		u = Uri.parse(u.toString());
		this.context.getContentResolver().update(u, values, null, null);
		//KMMDapp.db.update("kmmSchedules", values, "id=?", new String[] { this.getId() });
		//Need to update the schedules splits in the kmmsplits table as this is where the upcoming bills in desktop comes from.
		for(int i=0; i < this.Splits.size(); i++)
		{
			Split s = this.Splits.get(i);
			s.setPostDate(this.getDatabaseFormattedString());
			s.commitSplit(true);
		}	
		//Need to update the schedule in kmmTransactions postDate to match the splits and the actual schedule for the next payment due date.
		values.clear();
		values.put("postDate", this.getDatabaseFormattedString());
		u = Uri.withAppendedPath(KMMDProvider.CONTENT_TRANSACTION_URI, this.getId());
		this.context.getContentResolver().update(u, values, null, null);
		//KMMDapp.db.update("kmmTransactions", values, "id=?", new String[] { this.getId() });
	}
	
	public String getDatabaseFormattedString()
	{
		String strDay = null;
		int intDay = this.DueDate.get(Calendar.DAY_OF_MONTH);
		String strMonth = null;
		int intMonth = this.DueDate.get(Calendar.MONTH);

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
		
		return String.valueOf(this.DueDate.get(Calendar.YEAR) + "-" + strMonth + "-" + strDay);
	}
	
	static public String padFormattedDate(String date)
	{
		String str[] = date.split("-");
		String strDay = null;
		int intDay = Integer.valueOf(str[2]);
		String strMonth = null;
		int intMonth = Integer.valueOf(str[1]);

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
				strMonth = "0" + String.valueOf(intMonth);
				break;
			default:
				strMonth = String.valueOf(intMonth);
				break;
		}
		
		return String.valueOf(str[0] + "-" + strMonth + "-" + strDay);	
	}
	
	public Transaction convertToTransaction(String transId)
	{
		// All we need to do to convert a Schedule to a Transaction for entry is change the txType from 'S' to 'N' and
		// change the transactionId from SCHXXXXXX to TXXXXXXX
		// Need to do this on the transaction and on each split.
		Log.d(TAG, "trasId: " + transId);
		Transaction trans = this.Transaction;
		trans.setTransId(transId);
		trans.setTxType("N");
		trans.splits = this.Splits;
		for(int i=0; i<trans.splits.size(); i++)
		{
			trans.splits.get(i).setTransactionId(transId);
			trans.splits.get(i).setTxType("N");
			Log.d(TAG, "Split #" + i + " postDate: " + trans.splits.get(i).getPostDate());
		}
		
		return trans;
	}
	
	public void setTransDate(String date)
	{
		this.transDate = date;
	}
}