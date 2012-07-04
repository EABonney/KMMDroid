package com.vanhlebarsoftware.kmmdroid;

import java.util.Calendar;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

public class WelcomeActivity extends Activity
{
	@SuppressWarnings("unused")
	private static final String TAG = "WelcomeActivity";
	boolean closedDB = false;
	Context context;
	KMMDroidApp KMMDapp;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Get our application
        KMMDapp = ((KMMDroidApp) getApplication());
        
        Bundle extras = getIntent().getExtras();
        boolean Closing = false;
        
        if( extras != null)
        {
        	if(!extras.isEmpty())
        		Closing = extras.getBoolean("Closing");
        }
        
        // See if the user has a preferance set for the update interval of home widgets, if so set it.
        if(!KMMDapp.getAutoUpdate())
        {
        	String value = KMMDapp.prefs.getString("updateFrequency", "");
        	KMMDapp.setRepeatingAlarm(value);
        }
        
        if( !Closing )
        {
        	// See if the user has set the preference to start up with the last used database
        	if( KMMDapp.prefs.getBoolean("openLastUsed", false) )
        	{
        		KMMDapp.setFullPath(KMMDapp.prefs.getString("Full Path", ""));
        		startActivity(new Intent(this, HomeActivity.class));
        		finish();
        	}
        }
        
        // Find our views
        setContentView(R.layout.welcome);
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
		
	}
    
	// Called first time the user clicks on the menu button
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.welcome_menu, menu);
		return true;
	}
    
	// Called when an options item is clicked
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
    	Intent i = null;
    	
		switch (item.getItemId())
		{
			case R.id.itemNew:
    			i = new Intent(this, NewDatabaseActivity.class);
    			startActivityForResult(i, 0);
				break;
			case R.id.itemOpen:
    			i = new Intent(this, FileChooser.class);
    			startActivityForResult(i, 0);
				break;
			case R.id.itemRecent:
				break;
		}
		
		return true;
	}
    @Override
    protected void onActivityResult(int pRequestCode, int resultCode, Intent data)
    {
    	Intent i = null;
    	
    	if( resultCode != -1)
    	{
    		String fromActivity = data.getStringExtra("FromActivity");
    		Log.d(TAG, "result: " + fromActivity.toString());
    		
    		if( fromActivity.equalsIgnoreCase("FileChooser") )
    		{
    			String path = data.getStringExtra("FullPath");
    			Log.d(TAG, "Full Path: " + path);
    			KMMDapp.setFullPath(path);
    			i = new Intent(this, HomeActivity.class);
    			startActivity(i);
    		}
    		
    		if( fromActivity.equalsIgnoreCase("NewDatabase") )
    		{
    			String dbName = data.getStringExtra("DatabaseName");
    			DbHelper dbHelper = new DbHelper(this, dbName);
    			SQLiteDatabase db = dbHelper.getReadableDatabase();
    			db.close();
    			Toast.makeText(this, "New Database created: " + dbName, Toast.LENGTH_SHORT).show();
    		}
    	}
    		
    }
}