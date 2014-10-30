package com.vanhlebarsoftware.kmmdroid;

import org.acra.*;
import org.acra.annotation.*;
import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.Xml;

@SuppressWarnings("unused")
@ReportsCrashes(formKey="",
mailTo = "bugs@vanhlebarsoftware.com",
customReportContent = { ReportField.REPORT_ID, ReportField.USER_COMMENT, ReportField.ANDROID_VERSION, ReportField.APP_VERSION_NAME, 
						ReportField.BRAND, ReportField.PHONE_MODEL, ReportField.CUSTOM_DATA, ReportField.STACK_TRACE },
mode = ReportingInteractionMode.DIALOG,
resToastText = R.string.crash_toast_text, // optional, displayed as soon as the crash occurs, before collecting data which can take a few seconds
resDialogText = R.string.crash_dialog_text,
resDialogIcon = android.R.drawable.ic_dialog_info, //optional. default is a warning sign
resDialogTitle = R.string.crash_dialog_title, // optional. default is your application name
resDialogCommentPrompt = R.string.crash_dialog_comment_prompt, // optional. when defined, adds a user text field input with this text resource as a label
resDialogOkToast = R.string.crash_dialog_ok_toast // optional. displays a Toast message when the user accepts to send a report.
)
public class KMMDroidApp extends Application implements OnSharedPreferenceChangeListener
{
	private static final String TAG = KMMDroidApp.class.getSimpleName();
    final static public String DEVICE_STATE_FILE = "device_state";
	public static final int ALARM_HOMEWIDGET = 1001;
	public static final int ALARM_NOTIFICATIONS = 1002;
	public SharedPreferences prefs;
	SQLiteDatabase db;
	private String fullPath = null;
	private boolean dbOpen = false;
	private boolean serviceRunning = false;
	private boolean autoUpdate = true;
	private boolean splitsAreDirty = false;
	public ArrayList<Split> Splits;	
	public long flSplitsTotal = 0;
	

	@Override
	public void onCreate()
	{
		ACRA.init(this);
		super.onCreate();
		this.prefs = PreferenceManager.getDefaultSharedPreferences(this);
		this.prefs.registerOnSharedPreferenceChangeListener((OnSharedPreferenceChangeListener) this);
		
		// See if the user has the preference for opening up the last used database. If so, set the path to it.
    	if( this.prefs.getBoolean("openLastUsed", false) )
    	{
    		// First see if the path still exists if not then the user did something and we need to change the user preference and not 
    		// try to open any file this time around.
    		String path = this.prefs.getString("Full Path", "");
    		if( fileExists(path) )
    			setFullPath(path);
    		else
    		{
    			Editor edit = this.prefs.edit();
    			edit.putBoolean("openLastUsed", false);
    			edit.putString("Full Path", "");
    			edit.apply();
    		}
    	}
    	
		String value = this.prefs.getString("updateFrequency", "0");
		if(value.equals("-1"))
			this.setAutoUpdate(true);
		else
			this.setAutoUpdate(false);
		Splits = new ArrayList<Split>();
		Splits.clear();
	}
	
	@Override
	public void onTerminate()
	{
		super.onTerminate();
		
		// Make sure the database is closed.
		db.close();
		dbOpen = false;
	}
	
	public synchronized void onSharedPreferenceChanged( SharedPreferences sharedPreferences, String key)
	{
		SharedPreferences.Editor prefEditor = prefs.edit();
		
		// If the user wants to open the last used database when we start up, store the location.
		if (prefs.getBoolean("openLastUsed", false))
		{
			prefEditor.putString("Full Path", fullPath);
			prefEditor.commit();
		}
		else
		{
			prefEditor.putString("Full Path", "");
			prefEditor.commit();
		}
	}
	
	public void openDB()
	{	
		// always make sure that the db still exists, if not, send the user back to Welcome and pop up an Alert
		// saying that their database is gone somehow. Pass the path to the lost database to Welcome in extras.
		if( fullPath != null )
		{
			if( fileExists(fullPath) )
			{
				db = SQLiteDatabase.openDatabase(fullPath, null, 0);
				dbOpen = true;
			}
			else
			{
				// clear the users preferences in settings.
				Editor edit = this.prefs.edit();
				edit.putBoolean("openLastUsed", false);
				edit.remove("Full Path");
				edit.apply();
			
				// make sure that we no longer show the database open or have a path set for it.
				this.fullPath = null;
				this.dbOpen = false;
			
				// Send the user to Welcome.
				Intent i = new Intent(this, WelcomeActivity.class);
				i.putExtra("lostPath", fullPath);
				i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(i);
			}
		}
		else
		{
			// clear the users preferences in settings.
			Editor edit = this.prefs.edit();
			edit.putBoolean("openLastUsed", false);
			edit.remove("Full Path");
			edit.apply();
		
			// make sure that we no longer show the database open or have a path set for it.
			this.fullPath = null;
			this.dbOpen = false;
		
			// Send the user to Welcome.
			Intent i = new Intent(this, WelcomeActivity.class);
			i.putExtra("lostPath", fullPath);
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(i);			
		}
	}
	
