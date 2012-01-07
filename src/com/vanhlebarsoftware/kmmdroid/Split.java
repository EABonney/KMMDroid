package com.vanhlebarsoftware.kmmdroid;

import android.content.ContentValues;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class Split
{
	private static final String TAG = "ClassSplit";
	private String transactionId;
	private String txType;
	private int splitId;
	private String payeeId;
	private String reconcileDate;
	private String action;
	private String reconcileFlag;
	private String value;
	private String valueFormatted;
	private String shares;
	private String sharesFormatted;
	private String price;
	private String priceFormatted;
	private String memo;
	private String accountId;
	private String checkNumber;
	private String postDate;
	private String bankId;
	
	// Constructor used for creating new Splits.
	Split(String tId, String tType, int sId, String pId, String rDate, String a, String rFlag,
			String v, String vFormatted, String s, String sFormatted, String p, String pFormatted,
			String m, String aId, String ckNumber, String pDate, String bId)
	{
		this.transactionId = tId;
		this.txType = tType;
		this.splitId = sId;
		this.payeeId = pId;
		this.reconcileDate = rDate;
		this.action = a;
		this.reconcileFlag = rFlag;
		this.value = v;
		this.valueFormatted = vFormatted;
		this.shares = s;
		this.sharesFormatted = sFormatted;
		this.price = p;
		this.priceFormatted = pFormatted;
		this.memo = m;
		this.accountId = aId;
		this.checkNumber = ckNumber;
		this.postDate = pDate;
		this.bankId = bId;
	}
	
	public boolean commitSplit(boolean updating, SQLiteDatabase db)
	{
	
		// create the ContentValue pairs
		ContentValues valuesSplit = new ContentValues();
		valuesSplit.put("transactionId", transactionId);
		valuesSplit.put("txType", txType);
		valuesSplit.put("splitId", splitId);
		valuesSplit.put("payeeId", payeeId);
		valuesSplit.put("reconcileDate", reconcileDate);
		valuesSplit.put("action", action);
		valuesSplit.put("reconcileFlag", reconcileFlag);
		valuesSplit.put("value", value);
		valuesSplit.put("valueFormatted", valueFormatted);
		valuesSplit.put("shares", shares);
		valuesSplit.put("sharesFormatted", sharesFormatted);
		valuesSplit.put("price", price);
		valuesSplit.put("priceFormatted", priceFormatted);
		valuesSplit.put("memo", memo);
		valuesSplit.put("accountId", accountId);
		valuesSplit.put("checkNumber", checkNumber);
		valuesSplit.put("postDate", postDate);
		valuesSplit.put("bankId", bankId);
		
		if( updating )
		{
			db.update("kmmSplits", valuesSplit, "transactionId=? AND splitId=?", new String[] { transactionId, String.valueOf(splitId) });
		}
		else
		{
			Log.d(TAG, valuesSplit.toString());
			db.insertOrThrow("kmmSplits", null, valuesSplit);
		}
		
		return true;
	}
	

	public void dump()
	{
		Log.d(TAG, "transactionId: " + transactionId);
		Log.d(TAG, "txType: " + txType);
		Log.d(TAG, "splitId: " + splitId);
		Log.d(TAG, "payeeId: " + payeeId);
		Log.d(TAG, "reconcileDate: " + reconcileDate);
		Log.d(TAG, "value: " + value);
		Log.d(TAG, "valueFormatted: " + valueFormatted);
		Log.d(TAG, "shares: " + shares);
		Log.d(TAG, "sharesFormatted: " + sharesFormatted);
		Log.d(TAG, "price: " + price);
		Log.d(TAG, "priceFormatted: " + priceFormatted);
		Log.d(TAG, "memo: " + memo);
		Log.d(TAG, "accountId: " + accountId);
		Log.d(TAG, "checkNumber: " + checkNumber);
		Log.d(TAG, "postDate: " + postDate);
		Log.d(TAG, "bankId: " + bankId);
	}
	
	public String getCheckNumber()
	{
		return checkNumber;
	}
	
	public String getPayeeId()
	{
		return payeeId;
	}
	
	public String getAccountId()
	{
		return accountId;
	}
	
	public String getReconcileFlag()
	{
		return reconcileFlag;
	}
	
	public String getValueFormatted()
	{
		return valueFormatted;
	}
	
	public String getMemo()
	{
		return memo;
	}
}
