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
import android.widget.Toast;

public class WidgetPreferredAccounts extends AppWidgetProvider
{
	private static final String TAG = WidgetPreferredAccounts.class.getSimpleName();
	public static final String RECEIVE_HOME_UPDATE_NOTIFICATIONS = "com.vanhlebarsoftware.kmmdroid.RECEIVE_HOME_UPDATE_NOTIFICATIONS";
	public static final String DATA_CHANGED = "com.vanhlebarsoftware.kmmdroid.DATA_CHANGED";
	private static final String URI_SCHEME = "com.vanhlebarsoftware.kmmdroid";
	
	/* (non-Javadoc)
	 * @see android.appwidget.AppWidgetProvider#onUpdate(android.content.Context, android.appwidget.AppWidgetManager, int[])
	 */
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) 
	{
		// Iterate over the array of active widgets.
		final int N = appWidgetIds.length;
		
		for(int i=0; i<N; i++)
		{
			int appWidgetId = appWidgetIds[i];
			
			// Set up the intent to start the RemoteViews Service which will supply the views
			// shown in the ListView
			Intent intent = new Intent(context, WidgetPreferredAccountsRVService.class);
			
			// Add the app widget ID to the intent extras.
			intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
			
			// Instantiate the RemoteViews object for the App Widget layout.
			RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.preferred_accounts_widget);
			
			// Setup the RemoteViews object to use a RemoteViews adapter.
			views.setRemoteAdapter(R.id.preferredListView, intent);
			
			// The empty view is displayed when the collection has no items.
			views.setEmptyView(R.id.preferredListView, R.id.empty_widget_text);
			
			// Create a Pending Intent template to provide interactivity to each item displayed
			Intent templateIntent = new Intent(context, LedgerActivity.class);
			templateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
			PendingIntent templatePendingIntent = PendingIntent.getActivity(context, 0, templateIntent, PendingIntent.FLAG_UPDATE_CURRENT);
			views.setPendingIntentTemplate(R.id.preferredListView, templatePendingIntent);
			
			// Setup the onClick response to the various buttons on the widget
			// Start application by clicking on the icon
			intent = new Intent(context, WelcomeActivity.class);
			intent.putExtra("fromWidgetId", String.valueOf(appWidgetId));
			String action = "com.vanhlebarsoftware.kmmdroid.Welcome" + "#" + String.valueOf(appWidgetId);
			intent.setAction(action);
			PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
			views.setOnClickPendingIntent(R.id.kmmd_icon, pendingIntent);
			
			// Refresh icon
			intent = new Intent(context, WidgetPreferredAccounts.class);
			intent.putExtra(DATA_CHANGED, String.valueOf(appWidgetId));
			Uri uri = Uri.withAppendedPath(Uri.parse(URI_SCHEME + "://widget/id/"),String.valueOf(appWidgetId));
			intent.setAction(DATA_CHANGED);
			intent.setData(uri);
			pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
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
		}
	}

	/* (non-Javadoc)
	 * @see android.appwidget.AppWidgetProvider#onReceive(android.content.Context, android.content.Intent)
	 */
	@Override
	public void onReceive(Context context, Intent intent) 
	{
		// TODO Auto-generated method stub
		super.onReceive(context, intent);
	
		if(intent.getAction().equalsIgnoreCase(DATA_CHANGED))
		{
			// We know we want to refresh the widget/widgets, now determine if a single widget or ALL need to be refreshed.
			String widgetId = intent.getStringExtra(DATA_CHANGED);
			if(!widgetId.equalsIgnoreCase("0"))
			{
				Log.d(TAG, "Refreshing widgetId: " + widgetId);
				int id = Integer.valueOf(widgetId);
				updateWidget(context, id);
			}
			else
			{
				Log.d(TAG, "Refreshing all preferred accounts widgets.");
				updateWidgets(context);
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see android.appwidget.AppWidgetProvider#onDeleted(android.content.Context, int[])
	 */
	@Override
	public void onDeleted(Context context, int[] appWidgetIds) 
	{
		// TODO Auto-generated method stub
		super.onDeleted(context, appWidgetIds);
		
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
		// TODO Auto-generated method stub
		super.onDisabled(context);
	}

	private void updateWidget(Context context, int ID)
	{
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		appWidgetManager.notifyAppWidgetViewDataChanged(ID, R.id.preferredListView);
		Toast.makeText(context, "Refreshing...", Toast.LENGTH_LONG).show();
	}
	
	private void updateWidgets(Context context)
	{
		ComponentName thisWidget = new ComponentName(context, WidgetPreferredAccounts.class);
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		int[] widgetIDs = appWidgetManager.getAppWidgetIds(thisWidget);
		appWidgetManager.notifyAppWidgetViewDataChanged(widgetIDs, R.id.preferredListView);	
	}
	
}
