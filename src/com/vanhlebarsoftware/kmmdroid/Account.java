package com.vanhlebarsoftware.kmmdroid;

import java.math.BigInteger;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.StringTokenizer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.app.Fragment;
import android.util.Log;

public class Account implements Parcelable
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
	private String institutionId;
	private String accountNumber;
	private String IBAN;
	private String openDate;
	private String currencyId;
	private String notes;
	private int accountType;
	private int transactionCount;
	private boolean isParent;
	private boolean isClosed;
	private boolean isPreferred;
	private List<Price> prices;
	private Context context;
	

	Account(Context c)
	{		
		this.id = null;
		this.parentId = null;
		this.accountName = null;
		this.balance = null;
		this.accountTypeString = null;
		this.accountType = 0;
		this.isParent = false;
		this.isClosed = false;
		this.institutionId = null;
		this.accountNumber = null;
		this.IBAN = null;
		this.openDate = null;
		this.currencyId = null;
		this.isPreferred = false;
		this.transactionCount = 0;
		this.notes = null;
		this.context = c;
		this.prices = new ArrayList<Price>();
	}
	
	Account(String id, String name, String bal, String acctTypeStr, int acctType, boolean isP, Context c)
	{
		this.id = id;
		this.parentId = null;
		this.accountName = name;
		this.balance = bal;
		this.accountTypeString = acctTypeStr;
		this.accountType = acctType;
		this.isParent = isP;
		this.isClosed = false;
		this.institutionId = null;
		this.accountNumber = null;
		this.IBAN = null;
		this.openDate = null;
		this.currencyId = null;
		this.isPreferred = false;
		this.transactionCount = 0;
		this.notes = null;
		this.context = c;
		this.prices = new ArrayList<Price>();
	}
	
	Account(Cursor cur, Context c)
	{
		this.context = c;
		int index = cur.getColumnIndex("_id");
		if( index == -1 )
			this.id = cur.getString(cur.getColumnIndex("id"));
		else
			this.id = cur.getString(cur.getColumnIndex("_id"));
		
		this.parentId = cur.getString(cur.getColumnIndex("parentId"));
		this.accountName = cur.getString(cur.getColumnIndex("accountName"));
		this.balance = cur.getString(cur.getColumnIndex("balance"));
		
		index = cur.getColumnIndex("institutionId");
		if( index == -1 )
			this.institutionId = null;
		else
			this.institutionId = cur.getString(cur.getColumnIndex("institutionId"));
		
		index = cur.getColumnIndex("openingDate");
		if( index == -1 )
			this.openDate = null;
		else
			this.openDate = cur.getString(cur.getColumnIndex("openingDate"));
		
		index = cur.getColumnIndex("accountNumber");
		if( index == -1 )
			this.accountNumber = null;
		else
			this.accountNumber = cur.getString(cur.getColumnIndex("accountNumber"));
		
		index = cur.getColumnIndex("accountType");
		if( index == -1 )
			this.accountType = 0;
		else
			this.accountType = cur.getInt(cur.getColumnIndex("accountType"));

		index = cur.getColumnIndex("accountTypeString");
		if( index == -1 )
			this.accountTypeString = null;
		else
			this.accountTypeString = cur.getString(cur.getColumnIndex("accountTypeString"));
		
		index = cur.getColumnIndex("currencyId");
		if( index == -1 )		
			this.currencyId = null;
		else
			this.currencyId = cur.getString(cur.getColumnIndex("currencyId"));
		
		index = cur.getColumnIndex("transactionCount");
		if( index == -1 )			
			this.transactionCount = 0;
		else
			this.transactionCount = cur.getInt(cur.getColumnIndex("transactionCount"));	
		
		index = cur.getColumnIndex("description");
		if( index == -1 )
			this.notes = null;
		else
			this.notes = cur.getString(cur.getColumnIndex("description"));
		
		// Get our KVPs for this account.
		String frag = "#9999";
		Uri u = Uri.withAppendedPath(KMMDProvider.CONTENT_KVP_URI, frag);
		u = Uri.parse(u.toString());
		Cursor kvp = this.context.getContentResolver().query(u, new String[] { "*" }, "kvpType='ACCOUNT' AND kvpId=?", new String[] { this.id }, null);
		
		this.isClosed = getKVPIsClosed(kvp);
		this.IBAN = getKVPIBAN(kvp);		
		this.isPreferred = getKVPIsPreferred(kvp);
		kvp.close();
		
		this.isParent = false;	
		
		this.prices = new ArrayList<Price>();
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
	
	public boolean getIsClosed()
	{
		return this.isClosed;
	}
	
	public String getInstitutionId()
	{
		return this.institutionId;
	}
	
	public String getAccountNumber()
	{
		return this.accountNumber;
	}
	
	public String getIBAN()
	{
		return this.IBAN;
	}
	
	public String getOpenDate()
	{
		return this.openDate;
	}
	
	public String getCurrencyId()
	{
		return this.currencyId;
	}
	
	public boolean getIsPreferred()
	{
		return this.isPreferred;
	}
	
	public int getTransactionCount()
	{
		return this.transactionCount;
	}
	
	public String getNotes()
	{
		return this.notes;
	}
	
	public ArrayList<Account> getChildren()
	{
		ArrayList<Account> children = new ArrayList<Account>();
		String frag = "#9999";
		Uri u = Uri.withAppendedPath(KMMDProvider.CONTENT_ACCOUNT_URI, id + frag);
		u = Uri.parse(u.toString());
		Cursor c = context.getContentResolver().query(u, new String[] {"id"}, "parentId=?", new String[] { id }, null);
		
		if(c.getCount() > 0)
		{
			for(int i=0; i < c.getCount(); i++)
			{
				c.moveToPosition(i);
				children.add(new Account(c, this.context));
			}
		}
		else
			return null;
		
		return children;
	}
	
	public String getStockValue()
	{
		Long prValue = Account.convertBalance(this.prices.get(0).getPrice());
		Long prNumShares = Transaction.convertToPennies(this.getBalance());
		return Transaction.convertToDollars((prValue * prNumShares), true, true);
	}
	
	public String getStockCost()
	{
		// Get all the splits for this account.
		String[] projection = { "value" };
		String selection = "accountId=?";
		String[] selArgs = { this.id };
		String frag = "#9999";
		Uri u = Uri.withAppendedPath(KMMDProvider.CONTENT_SPLIT_URI, frag);
		u = Uri.parse(u.toString());
		Cursor splits = this.context.getContentResolver().query(u, projection, selection, selArgs, null);
		
		Long cost = Long.valueOf(0);
		for(int i=0; i<splits.getCount(); i++)
		{
			splits.moveToPosition(i);
			Log.d(TAG, "cost: " + cost);
			cost = cost + Account.convertBalance(splits.getString(splits.getColumnIndexOrThrow("value")));
		}
		
		splits.close();
		Log.d(TAG, "cost before convertToDollars: " + cost);
		return Transaction.convertToDollars(cost, true, true);
	}
	
	public void setAccountName(String strName)
	{
		this.accountName = strName;
	}
	
	public void setIsParent(boolean flag)
	{
		this.isParent = flag;
	}
	
	public void setIsClosed(boolean flag)
	{
		this.isClosed = flag;
	}
	
	public void setIBAN(String iban)
	{
		this.IBAN = iban;
	}
	
	public void setIsPreferred(boolean flag)
	{
		this.isPreferred = flag;
	}
	
	public void setOpenDate(String date)
	{
		this.openDate = date;
	}
	
	public void setOpenBalance(String bal)
	{
		this.balance = bal;
	}
	
	public void setId(String id)
	{
		this.id = id;
	}
	
	public void setParentId(String pId)
	{
		this.parentId = pId;
	}
	
	public void setCurrency(String strCurrency)
	{
		this.currencyId = strCurrency;
	}
	
	public void setAccountType(String strType)
	{
		this.accountTypeString = strType;
	}
	
	public void setAccountType(int nType)
	{
		this.accountType = nType;
	}
	
	public void setInstitutionId(String instId)
	{
		this.institutionId = instId;
	}
	
	public void setAccountNumber(String strAcctNumber)
	{
		this.accountNumber = strAcctNumber;
	}
	
	public void setNotes(String strNote)
	{
		this.notes = strNote;
	}
	
	public void setPrices(Cursor p)
	{
		for(int i=0; i<p.getCount(); i++)
		{
			this.prices.add(new Price(p, i, null, this.context));
		}
	}
	
	static public void updateAccount(Context context, String accountId, String transValue, int nChange)
	{
		String frag = "#9999";
		Uri u = Uri.withAppendedPath(KMMDProvider.CONTENT_ACCOUNT_URI,accountId + frag);
		u = Uri.parse(u.toString());
		Cursor c = context.getContentResolver().query(u, new String[] { "balance", "transactionCount" }, null, null, null);
		c.moveToFirst();
		
		// Update the current balance for this account.
		long balance = Account.convertBalance(c.getString(0));
		
		// If we are editing a transaction we need to reverse the original transaction values, this takes care of that for us.
		long tValue = Transaction.convertToPennies(transValue) * nChange;
		
		long newBalance = balance + tValue;

		// Update the number of transactions used for this account.
		int Count = c.getInt(1) + nChange;
		
		ContentValues values = new ContentValues();
		values.put("balanceFormatted", Transaction.convertToDollars(newBalance, false, false));
		values.put("balance", createBalance(newBalance));
		values.put("transactionCount", Count);
		
		context.getContentResolver().update(u, values, "id=?", new String[] { accountId });
		
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
		
		newBalanceFormatted = Transaction.convertToDollars(balance, true, false);
		
		return newBalanceFormatted;
	}

	static public String adjustForFutureTransactions(String accountId, Long balance, Cursor splits)
	{
		String newBalanceFormatted = null;

		while(splits.moveToNext())
		{
			// Since our current balance includes the future transactions we need to do the opposite of the transaction to correct the balance
			if( splits.getString(0) != null )
				balance = balance - Transaction.convertToPennies(splits.getString(0));
		}
		
		newBalanceFormatted = Transaction.convertToDollars(balance, true, false);
		
		return newBalanceFormatted;
	}
	
	private boolean getKVPIsClosed(Cursor cur)
	{
		cur.moveToFirst();
		while( !cur.isAfterLast() )
		{
			if( cur.getString(cur.getColumnIndex("kvpKey")).equals("mm-closed") )
				return true;
			cur.moveToNext();
		}
		return false;
	}

	private String getKVPIBAN(Cursor cur)
	{
		cur.moveToFirst();
		while( !cur.isAfterLast() )
		{
			if( cur.getString(cur.getColumnIndex("kvpKey")).equals("IBAN") )
				return cur.getString(cur.getColumnIndex("kvpData"));
			cur.moveToNext();
		}
		return null;
	}
	
	private boolean getKVPIsPreferred(Cursor cur)
	{
		cur.moveToFirst();
		while( !cur.isAfterLast() )
		{
			if( cur.getString(cur.getColumnIndex("kvpKey")).equals("PreferredAccount") )
				return true;
			cur.moveToNext();
		}
		return false;		
	}
	
/*	public void getDataChanges(CreateModifyAccountActivity context)
	{
		// We need to get each tab (fragment) if it is null, then the user didn't make any changes on that tab and ignore.
		// If the tab (fragment) is not null, then we need to pull the information into our object before we save it.
		
		// Get the Institution elements
		Fragment accountInst = context.getFragmentManager().findFragmentByTag("institution");
		if( accountInst != null )
		{
			boolean useInst = ((CreateAccountInstitutionActivity) accountInst).getUseInstitution();
			if(useInst)
				this.institutionId = ((CreateAccountInstitutionActivity) accountInst).getInstitutionId();
			else
				this.institutionId = "";
			
			this.accountNumber = ((CreateAccountInstitutionActivity) accountInst).getAccountNumber();
			this.IBAN = ((CreateAccountInstitutionActivity) accountInst).getIBAN();
		}
		
		// Get the general Account elements
		Fragment accountAcct = context.getSupportFragmentManager().findFragmentByTag("account");
		if( accountAcct != null )
		{
			this.accountName = ((CreateAccountAccountActivity) accountAcct).getAccountName();
			this.accountType = ((CreateAccountAccountActivity) accountAcct).getAccountType();
			this.accountTypeString = ((CreateAccountAccountActivity) accountAcct).getAccountTypeString();
			this.currencyId = ((CreateAccountAccountActivity) accountAcct).getCurrency();
			this.openDate = ((CreateAccountAccountActivity) accountAcct).getOpeningDate();
			this.balance = ((CreateAccountAccountActivity) accountAcct).getOpeningBalance();
			this.isPreferred = ((CreateAccountAccountActivity) accountAcct).getPreferredAccount();
		}
		
		// Get the Parent account
		Fragment accountParent = context.getSupportFragmentManager().findFragmentByTag("parent");
		if( accountParent != null )
			this.parentId = ((CreateAccountParentActivity) accountParent).getParentId();
	}
	
	public void getDataChanges(CreateModifyCategoriesActivity context)
	{
		// We need to get each tab (fragment) if it is null, then the user didn't make any changes on that tab and ignore.
		// If the tab (fragment) is not null, then we need to pull the information into our object before we save it.
		
		// Get the General data elements.
		Fragment general = context.getSupportFragmentManager().findFragmentByTag("general");
		if( general != null )
		{
			this.accountName = ((CategoriesGeneralActivity) general).getCategoryName();
			int type = context.getCategoryType();
			switch(type)
			{
			case Account.ACCOUNT_INCOME:
				this.accountType = type;
				this.accountTypeString = context.getString(R.string.Income);
				break;
			case Account.ACCOUNT_EXPENSE:
				this.accountType = type;
				this.accountTypeString = context.getString(R.string.Expense);
				break;
			}
			
			this.currencyId = ((CategoriesGeneralActivity) general).getCurrency();
			this.notes = ((CategoriesGeneralActivity) general).getNotes();
		}
		
		// Get the Hierarchy data elements.
		Fragment hierarchy = context.getSupportFragmentManager().findFragmentByTag("hierarchy");
		if( hierarchy != null )
		{
			if( context.getIsParentInvalid() )
				this.parentId = this.getParentId();
			else
				this.parentId = ((CategoriesHierarchyActivity) hierarchy).getParentAccount();
		}
		
		// If general is NOT null AND hierarchy IS null, then we need to see if the user changed the account type
		// but didn't go into the hierarchy tab, if so then update the parentId accordingly.
		if( general != null && hierarchy == null )
		{
			if( context.getIsParentInvalid() )
				this.parentId = this.getParentId();
		}
	}
*/	
	public void SaveAccount(Context context)
	{
		// Create the ContentValue pairs and then insert the new account.
		ContentValues valuesAccount = new ContentValues();	
		
		valuesAccount.put("id", getId());
		valuesAccount.put("institutionId", getInstitutionId());
		valuesAccount.put("parentId", getParentId());
		valuesAccount.put("openingDate", getOpenDate());
		valuesAccount.put("accountNumber", getAccountNumber());
		valuesAccount.put("accountType", getAccountType());
		valuesAccount.put("accountTypeString", getAccountTypeString());
		valuesAccount.put("accountName", getName());
		valuesAccount.put("description", "");
		valuesAccount.put("currencyId", getCurrencyId());
		
		valuesAccount.put("transactionCount", getTransactionCount());
		valuesAccount.put("isStockAccount", "N");
		valuesAccount.put("lastReconciled", "");
		valuesAccount.put("lastModified", "");
		// We initially set the balance and balanceFormatted to zero because they are updated to the actual amounts
		// when the splits are posted later on in createTransaction().
		valuesAccount.put("balance", "0/1");
		valuesAccount.put("balanceFormatted", "0.00");
		
		try 
		{
			String frag = "#9999";
			Uri u = Uri.withAppendedPath(KMMDProvider.CONTENT_ACCOUNT_URI, frag);
			u = Uri.parse(u.toString());
			context.getContentResolver().insert(u,valuesAccount);
			frag = "#9999";
			u = Uri.withAppendedPath(KMMDProvider.CONTENT_FILEINFO_URI, frag);
			u = Uri.parse(u.toString());
			context.getContentResolver().update(u, null, "hiAccountId", new String[] { "1" });
			context.getContentResolver().update(u, null, "accounts", new String[] { "1" });
		} 
		catch (SQLException e)
		{
			Log.d(TAG, "error: " + e.getMessage());
		}

		// We need to put the additional information in the kmmKeyValuePairs table for this account
		// and create the opening balance.
		// Only for Accounts NOT Categories
		if( getAccountType() != ACCOUNT_EXPENSE || getAccountType() != ACCOUNT_INCOME )
		{
			String kvpType = "ACCOUNT";
			String kvpId = getId();
			ContentValues valuesKVP = new ContentValues();
			valuesKVP.put("kvpType", kvpType);
			valuesKVP.put("kvpId", kvpId);
			valuesKVP.put("kvpKey", "IBAN");
			valuesKVP.put("kvpData", getIBAN());
			String frag = "#9999";
			Uri u = Uri.withAppendedPath(KMMDProvider.CONTENT_KVP_URI, frag);
			u = Uri.parse(u.toString());
			context.getContentResolver().insert(u, valuesKVP);
			valuesKVP.clear();
			if( getIsPreferred() )
			{
				valuesKVP.put("kvpType", kvpType);
				valuesKVP.put("kvpId", kvpId);
				valuesKVP.put("kvpKey", "PreferredAccount");
				valuesKVP.put("kvpData", "Yes");
				context.getContentResolver().insert(u, valuesKVP);
			}
			// Create the transaction and splits for any opening balance that was entered by the user.
			createTransaction(getOpenDate(), getCurrencyId(), getBalance(), getId(), context);
		}
	}
	
	public void UpdateAccount(Context context)
	{
		// Create the ContentValue pairs and then insert the new account.
		ContentValues valuesAccount = new ContentValues();
		
		valuesAccount.put("id", getId());
		valuesAccount.put("institutionId", getInstitutionId());
		valuesAccount.put("parentId", getParentId());
		valuesAccount.put("openingDate", getOpenDate());
		valuesAccount.put("accountNumber", getAccountNumber());
		valuesAccount.put("accountType", getAccountType());
		Log.d(TAG, "accountTypeString: " + getAccountTypeString());
		valuesAccount.put("accountTypeString", getAccountTypeString());
		valuesAccount.put("accountName", getName());
		valuesAccount.put("description", "");
		valuesAccount.put("currencyId", getCurrencyId());
		
		// First we need to get the old account balance and make any necessary adjustments to this balance based on
		// the changes the user may have made to our Opening balance amount for this account.
		// Now we need to adjust any Splits/Transactions we originally had for this account.
		// Only for Accounts NOT Category
		if( getAccountType() != ACCOUNT_EXPENSE && getAccountType() != ACCOUNT_INCOME )
			adjustOpenTrans(getId(), getBalance(), getOpenDate(), getCurrencyId(), context);
		
		// Actually update the account now.
		String frag = "#9999";
		Uri u = Uri.withAppendedPath(KMMDProvider.CONTENT_ACCOUNT_URI, getId() + frag);
		u = Uri.parse(u.toString());
		context.getContentResolver().update(u, valuesAccount, null, null);
		
		// We need to put the additional information in the kmmKeyValuePairs table for this account.
		// Only for Accounts NOT Category
		if( getAccountType() != ACCOUNT_EXPENSE && getAccountType() != ACCOUNT_INCOME )
		{
			String kvpType = "ACCOUNT";
			String kvpId = getId();
			ContentValues valuesKVP = new ContentValues();
			valuesKVP.put("kvpType", kvpType);
			valuesKVP.put("kvpId", kvpId);
			valuesKVP.put("kvpKey", "IBAN");
			valuesKVP.put("kvpData", getIBAN());
			frag = "#9999";
			u = Uri.withAppendedPath(KMMDProvider.CONTENT_KVP_URI, frag);
			u = Uri.parse(u.toString());
			context.getContentResolver().update(u, valuesKVP, "kvpId=? AND kvpType=? AND kvpKey=?", 
													 new String[] { getId(), kvpType, "IBAN" });
			valuesKVP.clear();
			// See if the account was previously setup as a preferred account.
			Cursor c = context.getContentResolver().query(u, new String[] { "kvpData" },
					"kvpId=? AND kvpType='ACCOUNT' AND kvpKey='PreferredAccount'", new String[] { getId() }, null);
			if( getIsPreferred() )
			{
				valuesKVP.put("kvpType", kvpType);
				valuesKVP.put("kvpId", kvpId);
				valuesKVP.put("kvpKey", "PreferredAccount");
				valuesKVP.put("kvpData", "Yes");
				if( c.getCount() > 0)
					context.getContentResolver().update(u, valuesKVP, "kvpId=? AND kvpType=? AND kvpKey=?",
														 new String[] { getId(), kvpType, "PreferredAccount" } );
				else
					context.getContentResolver().insert(u, valuesKVP);
			}
			else
			{
				if(c.getCount() > 0)
					context.getContentResolver().delete(u, "kvpId=? AND kvpType='ACCOUNT' AND kvpKey='PreferredAccount'",
															 new String[] { getId() });
			}
			c.close();
		}
	}
	
	private void createTransaction(String openDate, String baseCurId, String openBal, String acctId, Context context)
	{
		// Run a query to get the accountId for "Opening Balances".
		String frag = "#9999";
		Uri u = Uri.withAppendedPath(KMMDProvider.CONTENT_ACCOUNT_URI, frag);
		u = Uri.parse(u.toString());
		String Columns[] = { "id" };
		String Selection = "accountName='Opening Balances'";
		Cursor openBalanceCursor = context.getContentResolver().query(u, Columns, Selection, null, null);
		openBalanceCursor.moveToFirst();
		String OpeningBalancesId = openBalanceCursor.getString(openBalanceCursor.getColumnIndex("id"));
		openBalanceCursor.close();
		
		// create the ContentValue pairs
		ContentValues valuesTrans = new ContentValues();
		
		valuesTrans.put("txType", "N");
		valuesTrans.put("postDate", openDate);
		valuesTrans.put("memo", "");
		
        // get the current date
        final Calendar c = Calendar.getInstance();
        valuesTrans.put("entryDate", new StringBuilder()
			// Month is 0 based so add 1
			.append(c.get(Calendar.YEAR)).append("-")
			.append(c.get(Calendar.MONTH) + 1).append("-")
			.append(c.get(Calendar.DAY_OF_MONTH)).toString());
        
		valuesTrans.put("currencyId", baseCurId);
		valuesTrans.put("bankId", "");
		// Need to create the transaction id.
		String id = createTransactionId(context);
		valuesTrans.put("id", id);
		
		// Enter the transaction into the kmmTransactions table.
		frag = "#9999";
		u = Uri.withAppendedPath(KMMDProvider.CONTENT_TRANSACTION_URI, frag);
		u = Uri.parse(u.toString());
		context.getContentResolver().insert(u, valuesTrans);
		
		// We need to take our editAmount string which "may" contain a '.' as the decimal and replace it with the localized seperator.
		DecimalFormat decimal = new DecimalFormat();
		char decChar = decimal.getDecimalFormatSymbols().getDecimalSeparator();
		String strAmount = openBal.replace('.', decChar);
		
		// Create the splits information to be saved.
		// Take the amount entered as is, use it for the account's balance and then use the negative of that amount as the offset to
		// Open Balances.
		ArrayList<Split> splits = new ArrayList<Split>();
		String value = null, formatted = null;
		value = Account.createBalance(Transaction.convertToPennies(openBal));
		formatted = Transaction.convertToDollars(Account.convertBalance(value), false, false);
		splits.add(new Split(id, "N", 0, "", "", "", "0", value, formatted, value, formatted,
				 "", "", "", acctId, "", openDate, "", "9999", this.context));
		value = Account.createBalance(Transaction.convertToPennies(openBal) * -1);
		formatted = Transaction.convertToDollars(Account.convertBalance(value), false, false);
		splits.add(new Split(id, "N", 1, "", "", "", "0", value, formatted, value, formatted,
				 "", "", "", OpeningBalancesId, "", openDate, "", "9999", this.context));		
		
		// Actually enter the transaction and splits into the database, update the fileInfo table and the account with the # of transactions.
		frag = "#9999";
		u = Uri.withAppendedPath(KMMDProvider.CONTENT_FILEINFO_URI, frag);
		u = Uri.parse(u.toString());
		context.getContentResolver().update(u, null, "hiTransactionId", new String[] { "1" });
		context.getContentResolver().update(u, null, "transactions", new String[] { "1" });
		context.getContentResolver().update(u, null, "splits", new String[] { String.valueOf(splits.size()) });

		for(int i=0; i < splits.size(); i++)
		{
			Split s = splits.get(i);
			s.commitSplit(false);
			Account.updateAccount(context, s.getAccountId(), s.getValueFormatted(), 1);
		}
		context.getContentResolver().update(u, null, "lastModified", new String[] { "0" });
	}
	
	private String createTransactionId(Context context)
	{
		final String[] dbColumns = { "hiTransactionId"};
		// Run a query to get the Transaction ids so we can create a new one.
		String frag = "#9999";
		Uri u = Uri.withAppendedPath(KMMDProvider.CONTENT_FILEINFO_URI, frag);
		u = Uri.parse(u.toString());
		Cursor cursor = context.getContentResolver().query(u, dbColumns, null, null, null);
		
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
		
		// close our cursor
		cursor.close();
		
		return newId;
	}
	
	private void adjustOpenTrans(String acctId, String openBalance, String openDate, String baseCurId, Context context)
	{
		// Run a query to get the accountId for "Opening Balances".
		String frag = "#9999";
		Uri u = Uri.withAppendedPath(KMMDProvider.CONTENT_ACCOUNT_URI, frag);
		u = Uri.parse(u.toString());
		String Columns[] = { "id" };
		String Selection = "accountName='Opening Balances'";
		Cursor openBalanceCursor = context.getContentResolver().query(u, Columns, Selection, null, null);
		openBalanceCursor.moveToFirst();
		String OpeningBalancesId = openBalanceCursor.getString(openBalanceCursor.getColumnIndex("id"));
		// clean up our cursor.
		openBalanceCursor.close();
		long longOpenBal = Transaction.convertToPennies(openBalance);
		// First let's see if this account even has an opening balance transaction to begin with.
		frag = "#9999";
		u = Uri.withAppendedPath(KMMDProvider.CONTENT_SPLIT_URI, frag);
		u = Uri.parse(u.toString());
		Cursor splits = context.getContentResolver().query(u, new String[] { "*" }, "accountId=? AND splitId='0' AND "
												+ "(payeeId IS NULL OR payeeId='')", new String[] { acctId }, null);

		// If query returns null then we didn't have any previous opening balance transactions.
		// If we have an opening balance entry now, we need to create the transaction and the splits.
		if( splits.getCount() == 0 )
		{
			Log.d(TAG, "We didn't have any previous opening balance, so we are creating the transaction now.");
			if( longOpenBal != 0 )
				createTransaction(openDate, baseCurId, openBalance, acctId, context);
		}
		else if( splits.getCount() > 0 )
		{
			frag = "#9999";
			u = Uri.withAppendedPath(KMMDProvider.CONTENT_ACCOUNT_URI, acctId + frag);
			u = Uri.parse(u.toString());
			String newBalance = updateBalance(openBalance, context, acctId);
			ContentValues cv = new ContentValues();
			cv.put("balance", createBalance(newBalance));
			cv.put("balanceFormatted", newBalance);
			context.getContentResolver().update(u, cv, null, null);
			
			// If account had an opening transaction AND our current Opening Balance is not zero, update previous transaction with 
			// current value.
			splits.moveToFirst();
			if( longOpenBal != 0 )
			{
				Log.d(TAG, "We had a previous opening balance AND the user changed it to a non-zero amount, updating splits.");
				frag = "#9999";
				u = Uri.withAppendedPath(KMMDProvider.CONTENT_SPLIT_URI, frag);
				u = Uri.parse(u.toString());				
				cv.clear();
				cv.put("value", createBalance(openBalance));
				cv.put("valueFormatted", openBalance);
				cv.put("shares", createBalance(openBalance));
				cv.put("sharesFormatted", openBalance);
				context.getContentResolver().update(u, cv, "transactionId=? AND accountId=?", 
						new String[] { splits.getString(splits.getColumnIndex("transactionId")), acctId });
				cv.clear();
				// Now adjust the other side of the transaction, just using the negative.
				String revBal = Transaction.convertToDollars(longOpenBal * -1, false, false);
				cv.put("value", createBalance(revBal));
				cv.put("valueFormatted", revBal);
				cv.put("shares", createBalance(revBal));
				cv.put("sharesFormatted", revBal);
				context.getContentResolver().update(u, cv, "transactionId=? AND accountId=?", 
						new String[] { splits.getString(splits.getColumnIndex("transactionId")), OpeningBalancesId });
			}
			else if( longOpenBal == 0 )
			{
				Log.d(TAG, "We had a previous opening balance but the user has changed it to zero, deleting previous splits and transaction");
				// If account had an opening transaction AND our current Opening Balance is zero, we need to delete previous transaction
				frag = "#9999";
				u = Uri.withAppendedPath(KMMDProvider.CONTENT_SPLIT_URI, frag);
				u = Uri.parse(u.toString());	
				context.getContentResolver().delete(u, "transactionId=?", new String[] { splits.getString(splits.getColumnIndex("transactionId")) });
				frag = "#9999";
				u = Uri.withAppendedPath(KMMDProvider.CONTENT_TRANSACTION_URI,splits.getString(splits.getColumnIndex("transactionId")) + frag);
				u = Uri.parse(u.toString());	
				context.getContentResolver().delete(u, null, null);
				// Reduce the number of transactions and splits in kmmFileInfo table.
				frag = "#9999";
				u = Uri.withAppendedPath(KMMDProvider.CONTENT_FILEINFO_URI, frag);
				u = Uri.parse(u.toString());
				context.getContentResolver().update(u, null, "transactions", new String[] { "-1" });
				context.getContentResolver().update(u, null, "splits", new String[] { "-2" });
				// Reduce the number of transactions listed in kmmAccounts table for this account.
				frag = "#9999";
				u = Uri.withAppendedPath(KMMDProvider.CONTENT_ACCOUNT_URI, frag);
				u = Uri.parse(u.toString());				
				Cursor acct = context.getContentResolver().query(u, new String[] { "transactionCount" },
						"id=?", new String[] { acctId }, null);
				acct.moveToFirst();
				cv.clear();
				int count = acct.getInt(acct.getColumnIndex("transactionCount"));
				cv.put("transactionCount", count - 1);
				context.getContentResolver().update(u, cv, "id=?", new String[] { acctId });
				// clean up our cursor.
				acct.close();
			}
		}
		else
			Log.d(TAG, "Some how we didn't catch our splits/transactions update!");
		
		// clean up our cursor.
		splits.close();
	}
	
	private String updateBalance(String newOpenBal, Context context, String acctId)
	{
		long longNewOpenBal = Transaction.convertToPennies(newOpenBal);
		long longOldAcctBal = 0;
		long longOldOpenBal = 0;
		long longDifference = 0;
		String newBal = null;
		String frag = "#9999";
		Uri u = Uri.withAppendedPath(KMMDProvider.CONTENT_ACCOUNT_URI, acctId + frag);
		u = Uri.parse(u.toString());
		Cursor acct = context.getContentResolver().query(u, null, null, null, null);
		if( acct.getCount() == 1 )
		{
			acct.moveToFirst();
			longOldAcctBal = Transaction.convertToPennies(acct.getString(acct.getColumnIndex("balanceFormatted")));
		}
		else
		{
			longOldAcctBal = (long) 0;
		}
		
		frag = "#9999";
		u = Uri.withAppendedPath(KMMDProvider.CONTENT_SPLIT_URI, frag);
		u = Uri.parse(u.toString());
		Cursor bal = context.getContentResolver().query(u, new String[] { "valueFormatted", "postDate" }, "accountId=? AND splitId='0' AND "
												+ "(payeeId IS NULL OR payeeId='')", new String[] { acctId }, null);
		if( bal.getCount() > 0 )
		{
			bal.moveToFirst();
			longOldOpenBal = Transaction.convertToPennies(bal.getString(bal.getColumnIndex("valueFormatted")));
			longDifference = longNewOpenBal - longOldOpenBal;
			newBal = Transaction.convertToDollars(longOldAcctBal + longDifference, false, false);
		}
		else
			newBal = Transaction.convertToDollars(longOldAcctBal + longNewOpenBal, false, false);
		
		// Clean up our cursors.
		acct.close();
		bal.close();
		
		Log.d(TAG, "newOpenBal: " + newOpenBal);
		Log.d(TAG, "longNewOpenBal: " + longNewOpenBal);
		Log.d(TAG, "longOldAcctBal: " + longOldAcctBal);
		Log.d(TAG, "longOldOpenBal: " + longOldOpenBal);
		Log.d(TAG, "longDifference: " + longDifference);
		Log.d(TAG, "newBal: " + newBal);
		
		return newBal;
	}
	
	private String createBalance(String formattedValue)
	{
		StringTokenizer split = new StringTokenizer(formattedValue, ".");
		String dollars = split.nextToken();
		String cents = split.nextToken();
		String balance = dollars + cents;
		String denominator = "/100";
		
		return balance + denominator;
	}
	
	public void createAccountId(Context context)
	{
		final String[] dbColumns = { "hiAccountId"};
		final String strOrderBy = "hiAccountId DESC";
		String frag = "#9999";
		Uri u = Uri.withAppendedPath(KMMDProvider.CONTENT_FILEINFO_URI, frag);
		u = Uri.parse(u.toString());
		// Run a query to get the Acount ids so we can create a new one.
		Cursor c = context.getContentResolver().query(u, dbColumns, null, null, strOrderBy);
		
		c.moveToFirst();

		// Since id is in A000000 format, we need to pick off the actual number then increase by 1.
		int lastId = c.getInt(0);
		lastId = lastId +1;
		String nextId = Integer.toString(lastId);
		
		// Need to pad out the number so we get back to our P000000 format
		String newId = "A";
		for(int i= 0; i < (6 - nextId.length()); i++)
		{
			newId = newId + "0";
		}
		
		// Tack on the actual number created.
		newId = newId + nextId;
		
		// Close our cursor.
		c.close();
		
		this.id = newId;
	}

	// Get an Account from a given AccountId
	public static Account getAccount(Context context, String id)
	{
		Account account;
		
		final String[] dbColumns = { "*" };
		String frag = "#9999";
		Uri u = Uri.withAppendedPath(KMMDProvider.CONTENT_ACCOUNT_URI, id + frag);
		u = Uri.parse(u.toString());
		Cursor c = context.getContentResolver().query(u, dbColumns, null, null, null);
		
		c.moveToFirst();
		// Get the account the user wants to edit.
		account = new Account(c, context);

		// We actually need to pull the correct balance information for this account, if query returns null, then opening bal was zero.
		frag = "#9999";
		u = Uri.withAppendedPath(KMMDProvider.CONTENT_SPLIT_URI, frag);
		u = Uri.parse(u.toString());
		Cursor bal = context.getContentResolver().query(u, new String[] { "valueFormatted", "postDate" }, "accountId=? AND splitId='0' AND "
												+ "(payeeId IS NULL OR payeeId='')", new String[] { id }, null);
		if( bal.getCount() > 0 )
		{
			bal.moveToFirst();
			account.setOpenDate(bal.getString(bal.getColumnIndex("postDate")));
			account.setOpenBalance(bal.getString(bal.getColumnIndex("valueFormatted")));
		}
		else
			account.setOpenBalance("0.00");
		
		// close our cursors
		c.close();
		bal.close();
		
		return account;
	}
	/***********************************************************************************************
	 * Required methods to make Account parcelable to pass between activities
	 * 
	 * Any time we are using this parcel to get Account we MUST use the setContext() method to set the
	 * context of the actual Account as we can not pass this as part of the Parcel. Failing to do this
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
		dest.writeString(id);
		dest.writeString(parentId);
		dest.writeString(accountName);
		dest.writeString(balance);
		dest.writeString(accountTypeString);
		dest.writeString(institutionId);
		dest.writeString(accountNumber);
		dest.writeString(IBAN);
		dest.writeString(openDate);
		dest.writeString(currencyId);
		dest.writeString(notes);
		dest.writeInt(accountType);
		dest.writeInt(transactionCount);
		dest.writeValue(isParent);
		dest.writeValue(isClosed);
		dest.writeValue(isPreferred);
	}
	
	public void logAccount()
	{
		Log.d(TAG, "id: " + id);
		Log.d(TAG, "parentId: " + parentId);
		Log.d(TAG, "accountName: " + accountName);
		Log.d(TAG, "balance: " + balance);
		Log.d(TAG, "accountTypeString: " + accountTypeString);
		Log.d(TAG, "institutionId: " + institutionId);
		Log.d(TAG, "accountNumber: " + accountNumber);
		Log.d(TAG, "IBAN: " + IBAN);
		Log.d(TAG, "openDate: " + openDate);
		Log.d(TAG, "currencyId: " + currencyId);
		Log.d(TAG, "notes: " + notes);
		Log.d(TAG, "transactionCount: " + String.valueOf(transactionCount));
		Log.d(TAG, "isParent: " + String.valueOf(isParent));
		Log.d(TAG, "isClosed: " + String.valueOf(isClosed));
		Log.d(TAG, "isPreferred: " + String.valueOf(isPreferred));
	}
}
