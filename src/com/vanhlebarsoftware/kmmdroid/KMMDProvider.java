package com.vanhlebarsoftware.kmmdroid;

import java.util.Calendar;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.UriMatcher;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteCursorDriver;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteQuery;
import android.net.Uri;
import android.util.Log;
import android.widget.*;

public class KMMDProvider extends ContentProvider 
{
	private static final String TAG = KMMDProvider.class.getSimpleName();
	public static final Uri CONTENT_ACCOUNT_URI = Uri.parse("content://com.vanhlebarsoftware.kmmdroid.kmmdprovider/account");
	public static final Uri CONTENT_SCHEDULE_URI = Uri.parse("content://com.vanhlebarsoftware.kmmdroid.kmmdprovider/schedule");
	public static final Uri CONTENT_SPLIT_URI = Uri.parse("content://com.vanhlebarsoftware.kmmdroid.kmmdprovider/split");
	public static final Uri CONTENT_TRANSACTION_URI = Uri.parse("content://com.vanhlebarsoftware.kmmdroid.kmmdprovider/transaction");
	public static final Uri CONTENT_FILEINFO_URI = Uri.parse("content://com.vanhlebarsoftware.kmmdroid.kmmdprovider/fileinfo");
	public static final Uri CONTENT_INSTITUTION_URI = Uri.parse("content://com.vanhlebarsoftware.kmmdroid.kmmdprovider/institution");
	public static final Uri CONTENT_PAYEE_URI = Uri.parse("content://com.vanhlebarsoftware.kmmdroid.kmmdprovider/payee");
	public static final Uri CONTENT_LEDGER_URI = Uri.parse("content://com.vanhlebarsoftware.kmmdroid.kmmdprovider/ledger");
	public static final Uri CONTENT_KVP_URI = Uri.parse("content://com.vanhlebarsoftware.kmmdroid.kmmdprovider/kvp");
	public static final Uri CONTENT_CURRENCY_URI = Uri.parse("content://com.vanhlebarsoftware.kmmdroid.kmmdprovider/currency");
	public static final Uri CONTENT_TRANSTAB_URI = Uri.parse("content://com.vanhlebarsoftware.kmmdroid.kmmdprovider/transtab");
	public static final Uri CONTENT_PREFERREDACCOUNTS_URI = Uri.parse("content://com.vanhlebarsoftware.kmmdroid.kmmdprovider/preferredaccounts");
	public static final Uri CONTENT_PRICES = Uri.parse("content://com.vanhlebarsoftware.kmmdroid.kmmdprovider/prices");
	public static final String ACCOUNT_MIME_TYPE = "vnd.android.cursor.item/vnd.vanhlebarsoftware.kmmdroid.account";
	public static final String ACCOUNTS_MIME_TYPE = "vnd.android.cursor.dir/vnd.vanhlebarsoftware.com.kmmdroid.accounts";
	public static final String SCHEDULE_MIME_TYPE = "vnd.android.cursor.item/vnd.vanhlebarsoftware.kmmdroid.schedule";
	public static final String SCHEDULES_MIME_TYPE = "vnd.android.cursor.dir/vnd.vanhlebarsoftware.kmmdroid.schedules";
	public static final String SPLIT_MIME_TYPE = "vnd.android.cursor.item/vnd.vanhlebarsoftware.kmmdroid.split";
	public static final String SPLITS_MIME_TYPE = "vnd.android.cursor.dir/vnd.vanhlebarsoftware.kmmdroid.splits";
	public static final String TRANSACTION_MIME_TYPE = "vnd.android.cursor.item/vnd.vanhlebarsoftware.kmmdroid.transaction";
	public static final String TRANSACTIONS_MIME_TYPE = "vnd.android.cursor.dir/vnd.vanhlebarsoftware.kmmdroid.transactions";
	public static final String FILEINFO_MIME_TYPE = "vnd.android.cursor.item/vnd.vanhlebarsoftware.kmmdroid.fileinfo";
	public static final String INSTITUTION_MIME_TYPE = "vnd.android.cursor.item/vnd.vanhlebarsoftware.kmmdroid.institution";
	public static final String INSTITUTIONS_MIME_TYPE = "vnd.android.cursor.dir/vnd.vanhlebarsoftware.kmmdroid.institutions";
	public static final String PAYEE_MIME_TYPE = "vnd.android.cursor.item/vnd.vanhlebarsoftware.kmmdroid.payee";
	public static final String PAYEES_MIME_TYPE = "vnd.android.cursor.dir/vnd.vanhlebarsoftware.kmmdroid.payees";
	public static final String LEDGER_MIME_TYPE = "vnd.android.cursor.dir/vnd.vanhlebarsoftware.kmmdroid.ledger";
	public static final String KVP_MIME_TYPE = "vnd.android.cursor.item/vnd.vanhlebarsoftware.kmmdroid.kvp";
	public static final String KVPS_MIME_TYPE = "vnd.android.cursor.dir/vnd.vanhlebarsoftware.kmmdroid.kvps";
	public static final String CURRENCY_MIME_TYPE = "vnd.android.cursor.item/vnd.vanhlebarsoftware.kmmdroid.currency";
	public static final String CURRENCIES_MIME_TYPE = "vnd.android.cursor.dir/vnd.vanhlebarsoftware.kmmdroid.currencies";
	public static final String TRANSTAB_MIME_TYPE = "vnd.android.cursor.dir/vnd.vanhlebarsoftware.kmmdroid.transtab";
	public static final String PREFERREDACCOUNTS_MIME_TYPE = "vnd.android.cursor.dir/vnd.vanhlebarsoftware.kmmdroid.preferredaccounts";
	public static final String PRICE_MIME_TYPE = "vnd.android.cursor.item/vnd.vanhlebarsoftware.kmmdroid.price";
	public static final String PRICES_MIME_TYPE = "vnd.android.cursor.dir/vnd.vanhlebarsoftware.kmmdroid.prices";
	/*********************************************************************************************************************
	 * Parameters used for querying the Accounts table 
	 *********************************************************************************************************************/
	private static final String accountsTable = "kmmAccounts";
	private static final String[] accountsColumns = { "accountName", "balanceFormatted", "id AS _id"};
	private static final String accountsSelection = "(parentId='AStd::Asset' OR parentId='AStd::Liability') AND " +
			"(balance != '0/1')";
	private static final String accountsSingleSelection = "id=?";
	private static final String accountsOrderBy = "parentID, accountName ASC";
	
