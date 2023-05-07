package com.ixnah.hmcl.logging;

import com.ixnah.hmcl.api.LoaderApi;
import org.slf4j.Logger;
import org.slf4j.ILoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * JULLoggerFactory is an implementation of {@link ILoggerFactory} returning
 * the appropriately named {@link JULLoggerAdapter} instance.
 *
 * @author Ceki G&uuml;lc&uuml;
 */
public class JULLoggerFactory implements ILoggerFactory {

    // key: name (String), value: a JDK14LoggerAdapter;
    ConcurrentMap<String, Logger> loggerMap;

    /**
     * the root logger is called "" in JUL
     */
    private static String JUL_ROOT_LOGGER_NAME = "";

    public JULLoggerFactory() {
        loggerMap = new ConcurrentHashMap<>();
        // ensure jul initialization. see SLF4J-359
        // note that call to java.util.logging.LogManager.getLogManager() fails on the Google App Engine platform. See
        // SLF4J-363
        java.util.logging.Logger.getLogger("");
    }

    /*
     * (non-Javadoc)
     *
     * @see org.slf4j.ILoggerFactory#getLogger(java.lang.String)
     */
    public Logger getLogger(String name) {
        // the root logger is called "" in JUL
        if (name.equalsIgnoreCase(Logger.ROOT_LOGGER_NAME)) {
            name = JUL_ROOT_LOGGER_NAME;
        }

        Logger slf4jLogger = loggerMap.get(name);
        if (slf4jLogger != null)
            return slf4jLogger;
        else {
            java.util.logging.Logger julLogger = java.util.logging.Logger.getLogger(name);
            julLogger.setParent(LoaderApi.getHmclLogger());
            Logger newInstance = new JULLoggerAdapter(julLogger);
            Logger oldInstance = loggerMap.putIfAbsent(name, newInstance);
            return oldInstance == null ? newInstance : oldInstance;
        }
    }
}
