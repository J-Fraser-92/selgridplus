//package com.groupon.seleniumgridextras.tasks;
//
//import com.google.gson.JsonObject;
//import com.groupon.seleniumgridextras.config.ConfigModifier;
//import com.groupon.seleniumgridextras.config.NodeInformation;
//
//import com.groupon.seleniumgridextras.config.RuntimeConfig;
//import com.groupon.seleniumgridextras.tasks.config.TaskDescriptions;
//import com.groupon.seleniumgridextras.utilities.HttpUtility;
//import com.groupon.seleniumgridextras.utilities.ThisHubInfo;
//import com.groupon.seleniumgridextras.utilities.json.JsonCodec;
//import com.groupon.seleniumgridextras.utilities.json.JsonParserWrapper;
//
//import org.apache.log4j.Logger;
//
//import java.io.File;
//import java.io.IOException;
//import java.net.ConnectException;
//import java.net.URI;
//import java.net.URISyntaxException;
//import java.util.List;
//import java.util.Map;
//import org.apache.commons.io.FileUtils;
//
//public class SetHubAll extends ExecuteOSTask {
//
//    private static Logger logger = Logger.getLogger(SetHubAll.class);
//
//    public SetHubAll() {
//        setEndpoint(TaskDescriptions.Endpoints.SET_HUB_ALL);
//        setDescription(TaskDescriptions.Description.SET_HUB_ALL);
//        JsonObject params = new JsonObject();
//        params.addProperty(JsonCodec.WebDriver.Downloader.HUB, "IP address of the hub to be used");
//        params.addProperty(JsonCodec.WebDriver.Downloader.REBOOT, "Set false if no reboot wanted yet");
//        setAcceptedParams(params);
//        setRequestType(TaskDescriptions.HTTP.GET);
//        setResponseType(TaskDescriptions.HTTP.JSON);
//        setClassname(this.getClass().getCanonicalName());
//        setCssClass(TaskDescriptions.UI.BTN_SUCCESS);
//        setButtonText(TaskDescriptions.UI.ButtonText.ROLLBACK_CHROMEDRIVER);
//        setEnabledInGui(true);
//
//        addResponseDescription(JsonCodec.WebDriver.JSON.CONFIGS_UPDATED, "Number of machines now set to use this hub IP");
//
//        logger.debug(RuntimeConfig.getConfig());
//
//    }
//
//    public JsonObject execute(String ip, Boolean reboot) {
//
//        logger.info("Call to SetHubAll - IP: " + ip + " - Reboot: " + reboot);
//
//        List<String> machines = ThisHubInfo.getAllConnectedNodeIPs();
//        int counter = 0;
//        if (machines.size() > 0) {
//
//            for (String machine : machines) {
//                    counter++;
//                    File json = new File(machine, "node_5555.json");
//                    ConfigModifier reader = new ConfigModifier(json.getAbsolutePath());
//                    Map map = reader.toHashMap();
//                    Map config = ((Map) map.get("configuration"));
//                    config.put("hubHost", ip);
//                    map.put("configuration", config);
//                    try {
//                        FileUtils.writeStringToFile(json, JsonParserWrapper.prettyPrintString(map));
//
//                        String reqUri = "http://" + machine + ":3000" + TaskDescriptions.Endpoints.SET_HUB + "?"
//                                + JsonCodec.WebDriver.Downloader.HUB + "=" + ip
//                                + "&" + JsonCodec.WebDriver.Downloader.REBOOT + "=false";
//
//                        HttpUtility.getRequest(new URI(reqUri));
//                        if (reboot && (ip != NodeInformation.GetIpAddress())) {
//                            HttpUtility.getRequest(new URI("http://" + machine + ":3000" + TaskDescriptions.Endpoints.RESET));
//                        }
//                    } catch (ConnectException ex) {
//                        //Do nothing, node is just down - no need to restart anyway
//                    } catch (IOException ex) {
//                        Logger.getLogger(SetHubAll.class.getName()).error(ex);
//                    } catch (URISyntaxException ex) {
//                        Logger.getLogger(SetHubAll.class.getName()).error(ex);
//                    }
//
//            }
//        }
//
//        getJsonResponse().addKeyValues(JsonCodec.WebDriver.JSON.CONFIGS_UPDATED, counter);
//
//        return getJsonResponse().getJson();
//    }
//
//    @Override
//    public JsonObject execute(Map<String, String> parameter) {
//        if (!parameter.isEmpty() && parameter.containsKey(JsonCodec.WebDriver.Downloader.HUB)) {
//            Boolean reboot = true;
//            if (parameter.containsKey(JsonCodec.WebDriver.Downloader.REBOOT)) {
//                reboot = !parameter.get(JsonCodec.WebDriver.Downloader.REBOOT).toLowerCase().equals("false");
//            }
//            return execute(parameter.get(JsonCodec.WebDriver.Downloader.HUB), reboot);
//
//        } else {
//            return execute();
//        }
//    }
//
//    @Override
//    public JsonObject execute() {
//
//        return execute(NodeInformation.GetIpAddress(), true);
//
//    }
//
//}
