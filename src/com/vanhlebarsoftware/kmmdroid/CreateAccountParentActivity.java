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

public class CreateAccountParentActivity extends Activity
{
	private final String TAG = CreateAccountParentActivity.class.getSimpleName();
	static private String strSelection = null;
	static private String strParentId = null;
	Spinner spinParent;
	Cursor cursor;
	SimpleCursorAdapter adapter;
	KMMDroidApp KMMDapp;
	private boolean firstRun = true;
	private CreateModifyAccountActivity parentTabHost;
	
	@Override
    public void onCreate(Bundle savedInstanceState) 
	{
        super.onCreate(savedInstanceState);
        setContentView(R.layout.createaccount_parent);
        
        // Get our application
        KMMDapp = ((KMMDroidApp) getApplication());
        
        // Get the activity for the tabHost.
        parentTabHost = ((CreateModifyAccountActivity) this.getParent());
        
        // Find our views
        spinParent = (Spinner) findViewById(R.id.accountSubAccount);
        
        // Set our listeners for our items.
        spinParent.setOnItemSelectedListener(new AccountParentOnItemSelectedListener());
        
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
		
		cursor = KMMDapp.db.query("kmmAccounts", new String[] { "id AS _id", "accountName" }, strSelection, null, null, null, null);
		startManagingCursor(cursor);
		
		// Set up the adapters
		adapter = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, cursor, 
				new String[] { "accountName" }, new int[] { android.R.id.text1 });
		adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
		spinParent.setAdapter(adapter);
		
		// Set the spinner location.
		if( strParentId != null )
			spinParent.setSelection(setParent(strParentId));
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

	public class AccountParentOnItemSelectedListener implements OnItemSelectedListener
	{
		public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
		{
			if( !firstRun )
			{
				Cursor c = (Cursor) parent.getAdapter().getItem(pos);			
				strParentId = c.getString(0);
				parentTabHost.setIsDirty(true);
			}
			else
				firstRun = false;
		}

		public void onNothingSelected(AdapterView<?> arg0) {
			// do nothing.
		}		
	}
	// ***********************************************************************************************
	// ********************************* Helper Functions ********************************************
	private int setParent(String parentId)
	{
		int i = 0;
		cursor.moveToFirst();
		
		if( parentId != null )
		{
			while(!parentId.equals(cursor.getString(0)))
			{
				cursor.moveToNext();
			
				//check to see if we have moved past the last item in the cursor, if so return current i.
				if(cursor.isAfterLast())
					return i;
			
				i++;
			}
		}
		return i;
	}
	
	static public void setSelected(String selection)
	{
		strSelection = selection;
	}
	
	public String getParentName()
	{
		return strSelection;
	}
	
	public String getParentId()
	{
		return strParentId;
	}
	
	public void putParentName(String name)
	{
		strSelection = name;
	}
	
	public void putParentId(String id)
	{
		strParentId = id;
	}
}