	/*********************************************************************************************************************
	 * Parameters used for querying the schedules table
	 ********************************************************************************************************************/
	private static final String schedulesTable = "kmmSchedules, kmmSplits";
	private static final String[] schedulesColumns = { "kmmSchedules.id AS _id", "kmmSchedules.name AS Description", "occurence", "occurenceString", "occurenceMultiplier",
												"nextPaymentDue", "startDate", "endDate", "lastPayment", "valueFormatted", "autoEnter", "type", "typeString", "paymentType",
												"paymentTypeString", "fixed", "autoEnter", "weekendOption", "weekendOptionString" };
	private static final String schedulesSelection = "kmmSchedules.id = kmmSplits.transactionId AND nextPaymentDue > 0" + 
												" AND ((occurence = 1 AND lastPayment IS NULL) OR occurence != 1)" +
												" AND kmmSplits.splitId = 0"; // AND kmmSplits.accountId=";
	//private static final String[] schedulesSingleSelectionColumns = { "kmmSchedules.id AS _id", "kmmSchedules.name AS Description", "occurence", "occurenceString", "occurenceMultiplier",
	//	"nextPaymentDue", "startDate", "endDate", "lastPayment" };
	private static final String scheduleSingleSelection = "kmmSchedules.id = kmmSplits.transactionId" +
			" AND kmmSplits.splitId = 0 AND kmmSchedules.Id=?";
	private static final String schedulesOrderBy = "nextPaymentDue ASC";
	/*********************************************************************************************************************
	 * Parameters used for querying the transactions table
	 ********************************************************************************************************************/	
	//private static final String transactionsTable = "kmmTransactions";
	//private static final String[] transactionsColumns = { "*" };
	//private static final String transactionsSingleSelection = "id=?";
	/*********************************************************************************************************************
	 * Parameters used for querying the splits table
	 ********************************************************************************************************************/	
	//private static final String splitsTable = "kmmSplits";
	//private static final String[] splitsColumns = { "*" };
	//private static final String splitsSingleSelection = "transactionId=?";
	//private static final String splitsOrderBy = "splitId ASC";
	/*********************************************************************************************************************
	 * Parameters used for querying the fileinfo table
	 ********************************************************************************************************************/
	private static final String fileinfoTable = "kmmFileInfo";
		
	private String dbTable = null;
	private String[] dbColumns = null;
	private String dbSelection = null;
	private String[] dbSelectionArgs = null;
	private String dbOrderBy = null;
	private String accountUsed = null;
	private String path = null;
	private static final String authority = "com.vanhlebarsoftware.kmmdroid.kmmdprovider";
	private static final int ACCOUNTS = 1;
	private static final int ACCOUNTS_ID = 2;
	private static final int SCHEDULES = 3;
	private static final int SCHEDULES_ID = 4;
	private static final int SPLITS = 5;
	private static final int SPLITS_ID = 6;
	private static final int TRANSACTIONS = 7;
	private static final int TRANSACTIONS_ID= 8;
	private static final int FILEINFO = 9;
	private static final int INSTITUTIONS = 10;
	private static final int INSTITUTIONS_ID = 11;
	private static final int PAYEES = 12;
	private static final int PAYEES_ID = 13;
	private static final int LEDGER = 14;
	private static final int KVPS = 15;
	private static final int KVPS_ID = 16;
	private static final int CURRENCY = 17;
	private static final int CURRENCIES = 18;
	private static final int TRANSTAB = 19;
	private static final int PREFERREDACCOUNTS = 20;
	private static final int PRICE_ID = 21;
	private static final int PRICES = 22;
	
