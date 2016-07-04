package io.dropwizard.servlets.tasks;

import com.google.common.collect.ImmutableMultimap;
import org.junit.Test;

import java.io.PrintWriter;

import static org.assertj.core.api.Assertions.assertThat;

public class TaskTest {
    private final Task task = new Task("test") {
        @Override
        public void execute(ImmutableMultimap<String, String> parameters,
                            PrintWriter output) throws Exception {

        }
    };

    @Test
    public void hasAName() throws Exception {
        assertThat(task.getName())
                .isEqualTo("test");
    }
}
