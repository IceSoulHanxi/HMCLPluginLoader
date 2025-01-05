package com.ixnah.hmcl.logging;

import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.spi.LocationAwareLogger;

import java.text.MessageFormat;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.StreamHandler;

/**
 * JULLoggerFactory is an implementation of {@link ILoggerFactory} returning
 * the appropriately named {@link SwitchLoggerAdapter} instance.
 *
 * @author Ceki G&uuml;lc&uuml;
 */
public class HmclLoggerFactory implements ILoggerFactory {

    // key: name (String), value: a JDK14LoggerAdapter;
    private static final ConcurrentMap<String, Logger> LOGGER_MAP = new ConcurrentHashMap<>();

    /**
     * the root logger is called "" in JUL
     */
    private static final String JUL_ROOT_LOGGER_NAME = "";
    private final StreamHandler consoleHandler;

    public HmclLoggerFactory() {
        // ensure jul initialization. see SLF4J-359
        // note that call to java.util.logging.LogManager.getLogManager() fails on the Google App Engine platform. See
        // SLF4J-363
        java.util.logging.Logger.getLogger("");
        consoleHandler = new ConsoleHandler();
        consoleHandler.setFormatter(new Formatter() {
            final MessageFormat messageFormat = new MessageFormat("[{0,Time,HH:mm:ss}] [{1}.{2}/{3}] {4}\n");
            @Override
            public String format(LogRecord record) {
                return messageFormat.format(new Object[]{new Date(record.getMillis()), record.getSourceClassName(), record.getSourceMethodName(), record.getLevel(), record.getMessage()});
            }
        });
    }

    /*
     * (non-Javadoc)
     *
     * @see org.slf4j.ILoggerFactory#getLogger(java.lang.String)
     */
    @Override
    public Logger getLogger(String name) {
        // the root logger is called "" in JUL
        if (name.equalsIgnoreCase(Logger.ROOT_LOGGER_NAME)) {
            name = JUL_ROOT_LOGGER_NAME;
        }

        AtomicReference<LocationAwareLogger> delegate = new AtomicReference<>();
        java.util.logging.Logger julLogger = java.util.logging.Logger.getLogger(name);
        julLogger.addHandler(consoleHandler);
        julLogger.setUseParentHandlers(false);
        delegate.set(HMCL_LOGGER != null ? HMCL_LOGGER : new JULLoggerAdapter(julLogger));
        return LOGGER_MAP.computeIfAbsent(name, k -> new SwitchLoggerAdapter(delegate.get(), k));
    }

    private static volatile LocationAwareLogger HMCL_LOGGER;

    public static void initHmclLogger(LocationAwareLogger delegate) {
        HMCL_LOGGER = delegate;
        LOGGER_MAP.forEach((k, v) -> ((SwitchLoggerAdapter) v).setDelegate(delegate));
    }
}
