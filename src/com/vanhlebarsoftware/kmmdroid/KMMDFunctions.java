package com.vanhlebarsoftware.kmmdroid;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

/****************************************************************************************************************************************/
/* This class holds all of our global functions in one location.
 * All functions are statically called and provide all items neccessary for that function.
 * This is to reduce overall code and collect all of our generic functions in one spot.
 * @author eric
 */
/****************************************************************************************************************************************/
public class KMMDFunctions 
{
	private static String TAG = KMMDFunctions.class.getSimpleName();
	private static final String URI_SCHEME = "com.vanhlebarsoftware.kmmdroid";
	
	public static void updateSchedulesWidget(String widgetToUpdate, Context context)
	{
		Log.d(TAG, "Send off intent to update the widget:" + widgetToUpdate);
		Intent intent = new Intent(context, WidgetSchedules.class);
		intent.putExtra(KMMDService.DATA_CHANGED, widgetToUpdate);
		Uri uri = Uri.withAppendedPath(Uri.parse(URI_SCHEME + "://widget/id/"),String.valueOf(widgetToUpdate));
		intent.setAction(KMMDService.DATA_CHANGED);
		intent.setData(uri);
		context.sendBroadcast(intent, KMMDService.RECEIVE_HOME_UPDATE_NOTIFICATIONS);
	}
	
	public static void updateSchedulesWidgets(Context context)
	{
		Log.d(TAG, "Send off intent to update the Schedules widgets.");
		Intent intent = new Intent(context, WidgetSchedules.class);
		intent.putExtra(KMMDService.DATA_CHANGED, "0");
		Uri uri = Uri.withAppendedPath(Uri.parse(URI_SCHEME + "://widget/id/"),"");
		intent.setAction(KMMDService.DATA_CHANGED);
		intent.setData(uri);
		context.sendBroadcast(intent, KMMDService.RECEIVE_HOME_UPDATE_NOTIFICATIONS);		
	}
	
	public static void updatePreferredAccountsWidget(String widgetToUpdate, Context context)
	{
		Log.d(TAG, "Send of intent to update the PreferredAccounts widgets.");
		Intent prefAccts = new Intent(context, WidgetPreferredAccounts.class);
		prefAccts.putExtra(KMMDService.DATA_CHANGED, widgetToUpdate);
		Uri uri = Uri.withAppendedPath(Uri.parse(URI_SCHEME + "://widget/id/"),String.valueOf(widgetToUpdate));
		prefAccts.setAction(KMMDService.DATA_CHANGED);
		prefAccts.setData(uri);
		context.sendBroadcast(prefAccts, KMMDService.RECEIVE_HOME_UPDATE_NOTIFICATIONS);
	}
	
	public static void updatePreferredAccountsWidgets(Context context)
	{
		Log.d(TAG, "Send of intent to update the PreferredAccounts widgets.");
		Intent prefAccts = new Intent(context, WidgetPreferredAccounts.class);
		prefAccts.putExtra(KMMDService.DATA_CHANGED, "0");
		Uri uri = Uri.withAppendedPath(Uri.parse(URI_SCHEME + "://widget/id/"),"");
		prefAccts.setAction(KMMDService.DATA_CHANGED);
		prefAccts.setData(uri);
		context.sendBroadcast(prefAccts, KMMDService.RECEIVE_HOME_UPDATE_NOTIFICATIONS);		
	}
}
