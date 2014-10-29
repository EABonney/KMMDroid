package com.vanhlebarsoftware.kmmdroid;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Spinner;

public class HomeScreenConfiguration extends FragmentActivity 
{
	private static final String TAG = HomeScreenConfiguration.class.getSimpleName();
	private static final int INVALID_WIDGET = 0;
	private static final int WIDGET_PREFERREDACCOUNTS = 2000;
	private static final int WIDGET_SCHEDULES = 2001;
	private int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
	private final String URI_SCHEME = "KMMD";
	static final String[] FROM = { "name" };
	static final int[] TO = { android.R.id.text1 };
	static final String[] FROM1 = { "accountName" };
	private String strAccountId = null;
	private int intDefaultFreq = 0;
	private int intNumOfWeeks = 0;
	private String widgetId = null;
	private String accountUsed = null;
	private String updateFreq = null;
	private String displayWeeks = null;
	private String widgetName = null;
	private int widgetType = 0;
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
		Log.d(TAG, "Entering HomeScreenConfiguration::onCreate()");
		
        // Get our application
        KMMDapp = ((KMMDroidApp) getApplication());
        
        // Get the specific appWidget the user is setting up.
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if( extras != null )
        {
        	appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        	widgetId = extras.getString("widgetId");
        	widgetType = KMMDapp.prefs.getInt("widgetType" + widgetId, 0);
        	Log.d(TAG, "widgetId: " + widgetId);
        	Log.d(TAG, "appWidgtetId: " + appWidgetId);
        }
        
        if(widgetType == 0)
        {
        	Log.d(TAG, "We are setting up the widget NOT editing it!");
    		widgetName = AppWidgetManager.getInstance(getBaseContext())
    				.getAppWidgetInfo(appWidgetId).provider.getShortClassName();
    		widgetName = widgetName.substring(1, widgetName.length());
    		widgetType = getWidgetType(widgetName);
        }
                
        // Get our views
        txtDatabasePath = (TextView) findViewById(R.id.homeConfigDatabaseTitle);
        spinDefaultAccount = (Spinner) findViewById(R.id.homeConfigDefaultAccountSpinner);
        spinDefaultFreq = (Spinner) findViewById(R.id.homeConfigDefaultUpdateFreqSpinner);
        spinNumOfWeeks = (Spinner) findViewById(R.id.homeConfigNumWeeksDisplaySpinner);
        btnSelectFile = (Button) findViewById(R.id.homeConfigDatabaseBtn);
        btnSave = (Button) findViewById(R.id.homeConfigSave);
        
        // Turn off items not needed in the PreferredAccounts widget
        switch(widgetType)
        {
        	case WIDGET_PREFERREDACCOUNTS:
        		findViewById(R.id.homeConfigDefaultAccountTitle).setVisibility(View.GONE);
        		spinDefaultAccount.setVisibility(View.GONE);
        		findViewById(R.id.homeConfigDefaultUpdateFreqTitle).setVisibility(View.GONE);
        		spinDefaultFreq.setVisibility(View.GONE);
        		findViewById(R.id.homeConfigNumWeeksDisplayTitle).setVisibility(View.GONE);
        		spinNumOfWeeks.setVisibility(View.GONE); 
        		break;
        	case WIDGET_SCHEDULES:
        	case INVALID_WIDGET:
        	default:
        		break;
        }
        
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
				Log.d(TAG, "Saving updated values....");
				Log.d(TAG, "Widget type: " + widgetName);
				Log.d(TAG, "WidgetId: " + String.valueOf(appWidgetId));
				Log.d(TAG, "Databasepath: " + txtDatabasePath.getText().toString());
				Log.d(TAG, "accountUsed: " + strAccountId);
				Log.d(TAG, "updateFrequency: " + String.valueOf(intDefaultFreq));
				Log.d(TAG, "displayWeeks: " + String.valueOf(intNumOfWeeks));
				Log.d(TAG, "widgetType: " + String.valueOf(widgetType));
				String strappWidgetId = String.valueOf(appWidgetId);
		    	// Save the config settings for this WidgetId.
				SharedPreferences.Editor editor = KMMDapp.prefs.edit();
				editor.putString("widgetDatabasePath" + strappWidgetId, txtDatabasePath.getText().toString());
				editor.putString("accountUsed" + strappWidgetId, strAccountId);
				editor.putString("updateFrequency" + strappWidgetId, String.valueOf(intDefaultFreq));
				editor.putString("displayWeeks" + strappWidgetId, String.valueOf(intNumOfWeeks));
				editor.putInt("widgetType" + strappWidgetId, widgetType);
				editor.apply();
		    	
