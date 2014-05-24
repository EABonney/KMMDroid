package com.vanhlebarsoftware.kmmdroid;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class WidgetPreferredAccounts extends AppWidgetProvider
{

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
			Intent intent = new Intent(context, WidgetPreferredAccountsRVSerivce.class);
			
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
			
			// Notify the App Widget Manager to update the widget using the modified remote view.
			appWidgetManager.updateAppWidget(appWidgetId, views);
		}
	}
	
}
