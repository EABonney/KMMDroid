package com.vanhlebarsoftware.kmmdroid;

import java.util.Calendar;
import java.util.StringTokenizer;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class Account 
{
	private static final String TAG = Account.class.getSimpleName();

	Account()
	{		
	}
	
	static public void updateAccount(SQLiteDatabase db, String accountId, String transValue, int nChange)
	{
		Cursor c = db.query("kmmAccounts", new String[] { "balanceFormatted", "transactionCount" }, "id=?", new String[] { accountId }, null, null, null);
		c.moveToFirst();
		
		// Update the current balance for this account.
		long balance = Transaction.convertToPennies(c.getString(0));
		
		// If we are editing a transaction we need to reverse the original transaction values, this takes care of that for us.
		long tValue = Transaction.convertToPennies(transValue) * nChange;
		
		long newBalance = balance + tValue;

		// Update the number of transactions used for this account.
		int Count = c.getInt(1) + nChange;
		
		ContentValues values = new ContentValues();
		values.put("balanceFormatted", Transaction.convertToDollars(newBalance));
		values.put("balance", createBalance(newBalance));
		values.put("transactionCount", Count);
		
		db.update("kmmAccounts", values, "id=?", new String[] { accountId });
		
		// Do clean up of the cursor.
		c.close();
	}
	
	static public String createBalance(Long newBalance)
	{
		String balance = String.valueOf(newBalance);
		String denominator = "/100";
		Log.d(TAG, "createBalance Returning: " + balance + denominator);
		return balance + denominator;
	}
	
	static public String adjustForFutureTransactions(String accountId, String balanceFormatted, SQLiteDatabase db)
	{
		String newBalanceFormatted = null;
		Long balance = Transaction.convertToPennies(balanceFormatted);
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
		
		newBalanceFormatted = Transaction.convertToDollars(balance);
		
		return newBalanceFormatted;
	}
}
