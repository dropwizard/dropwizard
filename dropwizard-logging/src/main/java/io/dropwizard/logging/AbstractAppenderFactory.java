package io.dropwizard.logging;

import ch.qos.logback.classic.AsyncAppender;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.AsyncAppenderBase;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.LayoutBase;
import ch.qos.logback.core.pattern.PatternLayoutBase;
import ch.qos.logback.core.spi.DeferredProcessingAware;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.logback.ThrottlingAppenderWrapper;
import io.dropwizard.logging.async.AsyncAppenderFactory;
import io.dropwizard.logging.filter.FilterFactory;
import io.dropwizard.logging.layout.DiscoverableLayoutFactory;
import io.dropwizard.logging.layout.LayoutFactory;
import io.dropwizard.util.Strings;
import io.dropwizard.util.Duration;
import io.dropwizard.validation.MaxDuration;
import io.dropwizard.validation.MinDuration;

import javax.annotation.Nullable;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

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
 *         <td>{@code messageRate}</td>
 *         <td>
 *             Maximum message rate: average duration between messages. Extra messages are discarded.
 *             This setting avoids flooding a paid logging service by accident.
 *             For example, a duration of 100ms allows for a maximum of 10 messages per second and 30s would mean
 *             1 message every 30 seconds.
 *             The maximum acceptable duration is 1 minute.
 *             By default, this duration is not set and this feature is disabled.
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

    @Nullable
    protected String logFormat;

    @Nullable
    protected DiscoverableLayoutFactory<E> layout;

    @NotNull
    protected TimeZone timeZone = TimeZone.getTimeZone("UTC");

    @Min(1)
    @Max(Integer.MAX_VALUE)
    private int queueSize = AsyncAppenderBase.DEFAULT_QUEUE_SIZE;

    private int discardingThreshold = -1;

    @Nullable
    @MinDuration(value = 0, unit = TimeUnit.SECONDS, inclusive = false)
    @MaxDuration(value = 1, unit = TimeUnit.MINUTES)
    private Duration messageRate;

    private boolean includeCallerData = false;

    private List<FilterFactory<E>> filterFactories = Collections.emptyList();

    private boolean neverBlock = false;

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
    @Nullable
    public Duration getMessageRate() {
        return messageRate;
    }

    @JsonProperty
    public void setMessageRate(Duration messageRate) {
        this.messageRate = messageRate;
    }

    @JsonProperty
    public String getThreshold() {
        return threshold.toString();
    }

    @JsonProperty
    public void setThreshold(String threshold) {
        this.threshold = DefaultLoggingFactory.toLevel(threshold);
    }

    @JsonProperty
    @Nullable
    public String getLogFormat() {
        return logFormat;
    }

    @JsonProperty
    public void setLogFormat(@Nullable String logFormat) {
        this.logFormat = logFormat;
    }

    @JsonProperty
    public TimeZone getTimeZone() {
        return timeZone;
    }

    @JsonProperty
    public void setTimeZone(String zoneId) {
        this.timeZone = Strings.nullToEmpty(zoneId).equalsIgnoreCase("system") ? TimeZone.getDefault() :
            TimeZone.getTimeZone(zoneId);
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
    public List<FilterFactory<E>> getFilterFactories() {
        return filterFactories;
    }

    @JsonProperty
    public void setFilterFactories(List<FilterFactory<E>> appenders) {
        this.filterFactories = new ArrayList<>(appenders);
    }

    @JsonProperty
    public void setNeverBlock(boolean neverBlock) {
        this.neverBlock = neverBlock;
    }

    @Nullable
    public DiscoverableLayoutFactory<?> getLayout() {
        return layout;
    }

    public void setLayout(@Nullable DiscoverableLayoutFactory<E> layout) {
        this.layout = layout;
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
        asyncAppender.setNeverBlock(neverBlock);
        asyncAppender.start();
        if (messageRate == null) {
            return asyncAppender;
        } else {
            return new ThrottlingAppenderWrapper<>(asyncAppender, messageRate.getQuantity(), messageRate.getUnit());
        }
    }

    protected LayoutBase<E> buildLayout(LoggerContext context, LayoutFactory<E> defaultLayoutFactory) {
        final LayoutBase<E> layoutBase;
        if (layout == null) {
            final PatternLayoutBase<E> patternLayoutBase = defaultLayoutFactory.build(context, timeZone);
            if (!Strings.isNullOrEmpty(logFormat)) {
                patternLayoutBase.setPattern(logFormat);
            }
            layoutBase = patternLayoutBase;
        } else {
            layoutBase = layout.build(context, timeZone);
        }

        layoutBase.start();
        return layoutBase;
    }
}
