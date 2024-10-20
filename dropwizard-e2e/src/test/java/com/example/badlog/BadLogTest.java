package com.example.badlog;

import io.dropwizard.configuration.ResourceConfigurationSourceProvider;
import io.dropwizard.core.Configuration;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.DropwizardTestSupport;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

class BadLogTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(BadLogTest.class);
    private static final PrintStream oldOut = System.out;
    private static final PrintStream oldErr = System.err;
    private ByteArrayOutputStream out;
    private ByteArrayOutputStream err;

    @BeforeEach
    void setup() {
        out = new ByteArrayOutputStream();
        err = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));
        System.setErr(new PrintStream(err));
    }

    @AfterAll
    static void teardown() {
        System.setOut(oldOut);
        System.setErr(oldErr);
    }

    @Test
    void thatLoggingIsNotBrokenOnCleanup() throws Exception {
        new BadLogApp().run("server");
        LOGGER.info("I'm after the test");
        Thread.sleep(100);

        assertThat(out.toByteArray()).asString(UTF_8)
            .contains("Mayday we're going down")
            .contains("I'm after the test");

        assertThat(err.toByteArray()).asString(UTF_8)
            .contains("I'm a bad app");
    }

    @Test
    void testSupportShouldResetLogging(@TempDir Path tempDir) throws Exception {
        // Clear out the log file
        final Path logFile = Files.write(tempDir.resolve("example.log"), new byte[0]);

        final ConfigOverride logOverride = ConfigOverride.config("logging.appenders[0].currentLogFilename", logFile.toString());
        final String configResource = "badlog/config.yaml";
        final DropwizardTestSupport<Configuration> app = new DropwizardTestSupport<>(BadLogApp.class, configResource, new ResourceConfigurationSourceProvider(), logOverride);
        assertThatThrownBy(app::before).hasMessage("I'm a bad app");

        // Dropwizard test support resets configuration overrides if `before` throws an exception
        // which is fine, as that would normally signal the end of the test, but since we're
        // testing logging behavior that is set up in the application `run` method, we need
        // to ensure our log override is still present (it's removed again in `after`)
        logOverride.addToSystemProperties();

        // Explicitly run the command so that the fatal error function runs
        app.getApplication().run("server", resourceFilePath(configResource));
        app.after();
        Thread.sleep(100L);

        // Since our dropwizard app is only using the file appender, the console
        // appender should not contain our logging statements
        assertThat(out.toByteArray()).asString(UTF_8).doesNotContain("Mayday we're going down");
        out.reset();

        // and the file should have our logging statements
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(
                () -> assertThat(logFile).content(UTF_8).contains("Mayday we're going down"));

        // Clear out the log file
        Files.write(logFile, new byte[0]);

        final DropwizardTestSupport<Configuration> app2 = new DropwizardTestSupport<>(BadLogApp.class, new Configuration());
        assertThatThrownBy(app2::before).hasMessage("I'm a bad app");

        // Explicitly run the command so that the fatal error function runs
        app2.getApplication().run("server");
        app2.after();
        Thread.sleep(100L);

        // Now the console appender will see the fatal error
        assertThat(out.toByteArray()).asString(UTF_8).contains("Mayday we're going down");
        out.reset();

        // and the old log file shouldn't
        assertThat(logFile).content(UTF_8).doesNotContain("Mayday we're going down");

        // And for the final set of assertions will make sure that going back to the app
        // that logs to a file is still behaviorally correct.
        // Clear out the log file
        Files.write(logFile, new byte[0]);

        final DropwizardTestSupport<Configuration> app3 = new DropwizardTestSupport<>(BadLogApp.class, configResource, new ResourceConfigurationSourceProvider(), logOverride);
        assertThatThrownBy(app3::before).hasMessage("I'm a bad app");

        // See comment above about manually adding config to system properties
        logOverride.addToSystemProperties();

        // Explicitly run the command so that the fatal error function runs
        app3.getApplication().run("server", resourceFilePath(configResource));
        app3.after();
        Thread.sleep(100L);

        // Since our dropwizard app is only using the file appender, the console
        // appender should not contain our logging statements
        assertThat(out.toByteArray()).asString(UTF_8).doesNotContain("Mayday we're going down");

        // and the file should have our logging statements
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(
                () -> assertThat(logFile).content(UTF_8).contains("Mayday we're going down"));
    }
}
