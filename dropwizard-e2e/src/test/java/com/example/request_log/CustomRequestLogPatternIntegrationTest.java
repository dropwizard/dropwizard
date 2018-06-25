package com.example.request_log;

import io.dropwizard.testing.ConfigOverride;
import org.junit.Test;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

public class CustomRequestLogPatternIntegrationTest extends AbstractRequestLogPatternIntegrationTest {

    private static final String LOG_FORMAT = "%h|%reqParameter{name}|%m|%U|%s|%b|%i{User-Agent}|%responseHeader{Content-Type}";

    @Override
    protected List<ConfigOverride> configOverrides() {
        final List<ConfigOverride> configOverrides = new ArrayList<>(super.configOverrides());
        configOverrides.add(ConfigOverride.config("server.requestLog.appenders[0].logFormat", LOG_FORMAT));
        return configOverrides;
    }

    @Test
    public void testCustomPattern() throws Exception {
        String url = String.format("http://localhost:%d/greet?name=Charley", dropwizardAppRule.getLocalPort());
        for (int i = 0; i < 100; i++) {
            client.target(url).request().get();
        }

        Thread.sleep(100); // To let async logs to finish

        List<String> logs;
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(tempFile), UTF_8)) {
            logs = reader.lines().collect(Collectors.toList());
        }

        // We should have exactly 100 log entries with correct data
        assertThat(logs).hasSize(100).containsOnly(
            "127.0.0.1|Charley|GET|/greet|200|15|TestApplication (test-request-logs)|text/plain");
    }
}
