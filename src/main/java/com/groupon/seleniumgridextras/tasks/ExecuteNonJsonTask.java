package com.groupon.seleniumgridextras.tasks;

/**
 *
 * @author JamesFraser
 */

import com.groupon.seleniumgridextras.ExtrasEndPoint;
import com.groupon.seleniumgridextras.config.RuntimeConfig;
import org.apache.log4j.Logger;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public abstract class ExecuteNonJsonTask extends ExtrasEndPoint {

    final public String notImplementedError
            = "This task was not implemented on " + RuntimeConfig.getOS().getOSName();

    public boolean waitToFinishTask = true;

    private static Logger logger = Logger.getLogger(ExecuteOSTask.class);

    public boolean initialize() {

        if (allDependenciesLoaded()) {
            printInitilizedSuccessAndRegisterWithAPI();
            return true;
        } else {
            printInitilizedFailure();
            return false;
        }
    }

    public void printInitilizedSuccessAndRegisterWithAPI() {

        final String message
                = "Y " + this.getClass().getSimpleName() + " - " + this.getEndpoint() + " - " + this
                .getDescription();

        System.out.println(message);
        logger.info(message);
        registerApi();
    }

    public void printInitilizedFailure() {
        final String message = "N " + this.getClass().getSimpleName();
        System.out.println(message);
        logger.info(message);
    }

    public Boolean allDependenciesLoaded() {
        Boolean returnValue = true;

        for (String module : getDependencies()) {
            if (RuntimeConfig.getConfig().checkIfModuleEnabled(module) && returnValue) {
                logger.info(this.getClass().getSimpleName() + " is enabled");
            } else {
                logger.info("  " + this.getClass().getSimpleName() + " depends on " + module
                        + " but it is not activated");
                returnValue = false;
            }
        }
        return returnValue;
    }

    public List<String> getDependencies() {
        List<String> dependencies = new LinkedList();
        return dependencies;
    }

    protected void systemAndLog(String output) {
        System.out.println(output);
        logger.info(output);
    }

    public String execute(Map<String, String> parameter) {
        return execute();
    }

    public String execute() {
        return "Not overridden";
    }

}
