package com.vanhlebarsoftware.kmmdroid;

import java.util.Calendar;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;
import android.widget.EditText;

public class KMMDDatePickerFragment extends DialogFragment implements
DatePickerDialog.OnDateSetListener
{
	public EditText activity_etUpdate;
	
	public KMMDDatePickerFragment(EditText edit_textView)
	{
		activity_etUpdate = edit_textView;
	}
	
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) 
	{
        // Use the current date as the default date in the picker
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        // Create a new instance of DatePickerDialog and return it
        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    public void onDateSet(DatePicker view, int year, int month, int day) 
    {
        // Do something with the date chosen by the user
    	activity_etUpdate.setText(updateDisplay(year, month, day));
    }
	// **************************************************************************************************
	// ************************************ Helper methods **********************************************
	
	private StringBuilder updateDisplay(int year, int month, int day)
	{
		String strDay = null;
		String strMonth = null;
		switch(day)
		{
			case 0:
			case 1:
			case 2:
			case 3:
			case 4:
			case 5:
			case 6:
			case 7:
			case 8:
			case 9:
				strDay = "0" + String.valueOf(day);
				break;
			default:
				strDay = String.valueOf(day);
			break;
		}
		
		switch(month)
		{
			case 0:
			case 1:
			case 2:
			case 3:
			case 4:
			case 5:
			case 6:
			case 7:
			case 8:
			//case 9:
				strMonth = "0" + String.valueOf(month + 1);
				break;
			default:
				strMonth = String.valueOf(month + 1);
				break;
		}
		
		return new StringBuilder()
					// Month is 0 based so add 1
					.append(strMonth).append("-")
					.append(strDay).append("-")
					.append(year);
	}
}
