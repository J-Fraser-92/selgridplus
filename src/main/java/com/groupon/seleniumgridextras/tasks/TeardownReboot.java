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
import com.groupon.seleniumgridextras.grid.SessionTracker;
import com.groupon.seleniumgridextras.tasks.config.TaskDescriptions;
import com.groupon.seleniumgridextras.utilities.ScreenshotUtility;
import java.awt.AWTException;
import java.awt.Color;
import java.awt.image.BufferedImage;
import org.apache.log4j.Logger;

public class TeardownReboot extends ExecuteOSTask {

    public TeardownReboot() {
        setEndpoint(TaskDescriptions.Endpoints.TEARDOWN_REBOOT);
        setDescription(TaskDescriptions.Description.TEARDOWN_REBOOT);
        JsonObject params = new JsonObject();
        setAcceptedParams(params);
        setRequestType("GET");
        setResponseType("json");
        setClassname(this.getClass().getCanonicalName().toString());
        setCssClass("btn-success");
        setButtonText("Check Reboot");
        setEnabledInGui(true);

        addResponseDescription("reboot_requested", "Manual reboot request");
    }

    @Override
    public JsonObject execute(String param) {

        boolean rebootRequested = NodeInformation.GetReboot();

        getJsonResponse().addKeyValues("reboot_requested", rebootRequested);

        if (NodeInformation.DueReboot() && !NodeInformation.IsBusy()) {
            new RebootNode().execute();
        }

        return getJsonResponse().getJson();
    }


}
