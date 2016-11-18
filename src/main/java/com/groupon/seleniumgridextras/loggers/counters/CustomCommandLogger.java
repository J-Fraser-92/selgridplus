/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.groupon.seleniumgridextras.loggers.counters;

import com.groupon.seleniumgridextras.config.DefaultConfig;
import org.apache.log4j.Logger;

import java.io.File;

/**
 *
 * @author JamesFraser
 */
public class CustomCommandLogger extends AbstractCustomCounterLogger {

    private final Logger logger = Logger.getLogger(CustomCommandLogger.class);

    public String getPrefix() {
        return "commands";
    }

    public File getLogDirectory() {
        return DefaultConfig.COMMAND_LOG_DIRECTORY;
    }

}
