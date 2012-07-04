package com.vanhlebarsoftware.kmmdroid;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

public class KMMDProvider extends ContentProvider 
{
	private static final String TAG = KMMDProvider.class.getSimpleName();
	public static final Uri CONTENT_ACCOUNT_URI = Uri.parse("content://com.vanhlebarsoftware.kmmdroid.kmmdprovider/account");
	public static final Uri CONTENT_SCHEDULE_URI = Uri.parse("content://com.vanhlebarsoftware.kmmdroid.kmmdprovider/schedule");
	public static final String ACCOUNT_MIME_TYPE = "vnd.android.cursor.item/vnd.vanhlebarsoftware.kmmdroid.account";
	public static final String ACCOUNTS_MIME_TYPE = "vnd.android.cursor.dir/vnd.vanhlebarsoftware.com.kmmdroid.accounts";
	public static final String SCHEDULE_MIME_TYPE = "vnd.android.cursor.item/vnd.vanhlebarsoftware.kmmdroid.schedule";
	public static final String SCHEDULES_MIME_TYPE = "vnd.android.cursor.item/vnd.vanhlebarsoftware.kmmdroid.schedules";
	
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
												"nextPaymentDue", "startDate", "endDate", "lastPayment", "valueFormatted" };
	private static final String schedulesSelection = "kmmSchedules.id = kmmSplits.transactionId AND nextPaymentDue > 0" + 
												" AND ((occurenceString = 'Once' AND lastPayment IS NULL) OR occurenceString != 'Once')" +
												" AND kmmSplits.splitId = 0 AND kmmSplits.accountId=";
	private static final String schedulesOrderBy = "nextPaymentDue ASC";
	private String dbTable = null;
	private String[] dbColumns = null;
	private String dbSelection = null;
	private String dbOrderBy = null;
	private String accountUsed = null;
	private String path = null;
	private static final String authority = "com.vanhlebarsoftware.kmmdroid.kmmdprovider";
	private static final int ACCOUNTS = 1;
	private static final int ACCOUNTS_ID = 2;
	private static final int SCHEDULES = 3;
	private static final int SCHEDULES_ID = 4;
	private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	static
	{
		sURIMatcher.addURI(authority, "account", ACCOUNTS);
		sURIMatcher.addURI(authority, "account/*", ACCOUNTS_ID);
		sURIMatcher.addURI(authority, "schedule", SCHEDULES);
		sURIMatcher.addURI(authority, "schedule/*", SCHEDULES_ID);
	}
	private boolean firstRun = true;
	public SharedPreferences prefs;
	SQLiteDatabase db = null;
	
	@Override
	public int delete(Uri arg0, String arg1, String[] arg2) 
	{
		// TODO Auto-generated method stub
		return 0;
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
		default:
			return null;
		}
	}

	@Override
	public Uri insert(Uri arg0, ContentValues arg1) 
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean onCreate() 
	{  
		Log.d(TAG, "onCreate() entered");
		// Need to get the prefs for our application.
		Context context = getContext();
		Context cont = null;
		try 
		{
			cont = context.createPackageContext("com.vanhlebarsoftware.kmmdroid", Context.CONTEXT_IGNORE_SECURITY);
		} 
		catch (NameNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		SharedPreferences prefs = cont.getSharedPreferences("com.vanhlebarsoftware.kmmdroid_preferences", Context.MODE_WORLD_READABLE);
		
		// Get the path to the database the user wants to use for this widget.
		path = prefs.getString("Full Path", "");

		Log.d(TAG, "Database path: " + path);
		       
		return false;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) 
	{
		String id = null;
		
		// We need to open the database.
		if(firstRun)
		{
			if(!path.isEmpty())
			{
				Log.d(TAG, "Attempting to open database.");
				db = SQLiteDatabase.openDatabase(path, null, 0);
			}
			else
				Log.d(TAG, "No database to open!!!!");
			
			firstRun = false;
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		SharedPreferences prefs = cont.getSharedPreferences("com.vanhlebarsoftware.kmmdroid_preferences", Context.MODE_WORLD_READABLE);
		accountUsed = prefs.getString("accountUsed", "");
		Log.d(TAG, "accountUsed: " + accountUsed);
		Log.d(TAG, "Starting query on CONTENT_URI: " + uri.toString());
		// See which content uri is requested.
		int match = sURIMatcher.match(uri);
		switch(match)
		{
			case ACCOUNTS:
				dbTable = accountsTable;
				dbColumns = accountsColumns;
				dbSelection = accountsSelection;
				dbOrderBy = accountsOrderBy;
				break;
			case ACCOUNTS_ID:
				dbTable = accountsTable;
				dbColumns = accountsColumns;
				dbSelection = accountsSingleSelection;
				dbOrderBy = accountsOrderBy;
				id = this.getId(uri);
				break;
			case SCHEDULES:
				dbTable = schedulesTable;
				dbColumns = schedulesColumns;
				dbSelection = schedulesSelection + "'" + accountUsed + "'";
				dbOrderBy = schedulesOrderBy;
				break;
			case SCHEDULES_ID:
				break;
			default:
				break;
		}
		
		if(id == null)
		{
			if(db.isOpen())
			{
				return db.query(dbTable, dbColumns, dbSelection, null, null, null, dbOrderBy);
			}
			else
			{	
				Log.d(TAG, "Database is not open!");
				return null;
			}
		}
		else
		{
			if(db.isOpen())
				return db.query(dbTable, dbColumns, dbSelection, new String[] { id }, null, null, null);
			else
			{
				Log.d(TAG, "Database is not open!");
				return null;
			}
		}
	}

	@Override
	public int update(Uri arg0, ContentValues arg1, String arg2, String[] arg3) 
	{
		// TODO Auto-generated method stub
		return 0;
	}

	private String getId(Uri uri)
	{
		String lastPathSegment = uri.getLastPathSegment();
		
		int match = sURIMatcher.match(uri);
		Log.d(TAG, "Matcher returned: " + match);
		switch(match)
		{
		case ACCOUNTS:
			return null;
		case ACCOUNTS_ID:
			return lastPathSegment;
		case SCHEDULES:
			return null;
		case SCHEDULES_ID:
			return lastPathSegment;
		default:
			return null;
		}		
		
		/*if(!lastPathSegment.equals("account") || !lastPathSegment.equals("schedule"))
		{
			try
			{
				return lastPathSegment;
			}
			catch(NumberFormatException e)
			{
				// at least we tried
			}
		}*/
	}
}
