/**
 * Copyright (c) 2013, Groupon, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * Neither the name of GROUPON nor the names of its contributors may be
 * used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 * Created with IntelliJ IDEA.
 * User: Dima Kovalenko (@dimacus) && Darko Marinov
 * Date: 5/10/13
 * Time: 4:06 PM
 */

package com.groupon.seleniumgridextras.tasks;

import com.google.gson.JsonObject;
import com.groupon.seleniumgridextras.ExecuteCommand;
import com.groupon.seleniumgridextras.config.NodeInformation;
import com.groupon.seleniumgridextras.config.RuntimeConfig;
import com.groupon.seleniumgridextras.tasks.config.TaskDescriptions;
import com.groupon.seleniumgridextras.utilities.json.JsonCodec;

import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;

public class Sterilise extends ExecuteOSTask {

    private String command = "";
    private static Logger logger = Logger.getLogger(Sterilise.class);

    public Sterilise() {
        setEndpoint(TaskDescriptions.Endpoints.STERILISE);
        setDescription(TaskDescriptions.Description.STERILISE);
        JsonObject params = new JsonObject();
        params.addProperty(JsonCodec.WebDriver.Grid.FORCE, "Set to true if force sterilisation, false for sterilise only if idle. False by default");
        setAcceptedParams(params);
        setRequestType("GET");
        setResponseType("json");
        setClassname(this.getClass().getCanonicalName().toString());
        setCssClass(TaskDescriptions.UI.BTN_DANGER);
        setButtonText(TaskDescriptions.UI.ButtonText.STERILISE);
        setEnabledInGui(true);

        if (RuntimeConfig.getOS().isWindows()) {
            command = buildWindowsCommand();
        } else {
            command = buildLinuxCommand();
        }
    }

    @Override
    public JsonObject execute(Map<String, String> parameter) {
        if (!parameter.isEmpty() && parameter.containsKey(JsonCodec.WebDriver.Grid.FORCE)) {
            Boolean force = parameter.get(JsonCodec.WebDriver.Grid.FORCE).toLowerCase().equals("true");
            return sterilise(force);
        } else {
            return execute();
        }
    }

    @Override
    public JsonObject execute() {
        return sterilise(false);
    }

    public JsonObject sterilise(boolean force) {
        logger.info("Sterilising - Force: " + force + " - Busy: " + NodeInformation.IsBusy());
        if(force || !NodeInformation.IsBusy()) {
            JsonObject response = ExecuteCommand.execRuntime(command, waitToFinishTask);

            response.addProperty(JsonCodec.OS.KillCommands.COMMAND, command);
            response.addProperty(JsonCodec.OS.KillCommands.WAIT_TO_FINISH, waitToFinishTask);
            
            logger.info(response);
            return response;
        } else {
            logger.info("Did not sterilise, not forcing on busy node");
            getJsonResponse().addKeyValues(JsonCodec.OUT, "Did not sterilise, not forcing on busy node");
            return getJsonResponse().getJson();
        }
    }

    private String buildWindowsCommand() {
        String command = "";
        List<String> unwantedProcesses = RuntimeConfig.getConfig().getUnwantedProcesses();
        if(unwantedProcesses.size() >= 1) {
            command = "taskkill -F";
            for(String process : unwantedProcesses) {
                command += " -IM " + process;
            }
        }
        return command;
    }

    private String buildLinuxCommand() {
        String command = "";
        List<String> unwantedProcesses = RuntimeConfig.getConfig().getUnwantedProcesses();
        if(unwantedProcesses.size() >= 1) {
            command = "killall -v";
            for(String process : unwantedProcesses) {
                command += " " + process;
            }
        }
        return command;
    }

}
