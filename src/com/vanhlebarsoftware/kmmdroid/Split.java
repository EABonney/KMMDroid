package com.vanhlebarsoftware.kmmdroid;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

public class Split
{
	/****** Contants for Split columns supplied via a Cursor *****/
	private static int C_TRANSACTIONID = 0;
	private static int C_TXTYPE = 1;
	private static int C_SPLITID = 2;
	private static int C_PAYEEID = 3;
	private static int C_RECONCILEDATE = 4;
	private static int C_ACTION = 5;
	private static int C_RECONCILEFLAG = 6;
	private static int C_VALUE = 7;
	private static int C_VALUEFORMATTED = 8;
	private static int C_SHARES = 9;
	private static int C_SHARESFORMATTED = 10;
	private static int C_PRICE = 11;
	private static int C_PRICEFORMATTED = 12;
	private static int C_MEMO = 13;
	private static int C_ACCOUNTID = 14;
	private static int C_CHECKNUMBER = 15;
	private static int C_POSTDATE = 16;
	private static int C_BANKID = 17;
	
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
		this.splitId = sId;			this.payeeId = pId;
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
	
	// Create a split from a provided Cursor that has queried the database for all columns.
	Split( Cursor curSplit, int element )
	{
		// Move to the correct cursor element passed to us.
		curSplit.moveToPosition(element);

		this.transactionId = curSplit.getString(C_TRANSACTIONID);
		this.txType = curSplit.getString(C_TXTYPE);
		this.splitId = curSplit.getInt(C_SPLITID);
		this.payeeId = curSplit.getString(C_PAYEEID);
		this.reconcileDate = curSplit.getString(C_RECONCILEDATE);
		this.action = curSplit.getString(C_ACTION);
		this.reconcileFlag = curSplit.getString(C_RECONCILEFLAG);
		this.value = curSplit.getString(C_VALUE);
		this.valueFormatted = curSplit.getString(C_VALUEFORMATTED);
		this.shares = curSplit.getString(C_SHARES);
		this.sharesFormatted = curSplit.getString(C_SHARESFORMATTED);
		this.price = curSplit.getString(C_PRICE);
		this.priceFormatted = curSplit.getString(C_PRICEFORMATTED);
		this.memo = curSplit.getString(C_MEMO);
		this.accountId = curSplit.getString(C_ACCOUNTID);
		this.checkNumber = curSplit.isNull(C_CHECKNUMBER) ? "" : curSplit.getString(C_CHECKNUMBER);
		this.postDate = curSplit.getString(C_POSTDATE);
		this.bankId = curSplit.getString(C_BANKID);
	}
	
	public boolean commitSplit(boolean updating, Context context)
	{
		Log.d(TAG, "transactionId: " + this.transactionId);
		// create the ContentValue pairs
		ContentValues valuesSplit = new ContentValues();
		valuesSplit.put("transactionId", this.transactionId);
		valuesSplit.put("txType", this.txType);
		valuesSplit.put("splitId", this.splitId);
		valuesSplit.put("payeeId", this.payeeId);
		valuesSplit.put("reconcileDate", this.reconcileDate);
		valuesSplit.put("action", this.action);
		valuesSplit.put("reconcileFlag", this.reconcileFlag);
		valuesSplit.put("value", this.value);
		valuesSplit.put("valueFormatted", this.valueFormatted);
		valuesSplit.put("shares", this.shares);
		valuesSplit.put("sharesFormatted", this.sharesFormatted);
		valuesSplit.put("price", this.price);
		valuesSplit.put("priceFormatted", this.priceFormatted);
		valuesSplit.put("memo", this.memo);
		valuesSplit.put("accountId", this.accountId);
		valuesSplit.put("checkNumber", this.checkNumber);
		valuesSplit.put("postDate", this.postDate);
		valuesSplit.put("bankId", this.bankId);
		
		String frag = "#9999";
		Uri u = Uri.withAppendedPath(KMMDProvider.CONTENT_SPLIT_URI, frag);
		u = Uri.parse(u.toString());
		
		if( updating )
		{
			int result = context.getContentResolver().update(u, valuesSplit, "transactionId=? AND splitId=?", 
															 new String[] { this.transactionId, String.valueOf(this.splitId) });
		}
		else
		{
			context.getContentResolver().insert(u, valuesSplit);
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
		return this.checkNumber;
	}
	
	public String getPayeeId()
	{
		return this.payeeId;
	}
	
	public String getAccountId()
	{
		return this.accountId;
	}
	
	public String getReconcileFlag()
	{
		return this.reconcileFlag;
	}
	
	public String getValueFormatted()
	{
		return this.valueFormatted;
	}
	
	public String getMemo()
	{
		return this.memo;
	}
	
	public void setTransactionId(String id)
	{
		this.transactionId = id;
	}
	
	public String getTransactionId()
	{
		return this.transactionId;
	}
	
	public int getSplitId()
	{
		return this.splitId;
	}
	
	public void setPostDate(String date)
	{
		this.postDate = date;
	}
	
	public String getPostDate()
	{
		return this.postDate;
	}
	
	public void setTxType(String type)
	{
		this.txType = type;
	}
	
	public String getTxType()
	{
		return this.txType;
	}
	
	public String getReconcileDate()
	{
		return this.reconcileDate;
	}
	
	public String getValue()
	{
		return this.value;
	}
	
	public String getShares()
	{
		return this.shares;
	}
	
	public String getSharesFormatted()
	{
		return this.sharesFormatted;
	}
	
	public String getPrice()
	{
		return this.price;
	}
	
	public String getPriceFormatted()
	{
		return this.priceFormatted;
	}
	
	public String getBankId()
	{
		return this.bankId;
	}
	
	public String getAction()
	{
		return this.action;
	}
}
