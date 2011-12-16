package com.vanhlebarsoftware.kmmdroid;

import com.vanhlebarsoftware.kmmdroid.PayeeActivity.KMMDCursorAdapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
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

public class InstitutionsActivity extends Activity
{
	private static final String TAG = "InstitutionsActivity";
	private static final int ACTION_NEW = 1;
	private static final int ACTION_EDIT = 2;
	private static final int C_PAYEENAME = 0;
	private static final int C_ID = 1;
	private static final String dbTable = "kmmInstitutions";
	private static final String[] dbColumns = { "name", "id AS _id"};
	private static final String strOrderBy = "name ASC";
	static final String[] FROM = { "name" };
	static final int[] TO = { R.id.prPayeeName };
	private String selectedInstitutionId = null;
	private String selectedInstitutionName = null;
	KMMDroidApp KMMDapp;
	Cursor cursor;
	ListView listInstitutions;
	SimpleCursorAdapter adapter;
	
	/* Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
        setContentView(R.layout.institutions);
        
        // Get our application
        KMMDapp = ((KMMDroidApp) getApplication());
        
        // Find our views
        listInstitutions = (ListView) findViewById(R.id.listInstitutionsView);
        listInstitutions.setFastScrollEnabled(true);
        
    	// Now hook into listInstitutions ListView and set its onItemClickListener member
    	// to our class handler object.
        listInstitutions.setOnItemClickListener(mMessageClickedHandler);
        
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
		selectedInstitutionId = null;
		selectedInstitutionName = null;
		
		//Get all the accounts to be displayed.
		cursor = KMMDapp.db.query(dbTable, dbColumns, null, null, null, null, strOrderBy);
		startManagingCursor(cursor);
		
		// Set up the adapter
		//adapter = new SimpleCursorAdapter(this, R.layout.payee_row, cursor, FROM, TO);
		listInstitutions.setAdapter(
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
	    	selectedInstitutionId = cursor.getString(C_ID);
	    	selectedInstitutionName = cursor.getString(C_PAYEENAME);
	    	Log.d(TAG, "institutionId: " + selectedInstitutionId);
			Intent i = new Intent(getBaseContext(), CreateModifyInstitutionActivity.class);
			i.putExtra("Action", ACTION_EDIT);
			i.putExtra("instId", selectedInstitutionId);
			i.putExtra("InstitutionName", selectedInstitutionName);
			startActivity(i);
	    }
	};
	
	// Called first time the user clicks on the menu button
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.institutions_menu, menu);
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
			case R.id.itemPayees:
				startActivity(new Intent(this, PayeeActivity.class));
				break;
			case R.id.itemCategories:
				startActivity(new Intent(this, CategoriesActivity.class));
				break;
			case R.id.itemPrefs:
				startActivity(new Intent(this, PrefsActivity.class));
				break;
			case R.id.itemNew:
				Intent i = new Intent(getBaseContext(), CreateModifyInstitutionActivity.class);
				i.putExtra("Action", ACTION_NEW);
				startActivity(i);
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
