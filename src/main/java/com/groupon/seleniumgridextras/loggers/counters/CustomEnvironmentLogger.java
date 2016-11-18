/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.groupon.seleniumgridextras.loggers.counters;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.groupon.seleniumgridextras.config.DefaultConfig;
import com.groupon.seleniumgridextras.utilities.FileIOUtility;
import com.groupon.seleniumgridextras.utilities.json.JsonParserWrapper;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Map;


/**
 *
 * @author JamesFraser
 */
public class CustomEnvironmentLogger extends AbstractCustomCounterLogger {

    private final Logger logger = Logger.getLogger(CustomEnvironmentLogger.class);

    public String getPrefix() {
        return "environments";
    }

    public File getLogDirectory() {
        return DefaultConfig.ENVIRONMENT_LOG_DIRECTORY;
    }

}
