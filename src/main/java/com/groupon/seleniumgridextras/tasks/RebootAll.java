package com.groupon.seleniumgridextras.tasks;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import com.groupon.seleniumgridextras.config.RuntimeConfig;
import com.groupon.seleniumgridextras.tasks.config.TaskDescriptions;
import com.groupon.seleniumgridextras.utilities.HttpUtility;
import com.groupon.seleniumgridextras.utilities.ThisHubInfo;
import com.groupon.seleniumgridextras.utilities.json.JsonCodec;

import org.apache.log4j.Logger;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

public class RebootAll extends ExecuteOSTask {

    private static Logger logger = Logger.getLogger(RebootAll.class);

    public RebootAll() {
        setEndpoint(TaskDescriptions.Endpoints.REBOOT_ALL);
        setDescription(TaskDescriptions.Description.REBOOT_ALL);
        JsonObject params = new JsonObject();
        params.addProperty(JsonCodec.WebDriver.Grid.FORCE_REBOOT, "Should the hub force shutdown of all nodes - default: False");
        params.addProperty(JsonCodec.WebDriver.Grid.INCLUDE_HUB, "Should the hub reboot as well - default: False");
        setAcceptedParams(params);
        setRequestType(TaskDescriptions.HTTP.GET);
        setResponseType(TaskDescriptions.HTTP.JSON);
        setClassname(this.getClass().getCanonicalName().toString());
        setCssClass(TaskDescriptions.UI.BTN_SUCCESS);
        setButtonText(TaskDescriptions.UI.ButtonText.ROLLBACK_WEBDRIVER);
        setEnabledInGui(true);

        addResponseDescription(JsonCodec.WebDriver.JSON.MACHINES_UPDATED, "Number of machines set to reboot");
        addResponseDescription(JsonCodec.WebDriver.JSON.MACHINES_NOT_UPDATED, "Number of machines that encountered exceptions");
        addResponseDescription(JsonCodec.WebDriver.JSON.MACHINES_NOT_UPDATED_LIST, "Names of machines that encountered exceptions");

        logger.debug(RuntimeConfig.getConfig());

    }

    public JsonObject execute(Boolean force_reboot, Boolean include_hub) {

        logger.info("Reboot all nodes - Force: " + force_reboot + " - Include Hub: " + include_hub);

        List<String> nodes = ThisHubInfo.getAllConnectedNodeIPs();

        int counter = 0;
        int errors = 0;
        JsonArray not_updated = new JsonArray();

        for (String node : nodes) {
            try {
                if (force_reboot) {
                    HttpUtility.getRequest(new URI("http://" + node + ":3000" + TaskDescriptions.Endpoints.REBOOT));
                } else {
                    HttpUtility.getRequest(new URI("http://" + node + ":3000" + TaskDescriptions.Endpoints.SAFE_REBOOT));
                }
            } catch (URISyntaxException ex) {
                Logger.getLogger(RebootAll.class.getName()).error(ex);
                errors++;
                not_updated.add(new JsonParser().parse(node));
            } catch (IOException ex) {
                Logger.getLogger(RebootAll.class.getName()).error(ex);
                errors++;
                not_updated.add(new JsonParser().parse(node));
            }
            counter++;
        }
        getJsonResponse().addKeyValues(JsonCodec.WebDriver.JSON.MACHINES_UPDATED, counter);
        getJsonResponse().addKeyValues(JsonCodec.WebDriver.JSON.MACHINES_NOT_UPDATED, errors);
        getJsonResponse().addKeyValues(JsonCodec.WebDriver.JSON.MACHINES_NOT_UPDATED_LIST, not_updated);

        if (include_hub) {
            counter++;
            if (force_reboot) {
                new SafeReboot().execute();
            } else {
                new RebootNode().execute();
            }
        }
        return getJsonResponse().getJson();
    }

    @Override
    public JsonObject execute(Map<String, String> parameter) {
        Boolean force_reboot = false;
        Boolean include_hub = false;

        if (parameter.containsKey(JsonCodec.WebDriver.Grid.FORCE_REBOOT)) {
            force_reboot = parameter.get(JsonCodec.WebDriver.Grid.FORCE_REBOOT).toLowerCase().equals("true");
        }
        if (parameter.containsKey(JsonCodec.WebDriver.Grid.INCLUDE_HUB)) {
            include_hub = parameter.get(JsonCodec.WebDriver.Grid.INCLUDE_HUB).toLowerCase().equals("true");
        }

        return execute(force_reboot, include_hub);
    }
}
