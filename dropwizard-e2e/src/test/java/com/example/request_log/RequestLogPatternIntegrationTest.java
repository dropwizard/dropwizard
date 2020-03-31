package com.example.request_log;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.nio.file.Files;
import java.util.List;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

@DisabledOnOs(OS.WINDOWS) // FIXME: Make tests run on Windows
class RequestLogPatternIntegrationTest extends AbstractRequestLogPatternIntegrationTest {

    private static final Pattern REQUEST_LOG_PATTERN = Pattern.compile(
        "127\\.0\\.0\\.1 - - \\[.+\\] \"GET /greet\\?name=Charley HTTP/1\\.1\" 200 15 \"-\" \"TestApplication \\(test-request-logs\\)\" \\d+"
    );

    @Test
    void testDefaultPattern() throws Exception {
        String url = String.format("http://localhost:%d/greet?name=Charley", dropwizardAppRule.getLocalPort());
        for (int i = 0; i < 100; i++) {
            client.target(url).request().get();
        }

        dropwizardAppRule.getConfiguration().getLoggingFactory().stop();
        dropwizardAppRule.getConfiguration().getLoggingFactory().reset();
        Thread.sleep(100L);

        List<String> logs = Files.readAllLines(requestLogFile, UTF_8);
        assertThat(logs).hasSize(100).allMatch(s -> REQUEST_LOG_PATTERN.matcher(s).matches());
    }
}
