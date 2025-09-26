package io.dropwizard.servlets.tasks;

import org.junit.jupiter.api.Test;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class GarbageCollectionTaskTest {
    private final PrintWriter output = mock(PrintWriter.class);

    @Test
    void runsOnceWithNoParameters() throws Exception {
        final AtomicInteger count = new AtomicInteger();
        final GarbageCollectionTask task = new GarbageCollectionTask(count::incrementAndGet);
        task.execute(Collections.emptyMap(), output);

        assertThat(count).hasValue(1);
    }

    @Test
    void usesTheFirstRunsParameter() throws Exception {
        final AtomicInteger count = new AtomicInteger();
        final GarbageCollectionTask task = new GarbageCollectionTask(count::incrementAndGet);
        final Map<String, List<String>> parameters = Collections.singletonMap("runs", Arrays.asList("3", "2"));
        task.execute(parameters, output);

        assertThat(count).hasValue(3);
    }

    @Test
    void defaultsToOneRunIfTheQueryParamDoesNotParse() throws Exception {
        final AtomicInteger count = new AtomicInteger();
        final GarbageCollectionTask task = new GarbageCollectionTask(count::incrementAndGet);
        task.execute(Collections.singletonMap("runs", Collections.singletonList("$")), output);

        assertThat(count).hasValue(1);
    }
}
