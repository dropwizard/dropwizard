package io.dropwizard.logging;

import java.util.Optional;
import java.util.TimeZone;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import ch.qos.logback.classic.AsyncAppender;
import ch.qos.logback.classic.Level;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.AsyncAppenderBase;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.spi.DeferredProcessingAware;

import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * A base implementation of {@link AppenderFactory}.
 * <p/>
 * <b>Configuration Parameters:</b>
 * <table>
 *     <tr>
 *         <td>Name</td>
 *         <td>Default</td>
 *         <td>Description</td>
 *     </tr>
 *     <tr>
 *         <td>{@code threshold}</td>
 *         <td>ALL</td>
 *         <td>The minimum event level the appender will handle.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code queueSize}</td>
 *         <td>{@link AsyncAppenderBase}</td>
 *         <td>The maximum capacity of the blocking queue.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code includeCallerData}</td>
 *         <td>{@link AsyncAppenderBase}</td>
 *         <td>
 *             Whether to include caller data, required for line numbers.
 *             Beware, is considered expensive.
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>{@code discardingThreshold}</td>
 *         <td>{@link AsyncAppenderBase}</td>
 *         <td>
 *             By default, when the blocking queue has 20% capacity remaining,
 *             it will drop events of level TRACE, DEBUG and INFO, keeping only
 *             events of level WARN and ERROR. To keep all events, set discardingThreshold to 0.
 *         </td>
 *     </tr>
 * </table>
 */
public abstract class AbstractAppenderFactory<E extends DeferredProcessingAware> implements AppenderFactory<E> {

    private static String getDefaultLogFormat(TimeZone timeZone) {
        return "%-5p [%d{ISO8601," + timeZone.getID() + "}] %c: %m%n%rEx";
    }

    @NotNull
    protected Level threshold = Level.ALL;

    protected Optional<String> logFormat = Optional.empty();

    @NotNull
    protected TimeZone timeZone = TimeZone.getTimeZone("UTC");

    @Min(1)
    @Max(Integer.MAX_VALUE)
    private int queueSize = AsyncAppenderBase.DEFAULT_QUEUE_SIZE;

    private int discardingThreshold = -1;

    @JsonProperty
    @Override
    public String getLogFormat() {
        return logFormat.orElse(getDefaultLogFormat(timeZone));
    }

    @JsonProperty
    public void setLogFormat(String logFormat) {
        this.logFormat = Optional.ofNullable(logFormat);
    }

    @JsonProperty
    public TimeZone getTimeZone() {
        return timeZone;
    }

    @JsonProperty
    public void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
    }

    private boolean includeCallerData = false;

    @JsonProperty
    public int getQueueSize() {
        return queueSize;
    }

    @JsonProperty
    public void setQueueSize(int queueSize) {
        this.queueSize = queueSize;
    }

    @JsonProperty
    public int getDiscardingThreshold() {
        return discardingThreshold;
    }

    @JsonProperty
    public void setDiscardingThreshold(int discardingThreshold) {
        this.discardingThreshold = discardingThreshold;
    }

    @JsonProperty
    public Level getThreshold() {
        return threshold;
    }

    @JsonProperty
    public void setThreshold(Level threshold) {
        this.threshold = threshold;
    }

    @JsonProperty
    public boolean isIncludeCallerData() {
        return includeCallerData;
    }

    @JsonProperty
    public void setIncludeCallerData(boolean includeCallerData) {
        this.includeCallerData = includeCallerData;
    }

    protected Appender<E> wrapAsync(Appender<E> appender, AsyncAppenderFactory<E> asyncAppenderFactory) {
        return wrapAsync(appender, asyncAppenderFactory, appender.getContext());
    }

    protected Appender<E> wrapAsync(Appender<E> appender, AsyncAppenderFactory<E> asyncAppenderFactory, Context context) {
        final AsyncAppenderBase<E> asyncAppender = asyncAppenderFactory.build();
        if (asyncAppender instanceof AsyncAppender) {
            ((AsyncAppender) asyncAppender).setIncludeCallerData(includeCallerData);
        }
        asyncAppender.setQueueSize(queueSize);
        asyncAppender.setDiscardingThreshold(discardingThreshold);
        asyncAppender.setContext(context);
        asyncAppender.setName("async-" + appender.getName());
        asyncAppender.addAppender(appender);
        asyncAppender.start();
        return asyncAppender;
    }
}
