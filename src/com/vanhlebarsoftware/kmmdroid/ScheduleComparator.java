package com.vanhlebarsoftware.kmmdroid;

import java.util.Comparator;

public class ScheduleComparator implements Comparator<Schedule>
{
	public int compare(Schedule arg0, Schedule arg1) 
	{
		// TODO Auto-generated method stub
		return arg0.getDueDate().compareTo(arg1.getDueDate());
	}
}
