package io.dropwizard.logging;

import ch.qos.logback.core.Appender;
import ch.qos.logback.core.AsyncAppenderBase;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.LogbackException;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.DeferredProcessingAware;
import ch.qos.logback.core.spi.FilterReply;
import ch.qos.logback.core.status.Status;
import com.google.common.util.concurrent.RateLimiter;
import io.dropwizard.util.Duration;

import java.util.List;

/**
 * An {@link AsyncAppenderBase} that applies throttling to a proxied appender.
 * Throttling is defined by an average duration between messages.
 * Throttled messages are discarded.
 */
class ThrottlingAppenderWrapper<E extends DeferredProcessingAware> implements Appender<E>, AsyncAppenderBaseProxy<E> {

    private final AsyncAppenderBase<E> appender;
    private final RateLimiter rateLimiter;

    public ThrottlingAppenderWrapper(AsyncAppenderBase<E> delegate, Duration messageRate) {
        this.appender = delegate;
        this.rateLimiter = RateLimiter.create(1_000_000_000.0 / messageRate.toNanoseconds());
    }

    @Override
    public AsyncAppenderBase<E> getAppender() {
        return appender;
    }

    @Override
    public void start() {
        appender.start();
    }

    @Override
    public void stop() {
        appender.stop();
    }

    @Override
    public boolean isStarted() {
        return appender.isStarted();
    }

    @Override
    public void doAppend(E event) throws LogbackException {
        if (rateLimiter.tryAcquire()) {
            appender.doAppend(event);
        }
    }

    @Override
    public String getName() {
        return appender.getName();
    }

    @Override
    public void setName(String name) {
        appender.setName(name);
    }

    @Override
    public Context getContext() {
        return appender.getContext();
    }

    @Override
    public void setContext(Context context) {
        appender.setContext(context);
    }

    @Override
    public void addStatus(Status status) {
        appender.addStatus(status);
    }

    @Override
    public void addInfo(String msg) {
        appender.addInfo(msg);
    }

    @Override
    public void addInfo(String msg, Throwable ex) {
        appender.addInfo(msg, ex);
    }

    @Override
    public void addWarn(String msg) {
        appender.addWarn(msg);
    }

    @Override
    public void addWarn(String msg, Throwable ex) {
        appender.addWarn(msg, ex);
    }

    @Override
    public void addError(String msg) {
        appender.addError(msg);
    }

    @Override
    public void addError(String msg, Throwable ex) {
        appender.addError(msg, ex);
    }

    @Override
    public void addFilter(Filter<E> newFilter) {
        appender.addFilter(newFilter);
    }

    @Override
    public void clearAllFilters() {
        appender.clearAllFilters();
    }

    @Override
    public List<Filter<E>> getCopyOfAttachedFiltersList() {
        return appender.getCopyOfAttachedFiltersList();
    }

    @Override
    public FilterReply getFilterChainDecision(E event) {
        return appender.getFilterChainDecision(event);
    }
}
