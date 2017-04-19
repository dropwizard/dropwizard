package com.yammer.dropwizard.auth.oauth.tests;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.yammer.dropwizard.auth.CachingAuthenticator;
import com.yammer.dropwizard.auth.oauth.OAuthCacheInvalidationTask;
import com.yammer.dropwizard.tasks.Task;
import org.junit.Test;

import java.io.PrintWriter;

import static org.mockito.Mockito.*;

public class OAuthCacheInvalidationTaskTest {
    private final CachingAuthenticator<String, ?> auth = mock(CachingAuthenticator.class);
    private final PrintWriter output = mock(PrintWriter.class);
    private final Task task = new OAuthCacheInvalidationTask(auth);

    @Test
    public void runsWithNoParameters() throws Exception {
        task.execute(ImmutableMultimap.<String, String>of(), output);

        verify(auth).invalidateAll();
    }

    @Test
    public void runsWithSingleParameter() throws Exception {
        task.execute(ImmutableMultimap.of("credentials", "foobar"), output);

        verify(auth).invalidateAll(ImmutableList.of("foobar"));
    }

    @Test
    public void runsWithMultipleParameters() throws Exception {
        task.execute(ImmutableMultimap.of("credentials", "foobar", "credentials", "barbaz"), output);

        verify(auth).invalidateAll(ImmutableList.of("foobar", "barbaz"));
    }
}
