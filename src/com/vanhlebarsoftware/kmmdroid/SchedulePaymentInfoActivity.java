package com.vanhlebarsoftware.kmmdroid;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;

public class SchedulePaymentInfoActivity extends Activity 
{
	private static final String TAG = SchedulePaymentInfoActivity.class.getSimpleName();
	private static final int ACTION_NEW = 1;
	private static final int ACTION_EDIT = 2;
	private int Action = 0;
	KMMDroidApp KMMDapp;
	Cursor cursor;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
        setContentView(R.layout.schedule_paymentinfo);
        
        // Get the Action.
        Bundle extras = getIntent().getExtras();
        //Action = extras.getInt("Action");
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
		// TODO Auto-generated method stub
		super.onResume();
	}
}
