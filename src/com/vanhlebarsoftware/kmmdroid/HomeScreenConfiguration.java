package com.vanhlebarsoftware.kmmdroid;

import android.app.Activity;
import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Spinner;

public class HomeScreenConfiguration extends Activity 
{
	private int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
	static final String[] FROM = { "name" };
	static final int[] TO = { android.R.id.text1 };
	static final String[] FROM1 = { "accountName" };
	private String strAccountId = null;
	private int intDefaultFreq = 0;
	private int intNumOfWeeks = 0;
	TextView txtDatabasePath;
	Spinner spinDefaultAccount;
	Spinner spinDefaultFreq;
	Spinner spinNumOfWeeks;
	Button btnSelectFile;
	Button btnSave;
	ArrayAdapter<CharSequence> adapterDefaultFreq;
	ArrayAdapter<CharSequence> adapterNumOfWeeks;
	SimpleCursorAdapter adapterDefaultAccount;
	SQLiteDatabase db;
	KMMDroidApp KMMDapp;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
        setContentView(R.layout.homescreenconfiguration);
        
        // Get our application
        KMMDapp = ((KMMDroidApp) getApplication());
        
        // See if we have already setup a widget, if so then pop an alert and tell the user to bad.
        if( KMMDapp.prefs.getBoolean("homeWidgetSetup", false) )
        {
			AlertDialog.Builder alertDel = new AlertDialog.Builder(this);
			alertDel.setTitle(R.string.homeWidgetError);
			alertDel.setMessage(getString(R.string.titleHomeWidgetError));

			alertDel.setPositiveButton(getString(R.string.titleButtonOK), new DialogInterface.OnClickListener() 
			{
				public void onClick(DialogInterface dialog, int whichButton)
				{					
			        setResult(RESULT_CANCELED, null);
					finish();
				}
			});			
			alertDel.show();
        }
        
        // Get our views
        txtDatabasePath = (TextView) findViewById(R.id.homeConfigDatabaseTitle);
        spinDefaultAccount = (Spinner) findViewById(R.id.homeConfigDefaultAccountSpinner);
        spinDefaultFreq = (Spinner) findViewById(R.id.homeConfigDefaultUpdateFreqSpinner);
        spinNumOfWeeks = (Spinner) findViewById(R.id.homeConfigNumWeeksDisplaySpinner);
        btnSelectFile = (Button) findViewById(R.id.homeConfigDatabaseBtn);
        btnSave = (Button) findViewById(R.id.homeConfigSave);
        
        // Set our onClickListener events.
        btnSelectFile.setOnClickListener(new View.OnClickListener()
        {
			public void onClick(View arg0)
			{
    			Intent i = new Intent(arg0.getContext(), FileChooser.class);
    			startActivityForResult(i, 0);
			}
		});
        
        btnSave.setOnClickListener(new View.OnClickListener()
        {
			public void onClick(View arg0)
			{
		    	// Save the config settings for this WidgetId.
				SharedPreferences.Editor editor = KMMDapp.prefs.edit();
				editor.putString("widgetDatabasePath", txtDatabasePath.getText().toString());
				editor.putString("accountUsed", strAccountId);
				editor.putString("updateFrequency", String.valueOf(intDefaultFreq));
				editor.putString("displayWeeks", String.valueOf(intNumOfWeeks));
				editor.putBoolean("homeWidgetSetup", true);
				editor.apply();
		    	
		    	// Notify the Widget Manager that the config has completed.
		    	Intent result = new Intent();
		    	result.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
		    	setResult(RESULT_OK, result);
		    	
		    	// Make sure we refresh the new widget.
				Intent intent = new Intent(KMMDService.DATA_CHANGED);
				sendBroadcast(intent, KMMDService.RECEIVE_HOME_UPDATE_NOTIFICATIONS);
				
		    	finish();
			}
		});
        
        // Set the OnItemSelectedListeners for the spinners.
        spinDefaultAccount.setOnItemSelectedListener(new AccountOnItemSelectedListener());
        spinDefaultFreq.setOnItemSelectedListener(new AccountOnItemSelectedListener());
        spinNumOfWeeks.setOnItemSelectedListener(new AccountOnItemSelectedListener());
        
