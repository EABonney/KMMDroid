package com.vanhlebarsoftware.kmmdroid;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

public class ReportsActivity extends FragmentActivity 
{
	private final static String TAG = ReportsActivity.class.getSimpleName();
	ListView listReports;
	
	/* Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
        setContentView(R.layout.reports);
        
        // Find our views
        listReports = (ListView) findViewById(R.id.listReports);
        
    	// Now hook into listReports ListView and set its onItemClickListener member
    	// to our class handler object.
        listReports.setOnItemClickListener(mMessageClickedHandler);
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
	
	// Message Handler for our listReports List View clicks
	private OnItemClickListener mMessageClickedHandler = new OnItemClickListener() {
	    public void onItemClick(AdapterView<?> parent, View v, int position, long id)
	    {
	    	String selection = (String) parent.getItemAtPosition(position);
	    	
	    	if( selection.equalsIgnoreCase(getString(R.string.titleCashRequirements)) )
	    	{
		    	Intent i = new Intent(getBaseContext(), CashRequirementsOptionsActivity.class);
		    	startActivity(i);
		    	finish();	    		
	    	}
	    	else
	    	{
		    	Toast.makeText(getBaseContext(), "Can't run the selected report!", Toast.LENGTH_SHORT).show();
	    	}
	    }
	};
}
