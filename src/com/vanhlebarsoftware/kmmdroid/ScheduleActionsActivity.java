package com.vanhlebarsoftware.kmmdroid;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class ScheduleActionsActivity extends Activity
{
	private static final String TAG = ScheduleActionsActivity.class.getSimpleName();
	private String scheduleId = null;
	private String scheduleDesc = null;
	private int Action = 0;
	
	Button buttonEnter;
	Button buttonSkip;
	TextView tvScheduleDescription;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
        setContentView(R.layout.dialogscheduleactions);
        
        // Get the scheduleId the user clicked on in the widget.
        Bundle extras = getIntent().getExtras();
        scheduleId = extras.getString("scheduleId");
        scheduleDesc = extras.getString("scheduleDescription");
        Action = extras.getInt("Action");
        
        // Find our views
        buttonEnter = (Button) findViewById(R.id.btnEnterSchedule);
        buttonSkip = (Button) findViewById(R.id.btnSkipSchedule);
        tvScheduleDescription = (TextView) findViewById(R.id.scheduleDescription);
        
        // Update the description of the TextView to include the actual description of the schedule the user wants to operate on.
        String str = tvScheduleDescription.getText().toString() + "\n" + scheduleDesc;
        tvScheduleDescription.setText(str);
        
        // Set our onClickListener events.
        buttonEnter.setOnClickListener(new View.OnClickListener()
        {
			public void onClick(View arg0)
			{
				Intent i = new Intent(getBaseContext(), CreateModifyTransactionActivity.class);
				i.putExtra("scheduleId", scheduleId);
				i.putExtra("Action", Action);
				startActivity(i);
				finish();
			}
		});

        buttonSkip.setOnClickListener(new View.OnClickListener()
        {

			public void onClick(View arg0)
			{					
				AlertDialog.Builder alertDel = new AlertDialog.Builder(arg0.getContext());
				alertDel.setTitle(R.string.skip);
				alertDel.setMessage(getString(R.string.titleSkipSchedule));

				alertDel.setPositiveButton(getString(R.string.titleButtonYes), new DialogInterface.OnClickListener() 
				{
					public void onClick(DialogInterface dialog, int whichButton)
					{					
						Intent intent = new Intent(getBaseContext(), KMMDService.class);
						intent.putExtra("skipScheduleId", scheduleId);
						startService(intent);
						finish();
					}
				});
				alertDel.setNegativeButton(getString(R.string.titleButtonNo), new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int whichButton) 
					{
						// Canceled.
						finish();
					}
				});				
				alertDel.show();
			}
		});
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
