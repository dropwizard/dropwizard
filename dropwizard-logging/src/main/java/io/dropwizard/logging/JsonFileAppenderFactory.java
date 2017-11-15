package io.dropwizard.logging;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.encoder.LayoutWrappingEncoder;
import ch.qos.logback.core.spi.DeferredProcessingAware;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.dropwizard.logging.async.AsyncAppenderFactory;
import io.dropwizard.logging.filter.LevelFilterFactory;
import io.dropwizard.logging.layout.LayoutFactory;


/**
 * An {@link AppenderFactory} implementation which provides an appender that writes events to a file, archiving older
 * files as it goes.
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
 *         <td>The appender type. Must be {@code file}.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code threshold}</td>
 *         <td>{@code ALL}</td>
 *         <td>The lowest level of events to write to the file.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code currentLogFilename}</td>
 *         <td><b>REQUIRED</b></td>
 *         <td>The filename where current events are logged.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code archive}</td>
 *         <td>{@code true}</td>
 *         <td>Whether or not to archive old events in separate files.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code archivedLogFilenamePattern}</td>
 *         <td><b>REQUIRED</b> if {@code archive} is {@code true}.</td>
 *         <td>
 *             The filename pattern for archived files. {@code %d} is replaced with the date in {@code yyyy-MM-dd} form,
 *             and the fact that it ends with {@code .gz} indicates the file will be gzipped as it's archived. Likewise,
 *             filename patterns which end in {@code .zip} will be filled as they are archived.
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>{@code archivedFileCount}</td>
 *         <td>{@code 5}</td>
 *         <td>
 *             The number of archived files to keep. Must be greater than {@code 0}.
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>{@code maxFileSize}</td>
 *         <td>(unlimited)</td>
 *         <td>
 *             The maximum size of the currently active file before a rollover is triggered. The value can be expressed
 *             in bytes, kilobytes, megabytes, gigabytes, and terabytes by appending B, K, MB, GB, or TB to the
 *             numeric value.  Examples include 100MB, 1GB, 1TB.  Sizes can also be spelled out, such as 100 megabytes,
 *             1 gigabyte, 1 terabyte.
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>{@code timeZone}</td>
 *         <td>{@code UTC}</td>
 *         <td>The time zone to which event timestamps will be converted.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code timestampFormat}</td>
 *         <td>{@code None}</td>
 *         <td>The formatter string to use to format timestamps. Defaults to the time elapsed since unix epoch</td>
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
 #     <tr>
 *         <td>{@code prettyPrint}</td>
 *         <td>{@code false}</td>
 *         <td>Whether jackson json printing should beautify the output for human readability</td>
 *     </tr>
 *     <tr>
 *         <td>{@code includeStackTrace}</td>
 *         <td>true</td>
 *         <td>
 *             whether to include the stacktrace along with the exception and other details
 *         </td>
 *     </tr>
 * </table>
 *
 * @see AbstractAppenderFactory
 */
@JsonTypeName("jsonFile")
public class JsonFileAppenderFactory<E extends DeferredProcessingAware> extends FileAppenderFactory<E> {


    private boolean prettyPrint = false;

    private boolean includeStackTrace = true;

    private String timestampFormat = null;

    @JsonProperty
    public String getTimestampFormat() {
        return timestampFormat;
    }

    @JsonProperty
    public void setTimestampFormat(String timestampFormat) {
        this.timestampFormat = timestampFormat;
    }

    @JsonProperty
    public boolean isPrettyPrint() {
        return prettyPrint;
    }

    @JsonProperty
    public void setPrettyPrint(boolean prettyPrint) {
        this.prettyPrint = prettyPrint;
    }

    @JsonProperty
    public boolean isIncludeStackTrace() {
        return includeStackTrace;
    }

    @JsonProperty
    public void setIncludeStackTrace(boolean includeStackTrace) {
        this.includeStackTrace = includeStackTrace;
    }



    @Override
    public Appender<E> build(LoggerContext context, String applicationName, LayoutFactory<E> layoutFactory,
                             LevelFilterFactory<E> levelFilterFactory, AsyncAppenderFactory<E> asyncAppenderFactory) {
        final FileAppender<E> appender = super.buildAppender(context);
        appender.setName("jsonFile-appender");

        appender.setAppend(true);
        appender.setContext(context);
        appender.setImmediateFlush(true);
        final LayoutWrappingEncoder<E> layoutEncoder = new LayoutWrappingEncoder<>();

        layoutEncoder.setLayout(layoutFactory.buildJsonLayout(context, timeZone, getTimestampFormat(),
                                                              includeStackTrace, prettyPrint));
        appender.setEncoder(layoutEncoder);

        appender.setPrudent(false);
        appender.addFilter(levelFilterFactory.build(threshold));
        getFilterFactories().stream().forEach(f -> appender.addFilter(f.build()));
        appender.start();

        return wrapAsync(appender, asyncAppenderFactory);
    }


}
