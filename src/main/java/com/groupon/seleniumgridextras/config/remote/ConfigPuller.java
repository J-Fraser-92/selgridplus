package com.groupon.seleniumgridextras.config.remote;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.groupon.seleniumgridextras.config.RuntimeConfig;
import com.groupon.seleniumgridextras.tasks.GetBrowserVersions;
import com.groupon.seleniumgridextras.utilities.DoubleToIntConverter;
import com.groupon.seleniumgridextras.utilities.FileIOUtility;
import com.groupon.seleniumgridextras.utilities.HttpUtility;
import com.groupon.seleniumgridextras.utilities.json.JsonCodec;
import com.groupon.seleniumgridextras.utilities.json.JsonParserWrapper;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

public class ConfigPuller {

    private File configFile;
    private static Logger logger = Logger.getLogger(ConfigPuller.class);

    public ConfigPuller() {
        this.configFile
                = new File(RuntimeConfig.getConfig().getConfigsDirectory().getAbsolutePath() + RuntimeConfig
                        .getOS()
                        .getFileSeparator() + RuntimeConfig.getConfig().getCentralConfigFileName());
        logger
                .info("Config file storing central repository URL is " + this.configFile.getAbsolutePath());
    }

    public void updateFromRemote() {
        String url = getCentralUrl(this.configFile);
        logger.info("Central config URL for current node is set to '" + url + "'");

        if (url.equals("")) {
            logger.info("The central config URL is empty, will not download the latest configs.");
        } else {
            try {
                String message
                        = "Checking central Config repository for " + RuntimeConfig.getOS().getHostName()
                        + " node's config from " + url + " . (Timeout set to " + RuntimeConfig.getConfig()
                        .getConfigPullerHttpTimeout() + "ms)";
                logger.info(message);
                System.out.println(message);
                downloadRemoteConfigs(new URL(url));
            } catch (MalformedURLException error) {
                String message
                        = "The URL of central config  '" + url + "' seems to be malformed in the '" + this.configFile.getAbsolutePath() + "' file. Will skip remote update";
                System.out.println(message);
                logger.warn(message);
                logger.warn(error);
            }
        }

    }

    protected void downloadRemoteConfigs(URL url) {
        boolean retry = true;

        while (retry) {
            try {
                retry = false;
                String rawJson
                        = HttpUtility
                        .getRequestAsString(url, RuntimeConfig.getConfig().getConfigPullerHttpTimeout());
                logger.debug(rawJson);
                Map remoteConfigs = JsonParserWrapper.toHashMap(rawJson);
                logger.debug(remoteConfigs);
                System.out.println(remoteConfigs.keySet());
                if (remoteConfigs.containsKey(JsonCodec.EXIT_CODE)) {
                    Integer exitCode = ((Double) remoteConfigs.get(JsonCodec.EXIT_CODE)).intValue();
                    logger.info("Remote server responded with this exit code to our request " + exitCode);
                    if (exitCode == 0) {
                        saveIndividualFiles(remoteConfigs);
                    } else {
                        logger.info("Remote config request had an error, will skip update from remote source");
                        logger.info(remoteConfigs.get(JsonCodec.ERROR));
                    }
                }

            } catch (IOException error) {
                retry = true;
                String message = "Cannot establish connection to hub, retrying in 15 seconds";
                System.out.println(message);
                logger.info(message);
                try {
                    Thread.sleep(15000);
                } catch (Exception e) {
                }
                

//                    String message
//                            = "Reading config from " + url + " central repository encountered an error: " + error
//                            .getMessage()
//                            + "\nWill use config which already exist on the node";
//                    System.out.println(message);
//                    logger.info(message);
//                    logger.debug(error);
            }
        }

    }

    protected void saveIndividualFiles(Map config) {

        for (String filename : (Set<String>) config.keySet()) {
            if (!filename.equals(JsonCodec.EXIT_CODE) && !filename.equals(JsonCodec.OUT) && !filename
                    .equals(JsonCodec.ERROR)) {
                try {
                    String fileContents = (String) (((ArrayList) config.get(filename)).get(0));

                    Map contents = JsonParserWrapper.toHashMap(fileContents);
                    contents.put(JsonCodec.WARNING, "THIS FILE IS AUTOMATICALLY GENERATED EVERY RESTART FROM " + getCentralUrl(this.configFile) + ". TO PERMANENTLY CHANGE ANY CONFIGURATIONS, PLEASE MODIFY IT ON THE HUB SERVER IN THE " + new File("configs", RuntimeConfig.getOS().getHostName()).getPath());

                    DoubleToIntConverter.convertAllDoublesToInt(contents);

                    if (filename.equals("node_5555.json")) {

                        JsonObject node_config = JsonParserWrapper.toJsonObject(fileContents);

                        if (node_config.has("capabilities")) {
                            JsonElement caps = node_config.get("capabilities");
                            GetBrowserVersions browserVers = new GetBrowserVersions();
                            JsonArray newCaps = new JsonArray();
                            for (JsonElement cap : caps.getAsJsonArray()) {
                                if (cap.getAsJsonObject().has("browserName")) {
                                    String browser = cap.getAsJsonObject().get("browserName").toString().toLowerCase().replace("\"", "");
                                    if (browser.equals("chrome")) {
                                        cap.getAsJsonObject().addProperty("version", browserVers.getChromeMainVersion());
                                    } else if (browser.equals("firefox")) {
                                        cap.getAsJsonObject().addProperty("version", browserVers.getFirefoxMainVersion());
                                    } else if (browser.equals("internet explorer")) {
                                        cap.getAsJsonObject().addProperty("version", browserVers.getIeMainVersion());
                                    } else if (browser.equals("safari")) {
                                        cap.getAsJsonObject().addProperty("version", browserVers.getSafariMainVersion());
                                    }
                                    newCaps.add(cap);
                                }
                            }

                            contents.put("capabilities", newCaps);
                        }
                    }

                    FileIOUtility.writePrettyJsonToFile(new File(filename), contents);

                    String message = "Updated '" + filename + "' from central config repository";
                    System.out.println(message);
                    logger.info(message);
                    logger.debug("Config contents: " + fileContents);

                } catch (IOException error) {
                    logger.warn(
                            "Writing content of '" + filename + "' to HD encountered an error. Content:\n"
                            + config
                            .get(filename));
                    logger.warn(error);
                }
            }
        }

    }

    protected String getCentralUrl(File filename) {
        try {
            return FileIOUtility.getAsString(filename) + "get_node_config?node=" + RuntimeConfig.getOS()
                    .getHostName();
        } catch (FileNotFoundException error) {
            logger.info("Config file for central repo does not exist " + filename.getAbsolutePath());
            return "";
        }
    }

}
