package com.vanhlebarsoftware.kmmdroid;

import java.util.Calendar;
import java.util.GregorianCalendar;

import android.util.Log;

public class Transaction 
{
	private static final String TAG = "Transaction.class";
	private long nAmount;
	private long nBalance;
	private String strPayee;
	private String strMemo;
	private String strTransId;
	private String strStatus;
	private Calendar Date;
	
	Transaction(String amount, String payee, String date, String memo, String transid, String status)
	{
		this.nAmount = convertToPennies(amount);
		this.nBalance = 0;
		this.strPayee = payee;
		this.strMemo = memo;
		this.strTransId = transid;
		this.strStatus = status;
		this.Date = convertDate(date);
	}
	
	/********************************************************************************************
	* Adapted from code found at currency : Java Glossary
	* website: http://mindprod.com/jgloss/currency.html
	********************************************************************************************/
	static public long convertToPennies(String numStr)
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
	static public String convertToDollars(long pennies)
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
		        strNumber = "0.0" + s;
		        break;
		    case 2:
		        strNumber = "0." + s;
		        break;
		    default:
		    	strNumber = strNumber + "." + s.substring(len-2, len);
		        break;
		} // end switch
		
		if ( negative )
			strNumber = "(" + strNumber + ")";

		return strNumber;
	}
	
	public long calcBalance(long nPrevBal, long nPrevTrans)
	{
		// Since we are working backwards we need to actually "add" or do the opposit of thhe current transaction.
		this.nBalance = nPrevBal - nPrevTrans;
		
		return this.nBalance; 
	}
	
	public String formatDateString()
	{
		return String.valueOf(this.Date.get(Calendar.YEAR) + "-" + String.valueOf(this.Date.get(Calendar.MONTH) + 1) + "-" 
					+ String.valueOf(this.Date.get(Calendar.DAY_OF_MONTH)));
	}
	
	public String getPayee()
	{
		return strPayee;
	}
	
	public long getAmount()
	{
		return nAmount;
	}
	
	public long getBalance()
	{
		return nBalance;
	}
	
	public String getMemo()
	{
		return strMemo;
	}
	
	public String getTransId()
	{
		return strTransId;
	}
	
	public String getStatus()
	{
		return strStatus;
	}
	
	public Calendar getDate()
	{
		return Date;
	}
	
	public void setBalance(long amount)
	{
		nBalance = amount;
	}
	
	private Calendar convertDate(String date)
	{
		Calendar cDate = new GregorianCalendar();
		String dates[] = date.split("-");
		cDate.set(Integer.valueOf(dates[0]), Integer.valueOf(dates[1]), Integer.valueOf(dates[2]));
		
		return cDate;
	}
}
