package com.example.request_log;

import com.google.common.collect.ImmutableList;
import io.dropwizard.testing.ConfigOverride;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class CustomRequestLogPatternIntegrationTest extends AbstractRequestLogPatternIntegrationTest {

    private static final String LOG_FORMAT = "%h|%reqParameter{name}|%m|%U|%s|%b|%i{User-Agent}|%responseHeader{Content-Type}";

    @Override
    protected List<ConfigOverride> configOverrides() {
        return ImmutableList.<ConfigOverride>builder()
            .addAll(super.configOverrides())
            .add(ConfigOverride.config("server.requestLog.appenders[0].logFormat", LOG_FORMAT))
            .build();
    }

    @Test
    public void testCustomPattern() throws Exception {
        String url = String.format("http://localhost:%d/greet?name=Charley", dropwizardAppRule.getLocalPort());
        for (int i = 0; i < 100; i++) {
            client.target(url).request().get();
        }

        Thread.sleep(100); // To let async logs to finish

        List<String> logs;
        try (BufferedReader reader = new BufferedReader(new FileReader(new File(tempFile)))) {
            logs = reader.lines().collect(Collectors.toList());
        }

        // We should have exactly 100 log entries with correct data
        assertThat(logs).hasSize(100).containsOnly(
            "127.0.0.1|Charley|GET|/greet|200|15|TestApplication (test-request-logs)|text/plain");
    }
}
