package io.dropwizard.servlets.tasks;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class TaskTest {
    private final Task task = new Task("test") {
        @Override
        public void execute(Map<String, List<String>> parameters, PrintWriter output) throws Exception {}
    };

    @Test
    void hasAName() throws Exception {
        assertThat(task.getName()).isEqualTo("test");
    }
}
