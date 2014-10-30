package com.vanhlebarsoftware.kmmdroid;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DbHelper extends SQLiteOpenHelper
{
	static final String TAG = "DbHelper";
	static final int DB_VERSION = 1;
	Context context;
	
	// Constructor
	public DbHelper (Context context, String db_name)
	{
		super (context, db_name, null, DB_VERSION);
		this.context = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db)
	{
		Log.d(TAG, "onCreate");
		String sql = null;
		// TODO Auto-generated method stub
		sql = createAccounts();
		db.execSQL(sql);
		Log.d(TAG,  "kmmAccounts created");
		
		sql = createBudgetConfig();
		db.execSQL(sql);
		Log.d(TAG,  "kmmBudgetConfig created");	
		
		sql = createCurrencies();
		db.execSQL(sql);
		Log.d(TAG,  "kmmCurrencies created");
		
		sql = createFileInfo();
		db.execSQL(sql);
		Log.d(TAG,  "kmmFileInfo created");
		
		sql = createInstitutions();
		db.execSQL(sql);
		Log.d(TAG,  "kmmInstitutions created");
		
		sql = createKeyValuePairs();
		db.execSQL(sql);
		Log.d(TAG,  "kmmKeyValuePairs created");
		
		sql = createPayees();
		db.execSQL(sql);
		Log.d(TAG,  "kmmPayees created");
		
		sql = createPrices();
		db.execSQL(sql);
		Log.d(TAG,  "kmmPrices created");
		
		sql = createReportConfig();
		db.execSQL(sql);
		Log.d(TAG,  "kmmReportConfig created");
		
		sql = createSchedulePaymentHistory();
		db.execSQL(sql);
		Log.d(TAG,  "kmmSchedulePaymentHistory created");
		
		sql = createSchedules();
		db.execSQL(sql);
		Log.d(TAG,  "kmmSchedules created");
		
		sql = createSecurities();
		db.execSQL(sql);
		Log.d(TAG,  "kmmSecurities created");
		
		sql = createSplits();
		db.execSQL(sql);
		Log.d(TAG,  "kmmSplits created");
		
		sql = createTransactions();
		db.execSQL(sql);
		Log.d(TAG,  "kmmTransactions created");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		// TODO Auto-generated method stub
		
	}
	
	/*
	 * Private methods to create each of the tables inside the database.
	 * Each method returns the SQL string for the creation of the table.
	 * Current tables are:
	 * 		kmmAccounts
	 * 		kmmBudgetConfig
	 * 		kmmCurrencies
	 * 		kmmFileInfo
	 * 		kmmInstitutions
	 * 		kmmKeyValuePairs
	 * 		kmmPayees
	 * 		kmmPrices
	 * 		kmmReportConfig
	 * 		kmmSchedulePaymentHistory
	 * 		kmmSchedules
	 * 		kmmSecurities
	 * 		kmmSplits
	 * 		kmmTransactions
	 */
	private String createAccounts()
	{
		String sql = "CREATE TABLE kmmAccounts (id varchar(32) NOT NULL, institutionId varchar(32), parentId varchar(32)," +
				" lastReconciled timestamp, lastModified timestamp, openingDate date, accountNumber mediumtext," +
				" accountType varchar(16) NOT NULL, accountTypeString mediumtext, isStockAccount char(1)," +
				" accountName mediumtext, description mediumtext, currencyId varchar(32), balance mediumtext," +
				" balanceFormatted mediumtext, transactionCount bigint unsigned, PRIMARY KEY (id))";
		
		return sql;
	}
	
	private String createBudgetConfig()
	{
		String sql = "CREATE TABLE kmmBudgetConfig (id varchar(32) NOT NULL, name text NOT NULL, start date NOT NULL," +
				" XML longtext, PRIMARY KEY (id))";
		
		return sql;
	}
	
	private String createCurrencies()
	{
		String sql = "CREATE TABLE kmmCurrencies (ISOcode char(3) NOT NULL, name text NOT NULL, type smallint unsigned," +
				" typeString mediumtext, symbol1 smallint unsigned, symbol2 smallint unsigned," +
				" symbol3 smallint unsigned, symbolString varchar(255), partsPerUnit varchar(24)," +
				" smallestCashFraction varchar(24), smallestAccountFraction varchar(24), PRIMARY KEY (ISOcode))";
		
		return sql;
	}
	
	private String createFileInfo()
	{
		String sql = "CREATE TABLE kmmFileInfo (version varchar(16), created date, lastModified date," +
				" baseCurrency char(3), institutions bigint unsigned, accounts bigint unsigned," +
				" payees bigint unsigned, transactions bigint unsigned, splits bigint unsigned," +
				" securities bigint unsigned, prices bigint unsigned, currencies bigint unsigned," +
				" schedules bigint unsigned, reports bigint unsigned, kvps bigint unsigned, dateRangeStart date," +
				" dateRangeEnd date, hiInstitutionId bigint unsigned, hiPayeeId bigint unsigned," +
				" hiAccountId bigint unsigned, hiTransactionId bigint unsigned, hiScheduleId bigint unsigned," +
				" hiSecurityId bigint unsigned, hiReportId bigint unsigned, encryptData varchar(255)," +
				" updateInProgress char(1), budgets bigint unsigned, hiBudgetId bigint unsigned," +
				" logonUser varchar(255), logonAt timestamp, fixLevel int unsigned)";
		
		return sql;
	}
	
	private String createInstitutions()
	{
		String sql = "CREATE TABLE kmmInstitutions (id varchar(32) NOT NULL, name text NOT NULL, manager mediumtext," +
				" routingCode mediumtext, addressStreet mediumtext, addressCity mediumtext, addressZipcode mediumtext," +
				" telephone mediumtext, PRIMARY KEY (id))";
		
		return sql;
	}
	
	private String createKeyValuePairs()
	{
		String sql = "CREATE TABLE kmmKeyValuePairs (kvpType varchar(16) NOT NULL, kvpId varchar(32)," +
				" kvpKey varchar(255) NOT NULL, kvpData mediumtext)";
		
		return sql;
	}
	
	private String createPayees()
	{
		String sql = "CREATE TABLE kmmPayees (id varchar(32) NOT NULL, name mediumtext, reference mediumtext," +
				" email mediumtext, addressStreet mediumtext, addressCity mediumtext, addressZipcode mediumtext," +
				" addressState mediumtext, telephone mediumtext, notes longtext, defaultAccountId varchar(32)," +
				" matchData tinyint unsigned, matchIgnoreCase char(1), matchKeys mediumtext, PRIMARY KEY (id))";
		
		return sql;
	}
	
	private String createPrices()
	{
		String sql = "CREATE TABLE kmmPrices (fromId varchar(32) NOT NULL, toId varchar(32) NOT NULL," +
				" priceDate date NOT NULL, price text NOT NULL, priceFormatted mediumtext, priceSource mediumtext," +
				" PRIMARY KEY (fromId, toId, priceDate))";
		
		return sql;
	}
	
	private String createReportConfig()
	{
		String sql = "CREATE TABLE kmmReportConfig (name varchar(255) NOT NULL, XML longtext, id varchar(32) NOT NULL," +
				" PRIMARY KEY (id))";
		
		return sql;
	}
	
	private String createSchedulePaymentHistory()
	{
		String sql = "CREATE TABLE kmmSchedulePaymentHistory (schedId varchar(32) NOT NULL, payDate date NOT NULL," +
				" PRIMARY KEY (schedId, payDate))";
		
		return sql;
	}
	
	private String createSchedules()
	{
		String sql = "CREATE TABLE kmmSchedules (id varchar(32) NOT NULL, name text NOT NULL," +
				" type tinyint unsigned NOT NULL, typeString mediumtext, occurence smallint unsigned NOT NULL," +
				" occurenceMultiplier smallint unsigned NOT NULL, occurenceString mediumtext," +
				" paymentType tinyint unsigned, paymentTypeString longtext, startDate date NOT NULL, endDate date," +
				" fixed char(1) NOT NULL, autoEnter char(1) NOT NULL, lastPayment date, nextPaymentDue date," +
				" weekendOption tinyint unsigned NOT NULL, weekendOptionString mediumtext, PRIMARY KEY (id))";
		
		return sql;
	}
	
	private String createSecurities()
	{
		String sql = "CREATE TABLE kmmSecurities (id varchar(32) NOT NULL, name text NOT NULL, symbol mediumtext," +
				" type smallint unsigned NOT NULL, typeString mediumtext, smallestAccountFraction varchar(24)," +
				" tradingMarket mediumtext, tradingCurrency char(3), PRIMARY KEY (id))";
		
		return sql;
	}
	
	private String createSplits()
	{
		String sql = "CREATE TABLE kmmSplits (transactionId varchar(32) NOT NULL, txType char(1)," +
				" splitId smallint unsigned NOT NULL, payeeId varchar(32), reconcileDate timestamp," +
				" action varchar(16), reconcileFlag char(1), value text NOT NULL, valueFormatted text," +
				" shares text NOT NULL, sharesFormatted mediumtext, price text, priceFormatted mediumtext," +
				" memo mediumtext, accountId varchar(32) NOT NULL, checkNumber varchar(32), postDate timestamp," +
				" bankId mediumtext, PRIMARY KEY (transactionId, splitId))";
		
		return sql;
	}
	
	private String createTransactions()
	{
		String sql = "CREATE TABLE kmmTransactions (id varchar(32) NOT NULL, txType char(1), postDate timestamp," +
				" memo mediumtext, entryDate timestamp, currencyId char(3), bankId mediumtext, PRIMARY KEY (id))";
		
		return sql;
	}
}
