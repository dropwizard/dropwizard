package io.dropwizard.logging.common;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.OutputStreamAppender;
import ch.qos.logback.core.rolling.DefaultTimeBasedFileNamingAndTriggeringPolicy;
import ch.qos.logback.core.rolling.FixedWindowRollingPolicy;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy;
import ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy;
import ch.qos.logback.core.rolling.TimeBasedFileNamingAndTriggeringPolicy;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
import ch.qos.logback.core.spi.DeferredProcessingAware;
import ch.qos.logback.core.util.FileSize;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.dropwizard.util.DataSize;
import io.dropwizard.validation.MinDataSize;
import io.dropwizard.validation.ValidationMethod;
import jakarta.validation.constraints.Min;
import org.checkerframework.checker.nullness.qual.Nullable;

import static java.util.Objects.requireNonNull;

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
 *             The filename pattern for archived files.
 *             If {@code maxFileSize} is specified, rollover is size-based, and the pattern must contain {@code %i} for
 *             an integer index of the archived file.
 *             Otherwise rollover is date-based, and the pattern must contain {@code %d}, which is replaced with the
 *             date in {@code yyyy-MM-dd} form.
 *             If the pattern ends with {@code .gz} or {@code .zip}, files will be compressed as they are archived.
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>{@code archivedFileCount}</td>
 *         <td>{@code 5}</td>
 *         <td>
 *             The number of archived files to keep. Must be greater than or equal to {@code 0}. Zero is a
 *             special value signifying to keep infinite logs (use with caution)
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>{@code maxFileSize}</td>
 *         <td>(unlimited)</td>
 *         <td>
 *             The maximum size of the currently active file before a rollover is triggered. The value can be expressed
 *             with SI and IEC prefixes, see {@link io.dropwizard.util.DataSizeUnit}.
 *             Examples include 100MiB, 1GiB, 1TiB.  Sizes can also be spelled out, such as 100 mebibytes,
 *             1 gibibyte, 1 tebibyte.
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>{@code totalSizeCap}</td>
 *         <td>(unlimited)</td>
 *         <td>
 *             Controls the total size of all files. Oldest archives are deleted asynchronously when the total
 *             size cap is exceeded.
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>{@code timeZone}</td>
 *         <td>{@code UTC}</td>
 *         <td>The time zone to which event timestamps will be converted.</td>
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
 *     <tr>
 *         <td>{@code bufferSize}</td>
 *         <td>8KiB</td>
 *         <td>
 *             The buffer size of the underlying FileAppender (setting added in logback 1.1.10). Increasing this from
 *             the default of 8KiB to 256KiB is reported to significantly reduce thread contention.
 *         </td>
 *     </tr>
 *      <tr>
 *         <td>{@code immediateFlush}</td>
 *         <td>{@code true}</td>
 *         <td>
 *             If set to true, log events will be immediately flushed to disk. Immediate flushing is safer, but
 *             it degrades logging throughput.
 *             See <a href="https://logback.qos.ch/manual/appenders.html#immediateFlush">the Logback documentation</a>
 *             for details.
 *         </td>
 *     </tr>
 * </table>
 *
 * @see AbstractAppenderFactory
 */
@JsonTypeName("file")
public class FileAppenderFactory<E extends DeferredProcessingAware> extends AbstractOutputStreamAppenderFactory<E> {

    @Nullable
    private String currentLogFilename;

    private boolean archive = true;

    @Nullable
    private String archivedLogFilenamePattern;

    @Min(0)
    private int archivedFileCount = 5;

    @Nullable
    private DataSize maxFileSize;

    @Nullable
    private DataSize totalSizeCap;

    @MinDataSize(1)
    private DataSize bufferSize = DataSize.bytes(FileAppender.DEFAULT_BUFFER_SIZE);

    private boolean immediateFlush = true;

    @JsonProperty
    @Nullable
    public String getCurrentLogFilename() {
        return currentLogFilename;
    }

