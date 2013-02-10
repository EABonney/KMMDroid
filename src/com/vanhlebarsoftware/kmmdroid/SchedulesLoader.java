package com.vanhlebarsoftware.kmmdroid;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

public class SchedulesLoader extends AsyncTaskLoader<List<Schedule>>
{
	private final static String TAG = SchedulesLoader.class.getSimpleName();
	private static final String[] dbColumns = { "kmmSchedules.id AS _id", "kmmSchedules.name AS Description", "occurence", "occurenceString", "occurenceMultiplier",
												"nextPaymentDue", "startDate", "endDate", "lastPayment", "valueFormatted", "autoEnter" };
	private static final String dbSelection = "kmmSchedules.id = kmmSplits.transactionId AND nextPaymentDue > 0" + 
												" AND ((occurence = 1 AND lastPayment IS NULL) OR occurence != 1)" +
												" AND kmmSplits.splitId = 0";
	private static final String dbOrderBy = "nextPaymentDue ASC";

	List<Schedule> mSchedules;
	Context mContext;
	Bundle mBundle;
	
	public SchedulesLoader(Context context, Bundle extras) 
	{
		super(context);
		this.mContext = context;
		this.mBundle = extras;
	}

	@Override
	public List<Schedule> loadInBackground() 
	{	
		return getSchedules();
	}
	
    /**
     * Called when there is new data to deliver to the client.  The
     * super class will take care of delivering it; the implementation
     * here just adds a little more logic.
     */
    @Override 
    public void deliverResult(List<Schedule> schedules) 
    {
        if (isReset()) 
        {
            // An async query came in while the loader is stopped.  We
            // don't need the result.
            if (schedules != null) 
            {
                onReleaseResources(schedules);
            }
        }
        List<Schedule> oldSchedules = schedules;
        mSchedules = schedules;

        if (isStarted()) 
        {
            // If the Loader is currently started, we can immediately
            // deliver its results.
            super.deliverResult(schedules);
        }

        // At this point we can release the resources associated with
        // 'oldApps' if needed; now that the new result is delivered we
        // know that it is no longer in use.
        if (oldSchedules != null) 
        {
            onReleaseResources(oldSchedules);
        }    	
    }
    
    /**
     * Handles a request to start the Loader.
     */
    @Override 
    protected void onStartLoading() 
    {
        if (mSchedules != null) 
        {
            // If we currently have a result available, deliver it
            // immediately.
            deliverResult(mSchedules);
        }
        
        if (takeContentChanged() || mSchedules == null )
        {
            // If the data has changed since the last time it was loaded
            // or is not currently available, start a load.
            forceLoad();
        }
    }
 
    /**
     * Handles a request to stop the Loader.
     */
    @Override protected void onStopLoading() 
    {
        // Attempt to cancel the current load task if possible.
        cancelLoad();
    }

    /**
     * Handles a request to cancel a load.
     */
    @Override 
    public void onCanceled(List<Schedule> schedules) 
    {
        super.onCanceled(schedules);

        // At this point we can release the resources associated with 'apps'
        // if needed.
        onReleaseResources(schedules);
    }

    /**
     * Handles a request to completely reset the Loader.
     */
    @Override 
    protected void onReset() 
    {
        super.onReset();
        
        // Ensure the loader is stopped
        onStopLoading();

        // At this point we can release the resources associated with 'apps'
        // if needed.
        if (mSchedules != null) 
        {
            onReleaseResources(mSchedules);
            mSchedules = null;
        }
    }
    
    /**
     * Helper function to take care of releasing resources associated
     * with an actively loaded data set.
     */
    protected void onReleaseResources(List<Schedule> schedules) 
    {
        // For a simple List<> there is nothing to do.  For something
        // like a Cursor, we would close it here.
    }
    
