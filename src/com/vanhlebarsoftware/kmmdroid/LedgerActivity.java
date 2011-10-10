package com.vanhlebarsoftware.kmmdroid;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.database.Cursor;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.TextView;
import android.util.Log;

@SuppressWarnings("unused")
public class LedgerActivity extends Activity
{
	private static final String TAG = "LedgerActivity";
	private static final int C_TRANSID = 0;
	private static final int C_PAYEEID = 1;
	private static final int C_AMOUNT = 2;
	private static final int C_MEMO = 3;
	private static final int C_DATE = 4;
	private static final int C_PAYEE = 5;
	private static final String sql = "SELECT transactionId AS _id, payeeId, valueFormatted, memo, postDate, name FROM " +
					"kmmSplits, kmmPayees WHERE (kmmSplits.payeeID = kmmPayees.id AND accountId = ? AND txType = 'N')" +
					" UNION SELECT transactionId, payeeId, valueFormatted, memo, postDate, checkNumber FROM" +
					" kmmSplits WHERE payeeID IS NULL AND accountId = ? AND txType = 'N' ORDER BY postDate DESC";
	static final String[] FROM = { "valueFormatted", "postDate", "name", "memo" };
	static final int[] TO = { R.id.lrAmount, R.id.lrDate, R.id.lrDetails, R.id.lrBalance  };
	String AccountID = null;
	String AccountName = null;
	static float Balance = 0;
	Cursor cursor;
	SimpleCursorAdapter adapter;
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
        Balance = Float.valueOf(extras.getString("Balance"));
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

		// Display the Account we are looking at in the TextView TitleLedger
		textTitleLedger.setText(AccountName);

		// Put the AccountID into a String array
		String[] selectionArgs = { AccountID };
		
		//Run the query on the database to get the transactions.
		cursor = KMMDapp.db.rawQuery(sql, selectionArgs);
		startManagingCursor(cursor);
		
		// Set up the adapter
		adapter = new SimpleCursorAdapter(this, R.layout.ledger_row, cursor, FROM, TO);
		adapter.setViewBinder(VIEW_BINDER);
		listTransactions.setAdapter(adapter); 
	}
	
	// Message Handler for our listTransactions List View clicks
	private OnItemClickListener mMessageClickedHandler = new OnItemClickListener() {
	    public void onItemClick(AdapterView<?> parent, View v, int position, long id)
	    {
	    	cursor.moveToPosition(position);
	    	Intent i = new Intent(getBaseContext(), ViewTransactionActivity.class);
	    	i.putExtra("Description", cursor.getString(C_PAYEE));
	    	i.putExtra("Date", cursor.getString(C_DATE));
	    	i.putExtra("Memo", cursor.getString(C_MEMO));
	    	i.putExtra("Amount", cursor.getString(C_AMOUNT));
	    	i.putExtra("TransID", cursor.getString(C_TRANSID));
	    	startActivity(i);
	    }
	};
	
	// View binder to do formatting of the string values to numbers with commas and parenthesis
	static final ViewBinder VIEW_BINDER = new ViewBinder() {
		public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
			//if(view.getId() != R.id.lrAmount)
				//return false;

			switch (view.getId() )
			{
			case R.id.lrAmount:
				// Format the Amount properly.
				((TextView) view).setText(String.format("%,(.2f", Float.valueOf(cursor.getString(columnIndex))));
				return true;
			case R.id.lrBalance:
				// Insert the balance amount.
				//((TextView) view).setText(String.format("%,(.2f", Float.valueOf(Balance)));
				//Balance = calculateBalance(cursor.getString(columnIndex-1));
				return true;
			default:
				return false;
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
	
	// Called when an options item is clicked
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
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
		}
		
		return true;
	}
	
	private static float calculateBalance(String amount)
	{
		return Balance - Float.valueOf(amount);		
	}
}