		    	// Notify the Widget Manager that the config has completed.
		    	Intent result = new Intent();
		    	result.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
		    	setResult(RESULT_OK, result);
		    	
		    	// Make sure we refresh the new widget.
		    	Intent intent = null;
		    	switch(widgetType)
		    	{
		    		case WIDGET_SCHEDULES:
		    			intent = new Intent(getBaseContext(), WidgetSchedules.class);
		    			intent.putExtra("refreshWidgetId", String.valueOf(appWidgetId));
		    			Log.d(TAG, "refreshing Schedules Widget, widgetId: " + appWidgetId);
		    			break;
		    		case WIDGET_PREFERREDACCOUNTS:
		    			intent = new Intent(getBaseContext(), WidgetPreferredAccounts.class);
		    			intent.putExtra("refreshWidgetId", String.valueOf(appWidgetId));
		    			Log.d(TAG, "refreshing Preferred Accounts Widget, widgetId: " + appWidgetId);
		    			break;
		    	}

    			String action = "com.vanhlebarsoftware.kmmdroid.Refresh"; // + "#" + String.valueOf(appWidgetId);
    			intent.setAction(action);
    			Uri data = Uri.withAppendedPath(Uri.parse(URI_SCHEME + "://widget/id/"), String.valueOf(appWidgetId));
    			intent.setData(data);
				sendBroadcast(intent, KMMDService.RECEIVE_HOME_UPDATE_NOTIFICATIONS);
				
