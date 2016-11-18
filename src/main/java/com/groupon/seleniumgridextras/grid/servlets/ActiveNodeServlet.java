
package com.groupon.seleniumgridextras.grid.servlets;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.groupon.seleniumgridextras.utilities.json.JsonParserWrapper;
import org.openqa.grid.internal.Registry;
import org.openqa.grid.internal.RemoteProxy;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.apache.log4j.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.openqa.grid.internal.ProxySet;
import org.openqa.grid.web.servlet.RegistryBasedServlet;

public class ActiveNodeServlet extends RegistryBasedServlet {

    private static final long serialVersionUID = 8484071790930378855L;

    public ActiveNodeServlet() {
        this(null);
    }

    public ActiveNodeServlet(Registry registry) {
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
        JsonArray nodes = new JsonArray();

        ProxySet allProxies = this.getRegistry().getAllProxies();
        
        Future[] calls = new Future[allProxies.size()];

        int counter = 0;
        for (RemoteProxy prox : allProxies) {
            calls[counter++] = ServletThreadPool.startCallable((new ActiveNodeServletThread(prox)));
        }

        for (Future call : calls) {
            try {
                nodes.add((JsonObject)call.get());
            }catch (InterruptedException ex) {
                Logger.getLogger(ActiveNodeServlet.class.getName()).error(ex);
            }catch (ExecutionException ex) {
                Logger.getLogger(ActiveNodeServlet.class.getName()).error(ex);
            }
        }

        requestJSON.add("nodes", nodes);
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

}
