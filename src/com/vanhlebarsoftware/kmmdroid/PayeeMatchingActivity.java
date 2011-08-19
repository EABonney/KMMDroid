package com.vanhlebarsoftware.kmmdroid;

import android.app.Activity;
import android.os.Bundle;
import android.widget.RadioGroup;

public class PayeeMatchingActivity extends Activity
{
	RadioGroup matching;
	
	@Override
    public void onCreate(Bundle savedInstanceState) 
	{
        super.onCreate(savedInstanceState);
        setContentView(R.layout.payee_matching);
        
        // Get our views
        matching = (RadioGroup) findViewById(R.id.radioGroup1);
    }
	
	public int getMatchingType()
	{
		return matching.getCheckedRadioButtonId();
	}
}
