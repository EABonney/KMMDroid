package com.vanhlebarsoftware.kmmdroid;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class PayeeMatchingActivity extends Fragment
{
	private static final String TAG = "PayeeMatchingActivity";
	private OnSendMatchingDataListener onSendMatchingData;
	private Activity ParentTab;
	RadioGroup matching;
	RadioButton noMatching;
	RadioButton matchonName;
	
	
	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onAttach(android.app.Activity)
	 */
	@Override
	public void onAttach(Activity activity) 
	{
		super.onAttach(activity);
		
		ParentTab = activity;
		
		try
		{
			onSendMatchingData = (OnSendMatchingDataListener) activity;
		}
		catch(ClassCastException e)
		{
			throw new ClassCastException(activity.toString() + " must implemenent OnSendMatchingDataListener.");
		}
	}

	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
        if (container == null) 
        {
            // We have different layouts, and in one of them this
            // fragment's containing frame doesn't exist.  The fragment
            // may still be created from its saved state, but there is
            // no reason to try to create its view hierarchy because it
            // won't be displayed.  Note this is not needed -- we could
            // just run the code below, where we would create and return
            // the view hierarchy; it would just never be used.
            return null;
        }
        
        View view = inflater.inflate(R.layout.payee_matching, container, false);
        
        // Get our views
        matching = (RadioGroup) view.findViewById(R.id.radioGroup1);
        noMatching = (RadioButton) view.findViewById(R.id.payeeNoMatching);
        matchonName = (RadioButton) view.findViewById(R.id.payeeMatchonName);
        
        // Set an onClickListener for the radioGroup
        matching.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            public void onCheckedChanged(RadioGroup rGroup, int checkedId)
            {
            	((CreateModifyPayeeActivity) ParentTab).setIsDirty(true);
            }
        });
        
		return view;
	}

	@Override
    public void onCreate(Bundle savedInstanceState) 
	{
        super.onCreate(savedInstanceState);
    }
	
	@Override
	public void onResume()
	{
		super.onResume();
		
		sendMatchingData();
		
		updateUIElements();
	}
	// *******************************************************************************************************
	// *********************************** Public Event Interfaces *******************************************
	public interface OnSendMatchingDataListener
	{
		public void onSendMatchingData();
	}	
	// ************************************************************************************************
	// ******************************* Helper Functions ***********************************************
	private void sendMatchingData()
	{
		onSendMatchingData.onSendMatchingData();
	}
	
	private void updateUIElements()
	{
		SharedPreferences prefs = getActivity().getPreferences(Activity.MODE_PRIVATE);		
		int matchtype = prefs.getInt("MatchType", -1);
		
		if( matchtype != -1 )
			this.putMatchingType(matchtype);
	}
	
	public int getMatchingType()
	{
		switch( matching.getCheckedRadioButtonId() )
		{
		case R.id.payeeNoMatching:
			return 0;
		case R.id.payeeMatchonName:
			return 1;
		default:
			return 0;
		}
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
