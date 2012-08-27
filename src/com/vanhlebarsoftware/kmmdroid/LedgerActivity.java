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
import android.graphics.Color;
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
	private static final int C_STATUS = 6;
	private static final String sql = "SELECT transactionId AS _id, payeeId, valueFormatted, memo, postDate, name, reconcileFlag FROM " +
					"kmmSplits, kmmPayees WHERE (kmmSplits.payeeID = kmmPayees.id AND accountId = ? AND txType = 'N') " + 
					"AND postDate <= ? AND postDate >= ?" +
					" UNION SELECT transactionId, payeeId, valueFormatted, memo, postDate, checkNumber, reconcileFlag FROM" +
					" kmmSplits WHERE payeeID IS NULL AND accountId = ? AND txType = 'N' AND postDate <= ? AND postDate >= ?";
	static final String[] FROM = { "valueFormatted", "postDate", "name", "memo" };
	static final int[] TO = { R.id.lrAmount, R.id.lrDate, R.id.lrDetails, R.id.lrBalance  };
	String AccountID = null;
	String AccountName = null;
	boolean bChangeBackground = false;
	long Balance = 0;
	ArrayList<Transaction> Transactions;
	Cursor cursor;
	TransactionAdapter adapter;
	KMMDroidApp KMMDapp;
	ListView listTransactions;
	TextView textTitleLedger;
	
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
		Calendar today = Calendar.getInstance();
		long balance = Balance;
		Cursor curBalance = KMMDapp.db.query("kmmAccounts", new String[] { "balanceFormatted" }, "id=?", new String[] { AccountID },
											null, null, null);
		startManagingCursor(curBalance);
		curBalance.moveToFirst();
		balance = Transaction.convertToPennies(curBalance.getString(0));
		
		// Get today's date and then subtract one year to limit the rows in our view.
		String strToday = String.valueOf(today.get(Calendar.YEAR)) + "-" + String.valueOf(today.get(Calendar.MONTH) + 1) + "-" +
							String.valueOf(today.get(Calendar.DAY_OF_MONTH));
		today.add(Calendar.YEAR, -1);
		Calendar lastyear = (Calendar) today.clone();
		String strLastYear = String.valueOf(lastyear.get(Calendar.YEAR)) + "-" + String.valueOf(lastyear.get(Calendar.MONTH) + 1) + "-" +
				String.valueOf(lastyear.get(Calendar.DAY_OF_MONTH));
		
		// Display the Account we are looking at in the TextView TitleLedger
		textTitleLedger.setText(AccountName);

		// Put the AccountID into a String array
		String[] selectionArgs = { AccountID, strToday, strLastYear };
		
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
			trans = new Transaction(cursor.getString(C_AMOUNT), cursor.getString(C_PAYEE), cursor.getString(C_DATE), cursor.getString(C_MEMO), 
									cursor.getString(C_TRANSID), cursor.getString(C_STATUS));
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
	}
	
	// Message Handler for our listTransactions List View clicks
	private OnItemClickListener mMessageClickedHandler = new OnItemClickListener() {
	    public void onItemClick(AdapterView<?> parent, View v, int position, long id)
	    {
	    	Intent i = new Intent(getBaseContext(), ViewTransactionActivity.class);
	    	Transaction trans = Transactions.get(position);
	    	i.putExtra("Description", trans.getPayee());
	    	i.putExtra("Date", trans.formatDateString());
	    	i.putExtra("Memo", trans.getMemo());
	    	i.putExtra("Amount", Transaction.convertToDollars(trans.getAmount()));
	    	i.putExtra("TransID", trans.getTransId());
	    	i.putExtra("Status", trans.getStatus());	    	
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
				
				DatePaid.setText(item.formatDateString());
				Payee.setText(item.getPayee());
				Amount.setText(Transaction.convertToDollars(item.getAmount()));
				Balance.setText(Transaction.convertToDollars(item.getBalance()));
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
}
