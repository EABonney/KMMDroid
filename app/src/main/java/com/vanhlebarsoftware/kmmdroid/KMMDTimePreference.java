/*********************************************************************
 * 
 * Adapted from Android TimePrefence example located at:
 * http://www.twodee.org/weblog/?p=1037 * 
 * 
 */

package com.vanhlebarsoftware.kmmdroid;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.DialogPreference;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TimePicker;

public class KMMDTimePreference extends DialogPreference
{
	private static final String TAG = KMMDTimePreference.class.getSimpleName();
	private TimePicker timePicker;
	private static final int DEFAULT_HOUR = 8;
	private static final int DEFAULT_MINUTE = 0;
	
	public KMMDTimePreference(Context context, AttributeSet attributes)
	{
		super(context, attributes);
		setPersistent(false);
	}
	
	@Override
	public void onBindDialogView(View view)
	{
		timePicker = (TimePicker) view.findViewById(R.id.prefTimePicker);
		timePicker.setIs24HourView(DateFormat.is24HourFormat(timePicker.getContext()));
		timePicker.setCurrentHour(getSharedPreferences().getInt(getKey() + ".hour", DEFAULT_HOUR));
		timePicker.setCurrentMinute(getSharedPreferences().getInt(getKey() + ".minute", DEFAULT_MINUTE));
	}
	
	@Override
	protected void onDialogClosed(boolean okToSave)
	{
		super.onDialogClosed(okToSave);
		
		if(okToSave)
		{
			timePicker.clearFocus();
			SharedPreferences.Editor editor = getEditor();
			editor.putInt(getKey() + ".hour", timePicker.getCurrentHour());
			editor.putInt(getKey() + ".minute", timePicker.getCurrentMinute());
			editor.commit();			
		}
	}
}
