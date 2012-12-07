package com.vanhlebarsoftware.kmmdroid;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.ContextThemeWrapper;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class PayeeMatchingActivity extends FragmentActivity
{
	private static final String TAG = "PayeeMatchingActivity";
	private CreateModifyPayeeActivity parentTabHost;
	RadioGroup matching;
	RadioButton noMatching;
	RadioButton matchonName;
	
	@Override
    public void onCreate(Bundle savedInstanceState) 
	{
        super.onCreate(savedInstanceState);
        setContentView(R.layout.payee_matching);
        
        // Get the tabHost on the parent.
        parentTabHost = ((CreateModifyPayeeActivity) this.getParent());
        
        // Get our views
        matching = (RadioGroup) findViewById(R.id.radioGroup1);
        noMatching = (RadioButton) findViewById(R.id.payeeNoMatching);
        matchonName = (RadioButton) findViewById(R.id.payeeMatchonName);
        
        // Set an onClickListener for the radioGroup
        matching.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            public void onCheckedChanged(RadioGroup rGroup, int checkedId)
            {
            	parentTabHost.setIsDirty(true);
            }
        });
    }
	
	@Override
	public void onBackPressed()
	{
		if( parentTabHost.getIsDirty() )
		{
			AlertDialog.Builder alertDel = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogNoTitle));
			alertDel.setTitle(R.string.BackActionWarning);
			alertDel.setMessage(getString(R.string.titleBackActionWarning));

			alertDel.setPositiveButton(getString(R.string.titleButtonOK), new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int whichButton)
				{
					finish();
				}
			});
			
			alertDel.setNegativeButton(getString(R.string.titleButtonCancel), new DialogInterface.OnClickListener() 
			{
				public void onClick(DialogInterface dialog, int whichButton) 
				{
					// Canceled.
				}
			});				
			alertDel.show();
		}
		else
		{
			finish();
		}
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