    private List<Schedule> getSchedules()
    {
    	ArrayList<Schedule> schedules = new ArrayList<Schedule>();
    	ArrayList<Schedule> finalList = new ArrayList<Schedule>();
    	ArrayList<Schedule> pastDueSchedules = new ArrayList<Schedule>();
    	ArrayList<Schedule> dueTodaySchedules = new ArrayList<Schedule>();
    	ArrayList<Schedule> autoEnteredSchedules = new ArrayList<Schedule>();
    	ArrayList<String> autoEnteredScheduleIds = new ArrayList<String>();
    	
    	// Pull the information from out bundle that was passed to the loader.
        String accountUsed = this.mBundle.getString("accountUsed");
        //dbSelection = schedulesSelection;// + "'" + accountUsed + "'";
        autoEnteredScheduleIds = this.mBundle.getStringArrayList("autoEnteredScheduleIds");
    	
		String frag = "#9999";
		Uri u = Uri.withAppendedPath(KMMDProvider.CONTENT_SCHEDULE_URI, frag);
		u = Uri.parse(u.toString());
		Cursor cursor = getContext().getContentResolver().query(KMMDProvider.CONTENT_SCHEDULE_URI, null, null, null, null);
		//Cursor cursor = getContext().getContentResolver().query(u, dbColumns, dbSelection, null, dbOrderBy);
		//Cursor cursor = KMMDapp.db.query(schedulesTable, schedulesColumns, dbSelection, null, null, null, schedulesOrderBy);
		
		GregorianCalendar calToday = new GregorianCalendar();
		GregorianCalendar calYesterday = new GregorianCalendar();
		calYesterday = (GregorianCalendar) calToday.clone();
		calYesterday.add(Calendar.DAY_OF_MONTH, -1);
		String strToday = String.valueOf(calToday.get(Calendar.YEAR)) + "-" + String.valueOf(calToday.get(Calendar.MONTH)+ 1) + "-"
				+ String.valueOf(calToday.get(Calendar.DAY_OF_MONTH));
		String strYesterday = String.valueOf(calYesterday.get(Calendar.YEAR)) + "-" + String.valueOf(calYesterday.get(Calendar.MONTH)+ 1) + "-"
				+ String.valueOf(calYesterday.get(Calendar.DAY_OF_MONTH));
		
		// Make sure all the ArrayLists are clear.
		schedules.clear();
		pastDueSchedules.clear();
		dueTodaySchedules.clear();
		autoEnteredSchedules.clear();
		
		Log.d(TAG, "Schedules returned from query: " + cursor.getCount());
		// We have our open schedules from the database, now create the user defined period of cash flow.
		schedules = Schedule.BuildCashRequired(cursor, Schedule.padFormattedDate(strYesterday), Schedule.padFormattedDate(strToday), 
												Transaction.convertToPennies("0.00"), getContext(), "9999");
		Log.d(TAG, "Schedules that are overDue or dueToday: " + schedules.size());
		// Seperate out the schedules for use in the adapters.
		for(int i=0; i < schedules.size(); i++)
		{
			if(schedules.get(i).isPastDue())
				pastDueSchedules.add(schedules.get(i));
			else if(schedules.get(i).isDueToday())
				dueTodaySchedules.add(schedules.get(i));
		}
		
		// Need to get the schedules that where entered based on auto enter preferences.
		Cursor cur = null;
		String selection = dbSelection + " AND id=?";
		for(int i=0; i<autoEnteredScheduleIds.size(); i++)
		{
			frag = "#9999";
			u = Uri.withAppendedPath(KMMDProvider.CONTENT_SCHEDULE_URI, autoEnteredScheduleIds.get(i) + frag);
			u = Uri.parse(u.toString());
			cur = getContext().getContentResolver().query(u, dbColumns, selection, null, null);
			//cur = KMMDapp.db.query(schedulesTable, schedulesColumns, selection, new String[] { autoEnteredScheduleIds.get(i) }, null, null, null);
			cur.moveToFirst();
			Calendar today = new GregorianCalendar();
			autoEnteredSchedules.add(new Schedule(cur.getString(1), today, cur.getString(9), getContext(), "9999"));
			cur.close();
		}
		
		// Add the items that are past due.
		if(this.mBundle.getBoolean("showOverDue", false))
		{
			for(int i=0; i<pastDueSchedules.size(); i++)
			{
				// Add the title attribute now.
				pastDueSchedules.get(i).setTitle(getContext().getString(R.string.titlePastDue));
				finalList.add(pastDueSchedules.get(i));
			}
				//adapter.addItem(getString(R.string.titlePastDue), pastDueSchedules.get(i));
		}
		
		// Add the items that are due today.
		if(this.mBundle.getBoolean("showDueToday", false))
		{
			for(int i=0; i<dueTodaySchedules.size(); i++)
			{
				// add the title attribute now
				dueTodaySchedules.get(i).setTitle(getContext().getString(R.string.titleDueToday));
				finalList.add(dueTodaySchedules.get(i));
				//adapter.addItem(getString(R.string.titleDueToday), dueTodaySchedules.get(i));
			}
		}
		
		// Add the items that where auto entered.
		for(int i=0; i<autoEnteredSchedules.size(); i++)
		{
			// add the title attribute now
			autoEnteredSchedules.get(i).setTitle(getContext().getString(R.string.titleAutoEntered));
			finalList.add(autoEnteredSchedules.get(i));
			//adapter.addItem(getString(R.string.titleAutoEntered), autoEnteredSchedules.get(i));
		}
		
		// Close the cursor to free up memory.
		cursor.close();
    	
		return schedules;
    }

}

