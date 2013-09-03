package com.codahale.dropwizard.lifecycle.setup;

import static org.fest.assertions.api.Assertions.assertThat;

import java.util.concurrent.Executor;

import org.junit.Test;

import com.codahale.dropwizard.lifecycle.Managed;
import com.google.common.util.concurrent.AbstractIdleService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.Service;
import com.google.common.util.concurrent.Service.State;

public class LifecycleEnvironmentTest
{
    private static class TestListener implements Service.Listener
    {
        public boolean started = false;
        public boolean stopped = false;

        public void terminated(State from)
        {
            stopped = true;
        }

        public void stopping(State from)
        {
        }

        public void starting()
        {
        }

        public void running()
        {
            started = true;
        }

        public void failed(State from, Throwable failure)
        {
        }
    }

    private final LifecycleEnvironment environment = new LifecycleEnvironment();

    @Test
    public void managesLifeCycleObjects() throws Exception
    {
        final TestListener listener = new TestListener();
        environment.manage(listener);

        final Service container = newIdleService();

        environment.attach(container);
        container.startAndWait();
        assertThat(listener.started).isTrue();
        assertThat(listener.stopped).isFalse();
        container.stopAndWait();
        assertThat(listener.stopped).isTrue();
    }

    private static class TestManaged implements Managed
    {
        public boolean started = false;
        public boolean stopped = false;

        public void start() throws Exception
        {
            started = true;
        }

        public void stop() throws Exception
        {
            stopped = true;
        }
    }

    @Test
    public void managesManagedObjects() throws Exception
    {
        final TestManaged managed = new TestManaged();

        environment.manage(managed);

        final Service container = newIdleService();
        environment.attach(container);
        container.startAndWait();
        assertThat(managed.started).isTrue();
        assertThat(managed.stopped).isFalse();
        container.stopAndWait();
        assertThat(managed.stopped).isTrue();
    }

    private Service newIdleService()
    {
        return new AbstractIdleService() {
            @Override
            protected void shutDown() throws Exception
            {
            }
            
            @Override
            protected Executor executor()
            {
                return MoreExecutors.sameThreadExecutor();
            }

            @Override
            protected void startUp() throws Exception
            {
            }
        };
    }
}
