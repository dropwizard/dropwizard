package io.dropwizard.logging;

import ch.qos.logback.core.Appender;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.LogbackException;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.DeferredProcessingAware;
import ch.qos.logback.core.spi.FilterReply;
import ch.qos.logback.core.status.Status;
import com.google.common.util.concurrent.RateLimiter;

import java.util.List;

/**
 * An {@link Appender} implementation that applies throttling to a delegate
 * appender. Throttling is defined by the max number of messages per second.
 * Throttled messages are discarded.
 */
class ThrottlingAppenderWrapper<E extends DeferredProcessingAware> implements Appender<E> {

    private final Appender<E> delegate;
    private final RateLimiter rateLimiter;

    public ThrottlingAppenderWrapper(Appender<E> delegate, double maxMessagesPerSecond) {
        this.delegate = delegate;
        this.rateLimiter = RateLimiter.create(maxMessagesPerSecond);
    }

    @Override
    public void start() {
        delegate.start();
    }

    @Override
    public void stop() {
        delegate.stop();
    }

    @Override
    public boolean isStarted() {
        return delegate.isStarted();
    }

    @Override
    public void doAppend(E event) throws LogbackException {
        if (rateLimiter.tryAcquire()) {
            delegate.doAppend(event);
        }
    }

    @Override
    public String getName() {
        return delegate.getName();
    }

    @Override
    public void setName(String name) {
        delegate.setName(name);
    }

    @Override
    public Context getContext() {
        return delegate.getContext();
    }

    @Override
    public void setContext(Context context) {
        delegate.setContext(context);
    }

    @Override
    public void addStatus(Status status) {
        delegate.addStatus(status);
    }

    @Override
    public void addInfo(String msg) {
        delegate.addInfo(msg);
    }

    @Override
    public void addInfo(String msg, Throwable ex) {
        delegate.addInfo(msg, ex);
    }

    @Override
    public void addWarn(String msg) {
        delegate.addWarn(msg);
    }

    @Override
    public void addWarn(String msg, Throwable ex) {
        delegate.addWarn(msg, ex);
    }

    @Override
    public void addError(String msg) {
        delegate.addError(msg);
    }

    @Override
    public void addError(String msg, Throwable ex) {
        delegate.addError(msg, ex);
    }

    @Override
    public void addFilter(Filter<E> newFilter) {
        delegate.addFilter(newFilter);
    }

    @Override
    public void clearAllFilters() {
        delegate.clearAllFilters();
    }

    @Override
    public List<Filter<E>> getCopyOfAttachedFiltersList() {
        return delegate.getCopyOfAttachedFiltersList();
    }

    @Override
    public FilterReply getFilterChainDecision(E event) {
        return delegate.getFilterChainDecision(event);
    }

}
