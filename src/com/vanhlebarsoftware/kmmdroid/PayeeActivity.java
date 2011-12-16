package com.vanhlebarsoftware.kmmdroid;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AlphabetIndexer;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.SimpleCursorAdapter;
import android.widget.AdapterView.OnItemClickListener;
import android.util.Log;

public class PayeeActivity extends Activity
{
	private static final String TAG = "PayeeActivity";
	private static final int ACTION_NEW = 1;
	private static final int ACTION_EDIT = 2;
	private static final int C_PAYEENAME = 0;
	private static final int C_ID = 1;
	private static final String dbTable = "kmmPayees";
	private static final String[] dbColumns = { "name", "id AS _id"};
	private static final String strOrderBy = "name ASC";
	static final String[] FROM = { "name" };
	static final int[] TO = { R.id.prPayeeName };
	private String selectedPayeeId = null;
	private String selectedPayeeName = null;
	KMMDroidApp KMMDapp;
	Cursor cursor;
	ListView listPayees;
	SimpleCursorAdapter adapter;
	
	/* Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
        setContentView(R.layout.payee);
        
        // Get our application
        KMMDapp = ((KMMDroidApp) getApplication());
        
        // Find our views
        listPayees = (ListView) findViewById(R.id.listPayeesView);
		listPayees.setFastScrollEnabled(true);
		
    	// Now hook into listAccounts ListView and set its onItemClickListener member
    	// to our class handler object.
        listPayees.setOnItemClickListener(mMessageClickedHandler);

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
		
		// Make sure the edit and delete buttons are not visible and no payee is selected.
		// This is to control the menu items.
		selectedPayeeId = null;
		selectedPayeeName = null;
		
		//Get all the accounts to be displayed.
		cursor = KMMDapp.db.query(dbTable, dbColumns, null, null, null, null, strOrderBy);
		startManagingCursor(cursor);
		
		// Set up the adapter
		//adapter = new SimpleCursorAdapter(this, R.layout.payee_row, cursor, FROM, TO);
		listPayees.setAdapter(
				new KMMDCursorAdapter(
						getApplicationContext(),
						R.layout.payee_row,
						cursor, FROM, TO));
	}
		
	// Message Handler for our listAccounts List View clicks
	private OnItemClickListener mMessageClickedHandler = new OnItemClickListener() {
	    public void onItemClick(AdapterView<?> parent, View v, int position, long id)
	    {
	    	cursor.moveToPosition(position);
	    	selectedPayeeId = cursor.getString(C_ID);
	    	selectedPayeeName = cursor.getString(C_PAYEENAME);
			Intent i = new Intent(getBaseContext(), CreateModifyPayeeActivity.class);
			i.putExtra("Activity", ACTION_EDIT);
			i.putExtra("PayeeId", selectedPayeeId);
			i.putExtra("PayeeName", selectedPayeeName);
			startActivity(i);
	    }
	};
	
	// Called first time the user clicks on the menu button
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.payees_menu, menu);
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
			case R.id.itemCategories:
				startActivity(new Intent(this, CategoriesActivity.class));
				break;
			case R.id.itemInstitutions:
				startActivity(new Intent(this, InstitutionsActivity.class));
				break;
			case R.id.itemPrefs:
				startActivity(new Intent(this, PrefsActivity.class));
				break;
			case R.id.itemNew:
				AlertDialog.Builder alert = new AlertDialog.Builder(this);

				alert.setTitle(getString(R.string.createNewPayee));
				alert.setMessage(getString(R.string.msgPayeeName));

				// Set an EditText view to get user input 
				final EditText input = new EditText(this);
				alert.setView(input);

				alert.setPositiveButton(getString(R.string.titleButtonOK), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
				  String value = input.getText().toString();
				  // Do something with value!
					Intent i = new Intent(getBaseContext(), CreateModifyPayeeActivity.class);
					i.putExtra("Activity", ACTION_NEW);
					i.putExtra("PayeeName", value);
					startActivity(i);
				  }
				});

				alert.setNegativeButton(getString(R.string.titleButtonCancel), new DialogInterface.OnClickListener() {
				  public void onClick(DialogInterface dialog, int whichButton) {
				    // Canceled.
				  }
				});

				alert.show();
				break;	
			case R.id.itemAbout:
				startActivity(new Intent(this, AboutActivity.class));
				break;
		}
		return true;
	}
	
	class KMMDCursorAdapter extends SimpleCursorAdapter implements SectionIndexer
	{
		AlphabetIndexer alphaIndexer;
		
		public KMMDCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to)
		{
			super(context, layout, c, from, to);
			alphaIndexer = new AlphabetIndexer(c, cursor.getColumnIndex("name"), " ABCDEFGHIJKLMNOPQRSTUVWXYZ");
			
		}

		public int getPositionForSection(int section)
		{
			// TODO Auto-generated method stub
			return alphaIndexer.getPositionForSection(section);
		}

		public int getSectionForPosition(int position)
		{
			// TODO Auto-generated method stub
			return alphaIndexer.getSectionForPosition(position);
		}

		public Object[] getSections() 
		{
			// TODO Auto-generated method stub
			return alphaIndexer.getSections();
		}
	}
}
