package com.codahale.dropwizard.logging;

import ch.qos.logback.access.spi.IAccessEvent;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.Layout;
import ch.qos.logback.core.encoder.Encoder;
import ch.qos.logback.core.rolling.DefaultTimeBasedFileNamingAndTriggeringPolicy;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
import ch.qos.logback.core.spi.DeferredProcessingAware;
import com.codahale.dropwizard.validation.ValidationMethod;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.TimeZone;

/**
 * An {@link AppenderFactory} implementation which provides an appender that writes events to a file, archiving older
 * files as it goes.
 * <p/>
 * <b>Configuration Parameters:</b>
 * <table>
 * <tr>
 * <td>Name</td>
 * <td>Default</td>
 * <td>Description</td>
 * </tr>
 * <tr>
 * <td>{@code type}</td>
 * <td><b>REQUIRED</b></td>
 * <td>The appender type. Must be {@code file}.</td>
 * </tr>
 * <tr>
 * <td>{@code threshold}</td>
 * <td>{@code ALL}</td>
 * <td>The lowest level of events to write to the file.</td>
 * </tr>
 * <tr>
 * <td>{@code currentLogFilename}</td>
 * <td><b>REQUIRED</b></td>
 * <td>The filename where current events are logged.</td>
 * </tr>
 * <tr>
 * <td>{@code archive}</td>
 * <td>{@code true}</td>
 * <td>Whether or not to archive old events in separate files.</td>
 * </tr>
 * <tr>
 * <td>{@code archivedLogFilenamePattern}</td>
 * <td><b>REQUIRED</b> if {@code archive} is {@code true}.</td>
 * <td>
 * The filename pattern for archived files. {@code %d} is replaced with the date in {@code yyyy-MM-dd} form,
 * and the fact that it ends with {@code .gz} indicates the file will be gzipped as it's archived. Likewise,
 * filename patterns which end in {@code .zip} will be filled as they are archived.
 * </td>
 * </tr>
 * <tr>
 * <td>{@code archivedFileCount}</td>
 * <td>{@code 5}</td>
 * <td>
 * The number of archived files to keep. Must be between {@code 1} and {@code 50}.
 * </td>
 * </tr>
 * <tr>
 * <td>{@code timeZone}</td>
 * <td>{@code UTC}</td>
 * <td>The time zone to which event timestamps will be converted.</td>
 * </tr>
 * <tr>
 * <td>{@code logFormat}</td>
 * <td>the default format</td>
 * <td>
 * The Logback pattern with which events will be formatted. See
 * <a href="http://logback.qos.ch/manual/layouts.html#conversionWord">the Logback documentation</a>
 * for details.
 * </td>
 * </tr>
 * </table>
 *
 * @see AbstractAppenderFactory
 */
@JsonTypeName("file")
public class FileAppenderFactory extends AbstractAppenderFactory implements AccessAppenderFactory {
    @NotNull
    private String currentLogFilename;

    private boolean archive = true;

    private String archivedLogFilenamePattern;

    @Min(1)
    @Max(50)
    private int archivedFileCount = 5;

    @NotNull
    private TimeZone timeZone = TimeZone.getTimeZone("UTC");

    @JsonProperty
    public String getCurrentLogFilename() {
        return currentLogFilename;
    }

    @JsonProperty
    public void setCurrentLogFilename(String currentLogFilename) {
        this.currentLogFilename = currentLogFilename;
    }

    @JsonProperty
    public boolean isArchive() {
        return archive;
    }

    @JsonProperty
    public void setArchive(boolean archive) {
        this.archive = archive;
    }

    @JsonProperty
    public String getArchivedLogFilenamePattern() {
        return archivedLogFilenamePattern;
    }

    @JsonProperty
    public void setArchivedLogFilenamePattern(String archivedLogFilenamePattern) {
        this.archivedLogFilenamePattern = archivedLogFilenamePattern;
    }

    @JsonProperty
    public int getArchivedFileCount() {
        return archivedFileCount;
    }

    @JsonProperty
    public void setArchivedFileCount(int archivedFileCount) {
        this.archivedFileCount = archivedFileCount;
    }

    @JsonProperty
    public TimeZone getTimeZone() {
        return timeZone;
    }

    @JsonProperty
    public void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
    }

    @JsonIgnore
    @ValidationMethod(message = "must have archivedLogFilenamePattern if archive is true")
    public boolean isValidArchiveConfiguration() {
        return !archive || (archivedLogFilenamePattern != null);
    }

    @Override
    public Appender<ILoggingEvent> build(LoggerContext context, String applicationName, Layout<ILoggingEvent> layout) {
        final FileAppender<ILoggingEvent> appender = buildAndSetupAppender(context);
        appender.setLayout(layout == null ? buildLayout(context, timeZone) : layout);
        addThresholdFilter(appender, threshold);
        appender.stop();
        appender.start();

        return wrapAsync(appender);
    }

    private <E extends DeferredProcessingAware> FileAppender<E> buildAndSetupAppender(LoggerContext context) {
        FileAppender<E> appender = new FileAppender<>();
        if(archive) {
            appender = buildRollingAppender(context);
        }
        appender.setName("file-appender");
        appender.setAppend(true);
        appender.setContext(context);
        appender.setFile(currentLogFilename);
        appender.setPrudent(false);
        return appender;
    }

    private <E extends DeferredProcessingAware> FileAppender<E> buildRollingAppender(LoggerContext context) {

        final RollingFileAppender<E> appender = new RollingFileAppender<>();
        final DefaultTimeBasedFileNamingAndTriggeringPolicy<E> triggeringPolicy =
                new DefaultTimeBasedFileNamingAndTriggeringPolicy<>();
        triggeringPolicy.setContext(context);

        final TimeBasedRollingPolicy<E> rollingPolicy = new TimeBasedRollingPolicy<>();
        rollingPolicy.setContext(context);
        rollingPolicy.setFileNamePattern(archivedLogFilenamePattern);
        rollingPolicy.setTimeBasedFileNamingAndTriggeringPolicy(
                triggeringPolicy);
        triggeringPolicy.setTimeBasedRollingPolicy(rollingPolicy);
        rollingPolicy.setMaxHistory(archivedFileCount);

        appender.setRollingPolicy(rollingPolicy);
        appender.setTriggeringPolicy(triggeringPolicy);

        rollingPolicy.setParent(appender);
        rollingPolicy.start();

        return appender;
    }

    @Override
    public Appender<IAccessEvent> build(LoggerContext context, Encoder<IAccessEvent> encoder) {
        final FileAppender<IAccessEvent> appender = buildAndSetupAppender(context);
        appender.setEncoder(encoder);
        appender.stop();
        appender.start();

        return wrapAsync(appender);
    }
}
