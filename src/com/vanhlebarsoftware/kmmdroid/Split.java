package com.vanhlebarsoftware.kmmdroid;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class Split implements Parcelable
{
	private static final String TAG = Split.class.getSimpleName();
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
	
	private String fromWidgetId;
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
	private Context context;
	
	// Constructor used for creating new Splits.
	Split(Context c)
	{
		this.transactionId = null;
		this.txType = null;
		this.splitId = 0;			
		this.payeeId = null;
		this.reconcileDate = null;
		this.action = null;
		this.reconcileFlag = null;
		this.value = null;
		this.valueFormatted = null;
		this.shares = null;
		this.sharesFormatted = null;
		this.price = null;
		this.priceFormatted = null;
		this.memo = null;
		this.accountId = null;
		this.checkNumber = null;
		this.postDate = null;
		this.bankId = null;
		this.fromWidgetId = null;
		this.context = c;
	}
	
	Split(String tId, String tType, int sId, String pId, String rDate, String a, String rFlag,
			String v, String vFormatted, String s, String sFormatted, String p, String pFormatted,
			String m, String aId, String ckNumber, String pDate, String bId, String wId, Context cont)
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
		this.fromWidgetId = wId;
		this.context = cont;
	}
	
	// Create a split from a provided Cursor that has queried the database for all columns.
	Split( Cursor curSplit, int element, String wId, Context cont )
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
		this.fromWidgetId = wId;
		this.context = cont;
	}
	
	public boolean commitSplit(boolean updating)
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
			int result = this.context.getContentResolver().update(u, valuesSplit, "transactionId=? AND splitId=?", 
															 new String[] { this.transactionId, String.valueOf(this.splitId) });
		}
		else
		{
			this.context.getContentResolver().insert(u, valuesSplit);
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
	
	public void setCheckNumber(String ckNum)
	{
		this.checkNumber = ckNum;
	}
	
	public String getCheckNumber()
	{
		return this.checkNumber;
	}
	
	public void setPayeeId(String pId)
	{
		this.payeeId = pId;
	}
	
	public String getPayeeId()
	{
		return this.payeeId;
	}
	
	public void setAccountId(String id)
	{
		this.accountId = id;
	}
	
	public String getAccountId()
	{
		return this.accountId;
	}
	
	public void setReconcileFlag(String flag)
	{
		this.reconcileFlag = flag;
	}
	
	public String getReconcileFlag()
	{
		return this.reconcileFlag;
	}

	public void setValueFormatted(String vf)
	{
		this.valueFormatted = vf;
	}
	
	public String getValueFormatted()
	{
		return this.valueFormatted;
	}
	
	public void setMemo(String m)
	{
		this.memo = m;
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
	
	public void setSplitId(String sId)
	{
		this.splitId = Integer.valueOf(sId);
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
	
	public void setReconcileDate(String date)
	{
		this.reconcileDate = date;
	}
	
	public String getReconcileDate()
	{
		return this.reconcileDate;
	}
	
	public void setValue(String v)
	{
		this.value = v;
	}
	
	public String getValue()
	{
		return this.value;
	}
	
	public void setShares(String s)
	{
		this.shares = s;
	}
	
	public String getShares()
	{
		return this.shares;
	}
	
	public void setSharesFormatted(String sf)
	{
		this.sharesFormatted = sf;
	}
	
	public String getSharesFormatted()
	{
		return this.sharesFormatted;
	}
	
	public void setPrice(String p)
	{
		this.price = p;
	}
	
	public String getPrice()
	{
		return this.price;
	}
	
	public void setPriceFormatted(String pf)
	{
		this.priceFormatted = pf;
	}
	
	public String getPriceFormatted()
	{
		return this.priceFormatted;
	}
	
	public void setBankId(String bId)
	{
		this.bankId = bId;
	}
	
	public String getBankId()
	{
		return this.bankId;
	}
	
	public void setAction(String a)
	{
		this.action = a;
	}
	
	public String getAction()
	{
		return this.action;
	}
	
	public void setContext(Context c)
	{
		this.context = c;
	}
	
	public String getPayeeName()
	{
		Uri u = Uri.withAppendedPath(KMMDProvider.CONTENT_PAYEE_URI,this.payeeId + "#" + this.fromWidgetId);
		u = Uri.parse(u.toString());
		Cursor payee = this.context.getContentResolver().query(u, null, null, null, null);
		if( payee != null )
			payee.moveToFirst();
		else
			return null;
		
		return payee.getString(payee.getColumnIndex("name"));
	}
	
	public Split copy()
	{
		Split tmp = new Split(this.transactionId, this.txType, this.splitId, this.payeeId, this.reconcileDate, this.action,
							  this.reconcileFlag, this.value, this.valueFormatted, this.shares, this.sharesFormatted,
							  this.price, this.priceFormatted, this.memo, this.accountId, this.checkNumber, this.postDate,
							  this.bankId, null, null);
		
		return tmp;
	}

	/***********************************************************************************************
	 * Required methods to make Splits parcelable to pass between activities
	 * 
	 * Any time we are using this parcel to get Splits we MUST use the setContext() method to set the
	 * context of the actual Split as we can not pass this as part of the Parcel. Failing to do this
	 * will cause context to be null and crash!
	 **********************************************************************************************/
	public int describeContents() 
	{
		return 0;
	}

	public void writeToParcel(Parcel dest, int flags) 
	{
		// TODO Auto-generated method stub
		dest.writeString(fromWidgetId);
		dest.writeString(transactionId);
		dest.writeString(txType);
		dest.writeInt(splitId);
		dest.writeString(payeeId);
		dest.writeString(reconcileDate);
		dest.writeString(action);
		dest.writeString(reconcileFlag);
		dest.writeString(value);
		dest.writeString(valueFormatted);
		dest.writeString(shares);
		dest.writeString(sharesFormatted);
		dest.writeString(price);
		dest.writeString(priceFormatted);
		dest.writeString(memo);
		dest.writeString(accountId);
		dest.writeString(checkNumber);
		dest.writeString(postDate);
		dest.writeString(bankId);
	}
}
