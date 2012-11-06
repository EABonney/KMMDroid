package com.vanhlebarsoftware.kmmdroid;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;

public class CategoriesHierarchyActivity extends Activity
{
	private static final String TAG = "CategoriesGeneralActivity";
	private static final String[] dbColumns = { "accountName", "id AS _id"};
	private static final String strSelectionType = "accountTypeString=?";
	private static final String strOrderBy = "accountName ASC";
	static final String[] FROM = { "accountName" };
	static final int[] TO = { android.R.id.text1 };
	String strAccountType = null;
	String strParentAccount = null;
	int parentId = 0;
	private int numberOfPasses = 0;
	private CreateModifyCategoriesActivity parentTabHost;
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
        
        // Get the tabHost on the parent.
        parentTabHost = ((CreateModifyCategoriesActivity) this.getParent());
             
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
	
		strAccountType = CreateModifyCategoriesActivity.strType;

		//Get all the accounts to be displayed.
		cursor = KMMDapp.db.query("kmmAccounts", dbColumns, strSelectionType, 
				new String[] { strAccountType }, null, null, strOrderBy);
		startManagingCursor(cursor);
		
		// Set up the adapters
		adapter = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, cursor, FROM, TO);
		adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
		
		spinParent.setAdapter(adapter);
		
		Log.d(TAG, "inValidateParentId: " + String.valueOf(CreateModifyCategoriesActivity.inValidateParentId));
		// See if we need to clear out the parentId selection.
		if(CreateModifyCategoriesActivity.inValidateParentId)
			strParentAccount = null;
		
		// set the default position to either Income or Expense or to the already selected item.
		if(strParentAccount == null)
			spinParent.setSelection(setParentItem(strAccountType, 0));
		else
			spinParent.setSelection(setParentItem(strParentAccount, 1));
	}
	

	public class CategoryHierarchyOnItemSelectedListener implements OnItemSelectedListener
	{
		public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
		{
			if( numberOfPasses > 0 )
			{
				Cursor c = (Cursor) parent.getAdapter().getItem(pos);
				strParentAccount = c.getString(1);
				CreateModifyCategoriesActivity.inValidateParentId = false;
				parentTabHost.setIsDirty(true);
			}
			else
				numberOfPasses++;
		}

		public void onNothingSelected(AdapterView<?> arg0) {
			// do nothing.
		}		
	}
	
	@Override
	public void onBackPressed()
	{
		Log.d(TAG, "User clicked the back button");
		if( parentTabHost.getIsDirty() )
		{
			AlertDialog.Builder alertDel = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogNoTitle));
			alertDel.setTitle(R.string.BackActionWarning);
			alertDel.setMessage(getString(R.string.titleBackActionWarning));

			alertDel.setPositiveButton(getString(R.string.titleButtonOK), new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int whichButton)
				{
					finish();
				}
			});
			
			alertDel.setNegativeButton(getString(R.string.titleButtonCancel), new DialogInterface.OnClickListener() 
			{
				public void onClick(DialogInterface dialog, int whichButton) 
				{
					// Canceled.
					Log.d(TAG, "User cancelled back action.");
				}
			});				
			alertDel.show();
		}
		else
		{
			finish();
		}
	}	
	// ************************************************************************************************
	// ******************************* Helper Functions ***********************************************
	private int setParentItem(String type, int columnCompare)
	{
		int i = 0;
		
		Log.d(TAG, "type: " + type);
		Log.d(TAG, "columnCompare: " + String.valueOf(columnCompare));
		//Get all the accounts to be displayed.
		Cursor c = KMMDapp.db.query("kmmAccounts", dbColumns, strSelectionType, 
				new String[] { strAccountType }, null, null, strOrderBy);
		startManagingCursor(c);
		c.moveToFirst();
		
		if(c.getCount() > 0)
		{
			while(!type.equals(c.getString(columnCompare)))
			{
				Log.d(TAG, "compareItem: " + c.getString(columnCompare));
				c.moveToNext();
				i++;
			}
		}
		
		return i;
	}
	
	public String getParentAccount()
	{
		return strParentAccount;
	}
	
	public void putParentAccount(String id)
	{
		strParentAccount = id;
	}
}
