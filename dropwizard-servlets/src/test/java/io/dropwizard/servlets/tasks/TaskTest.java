package io.dropwizard.servlets.tasks;

import org.junit.jupiter.api.Test;

import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class TaskTest {
    private final Task task = new Task("test") {
        @Override
        public void execute(Map<String, List<String>> parameters,
                            PrintWriter output) {

        }
    };

    @Test
    void hasAName() throws Exception {
        assertThat(task.getName())
                .isEqualTo("test");
    }
}
