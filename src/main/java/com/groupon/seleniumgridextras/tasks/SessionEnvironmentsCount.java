package com.groupon.seleniumgridextras.tasks;

import com.google.gson.JsonObject;
import com.groupon.seleniumgridextras.loggers.counters.CustomEnvironmentLogger;
import com.groupon.seleniumgridextras.tasks.config.TaskDescriptions;
import org.apache.log4j.Logger;

public class SessionEnvironmentsCount extends ExecuteOSTask {

    private static Logger logger = Logger.getLogger(SessionEnvironmentsCount.class);
    private CustomEnvironmentLogger environmentsLog = new CustomEnvironmentLogger();
    
    public SessionEnvironmentsCount() {
        setEndpoint(TaskDescriptions.Endpoints.SESSION_ENVIRONMENT_COUNT);
        setDescription("Displays the number of session environments per day");
        JsonObject params = new JsonObject();

        addResponseDescription("session_environments", "The amount of each session environments");

        setAcceptedParams(params);
        setRequestType("GET");
        setResponseType("json");
        setClassname(this.getClass().getCanonicalName().toString());
        setCssClass("btn-info");

    }

    @Override
    public JsonObject execute() {
        
        getJsonResponse().addKeyValues("session_environments", environmentsLog.getTodaysKeysCounts());

        return getJsonResponse().getJson();
    }
}
