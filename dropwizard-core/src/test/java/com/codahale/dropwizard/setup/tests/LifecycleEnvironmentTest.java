package com.codahale.dropwizard.setup.tests;

import com.codahale.dropwizard.jetty.JettyManaged;
import com.codahale.dropwizard.lifecycle.Managed;
import com.codahale.dropwizard.setup.LifecycleEnvironment;
import org.eclipse.jetty.util.component.AggregateLifeCycle;
import org.eclipse.jetty.util.component.LifeCycle;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class LifecycleEnvironmentTest {
    private final AggregateLifeCycle aggregate = mock(AggregateLifeCycle.class);
    private final LifecycleEnvironment environment = new LifecycleEnvironment(aggregate);

    @Test
    public void managesLifeCycleObjects() throws Exception {
        final LifeCycle lifeCycle = mock(LifeCycle.class);
        environment.manage(lifeCycle);

        verify(aggregate).addBean(lifeCycle);
    }

    @Test
    public void managesManagedObjects() throws Exception {
        final Managed managed = mock(Managed.class);
        environment.manage(managed);

        final ArgumentCaptor<JettyManaged> wrapper = ArgumentCaptor.forClass(JettyManaged.class);
        verify(aggregate).addBean(wrapper.capture());

        assertThat(wrapper.getValue().getManaged())
                .isEqualTo(managed);
    }
}
