package com.vanhlebarsoftware.kmmdroid;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class NewDatabaseActivity extends FragmentActivity implements OnClickListener
{
	EditText editDatabaseName;
	Button btnOk;
	Button btnCancel;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_database);
        
        // Find our views
        editDatabaseName = (EditText) findViewById(R.id.editDatabaseName);
        btnOk = (Button) findViewById(R.id.buttonOk);
        btnCancel = (Button) findViewById(R.id.buttonCancel);
        
        // Set the onClickListener events
        btnOk.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
    }
    
	public void onClick(View v) 
	{
		Intent i = this.getIntent();
		
		switch(v.getId())
		{
			case R.id.buttonCancel:
				i.putExtra("DatabaseName", "");
				setResult(-1, i);
				finish();
				break;
			case R.id.buttonOk:
				i.putExtra("FromActivity", "NewDatabase");
				i.putExtra("DatabaseName", editDatabaseName.getText().toString());
				setResult(1, i);
				finish();
		}
	}

	@Override
	public void onBackPressed()
	{
		setResult(-1, null);
		finish();
	}
}
