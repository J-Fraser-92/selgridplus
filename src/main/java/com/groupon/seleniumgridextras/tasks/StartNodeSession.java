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
import com.groupon.seleniumgridextras.loggers.CustomNodeSessionLogger;
import com.groupon.seleniumgridextras.tasks.config.TaskDescriptions;
import com.groupon.seleniumgridextras.utilities.json.JsonCodec;
import com.groupon.seleniumgridextras.utilities.json.JsonParserWrapper;
import org.apache.log4j.Logger;

import java.util.Map;

public class StartNodeSession extends ExecuteOSTask {

    private static Logger logger = Logger.getLogger(StartNodeSession.class);

    public StartNodeSession() {
        setEndpoint(TaskDescriptions.Endpoints.START_NODE_SESSION);
        setDescription(TaskDescriptions.Description.START_NODE_SESSION);

        JsonObject params = new JsonObject();
        params.addProperty(JsonCodec.WebDriver.Grid.ENVIRONMENT, "The environment of the current test");
        params.addProperty(JsonCodec.WebDriver.Grid.BROWSER_NAME, "The requested browser of the current test");
        setAcceptedParams(params);
        setRequestType("GET");
        setResponseType("json");
        setClassname(this.getClass().getCanonicalName().toString());
        setCssClass("btn-success");
        setButtonText("Start recent sessions");
        setEnabledInGui(true);
    }

    @Override
    public JsonObject execute(Map<String, String> parameter) {

        CustomNodeSessionLogger sessionLogger = new CustomNodeSessionLogger();

        String passed = parameter.get(JsonCodec.PARAMETER).replace("\\\"","\"");
        JsonObject sessionDetails = JsonParserWrapper.toJsonObject(passed);
        sessionDetails.addProperty(JsonCodec.WebDriver.Grid.TIMESTAMP, sessionLogger.getTimestamp());
        sessionLogger.recordNew(sessionDetails.toString());

        return new Setup().execute();
    }

}
