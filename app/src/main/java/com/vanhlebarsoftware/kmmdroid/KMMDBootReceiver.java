package com.vanhlebarsoftware.kmmdroid;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class KMMDBootReceiver extends BroadcastReceiver 
{
	private static final String TAG = KMMDBootReceiver.class.getSimpleName();
	SharedPreferences prefs = null;
	
	@Override
	public void onReceive(Context context, Intent intent) 
	{
		// Need to get the prefs for our application so we can update the account used..
		prefs = context.getSharedPreferences("com.vanhlebarsoftware.kmmdroid_preferences", Context.MODE_WORLD_READABLE);
		SharedPreferences.Editor prefEditor = prefs.edit();
		
        // See if the user wants to get notifications of schedules that are due Today or past due.
        if(prefs.getBoolean("receiveNotifications", false))
        {
        	final Calendar updateTime = Calendar.getInstance();
        	int intHour = prefs.getInt("notificationTime.hour", 0);
        	int intMin = prefs.getInt("notificationTime.minute", 0);
        	updateTime.set(Calendar.HOUR_OF_DAY, intHour);
        	updateTime.set(Calendar.MINUTE, intMin);
        	updateTime.set(Calendar.SECOND, 0);
        	setRepeatingAlarm(context, null, updateTime, KMMDroidApp.ALARM_NOTIFICATIONS);
    		prefEditor.putBoolean("notificationAlarmSet", true);
    		prefEditor.apply();
        }
        else
        {
    		prefEditor.putBoolean("notificationAlarmSet", false);
    		prefEditor.apply();
        }
	}

	public void setRepeatingAlarm(Context context, String updateValue, Calendar updateTime, int alarmType)
	{
		PendingIntent service = null;
		Intent intent = null;
		
		// Set up the repeating alarm based on the user's preferences.
		final AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);	

		intent = new Intent(KMMDNotificationsService.CHECK_SCHEDULES);
		service = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
			
		//alarmMgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, updateTime.getTimeInMillis(), AlarmManager.INTERVAL_DAY, service);
		alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, updateTime.getTimeInMillis(), AlarmManager.INTERVAL_DAY, service);
	}
}
