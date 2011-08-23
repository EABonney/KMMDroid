package com.vanhlebarsoftware.kmmdroid;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class PayeeMatchingActivity extends Activity
{
	private static final String TAG = "PayeeMatchingActivity";
	RadioGroup matching;
	RadioButton noMatching;
	RadioButton matchonName;
	
	@Override
    public void onCreate(Bundle savedInstanceState) 
	{
        super.onCreate(savedInstanceState);
        setContentView(R.layout.payee_matching);
        
        // Get our views
        matching = (RadioGroup) findViewById(R.id.radioGroup1);
        noMatching = (RadioButton) findViewById(R.id.payeeNoMatching);
        matchonName = (RadioButton) findViewById(R.id.payeeMatchonName);
    }
	
	public int getMatchingType()
	{
		return matching.getCheckedRadioButtonId();
	}
	
	public void putMatchingType(int button)
	{
		switch(button)
		{
		case 0:
			noMatching.setChecked(true);
			matchonName.setChecked(false);
			break;
		case 1:
			noMatching.setChecked(false);
			matchonName.setChecked(true);
			break;
		}
	}
}
