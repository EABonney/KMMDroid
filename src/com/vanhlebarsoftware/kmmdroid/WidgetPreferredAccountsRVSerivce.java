package com.vanhlebarsoftware.kmmdroid;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

public class WidgetPreferredAccountsRVSerivce extends RemoteViewsService
{

	@Override
	public RemoteViewsFactory onGetViewFactory(Intent intent) 
	{
		// TODO Auto-generated method stub
		return new WidgetPreferredAccountsRVFactory(getApplicationContext(), intent);
	}

	/************************* Begin inner class WidgetPreferredAccountsRVFactory *****************/
	class WidgetPreferredAccountsRVFactory implements RemoteViewsFactory 
	{
		private Context context;
		private Intent intent;
		private int widgetId;
		private Cursor c;
		
		public WidgetPreferredAccountsRVFactory(Context context, Intent intent)
		{
			this.context = context;
			this.intent = intent;
			widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
		}

		public void onCreate() 
		{
			c = executeQuery();
		}

		public void onDataSetChanged() 
		{
			c = executeQuery();
		}

		public void onDestroy() 
		{
			c.close();
		}

		public int getCount() 
		{
			if(c != null)
				return c.getCount();
			else
				return 0;
		}

		public RemoteViews getViewAt(int position) 
		{
			// Move the cursor to the correct position
			c.moveToPosition(position);
			
			// Get our values from the cursor here
			int idIdx = c.getColumnIndex("id");
			String id = c.getString(idIdx);
			
			// Create the Remote Views object and use it to populate the layout for each account.
			RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.home_row);
			rv.setTextViewText(R.id.hrAccountName, c.getString(c.getColumnIndex("accountName")));
			String value = Transaction.convertToDollars(Transaction.convertToPennies(c.getString(c.getColumnIndex("balance"))), true);
			rv.setTextViewText(R.id.hrAccountBalance, value);
			
			// Create the fill-in Intent that adds the URI for the current item to the template Intent.
			Intent fillInIntent = new Intent();
			Uri uri = Uri.withAppendedPath(KMMDProvider.CONTENT_ACCOUNT_URI, id);
			fillInIntent.setData(uri);
			
			rv.setOnClickFillInIntent(R.id.hrAccountName, fillInIntent);
			rv.setOnClickFillInIntent(R.id.hrAccountBalance, fillInIntent);
			
			return rv;
		}

		public RemoteViews getLoadingView() 
		{
			// TODO Auto-generated method stub
			return null;
		}

		public int getViewTypeCount() 
		{
			// TODO Auto-generated method stub
			return 0;
		}

		public long getItemId(int position) 
		{
			if(c != null )
				return c.getLong(c.getColumnIndex("id"));
			else
				return position;
		}

		public boolean hasStableIds() 
		{
			// TODO Auto-generated method stub
			return false;
		}

		/******************************** Helper functions ************************************/
		private Cursor executeQuery()
		{
			// Run the query against the Content provider to pull ALL "preferred" accounts
			String[] projection = { "id", "accountName", "balance", "kvpId", "kvpData" };
			String selection = "kvpKey='PreferredAccount' AND kvpData='Yes' AND kvpId=id";
			
			return context.getContentResolver().query(KMMDProvider.CONTENT_PREFERREDACCOUNTS_URI, projection, selection, null, null);

		}
	}
	/************************** End of inner class WidgetPreferredAccountsRVFactory ****************/
}
