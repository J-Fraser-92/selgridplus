/**
 * Copyright (c) 2013, Groupon, Inc. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * Neither the name of GROUPON nor the names of its contributors may be used to
 * endorse or promote products derived from this software without specific prior
 * written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * Created with IntelliJ IDEA. User: Dima Kovalenko (@dimacus) && Darko Marinov
 * Date: 5/10/13 Time: 4:06 PM
 */
package com.groupon.seleniumgridextras.tasks;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.groupon.seleniumgridextras.config.ConfigModifier;

import com.groupon.seleniumgridextras.config.RuntimeConfig;
import com.groupon.seleniumgridextras.tasks.config.TaskDescriptions;
import com.groupon.seleniumgridextras.utilities.HttpUtility;
import com.groupon.seleniumgridextras.utilities.ThisHubInfo;
import com.groupon.seleniumgridextras.utilities.json.JsonCodec;
import com.groupon.seleniumgridextras.utilities.json.JsonParserWrapper;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Map;
import org.apache.commons.io.FileUtils;

public class RollbackIEDriver extends ExecuteOSTask {

    private String bit = JsonCodec.WebDriver.Downloader.BIT_32;
    private static Logger logger = Logger.getLogger(RollbackIEDriver.class);

    public RollbackIEDriver() {
        setEndpoint(TaskDescriptions.Endpoints.ROLLBACK_IEDRIVER);
        setDescription(TaskDescriptions.Description.ROLLBACK_IEDRIVER);
        JsonObject params = new JsonObject();
        params.addProperty(JsonCodec.WebDriver.Downloader.VERSION, "Version of IEDriver to download, such as 2.33.0");
        params.addProperty(JsonCodec.WebDriver.Downloader.REBOOT, "Set false if no reboot wanted yet");
        setAcceptedParams(params);
        setRequestType(TaskDescriptions.HTTP.GET);
        setResponseType(TaskDescriptions.HTTP.JSON);
        setClassname(this.getClass().getCanonicalName().toString());
        setCssClass(TaskDescriptions.UI.BTN_SUCCESS);
        setButtonText(TaskDescriptions.UI.ButtonText.ROLLBACK_IEDRIVER);
        setEnabledInGui(true);

        addResponseDescription(JsonCodec.WebDriver.JSON.CONFIGS_UPDATED, "Number of configs now set to this version");
        addResponseDescription(JsonCodec.WebDriver.JSON.CONFIGS_NOT_UPDATED, "Number of configs that encountered exceptions");
        addResponseDescription(JsonCodec.WebDriver.JSON.CONFIGS_NOT_UPDATED_LIST, "Names of configs that encountered exceptions");

        logger.debug(RuntimeConfig.getConfig());

    }

    public JsonObject execute(String version, Boolean reboot) {

        logger.info("Call to RollbackIEDriver - Version: " + version + " - Reboot: " + reboot);

        File configs = RuntimeConfig.getConfig().getConfigsDirectory();
        File[] files = configs.listFiles();

        int counter = 0;
        int errors = 0;
        JsonArray not_updated = new JsonArray();
        for (File configFile : files) {
            counter++;

            File json = new File(configFile.getAbsolutePath(), "selenium_grid_extras_config.json");

            ConfigModifier reader = new ConfigModifier(json.getAbsolutePath());

            Map map = reader.toHashMap();
            Map configMap = ((Map) map.get("theConfigMap"));
            Map ie = ((Map) configMap.get("iedriver"));

            ie.put("version", version);
            configMap.put("iedriver", ie);

            map.put("theConfigMap", configMap);

            try {
                FileUtils.writeStringToFile(json, JsonParserWrapper.prettyPrintString(map));
                //Do nothing, node is just down - no need to restart anyway
            } catch (IOException ex) {
                Logger.getLogger(RollbackIEDriver.class.getName()).error(ex);
                errors++;
                not_updated.add(new JsonParser().parse(configFile.getName()));
            }
        }

        try {
            if (reboot) {
                for(String ip : ThisHubInfo.getAllConnectedNodeIPs()) {
                    HttpUtility.getRequest(new URI("http://" + ip + ":3000" + TaskDescriptions.Endpoints.RESET));
                }
            }
        }
        catch (Exception ex) {
            Logger.getLogger(RollbackChromeDriver.class.getName()).error(ex);
            errors++;
        }

        getJsonResponse().addKeyValues(JsonCodec.WebDriver.JSON.CONFIGS_UPDATED, counter);
        getJsonResponse().addKeyValues(JsonCodec.WebDriver.JSON.CONFIGS_NOT_UPDATED, errors);
        getJsonResponse().addKeyValues(JsonCodec.WebDriver.JSON.CONFIGS_NOT_UPDATED_LIST, not_updated);
        return getJsonResponse().getJson();
    }

    @Override
    public JsonObject execute(Map<String, String> parameter) {
        if (!parameter.isEmpty() && parameter.containsKey(JsonCodec.WebDriver.Downloader.VERSION)) {
            Boolean reboot = true;
            if (parameter.containsKey(JsonCodec.WebDriver.Downloader.REBOOT)) {
                reboot = !parameter.get(JsonCodec.WebDriver.Downloader.REBOOT).toLowerCase().equals("false");
            }
            return execute(parameter.get(JsonCodec.WebDriver.Downloader.VERSION), reboot);

        } else {
            return execute();
        }
    }
}
