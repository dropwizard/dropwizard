package io.dropwizard.servlets.tasks;

import org.junit.jupiter.api.Test;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class PostBodyTaskTest {
    private final PostBodyTask task = new PostBodyTask("test") {
        @Override
        public void execute(Map<String, List<String>> parameters, String body, PrintWriter output) {

        }
    };

    @SuppressWarnings("deprecation")
    @Test
    void throwsExceptionWhenCallingExecuteWithoutThePostBody() {
        assertThatExceptionOfType(UnsupportedOperationException.class)
            .isThrownBy(() -> task.execute(Collections.emptyMap(), new PrintWriter(new OutputStreamWriter(System.out, UTF_8))));
    }
}
