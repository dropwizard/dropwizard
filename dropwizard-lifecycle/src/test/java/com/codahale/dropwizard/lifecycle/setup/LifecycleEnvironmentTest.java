package com.codahale.dropwizard.lifecycle.setup;

import com.codahale.dropwizard.lifecycle.JettyManaged;
import com.codahale.dropwizard.lifecycle.Managed;
import com.google.common.collect.ImmutableList;
import org.eclipse.jetty.util.component.ContainerLifeCycle;
import org.eclipse.jetty.util.component.LifeCycle;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class LifecycleEnvironmentTest {
    private final LifecycleEnvironment environment = new LifecycleEnvironment();

    @Test
    public void managesLifeCycleObjects() throws Exception {
        final LifeCycle lifeCycle = mock(LifeCycle.class);
        environment.manage(lifeCycle);

        final ContainerLifeCycle container = new ContainerLifeCycle();
        environment.attach(container);

        assertThat(container.getBeans())
                .contains(lifeCycle);
    }

    @Test
    public void managesManagedObjects() throws Exception {
        final Managed managed = mock(Managed.class);
        environment.manage(managed);

        final ContainerLifeCycle container = new ContainerLifeCycle();
        environment.attach(container);

        final Object bean = ImmutableList.copyOf(container.getBeans()).get(0);
        assertThat(bean)
                .isInstanceOf(JettyManaged.class);

        final JettyManaged jettyManaged = (JettyManaged) bean;

        assertThat(jettyManaged.getManaged())
                .isEqualTo(managed);
    }
}