        // Get the specific appWidget the user is setting up.
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if( extras != null )
        	appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        
        // Set the result to canceled in case the user exits the activity without accepting the config changes.
        setResult(RESULT_CANCELED, null);
        
	}

	@Override
	protected void onDestroy() 
	{
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	protected void onResume() 
	{
		super.onResume();
		
		// Set up the adapters
		adapterDefaultFreq = ArrayAdapter.createFromResource(this, R.array.UpdateFrequency, android.R.layout.simple_spinner_item);
		adapterDefaultFreq.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinDefaultFreq.setAdapter(adapterDefaultFreq);
		adapterNumOfWeeks = ArrayAdapter.createFromResource(this, R.array.WeeksToDisplay, android.R.layout.simple_spinner_item);
		adapterNumOfWeeks.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
		spinNumOfWeeks.setAdapter(adapterNumOfWeeks);
	}
	
    @Override
    protected void onActivityResult(int pRequestCode, int resultCode, Intent data)
    {	
    	if( resultCode != -1)
    	{
    		String fromActivity = data.getStringExtra("FromActivity");
    		
    		if( fromActivity.equalsIgnoreCase("FileChooser") )
    		{
    			final String dbTable = "kmmAccounts";
    			final String[] dbColumns = { "accountName", "id AS _id"};
    			final String strSelection = "(parentId='AStd::Asset' OR parentId='AStd::Liability') AND " +
    					"(balance != '0/1')";
    			final String strOrderBy = "parentID, accountName ASC";
    			
    			txtDatabasePath.setText(data.getStringExtra("FullPath"));
    			db = SQLiteDatabase.openDatabase(txtDatabasePath.getText().toString(), null, 0);
    			
    			// Get a cursor with the accounts for the user based on the database they selected.
    			Cursor cur = db.query(dbTable, dbColumns, strSelection, null, null, null, strOrderBy);
    			startManagingCursor(cur);
    			
    			// Setup the adapter now that we have a database and a cursor
    			adapterDefaultAccount = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, cur, FROM1, TO);
    			adapterDefaultAccount.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
    			spinDefaultAccount.setAdapter(adapterDefaultAccount);
    			
    			// Clean up the cursor and database.
    			db.close();
    		}
    	}
    		
    }
	
	public class AccountOnItemSelectedListener implements OnItemSelectedListener
	{
		public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
		{
			String str = parent.getAdapter().getItem(pos).toString();
			
			switch(parent.getId())
			{
			case R.id.homeConfigDefaultUpdateFreqSpinner:
				if( str.equals("None") )
					intDefaultFreq = 0;
				else if( str.equals("15 minutes") )
					intDefaultFreq = 90000;
				else if( str.equals("30 minutes") )
					intDefaultFreq = 180000;
				else if( str.equals("1 hour") )
					intDefaultFreq = 360000;
				else if( str.equals("2 hours") )
					intDefaultFreq = 720000;
				else if( str.equals("4 hours") )
					intDefaultFreq = 14400000;
				else if( str.equals("8 hours") )
					intDefaultFreq = 28800000;
				else if( str.equals("Upon KMyMoney modification") )
					intDefaultFreq = -1;
				else
					intDefaultFreq = 0;
				break;
			case R.id.homeConfigNumWeeksDisplaySpinner:
				if( str.equals("One Week") )
					intNumOfWeeks = 1;
				else if( str.equals("Two Weeks") )
					intNumOfWeeks = 2;
				else if( str.equals("Three Weeks") )
					intNumOfWeeks = 3;
				else if( str.equals("Four Weeks") )
					intNumOfWeeks = 4;
				else
					intNumOfWeeks = 1;
				break;
			case R.id.homeConfigDefaultAccountSpinner:
				Cursor c = (Cursor) parent.getAdapter().getItem(pos);
				strAccountId = c.getString(1).toString();
				break;
			default:
				break;
			}
		}

		public void onNothingSelected(AdapterView<?> arg0) 
		{
			// do nothing.
		}		
	}
}
