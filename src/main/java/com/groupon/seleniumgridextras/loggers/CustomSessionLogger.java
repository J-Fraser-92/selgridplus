/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.groupon.seleniumgridextras.loggers;

import com.groupon.seleniumgridextras.config.DefaultConfig;
import com.groupon.seleniumgridextras.utilities.TimeFrame;
import java.io.File;
import java.util.Calendar;
import java.util.Map;
import org.apache.log4j.Logger;

/**
 *
 * @author JamesFraser
 */
public class CustomSessionLogger extends AbstractCustomLogger {

    private final Logger logger = Logger.getLogger(CustomSessionLogger.class);


    public String getPrefix() {
        return "sessions";
    }

    public File getLogDirectory() {
        return DefaultConfig.SESSION_LOG_DIRECTORY;
    }

    public void recordNew(String node, Map<String, Object> details) {
        details.put("timestamp", getTimestamp());
        details.put("node", node);
        recordNew(details);
    }
   
    public int getSessions(TimeFrame.Length length) {
        Calendar cal = Calendar.getInstance();

        cal.add(Calendar.DATE, TimeFrame.offsetFor(length));

        int total = 0;
        for (int i = 0; i < TimeFrame.daysFor(length); i++) {
            total += getEntriesCount(formatDate(cal.getTime()));
            cal.add(Calendar.DATE, -1);
        }
        return total;
    }

}
