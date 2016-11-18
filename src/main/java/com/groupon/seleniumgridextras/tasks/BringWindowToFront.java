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
//
//import java.util.Map;
//import org.apache.log4j.Logger;
//
//public class BringWindowToFront extends ExecuteOSTask {
//
//    private static Logger logger = Logger.getLogger(BringWindowToFront.class);
//
//    public BringWindowToFront() {
//        setEndpoint("/bring_front");
//        setDescription("Brings a window based on its name or PID to the front");
//        JsonObject params = new JsonObject();
//        params.addProperty("pid", "Name of process");
//        params.addProperty("name", "Name of process");
//        setAcceptedParams(params);
//        setRequestType("GET");
//        setResponseType("json");
//        setClassname(this.getClass().getCanonicalName().toString());
//        setCssClass(TaskDescriptions.UI.BTN_DANGER);
//        setButtonText("/bring_front");
//        setEnabledInGui(true);
//
//    }
//
//    @Override
//    public JsonObject execute() {
//        getJsonResponse().addKeyValues(JsonCodec.ERROR, "Requires only one either 'pid' or 'name' param");
//        return getJsonResponse().getJson();
//    }
//
//    @Override
//    public JsonObject execute(Map<String, String> parameter) {
//
//        String pid = null;
//        String name = null;
//
//        if (parameter.containsKey("name")) {
//            name = parameter.get("name");
//        }
//
//        if (parameter.containsKey("pid")) {
//            pid = parameter.get("pid");
//        }
//
//        if ((pid == null) ^ (name == null)) {
//            if (RuntimeConfig.getOS().isWindows()) {
//                String param;
//            if (pid != null) {
//                param = "/" + pid;
//            } else if (name != null) {
//                param = "\"" + name + "\"";
//            } else {
//                return execute();
//            }
//
//            ExecuteCommand.execRuntime("nircmd.exe win settopmost process " + param + " 1", waitToFinishTask);
//            ExecuteCommand.execRuntime("nircmd.exe win settopmost process " + param + " 0", waitToFinishTask);
//            getJsonResponse().addKeyValues(JsonCodec.OUT, "Brought process " + param + " to front");
//            
//            } 
//            else {
//                //if not Windows, if not Mac then Linux
//                String param = "";
//                if (pid != null) {
//                    param = pid;
//                    // get name of the process
//                    ExecuteCommand.execRuntime("wmctrl -a $(ps -p " + pid + " -o comm=)", waitToFinishTask);
//                } else if (name != null) {
//                    param = name;
//                    ExecuteCommand.execRuntime("wmctrl -a " + name, waitToFinishTask);
//                }
//                getJsonResponse().addKeyValues(JsonCodec.OUT, "Brought process " + param + " to front");
//            }
//
//
//            return getJsonResponse().getJson();
//        } else {
//            return execute();
//        }
//    }
//
//}
