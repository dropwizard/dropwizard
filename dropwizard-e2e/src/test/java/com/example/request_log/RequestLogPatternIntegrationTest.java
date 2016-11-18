package com.example.request_log;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class RequestLogPatternIntegrationTest extends AbstractRequestLogPatternIntegrationTest {

    private static final Pattern REQUEST_LOG_PATTERN = Pattern.compile(
        "127\\.0\\.0\\.1 - - \\[.+\\] \"GET /greet\\?name=Charley HTTP/1\\.1\" 200 15 \"-\" \"TestApplication \\(test-request-logs\\)\" \\d+"
    );

    @Test
    public void testDefaultPattern() throws Exception {
        String url = String.format("http://localhost:%d/greet?name=Charley", dropwizardAppRule.getLocalPort());
        for (int i = 0; i < 100; i++) {
            client.target(url).request().get();
        }

        Thread.sleep(100); // To let async logs to finish

        List<String> logs;
        try (BufferedReader reader = new BufferedReader(new FileReader(new File(tempFile)))) {
            logs = reader.lines().collect(Collectors.toList());
        }
        assertThat(logs).hasSize(100).allMatch(s -> REQUEST_LOG_PATTERN.matcher(s).matches());
    }
}