	private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	static
	{
		sURIMatcher.addURI(authority, "account", ACCOUNTS);
		sURIMatcher.addURI(authority, "account/*", ACCOUNTS_ID);
		sURIMatcher.addURI(authority, "schedule", SCHEDULES);
		sURIMatcher.addURI(authority, "schedule/*", SCHEDULES_ID);
		sURIMatcher.addURI(authority, "split", SPLITS);
		sURIMatcher.addURI(authority, "split/*", SPLITS_ID);
		sURIMatcher.addURI(authority, "transaction", TRANSACTIONS);
		sURIMatcher.addURI(authority, "transaction/*", TRANSACTIONS_ID);
		sURIMatcher.addURI(authority, "fileinfo", FILEINFO);
		sURIMatcher.addURI(authority, "institution", INSTITUTIONS);
		sURIMatcher.addURI(authority, "institution/*", INSTITUTIONS_ID);
		sURIMatcher.addURI(authority, "payee", PAYEES);
		sURIMatcher.addURI(authority, "payee/*", PAYEES_ID);
		sURIMatcher.addURI(authority, "ledger", LEDGER);
		sURIMatcher.addURI(authority, "kvp", KVPS);
		sURIMatcher.addURI(authority, "kvp/*", KVPS_ID);
		sURIMatcher.addURI(authority, "currency", CURRENCIES);
		sURIMatcher.addURI(authority, "currency/*", CURRENCY);
		sURIMatcher.addURI(authority, "transtab", TRANSTAB);
		sURIMatcher.addURI(authority, "preferredaccounts", PREFERREDACCOUNTS);
		sURIMatcher.addURI(authority, "prices", PRICES);
		sURIMatcher.addURI(authority, "prices/*", PRICE_ID);
	}
	public SharedPreferences prefs;
	private SQLiteDatabase db = null;

	@Override
	public boolean onCreate() 
	{         
		return false;
	}

	@Override
	public String getType(Uri uri) 
	{
		int match = sURIMatcher.match(uri);
		switch(match)
		{
		case ACCOUNTS:
			return ACCOUNTS_MIME_TYPE;
		case ACCOUNTS_ID:
			return ACCOUNT_MIME_TYPE;
		case SCHEDULES:
			return SCHEDULES_MIME_TYPE;
		case SCHEDULES_ID:
			return SCHEDULE_MIME_TYPE;
		case SPLITS:
			return SPLITS_MIME_TYPE;
		case SPLITS_ID:
			return SPLIT_MIME_TYPE;
		case TRANSACTIONS:
			return TRANSACTIONS_MIME_TYPE;
		case TRANSACTIONS_ID:
			return TRANSACTION_MIME_TYPE;
		case FILEINFO:
			return FILEINFO_MIME_TYPE;
		case INSTITUTIONS:
			return INSTITUTIONS_MIME_TYPE;
		case INSTITUTIONS_ID:
			return INSTITUTION_MIME_TYPE;
		case PAYEES:
			return PAYEES_MIME_TYPE;
		case PAYEES_ID:
			return PAYEE_MIME_TYPE;
		case LEDGER:
			return LEDGER_MIME_TYPE;
		case KVPS:
			return KVPS_MIME_TYPE;
		case KVPS_ID:
			return KVP_MIME_TYPE;
		case CURRENCY:
			return CURRENCY_MIME_TYPE;
		case CURRENCIES:
			return CURRENCIES_MIME_TYPE;
		case TRANSTAB:
			return TRANSTAB_MIME_TYPE;
		case PREFERREDACCOUNTS:
			return PREFERREDACCOUNTS_MIME_TYPE;
		case PRICE_ID:
			return PRICE_MIME_TYPE;
		case PRICES:
			return PRICES_MIME_TYPE;
		default:
			return null;
		}
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) 
	{
		// Need to get the prefs for our application so we can update the file used as dirty.
		Context context = getContext();
		Context cont = null;
		try 
		{
			cont = context.createPackageContext("com.vanhlebarsoftware.kmmdroid", Context.CONTEXT_IGNORE_SECURITY);
		} 
		catch (NameNotFoundException e)
		{
			e.printStackTrace();
		}
		SharedPreferences prefs = cont.getSharedPreferences("com.vanhlebarsoftware.kmmdroid_preferences", Context.MODE_WORLD_READABLE);
		
		String widgetId = uri.getFragment();
		
		// See if we need to open the database.
		if( !db.isOpen() )
			db = openDatabase(widgetId);
		
		// See which content uri is requested.
		int match = sURIMatcher.match(uri);
		String selArgs[] = { null };
		switch(match)
		{
			case ACCOUNTS:
				dbTable = "kmmAccounts";
				break;
			case ACCOUNTS_ID:
				dbTable = "kmmAccounts";
				dbSelection = "accountId=?";
				selArgs[0] = getId(uri);
				dbSelectionArgs = selArgs;
				break;
			case SCHEDULES:
				dbTable = "kmmSchedules";
				break;
			case SCHEDULES_ID:
				dbTable = "kmmSchedules";
				dbSelection = "scheduleId=?";
				selArgs[0] = getId(uri);
				dbSelectionArgs = selArgs;			
				break;
			case SPLITS:
				dbTable = "kmmSplits";
				break;
			case SPLITS_ID:
				dbTable = "kmmSplits";
				dbSelection = "splitsId=?";
				selArgs[0] = getId(uri);
				dbSelectionArgs = selArgs;
				break;
			case TRANSACTIONS:
				dbTable = "kmmTransactions";
				break;
			case TRANSACTIONS_ID:
				dbTable = "kmmTransactions";
				dbSelection = "transactionId=?";
				selArgs[0] = getId(uri);
				dbSelectionArgs = selArgs;
				break;
			case FILEINFO:
				dbTable = "kmmFileInfo";
				break;
			case INSTITUTIONS:
				dbTable = "kmmInstitutions";
				break;
			case PAYEES:
				dbTable = "kmmPayees";
				break;
			case KVPS:
				dbTable = "kmmKeyValuePairs";
				break;
			case CURRENCY:
				break;
			case CURRENCIES:
				dbTable = "kmmCurrencies";
				break;
			case PRICE_ID:
				dbTable = "kmmPrices";
				dbSelection = "fromId=?";
				selArgs[0] = getId(uri);
				dbSelectionArgs = selArgs;
				break;
			case PRICES:
				dbTable = "kmmPrices";
				break;
			default:
				break;
		}
		
		// perform the insert.
		int rows = db.delete(dbTable, selection, selectionArgs);
		
		// Need to mark the file as dirty for our cloud services.
		Editor editor = prefs.edit();
		editor.putString(db.getPath(), "1:1:1");
		editor.apply();
		
		// close the database.
		db.close();
		
		Uri u = Uri.parse(uri.getPath());
		getContext().getContentResolver().notifyChange(u, null);

		return rows;
	}
	
