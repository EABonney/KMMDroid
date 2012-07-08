package com.vanhlebarsoftware.kmmdroid;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import android.database.Cursor;
import android.util.Log;

public class Schedule 
{
	private static final String TAG = Schedule.class.getSimpleName();
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
	
	private String id;
	private String Description;
	private Calendar DueDate;
	private Calendar EndDate;
	private long nAmount;		//Holds the amount of this transaction in pennies.
	private long nBalance;		//Hold the balance AFTER this transactions occurs. This is calculated and only for the Cash Required report.
								//Held in pennies.
	private int occurence;
	private int occurenceMultiplier;
	
	// Constructor for a Schedule
	Schedule(String Desc, Calendar dueDate, String strAmt)
	{
		this.id = null;
		this.occurence = 0;
		this.occurenceMultiplier = 0;
		this.Description = Desc;
		this.DueDate = dueDate;
		this.EndDate = null;
		this.nAmount = convertToPennies(strAmt);
		this.nBalance = 0;
	}
	
	Schedule(Cursor c)
	{
		String[] date = {null, null, null};
		c.moveToFirst();
		
		this.id = c.getString(C_ID);
		this.occurence = c.getInt(C_OCCURENCE);
		this.occurenceMultiplier = c.getInt(C_OCCURENCEMULTIPLIER);
		this.Description = c.getString(C_DESCRIPTION);
		this.nAmount = convertToPennies(c.getString(C_VALUEFORMATTED));
		this.nBalance = 0;
		if(c.getString(C_NEXTPAYMENTDUE) != null)
		{
			this.DueDate = Calendar.getInstance();
			date = c.getString(C_NEXTPAYMENTDUE).split("-");			
			this.DueDate.set(Integer.valueOf(date[0]), Integer.valueOf(date[1]) - 1, Integer.valueOf(date[2]));
			date[0] = date[1] = date[2] = null;
		}
		
		if(c.getString(C_ENDDATE) != null)
		{
			this.EndDate = Calendar.getInstance();
			date = c.getString(C_ENDDATE).split("-");
			this.EndDate.set(Integer.valueOf(date[0]), Integer.valueOf(date[1]) - 1, Integer.valueOf(date[2]));
		}
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
		Log.d(TAG, "Date: " + DueDate.toString());
		return String.valueOf(this.DueDate.get(Calendar.MONTH) + 1) + "/" + String.valueOf(this.DueDate.get(Calendar.DAY_OF_MONTH)) +
				"/" + String.valueOf(this.DueDate.get(Calendar.YEAR));
	}
	
	static public ArrayList<Schedule> BuildCashRequired(Cursor c, String strStartDate, String strEndDate, long nBegBalance)
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
				schd = new Schedule(c.getString(C_DESCRIPTION), dueDates.get(d), c.getString(C_VALUEFORMATTED));
				// Add the id, occurence, occurenceMultiplier and enddate for this schedule
				schd.setId(c.getString(C_ID));
				schd.setOccurence(c.getInt(C_OCCURENCE));
				schd.setOccurenceMultiplier(c.getInt(C_OCCURENCEMULTIPLIER));
				schd.setEndDate(c.getString(C_ENDDATE));
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
	
	public boolean isPastDue()
	{
		GregorianCalendar calToday = new GregorianCalendar();
		
		return this.DueDate.before(calToday);
	}
	
	public void skipSchedule()
	{
		this.advanceDueDate(Schedule.getOccurence(this.occurence, this.occurenceMultiplier));
	}
	
	private void advanceDueDate(int occurenceRate)
	{
		
		switch (occurenceRate)
		{
			case OCCUR_ONCE:
				if(this.DueDate.before(this.EndDate) || this.EndDate == null)
					this.DueDate = this.EndDate;
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
	}
	
	public String getDatabaseFormattedString()
	{
		return String.valueOf(this.DueDate.get(Calendar.YEAR)) + "-" + String.valueOf(this.DueDate.get(Calendar.MONTH) + 1) +
				"-" + String.valueOf(this.DueDate.get(Calendar.DAY_OF_MONTH));
	}
}