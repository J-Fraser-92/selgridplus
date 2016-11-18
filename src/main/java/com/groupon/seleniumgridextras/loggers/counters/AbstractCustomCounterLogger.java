/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.groupon.seleniumgridextras.loggers.counters;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.groupon.seleniumgridextras.loggers.AbstractCustomLogger;
import com.groupon.seleniumgridextras.utilities.FileIOUtility;
import com.groupon.seleniumgridextras.utilities.json.JsonParserWrapper;
import org.apache.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author JamesFraser
 */
public abstract class AbstractCustomCounterLogger extends AbstractCustomLogger {

    private Logger logger = Logger.getLogger(AbstractCustomCounterLogger.class);
    private Map<String, Double> counts;

    @Override
    public void recordNew(String input) {
        initialise();

        readInExistingHistory();
        incrementCount(input);

        backupToFile();
    }

    private void readInExistingHistory() {
        try {
            String fileContents = FileIOUtility.getAsString(getTodaysLog());
            counts = JsonParserWrapper.toHashMap(fileContents);
        } catch (FileNotFoundException e) {
            logger.error(String.format("Error reading previous log file %s", getTodaysLog().getAbsolutePath()), e);
        } catch (JsonSyntaxException e) {
            logger.error(String.format("File %s seems to have corrupted JSON, will start fresh", getTodaysLog().getAbsolutePath()), e);
        } catch (Exception e) {
            logger.error("Some unhandled error occurred, will get rid of the previous log file and start fresh", e);
        }

        if (counts == null) {
            counts = new HashMap();
        }
    }

    private void incrementCount(String key) {
        try {
            if (counts.containsKey(key)) {
                counts.put(key, (counts.get(key)) + 1.0);
            } else {
                counts.put(key, 1.0);
            }
        } catch (Exception e) {
            logger.warn("Unable to add count for: " + key);
            logger.warn(e);
        }
    }

    private void backupToFile() {
        try {
            FileIOUtility.writePrettyJsonToFile(getTodaysLog(), counts);
        } catch (IOException e) {
            logger.warn("Unable to backup session threads to file for " + getTodaysLog().getAbsolutePath());
            logger.warn(e);
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public JsonObject getTodaysKeysCounts() {
        initialise();
        JsonObject json = new JsonObject();

        String error = "";

        try {
            String fileContents = FileIOUtility.getAsString(getTodaysLog());
            Map map = JsonParserWrapper.toHashMap(fileContents);

            if (map != null) {
                for (Object key : map.keySet()) {
                    String text = map.get(key).toString();
                    int parsed = (int) Double.parseDouble(text);
                    json.addProperty(key.toString(), parsed);
                }
            }

        } catch (FileNotFoundException e) {
            error = String.format("A file that existed a minute ago is now missing, %s\n%s",
                    getTodaysLog().getAbsolutePath(), e.getMessage());
            logger.error(error, e);
        } catch (JsonSyntaxException e) {
            error = String.format("File %s seems to be corrupted", getTodaysLog().getAbsolutePath());
            logger.error(error, e);
        } catch (Exception e) {
            error = String.format("Something went wrong and was not caught \n");
            logger.error(error, e);
        }

        return json;
    }
}
