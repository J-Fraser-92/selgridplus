/*
 Copyright 2011 Selenium committers
 Copyright 2011 Software Freedom Conservancy

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
package com.groupon.seleniumgridextras.grid.servlets;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.groupon.seleniumgridextras.utilities.json.JsonParserWrapper;
import org.openqa.grid.internal.Registry;
import org.openqa.grid.internal.RemoteProxy;
import org.openqa.selenium.remote.DesiredCapabilities;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.openqa.grid.internal.ProxySet;
import org.openqa.grid.internal.TestSlot;
import org.openqa.grid.web.servlet.RegistryBasedServlet;

public class GridReporterServlet extends RegistryBasedServlet {

    private static final long serialVersionUID = 8484071790930378855L;
    private Date lastRegistryPull = null;
    //private int queueSize = -1;
    private Iterable<DesiredCapabilities> caps = Collections.<DesiredCapabilities>emptyList();
    private ProxySet allProxies = new ProxySet(false);

    public GridReporterServlet() {
        this(null);
    }

    public GridReporterServlet(Registry registry) {
        super(registry);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        process(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        process(request, response);
    }

    private JsonObject getResponse() throws IOException {
        JsonObject requestJSON = new JsonObject();

        JsonArray queue = new JsonArray();
        JsonArray nodes = new JsonArray();
        JsonArray active = new JsonArray();
        JsonArray errors = new JsonArray();

        updateData();

        for (DesiredCapabilities req : caps) {
            queue.add(JsonParserWrapper.toJsonObject(req.asMap()));
        }
        
        
        for (RemoteProxy prox : allProxies) {
            try {
                JsonObject assocJson = prox.getOriginalRegistrationRequest().getAssociatedJSON();
                JsonObject config = assocJson.getAsJsonObject("configuration");
                nodes.add(assocJson);

                List<TestSlot> slots = prox.getTestSlots();
                for (TestSlot slot : slots) {
                    if (slot.getSession() != null) {
                        JsonObject activeSlot = new JsonObject();
                        
                        Object host = config.get("host");
                        if (host != null) {
                            activeSlot.addProperty("host", host.toString());
                        } else {
                            activeSlot.addProperty("host", "no_host");
                        }
                        
                        Object hostname = config.get("friendlyHostName");
                        if (hostname != null) {
                            activeSlot.addProperty("hostname", hostname.toString());
                        } else {
                            activeSlot.addProperty("hostname", "no_hostname");
                        }
                        
                        Object environment = slot.getSession().getRequestedCapabilities().get("environment");
                        if (environment != null) {
                            activeSlot.addProperty("environment", environment.toString());
                        } else {
                            activeSlot.addProperty("environment", "no_environment");
                        }
                        
                        active.add(activeSlot);
                    }
                }
            } catch (Exception e) {
                JsonObject error_json = new JsonObject();
                error_json.addProperty("proxy", prox.getRemoteHost().toString());
                error_json.addProperty("message", "Error in determining active nodes");
                error_json.addProperty("exception", e.getMessage());
                
                errors.add(error_json);
                
            }
        }

        requestJSON.add("nodes", nodes);
        requestJSON.addProperty("queue_size", queue.size());
        requestJSON.add("current_queue", queue);
        requestJSON.add("active_sessions", active);
        requestJSON.add("errors", errors);
        return requestJSON;
    }

    protected void process(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(200);

        JsonObject res = getResponse();
        response.getWriter().print(JsonParserWrapper.prettyPrintString(res));
        response.getWriter().close();

    }

    private void updateData() {
        if (lastRegistryPull != null) {
            if (getDateDiff(lastRegistryPull, new Date(), TimeUnit.SECONDS) < 10) {
                return;
            }
        }

        caps = this.getRegistry().getDesiredCapabilities();
        allProxies = this.getRegistry().getAllProxies();
        lastRegistryPull = new Date();
    }

    private static long getDateDiff(Date date1, Date date2, TimeUnit timeUnit) {
        long diffInMillis = date2.getTime() - date1.getTime();
        return timeUnit.convert(diffInMillis, TimeUnit.MILLISECONDS);
    }
}