	@Override
	public Uri insert(Uri uri, ContentValues contentValues) 
	{
		String widgetId = uri.getFragment();
		
		// Need to get the prefs for our application so we can update the file used as dirty.
		Context context = getContext();
		Context cont = null;
		try 
		{
			cont = context.createPackageContext("com.vanhlebarsoftware.kmmdroid", Context.CONTEXT_IGNORE_SECURITY);
		} 
		catch (NameNotFoundException e)
		{
			e.printStackTrace();
		}
		SharedPreferences prefs = cont.getSharedPreferences("com.vanhlebarsoftware.kmmdroid_preferences", Context.MODE_WORLD_READABLE);

		// See if we need to open the database.
		if( !db.isOpen() )
			db = openDatabase(widgetId);
		
		// See which content uri is requested.
		int match = sURIMatcher.match(uri);
		switch(match)
		{
			case ACCOUNTS:
				dbTable = "kmmAccounts";
				break;
			case ACCOUNTS_ID:
				dbTable = "kmmAccount";
				break;
			case SCHEDULES:
				dbTable = "kmmSchedules";
				break;
			case SCHEDULES_ID:
				dbTable = "kmmSchedules";
				break;
			case SPLITS:
				dbTable = "kmmSplits";
				break;
			case SPLITS_ID:
				dbTable = "kmmSplits";
				break;
			case TRANSACTIONS:
				dbTable = "kmmTransactions";
				break;
			case TRANSACTIONS_ID:
				dbTable = "kmmTransactions";
				break;
			case FILEINFO:
				dbTable = "kmmFileInfo";
				break;
			case INSTITUTIONS:
				dbTable = "kmmInstitutions";
				break;
			case PAYEES:
				dbTable = "kmmPayees";
				break;
			case KVPS:
				dbTable = "kmmKeyValuePairs";
				break;
			case CURRENCY:
				dbTable = "kmmCurrencies";
				break;
			case CURRENCIES:
				dbTable = "kmmCurrencies";
				break;
			case PRICE_ID:
				dbTable = "kmmPrices";
				break;
			case PRICES:
				dbTable = "kmmPrices";
				break;
			default:
				break;
		}
		
		// perform the insert.
		db.insert(dbTable, null, contentValues);
		
		// Need to mark the file as dirty for our cloud services.
		Editor editor = prefs.edit();
		editor.putString(db.getPath(), "1:1:1");
		editor.apply();
		
		// close the database.
		db.close();
		
		Uri u = Uri.parse(uri.getPath());
		getContext().getContentResolver().notifyChange(u, null);

		return null;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) 
	{
		String id = null;
		String widgetId = uri.getFragment();
		
		// see if we need to open the database.
		if( db == null || !db.isOpen() )
		{
			// We need to open the database.
			Log.d(TAG, "Need to open the database for widgetId: " + widgetId);
			db = openDatabase(widgetId);
		}
		
		// Need to get the prefs for our application so we can update the account used..
		Context context = getContext();
		Context cont = null;
		try 
		{
			cont = context.createPackageContext("com.vanhlebarsoftware.kmmdroid", Context.CONTEXT_IGNORE_SECURITY);
		} 
		catch (NameNotFoundException e)
		{
			e.printStackTrace();
		}

		// See which content uri is requested.
		int match = sURIMatcher.match(uri);	
		switch(match)
		{
			case ACCOUNTS:
				dbTable = "kmmAccounts";
				dbColumns = projection;
				dbSelection = selection;
				dbOrderBy = sortOrder;
				dbSelectionArgs = selectionArgs;
				break;
			case ACCOUNTS_ID:
				dbTable = accountsTable;
				if(projection != null)
					dbColumns = projection;
				else
					dbColumns = accountsColumns;
				if(selection != null)
					dbSelection = selection;
				else
					dbSelection = accountsSingleSelection;
				dbOrderBy = accountsOrderBy;
				id = this.getId(uri);
				dbSelectionArgs = new String[] { id };
				break;
			case SCHEDULES:
				SharedPreferences prefs = cont.getSharedPreferences("com.vanhlebarsoftware.kmmdroid_preferences", Context.MODE_WORLD_READABLE);
				// See if we have a widgetId or not, if so we need to get the accountId for that specific widget, if not leave off the accountId
				if( widgetId != null && !widgetId.equals("9999") )
				{
					accountUsed = prefs.getString("accountUsed" + String.valueOf(widgetId), "");
					dbSelection = schedulesSelection + " AND kmmSplits.accountId='" + accountUsed + "'";
					dbTable = schedulesTable;
					dbColumns = schedulesColumns;
					dbOrderBy = schedulesOrderBy;
					dbSelectionArgs = selectionArgs;
				}
				else if( widgetId != null && widgetId.equals("9999"))
				{
					dbTable = "kmmSchedules, kmmSplits, kmmPayees";
					dbColumns = projection;
					dbSelection = selection;
					dbSelectionArgs = selectionArgs;
					dbOrderBy = sortOrder;
				}
				else
				{
					dbSelection = schedulesSelection;
					dbTable = schedulesTable;
					dbColumns = schedulesColumns;
					dbOrderBy = schedulesOrderBy;
					dbSelectionArgs = selectionArgs;
				}
				break;
			case SCHEDULES_ID:
				dbTable = schedulesTable;
				if(projection != null)
					dbColumns = projection;
				else
					dbColumns = schedulesColumns;
				if(selection != null)
					dbSelection = selection;
				else
					dbSelection = scheduleSingleSelection;
				id = this.getId(uri);
				dbSelectionArgs = new String[] { id };
				dbOrderBy = schedulesOrderBy;	
				//dbSelectionArgs = selectionArgs;
				break;
			case TRANSACTIONS_ID:
				dbTable = "kmmTransactions";
				dbColumns = new String[] { "*" };
				dbSelection = "id=?";
				dbOrderBy = null;
				id = this.getId(uri);
				dbSelectionArgs = new String[] { id };
				break;
			case SPLITS:
				dbTable = "kmmSplits";
				dbColumns = projection;
				dbSelection = selection;
				dbOrderBy = sortOrder;
				dbSelectionArgs = selectionArgs;
				break;
			case SPLITS_ID:
				dbTable = "kmmSplits";
				dbColumns = new String[] { "*" };
				dbSelection = "transactionId=?";
				dbOrderBy = "splitId ASC";
				id = this.getId(uri);
				dbSelectionArgs = new String[] { id };
				break;
			case FILEINFO:
				dbTable = fileinfoTable;
				dbColumns = projection;
				dbSelection = selection;
				dbOrderBy = sortOrder;
				dbSelectionArgs = selectionArgs;
				break;
			case INSTITUTIONS:
				dbTable = "kmmInstitutions";
				dbColumns = projection;
				dbSelection = selection;
				dbOrderBy = sortOrder;
				dbSelectionArgs = selectionArgs;
				break;				
			case INSTITUTIONS_ID:
				dbTable = "kmmInstitutions";
				dbColumns = projection;
				if( selection != null )	
					dbSelection = selection;
				else
					dbSelection = "id=?";
				dbOrderBy = sortOrder;
				id = this.getId(uri);
				dbSelectionArgs = new String[] { id };
				break;
			case PAYEES:
				dbTable = "kmmPayees";
				dbColumns = projection;
				dbSelection = selection;
				dbOrderBy = sortOrder;
				dbSelectionArgs = selectionArgs;
				break;
			case PAYEES_ID:
				dbTable = "kmmPayees";
				if( projection != null )
					dbColumns = projection;
				else
					dbColumns = new String[] { "*" };
				dbSelection = "id=?";
				if( sortOrder != null )
					dbOrderBy = sortOrder;
				else
					dbOrderBy = "name ASC";
				id = this.getId(uri);
				dbSelectionArgs = new String[] { id };
				break;
			case LEDGER:
				dbTable = "kmmSplits, kmmPayees";
				dbColumns = projection;
				dbSelection = selection;
				dbOrderBy = sortOrder;
				dbSelectionArgs = selectionArgs;
				Log.d(TAG, "dbTable:" + dbTable);
				for(int j=0; j<dbColumns.length; j++)
					Log.d(TAG, "dbColumns[" + j + ":] " + dbColumns[j]);
				Log.d(TAG, "dbSelection: " + dbSelection);
				for(int i=0; i<dbSelectionArgs.length; i++)
					Log.d(TAG, "dbSelectionArgs[" + i + "]: " + dbSelectionArgs[i]);
				Log.d(TAG, "dbOrderBy: " + dbOrderBy);
				break;
			case KVPS:
				dbTable = "kmmKeyValuePairs";
				dbColumns = projection;
				dbSelection = selection;
				dbOrderBy = sortOrder;
				dbSelectionArgs = selectionArgs;
				break;
			case KVPS_ID:
				dbTable = "kmmKeyValuePairs";
				dbColumns = projection;
				dbSelection = selection;
				dbOrderBy = sortOrder;
				id = this.getId(uri);
				dbSelectionArgs = new String[] { id };
				break;
			case CURRENCY:
				dbTable = "kmmCurrencies";
				dbColumns = projection;
				dbSelection = selection;
				dbOrderBy = sortOrder;
				id = this.getId(uri);
				dbSelectionArgs = new String[] { id };
				break;
			case CURRENCIES:
				dbTable = "kmmCurrencies";
				dbColumns = projection;
				dbSelection = selection;
				dbOrderBy = sortOrder;
				dbSelectionArgs = selectionArgs;
				break;	
			case TRANSTAB:
				dbTable = "kmmSplits, kmmAccounts";
				dbColumns = projection;
				dbSelection = selection;
				dbOrderBy = sortOrder;
				dbSelectionArgs = selectionArgs;
				Log.d(TAG, "Retrieving transactions for the TransTab");
				break;
			case PREFERREDACCOUNTS:
				dbTable = "kmmAccounts, kmmKeyValuePairs";
				dbColumns = projection;
				dbSelection = selection;
				dbOrderBy = sortOrder;
				dbSelectionArgs = selectionArgs;
				Log.d(TAG, "Getting preferred accounts for the widget!");
				break;
			case PRICES:
				Log.d(TAG, "Using the PRICES case.");
				dbTable = "kmmPrices";
				dbColumns = projection;
				dbSelection = selection;
				dbOrderBy = sortOrder;
				dbSelectionArgs = selectionArgs;
				break;
			case PRICE_ID:
				Log.d(TAG, "Using the PRICE_ID case.");
				dbTable = "kmmPrices";
				if( projection != null )
					dbColumns = projection;
				else
					dbColumns = new String[] { "*" };
				dbSelection = "id=?";
				if( sortOrder != null )
					dbOrderBy = sortOrder;
				else
					dbOrderBy = "name ASC";
				id = this.getId(uri);
				dbSelectionArgs = new String[] { id };
				break;
			default:
				Log.d(TAG, "We didn't get a match for this query!");
				break;
		}
		
		Cursor cur = null;
		if(id == null)
		{
			if(db != null)
			{
				cur = db.query(dbTable, dbColumns, dbSelection, dbSelectionArgs, null, null, dbOrderBy);
			}
			else
			{	
				Log.d(TAG, "Database is not open!");
				return null;
			}
		}
		else
		{
			if(db != null)
				cur = db.query(dbTable, dbColumns, dbSelection, dbSelectionArgs, null, null, dbOrderBy);
			else
			{
				Log.d(TAG, "Database is not open!");
				return null;
			}
		}

		// set the cursor to receive notifications of updates.
		cur.setNotificationUri(getContext().getContentResolver(), uri);
		return cur;
	}

