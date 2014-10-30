package com.vanhlebarsoftware.kmmdroid;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;

public class LoadMoreTransactionsActivity extends FragmentActivity
{
	private final static String TAG = LoadMoreTransactionsActivity.class.getSimpleName();
	Button btnOneMonth;
	Button btnOneYear;
	Button btnAll;
	Intent i = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
        setContentView(R.layout.loadmoretransactions);
        
        // populate our intent for the returned value.
        i = this.getIntent();
        
        // Get our buttons.
        btnOneMonth = (Button) findViewById(R.id.buttonOneMonth);
        btnOneYear = (Button) findViewById(R.id.buttonOneYear);
        btnAll = (Button) findViewById(R.id.buttonAll);
        
        // Setup our onClick listener's for the buttons.
        btnOneMonth.setOnClickListener(new View.OnClickListener()
        {
			public void onClick(View arg0)
			{
				i.putExtra("LoadMore", "Month");
				setResult(1, i);
				finish();
			}
		});
        
        btnOneYear.setOnClickListener(new View.OnClickListener()
        {
			public void onClick(View arg0)
			{
				i.putExtra("LoadMore", "Year");
				setResult(1, i);
				finish();
			}
		});
        
        btnAll.setOnClickListener(new View.OnClickListener()
        {
			public void onClick(View arg0)
			{
				i.putExtra("LoadMore", "All");
				setResult(1, i);
				finish();
			}
		});
	}
	
	@Override
	protected void onResume()
	{
		super.onResume();
	}
	
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
	}
	
	@Override
	public void onBackPressed()
	{
		setResult(-1, null);
		finish();
	}
}
