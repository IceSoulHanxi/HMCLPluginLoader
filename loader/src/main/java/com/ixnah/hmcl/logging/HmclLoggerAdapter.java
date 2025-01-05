package com.ixnah.hmcl.logging;

import com.ixnah.hmcl.api.LoaderApi;
import org.slf4j.Marker;
import org.slf4j.event.Level;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.LegacyAbstractLogger;
import org.slf4j.helpers.MessageFormatter;
import org.slf4j.spi.LocationAwareLogger;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.EnumMap;

public class HmclLoggerAdapter extends LegacyAbstractLogger implements LocationAwareLogger {
    private static final String CALLER_NAME = HmclLoggerAdapter.class.getName();

    private final EnumMap<Level, Object> LEVEL_MAP = new EnumMap<>(Level.class);
    private final MethodHandle logHandle;
    private final MethodHandle getStackTraceHandle;

    public HmclLoggerAdapter(ClassLoader classLoader) throws Exception {
        classLoader.loadClass("org.jackhuang.hmcl.util.logging.CallerFinder");
        Class<?> loggerClass = classLoader.loadClass("org.jackhuang.hmcl.util.logging.Logger");
        Class<?> levelClass = classLoader.loadClass("org.jackhuang.hmcl.util.logging.Level");
        Field logField = loggerClass.getField("LOG");
        Method logMethod = loggerClass.getMethod("log", levelClass, String.class);
        logHandle = MethodHandles.lookup().unreflect(logMethod).bindTo(logField.get(null));
        LEVEL_MAP.put(Level.TRACE, levelClass.getField("TRACE").get(null));
        LEVEL_MAP.put(Level.DEBUG, levelClass.getField("DEBUG").get(null));
        LEVEL_MAP.put(Level.INFO, levelClass.getField("INFO").get(null));
        LEVEL_MAP.put(Level.WARN, levelClass.getField("WARNING").get(null));
        LEVEL_MAP.put(Level.ERROR, levelClass.getField("ERROR").get(null));
        Class<?> stringUtilsClass = classLoader.loadClass("org.jackhuang.hmcl.util.StringUtils");
        Method getStackTranceMethod = stringUtilsClass.getMethod("getStackTrace", Throwable.class);
        getStackTraceHandle = MethodHandles.lookup().unreflect(getStackTranceMethod);
    }

    @Override
    protected String getFullyQualifiedCallerName() {
        return CALLER_NAME;
    }

    @Override
    protected void handleNormalizedLoggingCall(Level level, Marker marker, String messagePattern, Object[] arguments, Throwable throwable) {
        Object hmclLevel = LEVEL_MAP.get(level);
        FormattingTuple tuple = MessageFormatter.arrayFormat(messagePattern, arguments, throwable);
        try {
            String msg = tuple.getMessage();
            if (throwable != null) {
                msg = msg + "\n" + getStackTraceHandle.invoke(throwable);
            }
            logHandle.invoke(hmclLevel, msg);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void log(Marker marker, String fqcn, int level, String message, Object[] argArray, Throwable t) {
        this.handleNormalizedLoggingCall(Level.intToLevel(level), marker, message, argArray, t);
    }

    @Override
    public boolean isTraceEnabled() {
        return LoaderApi.getPluginManager().isDevelopment();
    }

    @Override
    public boolean isDebugEnabled() {
        return LoaderApi.getPluginManager().isDevelopment();
    }

    @Override
    public boolean isInfoEnabled() {
        return true;
    }

    @Override
    public boolean isWarnEnabled() {
        return true;
    }

    @Override
    public boolean isErrorEnabled() {
        return true;
    }
}
