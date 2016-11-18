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
import com.groupon.seleniumgridextras.ExecuteCommand;
import com.groupon.seleniumgridextras.browser.BrowserVersionDetector;
import com.groupon.seleniumgridextras.config.RuntimeConfig;
import org.apache.log4j.Logger;

public class GetBrowserVersions extends ExecuteOSTask {

    private static Logger logger = Logger.getLogger(GetBrowserVersions.class);

    private final boolean supportsIE;
    private final boolean supportsChrome;
    private final boolean supportsFirefox;
    private final boolean supportsSafari;

    public GetBrowserVersions() {
        setEndpoint("/browser_versions");
        setDescription("Gets the current versions of browsers");
        JsonObject params = new JsonObject();
        setAcceptedParams(params);
        setRequestType("GET");
        setResponseType("json");
        setClassname(this.getClass().getCanonicalName().toString());
        setCssClass("btn-success");
        setButtonText("Browser Versions");
        setEnabledInGui(true);

        String config = RuntimeConfig.getConfig().toJson();

        supportsIE = config.contains("\"browserName\": \"internet explorer\"");
        supportsChrome = config.contains("\"browserName\": \"chrome\"");
        supportsFirefox = config.contains("\"browserName\": \"firefox\"");
        supportsSafari = config.contains("\"browserName\": \"safari\"");

        addResponseDescription("chrome", "Version of Chrome");
        addResponseDescription("firefox", "Version of Firefox");
        addResponseDescription("internet_explorer", "Version of IE");
        addResponseDescription("safari", "Version of Safari");
    }

    @Override
    public boolean initialize() {
        NodeInformation.GetUptime();
        return super.initialize();
    }

    @Override
    public JsonObject execute() {

        getJsonResponse().addKeyValues("chrome", getChromeMainVersion());
        getJsonResponse().addKeyValues("firefox", getFirefoxMainVersion());
        getJsonResponse().addKeyValues("internet_explorer", getIeMainVersion());
        getJsonResponse().addKeyValues("safari", getSafariMainVersion());

        return getJsonResponse().getJson();
    }

    private String parseCmdResult(String input) {
        input = input.replace("\"", "").replace("[", "").replace("]", "");
        input = input.replace("\\r", "");
        input = input.replaceAll("\\s+", " ");
        input = input.replace(" ,", ",");
        return input.trim();
    }

    private static String extractVersionFromString(String input) {
        boolean capturing = false;
        String result = "";
        for (char c : input.toCharArray()) {
            if (Character.isDigit(c)) {
                capturing = true;
                result += c;
            } else {
                if (capturing) {
                    if (c == '.') {
                        result += '.';
                    } else {
                        return result;
                    }
                }
            }
        }
        return result;
    }

    private static String getMainVersionFromString(String input) {
        return extractVersionFromString(input).split("\\.")[0];
    }

    public String getChromeMainVersion() {
        if (supportsChrome) {
            return BrowserVersionDetector.guessBrowserVersion("chrome");
//            if (RuntimeConfig.getOS().isWindows()) {
//                return getMainVersionFromString(parseCmdResult(ExecuteCommand.execRuntime("type \"C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.VisualElementsManifest.xml\" | find \" Square150x150Logo\"", waitToFinishTask).get("out").toString().replace("Square150x150Logo", "")));
//            } else {
//                return getMainVersionFromString(parseCmdResult(ExecuteCommand.execRuntime("google-chrome --product-version", waitToFinishTask).get("out").toString()));
//            }
        }
        return "-";
    }

    public String getFirefoxMainVersion() {
        if (supportsFirefox) {
            return BrowserVersionDetector.guessBrowserVersion("firefox");
//            if (RuntimeConfig.getOS().isWindows()) {
//                return getMainVersionFromString(parseCmdResult(ExecuteCommand.execRuntime("\"C:\\Program Files (x86)\\Mozilla Firefox\\firefox\" -v | more", waitToFinishTask).get("out").toString()));
//            } else {
//                return getMainVersionFromString(parseCmdResult(ExecuteCommand.execRuntime("firefox -v", waitToFinishTask).get("out").toString()));
//            }
        }
        return "-";
    }

    public String getIeMainVersion() {
        if (supportsIE) {
            return BrowserVersionDetector.guessBrowserVersion("internet explorer");
//            if (RuntimeConfig.getOS().isWindows()) {
//                return getMainVersionFromString(parseCmdResult(ExecuteCommand.execRuntime("reg query \"HKEY_LOCAL_MACHINE\\Software\\Microsoft\\Internet Explorer\" /v svcversion | find \"svcversion\"", waitToFinishTask).get("out").toString()));
//            }
        }
        return "-";
    }

    public String getSafariMainVersion() {
        return "-";
    }
}
