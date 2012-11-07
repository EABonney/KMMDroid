package com.vanhlebarsoftware.kmmdroid;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import android.app.Activity;
import android.os.Bundle;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.TextView;
import android.util.Log;

public class LedgerActivity extends Activity
{
	private static final String TAG = "LedgerActivity";
	private static final int ACTION_NEW = 1;
	private static final int ACTION_EDIT = 2;
	private static final int C_TRANSID = 0;
	private static final int C_PAYEEID = 1;
	private static final int C_AMOUNT = 2;
	private static final int C_MEMO = 3;
	private static final int C_DATE = 4;
	private static final int C_PAYEE = 5;
	private static final int C_CKNUM = 6;
	private static final int C_STATUS = 7;
	private String sql = "SELECT transactionId AS _id, payeeId, value, memo, postDate, name, checkNumber, reconcileFlag FROM " +
					"kmmSplits, kmmPayees WHERE (kmmSplits.payeeID = kmmPayees.id AND accountId = ? AND txType = 'N') " + 
					"AND postDate <= ? AND postDate >= ?" +
					" UNION SELECT transactionId, payeeId, value, memo, postDate, bankId, checkNumber, reconcileFlag FROM" +
					" kmmSplits WHERE payeeID IS NULL AND accountId = ? AND txType = 'N' AND postDate <= ? AND postDate >= ?";
	static final String[] FROM = { "valueFormatted", "postDate", "name", "memo" };
	static final int[] TO = { R.id.lrAmount, R.id.lrDate, R.id.lrDetails, R.id.lrBalance  };
	String AccountID = null;
	String AccountName = null;
	boolean bChangeBackground = false;
	boolean showAll = false;
	long Balance = 0;
	ArrayList<Transaction> Transactions;
	Cursor cursor;
	TransactionAdapter adapter;
	KMMDroidApp KMMDapp;
	ListView listTransactions;
	TextView textTitleLedger;
	
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
        setContentView(R.layout.ledger);

        // Get our application
        KMMDapp = ((KMMDroidApp) getApplication());
        
        // Find our views
        listTransactions = (ListView) findViewById(R.id.listTransactions);
        textTitleLedger = (TextView) findViewById(R.id.titleLedger);
        
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
        
        Transactions = new ArrayList<Transaction>();
        
        // Setup our baseline of one year for the transactions query.
        today = Calendar.getInstance();
        //strToday = String.valueOf(today.get(Calendar.YEAR)) + "-" + String.valueOf(today.get(Calendar.MONTH) + 1) + "-" +
				//String.valueOf(today.get(Calendar.DAY_OF_MONTH));
		strToday = getDatabaseFormattedString(today);
        today.add(Calendar.YEAR, -1);
        lastyear = (Calendar) today.clone();
        //strLastYear = String.valueOf(lastyear.get(Calendar.YEAR)) + "-" + String.valueOf(lastyear.get(Calendar.MONTH) + 1) + "-" +
				//String.valueOf(lastyear.get(Calendar.DAY_OF_MONTH));
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
		long balance = Balance;
		Cursor curBalance = KMMDapp.db.query("kmmAccounts", new String[] { "balance" }, "id=?", new String[] { AccountID },
											null, null, null);
		startManagingCursor(curBalance);
		curBalance.moveToFirst();
		balance = Account.convertBalance(curBalance.getString(0));
		
		// Display the Account we are looking at in the TextView TitleLedger
		textTitleLedger.setText(AccountName);

		// Put the AccountID into a String array
		if( !showAll )
		{
			selectionArgs[0] = AccountID;
			selectionArgs[1] = strToday;
			selectionArgs[2] = strLastYear;
		}
		else
		{
			String selection[] = { AccountID };
			selectionArgs = selection;
		}
		
        Log.d(TAG, "strToday: " + strToday + " / strLastYear: " + strLastYear);
		//Run the query on the database to get the transactions.
		cursor = KMMDapp.db.rawQuery(sql, selectionArgs);
		startManagingCursor(cursor);
		
		// Load up our transactions into our ArrayList
		// This probably is extremely ineffecient, may want to review this better.
		Transaction trans = null;
		cursor.moveToFirst();
		// Make sure Transactions are empty.
		Transactions.clear();
		
		for(int i=0; i < cursor.getCount(); i++)
		{
			trans = new Transaction(Transaction.convertToDollars(Account.convertBalance(cursor.getString(C_AMOUNT)), true),
									cursor.getString(C_PAYEE), cursor.getString(C_DATE), cursor.getString(C_MEMO), 
									cursor.getString(C_TRANSID), cursor.getString(C_STATUS), cursor.getString(C_CKNUM));
			Transactions.add(trans);

			cursor.moveToNext();
		}
		
		// Ensure that we are in date order.
		TransactionComparator comparator = new TransactionComparator();
		Collections.sort(Transactions, comparator);	
		
		// Calc the balances now.
		for(int i = Transactions.size() - 1; i >= 0; i--)
		{
			if(i == Transactions.size() - 1)
				Transactions.get(i).setBalance(balance);
			else if (i == 0)
				balance = Transactions.get(i).calcBalance(balance, 0);
			else
				balance = Transactions.get(i).calcBalance(balance, Transactions.get(i+1).getAmount());
		}
		
		// Set up the adapter
		adapter = new TransactionAdapter(this, R.layout.ledger_row, Transactions);
		listTransactions.setAdapter(adapter); 
		
		listTransactions.setSelection(Transactions.size());
		
		// Close the cursor to free up memory.
		cursor.close();
		