	@Override
	public int update(Uri uri, ContentValues contentvalues, String selection, String[] selectionArgs) 
	{
		int result = 0;
		String widgetId = uri.getFragment();
			
		// See if we need to open the database.
		if( !db.isOpen() )
			db = openDatabase(widgetId);
		
		// See which content uri is requested.
		int match = sURIMatcher.match(uri);
		switch(match)
		{
			case ACCOUNTS:
				break;
			case ACCOUNTS_ID:
				dbTable = "kmmAccounts";
				dbSelection = "id=?";
				result = db.update(dbTable, contentvalues, dbSelection, new String[] { this.getId(uri) });
				break;
			case SCHEDULES:
				break;
			case SCHEDULES_ID:
				dbTable = "kmmSchedules";
				dbSelection = "id=?";
				result = db.update(dbTable, contentvalues, dbSelection, new String[] { this.getId(uri) });
				break;
			case SPLITS:
				dbTable = "kmmSplits";
				result = db.update(dbTable, contentvalues, selection, selectionArgs);
				break;
			case SPLITS_ID:
				break;
			case TRANSACTIONS:
				break;
			case TRANSACTIONS_ID:
				dbTable = "kmmTransactions";
				dbSelection = "id=?";
				result = db.update(dbTable, contentvalues, dbSelection, new String[] { this.getId(uri) });
				break;
			case FILEINFO:
				updateFileInfo(selection, Integer.valueOf(selectionArgs[0]));
				break;
			case PAYEES_ID:
				dbTable = "kmmPayees";
				dbSelection = "id=?";
				result = db.update(dbTable, contentvalues, dbSelection, new String[] { this.getId(uri) });
				break;
			case INSTITUTIONS_ID:
				dbTable = "kmmInstitutions";
				dbSelection = "id=?";
				result = db.update(dbTable, contentvalues, dbSelection, new String[] { this.getId(uri) });
				break;
			case KVPS:
				dbTable = "kmmKeyValuePairs";
				result = db.update(dbTable, contentvalues, selection, selectionArgs);
				break;
			case KVPS_ID:
				dbTable = "kmmKeyValuePairs";
				break;
			case CURRENCY:
				dbTable = "kmmCurrencies";
				break;
			case PRICE_ID:
				dbTable = "kmmPrices";
				dbSelection = "fromId=?";
				result = db.update(dbTable, contentvalues, selection, selectionArgs);
				break;
			default:
				break;
		}
		
		// clean up, close the database.
		db.close();
		
		Uri u = Uri.parse(uri.getPath());
		getContext().getContentResolver().notifyChange(u, null);
		
		return result;
	}

