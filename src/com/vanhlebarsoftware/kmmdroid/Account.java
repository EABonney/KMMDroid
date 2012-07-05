package com.vanhlebarsoftware.kmmdroid;

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
}
