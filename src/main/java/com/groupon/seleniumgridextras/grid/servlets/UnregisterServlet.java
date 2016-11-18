package com.groupon.seleniumgridextras.grid.servlets;

import org.openqa.grid.internal.Registry;
import org.openqa.grid.internal.RemoteProxy;
import java.io.IOException;
import org.apache.log4j.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.openqa.grid.internal.ProxySet;
import org.openqa.grid.web.servlet.RegistryBasedServlet;

public class UnregisterServlet extends RegistryBasedServlet {

    private static final long serialVersionUID = 8484071790930378855L;
    private static final Logger log = Logger.getLogger(UnregisterServlet.class.getName());

    public UnregisterServlet() {
        this(null);
    }

    public UnregisterServlet(Registry registry) {
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

    protected void process(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        ProxySet allProxies = this.getRegistry().getAllProxies();

        String remoteHost = request.getRemoteAddr();

        for (RemoteProxy proxy : allProxies) {
            if (proxy.getRemoteHost().getHost().equals(remoteHost)) {
                final RemoteProxy match = proxy;
                new Thread(new Runnable() {  // Thread safety reviewed
                    public void run() {
                        getRegistry().removeIfPresent(match);
                        log.info("proxy removed " + match.getRemoteHost().getHost());
                    }
                }).start();
            }
        }

        reply(response, "ok");

    }

    protected void reply(HttpServletResponse response, String content) throws IOException {
        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(200);
        response.getWriter().print(content);
    }

}
