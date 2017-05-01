package io.dropwizard.logging;


import io.dropwizard.logging.AbstractAppenderFactory;
import io.dropwizard.logging.AppenderFactory;
import io.dropwizard.logging.FileAppenderFactory;

import javax.validation.constraints.NotNull;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.rolling.FixedWindowRollingPolicy;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * An {@link AppenderFactory} implementation and extention of {@ling FileAppenderFactory} 
 * which provides an appender that writes events to a file, archiving files 
 * bigger as {@maxFileSize}.
 * <p/>
 * <b>Configuration Parameters:</b>
 * <table>
 *     <tr>
 *         <td>Name</td>
 *         <td>Default</td>
 *         <td>Description</td>
 *     </tr>
 *     <tr>
 *         <td>{@code maxFileSize}</td>
 *         <td><b>{@code 100MB}</b></td>
 *         <td>The max file size of the logfile untill rotation.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code archivedLogFilenamePattern}</td>
 *         <td><b>REQUIRED</b> if {@code archive} is {@code true}.</td>
 *         <td>
 *             The filename pattern for archived files. {@code %i} is replaced with an ongoing number,
 *             and the fact that it ends with {@code .gz} indicates the file will be gzipped as it's archived. Likewise,
 *             filename patterns which end in {@code .zip} will be filled as they are archived.
 *         </td>
 *     </tr>     
 * </table>
 *
 * @see FileAppenderFactory
 * 
 */
@JsonTypeName("file-size-rolled")
public class SizeBasedRollingFileAppenderFactory extends FileAppenderFactory {
    public static final String DEFAULT_MAX_FILE_SIZE_STR = "100MB";
    public static final String DEFAULT_FILE_LOG_FORMAT = "%date %-5level[%t %mdc] %logger: %msg%n%rEx";

    @NotNull
    @JsonProperty
    String maxFileSize = DEFAULT_MAX_FILE_SIZE_STR;

    public SizeBasedRollingFileAppenderFactory() {
	setLogFormat(DEFAULT_FILE_LOG_FORMAT);
    }

    @JsonProperty
    public String getMaxFileSize() {
	return maxFileSize;
    }

    @JsonProperty
    public void setMaxFileSize(String maxFileSize) {
	this.maxFileSize = maxFileSize;
    }


    @Override
    protected FileAppender<ILoggingEvent> buildAppender(LoggerContext context) {
	if (isArchive()) {
	    final RollingFileAppender<ILoggingEvent> appender = new RollingFileAppender<>();
	    appender.setFile(getCurrentLogFilename());

	    final FixedWindowRollingPolicy rollingPolicy = new FixedWindowRollingPolicy();
	    rollingPolicy.setContext(context);
	    rollingPolicy.setFileNamePattern(getArchivedLogFilenamePattern());
	    rollingPolicy.setMinIndex(0);
	    rollingPolicy.setMaxIndex(getArchivedFileCount() - 1);
	    rollingPolicy.setParent(appender);
	    rollingPolicy.start();
	    appender.setRollingPolicy(rollingPolicy);

	    final SizeBasedTriggeringPolicy<ILoggingEvent> triggeringPolicy = new SizeBasedTriggeringPolicy<>(getMaxFileSize());
	    triggeringPolicy.setContext(context);
	    triggeringPolicy.start();
	    appender.setTriggeringPolicy(triggeringPolicy);

	    return appender;
	}
	return new FileAppender<>();
    }


}
