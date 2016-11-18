package com.groupon.seleniumgridextras.loggers.counters;

import com.groupon.seleniumgridextras.config.DefaultConfig;
import org.apache.log4j.Logger;

import java.io.File;

public class CustomTerminationLogger extends AbstractCustomCounterLogger {

    private static Logger logger = Logger.getLogger(CustomTerminationLogger.class);

    public String getPrefix() {
        return "terminations";
    }

    public File getLogDirectory() {
        return DefaultConfig.SESSION_TERMINATION_LOG_DIRECTORY;
    }

}
