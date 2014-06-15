package com.vanhlebarsoftware.kmmdroid;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;

public class WidgetSchedules extends AppWidgetProvider
{
	private static final String TAG = WidgetSchedules.class.getSimpleName();
	private static final String URI_SCHEME = "com.vanhlebarsoftware.kmmdroid";
	private static final int ACTION_ENTER_SCHEDULE = 3;

	/* (non-Javadoc)
	 * @see android.appwidget.AppWidgetProvider#onUpdate(android.content.Context, android.appwidget.AppWidgetManager, int[])
	 */
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) 
	{		
		// Iterate over the array of active widgets.
		final int N = appWidgetIds.length;
		Log.d(TAG, "Number of widgets to update: " + String.valueOf(N));
		
		for(int i=0; i<N; i++)
		{
			int appWidgetId = appWidgetIds[i];
			Log.d(TAG, "Updating widgetId: " + String.valueOf(i));
			
			// Set up the intent to start the RemoteViews Service which will supply the views
			// shown in the ListView
			Intent intent = new Intent(context, WidgetSchedulesRVService.class);
			Uri data = Uri.withAppendedPath(Uri.parse(URI_SCHEME + "://widget/id/"), String.valueOf(appWidgetId));
			intent.setData(data);
			
			// Add the app widget ID to the intent extras.
			intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
			
			// Instantiate the RemoteViews object for the App Widget layout.
			RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_schedules);
			
			// Setup the RemoteViews object to use a RemoteViews adapter.
			views.setRemoteAdapter(R.id.schedulesListView, intent);
			
			// The empty view is displayed when the collection has no items.
			views.setEmptyView(R.id.schedulesListView, R.id.empty_widget_text);
			
			// Create a Pending Intent template to provide interactivity to each item displayed
			Uri uri = Uri.withAppendedPath(Uri.parse(URI_SCHEME + "://widget/id/"),String.valueOf(appWidgetId));
			Intent templateIntent = new Intent(context, ScheduleActionsActivity.class);
			templateIntent.putExtra("widgetId", appWidgetId);
			templateIntent.putExtra("Action", ACTION_ENTER_SCHEDULE);
			templateIntent.setData(uri);
			PendingIntent templatePendingIntent = PendingIntent.getActivity(context, 0, templateIntent, PendingIntent.FLAG_UPDATE_CURRENT);
			views.setPendingIntentTemplate(R.id.schedulesListView, templatePendingIntent);
			
			// Setup the onClick response to the various buttons on the widget
			// Start application by clicking on the icon
			intent = new Intent(context, WelcomeActivity.class);
			intent.putExtra("fromWidgetId", String.valueOf(appWidgetId));
			String action = "com.vanhlebarsoftware.kmmdroid.Welcome" + "#" + String.valueOf(appWidgetId);
			intent.setAction(action);
			PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
			views.setOnClickPendingIntent(R.id.kmmd_icon, pendingIntent);
			
			// Refresh icon
			intent = new Intent(context, WidgetSchedules.class);
			intent.putExtra("refreshWidgetId", appWidgetId);
			action = "com.vanhlebarsoftware.kmmdroid.Refresh"; // + "#" + String.valueOf(appWidgetId);
			intent.setAction(action);
			//pendingIntent = PendingIntent.getService(context, 0, intent, 0);
			pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
			views.setOnClickPendingIntent(R.id.kmmd_refresh, pendingIntent);
			
			// Preferences icon
			intent = new Intent(context, HomeScreenConfiguration.class);
			intent.putExtra("widgetId", String.valueOf(appWidgetId));
			action = "com.vanhlebarsoftware.kmmdroid.Preferences" + "#" + String.valueOf(appWidgetId);
			intent.setAction(action);
			pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
			views.setOnClickPendingIntent(R.id.kmmd_widgetSettings, pendingIntent);
			
			// Notify the App Widget Manager to update the widget using the modified remote view.
			appWidgetManager.updateAppWidget(appWidgetId, views);
			
			Log.d(TAG, "Leaving onUpdate for loop i=" + i);
		}
		super.onUpdate(context, appWidgetManager, appWidgetIds);
	}

	/* (non-Javadoc)
	 * @see android.appwidget.AppWidgetProvider#onReceive(android.content.Context, android.content.Intent)
	 */
	@Override
	public void onReceive(Context context, Intent intent) 
	{
		super.onReceive(context, intent);
		
		if(intent.getAction().equalsIgnoreCase("com.vanhlebarsoftware.kmmdroid.Refresh"))
		{
			int widgetId = intent.getExtras().getInt("refreshWidgetId");
			Log.d(TAG, "onReceive for widgetId: " + widgetId);
			updateWidget(context, widgetId);
		}
	}
	
	/* (non-Javadoc)
	 * @see android.appwidget.AppWidgetProvider#onDeleted(android.content.Context, int[])
	 */
	@Override
	public void onDeleted(Context context, int[] appWidgetIds) 
	{
		super.onDeleted(context, appWidgetIds);
		Log.d(TAG, "onDeleted() has been triggered.");
		Log.d(TAG, "Number of widgets to delete: " + appWidgetIds.length);
		Log.d(TAG, "widgetId to be deleted: " + appWidgetIds[0]);
		
		SharedPreferences.Editor editor = context.getSharedPreferences("com.vanhlebarsoftware.kmmdroid_preferences", Context.MODE_PRIVATE).edit();
		editor.remove("widgetDatabasePath" + String.valueOf(appWidgetIds[0]));
		editor.remove("accountUsed" + String.valueOf(appWidgetIds[0]));
		editor.remove("updateFrequency" + String.valueOf(appWidgetIds[0]));
		editor.remove("displayWeeks" + String.valueOf(appWidgetIds[0]));
		editor.remove("widgetType" + String.valueOf(appWidgetIds[0]));
		editor.apply();
	}

	/* (non-Javadoc)
	 * @see android.appwidget.AppWidgetProvider#onDisabled(android.content.Context)
	 */
	@Override
	public void onDisabled(Context context) 
	{
		super.onDisabled(context);
		Log.d(TAG, "onDisabled() has been triggered.");
	}

	private void updateWidget(Context context, int widgetID)
	{
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		Log.d(TAG, "attempting to notify widgetId: " + widgetID);
		appWidgetManager.notifyAppWidgetViewDataChanged(widgetID, R.id.schedulesListView);
	}
}
