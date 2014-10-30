package com.vanhlebarsoftware.kmmdroid;

import java.io.File;

import android.util.Log;

public class KMMDDeviceItem implements Comparable<KMMDDeviceItem>
{
	private static final String TAG = KMMDDeviceItem.class.getSimpleName();
	private String name;
	private String type;
	private String path;
	private String dbRevCode;
	private String gdRevCode;
	private String uoRevCode;
	private Boolean DropboxIsDirty;
	private Boolean GoogleDriveIsDirty;
	private Boolean UbuntuOneIsDirty;
	
	public KMMDDeviceItem()
	{
		this.name = null;
		this.type = null;
		this.path = null;
		this.dbRevCode = "";
		this.gdRevCode = "";
		this.uoRevCode = "";
		this.DropboxIsDirty = false;
		this.GoogleDriveIsDirty = false;
		this.UbuntuOneIsDirty = false;
	}
	
	public KMMDDeviceItem(File file)
	{
		this.name = file.getName();
		this.type = file.isFile() ? "File" : "Folder";
		this.path = file.getPath();
		this.dbRevCode = "";
		this.gdRevCode = "";
		this.uoRevCode = "";
		this.DropboxIsDirty = false;
		this.GoogleDriveIsDirty = false;
		this.UbuntuOneIsDirty = false;	
	}
	
	public KMMDDeviceItem(String n, String t, String p)
	{
		this.name = n;
		this.type = t;
		this.path = p;
		this.dbRevCode = "";
		this.gdRevCode = "";
		this.uoRevCode = "";
		this.DropboxIsDirty = false;
		this.GoogleDriveIsDirty = false;
		this.UbuntuOneIsDirty = false;
	}
	
	public KMMDDeviceItem(String n, String t, String p, String dbRC, String gdRC, String uoRC, Boolean dbDirty, Boolean gdDirty, Boolean uoDirty)
	{
		this.name = n;
		this.type = t;
		this.path = p;
		this.dbRevCode = dbRC;
		this.gdRevCode = gdRC;
		this.uoRevCode = uoRC;
		this.DropboxIsDirty = dbDirty;
		this.GoogleDriveIsDirty = gdDirty;
		this.UbuntuOneIsDirty = uoDirty;
	}
	
	public String getName()
	{
		return this.name;
	}
	
	public void setName(String n)
	{
		this.name = n;
	}
	
	public String getType()
	{
		return this.type;
	}
	
	public void setType(String t)
	{
		this.type = t;
	}
	
	public String getPath()
	{
		return this.path;
	}
	
	public void setPath(String p)
	{
		this.path = p;
	}
	
	public String getServerPath()
	{
		int start = this.path.indexOf("/KMMDroid");
		return this.path.substring(start).substring(9);
	}
	
	public void setIsDirty(Boolean dirty, int service)
	{
		switch(service)
		{
		case KMMDDropboxService.CLOUD_ALL:
			this.DropboxIsDirty = dirty;
			this.GoogleDriveIsDirty = dirty;
			this.UbuntuOneIsDirty = dirty;
			break;
		case KMMDDropboxService.CLOUD_DROPBOX:
			this.DropboxIsDirty = dirty;
			break;
		case KMMDDropboxService.CLOUD_GOOGLEDRIVE:
			this.GoogleDriveIsDirty = dirty;
			break;
		case KMMDDropboxService.CLOUD_UBUNTUONE:
			this.UbuntuOneIsDirty = dirty;
			break;
		}
	}
	
	public Boolean getIsDirty(int service)
	{
		switch(service)
		{
		case KMMDDropboxService.CLOUD_DROPBOX:
			return this.DropboxIsDirty;
		case KMMDDropboxService.CLOUD_GOOGLEDRIVE:
			return this.GoogleDriveIsDirty;
		case KMMDDropboxService.CLOUD_UBUNTUONE:
			return this.UbuntuOneIsDirty;
		default:
			return false;
		}		
	}
	
	public void setRevCode(String revCode, int service)
	{
		switch(service)
		{
		case KMMDDropboxService.CLOUD_DROPBOX:
			this.dbRevCode = revCode;
			break;
		case KMMDDropboxService.CLOUD_GOOGLEDRIVE:
			this.gdRevCode = revCode;
			break;
		case KMMDDropboxService.CLOUD_UBUNTUONE:
			this.uoRevCode = revCode;
			break;
		}
	}
	
	public String getRevCode(int service)
	{
		switch(service)
		{
		case KMMDDropboxService.CLOUD_DROPBOX:
			return this.dbRevCode;
		case KMMDDropboxService.CLOUD_GOOGLEDRIVE:
			return this.gdRevCode;
		case KMMDDropboxService.CLOUD_UBUNTUONE:
			return this.uoRevCode;
		default:
			return null;
		}		
	}
	
	public boolean equals(KMMDDeviceItem diItem)
	{		
		if(this.getType().equals(diItem.getType()))
		{
			// We have the same type (File or Folder) now see if the path is the same, return true if yes, false if not.
			return this.getPath().equals(diItem.getPath());
		}
		else
		{
			Log.d(TAG, "We didn't have the same type, no comparison done!");
			return false;
		}
	}
	
	public int compareTo(KMMDDeviceItem o)
	{			
		if(this.name != null)
			return this.name.toLowerCase().compareTo(o.getName().toLowerCase());
		else
			throw new IllegalArgumentException();
	}
	
	public String toString()
	{
		String tmp = "Type: " + this.type + " Name: " + this.name + " Path: " + this.path + "Server Path: " + this.getServerPath();
		return tmp;
	}
	
	public KMMDDeviceItem copy()
	{
		KMMDDeviceItem tmp = new KMMDDeviceItem(this.name, this.type, this.path, this.dbRevCode, this.gdRevCode, this.uoRevCode,
												this.DropboxIsDirty, this.GoogleDriveIsDirty, this.UbuntuOneIsDirty);
		return tmp;
	}
	
	public KMMDDeviceItem findMatch(String path)
	{
		// Returns the current DeviceItem if our path matches the path of this DeviceItem, otherwise returns null.
		if(this.path.equalsIgnoreCase(path))
			return this;
		else
			return null;
	}	
}
