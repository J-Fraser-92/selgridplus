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
import com.groupon.seleniumgridextras.config.RuntimeConfig;
import com.groupon.seleniumgridextras.tasks.config.TaskDescriptions;
import com.groupon.seleniumgridextras.utilities.json.JsonCodec;
import org.apache.log4j.Logger;

public class DashboardNodeStatus extends ExecuteOSTask {

    private static Logger logger = Logger.getLogger(DashboardNodeStatus.class);

    private final boolean supportsIE;
    private final boolean supportsChrome;
    private final boolean supportsFirefox;
    private final boolean supportsSafari;

    public DashboardNodeStatus() {
        setEndpoint(TaskDescriptions.Endpoints.DASHBOARD_NODE_STATUS);
        setDescription(TaskDescriptions.Description.DASHBOARD_NODE_STATUS);
        JsonObject params = new JsonObject();
        setAcceptedParams(params);
        setRequestType("GET");
        setResponseType("json");
        setClassname(this.getClass().getCanonicalName().toString());
        setCssClass("btn-success");
        setButtonText("Dashboard Node Status");
        setEnabledInGui(true);

        String config = RuntimeConfig.getConfig().toJson();
        supportsIE = config.contains("\"browserName\": \"internet explorer\"");
        supportsChrome = config.contains("\"browserName\": \"chrome\"");
        supportsFirefox = config.contains("\"browserName\": \"firefox\"");
        supportsSafari = config.contains("\"browserName\": \"safari\"");

        addResponseDescription(JsonCodec.WebDriver.JSON.IP_ADDRESS, "IP address of node");
        addResponseDescription(JsonCodec.WebDriver.JSON.MACHINE_NAME, "Name of node computer");
        addResponseDescription(JsonCodec.WebDriver.JSON.UPTIME, "Amount of time this node has been up and running");
        addResponseDescription(JsonCodec.WebDriver.JSON.AVAILABLE, "Available for incoming jobs");
        addResponseDescription(JsonCodec.WebDriver.JSON.BUSY, "Currently occupied with a job");
        addResponseDescription(JsonCodec.WebDriver.JSON.SUPPORTS_IE, "Supports Internet Explorer");
        addResponseDescription(JsonCodec.WebDriver.JSON.SUPPORTS_CHROME, "Supports Chrome");
        addResponseDescription(JsonCodec.WebDriver.JSON.SUPPORTS_FIREFOX, "Supports Firefox");
        addResponseDescription(JsonCodec.WebDriver.JSON.SUPPORTS_SAFARI, "Supports Safari");
        addResponseDescription(JsonCodec.WebDriver.JSON.TESTS_RAN, "Tests ran since last reboot");
        addResponseDescription(JsonCodec.WebDriver.JSON.TEST_LIMIT, "Tests to be run before reboot");
        addResponseDescription(JsonCodec.WebDriver.JSON.DUE_REBOOT, "Scheduled to reboot when current job completes");
        addResponseDescription(JsonCodec.WebDriver.JSON.TEST_DURATION_SECONDS, "Int representing how many seconds the current test has been running");
        addResponseDescription(JsonCodec.WebDriver.JSON.IDLE_TIME_SECONDS, "Int representing how many seconds the current test has been running");
        addResponseDescription(JsonCodec.WebDriver.JSON.BROWSER_ACTIVE, "Boolean representing if a browser is active on this node");
        addResponseDescription(JsonCodec.WebDriver.JSON.STATUS, "Current status of this node");
        addResponseDescription(JsonCodec.WebDriver.JSON.HUB_IP, "Returns the ip of the hub");
    }

    @Override
    public boolean initialize() {
        NodeInformation.GetUptime();
        return super.initialize();
    }

    @Override
    public JsonObject execute() {
        getJsonResponse().addKeyValues(JsonCodec.WebDriver.JSON.IP_ADDRESS, NodeInformation.GetIpAddress());        
        getJsonResponse().addKeyValues(JsonCodec.WebDriver.JSON.MACHINE_NAME, NodeInformation.GetHostName());
        getJsonResponse().addKeyValues(JsonCodec.WebDriver.JSON.UPTIME, NodeInformation.GetUptime());
        getJsonResponse().addKeyValues(JsonCodec.WebDriver.JSON.AVAILABLE, NodeInformation.IsAvailable());
        getJsonResponse().addKeyValues(JsonCodec.WebDriver.JSON.BUSY, NodeInformation.IsBusy());
        getJsonResponse().addKeyValues(JsonCodec.WebDriver.JSON.SUPPORTS_IE, supportsIE);
        getJsonResponse().addKeyValues(JsonCodec.WebDriver.JSON.SUPPORTS_CHROME, supportsChrome);
        getJsonResponse().addKeyValues(JsonCodec.WebDriver.JSON.SUPPORTS_FIREFOX, supportsFirefox);
        getJsonResponse().addKeyValues(JsonCodec.WebDriver.JSON.SUPPORTS_SAFARI, supportsSafari);
        getJsonResponse().addKeyValues(JsonCodec.WebDriver.JSON.TESTS_RAN, NodeInformation.GetSessionsEnded());
        getJsonResponse().addKeyValues(JsonCodec.WebDriver.JSON.TEST_LIMIT, NodeInformation.GetSessionsLimit());
        getJsonResponse().addKeyValues(JsonCodec.WebDriver.JSON.DUE_REBOOT, NodeInformation.DueReboot());
        getJsonResponse().addKeyValues(JsonCodec.WebDriver.JSON.TEST_DURATION_SECONDS, NodeInformation.getDuration());
        getJsonResponse().addKeyValues(JsonCodec.WebDriver.JSON.IDLE_TIME_SECONDS, NodeInformation.getIdleDuration());
        getJsonResponse().addKeyValues(JsonCodec.WebDriver.JSON.BROWSER_ACTIVE, NodeInformation.hasActiveBrowser());
        getJsonResponse().addKeyValues(JsonCodec.WebDriver.JSON.STATUS, NodeInformation.getStatus());
        getJsonResponse().addKeyValues(JsonCodec.WebDriver.JSON.HUB_IP, NodeInformation.getHubHost());

        return getJsonResponse().getJson();
    }
}
