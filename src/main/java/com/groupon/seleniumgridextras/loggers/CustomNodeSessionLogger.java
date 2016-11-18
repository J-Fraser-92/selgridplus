/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.groupon.seleniumgridextras.loggers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.groupon.seleniumgridextras.config.DefaultConfig;
import com.groupon.seleniumgridextras.utilities.json.JsonParserWrapper;
import org.apache.log4j.Logger;

import java.io.File;

/**
 *
 * @author JamesFraser
 */
public class CustomNodeSessionLogger extends AbstractCustomLogger {

    private final Logger logger = Logger.getLogger(CustomNodeSessionLogger.class);

    public String getPrefix() {
        return "nodesessions";
    }

    public File getLogDirectory() {
        return DefaultConfig.SESSION_LOG_DIRECTORY;
    }

    public JsonArray getRecentSessions(int number) {

        String[] recentSessions = getLastXLines(number);
        JsonArray results = new JsonArray();

        for(String recent : recentSessions) {
            JsonObject details = JsonParserWrapper.toJsonObject(recent);
            results.add(details);
        }
        return results;
    }
}
