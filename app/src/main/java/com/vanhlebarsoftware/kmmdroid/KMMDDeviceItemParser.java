package com.vanhlebarsoftware.kmmdroid;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;

import android.content.Context;
import android.sax.Element;
import android.sax.EndElementListener;
import android.sax.EndTextElementListener;
import android.sax.RootElement;
import android.sax.StartElementListener;
import android.util.Xml;

public class KMMDDeviceItemParser extends BaseDeviceItemParser
{
	public KMMDDeviceItemParser(String xmlFile, Context context)
	{
		super(xmlFile, context);
	}
	
	public List<KMMDDeviceItem> parse()
	{
		final KMMDDeviceItem currentDeviceItem = new KMMDDeviceItem();
		final List<KMMDDeviceItem> deviceItems = new ArrayList<KMMDDeviceItem>();
		RootElement root = new RootElement("DeviceState");
		Element item = root.getChild(ITEM);
		
		item.setEndElementListener(new EndElementListener()
		{
			public void end()
			{
				deviceItems.add(currentDeviceItem.copy());
			}
		});
		
		item.getChild(NAME).setEndTextElementListener(new EndTextElementListener()
		{
			public void end(String name)
			{
				currentDeviceItem.setName(name);
			}
		});
		
		item.getChild(PATH).setEndTextElementListener(new EndTextElementListener()
		{
			public void end(String path)
			{
				currentDeviceItem.setPath(path);
			}
		});
		
		item.getChild(TYPE).setEndTextElementListener(new EndTextElementListener()
		{
			public void end(String data)
			{
				currentDeviceItem.setType(data);
			}
		});
		
		item.getChild(ISDIRTY).setStartElementListener(new StartElementListener()
		{

			public void start(Attributes attributes) 
			{
				currentDeviceItem.setIsDirty(Boolean.valueOf(attributes.getValue("Dropbox")), KMMDDropboxService.CLOUD_DROPBOX);
				currentDeviceItem.setIsDirty(Boolean.valueOf(attributes.getValue("GoogleDrive")), KMMDDropboxService.CLOUD_GOOGLEDRIVE);
				currentDeviceItem.setIsDirty(Boolean.valueOf(attributes.getValue("UbuntuOne")), KMMDDropboxService.CLOUD_UBUNTUONE);				
			}
			
		});
		
		item.getChild(REVCODES).setStartElementListener(new StartElementListener()
		{

			public void start(Attributes attributes) 
			{
				currentDeviceItem.setRevCode(attributes.getValue("Dropbox"), KMMDDropboxService.CLOUD_DROPBOX);
				currentDeviceItem.setRevCode(attributes.getValue("GoogleDrive"), KMMDDropboxService.CLOUD_GOOGLEDRIVE);
				currentDeviceItem.setRevCode(attributes.getValue("UbuntuOne"), KMMDDropboxService.CLOUD_UBUNTUONE);				
			}
			
		});
		
		try
		{
			Xml.parse(this.getInputStream(), Xml.Encoding.UTF_8, root.getContentHandler());
		}
		catch (Exception e)
		{
			return null;
		}
		
		return deviceItems;
	}
}

abstract class BaseDeviceItemParser implements KMMDDeviceStateParser
{
	// names of our XML tags
	static final String ITEM = "item";
	static final String NAME = "name";
	static final String PATH = "path";
	static final String TYPE = "type";
	static final String ISDIRTY = "dirtyservices";
	static final String REVCODES = "revcodes";
	final String xmlDeviceState;
	private Context context;
	int service;
	
	protected BaseDeviceItemParser(String deviceStateFile, Context context)
	{
		this.xmlDeviceState = deviceStateFile;
		this.context = context;
		this.service = 0;
	}
	
	protected FileInputStream getInputStream()
	{
		try
		{
			return context.openFileInput(this.xmlDeviceState);	
		}
        catch (FileNotFoundException e) 
        {
        	return null;
		} 
	}
}
