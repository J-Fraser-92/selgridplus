/**
 * Copyright (c) 2013, Groupon, Inc. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * Neither the name of GROUPON nor the names of its contributors may be used to
 * endorse or promote products derived from this software without specific prior
 * written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * Created with IntelliJ IDEA. User: Dima Kovalenko (@dimacus) && Darko Marinov
 * Date: 5/10/13 Time: 4:06 PM
 */
package com.groupon.seleniumgridextras.grid.proxies;

import com.google.gson.JsonObject;
import com.groupon.seleniumgridextras.config.RuntimeConfig;
import com.groupon.seleniumgridextras.grid.proxies.sessions.threads.NodeRestartCallable;
import com.groupon.seleniumgridextras.loggers.counters.CustomCommandLogger;
import com.groupon.seleniumgridextras.tasks.config.TaskDescriptions;
import com.groupon.seleniumgridextras.utilities.JsonWireCommandTranslator;
import com.groupon.seleniumgridextras.utilities.json.JsonCodec;
import com.groupon.seleniumgridextras.utilities.threads.CommonThreadPool;
import com.groupon.seleniumgridextras.utilities.threads.RemoteGridExtrasAsyncCallable;
import com.groupon.seleniumgridextras.utilities.threads.SessionHistoryCallable;
import com.groupon.seleniumgridextras.utilities.threads.video.RemoteVideoRecordingControlCallable;
import org.apache.log4j.Logger;
import org.openqa.grid.common.RegistrationRequest;
import org.openqa.grid.internal.Registry;
import org.openqa.grid.internal.TestSession;
import org.openqa.grid.internal.listeners.TestSessionListener;
import org.openqa.grid.selenium.proxy.DefaultRemoteProxy;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

public class SetupTeardownProxy extends DefaultRemoteProxy implements TestSessionListener {

    private boolean available = true;
    private boolean restarting = false;
    private boolean busy = false;

    private List<String> sessionsRecording = new LinkedList<String>();

    private static Logger logger = Logger.getLogger(SetupTeardownProxy.class);
    private CustomCommandLogger sessionCommandLog = new CustomCommandLogger();

    public SetupTeardownProxy(RegistrationRequest request, Registry registry) {
        super(request, registry);
        logger.info(String.format("Attaching node %s", this.getId()));
    }

    @Override
    public TestSession getNewSession(Map<String, Object> requestedCapability) {
        if (isDown()) {
            return null;
        }

        if (isRestarting() || !isAvailable() || busy) {
            return null;
        }

        TestSession session;
        try {
            session = super.getNewSession(requestedCapability);
        } catch (Exception e) {
            logger.error(
                    String.format(
                            "Something went terribly wrong when trying to connect to remote node on proxy: %s requested capabilities: %s\n",
                            this.getId(),
                            requestedCapability));
            return null;
        }
        try {
            String host = session.getSlot().getRemoteURL().getHost();

            HashMap<String, String> sessionDetails = new HashMap<String, String>();
            JsonObject details = new JsonObject();
            details.addProperty(JsonCodec.WebDriver.Grid.ENVIRONMENT,
                    requestedCapability.containsKey(JsonCodec.WebDriver.Grid.ENVIRONMENT)
                            ? requestedCapability.get(JsonCodec.WebDriver.Grid.ENVIRONMENT).toString()
                            : "Unknown");
            details.addProperty(JsonCodec.WebDriver.Grid.BROWSER_NAME,
                    requestedCapability.containsKey(JsonCodec.WebDriver.Grid.BROWSER_NAME)
                            ? requestedCapability.get(JsonCodec.WebDriver.Grid.BROWSER_NAME).toString()
                            : "Unknown");
            sessionDetails.put(JsonCodec.PARAMETER, details.toString());

            CommonThreadPool.startCallable(new SessionHistoryCallable(session));

            CommonThreadPool.startCallable(
                    new RemoteGridExtrasAsyncCallable(
                            host,
                            3000,
                            TaskDescriptions.Endpoints.START_NODE_SESSION,
                            sessionDetails));



            startVideoRecording(session);

        } catch (Exception e) {
            logger.error(String.format("Error communicating with %s, \n%s",
                    session.getSlot().getProxy().getId(), e));
        } finally {
            return session;
        }

    }

