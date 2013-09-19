package io.dropwizard.servlets.tasks;

import com.google.common.collect.ImmutableMultimap;
import org.junit.Test;

import java.io.PrintWriter;

import static org.mockito.Mockito.*;

@SuppressWarnings("CallToSystemGC")
public class GarbageCollectionTaskTest {
    private final Runtime runtime = mock(Runtime.class);
    private final PrintWriter output = mock(PrintWriter.class);
    private final Task task = new GarbageCollectionTask(runtime);

    @Test
    public void runsOnceWithNoParameters() throws Exception {
        task.execute(ImmutableMultimap.<String, String>of(), output);

        verify(runtime, times(1)).gc();
    }

    @Test
    public void usesTheFirstRunsParameter() throws Exception {
        task.execute(ImmutableMultimap.of("runs", "3", "runs", "2"), output);

        verify(runtime, times(3)).gc();
    }

    @Test
    public void defaultsToOneRunIfTheQueryParamDoesNotParse() throws Exception {
        task.execute(ImmutableMultimap.of("runs", "$"), output);

        verify(runtime, times(1)).gc();
    }
}
