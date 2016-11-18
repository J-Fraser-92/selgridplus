package com.groupon.seleniumgridextras.grid.servlets;

import com.google.gson.JsonObject;
import com.groupon.seleniumgridextras.grid.GridStarter;
import com.groupon.seleniumgridextras.support.Utils;
import com.groupon.seleniumgridextras.tasks.config.TaskDescriptions;
import com.groupon.seleniumgridextras.utilities.HttpUtility;
import com.groupon.seleniumgridextras.utilities.json.JsonCodec;
import com.groupon.seleniumgridextras.utilities.json.JsonParserWrapper;
import java.io.BufferedWriter;
import java.net.URL;
import java.util.concurrent.Callable;
import org.jboss.netty.handler.timeout.ReadTimeoutException;
import org.openqa.grid.internal.RemoteProxy;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import org.json.JSONException;

/**
 *
 * @author JamesFraser
 */
public class ActiveNodeServletThread implements Callable {

    private final RemoteProxy prox;

    public ActiveNodeServletThread(RemoteProxy prox) {
        this.prox = prox;
    }

    public Object call() throws Exception {
        JsonObject config = prox.getOriginalRegistrationRequest().getAssociatedJSON().getAsJsonObject("configuration");
        JsonObject nodeInfo = new JsonObject();
        if (config != null) {

            String host = config.get("host").getAsString();

            nodeInfo.addProperty("host", host);
            nodeInfo.add("hostname", config.get("friendlyHostName"));

            try {
                String response = HttpUtility.getRequestAsString(new URL("http://" + host + ":3000" + TaskDescriptions.Endpoints.DASHBOARD_NODE_STATUS), 5000);

                JsonObject responseObj = JsonParserWrapper.toJsonObject(response);

                String status = Utils.parseJsonToPlaintext(responseObj.get(JsonCodec.WebDriver.JSON.STATUS).getAsString());
                String idle_time = Utils.parseJsonToPlaintext(responseObj.get(JsonCodec.WebDriver.JSON.IDLE_TIME_SECONDS).getAsString());
                String busy_time = Utils.parseJsonToPlaintext(responseObj.get(JsonCodec.WebDriver.JSON.TEST_DURATION_SECONDS).getAsString());
                String browser_active = Utils.parseJsonToPlaintext(responseObj.get(JsonCodec.WebDriver.JSON.BROWSER_ACTIVE).getAsString());

                nodeInfo.addProperty("status", status);
                nodeInfo.addProperty("idle_time", idle_time);
                nodeInfo.addProperty("busy_time", busy_time);
                nodeInfo.addProperty("browser_active", browser_active);

            } catch (ReadTimeoutException ex) {
                nodeInfo.addProperty("status", "unreachable");
                nodeInfo.addProperty("idle_time", "unreachable");
                nodeInfo.addProperty("busy_time", "unreachable");
                nodeInfo.addProperty("browser_active", "unreachable");
            } catch (IOException ex) {
                nodeInfo.addProperty("status", "unreachable");
                nodeInfo.addProperty("idle_time", "unreachable");
                nodeInfo.addProperty("busy_time", "unreachable");
                nodeInfo.addProperty("browser_active", "unreachable");
            }

        } else {
            nodeInfo.addProperty("host", "null");
            nodeInfo.addProperty("hostname", "null");
            nodeInfo.addProperty("status", "null");
        }

        return nodeInfo;
    }

    private void logHanger(String node, Map<String, Object> caps) {

        String date = new SimpleDateFormat("yyyy_MM_dd").format(new Date());

        File logs = new File(GridStarter.getLogFullPath());
        File hangingLog = new File(logs, "hanging_" + date + ".log");

        if (!hangingLog.exists()) {
            try {
                hangingLog.createNewFile();
            } catch (Exception e) {

            }
        }

        try {
            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(hangingLog, true)));
            out.println(new SimpleDateFormat("HH:mm:ss").format(new Date()));
            out.println(node);
            for (String key : caps.keySet()) {
                out.println("\t" + key + " : " + caps.get(key));
            }
            out.println();
            out.close();
        } catch (Exception e) {
            //exception handling left as an exercise for the reader
        }
    }
}
