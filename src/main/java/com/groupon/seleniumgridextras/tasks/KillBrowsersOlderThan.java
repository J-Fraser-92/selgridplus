///**
// * Copyright (c) 2013, Groupon, Inc. All rights reserved.
// *
// * Redistribution and use in source and binary forms, with or without
// * modification, are permitted provided that the following conditions are met:
// *
// * Redistributions of source code must retain the above copyright notice, this
// * list of conditions and the following disclaimer.
// *
// * Redistributions in binary form must reproduce the above copyright notice,
// * this list of conditions and the following disclaimer in the documentation
// * and/or other materials provided with the distribution.
// *
// * Neither the name of GROUPON nor the names of its contributors may be used to
// * endorse or promote products derived from this software without specific prior
// * written permission.
// *
// * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
// * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
// * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
// * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
// * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
// * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
// * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
// * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
// * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
// * POSSIBILITY OF SUCH DAMAGE.
// *
// * Created with IntelliJ IDEA. User: Dima Kovalenko (@dimacus) && Darko Marinov
// * Date: 5/10/13 Time: 4:06 PM
// */
//package com.groupon.seleniumgridextras.tasks;
//
//import com.google.gson.JsonObject;
//
//import com.groupon.seleniumgridextras.ExecuteCommand;
//import com.groupon.seleniumgridextras.config.RuntimeConfig;
//import com.groupon.seleniumgridextras.tasks.config.TaskDescriptions;
//import com.groupon.seleniumgridextras.utilities.json.JsonCodec;
//import java.text.SimpleDateFormat;
//import java.util.Date;
//
//import org.apache.log4j.Logger;
//
//public class KillBrowsersOlderThan extends ExecuteOSTask {
//
//    private static Logger logger = Logger.getLogger(KillBrowsersOlderThan.class);
//
//    public KillBrowsersOlderThan() {
//        setEndpoint("/kill_browsers_older_than");
//        setDescription("Used to kill browsers older than a set period");
//        JsonObject params = new JsonObject();
//        params.addProperty("seconds", "(int) the cutoff for the creation time of a browser");
//        setAcceptedParams(params);
//        setRequestType("GET");
//        setResponseType("json");
//        setClassname(this.getClass().getCanonicalName().toString());
//        setCssClass(TaskDescriptions.UI.BTN_DANGER);
//        setButtonText(TaskDescriptions.UI.ButtonText.KILL_ALL_BY_NAME);
//        setEnabledInGui(true);
//
//    }
//
//    @Override
//    public JsonObject execute() {
//        return execute("60");
//    }
//
//    @Override
//    public JsonObject execute(String parameter) {
//        if (RuntimeConfig.getOS().isWindows()) {
//            String command = "wmic process get caption,creationdate,handle | findstr /C:\"firefox.exe\" /C:\"chrome.exe\" /C:\"iexplore.exe\" /C:\"chromedriver\" /C:\"iedriver\" /C:\"inetinfo\" ";
//
//            JsonObject response = ExecuteCommand.execRuntime(command, waitToFinishTask);
//
//            int cutoff;
//            try {
//                cutoff = Integer.parseInt(parameter);
//            } catch (Exception e) {
//                cutoff = 60;
//                response.addProperty(JsonCodec.WARNING, "Could not parse '" + parameter + "' as integer, defaulting to 60");
//            }
//
//            response.addProperty(JsonCodec.OS.KillCommands.COMMAND, command);
//            response.addProperty(JsonCodec.OS.KillCommands.WAIT_TO_FINISH, waitToFinishTask);
//
//            //Hacky hacky hacky
//            String results = response.get(JsonCodec.OUT).toString();
//
//            results = results.replace("\"", "").replace("[", "").replace("]", "");
//            results = results.replace("\\r", "");
//            results = results.replaceAll("\\s+", " ");
//            results = results.replace(" ,", ",");
//
//            String[] splits = results.split(",");
//
//            for (String s : splits) {
//
//                String[] info = s.split(" ");
//
//                if(info.length >= 3) {
//                String application = info[0];
//                String creation = info[1];
//                String pid = info[2];
//
//                String time = creation.substring(0, 14);
//                try {
//                    Date credate = new SimpleDateFormat("yyyyMMddHHmmss").parse(time);
//                    Date now = new Date();
//                    long seconds = (now.getTime() - credate.getTime()) / 1000;
//
//                    if (seconds > cutoff) {
//                        logger.info("Killing " + application + " (idle for " + seconds + " seconds)");
//                        new KillPid().execute(pid);//
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                } else {
//                    logger.warn("Value returned doesn't seem to be correct format: " + results);
//                }
//            }
//            return response;
//        }
//        return new JsonObject();
//
//    }
//
//}
