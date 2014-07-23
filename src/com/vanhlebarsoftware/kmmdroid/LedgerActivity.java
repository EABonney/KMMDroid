package com.vanhlebarsoftware.kmmdroid;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;

import android.os.Bundle;

import android.content.Intent;
import android.database.Cursor;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;

public class LedgerActivity extends FragmentActivity implements
								LoaderManager.LoaderCallbacks<List<Transaction>>
{
	private static final String TAG = LedgerActivity.class.getSimpleName();
	private static final int TRANSACTIONS_LOADER = 0x07;
	private static final int ACTION_NEW = 1;
	String AccountID = null;
	String AccountName = null;
	boolean bChangeBackground = false;
	boolean showAll = false;
	long Balance = 0;
	private int previousLocation = 0;
	private int prevTotalTrans = 0;
	Cursor cursor;
	TransactionAdapter adapter;
	KMMDroidApp KMMDapp;
	ListView listTransactions;
	//TextView textTitleLedger;
	KMMDCustomFastScrollView fastScrollView;
	
	// Items for the transactions query
	private Calendar today = null; // = Calendar.getInstance();
	private String strToday = null;
	private Calendar lastyear = null; //(Calendar) today.clone();
	private String strLastYear = null;
	String[] selectionArgs = { null, null, null };
	
	/* Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.ledger);

        // Get our application
        KMMDapp = ((KMMDroidApp) getApplication());
        
        // Find our views
        listTransactions = (ListView) findViewById(R.id.listTransactions);
        //textTitleLedger = (TextView) findViewById(R.id.titleLedger);
        fastScrollView = (KMMDCustomFastScrollView) findViewById(R.id.fast_scroll_view);
 
    	// Now hook into listTransactions ListView and set its onItemClickListener member
    	// to our class handler object.
        listTransactions.setOnItemClickListener(mMessageClickedHandler);
        
        // See if the database is already open, if not open it Read/Write.
        if(!KMMDapp.isDbOpen())
        {
        	KMMDapp.openDB();
        }
        
        // Get the AccountId
        Bundle extras = getIntent().getExtras();
        AccountID = extras.getString("AccountId");
        AccountName = extras.getString("AccountName");
        Balance = Transaction.convertToPennies(extras.getString("Balance"));
        
        // Setup our baseline of one year for the transactions query.
        today = Calendar.getInstance();
		strToday = getDatabaseFormattedString(today);
        today.add(Calendar.YEAR, -1);
        lastyear = (Calendar) today.clone();
        strLastYear = getDatabaseFormattedString(lastyear);
        
		String selection[] = { AccountID, strToday, strLastYear, AccountID, strToday, strLastYear };
		selectionArgs = selection;
		Bundle bundle = new Bundle();
		bundle.putStringArray("selectionArgs", selectionArgs);
		bundle.putBoolean("showAll", showAll);
		
		// Setup the blank adapter
		adapter = new TransactionAdapter(this, R.layout.ledger_row, new ArrayList<Transaction>());
		listTransactions.setAdapter(adapter);
		
        // Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
        getSupportLoaderManager().initLoader(TRANSACTIONS_LOADER, bundle, this);
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
	}
	
	@Override
	protected void onResume()
	{
		super.onResume();
		
		strLastYear = getDatabaseFormattedString(lastyear);
		
		// Display the Account we are looking at in the TextView TitleLedger
		//textTitleLedger.setText(AccountName);
		
        if( previousLocation == 0 )
        	previousLocation = adapter.getCount();
        else if( previousLocation == prevTotalTrans )
        	previousLocation = adapter.getCount() - prevTotalTrans;
        
		//listTransactions.setSelection(previousLocation);
		
		prevTotalTrans = adapter.getCount();
	}
	
	// Message Handler for our listTransactions List View clicks
	private OnItemClickListener mMessageClickedHandler = new OnItemClickListener() {
	    public void onItemClick(AdapterView<?> parent, View v, int position, long id)
	    {
	    	Transaction trans = adapter.getItem(position);
	    	
	    	if(trans.getTransId().equals("999999"))
	    	{
	    		Intent i = new Intent(getBaseContext(), LoadMoreTransactionsActivity.class);
				startActivityForResult(i, 0);
				previousLocation = adapter.getCount();
	    	}
	    	else
	    	{
	    		Intent i = new Intent(getBaseContext(), ViewTransactionActivity.class);
	    		i.putExtra("transactionId", trans.getTransId());
	    		i.putExtra("fromWidgetId", "9999");
	    		startActivity(i);
	    		previousLocation = position;
	    	}
	    }
	};
	
	// Called first time the user clicks on the menu button
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.ledger_menu, menu);
		return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu (Menu menu)
	{		
		return true;
	}
	
	// Called when an options item is clicked
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.itemNew:
				Intent i = new Intent(getBaseContext(), CreateModifyTransactionActivity.class);
				i.putExtra("Action", ACTION_NEW);
				i.putExtra("accountUsed", AccountID);
				i.putExtra("fromWidgetId", "9999");
				startActivity(i);
				break;
			case R.id.itemPrefs:
				startActivity(new Intent(this, PrefsActivity.class));
				break;
			case R.id.itemAbout:
				startActivity(new Intent(this, AboutActivity.class));
				break;
		}
		
		return true;
	}
	
    @Override
    protected void onActivityResult(int pRequestCode, int resultCode, Intent data)
    {    	
    	if( resultCode != -1)
    	{ 		
    		String response = data.getStringExtra("LoadMore");
    		if(response.equalsIgnoreCase("Month"))
    			lastyear.add(Calendar.MONTH, -1);
    		else if(response.equalsIgnoreCase("Year"))
    			lastyear.add(Calendar.YEAR, -1);
    		else if(response.equalsIgnoreCase("All"))
    			showAll = true;
    		else
    			Log.d(TAG, "Unexpected result returned from LoadMoreTransactionsActivity!");
    		
            strLastYear = String.valueOf(lastyear.get(Calendar.YEAR)) + "-" + String.valueOf(lastyear.get(Calendar.MONTH) + 1) + "-" +
    				String.valueOf(lastyear.get(Calendar.DAY_OF_MONTH));
            
            // refresh the loader.
    		// Put the AccountID into a String array
    		if( !showAll )
    		{
    			String[] array = { AccountID, strToday, strLastYear };
    			selectionArgs = array;
    		}
    		else
    		{
    			String selection[] = { AccountID };
    			selectionArgs = selection;
    		}
    		Bundle bundle = new Bundle();
    		bundle.putStringArray("selectionArgs", selectionArgs);
    		bundle.putBoolean("showAll", showAll);
    		
    		getSupportLoaderManager().restartLoader(TRANSACTIONS_LOADER, bundle, this);
    	}
    }
	
	public class TransactionComparator implements Comparator<Transaction>
	{
		public int compare(Transaction arg0, Transaction arg1) 
		{
			return arg0.getDate().compareTo(arg1.getDate());
		}
	}
	
	private String getDatabaseFormattedString(Calendar date)
	{
		String strDay = null;
		int intDay = date.get(Calendar.DAY_OF_MONTH);
		String strMonth = null;
		int intMonth = date.get(Calendar.MONTH);

		switch(intDay)
		{
			case 0:
			case 1:
			case 2:
			case 3:
			case 4:
			case 5:
			case 6:
			case 7:
			case 8:
			case 9:
				strDay = "0" + String.valueOf(intDay);
				break;
			default:
				strDay = String.valueOf(intDay);
			break;
		}
		
		switch(intMonth)
		{
			case 0:
			case 1:
			case 2:
			case 3:
			case 4:
			case 5:
			case 6:
			case 7:
			case 8:
				strMonth = "0" + String.valueOf(intMonth + 1);
				break;
			default:
				strMonth = String.valueOf(intMonth + 1);
				break;
		}
		
		return String.valueOf(date.get(Calendar.YEAR) + "-" + strMonth + "-" + strDay);
	}

	public Loader<List<Transaction>> onCreateLoader(int id, Bundle args) 
	{
		setProgressBarIndeterminateVisibility(true);
		return new TransactionsLoader(this, args);
	}

	public void onLoadFinished(Loader<List<Transaction>> loader, List<Transaction> transactions) 
	{
		adapter.setData(transactions);
    	adapter.refreshSections();
		fastScrollView.listItemsChanged();
		if(previousLocation == 0)
			previousLocation = adapter.getCount();
		
		listTransactions.setSelection(previousLocation);
		setProgressBarIndeterminateVisibility(false);
		
		Log.d(TAG, "Got all our transactions to display!");
	}

	public void onLoaderReset(Loader<List<Transaction>> loader) 
	{	
		adapter.setData(null);
	}
}
