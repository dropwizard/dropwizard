package com.yammer.dropwizard.auth.tests;

import com.google.common.collect.ImmutableMultimap;
import com.yammer.dropwizard.auth.AuthCacheInvalidationTask;
import com.yammer.dropwizard.auth.CachingAuthenticator;
import com.yammer.dropwizard.tasks.Task;
import org.junit.Test;

import java.io.PrintWriter;

import static org.mockito.Mockito.*;

public class AuthCacheInvalidationTaskTest {
    private final CachingAuthenticator auth = mock(CachingAuthenticator.class);
    private final PrintWriter output = mock(PrintWriter.class);
    private final Task task = new AuthCacheInvalidationTask(auth);

    @Test
    public void runsWithNoParameters() throws Exception {
        task.execute(ImmutableMultimap.<String, String>of(), output);

        verify(auth).invalidateAll();
    }
}