	private String getId(Uri uri)
	{
		String lastPathSegment = uri.getLastPathSegment();
		
		int match = sURIMatcher.match(uri);
		switch(match)
		{
		case ACCOUNTS:
			return null;
		case ACCOUNTS_ID:
			return lastPathSegment;
		case SCHEDULES:
			Log.d(TAG, "Getting all schedules");
			return null;
		case SCHEDULES_ID:
			Log.d(TAG, "Schedule ID: " + lastPathSegment);
			return lastPathSegment;
		case SPLITS:
			return null;
		case SPLITS_ID:
			return lastPathSegment;
		case TRANSACTIONS:
			return null;
		case TRANSACTIONS_ID:
			return lastPathSegment;
		case FILEINFO:
			return null;
		case INSTITUTIONS:
			return null;
		case INSTITUTIONS_ID:
			return lastPathSegment;
		case PAYEES:
			return null;
		case PAYEES_ID:
			return lastPathSegment;
		case KVPS:
			return null;
		case KVPS_ID:
			return lastPathSegment;
		case CURRENCY:
			return lastPathSegment;
		case CURRENCIES:
			return null;
		case PRICES:
			return null;
		case PRICE_ID:
			return lastPathSegment;
		default:
			return null;
		}		
	}
	
	private SQLiteDatabase openDatabase(String widgetId)
	{
		// Need to get the prefs for our application.
		Context context = getContext();
		Context cont = null;
		try 
		{
			cont = context.createPackageContext("com.vanhlebarsoftware.kmmdroid", Context.CONTEXT_IGNORE_SECURITY);
		} 
		catch (NameNotFoundException e)
		{
			e.printStackTrace();
		}
		SharedPreferences prefs = cont.getSharedPreferences("com.vanhlebarsoftware.kmmdroid_preferences", Context.MODE_WORLD_READABLE);
		
		// Open the correct database
		// If widgetId is 9999, then we are already in the application and need to open the default database.
		// If widgetId is null, then we are coming from a scheduled event for checking schedules of the main app.
		String prefString = null;
		if( widgetId == null )
		{
			prefString = "Full Path";
		}
		else if( widgetId.equals("9999") )
		{
			prefString = "currentOpenedDatabase";
		}
		else
		{
			// Get the path to the database the user wants to use for this widget.
			prefString = "widgetDatabasePath" + String.valueOf(widgetId);
		}
		
		path = prefs.getString(prefString, "");		
		try 
		{
			return SQLiteDatabase.openDatabase(path, null, 0);
		} 
		catch (SQLiteException e) 
		{
			Log.d(TAG, "Error opening database!! Error message: " + e.getMessage());
		}
		
		return null;
	}
	
