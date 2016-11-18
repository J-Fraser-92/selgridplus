package com.groupon.seleniumgridextras.tasks;

import com.google.gson.JsonObject;
import com.groupon.seleniumgridextras.loggers.CustomSessionLogger;
//import com.groupon.seleniumgridextras.loggers.SessionHistoryLog;
import com.groupon.seleniumgridextras.tasks.config.TaskDescriptions;
import com.groupon.seleniumgridextras.utilities.TimeFrame;
import org.apache.log4j.Logger;

public class ServedSessionCount extends ExecuteOSTask {

    private static Logger logger = Logger.getLogger(ServedSessionCount.class);
    private CustomSessionLogger sessionLog = new CustomSessionLogger();

    public ServedSessionCount() {
        setEndpoint(TaskDescriptions.Endpoints.SERVED_SESSION_COUNT);
        setDescription(TaskDescriptions.Description.SERVED_SESSION_COUNT);
        JsonObject params = new JsonObject();

        addResponseDescription("today", "The amount of sessions served today");
        addResponseDescription("yesterday", "The amount of sessions served yesterday");
        addResponseDescription("last_week", "The amount of sessions served in the last week (Monday-Sunday)");
        addResponseDescription("week_so_far", "The amount of sessions served this current week (Monday-Today)");
        addResponseDescription("quarter", "The amount of sessions served this quarter");
        addResponseDescription("last_quarter", "The amount of sessions served last quarter");

        setAcceptedParams(params);
        setRequestType("GET");
        setResponseType("json");
        setClassname(this.getClass().getCanonicalName().toString());
        setCssClass("btn-info");

    }

    @Override
    public JsonObject execute() {

        getJsonResponse().addKeyValues("today", sessionLog.getSessions(TimeFrame.Length.TODAY));
        getJsonResponse().addKeyValues("yesterday", sessionLog.getSessions(TimeFrame.Length.YESTERDAY));
        getJsonResponse().addKeyValues("last_week", sessionLog.getSessions(TimeFrame.Length.LAST_WEEK));
        getJsonResponse().addKeyValues("week_so_far", sessionLog.getSessions(TimeFrame.Length.WEEK_SO_FAR));
        getJsonResponse().addKeyValues("quarter", sessionLog.getSessions(TimeFrame.Length.QUARTER));
        getJsonResponse().addKeyValues("last_quarter", sessionLog.getSessions(TimeFrame.Length.LAST_QUARTER));

        return getJsonResponse().getJson();
    }
}
