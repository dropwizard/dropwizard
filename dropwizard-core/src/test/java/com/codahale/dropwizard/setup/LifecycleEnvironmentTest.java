package com.codahale.dropwizard.setup;

import com.codahale.dropwizard.jetty.JettyManaged;
import com.codahale.dropwizard.lifecycle.Managed;
import com.google.common.collect.Lists;
import org.eclipse.jetty.util.component.LifeCycle;
import org.junit.Test;

import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class LifecycleEnvironmentTest {
    private final List<Object> managedObjects = Lists.newArrayList();
    private final List<LifeCycle.Listener> listeners = Lists.newArrayList();
    private final LifecycleEnvironment environment = new LifecycleEnvironment(managedObjects,
                                                                              listeners);

    @Test
    public void managesLifeCycleObjects() throws Exception {
        final LifeCycle lifeCycle = mock(LifeCycle.class);
        environment.manage(lifeCycle);

        assertThat(managedObjects)
                .contains(lifeCycle);
    }

    @Test
    public void managesManagedObjects() throws Exception {
        final Managed managed = mock(Managed.class);
        environment.manage(managed);

        assertThat(managedObjects.get(0))
                .isInstanceOf(JettyManaged.class);

        final JettyManaged jettyManaged = (JettyManaged) managedObjects.get(0);

        assertThat(jettyManaged.getManaged())
                .isEqualTo(managed);
    }
}
