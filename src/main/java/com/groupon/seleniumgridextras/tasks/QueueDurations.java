package com.groupon.seleniumgridextras.tasks;

import com.google.gson.JsonObject;
import com.groupon.seleniumgridextras.loggers.CustomQueueLogger;
//import com.groupon.seleniumgridextras.loggers.QueueDurationLog;
import com.groupon.seleniumgridextras.tasks.config.TaskDescriptions;
import org.apache.log4j.Logger;
import java.util.Map;

public class QueueDurations extends ExecuteOSTask {

    private static Logger logger = Logger.getLogger(QueueDurations.class);
    private CustomQueueLogger queueLog = new CustomQueueLogger();

    public QueueDurations() {
        setEndpoint(TaskDescriptions.Endpoints.QUEUE_DURATIONS);
        setDescription(TaskDescriptions.Description.QUEUE_DURATIONS);
        JsonObject params = new JsonObject();

        addResponseDescription("under_minute_day_pc", "The amount of sessions served within a minute today");
        addResponseDescription("under_minute_week_pc", "The amount of sessions served within a minute in the last 7 days");
        addResponseDescription("under_minute_month_pc", "The amount of sessions served within a minute in the last 28 days");

        addResponseDescription("average_queue_day_seconds", "The average duration taken to serve a session today");
        addResponseDescription("average_queue_week_seconds", "The average duration taken to serve a session in the last 7 days");
        addResponseDescription("average_queue_month_seconds", "The average duration taken to serve a session in the last 28 days");

        setAcceptedParams(params);
        setRequestType("GET");
        setResponseType("json");
        setClassname(this.getClass().getCanonicalName().toString());
        setCssClass("btn-info");

    }

    @Override
    public JsonObject execute() {

        //Map<String, Double> countMap = QueueDurationLog.getQueueStats();
        Map<String, Double> countMap = queueLog.getDashboardMetrics();
        String[] keys = new String[]{
            "average_queue_day_seconds",
            "under_minute_day_pc",
            "average_queue_week_seconds",
            "under_minute_week_pc",
            "average_queue_month_seconds",
            "under_minute_month_pc"
        };

        for (String key : keys) {
            if (countMap.containsKey(key)) {
                getJsonResponse().addKeyValues(key, countMap.get(key));
            } else {
                getJsonResponse().addKeyValues(key, "error retrieving");
            }
        }

        return getJsonResponse().getJson();
    }
}
