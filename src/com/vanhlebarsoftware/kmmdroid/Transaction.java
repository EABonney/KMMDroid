package com.vanhlebarsoftware.kmmdroid;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class Transaction 
{
	private static final String TAG = Transaction.class.getSimpleName();
	private static final int C_ID = 0;
	private static final int C_TXTYPE = 1;	
	private static final int C_POSTDATE = 2;
	private static final int C_MEMO = 3;
	private static final int C_ENTRYDATE = 4;
	private static final int C_CURRENCYID = 5;
	private static final int C_BANKID = 6;
	public static final int DEPOSIT = 0;
	public static final int TRANSFER = 1;
	public static final int WITHDRAW = 2;
	
	private long nAmount;
	private long nBalance;
	private String strPayee;
	private String strMemo;
	private String strTransId;
	private String strStatus;
	private String strCheckNum;
	private String strtxType;
	private String strCurrencyId;
	private String strBankId;
	private Calendar Date;
	private Calendar entryDate;
	ArrayList<Split> splits;
	
	Transaction(String amount, String payee, String date, String memo, String transid, String status, String checknum)
	{
		this.nAmount = convertToPennies(amount);
		this.nBalance = 0;
		this.strPayee = payee;
		this.strMemo = memo;
		this.strTransId = transid;
		this.strStatus = status;
		this.strCheckNum = checknum;
		this.strtxType = null;
		this.strCurrencyId = null;
		this.strBankId = null;
		if( date != null)
			this.Date = convertDate(date);
		else
			this.Date = null;
		this.entryDate = null;
		this.splits = new ArrayList<Split>();
	}
	
	Transaction(Cursor curTrans)
	{
		curTrans.moveToFirst();
		this.nAmount = 0;
		this.nBalance = 0;
		this.strPayee = null;
		this.strMemo = curTrans.getString(C_MEMO);
		this.strTransId = curTrans.getString(C_ID);
		this.strStatus = null;
		this.strCheckNum = null;
		this.Date = convertDate(curTrans.getString(C_POSTDATE));
		this.entryDate = convertDate(curTrans.getString(C_ENTRYDATE));
		this.strtxType = curTrans.getString(C_TXTYPE);
		this.strCurrencyId = curTrans.getString(C_CURRENCYID);
		this.strBankId = curTrans.getString(C_BANKID);
		this.splits = new ArrayList<Split>();
	}
	/********************************************************************************************
	* Adapted from code found at currency : Java Glossary
	* website: http://mindprod.com/jgloss/currency.html
	********************************************************************************************/
	static public long convertToPennies(String numStr)
	{
		DecimalFormat decimal = new DecimalFormat();
		char decChar = decimal.getDecimalFormatSymbols().getDecimalSeparator();
		
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
		        /*case e :
		        	if ( decpl == -1 )
		            {
		               decpl = 0;
		            }
		            else
		            {
		               throw new NumberFormatException( "more than one decimal point" );
		            }
		            break;*/
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
		        	if(c == decChar)
		        	{
			        	if ( decpl == -1 )
			            {
			               decpl = 0;
			            }
			            else
			            {
			               throw new NumberFormatException( "more than one decimal point" );
			            }
		        	}
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
			Log.d(TAG, "numStr: " + numStr);
			Log.d(TAG, "Number of decimals: " + String.valueOf(decpl));
			Log.d(TAG, "num:" + String.valueOf(num));
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
	static public String convertToDollars(long pennies, boolean formatted)
	{
		DecimalFormat decimal = new DecimalFormat();
		
		boolean negative;
		if ( pennies < 0 )
		{
			pennies = -pennies;
		    negative = true;
		}
		else
			negative = false;

		String s = Long.toString( pennies );
		// if formatted == true then we want to localize the formatting of the string for display
		// else we are putting the string into the table and we want to NOT have formatting other
		// than the symbol between dollars and cents.
		String strNumber = null;
		if(formatted)
			strNumber = String.format("%,d", (pennies / 100));
		else
			strNumber = String.format("%d", (pennies / 100));
		int len = s.length();
		switch ( len )
		{
			case 1:
		        strNumber = "0" + decimal.getDecimalFormatSymbols().getDecimalSeparator() + "0" + s;
		        break;
		    case 2:
		        strNumber = "0" + decimal.getDecimalFormatSymbols().getDecimalSeparator() + s;
		        break;
		    default:
		    	strNumber = strNumber + decimal.getDecimalFormatSymbols().getDecimalSeparator() + s.substring(len-2, len);
		        break;
		} // end switch
		
		if ( negative && formatted )
			strNumber = "(" + strNumber + ")";
		else if( negative && !formatted)
			strNumber = "-" + strNumber;

		return strNumber;
	}
	
	public long calcBalance(long nPrevBal, long nPrevTrans)
	{
		// Since we are working backwards we need to actually "add" or do the opposite of the current transaction.
		this.nBalance = nPrevBal - nPrevTrans;
		
		return this.nBalance; 
	}
	
	public String formatDateString()
	{
		return String.valueOf(this.Date.get(Calendar.YEAR) + "-" + String.valueOf(this.Date.get(Calendar.MONTH) + 1) + "-" 
					+ String.valueOf(this.Date.get(Calendar.DAY_OF_MONTH)));
	}
	
	public String formatEntryDateString()
	{
		return String.valueOf(this.entryDate.get(Calendar.YEAR) + "-" + String.valueOf(this.entryDate.get(Calendar.MONTH) + 1) + "-" 
				+ String.valueOf(this.entryDate.get(Calendar.DAY_OF_MONTH)));		
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
	
	public void setTransId(String id)
	{
		this.strTransId = id;
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
	
	public String getCheckNum()
	{
		return this.strCheckNum;
	}
	
	public void setBalance(long amount)
	{
		nBalance = amount;
	}
	
	public void setTxType(String type)
	{
		this.strtxType = type;
	}
	
	public String getTxType()
	{
		return this.strtxType;
	}
	
	public void setCurrencyId(String id)
	{
		this.strCurrencyId = id;
	}
	
	public String getCurrencyId()
	{
		return this.strCurrencyId;
	}
	
	public void setBankId(String id)
	{
		this.strBankId = id;
	}
	
	public String getBankId()
	{
		return this.strBankId;
	}
	
	public void setEntryDate(Calendar date)
	{
		this.entryDate = date;
	}
	
	public Calendar getEntryDate()
	{
		return this.entryDate;
	}
	
	public boolean isFuture()
	{
		Calendar today = Calendar.getInstance();
		return this.Date.after(today);
	}
	
	private Calendar convertDate(String date)
	{
		Calendar cDate = new GregorianCalendar();
		
		if(date != null)
		{
			String dates[] = date.split("-");
			cDate.set(Integer.valueOf(dates[0]), Integer.valueOf(dates[1]) - 1, Integer.valueOf(dates[2]));
		}
		
		return cDate;
	}
	
	public void enter(SQLiteDatabase db)
	{
		// create the ContentValue pairs
		ContentValues valuesTrans = new ContentValues();
		valuesTrans.put("id", this.strTransId);
		valuesTrans.put("txType", this.strtxType);
		valuesTrans.put("postDate", formatDateString());
		valuesTrans.put("memo", this.strMemo);
		valuesTrans.put("entryDate", formatEntryDateString());
		valuesTrans.put("currencyId", this.strCurrencyId);
		valuesTrans.put("bankId", this.strBankId);
		
		// Enter this transaction into the kmmTransactions table.
		db.insertOrThrow("kmmTransactions", null, valuesTrans);
		
		// Enter the splits into the kmmSplits table.
		for(int i=0; i<this.splits.size(); i++)
		{
			Log.d(TAG, "Trans Split #" + i + ": " + this.splits.get(i).getPostDate());
			this.splits.get(i).commitSplit(false, db);
			Account.updateAccount(db, this.splits.get(i).getAccountId(), this.splits.get(i).getValueFormatted(), 1);
		}
	}
}
