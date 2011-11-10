package com.yammer.dropwizard.tests;

import com.google.common.collect.ImmutableList;
import com.yammer.dropwizard.Module;
import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.cli.Command;
import com.yammer.dropwizard.cli.ManagedCommand;
import com.yammer.dropwizard.config.Configuration;
import com.yammer.dropwizard.config.Environment;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ServiceTest {
    @SuppressWarnings({"PackageVisibleInnerClass", "EmptyClass"})
    static class FakeConfiguration extends Configuration {

    }

    private class FakeService extends Service<FakeConfiguration> {
        FakeService() {
            super("test");
            addModule(module);
            addCommand(command);
            addCommand(managedCommand);
            setBanner("woo");
        }

        @Override
        public void initialize(FakeConfiguration configuration, Environment environment) {
        }
    }

    private final Module module = mock(Module.class);
    private final Command command = mock(Command.class);
    @SuppressWarnings("unchecked")
    private final ManagedCommand<FakeConfiguration> managedCommand = mock(ManagedCommand.class);
    private final FakeService service = new FakeService();

    @Before
    public void setUp() throws Exception {
        when(command.getName()).thenReturn("command");
        when(managedCommand.getName()).thenReturn("managed-command");
    }

    @Test
    public void hasAReferenceToItsTypeParameter() throws Exception {
        assertThat(service.getConfigurationClass(),
                   is(sameInstance(FakeConfiguration.class)));
    }

    @Test
    public void hasModules() throws Exception {
        assertThat(service.getModules(),
                   is(ImmutableList.of(module)));
    }

    @Test
    public void hasCommands() throws Exception {
        assertThat(service.getCommands(),
                   is(ImmutableList.<Command>of(command, managedCommand)));
    }

    @Test
    public void mightHaveABanner() throws Exception {
        assertThat(service.hasBanner(),
                   is(true));
        
        assertThat(service.getBanner(),
                   is("woo"));
    }
}
