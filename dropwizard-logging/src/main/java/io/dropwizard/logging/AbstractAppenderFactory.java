package io.dropwizard.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.filter.ThresholdFilter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.spi.FilterAttachable;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import io.dropwizard.util.Duration;
import io.dropwizard.validation.MinDuration;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
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
 *         <td>{@code bounded}</td>
 *         <td>true</td>
 *         <td>Whether or not the appender should block when its queue is full.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code batchSize}</td>
 *         <td>128</td>
 *         <td>
 *             The maximum number of events to write in a single batch.
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>{@code batchDuration}</td>
 *         <td>100ms</td>
 *         <td>
 *             The maximum amount of time to wait for a full batch before writing a partial batch.
 *         </td>
 *     </tr>
 * </table>
 */
public abstract class AbstractAppenderFactory implements AppenderFactory {
    private boolean bounded;

    @NotNull
    protected Level threshold = Level.ALL;

    protected String logFormat;

    @Min(1)
    @Max(Integer.MAX_VALUE)
    private int batchSize = 128;

    @NotNull
    @MinDuration(value = 1, unit = TimeUnit.MILLISECONDS)
    private Duration batchDuration = Duration.milliseconds(100);

    @JsonProperty
    public boolean isBounded() {
        return bounded;
    }

    @JsonProperty
    public void setBounded(boolean bounded) {
        this.bounded = bounded;
    }

    @JsonProperty
    public int getBatchSize() {
        return batchSize;
    }

    @JsonProperty
    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    @JsonProperty
    public Duration getBatchDuration() {
        return batchDuration;
    }

    @JsonProperty
    public void setBatchDuration(Duration batchDuration) {
        this.batchDuration = batchDuration;
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

    protected Appender<ILoggingEvent> wrapAsync(Appender<ILoggingEvent> appender) {
        final AsyncAppender asyncAppender = new AsyncAppender(appender, batchSize, batchDuration, bounded);
        asyncAppender.start();
        return asyncAppender;
    }

    protected void addThresholdFilter(FilterAttachable<ILoggingEvent> appender, Level threshold) {
        final ThresholdFilter filter = new ThresholdFilter();
        filter.setLevel(threshold.toString());
        filter.start();
        appender.addFilter(filter);
    }

    protected DropwizardLayout buildLayout(LoggerContext context, TimeZone timeZone) {
        final DropwizardLayout formatter = new DropwizardLayout(context, timeZone);
        if (!Strings.isNullOrEmpty(logFormat)) {
            formatter.setPattern(logFormat);
        }
        formatter.start();
        return formatter;
    }
}