	public void updateFileInfo(String updateColumn, int nChange)
	{
		Cursor cursor;
		ContentValues values = new ContentValues();
		
		if( updateColumn.equals("lastModified") )
		{
	        // get the current date
	        Calendar c = Calendar.getInstance();
	        int intYear = c.get(Calendar.YEAR);
	        int intMonth = c.get(Calendar.MONTH);
	        int intDay = c.get(Calendar.DAY_OF_MONTH);
	        String date = new StringBuilder()
			.append(intYear).append("-")
			.append(intMonth + 1).append("-")
			.append(intDay).toString();
	        
	        values.put("lastModified", date);
		}
		else if ( updateColumn.equals("institutions") )
		{
			final String[] dbColumns = { "institutions" };
			
			cursor = db.query("kmmFileInfo", dbColumns, null, null, null, null, null);
			cursor.moveToFirst();
			
			int id = cursor.getInt(0);
			id = id + nChange;
	
			values.put("institutions", id);			
		}
		else if ( updateColumn.equals("accounts") )
		{
			final String[] dbColumns = { "accounts" };
			
			cursor = db.query("kmmFileInfo", dbColumns, null, null, null, null, null);
			cursor.moveToFirst();
			
			int id = cursor.getInt(0);
			id = id + nChange;
	
			values.put("accounts", id);			
		}
		else if ( updateColumn.equals("payees") )
		{
			final String[] dbColumns = { "payees" };
			
			cursor = db.query("kmmFileInfo", dbColumns, null, null, null, null, null);
			cursor.moveToFirst();
			
			int id = cursor.getInt(0);
			id = id + nChange;
	
			values.put("payees", id);			
		}
		else if ( updateColumn.equals("transactions") )
		{
			final String[] dbColumns = { "transactions" };
			
			cursor = db.query("kmmFileInfo", dbColumns, null, null, null, null, null);
			cursor.moveToFirst();
			
			int id = cursor.getInt(0);
			id = id + nChange;
	
			values.put("transactions", id);			
		}
		else if ( updateColumn.equals("schedules") )
		{
			final String[] dbColumns = { "schedules" };
			
			cursor = db.query("kmmFileInfo", dbColumns, null, null, null, null, null);
			cursor.moveToFirst();
			
			int id = cursor.getInt(0);
			id = id + nChange;
	
			values.put("schedules", id);			
		}
		else if ( updateColumn.equals("splits") )
		{
			final String[] dbColumns = { "splits" };
			
			cursor = db.query("kmmFileInfo", dbColumns, null, null, null, null, null);
			cursor.moveToFirst();
			
			int id = cursor.getInt(0);
			id = id + nChange;
	
			values.put("splits", id);			
		}
		else if ( updateColumn.equals("hiInstitutionId") )
		{
			final String[] dbColumns = { "hiInstitutionId" };
			
			cursor = db.query("kmmFileInfo", dbColumns, null, null, null, null, null);
			cursor.moveToFirst();
			
			int id = cursor.getInt(0);
			id = id + 1;
	
			values.put("hiInstitutionId", id);			
		}
		else if ( updateColumn.equals("hiPayeeId") )
		{
			final String[] dbColumns = { "hiPayeeId" };
			
			cursor = db.query("kmmFileInfo", dbColumns, null, null, null, null, null);
			cursor.moveToFirst();
			
			int id = cursor.getInt(0);
			id = id + 1;
	
			values.put("hiPayeeId", id);	
		}
		else if ( updateColumn.equals("hiAccountId") )
		{
			final String[] dbColumns = { "hiAccountId" };
			
			cursor = db.query("kmmFileInfo", dbColumns, null, null, null, null, null);
			cursor.moveToFirst();
			
			int id = cursor.getInt(0);
			id = id + 1;
	
			values.put("hiAccountId", id);			
		}
		else if ( updateColumn.equals("hiTransactionId") )
		{
			final String[] dbColumns = { "hiTransactionId" };
			
			cursor = db.query("kmmFileInfo", dbColumns, null, null, null, null, null);
			cursor.moveToFirst();
			
			int id = cursor.getInt(0);
			id = id + 1;
			Log.d(TAG, "new hiTransactionId");
			values.put("hiTransactionId", id);			
		}
		else if ( updateColumn.equals("hiScheduleId") )
		{
			final String[] dbColumns = { "hiScheduleId" };
			
			cursor = db.query("kmmFileInfo", dbColumns, null, null, null, null, null);
			cursor.moveToFirst();
			
			int id = cursor.getInt(0);
			id = id + 1;
	
			values.put("hiScheduleId", id);
		}
		
		db.update("kmmFileInfo", values, null, null);
	}
}
