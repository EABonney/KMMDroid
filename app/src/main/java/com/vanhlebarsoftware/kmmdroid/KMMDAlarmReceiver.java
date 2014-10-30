package com.vanhlebarsoftware.kmmdroid;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class KMMDAlarmReceiver extends BroadcastReceiver 
{
	private static final String TAG = KMMDAlarmReceiver.class.getSimpleName();

	@Override
	public void onReceive(Context context, Intent intent)
	{
		if(intent.getAction().equals(KMMDNotificationsService.CHECK_SCHEDULES))
		{
			Intent notifyIntent = new Intent(context, KMMDNotificationsService.class);
			context.startService(notifyIntent);
		}
	}

}
