package com.example.request_log;

import io.dropwizard.testing.ConfigOverride;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

@DisabledOnOs(OS.WINDOWS) // FIXME: Make tests run on Windows
class CustomRequestLogPatternIntegrationTest extends AbstractRequestLogPatternIntegrationTest {
    private static final String LOG_FORMAT = "%h|%reqParameter{name}|%m|%U|%s|%b|%i{User-Agent}|%responseHeader{Content-Type}";

    @Override
    protected List<ConfigOverride> configOverrides() {
        final List<ConfigOverride> configOverrides = new ArrayList<>(super.configOverrides());
        configOverrides.add(ConfigOverride.config("server.requestLog.appenders[0].logFormat", LOG_FORMAT));
        return configOverrides;
    }

    @Test
    void testCustomPattern() throws Exception {
        String url = String.format("http://localhost:%d/greet?name=Charley", dropwizardAppRule.getLocalPort());
        for (int i = 0; i < 100; i++) {
            client.target(url).request().get();
        }

        dropwizardAppRule.getConfiguration().getLoggingFactory().stop();
        dropwizardAppRule.getConfiguration().getLoggingFactory().reset();
        Thread.sleep(100L);

        List<String> logs = Files.readAllLines(requestLogFile, UTF_8);
        assertThat(logs).hasSize(100).containsOnly(
            "127.0.0.1|Charley|GET|/greet|200|15|TestApplication (test-request-logs)|text/plain");
    }
}
