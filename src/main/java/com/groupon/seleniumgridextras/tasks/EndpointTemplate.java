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
import com.groupon.seleniumgridextras.config.RuntimeConfig;
import com.groupon.seleniumgridextras.tasks.config.TaskDescriptions;
import com.groupon.seleniumgridextras.utilities.json.JsonCodec;
import java.util.Map;
import org.apache.log4j.Logger;

public class EndpointTemplate extends ExecuteOSTask {

    private static Logger logger = Logger.getLogger(EndpointTemplate.class);

    //Once endpoint is made, must be added to DefaultConfig.java - loadEnabledPlugins() to become active
    
    public EndpointTemplate() {
               
        //These are best maintained in TaskDescriptions, but can be strings for drafting/testing
        //Set the endpoint - eg "/template"
        setEndpoint(TaskDescriptions.Endpoints.DASHBOARD_NODE_STATUS);
        //Set the description
        setDescription(TaskDescriptions.Description.DASHBOARD_NODE_STATUS);

        JsonObject params = new JsonObject();

        //Add any parameters and description here
        //Can leave params as empty JsonObject if no parameters
        params.addProperty(JsonCodec.WebDriver.Downloader.VERSION, "Version of driver to download and set, such as 2.11");
        params.addProperty(JsonCodec.WebDriver.Downloader.REBOOT, "Set false if no reboot wanted yet");

        //Mostly just leave these
        setAcceptedParams(params);
        setRequestType("GET");
        setResponseType("json");
        setClassname(this.getClass().getCanonicalName().toString());
        setCssClass("btn-success");
        
        //Give it a name here
        setButtonText("Template");
        
        //Leave this
        setEnabledInGui(true);

        //These are the values to be returned by the endpoint
        //Give them a name and description
        //The values returned must be declared here, or the endpoint will fail
        addResponseDescription(JsonCodec.WebDriver.JSON.IP_ADDRESS, "IP address of node");
        addResponseDescription(JsonCodec.WebDriver.JSON.MACHINE_NAME, "Name of node computer");
    }

    //Can use any amount of the execute methods below
    //They extend methods in ExecuteOSTask, so if not declared here then default ones are used
        
    //Run without any parameters
    //Can be called manually by a method, or when endpoint provides no parameters
    @Override
    public JsonObject execute() {
        getJsonResponse().addKeyValues(JsonCodec.WebDriver.JSON.IP_ADDRESS, NodeInformation.GetIpAddress());
        getJsonResponse().addKeyValues(JsonCodec.WebDriver.JSON.MACHINE_NAME, NodeInformation.GetHostName());
        
        //Returns the response
        return getJsonResponse().getJson();
    }
    
    //Manually called method, normally called from execute with parameters
    @Override
    public JsonObject execute(String parameter) {
    String command;

    //These allow you to run the same command in different ways on a different OS
    //Override get(OS)Command(parameter) to write your method
    if (RuntimeConfig.getOS().isWindows()) {
      command = getWindowsCommand(parameter);
    } else if (RuntimeConfig.getOS().isMac()) {
      command = getMacCommand(parameter);
    } else {
      command = getLinuxCommand(parameter);
    }

    //Executes the command in cmdline or shell etc
    return ExecuteCommand.execRuntime(command + parameter, waitToFinishTask);
  }
   
    //Execute with one or more parameters
    //Called by the endpoint, if no parameters given, calls execute()
    //Parameters are passed to endpoint like this
    // :3000/template?param1=value1&param2=value2&param3=value3 etc
    @Override
    public JsonObject execute(Map<String, String> parameter) {
    if (!parameter.isEmpty() && parameter.containsKey(JsonCodec.GridExtras.VERSION)) {
      return execute(parameter.get(JsonCodec.GridExtras.VERSION).toString());
    } else {
      return execute();
    }
  }
    
}
