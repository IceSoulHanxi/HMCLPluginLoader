package com.ixnah.hmcl.logging;

import org.slf4j.Marker;
import org.slf4j.event.Level;
import org.slf4j.helpers.LegacyAbstractLogger;
import org.slf4j.spi.LocationAwareLogger;

public class SwitchLoggerAdapter extends LegacyAbstractLogger implements LocationAwareLogger {
    private static final String CALLER_NAME = SwitchLoggerAdapter.class.getName();

    private volatile LocationAwareLogger delegate;
    private final String name;

    public SwitchLoggerAdapter(LocationAwareLogger delegate, String name) {
        this.delegate = delegate;
        this.name = name;
    }

    public LocationAwareLogger getDelegate() {
        return delegate;
    }

    public void setDelegate(LocationAwareLogger delegate) {
        synchronized (this) {
            this.delegate = delegate;
        }
    }

    @Override
    protected String getFullyQualifiedCallerName() {
        return CALLER_NAME;
    }

    @Override
    protected void handleNormalizedLoggingCall(Level level, Marker marker, String messagePattern, Object[] arguments, Throwable throwable) {
        delegate.log(marker, name, level.toInt(), messagePattern, arguments, throwable);
    }

    @Override
    public void log(Marker marker, String fqcn, int level, String message, Object[] argArray, Throwable t) {
        delegate.log(marker, fqcn, level, message, argArray, t);
    }

    @Override
    public boolean isTraceEnabled() {
        return delegate.isTraceEnabled();
    }

    @Override
    public boolean isDebugEnabled() {
        return delegate.isTraceEnabled();
    }

    @Override
    public boolean isInfoEnabled() {
        return delegate.isInfoEnabled();
    }

    @Override
    public boolean isWarnEnabled() {
        return delegate.isWarnEnabled();
    }

    @Override
    public boolean isErrorEnabled() {
        return delegate.isErrorEnabled();
    }
}
