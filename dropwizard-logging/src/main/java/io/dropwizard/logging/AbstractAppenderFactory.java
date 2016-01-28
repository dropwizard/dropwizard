package io.dropwizard.logging;

import ch.qos.logback.classic.AsyncAppender;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.AsyncAppenderBase;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.pattern.PatternLayoutBase;
import ch.qos.logback.core.spi.DeferredProcessingAware;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import io.dropwizard.logging.async.AsyncAppenderFactory;
import io.dropwizard.logging.filter.FilterFactory;
import io.dropwizard.logging.layout.LayoutFactory;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.TimeZone;

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
 *         <td>{@code logFormat}</td>
 *         <td>(none)</td>
 *         <td>An appender-specific log format.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code timeZone}</td>
 *         <td>{@code UTC}</td>
 *         <td>
 *             The time zone to which event timestamps will be converted.
 *             Ignored if logFormat is supplied.
 *         </td>
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
 *     <tr>
 *         <td>{@code filterFactories}</td>
 *         <td>(none)</td>
 *         <td>
 *             A list of {@link FilterFactory filters} to apply to the appender, in order,
 *             after the {@code threshold}.
 *         </td>
 *     </tr>
 * </table>
 */
public abstract class AbstractAppenderFactory<E extends DeferredProcessingAware> implements AppenderFactory<E> {

    @NotNull
    protected Level threshold = Level.ALL;

    protected String logFormat;

    @NotNull
    protected TimeZone timeZone = TimeZone.getTimeZone("UTC");

    @Min(1)
    @Max(Integer.MAX_VALUE)
    private int queueSize = AsyncAppenderBase.DEFAULT_QUEUE_SIZE;

    private int discardingThreshold = -1;

    private boolean includeCallerData = false;

    private ImmutableList<FilterFactory<E>> filterFactories = ImmutableList.of();

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
    public String getLogFormat() {
        return logFormat;
    }

    @JsonProperty
    public void setLogFormat(String logFormat) {
        this.logFormat = logFormat;
    }

    @JsonProperty
    public TimeZone getTimeZone() {
        return timeZone;
    }

    @JsonProperty
    public void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
    }

    @JsonProperty
    public boolean isIncludeCallerData() {
        return includeCallerData;
    }

    @JsonProperty
    public void setIncludeCallerData(boolean includeCallerData) {
        this.includeCallerData = includeCallerData;
    }

    @JsonProperty
    public ImmutableList<FilterFactory<E>> getFilterFactories() {
        return filterFactories;
    }

    @JsonProperty
    public void setFilterFactories(List<FilterFactory<E>> appenders) {
        this.filterFactories = ImmutableList.copyOf(appenders);
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

    protected PatternLayoutBase<E> buildLayout(LoggerContext context, LayoutFactory<E> layoutFactory) {
        final PatternLayoutBase<E> formatter = layoutFactory.build(context, timeZone);
        if (!Strings.isNullOrEmpty(logFormat)) {
            formatter.setPattern(logFormat);
        }
        formatter.start();
        return formatter;
    }
}
