package com.vanhlebarsoftware.kmmdroid;

import java.math.BigInteger;
import java.util.Calendar;
import java.util.StringTokenizer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

public class Account 
{
	private static final String TAG = Account.class.getSimpleName();
	public static final int ACCOUNT_CHECKING = 1;
	public static final int ACCOUNT_SAVINGS = 2;
	public static final int ACCOUNT_CASH = 3;
	public static final int ACCOUNT_CREDITCARD = 4;
	public static final int ACCOUNT_LOAN = 4;
	public static final int ACCOUNT_CERTIFICATEDEP = 6;
	public static final int ACCOUNT_INVESTMENT = 7;
	public static final int ACCOUNT_MONEYMARKET = 8;
	public static final int ACCOUNT_ASSET = 9;
	public static final int ACCOUNT_LIABILITY = 10;
	public static final int ACCOUNT_CURRENCY = 11;
	public static final int ACCOUNT_INCOME = 12;
	public static final int ACCOUNT_EXPENSE = 13;
	public static final int ACCOUNT_ASSETLOAN = 14;
	public static final int ACCOUNT_STOCK = 15;
	public static final int ACCOUNT_EQUITY = 16;
	
	private String id;
	private String parentId;
	private String accountName;
	private String balance;
	private String accountTypeString;
	private int accountType;
	private boolean isParent;
	

	Account()
	{		
		this.id = null;
		this.parentId = null;
		this.accountName = null;
		this.balance = null;
		this.accountTypeString = null;
		this.accountType = 0;
		this.isParent = false;
	}
	
	Account(String id, String name, String bal, String acctTypeStr, int acctType, boolean isP)
	{
		this.id = id;
		this.parentId = null;
		this.accountName = name;
		this.balance = bal;
		this.accountTypeString = acctTypeStr;
		this.accountType = acctType;
		this.isParent = isP;
	}
	
	Account(Cursor cur)
	{
		this.id = cur.getString(cur.getColumnIndex("_id"));
		this.parentId = cur.getString(cur.getColumnIndex("parentId"));
		this.accountName = cur.getString(cur.getColumnIndex("accountName"));
		this.balance = cur.getString(cur.getColumnIndex("balance"));
		this.accountTypeString = null;
		this.accountType = 0;
		this.isParent = false;
	}
	
	public String getName()
	{
		return this.accountName;
	}
	
	public String getId()
	{
		return this.id;
	}
	
	public String getBalance()
	{
		return this.balance;
	}
	
	public String getParentId()
	{
		return this.parentId;
	}
	
	public boolean getIsParent()
	{
		return this.isParent;
	}
	
	public int getAccountType()
	{
		return this.accountType;
	}
	
	public String getAccountTypeString()
	{
		return this.accountTypeString;
	}
	
	public int getNumberSubAccounts(Context context)
	{
		String frag = "#9999";
		Uri u = Uri.withAppendedPath(KMMDProvider.CONTENT_ACCOUNT_URI, this.getId() + frag);
		u = Uri.parse(u.toString());
		Cursor c = context.getContentResolver().query(u, new String[] { "id" }, "parentId=?", new String[] {this.getId()}, null);
		int subAccts = c.getCount();
		c.close();
		
		return subAccts;
	}
	
	public void setIsParent(boolean flag)
	{
		this.isParent = flag;
	}
	
	static public void updateAccount(SQLiteDatabase db, String accountId, String transValue, int nChange)
	{
		Cursor c = db.query("kmmAccounts", new String[] { "balance", "transactionCount" }, "id=?", new String[] { accountId }, null, null, null);
		c.moveToFirst();
		
		// Update the current balance for this account.
		long balance = Account.convertBalance(c.getString(0));
		
		// If we are editing a transaction we need to reverse the original transaction values, this takes care of that for us.
		long tValue = Transaction.convertToPennies(transValue) * nChange;
		
		long newBalance = balance + tValue;

		// Update the number of transactions used for this account.
		int Count = c.getInt(1) + nChange;
		
		ContentValues values = new ContentValues();
		values.put("balanceFormatted", Transaction.convertToDollars(newBalance, false));
		values.put("balance", createBalance(newBalance));
		values.put("transactionCount", Count);
		
		db.update("kmmAccounts", values, "id=?", new String[] { accountId });
		
		// Do clean up of the cursor.
		c.close();
	}
	
	static public String createBalance(Long newBalance)
	{
		/***************************************************************************
		 * We need to take the value passed in and convert
		 * it to a fraction, which should be reduced as far
		 * as possible.
		 * 
		 * Example:
		 *		newBalance = -12532
		 *		returned fraction = -3133/25
		 *
		 * Process of how we get there:
		 * 					-12532/100
		 * We get the greatest common denominator using BigIntegers and the built-in
		 * function of gcd(BigInteger). 
		 * 
		 * Using that gcd, we divide each fraction part by the gcd then convert them to
		 * Longs and return that as a string of numberator/denominator
		 **************************************************************************/
		BigInteger num = BigInteger.valueOf(newBalance);
		BigInteger den = BigInteger.valueOf(Long.valueOf(100));
		BigInteger gcd = num.gcd(den);

		Long numerator = num.longValue() / gcd.longValue();
		Long denominator = den.longValue() / gcd.longValue();
		
		return String.valueOf(numerator) + "/" + String.valueOf(denominator);
	}
	
	static public Long convertBalance(String fraction)
	{
		/***************************************************************************
		 * We take in the fraction string from the database and convert it to our
		 * Long value of cents.
		 **************************************************************************/
		String parts[] = fraction.split("/");
		Float num = Float.valueOf(parts[0]);
		Float den = Float.valueOf(parts[1]);
		Float value = (num / den) * 100;
		
		return Long.valueOf(value.longValue());
	}
	
	static public String adjustForFutureTransactions(String accountId, Long balance, SQLiteDatabase db)
	{
		String newBalanceFormatted = null;
		// Get today's date and format it correctly for the sql query.
		Calendar date = Calendar.getInstance();
		String strDate = date.get(Calendar.YEAR) + "-" + (date.get(Calendar.MONTH) + 1) + "-" + date.get(Calendar.DAY_OF_MONTH);
		strDate = Schedule.padFormattedDate(strDate);
		
		// Get all the splits for this account that are in the future.
		Cursor c = db.query("kmmSplits", new String[] { "valueFormatted" }, "postDate>? AND splitId=0 AND accountId=? AND txType='N'",
				new String[] { strDate, accountId }, null, null, null);
		
		// loop over the cursor and adjust the passed in balance for the values returned.
		c.moveToFirst();

		for(int i=0; i < c.getCount(); i++)
		{
			// Since our current balance includes the future transactions we need to do the opposite of the transaction to correct the balance
			balance = balance - Transaction.convertToPennies(c.getString(0));
		}
		
		newBalanceFormatted = Transaction.convertToDollars(balance, true);
		
		return newBalanceFormatted;
	}

	static public String adjustForFutureTransactions(String accountId, Long balance, Cursor splits)
	{
		String newBalanceFormatted = null;

		while(splits.moveToNext())
		{
			// Since our current balance includes the future transactions we need to do the opposite of the transaction to correct the balance
			balance = balance - Transaction.convertToPennies(splits.getString(0));
		}
		
		newBalanceFormatted = Transaction.convertToDollars(balance, true);
		
		return newBalanceFormatted;
	}
}
