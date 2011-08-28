package com.vanhlebarsoftware.kmmdroid;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;

public class CategoriesHierarchyActivity extends Activity
{
	private static final String TAG = "CategoriesGeneralActivity";
	private static final String[] dbColumns = { "Accountname", "id AS _id"};
	private static final String strSelectionType = "accountTypeString='?' AND (balance != '0/1')";
	private static final String strOrderBy = "Accountname ASC";
	static final String[] FROM = { "Accountname" };
	static final int[] TO = { android.R.id.text1 };
	String strAccountType = null;
	Spinner spinParent;
	Cursor cursor;
	SimpleCursorAdapter adapter;
	KMMDroidApp KMMDapp;
	
	@Override
    public void onCreate(Bundle savedInstanceState) 
	{
        super.onCreate(savedInstanceState);
        setContentView(R.layout.categories_hierarchy);
        
        // Get our application
        KMMDapp = ((KMMDroidApp) getApplication());
             
        // Find our views
        spinParent = (Spinner) findViewById(R.id.categorySubAccount);
        
        // Set the OnItemSelectedListeners for the spinners.
        spinParent.setOnItemSelectedListener(new CategoryHierarchyOnItemSelectedListener());
        
        // See if the database is already open, if not open it Read/Write.
        if(!KMMDapp.isDbOpen())
        {
        	KMMDapp.openDB();
        }
        
	}
	
	@Override
	protected void onResume()
	{
		super.onResume();
	
		//Get all the accounts to be displayed.
		cursor = KMMDapp.db.query("kmmAccounts", dbColumns, strSelectionType, 
				new String[] { strAccountType }, null, null, strOrderBy);
		startManagingCursor(cursor);
		
		// Set up the adapters
		adapter = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, cursor, FROM, TO);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		
		spinParent.setAdapter(adapter);
	}
	
	public class CategoryHierarchyOnItemSelectedListener implements OnItemSelectedListener
	{
		public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
		{
			Cursor c = (Cursor) parent.getAdapter().getItem(pos);
			
			// TODO Auto-generated method stub
			switch ( parent.getId() )
			{
				default:
					Log.d(TAG, "Somehow it thinks we did not select an account but we did!");
					break;
			}
		}

		public void onNothingSelected(AdapterView<?> arg0) {
			// TODO Auto-generated method stub
			// do nothing.
		}		
	}
}
