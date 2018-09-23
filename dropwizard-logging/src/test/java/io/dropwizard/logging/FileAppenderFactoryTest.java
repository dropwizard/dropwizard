package io.dropwizard.logging;

import ch.qos.logback.classic.AsyncAppender;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.OutputStreamAppender;
import ch.qos.logback.core.rolling.FixedWindowRollingPolicy;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy;
import ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
import ch.qos.logback.core.util.FileSize;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.configuration.ConfigurationException;
import io.dropwizard.configuration.ConfigurationValidationException;
import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.jackson.DiscoverableSubtypeResolver;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.logging.async.AsyncLoggingEventAppenderFactory;
import io.dropwizard.logging.filter.NullLevelFilterFactory;
import io.dropwizard.logging.layout.DropwizardLayoutFactory;
import io.dropwizard.util.Resources;
import io.dropwizard.util.Size;
import io.dropwizard.validation.BaseValidator;
import io.dropwizard.validation.ConstraintViolations;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.LoggerFactory;

import javax.validation.Validator;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class FileAppenderFactoryTest {

    static {
        BootstrapLogging.bootstrap();
    }

    private final ObjectMapper mapper = Jackson.newObjectMapper();
    private final Validator validator = BaseValidator.newValidator();

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void isDiscoverable() throws Exception {
        assertThat(new DiscoverableSubtypeResolver().getDiscoveredSubtypes())
                .contains(FileAppenderFactory.class);
    }

    @Test
    public void includesCallerData() {
        FileAppenderFactory<ILoggingEvent> fileAppenderFactory = new FileAppenderFactory<>();
        fileAppenderFactory.setArchive(false);
        AsyncAppender asyncAppender = (AsyncAppender) fileAppenderFactory.build(new LoggerContext(), "test", new DropwizardLayoutFactory(), new NullLevelFilterFactory<>(), new AsyncLoggingEventAppenderFactory());
        assertThat(asyncAppender.isIncludeCallerData()).isFalse();

        fileAppenderFactory.setIncludeCallerData(true);
        asyncAppender = (AsyncAppender) fileAppenderFactory.build(new LoggerContext(), "test", new DropwizardLayoutFactory(), new NullLevelFilterFactory<>(), new AsyncLoggingEventAppenderFactory());
        assertThat(asyncAppender.isIncludeCallerData()).isTrue();
    }

    @Test
    public void isRolling() throws Exception {
        // the method we want to test is protected, so we need to override it so we can see it
        FileAppenderFactory<ILoggingEvent> fileAppenderFactory = new FileAppenderFactory<ILoggingEvent>() {
            @Override
            public FileAppender<ILoggingEvent> buildAppender(LoggerContext context) {
                return super.buildAppender(context);
            }
        };

        fileAppenderFactory.setCurrentLogFilename(folder.newFile("logfile.log").toString());
        fileAppenderFactory.setArchive(true);
        fileAppenderFactory.setArchivedLogFilenamePattern(folder.newFile("example-%d.log.gz").toString());
        assertThat(fileAppenderFactory.buildAppender(new LoggerContext())).isInstanceOf(RollingFileAppender.class);
    }

    @Test
    public void hasArchivedLogFilenamePattern() throws Exception {
        FileAppenderFactory<?> fileAppenderFactory = new FileAppenderFactory<>();
        fileAppenderFactory.setCurrentLogFilename(folder.newFile("logfile.log").toString());
        Collection<String> errors =
                ConstraintViolations.format(validator.validate(fileAppenderFactory));
        assertThat(errors)
                .containsOnly("must have archivedLogFilenamePattern if archive is true");
        fileAppenderFactory.setArchive(false);
        errors =
                ConstraintViolations.format(validator.validate(fileAppenderFactory));
        assertThat(errors).isEmpty();
    }

    @Test
    public void isValidForInfiniteRolledFiles() throws Exception {
        FileAppenderFactory<?> fileAppenderFactory = new FileAppenderFactory<>();
        fileAppenderFactory.setCurrentLogFilename(folder.newFile("logfile.log").toString());
        fileAppenderFactory.setArchivedFileCount(0);
        fileAppenderFactory.setArchivedLogFilenamePattern(folder.newFile("example-%d.log.gz").toString());
        Collection<String> errors =
            ConstraintViolations.format(validator.validate(fileAppenderFactory));
        assertThat(errors).isEmpty();
        assertThat(fileAppenderFactory.buildAppender(new LoggerContext())).isNotNull();
    }

    @Test
    public void isValidForMaxFileSize() throws Exception {
        FileAppenderFactory<?> fileAppenderFactory = new FileAppenderFactory<>();
        fileAppenderFactory.setCurrentLogFilename(folder.newFile("logfile.log").toString());
        fileAppenderFactory.setMaxFileSize(Size.kilobytes(1));
        fileAppenderFactory.setArchivedLogFilenamePattern(folder.newFile("example-%d.log.gz").toString());
        Collection<String> errors =
                ConstraintViolations.format(validator.validate(fileAppenderFactory));
        assertThat(errors)
                .containsOnly("when specifying maxFileSize, archivedLogFilenamePattern must contain %i");
        fileAppenderFactory.setArchivedLogFilenamePattern(folder.newFile("example-%d-%i.log.gz").toString());
        errors = ConstraintViolations.format(validator.validate(fileAppenderFactory));
        assertThat(errors).isEmpty();
    }

    @Test
    public void hasMaxFileSizeValidation() throws Exception {
        FileAppenderFactory<?> fileAppenderFactory = new FileAppenderFactory<>();
        fileAppenderFactory.setCurrentLogFilename(folder.newFile("logfile.log").toString());
        fileAppenderFactory.setArchivedLogFilenamePattern(folder.newFile("example-%i.log.gz").toString());
        Collection<String> errors =
                ConstraintViolations.format(validator.validate(fileAppenderFactory));
        assertThat(errors)
                .containsOnly("when archivedLogFilenamePattern contains %i, maxFileSize must be specified");
        fileAppenderFactory.setMaxFileSize(Size.kilobytes(1));
        errors = ConstraintViolations.format(validator.validate(fileAppenderFactory));
        assertThat(errors).isEmpty();
    }

    @Test
    public void testCurrentFileNameErrorWhenArchiveIsNotEnabled() throws Exception {
        FileAppenderFactory<?> fileAppenderFactory = new FileAppenderFactory<>();
        fileAppenderFactory.setArchive(false);
        Collection<String> errors =
                ConstraintViolations.format(validator.validate(fileAppenderFactory));
        assertThat(errors)
                .containsOnly("currentLogFilename can only be null when archiving is enabled");
        fileAppenderFactory.setCurrentLogFilename("test");
        errors = ConstraintViolations.format(validator.validate(fileAppenderFactory));
        assertThat(errors).isEmpty();
    }

    @Test
    public void testCurrentFileNameCanBeNullWhenArchiveIsEnabled() throws Exception {
        FileAppenderFactory<?> fileAppenderFactory = new FileAppenderFactory<>();
        fileAppenderFactory.setArchive(true);
        fileAppenderFactory.setArchivedLogFilenamePattern("name-to-be-used");
        fileAppenderFactory.setCurrentLogFilename(null);
        Collection<String> errors =
                ConstraintViolations.format(validator.validate(fileAppenderFactory));
        assertThat(errors).isEmpty();
    }

    @Test
    public void testCurrentLogFileNameIsEmptyAndAppenderUsesArchivedNameInstead() throws Exception {
        final FileAppenderFactory<ILoggingEvent> appenderFactory = new FileAppenderFactory<>();
        appenderFactory.setArchivedLogFilenamePattern(folder.newFile("test-archived-name-%d.log").toString());
        final FileAppender<ILoggingEvent> rollingAppender = appenderFactory.buildAppender(new LoggerContext());

        final String file = rollingAppender.getFile();
        assertThat(new File(file)).hasName("test-archived-name-" +
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("YYYY-MM-dd")) + ".log");
    }

    @Test
    public void hasMaxFileSize() throws Exception {
        FileAppenderFactory<ILoggingEvent> fileAppenderFactory = new FileAppenderFactory<>();
        fileAppenderFactory.setCurrentLogFilename(folder.newFile("logfile.log").toString());
        fileAppenderFactory.setArchive(true);
        fileAppenderFactory.setMaxFileSize(Size.kilobytes(1));
        fileAppenderFactory.setArchivedLogFilenamePattern(folder.newFile("example-%d-%i.log.gz").toString());
        RollingFileAppender<ILoggingEvent> appender = (RollingFileAppender<ILoggingEvent>) fileAppenderFactory.buildAppender(new LoggerContext());

        assertThat(appender.getTriggeringPolicy()).isInstanceOf(SizeAndTimeBasedRollingPolicy.class);
        final Field maxFileSizeField = SizeAndTimeBasedRollingPolicy.class.getDeclaredField("maxFileSize");
        maxFileSizeField.setAccessible(true);
        final FileSize maxFileSize = (FileSize) maxFileSizeField.get(appender.getRollingPolicy());
        assertThat(maxFileSize.getSize()).isEqualTo(1024L);
    }

    @Test
    public void hasMaxFileSizeFixedWindow() throws Exception {
        FileAppenderFactory<ILoggingEvent> fileAppenderFactory = new FileAppenderFactory<>();
        fileAppenderFactory.setCurrentLogFilename(folder.newFile("logfile.log").toString());
        fileAppenderFactory.setArchive(true);
        fileAppenderFactory.setMaxFileSize(Size.kilobytes(1));
        fileAppenderFactory.setArchivedLogFilenamePattern(folder.newFile("example-%i.log.gz").toString());
        RollingFileAppender<ILoggingEvent> appender = (RollingFileAppender<ILoggingEvent>) fileAppenderFactory.buildAppender(new LoggerContext());

        assertThat(appender.getRollingPolicy()).isInstanceOf(FixedWindowRollingPolicy.class);
        assertThat(appender.getRollingPolicy().isStarted()).isTrue();

        assertThat(appender.getTriggeringPolicy()).isInstanceOf(SizeBasedTriggeringPolicy.class);
        assertThat(appender.getTriggeringPolicy().isStarted()).isTrue();
        final Field maxFileSizeField = SizeBasedTriggeringPolicy.class.getDeclaredField("maxFileSize");
        maxFileSizeField.setAccessible(true);
        final FileSize maxFileSize = (FileSize) maxFileSizeField.get(appender.getTriggeringPolicy());
        assertThat(maxFileSize.getSize()).isEqualTo(1024L);
    }

    @Test
    public void appenderContextIsSet() throws Exception {
        final Logger root = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        final FileAppenderFactory<ILoggingEvent> appenderFactory = new FileAppenderFactory<>();
        appenderFactory.setArchivedLogFilenamePattern(folder.newFile("example-%d.log.gz").toString());
        final Appender<ILoggingEvent> appender = appenderFactory.build(root.getLoggerContext(), "test", new DropwizardLayoutFactory(), new NullLevelFilterFactory<>(), new AsyncLoggingEventAppenderFactory());

        assertThat(appender.getContext()).isEqualTo(root.getLoggerContext());
    }

    @Test
    public void appenderNameIsSet() throws Exception {
        final Logger root = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        final FileAppenderFactory<ILoggingEvent> appenderFactory = new FileAppenderFactory<>();
        appenderFactory.setArchivedLogFilenamePattern(folder.newFile("example-%d.log.gz").toString());
        final Appender<ILoggingEvent> appender = appenderFactory.build(root.getLoggerContext(), "test", new DropwizardLayoutFactory(), new NullLevelFilterFactory<>(), new AsyncLoggingEventAppenderFactory());

        assertThat(appender.getName()).isEqualTo("async-file-appender");
    }

    @Test
    public void isNeverBlock() throws Exception {
        FileAppenderFactory<ILoggingEvent> fileAppenderFactory = new FileAppenderFactory<>();
        fileAppenderFactory.setArchive(false);
        fileAppenderFactory.setNeverBlock(true);
        AsyncAppender asyncAppender = (AsyncAppender) fileAppenderFactory.build(new LoggerContext(), "test", new DropwizardLayoutFactory(), new NullLevelFilterFactory<>(), new AsyncLoggingEventAppenderFactory());

        assertThat(asyncAppender.isNeverBlock()).isTrue();
    }

    @Test
    public void isNotNeverBlock() throws Exception {
        FileAppenderFactory<ILoggingEvent> fileAppenderFactory = new FileAppenderFactory<>();
        fileAppenderFactory.setArchive(false);
        fileAppenderFactory.setNeverBlock(false);
        AsyncAppender asyncAppender = (AsyncAppender) fileAppenderFactory.build(new LoggerContext(), "test", new DropwizardLayoutFactory(), new NullLevelFilterFactory<>(), new AsyncLoggingEventAppenderFactory());

        assertThat(asyncAppender.isNeverBlock()).isFalse();
    }

    @Test
    public void defaultIsNotNeverBlock() throws Exception {
        FileAppenderFactory<ILoggingEvent> fileAppenderFactory = new FileAppenderFactory<>();
        fileAppenderFactory.setArchive(false);
        // default neverBlock
        AsyncAppender asyncAppender = (AsyncAppender) fileAppenderFactory.build(new LoggerContext(), "test", new DropwizardLayoutFactory(), new NullLevelFilterFactory<>(), new AsyncLoggingEventAppenderFactory());

        assertThat(asyncAppender.isNeverBlock()).isFalse();
    }

    @Test
    public void overrideBufferSize() throws NoSuchFieldException, IllegalAccessException {
        FileAppenderFactory<ILoggingEvent> fileAppenderFactory = new FileAppenderFactory<>();
        fileAppenderFactory.setArchive(false);
        fileAppenderFactory.setBufferSize(Size.kilobytes(256));
        AsyncAppender asyncAppender = (AsyncAppender) fileAppenderFactory.build(new LoggerContext(), "test", new DropwizardLayoutFactory(), new NullLevelFilterFactory<>(), new AsyncLoggingEventAppenderFactory());
        final Appender<ILoggingEvent> fileAppender = asyncAppender.getAppender("file-appender");
        assertThat(fileAppender).isInstanceOf(FileAppender.class);
        final Field bufferSizeField = FileAppender.class.getDeclaredField("bufferSize");
        bufferSizeField.setAccessible(true);
        FileSize bufferSizeFromAppender = (FileSize) bufferSizeField.get(fileAppender);
        assertThat(bufferSizeFromAppender.getSize()).isEqualTo(fileAppenderFactory.getBufferSize().toBytes());
    }

    @Test
    public void isImmediateFlushed() throws Exception {
        FileAppenderFactory<ILoggingEvent> fileAppenderFactory = new FileAppenderFactory<>();
        fileAppenderFactory.setArchive(false);

        Field isImmediateFlushField = OutputStreamAppender.class.getDeclaredField("immediateFlush");
        isImmediateFlushField.setAccessible(true);

        fileAppenderFactory.setImmediateFlush(false);
        AsyncAppender asyncAppender = (AsyncAppender) fileAppenderFactory.build(new LoggerContext(), "test", new DropwizardLayoutFactory(), new NullLevelFilterFactory<>(), new AsyncLoggingEventAppenderFactory());
        Appender<ILoggingEvent> fileAppender = asyncAppender.getAppender("file-appender");
        assertThat((Boolean) isImmediateFlushField.get(fileAppender)).isEqualTo(fileAppenderFactory.isImmediateFlush());

        fileAppenderFactory.setImmediateFlush(true);
        asyncAppender = (AsyncAppender) fileAppenderFactory.build(new LoggerContext(), "test", new DropwizardLayoutFactory(), new NullLevelFilterFactory<>(), new AsyncLoggingEventAppenderFactory());
        fileAppender = asyncAppender.getAppender("file-appender");
        assertThat((Boolean) isImmediateFlushField.get(fileAppender)).isEqualTo(fileAppenderFactory.isImmediateFlush());
    }

    @Test
    public void validSetTotalSizeCap() throws IOException, ConfigurationException, NoSuchFieldException {
        final Field totalSizeCap = TimeBasedRollingPolicy.class.getDeclaredField("totalSizeCap");
        totalSizeCap.setAccessible(true);

        final Field maxFileSize = SizeAndTimeBasedRollingPolicy.class.getDeclaredField("maxFileSize");
        maxFileSize.setAccessible(true);

        final YamlConfigurationFactory<FileAppenderFactory> factory =
            new YamlConfigurationFactory<>(FileAppenderFactory.class, validator, mapper, "dw");

        final FileAppenderFactory appenderFactory = factory.build(new File(Resources.getResource("yaml/appender_file_cap.yaml").getFile()));
        final FileAppender appender = appenderFactory.buildAppender(new LoggerContext());
        assertThat(appender).isInstanceOfSatisfying(RollingFileAppender.class, roller -> {
            assertThat(roller.getRollingPolicy()).isInstanceOfSatisfying(SizeAndTimeBasedRollingPolicy.class, policy -> {
                try {
                    assertThat(totalSizeCap.get(policy))
                        .isInstanceOfSatisfying(FileSize.class, x ->
                            assertThat(x.getSize()).isEqualTo(Size.megabytes(50).toBytes()));

                    assertThat(maxFileSize.get(policy))
                        .isInstanceOfSatisfying(FileSize.class, x ->
                            assertThat(x.getSize()).isEqualTo(Size.megabytes(10).toBytes()));

                    assertThat(policy.getMaxHistory()).isEqualTo(5);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Unexpected illegal access", e);
                }
            });
        });
    }

    @Test
    public void validSetTotalSizeCapNoMaxFileSize() throws IOException, ConfigurationException, NoSuchFieldException {
        final Field totalSizeCap = TimeBasedRollingPolicy.class.getDeclaredField("totalSizeCap");
        totalSizeCap.setAccessible(true);

        final YamlConfigurationFactory<FileAppenderFactory> factory =
            new YamlConfigurationFactory<>(FileAppenderFactory.class, validator, mapper, "dw");

        final FileAppenderFactory appenderFactory = factory.build(new File(Resources.getResource("yaml/appender_file_cap2.yaml").getFile()));
        final FileAppender appender = appenderFactory.buildAppender(new LoggerContext());
        assertThat(appender).isInstanceOfSatisfying(RollingFileAppender.class, roller -> {
            assertThat(roller.getRollingPolicy()).isInstanceOfSatisfying(TimeBasedRollingPolicy.class, policy -> {
                try {
                    assertThat(totalSizeCap.get(policy))
                        .isInstanceOfSatisfying(FileSize.class, x ->
                            assertThat(x.getSize()).isEqualTo(Size.megabytes(50).toBytes()));

                    assertThat(policy.getMaxHistory()).isEqualTo(5);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Unexpected illegal access", e);
                }
            });
        });
    }

    @Test
    public void invalidUseOfTotalSizeCap() {
        final YamlConfigurationFactory<FileAppenderFactory> factory =
            new YamlConfigurationFactory<>(FileAppenderFactory.class, validator, mapper, "dw");
        assertThatThrownBy(() ->
            factory.build(new File(Resources.getResource("yaml/appender_file_cap_invalid.yaml").getFile()))
        ).isExactlyInstanceOf(ConfigurationValidationException.class)
            .hasMessageContaining("totalSizeCap has no effect when using maxFileSize and an archivedLogFilenamePattern " +
                "without %d, as archivedFileCount implicitly controls the total size cap");
    }
}