	public void closeDB()
	{
		db.close();
		dbOpen = false;
	}
	
	public void setFullPath(String path)
	{
		this.fullPath = path;
	}
	
	public String getFullPath()
	{
		return this.fullPath;
	}
	
	public boolean isDbOpen()
	{
		return dbOpen;
	}
	
	public void splitsInit()
	{
		Splits = new ArrayList<Split>();
	}
	
	public void splitsDestroy()
	{
		Splits.clear();
	}
	
	public ArrayList<Split> getSplits()
	{
		return Splits;
	}
	
	public void updatePrefs(String widgetId)
	{
		this.fullPath = this.prefs.getString("widgetDatabasePath" + widgetId, null);
		String update = this.prefs.getString("updateFrequency" + widgetId, null);
		if(update.equals("-1"))
			this.autoUpdate = true;
		else
			this.autoUpdate = false;
	}
	
	public void updateFileInfo(String updateColumn, int nChange)
	{
		Cursor cursor;
		ContentValues values = new ContentValues();
		
		if( updateColumn.equals("lastModified") )
		{
	        // get the current date
	        Calendar c = Calendar.getInstance();
	        int intYear = c.get(Calendar.YEAR);
	        int intMonth = c.get(Calendar.MONTH);
	        int intDay = c.get(Calendar.DAY_OF_MONTH);
	        String date = new StringBuilder()
			.append(intYear).append("-")
			.append(intMonth + 1).append("-")
			.append(intDay).toString();
	        
	        values.put("lastModified", date);
		}
		else if ( updateColumn.equals("institutions") )
		{
			final String[] dbColumns = { "institutions" };
			
			cursor = db.query("kmmFileInfo", dbColumns, null, null, null, null, null);
			cursor.moveToFirst();
			
			int id = cursor.getInt(0);
			id = id + nChange;
	
			values.put("institutions", id);			
		}
		else if ( updateColumn.equals("accounts") )
		{
			final String[] dbColumns = { "accounts" };
			
			cursor = db.query("kmmFileInfo", dbColumns, null, null, null, null, null);
			cursor.moveToFirst();
			
			int id = cursor.getInt(0);
			id = id + nChange;
	
			values.put("accounts", id);			
		}
		else if ( updateColumn.equals("payees") )
		{
			final String[] dbColumns = { "payees" };
			
			cursor = db.query("kmmFileInfo", dbColumns, null, null, null, null, null);
			cursor.moveToFirst();
			
			int id = cursor.getInt(0);
			id = id + nChange;
	
			values.put("payees", id);			
		}
		else if ( updateColumn.equals("transactions") )
		{
			final String[] dbColumns = { "transactions" };
			
			cursor = db.query("kmmFileInfo", dbColumns, null, null, null, null, null);
			cursor.moveToFirst();
			
			int id = cursor.getInt(0);
			id = id + nChange;
	
			values.put("transactions", id);			
		}
		else if ( updateColumn.equals("schedules") )
		{
			final String[] dbColumns = { "schedules" };
			
			cursor = db.query("kmmFileInfo", dbColumns, null, null, null, null, null);
			cursor.moveToFirst();
			
			int id = cursor.getInt(0);
			id = id + nChange;
	
			values.put("schedules", id);			
		}
		else if ( updateColumn.equals("splits") )
		{
			final String[] dbColumns = { "splits" };
			
			cursor = db.query("kmmFileInfo", dbColumns, null, null, null, null, null);
			cursor.moveToFirst();
			
			int id = cursor.getInt(0);
			id = id + nChange;
	
			values.put("splits", id);			
		}
		else if ( updateColumn.equals("hiInstitutionsId") )
		{
			final String[] dbColumns = { "hiInstitutionId" };
			
			cursor = db.query("kmmFileInfo", dbColumns, null, null, null, null, null);
			cursor.moveToFirst();
			
			int id = cursor.getInt(0);
			id = id + 1;
	
			values.put("hiInstitutionId", id);			
		}
		else if ( updateColumn.equals("hiPayeeId") )
		{
			final String[] dbColumns = { "hiPayeeId" };
			
			cursor = db.query("kmmFileInfo", dbColumns, null, null, null, null, null);
			cursor.moveToFirst();
			
			int id = cursor.getInt(0);
			id = id + 1;
	
			values.put("hiPayeeId", id);	
		}
		else if ( updateColumn.equals("hiAccountId") )
		{
			final String[] dbColumns = { "hiAccountId" };
			
			cursor = db.query("kmmFileInfo", dbColumns, null, null, null, null, null);
			cursor.moveToFirst();
			
			int id = cursor.getInt(0);
			id = id + 1;
	
			values.put("hiAccountId", id);			
		}
		else if ( updateColumn.equals("hiTransactionId") )
		{
			final String[] dbColumns = { "hiTransactionId" };
			
			cursor = db.query("kmmFileInfo", dbColumns, null, null, null, null, null);
			cursor.moveToFirst();
			
			int id = cursor.getInt(0);
			id = id + 1;
	
			values.put("hiTransactionId", id);			
		}
		else if ( updateColumn.equals("hiScheduleId") )
		{
			final String[] dbColumns = { "hiScheduleId" };
			
			cursor = db.query("kmmFileInfo", dbColumns, null, null, null, null, null);
			cursor.moveToFirst();
			
			int id = cursor.getInt(0);
			id = id + 1;
	
			values.put("hiScheduleId", id);
		}
		
		db.update("kmmFileInfo", values, null, null);
	}
	
