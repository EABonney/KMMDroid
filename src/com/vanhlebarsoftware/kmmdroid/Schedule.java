package com.vanhlebarsoftware.kmmdroid;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.util.Log;

public class Schedule implements Parcelable
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
	private String widgetId;
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
	Schedule(Context cont, String schId, String widget)
	{
		this.title = null;
		this.context = cont;
		this.widgetId = widget;
		
		if( schId != null )
		{
			// Get the schedule elements
			Uri u = Uri.withAppendedPath(KMMDProvider.CONTENT_SCHEDULE_URI, schId + "#" + widget);
			u = Uri.parse(u.toString());
			Cursor cur = cont.getContentResolver().query(u, null, null, null, null);
			
			// Get the transaction elements of this schedule
			Transaction = new Transaction(cont, schId, widget);
			
			if( cur != null )
			{
				cur.moveToFirst();
				this.id = schId;
				this.Description = cur.getString(cur.getColumnIndex("name"));
				this.Type = cur.getInt(cur.getColumnIndex("type"));
				this.TypeString = cur.getString(cur.getColumnIndex("typeString"));
				this.occurence = cur.getInt(cur.getColumnIndex("occurence"));
				this.occurenceMultiplier = cur.getInt(cur.getColumnIndex("occurenceMultiplier"));
				this.occurenceString = cur.getString(cur.getColumnIndex("occurenceString"));
				this.paymentType = cur.getInt(cur.getColumnIndex("paymentType"));
				this.paymentTypeString = cur.getString(cur.getColumnIndex("paymentTypeString"));
				this.StartDate = convertDate(cur.getString(cur.getColumnIndex("startDate")));
				this.EndDate = convertDate(cur.getString(cur.getColumnIndex("endDate")));
				this.Fixed = cur.getString(cur.getColumnIndex("fixed"));
				this.AutoEnter = cur.getString(cur.getColumnIndex("autoEnter"));
				this.LastPaymentDate = convertDate(cur.getString(cur.getColumnIndex("lastPayment")));
				this.DueDate = convertDate(cur.getString(cur.getColumnIndex("nextPaymentDue")));
				this.WeekendOption = cur.getInt(cur.getColumnIndex("weekendOption"));
				this.WeekendOptionString = cur.getString(cur.getColumnIndex("weeekendOptionString"));
				this.nAmount = Transaction.getAmount();
				this.nBalance = 0;
			}
			else
			{
				// We need to throw some kind of error here!
			}			
		}
		else
		{
			this.id = null;
			this.Description = null;
			this.Type = 0;
			this.TypeString = null;
			this.occurence = 0;
			this.occurenceMultiplier = 0;
			this.occurenceString = null;
			this.paymentType = 0;
			this.paymentTypeString = null;
			this.StartDate = Calendar.getInstance();
			this.EndDate = Calendar.getInstance();
			this.Fixed = null;
			this.AutoEnter = null;
			this.LastPaymentDate = Calendar.getInstance();
			this.DueDate = Calendar.getInstance();
			this.WeekendOption = 0;
			this.WeekendOptionString = null;
			this.nAmount = 0;
			this.nBalance = 0;			
		}

	}
	
	Schedule(String Desc, Calendar dueDate, String strAmt, Context cont, String widget)
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
		this.widgetId = widget;
	}
	
	Schedule(Cursor c, Context cont, String widget)
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
		this.widgetId = widget;
	}
	
	Schedule(Cursor curSchedule, Cursor curSplits, Context c, String widget)
	{
		// First poplulate the actual schedule details.
		String[] date = {null, null, null};
		curSchedule.moveToFirst();
		
		this.id = curSchedule.getString(curSchedule.getColumnIndexOrThrow("_id"));
		this.Description = curSchedule.getString(curSchedule.getColumnIndexOrThrow("Description"));
		this.Type = curSchedule.getInt(curSchedule.getColumnIndexOrThrow("type"));
		this.TypeString = curSchedule.getString(curSchedule.getColumnIndexOrThrow("typeString"));
		this.occurence = curSchedule.getInt(curSchedule.getColumnIndexOrThrow("occurence"));
		this.occurenceMultiplier = curSchedule.getInt(curSchedule.getColumnIndexOrThrow("occurenceMultiplier"));
		this.occurenceString = curSchedule.getString(curSchedule.getColumnIndexOrThrow("occurenceString"));
		this.paymentType = curSchedule.getInt(curSchedule.getColumnIndexOrThrow("paymentType"));
		this.paymentTypeString = curSchedule.getString(curSchedule.getColumnIndexOrThrow("paymentTypeString"));
		if(curSchedule.getString(curSchedule.getColumnIndexOrThrow("startDate")) != null)
		{
			this.StartDate = Calendar.getInstance();
			date = curSchedule.getString(curSchedule.getColumnIndexOrThrow("startDate")).split("-");
			this.StartDate.set(Integer.valueOf(date[0]),  Integer.valueOf(date[1]) - 1, Integer.valueOf(date[2]));
			date[0] = date[1] = date[2] = null;
		}
		else
			this.StartDate = null;
		if(curSchedule.getString(curSchedule.getColumnIndexOrThrow("endDate")) != null)
		{
			this.EndDate = Calendar.getInstance();
			date = curSchedule.getString(curSchedule.getColumnIndexOrThrow("endDate")).split("-");
			this.EndDate.set(Integer.valueOf(date[0]), Integer.valueOf(date[1]) - 1, Integer.valueOf(date[2]));
			date[0] = date[1] = date[2] = null;
		}
		else
			this.EndDate = null;
		this.Fixed = curSchedule.getString(curSchedule.getColumnIndexOrThrow("fixed"));
		this.AutoEnter = curSchedule.getString(curSchedule.getColumnIndexOrThrow("autoEnter"));
		if(curSchedule.getString(curSchedule.getColumnIndexOrThrow("lastPayment")) != null)
		{
			this.LastPaymentDate = Calendar.getInstance();
			date = curSchedule.getString(curSchedule.getColumnIndexOrThrow("lastPayment")).split("-");
			this.LastPaymentDate.set(Integer.valueOf(date[0]), Integer.valueOf(date[1]) - 1, Integer.valueOf(date[2]));
			date[0] = date[1] = date[2] = null;		
		}
		else
			this.LastPaymentDate = null;
		if(curSchedule.getString(curSchedule.getColumnIndexOrThrow("nextPaymentDue")) != null)
		{
			this.DueDate = Calendar.getInstance();
			date = curSchedule.getString(curSchedule.getColumnIndexOrThrow("nextPaymentDue")).split("-");			
			this.DueDate.set(Integer.valueOf(date[0]), Integer.valueOf(date[1]) - 1, Integer.valueOf(date[2]));
			date[0] = date[1] = date[2] = null;
		}
		else
			this.DueDate = null;
		this.WeekendOption = curSchedule.getInt(curSchedule.getColumnIndexOrThrow("weekendOption"));
		this.WeekendOptionString = curSchedule.getString(curSchedule.getColumnIndexOrThrow("weekendOptionString"));
		
		// We should only be using this particular Constructor for a "single" schedule instance, so nAmount and nBalance don't matter.
		this.nAmount = 0;
		this.nBalance = 0;
		
		// Now populate the Splits ArrayList from the supplied cursor.
		this.Splits = new ArrayList<Split>();
		//curSplits.moveToFirst();
		for(int i=0; i < curSplits.getCount(); i++)
			this.Splits.add(new Split(curSplits, i, "9999", c));
		
		this.Transaction = new Transaction(c, null, widget);
		this.title = null;
		this.context = c;
		this.widgetId = widget;
	}
	
	Schedule(Cursor curSchedule, Cursor curSplits, Cursor curTransaction, Context c, String widget)
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
		this.widgetId = widget;
	}
	
	public void createScheduleId()
	{
		Uri u = Uri.withAppendedPath(KMMDProvider.CONTENT_FILEINFO_URI, "#" + this.widgetId);
		u = Uri.parse(u.toString());
		final String[] dbColumns = { "hiScheduleId"};

		// Run a query to get the hi schedule id so we can create a new one.
		Cursor cursor = context.getContentResolver().query(u, dbColumns, null, null, null);
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
		
		this.id = newId;
		this.Transaction.setTransId(newId);
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
	
	public void setStartDate(Calendar date)
	{
		this.StartDate = date;
	}
	
	public Calendar getStartDate()
	{
		return this.StartDate;
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
	
	public int getPaymentType()
	{
		return this.paymentType;
	}
	
	public int getType()
	{
		return this.Type;
	}
	
	public int getWeekendOption()
	{
		return this.WeekendOption;
	}
	
	public String getIsEstimate()
	{
		return this.Fixed;
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
	
	static public ArrayList<Schedule> BuildCashRequired(Cursor c, String strStartDate, String strEndDate, long nBegBalance, Context cont, String widget)
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
				schd = new Schedule(c.getString(C_DESCRIPTION), dueDates.get(d), c.getString(C_VALUEFORMATTED), cont, widget);
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
	
	public Calendar convertDate(String date)
	{
		// Date assumed to be in YYYY-MM-DD format
		String dates[] = date.split("-");
		Calendar cal = Calendar.getInstance();
		cal.set(Integer.valueOf(dates[0]), Integer.valueOf(dates[1]), Integer.valueOf(dates[2]));
		
		return cal;
	}
	
	public String convertDate(Calendar date)
	{
		if( date == null )
			return null;
		else
			return padDate( String.valueOf(date.get(Calendar.YEAR)) + "-" + String.valueOf(date.get(Calendar.MONTH) + 1) + 
						"-" + String.valueOf(date.get(Calendar.DAY_OF_MONTH)) );
	}
	
	private String padDate(String str)
	{
		// Date passed in is in the form of YYY-MM-DD
		String[] dates = str.split("-");
		
		String strDay = null;
		String strMonth = null;
		switch(Integer.valueOf(dates[2]))
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
				strDay = "0" + dates[2];
				break;
			default:
				strDay = dates[2];
			break;
		}
		
		switch(Integer.valueOf(dates[1]))
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
				strMonth = "0" + String.valueOf(Integer.valueOf(dates[1]));
				break;
			default:
				strMonth = String.valueOf(Integer.valueOf(dates[1]));
				break;
		}
		
		return new StringBuilder()
					// Month is 0 based so add 1
					.append(dates[0]).append("-")
					.append(strMonth).append("-")
					.append(strDay).toString();
	}
	
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
	
	private Calendar getEndDate(int numOfTrans, Calendar calDate, int Frequency)
	{	
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
		
		return calDate;
	}
	
	public void getDataChanges(CreateModifyScheduleActivity cont)
	{
		// Get our tab fragments
		SchedulePaymentInfoActivity paymentInfo = (SchedulePaymentInfoActivity) cont.getSupportFragmentManager().findFragmentByTag("paymentinfo");
		ScheduleOptionsActivity options = (ScheduleOptionsActivity) cont.getSupportFragmentManager().findFragmentByTag("options");
		
		// Get the paymentInfo elements for the schedule.
		this.Description = paymentInfo.getScheduleName();
		this.Type = paymentInfo.getScheduleType();
		this.TypeString = this.getTypeDescription(this.Type);
		this.occurenceMultiplier = paymentInfo.getScheduleFrequency();
		this.occurenceString = paymentInfo.getScheduleFrequencyDescription();
		this.occurence = Schedule.getOccurrenceFromMultiplier(this.occurenceString);
		this.paymentType = paymentInfo.getSchedulePaymentMethod();
		this.paymentTypeString = this.getPaymentTypeToString(this.paymentType);
		this.StartDate = convertDate(paymentInfo.getStartDate());
		
		// Get the options elements for the schedule.
		this.Fixed = options.getScheduleEstimate();
		this.AutoEnter = options.getScheduleAutoEnter();
		this.WeekendOption = options.getScheduleWeekendOption();
		this.WeekendOptionString = this.getWeekendOptionString(this.WeekendOption);
	
		if( options.getWillScheduleEnd() && options.getRemainingTransactions() > 0 )
			this.EndDate = getEndDate(options.getRemainingTransactions(), this.StartDate, this.occurenceMultiplier);
		else if( options.getWillScheduleEnd() )
			this.EndDate = convertDate(options.getEndDate());
		else
			this.EndDate = null;

		// Get the transaction details for the schedule.
		this.Transaction.setDate(this.StartDate);
		this.Transaction.setMemo(paymentInfo.getScheduleMemo());
		this.Transaction.getDataChanges(cont);		
	}
	
	public void Save()
	{
		// Build the ContentValues for the schedule.
		ContentValues scheduleValues = new ContentValues();
		scheduleValues.put("id", this.id);
		scheduleValues.put("name", this.Description);
		scheduleValues.put("type", this.Type);
		scheduleValues.put("typeString", this.TypeString);
		scheduleValues.put("occurence", this.occurence);
		scheduleValues.put("occurenceMultiplier", this.occurenceMultiplier);
		scheduleValues.put("occurenceString", this.occurenceString);
		scheduleValues.put("paymentType", this.paymentType);
		scheduleValues.put("paymentTypeString", this.paymentTypeString);
		scheduleValues.put("startDate", convertDate(this.StartDate));
		scheduleValues.put("endDate", convertDate(this.EndDate));
		scheduleValues.put("fixed", this.Fixed);
		scheduleValues.put("autoEnter", this.AutoEnter);
		scheduleValues.put("lastPayment", convertDate(this.LastPaymentDate));
		scheduleValues.put("nextPaymentDue", convertDate(this.StartDate));
		scheduleValues.put("weekendOption", this.WeekendOption);
		scheduleValues.put("weekendOptionString", this.WeekendOptionString);
		
		// Save the actual schedule
		Uri u = Uri.withAppendedPath(KMMDProvider.CONTENT_SCHEDULE_URI, "#" + this.widgetId);
		u = Uri.parse(u.toString());
		this.context.getContentResolver().insert(u, scheduleValues);
		
		// Save the transaction and splits for this schedule
		this.Transaction.Save();
		
		// Build the ContentValues for the transaction
/*		ContentValues transactionValues = new ContentValues();
		transactionValues.put("id", id);
		transactionValues.put("txType", "S");
		transactionValues.put("postDate", scheduleStartDate);
		transactionValues.put("memo", memo);
		transactionValues.put("entryDate", entryDate);
		Cursor C = KMMDapp.db.query("kmmFileInfo", new String[] { "baseCurrency" }, null, null, null, null, null);
		C.moveToFirst();
		transactionValues.put("currencyId", C.getString(0));
		transactionValues.put("bankId", bankId);
		
		int numOfSplits = 2;
		boolean anySplits = false;
		if( !KMMDapp.Splits.isEmpty() )
		{
			anySplits = true;
			numOfSplits = KMMDapp.Splits.size();
		}
		
		// Build the the splits
		for( int i=0; i < numOfSplits; i++)
		{
			String value = null, formatted = null;
			if(i == 0)
			{
				if( nSchType == Schedule.TYPE_BILL )
				{
					value = "-" + Account.createBalance(Transaction.convertToPennies(amount));
					formatted = "-" + amount;
				}
				else
				{
					value = Account.createBalance(Transaction.convertToPennies(amount));
					formatted = amount;							
				}
			}
			else
			{
				// If we have splits grab the relevant information from the KMMDapp.Splits object.
				if( anySplits )
				{
					value = KMMDapp.Splits.get(i-1).getValue();
					formatted = KMMDapp.Splits.get(i-1).getValueFormatted();
					memo = KMMDapp.Splits.get(i-1).getMemo();
					accountId = KMMDapp.Splits.get(i-1).getAccountId();
				}
				else
				{
					if( nSchType == Schedule.TYPE_BILL )
					{
						value = Account.createBalance(Transaction.convertToPennies(amount));
						formatted = amount;								
					}
					else
					{
						value = "-" + Account.createBalance(Transaction.convertToPennies(amount));
						formatted = "-" + amount;								
					}
					accountId = categoryId;
				}
			}
			// Create the actual split for the transaction to be saved.
			if(i > 0)
				payeeId = null;
			Splits.add(new Split(id, "S", i, payeeId, null, null, String.valueOf(nStatus), value, formatted, value, formatted,
					null, null, memo, accountId, ckNumber, scheduleStartDate, null, this.widgetId, getBaseContext()));
*/
	}
	
	public void Update()
	{
		// Build the ContentValues for the schedule.
		ContentValues scheduleValues = new ContentValues();
		scheduleValues.put("id", this.id);
		scheduleValues.put("name", this.Description);
		scheduleValues.put("type", this.Type);
		scheduleValues.put("typeString", this.TypeString);
		scheduleValues.put("occurence", this.occurence);
		scheduleValues.put("occurenceMultiplier", this.occurenceMultiplier);
		scheduleValues.put("occurenceString", this.occurenceString);
		scheduleValues.put("paymentType", this.paymentType);
		scheduleValues.put("paymentTypeString", this.paymentTypeString);
		scheduleValues.put("startDate", convertDate(this.StartDate));
		scheduleValues.put("endDate", convertDate(this.EndDate));
		scheduleValues.put("fixed", this.Fixed);
		scheduleValues.put("autoEnter", this.AutoEnter);
		scheduleValues.put("lastPayment", convertDate(this.LastPaymentDate));
		scheduleValues.put("nextPaymentDue", convertDate(this.StartDate));
		scheduleValues.put("weekendOption", this.WeekendOption);
		scheduleValues.put("weekendOptionString", this.WeekendOptionString);
		
		// Save the actual schedule
		Uri u = Uri.withAppendedPath(KMMDProvider.CONTENT_SCHEDULE_URI,this.id + "#" + this.widgetId);
		u = Uri.parse(u.toString());
		this.context.getContentResolver().update(u, scheduleValues, null, null);
		
		// Save the transaction and splits for this schedule
		this.Transaction.Update();		
	}
	
	/***********************************************************************************************
	 * Required methods to make Schedule parcelable to pass between activities
	 * 
	 * Any time we are using this parcel to get Splits we MUST use the setContext() method to set the
	 * context of the actual Schedule as we can not pass this as part of the Parcel. Failing to do this
	 * will cause context to be null and crash!
	 **********************************************************************************************/

	public int describeContents() 
	{
		return 0;
	}

	public void writeToParcel(Parcel dest, int flags) 
	{		
		dest.writeString(widgetId);
		dest.writeString(id);
		dest.writeString(Description);
		dest.writeInt(Type);
		dest.writeString(TypeString);
		dest.writeInt(occurence);
		dest.writeInt(occurenceMultiplier);
		dest.writeString(occurenceString);
		dest.writeInt(paymentType);
		dest.writeString(paymentTypeString);
		dest.writeSerializable(StartDate);
		dest.writeSerializable(EndDate);
		dest.writeString(Fixed);
		dest.writeString(AutoEnter);
		dest.writeSerializable(LastPaymentDate);
		dest.writeSerializable(DueDate);
		dest.writeInt(WeekendOption);
		dest.writeString(WeekendOptionString);
		dest.writeLong(nAmount);
		dest.writeLong(nBalance);
		dest.writeString(title);
		dest.writeString(transDate);
		dest.writeTypedList(Splits);
		dest.writeParcelable(Transaction, flags);
	}
}
