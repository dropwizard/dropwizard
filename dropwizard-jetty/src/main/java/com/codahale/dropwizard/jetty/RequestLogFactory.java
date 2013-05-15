package com.codahale.dropwizard.jetty;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.LayoutBase;
import ch.qos.logback.core.spi.AppenderAttachableImpl;
import com.codahale.dropwizard.logging.AppenderFactory;
import com.codahale.dropwizard.logging.ConsoleAppenderFactory;
import com.codahale.dropwizard.util.Duration;
import com.codahale.dropwizard.validation.MinDuration;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import org.eclipse.jetty.server.RequestLog;
import org.slf4j.LoggerFactory;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * A factory for creating {@link RequestLog} instances.
 * <p/>
 * <b>Configuration Parameters:</b>
 * <table>
 *     <tr>
 *         <td>Name</td>
 *         <td>Default</td>
 *         <td>Description</td>
 *     </tr>
 *     <tr>
 *         <td>{@code timeZone}</td>
 *         <td>UTC</td>
 *         <td>The time zone to which request timestamps will be converted.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code batchSize}</td>
 *         <td>10,000</td>
 *         <td>
 *             The maximum number of requests to write in a single batch.
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>{@code batchDuration}</td>
 *         <td>100ms</td>
 *         <td>
 *             The maximum amount of time to wait for a full batch before writing a partial batch.
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>{@code appenders}</td>
 *         <td>a default {@link ConsoleAppenderFactory console} appender</td>
 *         <td>
 *             The set of {@link AppenderFactory appenders} to which requests will be logged.
 *         </td>
 *     </tr>
 * </table>
 */
public class RequestLogFactory {
    private static class RequestLogLayout extends LayoutBase<ILoggingEvent> {
        @Override
        public String doLayout(ILoggingEvent event) {
            return event.getFormattedMessage() + CoreConstants.LINE_SEPARATOR;
        }
    }

    @NotNull
    private TimeZone timeZone = TimeZone.getTimeZone("UTC");

    @Min(1)
    @Max(Integer.MAX_VALUE)
    private int batchSize = 10_000;

    @NotNull
    @MinDuration(value = 1, unit = TimeUnit.MILLISECONDS)
    private Duration batchDuration = Duration.milliseconds(100);

    @Valid
    @NotNull
    private ImmutableList<AppenderFactory> appenders = ImmutableList.<AppenderFactory>of(
            new ConsoleAppenderFactory()
    );

    @JsonProperty
    public ImmutableList<AppenderFactory> getAppenders() {
        return appenders;
    }

    @JsonProperty
    public void setAppenders(ImmutableList<AppenderFactory> appenders) {
        this.appenders = appenders;
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

    @JsonIgnore
    public boolean isEnabled() {
        return !appenders.isEmpty();
    }

    public RequestLog build(String name) {
        final Logger logger = (Logger) LoggerFactory.getLogger("http.request");
        logger.setAdditive(false);

        final LoggerContext context = logger.getLoggerContext();

        final RequestLogLayout layout = new RequestLogLayout();
        layout.start();

        final AppenderAttachableImpl<ILoggingEvent> attachable = new AppenderAttachableImpl<>();
        for (AppenderFactory output : this.appenders) {
            attachable.addAppender(output.build(context, name, layout));
        }

        return new AsyncRequestLog(attachable, timeZone, batchSize, batchDuration);
    }
}
