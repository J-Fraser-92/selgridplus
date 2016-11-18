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
import com.groupon.seleniumgridextras.tasks.config.TaskDescriptions;
import com.groupon.seleniumgridextras.utilities.json.JsonCodec;
import org.apache.log4j.Logger;

public class SafeUnregister extends ExecuteOSTask {


    private static Logger logger = Logger.getLogger(DashboardNodeStatus.class);

    public SafeUnregister() {
        setEndpoint(TaskDescriptions.Endpoints.SAFE_UNREGISTER);
        setDescription(TaskDescriptions.Description.SAFE_UNREGISTER);
        JsonObject params = new JsonObject();
        setAcceptedParams(params);
        setRequestType("GET");
        setResponseType("json");
        setClassname(this.getClass().getCanonicalName());
        setCssClass("btn-success");
        setButtonText("Safe Reboot");
        setEnabledInGui(true);
                
        addResponseDescription(JsonCodec.WebDriver.JSON.CURRENTLY_BUSY, "If node is busy, will wait for current job to finish");
    }

    @Override
    public JsonObject execute(String param) {
        
        logger.info("Call to SafeUnregister");
        
        NodeInformation.SafeUnregister();

        getJsonResponse().addKeyValues("currently_busy", NodeInformation.IsBusy());

        if (!NodeInformation.IsBusy()) {
            new StopGrid().execute("5555");
        }
        return getJsonResponse().getJson();
    }
}
