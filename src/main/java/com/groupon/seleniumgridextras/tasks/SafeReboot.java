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
import com.groupon.seleniumgridextras.config.NodeInformation;
import com.google.gson.JsonObject;
import com.groupon.seleniumgridextras.tasks.config.TaskDescriptions;
import com.groupon.seleniumgridextras.utilities.json.JsonCodec;
import org.apache.log4j.Logger;

public class SafeReboot extends ExecuteOSTask {

    
    private static Logger logger = Logger.getLogger(DashboardNodeStatus.class);
    
    public SafeReboot() {
        setEndpoint(TaskDescriptions.Endpoints.SAFE_REBOOT);
        setDescription(TaskDescriptions.Description.SAFE_REBOOT);
        JsonObject params = new JsonObject();
        setAcceptedParams(params);
        setRequestType("GET");
        setResponseType("json");
        setClassname(this.getClass().getCanonicalName());
        setCssClass("btn-success");
        setButtonText("Safe Reboot");
        setEnabledInGui(true);
                
        addResponseDescription(JsonCodec.WebDriver.JSON.CURRENTLY_BUSY, "If node is busy, will wait for current job to finish");
        addResponseDescription(JsonCodec.WebDriver.JSON.REBOOT_CONFIRMED, "Confirmation of the node instructed to reboot");
    }

    @Override
    public JsonObject execute(String param) {
        
        logger.info("Call to SafeReboot");
        
        NodeInformation.SafeReboot();

        getJsonResponse().addKeyValues("currently_busy", NodeInformation.IsBusy());
        getJsonResponse().addKeyValues("reboot_confirmed", NodeInformation.DueReboot());

        if (!NodeInformation.IsBusy()) {
            new RebootNode().execute();
        }
        return getJsonResponse().getJson();
    }
}
