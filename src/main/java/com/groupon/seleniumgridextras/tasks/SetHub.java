package com.groupon.seleniumgridextras.tasks;

import com.google.gson.JsonObject;

import com.groupon.seleniumgridextras.config.RuntimeConfig;
import com.groupon.seleniumgridextras.tasks.config.TaskDescriptions;
import com.groupon.seleniumgridextras.utilities.HttpUtility;
import com.groupon.seleniumgridextras.utilities.json.JsonCodec;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Map;
import org.apache.commons.io.FileUtils;

public class SetHub extends ExecuteOSTask {

    private static Logger logger = Logger.getLogger(SetHub.class);

    public SetHub() {
        setEndpoint(TaskDescriptions.Endpoints.SET_HUB);
        setDescription(TaskDescriptions.Description.SET_HUB);
        JsonObject params = new JsonObject();
        params.addProperty(JsonCodec.WebDriver.Downloader.HUB, "IP address of the hub to be used");
        params.addProperty(JsonCodec.WebDriver.Downloader.RESET, "Set false if no reset wanted yet");
        setAcceptedParams(params);
        setRequestType(TaskDescriptions.HTTP.GET);
        setResponseType(TaskDescriptions.HTTP.JSON);
        setClassname(this.getClass().getCanonicalName());
        setCssClass(TaskDescriptions.UI.BTN_SUCCESS);
        setButtonText(TaskDescriptions.UI.ButtonText.ROLLBACK_CHROMEDRIVER);
        setEnabledInGui(true);

        addResponseDescription("hub_ip", "The new hub IP");

        logger.debug(RuntimeConfig.getConfig());

    }

    public JsonObject execute(String ip, Boolean reset) {

        logger.info("Call to SetHub - IP: " + ip + " - Reset: " + reset);
        
        File configs = RuntimeConfig.getConfig().getConfigsDirectory();

        File central = new File(configs.getAbsolutePath(), "central_config_repo_config.txt");
        
        try {
            String oldIP = FileUtils.readFileToString(central);
            oldIP = oldIP.replace(":3000/", "");
            HttpUtility.getRequest(new URI(oldIP + ":4444/grid/admin/UnregisterServlet"));
        } catch (Exception ex) {
            Logger.getLogger(SetHub.class.getName()).error(ex);
        }     
        
        try {
            FileUtils.writeStringToFile(central, "http://" + ip + ":3000/");
        } catch (IOException ex) {
            Logger.getLogger(SetHub.class.getName()).error(ex);
        }

        getJsonResponse().addKeyValues("hub_ip", ip);

        if(reset) {
            new Reset().execute();
        }
        
        return getJsonResponse().getJson();
    }

    @Override
    public JsonObject execute(Map<String, String> parameter) {
        if (!parameter.isEmpty() && parameter.containsKey(JsonCodec.WebDriver.Downloader.HUB)) {
            Boolean reset = true;
            if (parameter.containsKey(JsonCodec.WebDriver.Downloader.RESET)) {
                reset = !parameter.get(JsonCodec.WebDriver.Downloader.RESET).toLowerCase().equals("false");
            }
            return execute(parameter.get(JsonCodec.WebDriver.Downloader.HUB), reset);
        } else {
            return execute();
        }
    }
}