    @JsonProperty
    public void setCurrentLogFilename(@Nullable String currentLogFilename) {
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
    @Nullable
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
    @Nullable
    public DataSize getMaxFileSize() {
        return maxFileSize;
    }

    @JsonProperty
    public void setMaxFileSize(@Nullable DataSize maxFileSize) {
        this.maxFileSize = maxFileSize;
    }

    /**
     * Returns the total size threshold at which archived log files will be
     * removed. A zero value means there is limit.
     * @since 2.0
     */
    @JsonProperty
    @Nullable
    public DataSize getTotalSizeCap() {
        return totalSizeCap;
    }

    /**
     * Sets the total size threshold at which archived log files will be
     * removed. A zero value means there is limit.
     * @since 2.0
     */
    @JsonProperty
    public void setTotalSizeCap(@Nullable DataSize totalSizeCap) {
        this.totalSizeCap = totalSizeCap;
    }

    @JsonProperty
    public DataSize getBufferSize() {
        return bufferSize;
    }

    @JsonProperty
    public void setBufferSize(DataSize bufferSize) {
        this.bufferSize = bufferSize;
    }

    public boolean isImmediateFlush() {
        return immediateFlush;
    }

    @JsonProperty
    public void setImmediateFlush(boolean immediateFlush) {
        this.immediateFlush = immediateFlush;
    }

    /**
     * Returns a boolean indicating whether the {@code totalSizeCap} property
     * will be used.
     * @since 2.0
     */
    @JsonIgnore
    @ValidationMethod(message = "totalSizeCap has no effect when using maxFileSize and an archivedLogFilenamePattern without %d, as archivedFileCount implicitly controls the total size cap")
    public boolean isTotalSizeCapValid() {
        return !archive || totalSizeCap == null ||
            !(maxFileSize != null && !requireNonNull(archivedLogFilenamePattern).contains("%d"));
    }

    @JsonIgnore
    @ValidationMethod(message = "must have archivedLogFilenamePattern if archive is true")
    public boolean isValidArchiveConfiguration() {
        return !archive || (archivedLogFilenamePattern != null);
    }

    @JsonIgnore
    @ValidationMethod(message = "when specifying maxFileSize, archivedLogFilenamePattern must contain %i")
    public boolean isValidForMaxFileSizeSetting() {
        return !archive || maxFileSize == null ||
                (archivedLogFilenamePattern != null && archivedLogFilenamePattern.contains("%i"));
    }

    @JsonIgnore
    @ValidationMethod(message = "when archivedLogFilenamePattern contains %i, maxFileSize must be specified")
    public boolean isMaxFileSizeSettingSpecified() {
        return !archive || !(archivedLogFilenamePattern != null && archivedLogFilenamePattern.contains("%i")) ||
                maxFileSize != null;
    }

    @JsonIgnore
    @ValidationMethod(message = "currentLogFilename can only be null when archiving is enabled")
    public boolean isValidFileConfiguration() {
        return archive || currentLogFilename != null;
    }

    @Override
    protected OutputStreamAppender<E> appender(LoggerContext context) {
        final FileAppender<E> appender = buildAppender(context);
        appender.setName("file-appender");
        appender.setAppend(true);
        appender.setContext(context);
        appender.setImmediateFlush(immediateFlush);
        appender.setPrudent(false);
        return appender;
    }

    protected FileAppender<E> buildAppender(LoggerContext context) {
        if (archive) {
            final RollingFileAppender<E> appender = new RollingFileAppender<>();
            appender.setContext(context);
            appender.setFile(currentLogFilename);
            appender.setBufferSize(new FileSize(bufferSize.toBytes()));

            if (maxFileSize != null && !requireNonNull(archivedLogFilenamePattern).contains("%d")) {
                final FixedWindowRollingPolicy rollingPolicy = new FixedWindowRollingPolicy();
                rollingPolicy.setContext(context);
                rollingPolicy.setMaxIndex(getArchivedFileCount());
                rollingPolicy.setFileNamePattern(getArchivedLogFilenamePattern());
                rollingPolicy.setParent(appender);
                rollingPolicy.start();
                appender.setRollingPolicy(rollingPolicy);

                final SizeBasedTriggeringPolicy<E> triggeringPolicy = new SizeBasedTriggeringPolicy<>();
                triggeringPolicy.setMaxFileSize(new FileSize(maxFileSize.toBytes()));
                triggeringPolicy.setContext(context);
                triggeringPolicy.start();
                appender.setTriggeringPolicy(triggeringPolicy);

                return appender;
            } else {
                final TimeBasedRollingPolicy<E> rollingPolicy;
                if (maxFileSize == null) {
                    rollingPolicy = new TimeBasedRollingPolicy<>();

                    final TimeBasedFileNamingAndTriggeringPolicy<E> triggeringPolicy = new DefaultTimeBasedFileNamingAndTriggeringPolicy<>();
                    triggeringPolicy.setContext(context);
                    triggeringPolicy.setTimeBasedRollingPolicy(rollingPolicy);
                    appender.setTriggeringPolicy(triggeringPolicy);
                } else {
                    // Creating a size and time policy does not need a separate triggering policy set
                    // on the appender because this policy registers the trigger policy
                    final SizeAndTimeBasedRollingPolicy<E> sizeAndTimeBasedRollingPolicy = new SizeAndTimeBasedRollingPolicy<>();
                    sizeAndTimeBasedRollingPolicy.setMaxFileSize(new FileSize(maxFileSize.toBytes()));
                    rollingPolicy = sizeAndTimeBasedRollingPolicy;
                }

                if (totalSizeCap != null) {
                    rollingPolicy.setTotalSizeCap(new FileSize(totalSizeCap.toBytes()));
                }

                rollingPolicy.setContext(context);
                rollingPolicy.setFileNamePattern(archivedLogFilenamePattern);
                rollingPolicy.setMaxHistory(archivedFileCount);

                appender.setRollingPolicy(rollingPolicy);

                rollingPolicy.setParent(appender);
                rollingPolicy.start();
                return appender;
            }
        }

        final FileAppender<E> appender = new FileAppender<>();
        appender.setContext(context);
        appender.setFile(currentLogFilename);
        appender.setBufferSize(new FileSize(bufferSize.toBytes()));
        return appender;
    }
}
