package com.codahale.dropwizard.tasks.tests;

import com.google.common.collect.ImmutableMultimap;
import com.codahale.dropwizard.tasks.Task;
import org.junit.Test;

import java.io.PrintWriter;

import static org.fest.assertions.api.Assertions.*;

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
