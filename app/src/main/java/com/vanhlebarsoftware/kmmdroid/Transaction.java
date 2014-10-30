package com.vanhlebarsoftware.kmmdroid;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.xmlpull.v1.XmlSerializer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.util.Xml;

public class Transaction implements Parcelable
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
	
	private String widgetId;
	private long nAmount;
	private long nBalance;
	private String strMemo;
	private String strTransId;
	private String strtxType;
	private String strCurrencyId;
	private String strBankId;
	private Calendar Date;
	private Calendar entryDate;
	ArrayList<Split> splits;
	ArrayList<Split> origSplits;
	Context context;
	
	Transaction(String amount, String date, String memo, String transid, String wId, Context c)
	{
		this.context = c;
		this.widgetId = null;
		this.nAmount = convertToPennies(amount);
		this.nBalance = 0;
		this.strMemo = memo;
		this.strTransId = transid;
		this.strtxType = "N";
		Uri u = Uri.withAppendedPath(KMMDProvider.CONTENT_FILEINFO_URI, "#" + this.widgetId);
		u = Uri.parse(u.toString());
		Cursor baseCurrency = this.context.getContentResolver().query(u, new String[] { "baseCurrency" }, null, null, null);
		baseCurrency.moveToFirst();
		this.strCurrencyId = baseCurrency.getString(baseCurrency.getColumnIndex("baseCurrency"));
		baseCurrency.close();
		this.strBankId = null;
		if( date != null)
			this.Date = convertDate(date);
		else
			this.Date = null;
		this.entryDate = Calendar.getInstance();
		
		if( transid != null )
		{
			// Get the splits for this transaction
			u = Uri.withAppendedPath(KMMDProvider.CONTENT_SPLIT_URI,this.strTransId + "#" + this.widgetId);
			u = Uri.parse(u.toString());
			Cursor tranSplits = this.context.getContentResolver().query(u, null, null, null, null);
			this.splits = new ArrayList<Split>();
			for(int i=0; i<tranSplits.getCount(); i++)
				this.splits.add(new Split(tranSplits, i, wId, c));
			tranSplits.close();
		}
		else
			this.splits = new ArrayList<Split>();
		
		this.origSplits = new ArrayList<Split>();
	}
	
	Transaction(Cursor curTrans, String wId, Context c)
	{
		this.context = c;
		this.widgetId = null;
		curTrans.moveToFirst();
		this.nAmount = 0;
		this.nBalance = 0;
		this.strMemo = curTrans.getString(C_MEMO);
		this.strTransId = curTrans.getString(C_ID);
		this.Date = convertDate(curTrans.getString(C_POSTDATE));
		this.entryDate = convertDate(curTrans.getString(C_ENTRYDATE));
		this.strtxType = curTrans.getString(C_TXTYPE);
		this.strCurrencyId = curTrans.getString(C_CURRENCYID);
		this.strBankId = curTrans.getString(C_BANKID);
		
		// Get the splits for this transaction
		Uri u = Uri.withAppendedPath(KMMDProvider.CONTENT_SPLIT_URI,this.strTransId + "#" + this.widgetId);
		u = Uri.parse(u.toString());
		Cursor tranSplits = this.context.getContentResolver().query(u, null, null, null, null);
		this.splits = new ArrayList<Split>();
		for(int i=0; i<tranSplits.getCount(); i++)
			this.splits.add(new Split(tranSplits, i, wId, c));
		tranSplits.close();
		this.origSplits = new ArrayList<Split>();
	}
	
	Transaction(Context c, String id, String wId)
	{
		this.context = c;
		this.widgetId = wId;
		
		if( id == null )
		{
			this.nAmount = 0;
			this.nBalance = 0;
			this.strMemo = null;
			this.strTransId = null;
			this.Date = Calendar.getInstance();
			this.entryDate = Calendar.getInstance();
			this.strtxType = "N";
			Uri u = Uri.withAppendedPath(KMMDProvider.CONTENT_FILEINFO_URI, "#" + this.widgetId);
			u = Uri.parse(u.toString());
			Cursor baseCurrency = this.context.getContentResolver().query(u, new String[] { "baseCurrency" }, null, null, null);
			baseCurrency.moveToFirst();
			this.strCurrencyId = baseCurrency.getString(baseCurrency.getColumnIndex("baseCurrency"));
			baseCurrency.close();
			this.strBankId = null;
			this.splits = new ArrayList<Split>();
			this.origSplits = new ArrayList<Split>();
		}
		else
		{
			// Get our actual transaction record
			Uri u = Uri.withAppendedPath(KMMDProvider.CONTENT_TRANSACTION_URI,id + "#" + this.widgetId);
			u = Uri.parse(u.toString());
			Cursor trans = this.context.getContentResolver().query(u, null, null, null, null);
			trans.moveToFirst();
			this.strTransId = trans.getString(trans.getColumnIndex("id"));
			this.strtxType = trans.getString(trans.getColumnIndex("txType"));
			this.Date = convertDate(trans.getString(trans.getColumnIndex("postDate")));
			this.strMemo = trans.getString(trans.getColumnIndex("memo"));
			this.entryDate = convertDate(trans.getString(trans.getColumnIndex("entryDate")));
			this.strCurrencyId = trans.getString(trans.getColumnIndex("currencyId"));
			this.strBankId = trans.getString(trans.getColumnIndex("bankId"));
			trans.close();
			
			// Get the splits for this transaction
			u = Uri.withAppendedPath(KMMDProvider.CONTENT_SPLIT_URI,id + "#" + this.widgetId);
			u = Uri.parse(u.toString());
			Cursor tranSplits = this.context.getContentResolver().query(u, null, null, null, null);
			this.splits = new ArrayList<Split>();
			for(int i=0; i<tranSplits.getCount(); i++)
				this.splits.add(new Split(tranSplits, i, wId, c));
			tranSplits.close();
			this.origSplits = new ArrayList<Split>();
		}
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
	static public String convertToDollars(long pennies, boolean formatted, boolean isStock)
	{
		DecimalFormat decimal = new DecimalFormat();
		long denominator;
		
		if(isStock)
			denominator = 100;
		else
			denominator = 100;
		
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
			strNumber = String.format("%,d", (pennies / denominator));
		else
			strNumber = String.format("%d", (pennies / denominator));
			
		if(isStock)
			Log.d(TAG, "strNumber before switch: " + strNumber);
		
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

		if(isStock)
		{
			Log.d(TAG, "s: " + s);
			Log.d(TAG, "pennies: " + pennies);
			Log.d(TAG, "denominator: " + denominator);
			Log.d(TAG, "strNumber: " + strNumber);
		}
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
	
	public long getAmount()
	{
		return nAmount;
	}
	
	public long getBalance()
	{
		return nBalance;
	}
	
	public void setMemo(String m)
	{
		this.strMemo = m;
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
	
	public void setDate(Calendar d)
	{
		this.Date = d;
	}
	
	public void setDate(String date)
	{
		this.Date = this.convertDate(date);
	}
	
	public Calendar getDate()
	{
		return Date;
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
	
	public void setEntryDate(String date)
	{
		this.entryDate = this.convertDate(date);
	}
	
	public Calendar getEntryDate()
	{
		return this.entryDate;
	}
	
	public void setWidgetId(String wId)
	{
		this.widgetId = wId;
	}
	
	public String getWidgetId()
	{
		return this.widgetId;
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
	
	public void createId()
	{
		Uri u = Uri.withAppendedPath(KMMDProvider.CONTENT_FILEINFO_URI, "#" + this.widgetId);
		u = Uri.parse(u.toString());
		final String[] dbColumns = { "hiTransactionId"};
		final String strOrderBy = "hiTransactionId DESC";
		// Run a query to get the Transaction ids so we can create a new one.
		Cursor cursor = this.context.getContentResolver().query(u, dbColumns, null, null, strOrderBy); 		
		cursor.moveToFirst();

		// Since id is in T000000000000000000 format, we need to pick off the actual number then increase by 1.
		int lastId = cursor.getInt(0);
		lastId = lastId +1;
		String nextId = Integer.toString(lastId);
		
		// Need to pad out the number so we get back to our P000000 format
		String newId = "T";
		for(int i= 0; i < (18 - nextId.length()); i++)
		{
			newId = newId + "0";
		}
		
		// Tack on the actual number created.
		newId = newId + nextId;
		
		// Clean up our cursor
		cursor.close();
		
		this.strTransId = newId;
	}
	
	public void getDataChanges(CreateModifyTransactionActivity cont)
	{
		// Get our fragments for payee and category
		PayeeFragment payeeFrag = (PayeeFragment) cont.getSupportFragmentManager().findFragmentById(R.id.payeeFragment);
		CategoryFragment catFrag = (CategoryFragment) cont.getSupportFragmentManager().findFragmentById(R.id.categoryFragment);
		
		this.Date = cont.getPostDate();
		this.entryDate = Calendar.getInstance();
		this.strMemo = cont.editMemo.getText().toString();
		
		// We need to take our editAmount string which "may" contain a '.' as the decimal and replace it with the localized seperator.
		DecimalFormat decimal = new DecimalFormat();
		char decChar = decimal.getDecimalFormatSymbols().getDecimalSeparator();
		String strAmount = cont.editAmount.getText().toString().replace('.', decChar);
		
		if( !cont.getHasSplits() )
		{
			// Save the old splits into the origSplits arraylist for use later.
			this.origSplits = new ArrayList<Split>();
			for(Split split : this.splits)
				this.origSplits.add(split);
		
			// Create the splits information to be saved.	
			// Clear out any split we might of had when we first got started.
			this.splits.clear();
		
			// In any case we have to create our initial split with the account we are in.
			String value = null, formatted = null;
			switch( cont.getTransactionType() )
			{
				case Transaction.DEPOSIT:
					value = Account.createBalance(Transaction.convertToPennies(strAmount));
					break;
				case Transaction.TRANSFER:
					value = Account.createBalance(Transaction.convertToPennies(strAmount));
					break;
				case Transaction.WITHDRAW:
					value = "-" + Account.createBalance(Transaction.convertToPennies(strAmount));
					break;
				default:
					break;
			}
			formatted = Transaction.convertToDollars(Account.convertBalance(value), false, false);
			this.splits.add(new Split(this.strTransId, "N", 0, payeeFrag.getPayeeId(), "", cont.getTranAction(),
									String.valueOf(cont.getTransactionStatus()), value, formatted, value, formatted, "", "", this.strMemo,
									cont.getAccountUsed(), cont.getCheckNumber(), padDate(formatDateString()), this.strBankId, this.widgetId,
									this.context));
			if( cont.getNumberOfSplits() > 2 )
			{
				// Do nothing for now.
			}
			else
			{
				// The user doesn't have any actual splits so create them from the single category selected by the user.
				switch( cont.getTransactionType() )
				{
					case Transaction.DEPOSIT:
						value = "-" + Account.createBalance(Transaction.convertToPennies(strAmount));
						break;
					case Transaction.TRANSFER:
						// We have to take the current value and change the sign.
						value = Account.createBalance(Transaction.convertToPennies(strAmount) * -1);
						break;
					case Transaction.WITHDRAW:
						value = Account.createBalance(Transaction.convertToPennies(strAmount));
						break;
					default:
						break;
				}
				formatted = Transaction.convertToDollars(Account.convertBalance(value), false, false);
				this.splits.add(new Split(this.strTransId, "N", 1, payeeFrag.getPayeeId(), "", cont.getTranAction(),
					String.valueOf(cont.getTransactionStatus()), value, formatted, value, formatted, "", "", this.strMemo,
					catFrag.getCategoryId(), cont.getCheckNumber(), padDate(formatDateString()), this.strBankId, this.widgetId,
					this.context));
			}
		}
	}
	
	public void getDataChanges(CreateModifyScheduleActivity cont)
	{
		// Get our fragments for payee and category
		PayeeFragment payeeFrag = (PayeeFragment) cont.getSupportFragmentManager().findFragmentById(R.id.payeeFragment);
		CategoryFragment catFrag = (CategoryFragment) cont.getSupportFragmentManager().findFragmentById(R.id.categoryFragment);
		SchedulePaymentInfoActivity pmtInfo = (SchedulePaymentInfoActivity) cont.getSupportFragmentManager().findFragmentByTag("paymentinfo");

		// Set our entryDate to null since it is a schedule.
		this.entryDate = null;
		
		String strAmount = pmtInfo.getScheduleAmount();
		
		if( !cont.getHasSplits() )
		{
			// Save the old splits into the origSplits arraylist for use later.
			this.origSplits = new ArrayList<Split>();
			for(Split split : this.splits)
				this.origSplits.add(split);
		
			// Create the splits information to be saved.	
			// Clear out any split we might of had when we first got started.
			this.splits.clear();
		
			// In any case we have to create our initial split with the account we are in.
			String value = null, formatted = null;
			switch( pmtInfo.getScheduleType() )
			{
				case Transaction.DEPOSIT:
					value = Account.createBalance(Transaction.convertToPennies(strAmount));
					break;
				case Transaction.TRANSFER:
					value = Account.createBalance(Transaction.convertToPennies(strAmount));
					break;
				case Transaction.WITHDRAW:
					value = "-" + Account.createBalance(Transaction.convertToPennies(strAmount));
					break;
				default:
					break;
			}
			formatted = Transaction.convertToDollars(Account.convertBalance(value), false, false);
			this.splits.add(new Split(this.strTransId, "N", 0, payeeFrag.getPayeeId(), "", pmtInfo.getScheduleTypeString(),
									String.valueOf(pmtInfo.getScheduleStatus()), value, formatted, value, formatted, "", "", this.strMemo,
									pmtInfo.getAccountTypeId(), pmtInfo.getCheckNumber(), padDate(formatDateString()), this.strBankId, this.widgetId,
									this.context));
			if( cont.getNumberOfSplits() > 2 )
			{
				// Do nothing for now.
			}
			else
			{
				// The user doesn't have any actual splits so create them from the single category selected by the user.
				switch( pmtInfo.getScheduleType() )
				{
					case Transaction.DEPOSIT:
						value = "-" + Account.createBalance(Transaction.convertToPennies(strAmount));
						break;
					case Transaction.TRANSFER:
						// We have to take the current value and change the sign.
						value = Account.createBalance(Transaction.convertToPennies(strAmount) * -1);
						break;
					case Transaction.WITHDRAW:
						value = Account.createBalance(Transaction.convertToPennies(strAmount));
						break;
					default:
						break;
				}
				formatted = Transaction.convertToDollars(Account.convertBalance(value), false, false);
				this.splits.add(new Split(this.strTransId, "N", 1, payeeFrag.getPayeeId(), "", pmtInfo.getScheduleTypeString(),
					String.valueOf(pmtInfo.getScheduleStatus()), value, formatted, value, formatted, "", "", this.strMemo,
					catFrag.getCategoryId(), pmtInfo.getCheckNumber(), padDate(formatDateString()), this.strBankId, this.widgetId,
					this.context));
			}
		}		
	}
	
	public void Save()
	{
		// create the ContentValue pairs
		ContentValues valuesTrans = new ContentValues();
		valuesTrans.put("id", this.strTransId);
		valuesTrans.put("txType", "N");
		valuesTrans.put("postDate", padDate(this.formatDateString()));
		valuesTrans.put("memo", this.strMemo);
        valuesTrans.put("entryDate", padDate(this.formatEntryDateString()));
		valuesTrans.put("currencyId", this.getCurrencyId());
		valuesTrans.put("bankId", "");

		Uri u = Uri.withAppendedPath(KMMDProvider.CONTENT_TRANSACTION_URI, "#" + this.widgetId);
		u = Uri.parse(u.toString());
		this.context.getContentResolver().insert(u, valuesTrans);
		u = Uri.withAppendedPath(KMMDProvider.CONTENT_FILEINFO_URI, "#" + this.widgetId);
		u = Uri.parse(u.toString());
		this.context.getContentResolver().update(u, null, "hiTransactionId", new String[] { "1" });
		this.context.getContentResolver().update(u, null, "transactions", new String[] { "1" });
		this.context.getContentResolver().update(u, null, "splits", new String[] { String.valueOf(this.splits.size()) });
	
		// Insert the splits for this transaction
		for(Split s : this.splits)
		{
			// Make sure we have the transactionId in our split and the correct context.
			s.setTransactionId(this.strTransId);
			s.setContext(this.context);
			s.commitSplit(false);
			Account.updateAccount(this.context, s.getAccountId(), s.getValueFormatted(), 1);
		}	
	}
	
	public void Update()
	{
		// create the ContentValue pairs
		ContentValues valuesTrans = new ContentValues();
		valuesTrans.put("id", this.strTransId);
		valuesTrans.put("txType", "N");
		valuesTrans.put("postDate", padDate(this.formatDateString()));
		valuesTrans.put("memo", this.strMemo);
        valuesTrans.put("entryDate", padDate(this.formatEntryDateString()));
		valuesTrans.put("currencyId", this.getCurrencyId());
		valuesTrans.put("bankId", "");		
		
		Uri u = Uri.withAppendedPath(KMMDProvider.CONTENT_TRANSACTION_URI,this.strTransId + "#" + this.widgetId);
		u = Uri.parse(u.toString());
		this.context.getContentResolver().update(u, valuesTrans, null, null);
		// Delete all the splits for this transaction first, getting the number or rows deleted.
		u = Uri.withAppendedPath(KMMDProvider.CONTENT_SPLIT_URI, "#" + this.widgetId);
		u = Uri.parse(u.toString());
		int rowsDel = this.context.getContentResolver().delete(u, "transactionId=?", new String[] { this.strTransId });
		u = Uri.withAppendedPath(KMMDProvider.CONTENT_FILEINFO_URI, "#" + this.widgetId);
		u = Uri.parse(u.toString());
		this.context.getContentResolver().update(u, null, "splits", new String[] { String.valueOf(this.origSplits.size() - rowsDel) });

		// Need to update the account by pulling out all the Original Splits information.
		for(Split origsplit : this.origSplits)
			Account.updateAccount(this.context, origsplit.getAccountId(), origsplit.getValueFormatted(), -1);

		// Insert the splits for this transaction
		for(Split s : this.splits)
		{
			// Make sure we have the correct context.
			s.setContext(this.context);
			s.commitSplit(false);
			Account.updateAccount(this.context, s.getAccountId(), s.getValueFormatted(), 1);
		}		
	}
	
	public boolean cacheTransaction()
	{
        XmlSerializer serializer = Xml.newSerializer();
        StringWriter writer = new StringWriter();
        try 
        {
            serializer.setOutput(writer);
            serializer.startDocument("UTF-8", true);
            serializer.docdecl(" KMMDROID-CACHE");
            serializer.startTag("", "KMMDCache");
            serializer.startTag("", "Transaction");
            serializer.attribute("", "id", this.getTransId() == null ? "" : this.getTransId());
            serializer.attribute("", "txType", this.getTxType());
            serializer.attribute("", "postDate", padDate(this.formatDateString()));
            serializer.attribute("", "memo", this.getMemo());
            serializer.attribute("", "entryDate", padDate(this.formatEntryDateString()));
            serializer.attribute("", "currencyId", this.getCurrencyId());
            serializer.attribute("", "bankId", this.getBankId() == null ? "" : this.getBankId());
            serializer.attribute("", "widgetId", this.getWidgetId());
            for (Split split : this.splits)
            {
            	serializer.startTag("", "Split");
           		serializer.attribute("", "transactionId", split.getTransactionId() == null ? "" : split.getTransactionId());
           		serializer.attribute("", "txType", split.getTxType());
           		serializer.attribute("", "splitId", String.valueOf(split.getSplitId()));
           		serializer.attribute("", "payeeId", split.getPayeeId() == null ? "" : split.getPayeeId());
           		serializer.attribute("", "reconcildeDate", split.getReconcileDate() == null ? "" : split.getReconcileDate());
           		serializer.attribute("", "action", split.getAction() == null ? "" : split.getAction());
           		serializer.attribute("", "reconcileFlag", split.getReconcileFlag());
           		serializer.attribute("", "value", split.getValue() == null ? "" : split.getValue());
           		serializer.attribute("", "valueFormatted", split.getValueFormatted() == null ? "" : split.getValueFormatted());
           		serializer.attribute("", "shares", split.getShares() == null ? "" : split.getShares());
           		serializer.attribute("", "sharesFormatted", split.getSharesFormatted() == null ? "" : split.getSharesFormatted());
           		serializer.attribute("", "price", split.getPrice() == null ? "" : split.getPrice());
           		serializer.attribute("", "priceFormatted", split.getPriceFormatted() == null ? "" : split.getPriceFormatted());
           		serializer.attribute("", "memo", split.getMemo() == null ? "" : split.getMemo());
           		serializer.attribute("", "accountId", split.getAccountId() == null ? "" : split.getAccountId());
           		serializer.attribute("", "checkNumber", split.getCheckNumber() == null ? "" : split.getCheckNumber());
           		serializer.attribute("", "postDate", split.getPostDate() == null ? "" : split.getPostDate());
           		serializer.attribute("", "bankId", split.getBankId() == null ? "" : split.getBankId());
           		serializer.endTag("", "Split");
            }
            serializer.endTag("", "Transaction");
            serializer.endTag("", "KMMDCache");
            serializer.endDocument();
        } 
        catch (Exception e) 
        {
            e.printStackTrace();
            return false;
        }
        		
        // Attempt to write the splits to cache.
        File cache = this.context.getCacheDir();

        try 
        {
			FileOutputStream fos = new FileOutputStream(cache.getPath() + File.separator + "tempSplits");
			fos.write(writer.toString().getBytes());
			fos.close();
		} 
        catch (FileNotFoundException e) 
        {
			e.printStackTrace();
			return false;
		} 
        catch (IOException e) 
        {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	public void getcachedTransaction()
	{
        File cache = this.context.getCacheDir();
    	KMMDSplitParser parser = new KMMDSplitParser(cache.getPath() + File.separator + "tempSplits", this.context);
    	Transaction tmpTrans = parser.parse();
    	
    	// Calc the Amount of our transaction.
    	tmpTrans.calcTransactionAmount();
    	
    	// Make sure our splits array is clear before copying the imported transaction to our current transaction.
    	this.splits.clear();
    	
    	// copy the loaded transaction into our object.
    	this.copy(tmpTrans);
    	
    	// Clear the cache
    	File tmpFile = new File(cache.getPath() + "tempSplits");
    	tmpFile.delete();
	}
	
	public void calcTransactionAmount()
	{
		for(int i=1; i<this.splits.size(); i++)
			this.nAmount =+ Account.convertBalance(this.splits.get(i).getValue());
	}
	
	public void copy(Transaction tmp)
	{
		this.nAmount = tmp.nAmount;
		this.nBalance = tmp.nBalance;
		this.strMemo = tmp.strMemo;
		this.strTransId = tmp.strTransId;
		this.Date = tmp.Date;
		this.entryDate = tmp.entryDate;
		this.strtxType = tmp.strtxType;
		this.strCurrencyId = tmp.strCurrencyId;
		this.strBankId = tmp.strBankId;
		this.context = tmp.context;
		this.widgetId = tmp.widgetId;
		for(Split split : tmp.splits)
		{
			this.splits.add(split);
			this.origSplits.add(split);
		}
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

	/***********************************************************************************************
	 * Required methods to make Transaction parcelable to pass between activities
	 * 
	 * Any time we are using this parcel to get a Transaction we MUST use the setContext() method to set the
	 * context of the actual Transaction as we can not pass this as part of the Parcel. Failing to do this
	 * will cause context to be null and crash!
	 **********************************************************************************************/
	public void setContext(Context c)
	{
		this.context = c;
	}
	
	public int describeContents() 
	{
		return 0;
	}

	public void writeToParcel(Parcel dest, int flags) 
	{
		dest.writeString(widgetId);
		dest.writeLong(nAmount);
		dest.writeLong(nBalance);
		dest.writeString(strMemo);
		dest.writeString(strTransId);
		dest.writeString(strtxType);
		dest.writeString(strCurrencyId);
		dest.writeString(strBankId);
		dest.writeSerializable(Date);
		dest.writeSerializable(entryDate);
		dest.writeTypedList(splits);
		dest.writeTypedList(origSplits);
	}
}
