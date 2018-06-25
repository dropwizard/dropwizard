package io.dropwizard.servlets.tasks;

import org.junit.Test;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SuppressWarnings("CallToSystemGC")
public class GarbageCollectionTaskTest {
    private final Runtime runtime = mock(Runtime.class);
    private final PrintWriter output = mock(PrintWriter.class);
    private final GarbageCollectionTask task = new GarbageCollectionTask(runtime);

    @Test
    public void runsOnceWithNoParameters() throws Exception {
        task.execute(Collections.emptyMap(), output);

        verify(runtime, times(1)).gc();
    }

    @Test
    public void usesTheFirstRunsParameter() throws Exception {
        final Map<String, List<String>> parameters = Collections.singletonMap("runs", Arrays.asList("3", "2"));
        task.execute(parameters, output);

        verify(runtime, times(3)).gc();
    }

    @Test
    public void defaultsToOneRunIfTheQueryParamDoesNotParse() throws Exception {
        task.execute(Collections.singletonMap("runs", Collections.singletonList("$")), output);

        verify(runtime, times(1)).gc();
    }
}
