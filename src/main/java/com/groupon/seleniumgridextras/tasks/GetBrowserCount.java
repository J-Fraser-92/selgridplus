package com.groupon.seleniumgridextras.tasks;

import com.google.gson.JsonObject;
import com.groupon.seleniumgridextras.loggers.counters.CustomBrowserLogger;
import com.groupon.seleniumgridextras.loggers.counters.CustomCommandLogger;
import com.groupon.seleniumgridextras.tasks.config.TaskDescriptions;
import org.apache.log4j.Logger;

public class GetBrowserCount extends ExecuteOSTask {

    private static Logger logger = Logger.getLogger(GetBrowserCount.class);
    private CustomBrowserLogger browserLog = new CustomBrowserLogger();

    public GetBrowserCount() {
        setEndpoint(TaskDescriptions.Endpoints.GET_BROWSER_COUNT);
        setDescription("Displays the number of sessions for each browser for the day");
        JsonObject params = new JsonObject();

        addResponseDescription("browsers", "The amount of each browser sessions");

        setAcceptedParams(params);
        setRequestType("GET");
        setResponseType("json");
        setClassname(this.getClass().getCanonicalName().toString());
        setCssClass("btn-info");

    }

    @Override
    public JsonObject execute() {
        
        getJsonResponse().addKeyValues("browsers", browserLog.getTodaysKeysCounts());

        return getJsonResponse().getJson();
    }
}
