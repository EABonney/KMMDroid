package com.vanhlebarsoftware.kmmdroid;

import org.acra.collector.CrashReportData;
import org.acra.sender.ReportSender;
import org.acra.sender.ReportSenderException;

public class KMMDReportSender implements ReportSender 
{
	KMMDroidApp KMMDapp;
	
    public KMMDReportSender(KMMDroidApp KMMDapp)
    {
        // initialize your sender with needed parameters
        // Get our application
        this.KMMDapp = KMMDapp;
    }

    public void send(CrashReportData report) throws ReportSenderException
    {
        // Iterate over the CrashReportData instance and do whatever
        // you need with each pair of ReportField key / String value
    }

}
