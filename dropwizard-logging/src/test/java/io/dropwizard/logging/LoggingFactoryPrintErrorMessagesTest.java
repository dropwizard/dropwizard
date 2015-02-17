package io.dropwizard.logging;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.util.StatusPrinter;
import com.codahale.metrics.MetricRegistry;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

public class LoggingFactoryPrintErrorMessagesTest {

    @Rule
    public final TemporaryFolder tempDir = new TemporaryFolder();

    File folderWithoutWritePermission;
    File folderWithWritePermission;

    LoggingFactory factory;
    LoggerContext loggerContext;

    @Before
    public void setUp() throws Exception {
        folderWithoutWritePermission = tempDir.newFolder("folder-without-write-permission");
        folderWithoutWritePermission.setWritable(false);

        folderWithWritePermission = tempDir.newFolder("folder-with-write-permission");

        loggerContext = new LoggerContext();
        factory = new LoggingFactory(loggerContext);
    }

    @After
    public void tearDown() throws Exception {
        loggerContext.stop();
    }

    private void configureLoggingFactoryWithFileAppender(File file){
        factory.setAppenders(singletonList(newFileAppenderFactory(file)));
    }

    private AppenderFactory newFileAppenderFactory(File file){
        FileAppenderFactory fileAppenderFactory = new FileAppenderFactory();

        fileAppenderFactory.setCurrentLogFilename(file.toString() + File.separator + "my-log-file.log");
        fileAppenderFactory.setArchive(false);

        return fileAppenderFactory;
    }

    private String configureAndCaptureSystemOut() throws UnsupportedEncodingException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            StatusPrinter.setPrintStream(new PrintStream(out));
            factory.configure(new MetricRegistry(), "logger-test");
        }finally {
            StatusPrinter.setPrintStream(System.out);
        }

        return out.toString(StandardCharsets.UTF_8.name());
    }

    @Test
    public void testWhenFileAppenderDoesNotHaveWritePermissionToFolder_PrintsErrorMessageToConsole() throws Exception {
        File file = folderWithoutWritePermission;

        configureLoggingFactoryWithFileAppender(file);

        assertThat(file.canWrite()).isFalse();
        assertThat(configureAndCaptureSystemOut()).contains(file.toString());
    }

    @Test
    public void testWhenSettingUpLoggingWithValidConfiguration_NoErrorMessageIsPrintedToConsole() throws Exception {
        File file = folderWithWritePermission;

        configureLoggingFactoryWithFileAppender(file);

        assertThat(file.canWrite()).isTrue();
        assertThat(configureAndCaptureSystemOut()).isEmpty();
    }
}
