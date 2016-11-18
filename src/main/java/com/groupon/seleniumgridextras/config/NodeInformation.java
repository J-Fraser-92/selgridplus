package com.groupon.seleniumgridextras.config;

import com.google.gson.internal.LinkedTreeMap;
import com.groupon.seleniumgridextras.support.Utils;
import com.groupon.seleniumgridextras.tasks.DashboardNodeStatus;
import com.groupon.seleniumgridextras.tasks.GetProcesses;
import com.groupon.seleniumgridextras.tasks.StopGrid;
import com.groupon.seleniumgridextras.tasks.config.TaskDescriptions;
import com.groupon.seleniumgridextras.utilities.HttpUtility;
import com.groupon.seleniumgridextras.utilities.StreamUtility;
import com.groupon.seleniumgridextras.utilities.json.JsonCodec;
import com.groupon.seleniumgridextras.utilities.json.JsonParserWrapper;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import org.apache.log4j.Logger;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author JamesFraser
 */
public class NodeInformation {

    private static String ipAddress;
    private static String hostName = DetermineHostName();
    private static final Date originTime = new Date();
    public static boolean available = true;
    public static boolean busy = false;
    public static boolean reboot = false;
    public static boolean unregister = false;
    //public static int jobsStarted = 0;
    public static int jobsEnded = 0;
    public static Date jobStartTime;
    public static Date jobEndTime = new Date();
    public static String hubHost = "";
    public static long lastReset = 0;

    public static String GetIpAddress() {
        if (ipAddress != null) {
            return ipAddress;
        }
        
        String ip;
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                // filters out 127.0.0.1 and inactive interfaces
                if (iface.isLoopback() || !iface.isUp())
                    continue;

                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while(addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    ip = addr.getHostAddress();
                    if (ip.startsWith("192.168")) {
                        ipAddress = ip;
                        return ip;
                    }
                }
            }
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
        
        return RuntimeConfig.getHostIp();        
    }

    private static String DetermineHostName() {        
        String name = System.getenv("COMPUTERNAME");
        if (name == null) {
            name = "UnnamedNode";
        }
        return name;
    }

    public static String GetHostName() {
        return hostName;
    }

    public static String GetUptime() {
        long difference = new Date().getTime() - originTime.getTime();

        long seconds = (difference / 1000) % 60;
        long minutes = (difference / (1000 * 60)) % 60;
        long hours = (difference / (1000 * 60 * 60));

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public static void SetAvailable(boolean available) {
        NodeInformation.available = available;
    }

    public static boolean IsAvailable() {
        return available;
    }

    public static boolean IsBusy() {
        return busy;
    }

    public static int GetSessionsEnded() {
        return jobsEnded;
    }

    public static int GetSessionsLimit() {
        return RuntimeConfig.getConfig().getRebootAfterSessions();
    }

    public static void SafeReboot() {
        reboot = true;
    }

    public static void SafeUnregister() { unregister = true; }

    public static boolean GetReboot() {
        return reboot;
    }

    public static boolean HitLimit() {
        return ((GetSessionsLimit() == GetSessionsEnded()) && (GetSessionsEnded() > 0));
    }

    public static boolean DueReboot() {

        if (HitLimit()) {
            return true;
        }
        return reboot;
    }

    public static boolean DueUnregister() {
        return unregister;
    }

    public static void StartJob() {
        busy = true;
        jobStartTime = new Date();
    }

    public static void FinishJob() {
        jobsEnded++;
        busy = false;
        jobEndTime = new Date();      
        
        if(unregister) {
            new StopGrid().execute("5555");
        }
    }

    public static long GetJobDuration() {
        if (!busy) {
            return 0;
        }
        Date current = new Date();
        return current.getTime() - jobStartTime.getTime();
    }

    public static long GetIdleTime() {
        if (busy) {
            return 0;
        }
        Date current = new Date();
        return current.getTime() - jobEndTime.getTime();
    }
    
    public static String getHubHost() {

        if (hubHost.equals("")) {

            File json = new File(RuntimeConfig.getSeleniungGridExtrasHomePath(), "node_5555.json");
            ConfigFileReader configReader = new ConfigFileReader(json.getAbsolutePath());
            Map map = configReader.toHashMap();
            try{
                LinkedTreeMap config = (LinkedTreeMap) (map.get("configuration"));
                hubHost = config.get("hubHost").toString();
            }
            catch (Exception ex)
            {                
            }

        }
        return hubHost;
    }

    public static boolean hasActiveBrowser() {
        try {
            String command;
            if (RuntimeConfig.getOS().isWindows()) {
                command = new GetProcesses().getWindowsCommand();
            } else if (RuntimeConfig.getOS().isMac()) {
                command = new GetProcesses().getMacCommand();
            } else {
                command = new GetProcesses().getLinuxCommand();
            }
            Process process = Runtime.getRuntime().exec(command);
            String resp = StreamUtility.inputStreamToString(process.getInputStream());

            boolean browser = (resp.contains("chrome.exe") || resp.contains("firefox.exe") || resp.contains("iexplore.exe") || resp.contains("Safari.exe"));
            return browser;

        } catch (IOException ex) {
            Logger.getLogger(NodeInformation.class
                    .getName()).error(ex);
            return true;
        }
    }

    public static void notifyReset() {
        lastReset = System.currentTimeMillis();

        Logger.getLogger(DashboardNodeStatus.class.getName()).info("Reset dump - busy: " + busy + " | browser active: " + hasActiveBrowser() + " | status: " + getStatus());

        if (busy) {
            FinishJob();
        }
    }

    public static boolean canReset() {
        long now = System.currentTimeMillis();
        return ((now - lastReset) >= (15 * 1000));
    }

    public static int getDuration() {
        return ((int) TimeUnit.MILLISECONDS.toSeconds(GetJobDuration()));
    }

    public static int getIdleDuration() {
        return ((int) TimeUnit.MILLISECONDS.toSeconds(GetIdleTime()));
    }
    
    public static String getStatus() {

        int duration = getDuration();

        boolean activeBrowser = NodeInformation.hasActiveBrowser();

        if (duration >= 60) {
            if (activeBrowser) {
                return "busy";
            } else {
                FinishJob();
                return "hanging";
            }
        } else if (duration == 0) {
            return "idle";
        } else {
            return "busy";
        }
    }
}