	public boolean isServiceRunning()
	{
		return serviceRunning;
	}
	
	public void setServiceRunning(boolean running)
	{
		this.serviceRunning = running;
	}
	
	public void setRepeatingAlarm(String updateValue, Calendar updateTime, int alarmType)
	{
		PendingIntent service = null;
		Intent intent = null;
		
		// Set up the repeating alarm based on the user's preferences.
		final AlarmManager alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		final Calendar TIME = Calendar.getInstance();
		TIME.set(Calendar.MINUTE, 0);
		TIME.set(Calendar.SECOND, 0);
		TIME.set(Calendar.MILLISECOND, 0);
		
		switch( alarmType )
		{
		case ALARM_HOMEWIDGET:
			intent = new Intent(this, KMMDService.class);
			service = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
			
			if(!updateValue.equals("0"))
				alarmMgr.setRepeating(AlarmManager.ELAPSED_REALTIME, TIME.getTime().getTime(), 
						Long.valueOf(updateValue), service);
			else
				alarmMgr.cancel(service);
			break;
		case ALARM_NOTIFICATIONS:
			intent = new Intent(KMMDNotificationsService.CHECK_SCHEDULES);
			service = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
			
			if(updateTime != null)
			{
				// Only set the alarm if it is not already set.
				// Will need to relook at this b/c if the alarm is already set but the time has changed we have a problem.
				if( !isNotificationAlarmSet() )
				{
					Log.d(TAG, "Setup the nofications alarm.");
					Log.d(TAG, "Alarm date: " + (updateTime.get(Calendar.MONTH) + 1) + "-" + updateTime.get(Calendar.DAY_OF_MONTH) + "-" + updateTime.get(Calendar.YEAR));
					Log.d(TAG, "Alarm time: " + updateTime.get(Calendar.HOUR_OF_DAY) + ":" + updateTime.get(Calendar.MINUTE) + ":" + updateTime.get(Calendar.SECOND));
					//alarmMgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, updateTime.getTimeInMillis(), AlarmManager.INTERVAL_DAY, service);
					alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, updateTime.getTimeInMillis(), AlarmManager.INTERVAL_DAY, service);
					SharedPreferences.Editor prefEditor = prefs.edit();
					prefEditor.putBoolean("notificationAlarmSet", true);
					prefEditor.commit();
				}
				else
					Log.d(TAG, "Not setting the notifications alarm as it is alredy set!");
			}
			else
			{
				// Cancel the alarm if it is already set.
				if( isNotificationAlarmSet() )	
				{
					Log.d(TAG, "Canceling the currently setup notifications alarm.");
					SharedPreferences.Editor prefEditor = prefs.edit();
					prefEditor.putBoolean("notificationAlarmSet", false);
					prefEditor.commit();
					alarmMgr.cancel(service);
				}
				else
					Log.d(TAG, "No need to cancel the notifications alarm as it was never set.");
			}
			break;
		}
	}
	
	public void setAutoUpdate(boolean value)
	{
		this.autoUpdate = value;
	}
	
	public boolean getAutoUpdate()
	{
		return this.autoUpdate;
	}
	
	public boolean isNotificationAlarmSet()
	{
		return prefs.getBoolean("notificationAlarmSet", false);
	}
	
	public boolean fileExists(String path)
	{
		File file = new File(path);
		
		return file.exists();		
	}
	
	public void setSplitsAryDirty(boolean dirty)
	{
		this.splitsAreDirty = dirty;
	}
	
	public boolean getSplitsAreDirty()
	{
		return this.splitsAreDirty;
	}
	
	public void markFileIsDirty(Boolean dirty, String widgetId)
	{
		// Need to mark the file as dirty for our cloud services.
    	// Read in the saved deviceState from the xml file and put it into a List<>.
    	List<KMMDDeviceItem> savedDeviceState = new ArrayList<KMMDDeviceItem>();
    	KMMDDeviceItem currentFile = null;
    	KMMDDeviceItemParser parser = new KMMDDeviceItemParser(KMMDDropboxService.DEVICE_STATE_FILE, this);
    	savedDeviceState = parser.parse();
    	
		// Get the correct database
		// If widgetId is 9999, then we are already in the application and need to get the default database.
		// If widgetId is null, then we are coming from a scheduled event for checking schedules of the main app.
		String prefString = null;
		if( widgetId == null )
			prefString = "Full Path";
		else if( widgetId.equals("9999") )
			prefString = "currentOpenedDatabase";
		else
			prefString = "widgetDatabasePath" + String.valueOf(widgetId);

		String path = prefs.getString(prefString, "");
		Log.d(TAG, "Path for the database marked dirty: " + path);
    	
		if( savedDeviceState != null )
		{
			// Find the correct file in our saved state and mark it as dirty.
			for(KMMDDeviceItem item : savedDeviceState)
			{
				currentFile = item.findMatch(path);
				if(currentFile != null)
					break;
				else
					currentFile = new KMMDDeviceItem(new File(path));
			}
		}
		else
		{
			// This is the first time we have run this routine and we only have one file to mark.
			currentFile = new KMMDDeviceItem(new File(path));
		}
		currentFile.setIsDirty(true, KMMDDropboxService.CLOUD_ALL);
		
		if( savedDeviceState != null )
		{
			// Replace this in the savedDeviceState list then write it to disk.
			for(int i=0; i<savedDeviceState.size(); i++)
			{
				if(savedDeviceState.get(i).equals(currentFile))
				{
					savedDeviceState.add(i, currentFile);
					savedDeviceState.remove(i+1);
					i = savedDeviceState.size() + 1;
				}
			}
		}
		else
		{
			savedDeviceState = new ArrayList<KMMDDeviceItem>();
			savedDeviceState.add(currentFile);
		}
		
		writeDeviceState(savedDeviceState);
	}
	
    private void writeDeviceState(List<KMMDDeviceItem> deviceState)
    {
        XmlSerializer serializer = Xml.newSerializer();
        StringWriter writer = new StringWriter();
        try 
        {
            serializer.setOutput(writer);
            serializer.startDocument("UTF-8", true);
            serializer.startTag("", "DeviceState");
            for (KMMDDeviceItem item: deviceState)
            {
           		serializer.startTag("", "item");
           		serializer.startTag("", "type");
           		serializer.text(item.getType());
           		serializer.endTag("", "type");
           		serializer.startTag("", "name");
           		serializer.text(item.getName());
           		serializer.endTag("", "name");
           		serializer.startTag("", "path");
           		serializer.text(item.getPath());
           		serializer.endTag("", "path");
           		serializer.startTag("", "dirtyservices");
           		serializer.attribute("", "Dropbox", String.valueOf(item.getIsDirty(KMMDDropboxService.CLOUD_DROPBOX)));
           		serializer.attribute("", "GoogleDrive", String.valueOf(item.getIsDirty(KMMDDropboxService.CLOUD_GOOGLEDRIVE)));
           		serializer.attribute("", "UbutntoOne", String.valueOf(item.getIsDirty(KMMDDropboxService.CLOUD_UBUNTUONE)));
           		serializer.endTag("", "dirtyservices");
           		serializer.startTag("", "revcodes");
           		serializer.attribute("", "Dropbox", item.getRevCode(KMMDDropboxService.CLOUD_DROPBOX));
           		serializer.attribute("", "GoogleDrive", item.getRevCode(KMMDDropboxService.CLOUD_GOOGLEDRIVE));
           		serializer.attribute("", "UbuntuOne", item.getRevCode(KMMDDropboxService.CLOUD_UBUNTUONE));
           		serializer.endTag("", "revcodes");
           		serializer.endTag("", "item");
            }
            serializer.endTag("", "DeviceState");
            serializer.endDocument();
        } 
        catch (Exception e) 
        {
            throw new RuntimeException(e);
        }
        		
        // Attempt to write the state file to the private storage area.
        String FILENAME = DEVICE_STATE_FILE;
        try 
        {
			FileOutputStream fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
			fos.write(writer.toString().getBytes());
			fos.close();
		} 
        catch (FileNotFoundException e) 
        {
			e.printStackTrace();
		} 
        catch (IOException e) 
        {
			e.printStackTrace();
		}
    }
}
