package com.vanhlebarsoftware.kmmdroid;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

public class WelcomeActivity extends Activity implements OnClickListener
{
	@SuppressWarnings("unused")
	private static final String TAG = "WelcomeActivity";
	TextView startNew;
	TextView openDb;
	Context context;
	KMMDroidApp KMMDapp;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Get our application
        KMMDapp = ((KMMDroidApp) getApplication());
        
        // See if the user has set the preference to start up with the last used database
        if( KMMDapp.prefs.getBoolean("openLastUsed", false) )
        {
        	KMMDapp.setFullPath(KMMDapp.prefs.getString("Full Path", ""));
			startActivity(new Intent(this, HomeActivity.class));
			finish();
        }
        
        // Find our views
        setContentView(R.layout.welcome);
        startNew = (TextView) findViewById(R.id.titleStartNew);
        openDb = (TextView) findViewById(R.id.titleOpenDatabase);
        
        // Set the onClickListener events
        startNew.setOnClickListener(this);
        openDb.setOnClickListener(this);
    }
    
    // Called when the TextView's are clicked
    public void onClick(View v)
    {
    	Intent i = null;
    	
    	switch (v.getId())
    	{
    		case R.id.titleStartNew:
    			i = new Intent(this, NewDatabaseActivity.class);
    			startActivityForResult(i, 0);
    			break;
    		case R.id.titleOpenDatabase:
    			i = new Intent(this, FileChooser.class);
    			startActivityForResult(i, 0);
    			break;
    	}
    }
    
    @Override
    protected void onActivityResult(int pRequestCode, int resultCode, Intent data)
    {
    	Intent i = null;
    	
    	if( resultCode != -1)
    	{
    		String fromActivity = data.getStringExtra("FromActivity");
    		
    		if( fromActivity.equalsIgnoreCase("FileChooser") )
    		{
    			String path = data.getStringExtra("FullPath");
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