    @Override
    public void beforeCommand(TestSession session, HttpServletRequest request,
                              HttpServletResponse response) {
        updateLastCommand(session, request);
        session.put("lastCommand", request.getMethod() + " - " + request.getPathInfo() + " executed.");

        String command = new JsonWireCommandTranslator(request.getMethod(), request.getRequestURI(), JsonWireCommandTranslator.getBodyAsString(request)).toString();
        command = command.replace(JsonWireCommandTranslator.getBodyAsString(request), "").replace(":", "").trim();

        sessionCommandLog.recordNew(command);
    }

    @Override
    public void beforeSession(TestSession session) {
        super.beforeSession(session);
        logger.info("BeforeSession - " + session.toString());
        busy = true;
    }

    @Override
    public void afterSession(TestSession session) {
        super.afterSession(session);
        busy = false;

        if (session.getExternalKey() != null) {
            stopVideoRecording(session);
        }

        CommonThreadPool.startCallable(
                new RemoteGridExtrasAsyncCallable(
                        this.getRemoteHost().getHost(),
                        RuntimeConfig.getGridExtrasPort(),
                        TaskDescriptions.Endpoints.TEARDOWN,
                        new HashMap<String, String>()));

        if (NodeRestartCallable.timeToReboot(this.getRemoteHost().getHost(), this.getId())) {
            this.setAvailable(false);
            this.setRestarting(true);

            CommonThreadPool.startCallable(
                    new NodeRestartCallable(
                            this,
                            session));
        }
    }

    private boolean alreadyRecordingCurrentSession(TestSession session) {
        if ((session.getExternalKey() == null) || !getSessionsRecording().contains(session.getExternalKey().getKey())) {
            return false;
        }

        return true;
    }

    private void startVideoRecording(TestSession session) {

        if (alreadyRecordingCurrentSession(session)) {
            return;
        }

        CommonThreadPool.startCallable(
                new RemoteVideoRecordingControlCallable(
                        this,
                        session,
                        JsonCodec.Video.START));
    }

    private void stopVideoRecording(TestSession session) {
        Future a = CommonThreadPool.startCallable(
                new RemoteVideoRecordingControlCallable(
                        this,
                        session,
                        JsonCodec.Video.STOP));

        //try {
        logger.info(String.format(
                //"Waiting for stop command to finish for session: %s, output:\n%s",
                //session.getExternalKey().getKey(),
                //a.get()));
                "Waiting for stop command to finish for session: %s",
                session.getExternalKey().getKey()));

        //} catch (InterruptedException e) {
        //    logger.error(e.getMessage(), e);
        // } catch (ExecutionException e) {
        //     logger.error(e.getMessage(), e);
        // }
    }

    private void updateLastCommand(TestSession session, HttpServletRequest request) {

        if (session.getExternalKey() == null) {
            return;
        }

        try {
            String command
                    = new JsonWireCommandTranslator(request.getMethod(), request.getRequestURI(),
                    JsonWireCommandTranslator.getBodyAsString(request))
                    .toString();

            CommonThreadPool.startCallable(
                    new RemoteVideoRecordingControlCallable(
                            this,
                            session,
                            JsonCodec.Video.HEARTBEAT,
                            command));
        } catch (Exception e) {
            logger.error(String.format("Error updating last action for int. key: %s"), e);
        }
    }

    public List<String> getSessionsRecording() {
        return this.sessionsRecording;
    }

    protected boolean isAvailable() {
        return this.available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public boolean isRestarting() {
        return this.restarting;
    }

    public void setRestarting(boolean restarting) {
        this.restarting = restarting;
    }

}