package com.groupon.seleniumgridextras.grid.servlets;

import com.groupon.seleniumgridextras.utilities.threads.*;
import org.apache.log4j.Logger;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ServletThreadPool {

    private static Logger logger = Logger.getLogger(ServletThreadPool.class);
    protected static ExecutorService cachedPool;

    public static Future<String> startCallable(Callable callable) {

        if (cachedPool == null) {
            logger.info("Initializing new " + ServletThreadPool.class);
            cachedPool = Executors.newCachedThreadPool();
        }

        try {
            return (Future<String>) cachedPool.submit(callable);
        } catch (Exception e) {
            logger.error(e);
            return null;
        }
    }
}
