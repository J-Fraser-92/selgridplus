/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.groupon.seleniumgridextras.loggers;

import com.groupon.seleniumgridextras.config.DefaultConfig;
import com.groupon.seleniumgridextras.utilities.QueueDataHolder;
import com.groupon.seleniumgridextras.utilities.TimeFrame;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;

/**
 *
 * @author JamesFraser
 */
public class CustomQueueLogger extends AbstractCustomLogger {

    private final Logger logger = Logger.getLogger(CustomQueueLogger.class);

    public String getPrefix() {
        return "queues";
    }

    public File getLogDirectory() {
        return DefaultConfig.QUEUE_LOG_DIRECTORY;
    }

    public File getStatsLog() {
        initialise();

        File log = new File(getLogDirectory(), getPrefix() + "_stats.log");
        return createFileIfDoesntExist(log);
    }

    public QueueDataHolder getMetrics(String date) {
        initialise();

        if (isToday(date)) {
            return getRawMetrics(date);
        } else {
            return getArchivedMetrics(date);
        }
    }

    public QueueDataHolder getRawMetrics(String date) {

        QueueDataHolder dayMetrics = new QueueDataHolder();

        try {
            BufferedReader br = new BufferedReader(new FileReader(getLog(date)));
            String line;
            while ((line = br.readLine()) != null) {
                try {
                    int value = Integer.parseInt(line);
                    dayMetrics.addValue(value);
                } catch (Exception e) {

                }
            }
        } catch (Exception e) {
            logger.error("getRawMetrics " + e);
        }

        return dayMetrics;
    }

    public QueueDataHolder getArchivedMetrics(String date) {

        try {
            String contents = FileUtils.readFileToString(getStatsLog());

            JSONObject metrics;

            try {
                metrics = new JSONObject(contents);
            } catch (Exception e) {
                metrics = new JSONObject();
            }

            if (metrics.has(date)) {

                JSONObject dayData = metrics.getJSONObject(date);
                QueueDataHolder dayMetrics = new QueueDataHolder(dayData.getInt("total"), dayData.getInt("over"), dayData.getInt("under"));

                return dayMetrics;
            }

            QueueDataHolder dayMetrics = getRawMetrics(date);

            JSONObject dayData = new JSONObject();
            dayData.put("total", dayMetrics.getTotal());
            dayData.put("over", dayMetrics.getOver());
            dayData.put("under", dayMetrics.getUnder());

            metrics.put(date, dayData);

            try {
                FileWriter fileWriter = new FileWriter(getStatsLog().getPath(), false);
                BufferedWriter bufferWritter = new BufferedWriter(fileWriter);
                bufferWritter.write(metrics.toString());
                bufferWritter.close();
            } catch (Exception e) {
                logger.error("getArchivedMetrics " + e);
            }

            return dayMetrics;
        } catch (Exception e) {
            logger.error("getArchivedMetrics " + e);
        }
        return new QueueDataHolder();
    }

    public QueueDataHolder getQueueMetrics(TimeFrame.Length length) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, TimeFrame.offsetFor(length));

        QueueDataHolder totalMetrics = new QueueDataHolder();

        for (int i = 0; i < TimeFrame.daysFor(length); i++) {
            QueueDataHolder dayMetrics = getMetrics(formatDate(cal.getTime()));
            totalMetrics.merge(dayMetrics);
            cal.add(Calendar.DATE, -1);
        }

        return totalMetrics;
    }

    public Map<String, Double> getDashboardMetrics() {

        HashMap<String, Double> metrics = new HashMap<String, Double>();

        QueueDataHolder day = getQueueMetrics(TimeFrame.Length.TODAY);
        metrics.put("average_queue_day_seconds", day.getAverage());
        metrics.put("under_minute_day_pc", day.getTargetPercentage());

        QueueDataHolder week = getQueueMetrics(TimeFrame.Length.PAST_SEVEN_DAYS);
        metrics.put("average_queue_week_seconds", week.getAverage());
        metrics.put("under_minute_week_pc", week.getTargetPercentage());

        QueueDataHolder month = getQueueMetrics(TimeFrame.Length.PAST_TWENTY_EIGHT_DAYS);
        metrics.put("average_queue_month_seconds", month.getAverage());
        metrics.put("under_minute_month_pc", month.getTargetPercentage());

        return metrics;
    }

}