		Log.d(TAG, "Number of transactions displayed: " + Transactions.size());
	}
	
	// Message Handler for our listTransactions List View clicks
	private OnItemClickListener mMessageClickedHandler = new OnItemClickListener() {
	    public void onItemClick(AdapterView<?> parent, View v, int position, long id)
	    {
	    	Intent i = new Intent(getBaseContext(), ViewTransactionActivity.class);
	    	Transaction trans = Transactions.get(position);
	    	i.putExtra("transactionId", trans.getTransId());
	    	startActivity(i);
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
		// Once the user has elected to showAll, disable the Load more menu item.
		if(showAll)
			menu.getItem(1).setVisible(false);
		
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
				startActivity(i);
				break;
			case R.id.itemLoadMore:
				i = new Intent(this, LoadMoreTransactionsActivity.class);
				startActivityForResult(i, 0);
				break;
			case R.id.itemHome:
				startActivity(new Intent(this, HomeActivity.class));
				break;
			case R.id.itemAccounts:
				startActivity(new Intent(this, AccountsActivity.class));
				break;
			case R.id.itemInstitutions:
				startActivity(new Intent(this, InstitutionsActivity.class));
				break;
			case R.id.itemPayees:
				startActivity(new Intent(this, PayeeActivity.class));
				break;
			case R.id.itemCategories:
				startActivity(new Intent(this, CategoriesActivity.class));
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
    		{
    			Log.d(TAG, "User wants us to add one month.");
    			lastyear.add(Calendar.MONTH, -1);
    		}
    		else if(response.equalsIgnoreCase("Year"))
    		{
    			Log.d(TAG, "User wants us to add one year.");
    			lastyear.add(Calendar.YEAR, -1);
    		}
    		else if(response.equalsIgnoreCase("All"))
    		{
    			Log.d(TAG, "User wants us to add all.");
    			sql = "SELECT transactionId AS _id, payeeId, value, memo, postDate, name, checkNumber, reconcileFlag FROM " +
    					"kmmSplits, kmmPayees WHERE (kmmSplits.payeeID = kmmPayees.id AND accountId = ? AND txType = 'N') " + 
    					//"AND postDate <= ? AND postDate >= ?" +
    					" UNION SELECT transactionId, payeeId, value, memo, postDate, bankId, checkNumber, reconcileFlag FROM" +
    					" kmmSplits WHERE payeeID IS NULL AND accountId = ? AND txType = 'N'";// AND postDate <= ? AND postDate >= ?";
    			showAll = true;
    		}
    		else
    			Log.d(TAG, "Unexpected result returned from LoadMoreTransactionsActivity!");
    		
            strLastYear = String.valueOf(lastyear.get(Calendar.YEAR)) + "-" + String.valueOf(lastyear.get(Calendar.MONTH) + 1) + "-" +
    				String.valueOf(lastyear.get(Calendar.DAY_OF_MONTH));
    	}
    }
    
	private class TransactionAdapter extends ArrayAdapter<Transaction>
	{
		private ArrayList<Transaction> items;
		private Context context;
		
		public TransactionAdapter(Context context, int textViewResourceId, ArrayList<Transaction> items)
		{
			super(context, textViewResourceId, items);
			this.context = context;
			this.items = items;
		}
		
		public View getView(int position, View convertView, ViewGroup parent)
		{
			View view = convertView;
			if(view == null)
			{
				LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				view = inflater.inflate(R.layout.ledger_row, null);
			}
			
			Transaction item = items.get(position);
			// Load the items into the view now for this schedule.
			if(item != null)
			{
				TextView DatePaid = (TextView) view.findViewById(R.id.lrDate);
				TextView Payee = (TextView) view.findViewById(R.id.lrDetails);
				TextView Amount = (TextView) view.findViewById(R.id.lrAmount);
				TextView Balance = (TextView) view.findViewById(R.id.lrBalance);
				LinearLayout row = (LinearLayout) view.findViewById(R.id.row);

				if(bChangeBackground)
				{
					row.setBackgroundColor(Color.rgb(0x62, 0xB1, 0xF6));
					bChangeBackground = false;
				}
				else
				{
					row.setBackgroundColor(Color.rgb(0x62, 0xa1, 0xc6));
					bChangeBackground = true;
				}
				
				// See if this is a future transaction, if so change to italics.
				if(item.isFuture())
				{
					DatePaid.setTypeface(Typeface.defaultFromStyle(Typeface.ITALIC), Typeface.ITALIC);
					DatePaid.setTextColor(Color.LTGRAY);
					Payee.setTypeface(Typeface.defaultFromStyle(Typeface.ITALIC), Typeface.ITALIC);
					Payee.setTextColor(Color.LTGRAY);
					Amount.setTypeface(Typeface.defaultFromStyle(Typeface.ITALIC), Typeface.ITALIC);
					Amount.setTextColor(Color.LTGRAY);
					Balance.setTypeface(Typeface.defaultFromStyle(Typeface.ITALIC), Typeface.ITALIC);
					Balance.setTextColor(Color.LTGRAY);
				}
				else
				{
					DatePaid.setTypeface(Typeface.DEFAULT);
					DatePaid.setTextColor(Color.BLACK);
					Payee.setTypeface(Typeface.DEFAULT);
					Payee.setTextColor(Color.BLACK);
					Amount.setTypeface(Typeface.DEFAULT);
					Amount.setTextColor(Color.BLACK);
					Balance.setTypeface(Typeface.DEFAULT);
					Balance.setTextColor(Color.BLACK);
				}
				
				DatePaid.setText(item.formatDateString());
				Payee.setText(item.getPayee());
				Amount.setText(Transaction.convertToDollars(item.getAmount(), true));
				Balance.setText(Transaction.convertToDollars(item.getBalance(), true));
			}
			else
				Log.d(TAG, "Never got a Schedule!");
			
			return view;
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
}
