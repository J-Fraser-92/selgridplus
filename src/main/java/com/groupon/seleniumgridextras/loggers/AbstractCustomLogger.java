/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.groupon.seleniumgridextras.loggers;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import org.apache.log4j.Logger;

/**
 *
 * @author JamesFraser
 */
public abstract class AbstractCustomLogger {

    private Logger logger = Logger.getLogger(AbstractCustomLogger.class);
    private final DateFormat format = new SimpleDateFormat("yyyy_MM_dd");
    private final DateFormat timestampFormat = new SimpleDateFormat("HH:mm:ss");
    private String today = "";

    public File getTodaysLog() {
        today = formatDate(new Date());
        return getLog(today);
    }

    public File getLog(String date) {
        initialise();

        File log = new File(getLogDirectory(), getPrefix() + "_" + date + ".log");
        return createFileIfDoesntExist(log);
    }

    public void recordNew(String input) {
        recordNew(input, false);
    }

    public void recordNew(String input, boolean timestamp) {
        if (timestamp) {
            input = getTimestamp() + " " + input;
        }
        try {
            FileWriter fileWriter = new FileWriter(getTodaysLog(), true);
            BufferedWriter bufferWriter = new BufferedWriter(fileWriter);
            bufferWriter.write(input + "\n");
            bufferWriter.close();
        } catch (Exception e) {
            logger.error("recordNew " + e);
        }
    }

    public void recordNew(Map map) {
        recordNew(map.toString(), false);
    }

    public void recordNew(Map map, boolean timestamp) {
        recordNew(map.toString(), timestamp);
    }

    public int getTodaysEntriesCount() {
        today = formatDate(new Date());
        return getEntriesCount(today);
    }

    public int getEntriesCount(String date) {
        initialise();

        File checkFile = getLog(date);
        return countLines(checkFile);
    }

    protected void initialise() {
        createDirIfDoesntExist(getLogDirectory());
    }

    protected File createDirIfDoesntExist(File file) {
        if (!file.exists()) {
            try {
                file.mkdir();
            } catch (Exception e) {
                logger.error(e);
                return null;
            }
        }
        return file;
    }

    protected File createFileIfDoesntExist(File file) {
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (Exception e) {
                logger.error(e);
                return null;
            }
        }
        return file;
    }

    protected String mapToSingleLineJson(Map<String, Object> map) {

        if (map.isEmpty()) {
            return "{ }";
        }

        String line = "{";
        for (String key : map.keySet()) {
            line += String.format(" \"%s\": \"%s\",", key, map.get(key));
        }
        line = line.substring(0, line.length() - 1);
        line += " }";

        line = line.replace("\n", "").replace("\r", "");

        return line;
    }

    protected int countLines(File file) {
        try {

            InputStream is = new BufferedInputStream(new FileInputStream(file));

            byte[] c = new byte[1024];
            int count = 0;
            int readChars;
            boolean empty = true;
            while ((readChars = is.read(c)) != -1) {
                empty = false;
                for (int i = 0; i < readChars; ++i) {
                    if (c[i] == '\n') {
                        ++count;
                    }
                }
            }
            is.close();
            return (count == 0 && !empty) ? 1 : count;

        } catch (Exception e) {
            logger.error(e);
        }
        return -1;
    }

    protected String formatDate(Date date) {
        return format.format(date);
    }

    protected boolean isToday(String date) {
        today = formatDate(new Date());
        return date.equals(today);
    }

    public String getTimestamp() {
        return timestampFormat.format(new Date());
    }

    protected String[] getLastXLines(int number) {
        int stored = countLines(getTodaysLog());
        if(number > stored) {
            number = stored;
        }
        try {
            BufferedReader br = new BufferedReader(new FileReader(getTodaysLog()));

            int skipLines = stored - number;
            for(int i = 0; i < skipLines; i++) {
                br.readLine();
            }
            String[] saved = new String[number];
            for(int i = 0; i < number; i++) {
                saved[i] = br.readLine();
            }
            return saved;

        } catch (Exception e) {
            logger.error(e);
            return new String[0];
        }
    }

    public abstract String getPrefix();

    public abstract File getLogDirectory();


}
