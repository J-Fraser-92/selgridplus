package com.groupon.seleniumgridextras.tasks;

import com.google.gson.JsonObject;
import com.groupon.seleniumgridextras.loggers.counters.CustomTerminationLogger;
import com.groupon.seleniumgridextras.tasks.config.TaskDescriptions;
import org.apache.log4j.Logger;

public class SessionTerminationsCount extends ExecuteOSTask {

    private static Logger logger = Logger.getLogger(SessionTerminationsCount.class);
    private CustomTerminationLogger terminationLog = new CustomTerminationLogger();
    
    public SessionTerminationsCount() {
        setEndpoint(TaskDescriptions.Endpoints.SESSION_TERMINATION_COUNT);
        setDescription("Displays the types and number of session terminations");
        JsonObject params = new JsonObject();

        addResponseDescription("session_terminations", "The amount of each session termination");

        setAcceptedParams(params);
        setRequestType("GET");
        setResponseType("json");
        setClassname(this.getClass().getCanonicalName().toString());
        setCssClass("btn-info");

    }

    @Override
    public JsonObject execute() {
        
        getJsonResponse().addKeyValues("session_terminations", terminationLog.getTodaysKeysCounts());

        return getJsonResponse().getJson();
    }
}
