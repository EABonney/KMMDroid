package com.vanhlebarsoftware.kmmdroid;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class SchedulesActivity extends Activity
{
	private static final int ACTION_NEW = 1;
	private static final int ACTION_EDIT = 2;
	private static final String dbTable = "kmmSchedules, kmmSplits, kmmPayees";
	private static final String[] dbColumns = { "kmmSchedules.id AS _id", "kmmSchedules.name AS Description", "occurenceString", "nextPaymentDue", 
												"endDate", "lastPayment", "valueFormatted", "kmmPayees.name AS Payee" };
	private static final String strSelection = "kmmSchedules.id = kmmSplits.transactionId AND kmmSplits.payeeId = kmmPayees.id AND nextPaymentDue > 0" + 
												" AND ((occurenceString = 'Once' AND lastPayment IS NULL) OR occurenceString != 'Once')";
	private static final String strOrderBy = "nextPaymentDue ASC";
	static final String[] FROM = { "Description", "occurenceString", "nextPaymentDue", "valueFormatted", "Payee" };
	static final int[] TO = { R.id.srDescription, R.id.srFrequency, R.id.srNextDueDate, R.id.srAmount, R.id.srPayee };
	KMMDroidApp KMMDapp;
	Cursor cursor;
	ListView listSchedules;
	ScheduleCursorAdapter adapter;
	
	/* Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
        setContentView(R.layout.schedules);

        // Get our application
        KMMDapp = ((KMMDroidApp) getApplication());
        
        // Find our views
        listSchedules = (ListView) findViewById(R.id.listSchedules);
        
    	// Now hook into listTransactions ListView and set its onItemClickListener member
    	// to our class handler object.
        listSchedules.setOnItemClickListener(mMessageClickedHandler);
        
        // See if the database is already open, if not open it Read/Write.
        if(!KMMDapp.isDbOpen())
        {
        	KMMDapp.openDB();
        }
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
		
        // See if the database is already open, if not open it Read/Write.
        if(!KMMDapp.isDbOpen())
        {
        	KMMDapp.openDB();
        }
        
		//Run the query on the database to get the transactions.
		cursor = KMMDapp.db.query(dbTable, dbColumns, strSelection, null, null, null, strOrderBy, null);
		startManagingCursor(cursor);
		
		// Set up the adapter
		adapter = new ScheduleCursorAdapter(this, R.layout.schedules_rows, cursor, FROM, TO);
		listSchedules.setAdapter(adapter); 
	}
	
	// Message Handler for our listTransactions List View clicks
	private OnItemClickListener mMessageClickedHandler = new OnItemClickListener() {
	    public void onItemClick(AdapterView<?> parent, View v, int position, long id)
	    {
	    	Intent i = new Intent(getBaseContext(), ScheduleActionsActivity.class);
	    	Cursor sch = (Cursor) parent.getAdapter().getItem(position);
	    	i.putExtra("scheduleId", sch.getString(0));	
	    	i.putExtra("scheduleDescription", sch.getString(1));
	    	i.putExtra("Action", 3);
	    	startActivity(i);
	    }
	};
	
	// Called first time the user clicks on the menu button
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.schedules_menu, menu);
		return true;
	}
	
	// Called when an options item is clicked
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.itemNew:
				Intent i = new Intent(this, CreateModifyScheduleActivity.class);
				i.putExtra("Action", ACTION_NEW);
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
	
	public class ScheduleCursorAdapter extends SimpleCursorAdapter
	{
		private Cursor c;
		private Context context;
		
		public ScheduleCursorAdapter(Context context, int layout, Cursor c,
				String[] from, int[] to)
		{
			super(context, layout, c, from, to);
			this.c = c;
			this.context = context;
		}
		
		public View getView(int pos, View inView, ViewGroup parent)
		{
			View view = inView;
			TextView txtDesc;
			TextView txtOccurence;
			TextView txtNextPaymentDue;
			TextView txtAmount;
			TextView txtPayee;
			
			if(view == null)
			{
				LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				view = inflater.inflate(R.layout.schedules_rows, null);
			}
			
			this.c.moveToPosition(pos);
			
			// Find our views
			txtDesc = (TextView) view.findViewById(R.id.srDescription);
			txtOccurence = (TextView) view.findViewById(R.id.srFrequency);
			txtNextPaymentDue = (TextView) view.findViewById(R.id.srNextDueDate);
			txtAmount = (TextView) view.findViewById(R.id.srAmount);
			txtPayee = (TextView) view.findViewById(R.id.srPayee);
			
			// load up the current record.
			txtDesc.setText(this.c.getString(1));
			txtOccurence.setText(this.c.getString(2));
			txtNextPaymentDue.setText(this.c.getString(3));
			txtAmount.setText(Transaction.convertToDollars(Transaction.convertToPennies(this.c.getString(6))));
			txtPayee.setText(this.c.getString(7));
			
			return view;
		}
	}
}