		    	finish();
			}
		});
        
        // Set the OnItemSelectedListeners for the spinners.
        spinDefaultAccount.setOnItemSelectedListener(new AccountOnItemSelectedListener());
        spinDefaultFreq.setOnItemSelectedListener(new AccountOnItemSelectedListener());
        spinNumOfWeeks.setOnItemSelectedListener(new AccountOnItemSelectedListener());
        

        
        // Set the result to canceled in case the user exits the activity without accepting the config changes.
        setResult(RESULT_CANCELED, null);
        
	}

	@Override
	protected void onDestroy() 
	{
		super.onDestroy();
	}

	@Override
	protected void onResume() 
	{
		super.onResume();
		Log.d(TAG, "Editing widgetId: " + widgetId);
		
		// Set up the adapters
		adapterDefaultFreq = ArrayAdapter.createFromResource(this, R.array.UpdateFrequency, android.R.layout.simple_spinner_item);
		adapterDefaultFreq.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinDefaultFreq.setAdapter(adapterDefaultFreq);
		adapterNumOfWeeks = ArrayAdapter.createFromResource(this, R.array.WeeksToDisplay, android.R.layout.simple_spinner_item);
		adapterNumOfWeeks.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
		spinNumOfWeeks.setAdapter(adapterNumOfWeeks);
		
		// Get all the information for the specific widget if an Id was passed.
		if(widgetId != null)
		{
			// Since we are passing in a widgetId, we need to set appWidgetId to this passed in value.
			appWidgetId = Integer.valueOf(widgetId);
			
			txtDatabasePath.setText(KMMDapp.prefs.getString("widgetDatabasePath" + widgetId, ""));
			accountUsed = KMMDapp.prefs.getString("accountUsed" + widgetId, "");
			updateFreq = KMMDapp.prefs.getString("updateFrequency" + widgetId, "");
			displayWeeks = KMMDapp.prefs.getString("displayWeeks" + widgetId, "");
			
			// Set the spinners correctly.
			spinDefaultFreq.setSelection(setDefaultFrequency(updateFreq));
			spinNumOfWeeks.setSelection(setNumberOfWeeks(displayWeeks));
			
			// Set up the adapter for the accounts and set the spinner correctly.
			final String dbTable = "kmmAccounts";
			final String[] dbColumns = { "accountName", "id AS _id"};
			final String strSelection = "(parentId='AStd::Asset' OR parentId='AStd::Liability') AND " +
					"(balance != '0/1')";
			final String strOrderBy = "parentID, accountName ASC";
			
			db = SQLiteDatabase.openDatabase(txtDatabasePath.getText().toString(), null, 0);
			
			// Get a cursor with the accounts for the user based on the database they selected.
			Cursor cur = db.query(dbTable, dbColumns, strSelection, null, null, null, strOrderBy);
			startManagingCursor(cur);
			
			// Setup the adapter now that we have a database and a cursor
			adapterDefaultAccount = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, cur, FROM1, TO);
			adapterDefaultAccount.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
			spinDefaultAccount.setAdapter(adapterDefaultAccount);
			spinDefaultAccount.setSelection(setAccountUsed(accountUsed, cur));
			
			// Clean up the cursor and database.
			db.close();			
		}
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
				if( str.equals(getString(R.string.None)) )
					intDefaultFreq = 0;
				else if( str.equals(getString(R.string.FifteenMinutes)) )
					intDefaultFreq = 90000;
				else if( str.equals(getString(R.string.ThirtyMinutes)) )
					intDefaultFreq = 180000;
				else if( str.equals(getString(R.string.OneHour)) )
					intDefaultFreq = 360000;
				else if( str.equals(getString(R.string.TwoHours)) )
					intDefaultFreq = 720000;
				else if( str.equals(getString(R.string.FourHours)) )
					intDefaultFreq = 14400000;
				else if( str.equals(getString(R.string.SixHours)) )
					intDefaultFreq = 28800000;
				else if( str.equals(getString(R.string.AutoUpdate)) )
					intDefaultFreq = -1;
				else
					intDefaultFreq = 0;
				break;
			case R.id.homeConfigNumWeeksDisplaySpinner:
				if( str.equals(getString(R.string.OneWeek)) )
					intNumOfWeeks = 1;
				else if( str.equals(getString(R.string.TwoWeeks)) )
					intNumOfWeeks = 2;
				else if( str.equals(getString(R.string.ThreeWeeks)) )
					intNumOfWeeks = 3;
				else if( str.equals(getString(R.string.FourWeeks)) )
					intNumOfWeeks = 4;
				else
					intNumOfWeeks = 1;
				break;
			case R.id.homeConfigDefaultAccountSpinner:
				Cursor c = (Cursor) parent.getAdapter().getItem(pos);
				strAccountId = c.getString(1).toString();
				Log.d(TAG, "accountId: " + strAccountId);
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
	
	// **************************************************************************************************
	// ************************************ Helper methods **********************************************
	private int setDefaultFrequency(String displayFreq)
	{
		switch(Integer.valueOf(displayFreq))
		{
		case 0:
			intDefaultFreq = 0;
			return 0;
		case 90000:
			intDefaultFreq = 90000;
			return 1;
		case 180000:
			intDefaultFreq = 180000;
			return 2;
		case 360000:
			intDefaultFreq = 360000;
			return 3;
		case 720000:
			intDefaultFreq = 720000;
			return 4;
		case 14400000:
			intDefaultFreq = 14400000;
			return 5;
		case 28800000:
			intDefaultFreq = 28800000;
			return 6;
		case -1:
			intDefaultFreq = -1;
			return 7;
		default:
			intDefaultFreq = 0;
			return 0;
		}
	}
	
	private int setNumberOfWeeks(String weeks)
	{
		switch(Integer.valueOf(weeks))
		{
		case 1:
			intNumOfWeeks = 1;
			return 0;
		case 2:
			intNumOfWeeks = 2;
			return 1;
		case 3:
			intNumOfWeeks = 3;
			return 2;
		case 4:
			intNumOfWeeks = 4;
			return 3;
		default:
			intNumOfWeeks = 1;
			return 0;
		}
	}
	
	private int setAccountUsed(String account, Cursor cur)
	{
		int i = 0;
		cur.moveToFirst();
		
		if( account != null )
		{
			while(!account.equals(cur.getString(1)))
			{
				cur.moveToNext();
			
				//check to see if we have moved past the last item in the cursor, if so return current i.
				if(cur.isAfterLast())
					return i;
			
				i++;
			}
		}
		return i;
	}
	
	private int getWidgetType(String name)
	{
		if(name.equalsIgnoreCase(WidgetPreferredAccounts.class.getSimpleName()))
			return WIDGET_PREFERREDACCOUNTS;
		else if(name.equalsIgnoreCase(WidgetSchedules.class.getSimpleName()))
			return WIDGET_SCHEDULES;
		else
			return 0;
	}
}
