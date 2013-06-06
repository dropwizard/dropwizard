package com.codahale.dropwizard.logging;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.Layout;
import com.codahale.dropwizard.validation.OneOf;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

import javax.validation.constraints.NotNull;
import java.util.TimeZone;

/**
 * An {@link AppenderFactory} implementation which provides an appender that writes events to the console.
 * <p/>
 * <b>Configuration Parameters:</b>
 * <table>
 *     <tr>
 *         <td>Name</td>
 *         <td>Default</td>
 *         <td>Description</td>
 *     </tr>
 *     <tr>
 *         <td>{@code type}</td>
 *         <td><b>REQUIRED</b></td>
 *         <td>The appender type. Must be {@code console}.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code threshold}</td>
 *         <td>{@code ALL}</td>
 *         <td>The lowest level of events to print to the console.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code timeZone}</td>
 *         <td>{@code UTC}</td>
 *         <td>The time zone to which event timestamps will be converted.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code target}</td>
 *         <td>{@code stdout}</td>
 *         <td>
 *             The name of the standard stream to which events will be written.
 *             Can be {@code stdout} or {@code stderr}.
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>{@code logFormat}</td>
 *         <td>the default format</td>
 *         <td>
 *             The Logback pattern with which events will be formatted. See
 *             <a href="http://logback.qos.ch/manual/layouts.html#conversionWord">the Logback documentation</a>
 *             for details.
 *         </td>
 *     </tr>
 * </table>
 */
@JsonTypeName("console")
public class ConsoleAppenderFactory extends AbstractAppenderFactory {
    @NotNull
    private TimeZone timeZone = TimeZone.getTimeZone("UTC");

    @NotNull
    @OneOf(value = {"stderr", "stdout"}, ignoreCase = true, ignoreWhitespace = true)
    private String target = "stdout";

    /**
     * Returns the time zone to which event timestamps will be converted.
     */
    @JsonProperty
    public TimeZone getTimeZone() {
        return timeZone;
    }

    /**
     * Sets the time zone to which event timestamps will be converted.
     */
    @JsonProperty
    public void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
    }

    /**
     * Returns the name of the standard stream to which events will be written.
     */
    @JsonProperty
    public String getTarget() {
        return target;
    }

    /**
     * Sets the name of the standard stream to which events will be written. Can be {@code stdout} or {@code stderr}.
     */
    @JsonProperty
    public void setTarget(String target) {
        this.target = target;
    }

    @Override
    public Appender<ILoggingEvent> build(LoggerContext context, String applicationName, Layout<ILoggingEvent> layout) {
        final ConsoleAppender<ILoggingEvent> appender = new ConsoleAppender<>();
        appender.setContext(context);
        if ("stderr".equalsIgnoreCase(target)) {
            appender.setTarget("System.err");
        }
        appender.setLayout(layout == null ? buildLayout(context, timeZone) : layout);
        addThresholdFilter(appender, threshold);
        appender.start();

        return appender;
    }
}
