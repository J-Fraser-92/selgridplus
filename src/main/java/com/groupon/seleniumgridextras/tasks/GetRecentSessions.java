/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.groupon.seleniumgridextras.tasks;

/**
 *
 * @author JamesFraser
 */

import com.google.gson.JsonObject;
import com.groupon.seleniumgridextras.config.NodeInformation;
import com.groupon.seleniumgridextras.config.RuntimeConfig;
import com.groupon.seleniumgridextras.loggers.CustomNodeSessionLogger;
import com.groupon.seleniumgridextras.tasks.config.TaskDescriptions;
import com.groupon.seleniumgridextras.utilities.json.JsonCodec;
import org.apache.log4j.Logger;

import java.util.Map;

public class GetRecentSessions extends ExecuteOSTask {

    private static Logger logger = Logger.getLogger(GetRecentSessions.class);

    public GetRecentSessions() {
        setEndpoint(TaskDescriptions.Endpoints.GET_RECENT_SESSIONS);
        setDescription(TaskDescriptions.Description.GET_RECENT_SESSIONS);

        JsonObject params = new JsonObject();
        params.addProperty(JsonCodec.NUMBER, "Number of sessions to be returned. Default 10");
        setAcceptedParams(params);
        setRequestType("GET");
        setResponseType("json");
        setClassname(this.getClass().getCanonicalName().toString());
        setCssClass("btn-success");
        setButtonText("Get recent sessions");
        setEnabledInGui(true);

        addResponseDescription("recent_sessions", "JsonArray of recent X sessions");
    }

    @Override
    public JsonObject execute() {
        getJsonResponse().addKeyValues("recent_sessions", new CustomNodeSessionLogger().getRecentSessions(10));
        return getJsonResponse().getJson();
    }

    @Override
    public JsonObject execute(Map<String, String> parameter) {
        if (!parameter.isEmpty() && parameter.containsKey(JsonCodec.NUMBER)) {
            int sessions = 10;
            try {
                sessions = Integer.parseInt(parameter.get(JsonCodec.NUMBER));
            } catch (Exception e) {
                getJsonResponse().addKeyValues(JsonCodec.WARNING, "Could not parse parameter, defaulting to 10");
            }
            getJsonResponse().addKeyValues("recent_sessions", new CustomNodeSessionLogger().getRecentSessions(sessions));
            return getJsonResponse().getJson();
        }
        return execute();
        
    }


}
