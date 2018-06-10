package io.dropwizard.logging;

import ch.qos.logback.core.Appender;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.LogbackException;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;
import ch.qos.logback.core.status.Status;
import io.dropwizard.util.Duration;
import java.util.List;

/**
 * An {@link Appender} implementation that applies throttling to a delegate
 * appender. Throttling is defined by a time window and a max number of messages
 * over this time window. Throttled messages are discarded.
 */
class ThrottlingAppenderWrapper<E> implements Appender<E> {

    private final Appender<E> delegate;
    private final long throttlingTimeWindow;
    private final long[] timestamps;
    private int index;

    public ThrottlingAppenderWrapper(Appender<E> delegate, Duration throttlingTimeWindow, int maxMessagesPerThrottlingTimeWindow) {
        this.delegate = delegate;
        this.throttlingTimeWindow = throttlingTimeWindow.toNanoseconds();
        this.timestamps = new long[maxMessagesPerThrottlingTimeWindow];
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

    private boolean keepMessage() {
        boolean keep = true;
        long t = System.nanoTime();
        synchronized (timestamps) {
            if (index == timestamps.length) {
                if (timestamps[0] + throttlingTimeWindow > t) {
                    keep = false;
                } else {
                    System.arraycopy(timestamps, 1, timestamps, 0, timestamps.length - 1);
                    timestamps[timestamps.length - 1] = t;
                }
            } else {
                timestamps[index++] = t;
            }
        }
        return keep;
    }

    @Override
    public void doAppend(E event) throws LogbackException {
        if (keepMessage()) {
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
