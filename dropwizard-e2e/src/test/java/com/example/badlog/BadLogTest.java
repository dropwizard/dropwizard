package com.example.badlog;

import io.dropwizard.Configuration;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.DropwizardTestSupport;
import io.dropwizard.testing.ResourceHelpers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class BadLogTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(BadLogTest.class);
    private static ByteArrayOutputStream out;
    private static ByteArrayOutputStream err;
    private static PrintStream oldOut = System.out;
    private static PrintStream oldErr = System.err;

    @BeforeEach
    public void setup() throws Exception {
        out = new ByteArrayOutputStream();
        err = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));
        System.setErr(new PrintStream(err));
    }

    @AfterEach
    public void teardown() {
        System.setOut(oldOut);
        System.setErr(oldErr);
    }

    @Test
    public void thatLoggingIsntBrokenOnCleanup() throws Exception {
        BadLogApp.runMe(new String[]{"server"});
        LOGGER.info("I'm after the test");
        Thread.sleep(100);

        assertThat(new String(out.toByteArray(), UTF_8))
            .contains("Mayday we're going down")
            .contains("I'm after the test");

        assertThat(new String(err.toByteArray(), UTF_8))
            .contains("I'm a bad app");
    }

    @Test
    public void testSupportShouldResetLogging(@TempDir Path tempDir) throws Exception {
        final Path logFile = tempDir.resolve("example.log");

        // Clear out the log file
        Files.write(logFile, new byte[]{});

        final ConfigOverride logOverride = ConfigOverride.config("logging.appenders[0].currentLogFilename", logFile.toString());
        final String configPath = ResourceHelpers.resourceFilePath("badlog/config.yaml");
        final DropwizardTestSupport<Configuration> app = new DropwizardTestSupport<>(BadLogApp.class, configPath, logOverride);
        assertThatThrownBy(app::before).hasMessage("I'm a bad app");

        // Dropwizard test support resets configuration overrides if `before` throws an exception
        // which is fine, as that would normally signal the end of the test, but since we're
        // testing logging behavior that is setup in the application `run` method, we need
        // to ensure our log override is still present (it's removed again in `after`)
        logOverride.addToSystemProperties();

        // Explicitly run the command so that the fatal error function runs
        app.getApplication().run("server", configPath);
        app.after();

        // Since our dropwizard app is only using the file appender, the console
        // appender should not contain our logging statements
        assertThat(new String(out.toByteArray(), UTF_8))
            .doesNotContain("Mayday we're going down");
        out.reset();

        // and the file should have our logging statements
        final String contents = new String(Files.readAllBytes(logFile), UTF_8);
        assertThat(contents).contains("Mayday we're going down");

        // Clear out the log file
        Files.write(logFile, new byte[]{});

        final DropwizardTestSupport<Configuration> app2 = new DropwizardTestSupport<>(BadLogApp.class, new Configuration());
        assertThatThrownBy(app2::before).hasMessage("I'm a bad app");

        // Explicitly run the command so that the fatal error function runs
        app2.getApplication().run("server");
        app2.after();

        // Now the console appender will see the fatal error
        assertThat(new String(out.toByteArray(), UTF_8))
            .contains("Mayday we're going down");
        out.reset();

        // and the old log file shouldn't
        final String contents2 = new String(Files.readAllBytes(logFile), UTF_8);
        assertThat(contents2).doesNotContain("Mayday we're going down");

        // And for the final set of assertions will make sure that going back to the app
        // that logs to a file is still behaviorally correct.
        // Clear out the log file
        Files.write(logFile, new byte[]{});

        final DropwizardTestSupport<Configuration> app3 = new DropwizardTestSupport<>(BadLogApp.class, configPath, logOverride);
        assertThatThrownBy(app3::before).hasMessage("I'm a bad app");

        // See comment above about manually adding config to system properties
        logOverride.addToSystemProperties();

        // Explicitly run the command so that the fatal error function runs
        app3.getApplication().run("server", configPath);
        app3.after();

        // Since our dropwizard app is only using the file appender, the console
        // appender should not contain our logging statements
        assertThat(new String(out.toByteArray(), UTF_8))
            .doesNotContain("Mayday we're going down");

        // and the file should have our logging statements
        final String contents3 = new String(Files.readAllBytes(logFile), UTF_8);
        assertThat(contents3).contains("Mayday we're going down");
    }
}
