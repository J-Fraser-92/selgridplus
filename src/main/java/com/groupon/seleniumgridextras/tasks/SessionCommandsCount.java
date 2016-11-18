package com.groupon.seleniumgridextras.tasks;

import com.google.gson.JsonObject;
import com.groupon.seleniumgridextras.loggers.counters.CustomCommandLogger;
import com.groupon.seleniumgridextras.tasks.config.TaskDescriptions;
import org.apache.log4j.Logger;

public class SessionCommandsCount extends ExecuteOSTask {

    private static Logger logger = Logger.getLogger(SessionCommandsCount.class);
    private CustomCommandLogger commandsLog = new CustomCommandLogger();

    public SessionCommandsCount() {
        setEndpoint(TaskDescriptions.Endpoints.SESSION_COMMAND_COUNT);
        setDescription("Displays the number of session commands per day");
        JsonObject params = new JsonObject();

        addResponseDescription("session_commands", "The amount of each session commands");

        setAcceptedParams(params);
        setRequestType("GET");
        setResponseType("json");
        setClassname(this.getClass().getCanonicalName().toString());
        setCssClass("btn-info");

    }

    @Override
    public JsonObject execute() {
        
        getJsonResponse().addKeyValues("session_commands", commandsLog.getTodaysKeysCounts());

        return getJsonResponse().getJson();
    }
}
