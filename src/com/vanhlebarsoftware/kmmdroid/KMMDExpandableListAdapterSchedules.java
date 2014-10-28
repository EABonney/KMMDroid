package com.vanhlebarsoftware.kmmdroid;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public class KMMDExpandableListAdapterSchedules extends BaseExpandableListAdapter
{
    private Context context;
    private ArrayList<String> groups;
    private ArrayList<ArrayList<Schedule>> children;
    KMMDroidApp kmmdApp;

    @Override
    public boolean areAllItemsEnabled()
    {
        return true;
    }

    public KMMDExpandableListAdapterSchedules(Context context, ArrayList<String> groups, ArrayList<ArrayList<Schedule>> children, KMMDroidApp kmmdApp) 
    {
        this.context = context;
        this.groups = groups;
        this.children = children;
        this.kmmdApp = kmmdApp;
    }
    
	public void setData(List<Schedule> data) 
    {
        clear();
        if (data != null) 
        {
        	for(Schedule schedule : data)
        	{
        		addItem(schedule.getTitle(), schedule);
        	}
        }
    }
	
	private void clear()
	{
		groups.clear();
		children.clear();
	}
	
    /**
     * A general add method, that allows you to add a Vehicle to this list
     * 
     * Depending on if the parentId of the Schedule is present or not,
     * the corresponding item will either be added to an existing group if it 
     * exists, else the group will be created and then the item will be added
     * 
     */
    public void addItem(String strParentId, Schedule schedule) 
    {
        if ( !groups.contains(strParentId) ) 
        {
            groups.add(strParentId);
        }
        int index = groups.indexOf(strParentId);
        if (children.size() < index + 1) 
        {
            children.add(new ArrayList<Schedule>());
        }
        children.get(index).add(schedule);
    }

    public void removeItem(int groupPosition, int childPosition)
    {
    	children.get(groupPosition).remove(childPosition);   		
    }
    
    public Object getChild(int groupPosition, int childPosition) 
    {
        return children.get(groupPosition).get(childPosition);
    }

    public long getChildId(int groupPosition, int childPosition) 
    {
        return childPosition;
    }
    
    // Return a child view. You can load your custom layout here.
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) 
    {   	
        if (convertView == null) 
        {
            LayoutInflater infalInflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.schedule_notifications_row, null);
        }    

		LinearLayout row = (LinearLayout) convertView.findViewById(R.id.schNotificationsRow);
		if( childPosition % 2 == 0)
			row.setBackgroundColor(Color.rgb(0x62, 0xB1, 0xF6));
		else
			row.setBackgroundColor(Color.rgb(0x62, 0xa1, 0xc6));
		
    	Schedule schedule = (Schedule) getChild(groupPosition, childPosition); 

    	TextView tvDate = (TextView) convertView.findViewById(R.id.scheduleDate);
    	TextView tvName = (TextView) convertView.findViewById(R.id.scheduleName);
    	TextView tvAmount = (TextView) convertView.findViewById(R.id.scheduleAmount);
    	tvDate.setText(schedule.formatDateString());
        tvName.setText(schedule.getDescription());
        tvAmount.setText(String.format(Transaction.convertToDollars(schedule.getAmount(), true, false)));
        return convertView;
    }

    public int getChildrenCount(int groupPosition) 
    {
        return children.get(groupPosition).size();
    }

    public Object getGroup(int groupPosition) 
    {
        return groups.get(groupPosition);
    }

    public int getGroupCount() 
    {
        return groups.size();
    }

    public long getGroupId(int groupPosition) 
    {
        return groupPosition;
    }

    // Return a group view. You can load your custom layout here.
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) 
    {
        String group = (String) getGroup(groupPosition);
        if (convertView == null) 
        {
            LayoutInflater infalInflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.group_layout, null);
        }
        TextView tv = (TextView) convertView.findViewById(R.id.tvGroup);
        tv.setText(group + " (" + getChildrenCount(groupPosition) + ")");
        return convertView;
    }

    public boolean hasStableIds() 
    {
        return true;
    }

    public boolean isChildSelectable(int arg0, int arg1) 
    {
        return true;
    }
}
