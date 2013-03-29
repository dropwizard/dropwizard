package com.yammer.dropwizard.tests;

import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.cli.CheckCommand;
import com.yammer.dropwizard.cli.Command;
import com.yammer.dropwizard.cli.ServerCommand;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Configuration;
import com.yammer.dropwizard.config.Environment;
import org.junit.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.fest.assertions.api.Assertions.assertThat;

public class ServiceTest {
    private static class FakeConfiguration extends Configuration {}

    private static class FakeService extends Service<FakeConfiguration> {
        @Override
        public void initialize(Bootstrap<FakeConfiguration> bootstrap) {}

        @Override
        public void run(FakeConfiguration configuration, Environment environment) {}
    }

    private static class PoserService extends FakeService {}

    private static class WrapperService<C extends FakeConfiguration> extends Service<C> {
        private final Service<C> service;

        private WrapperService(Service<C> service) {
            this.service = service;
        }

        @Override
        public void initialize(Bootstrap<C> bootstrap) {
            this.service.initialize(bootstrap);
        }

        @Override
        public void run(C configuration, Environment environment) throws Exception {
            this.service.run(configuration, environment);
        }
    }

    @Test
    public void hasAReferenceToItsTypeParameter() throws Exception {
        assertThat(new FakeService().getConfigurationClass())
                .isSameAs(FakeConfiguration.class);
    }

    @Test
    public void canDetermineConfiguration() throws Exception {
        assertThat(new PoserService().getConfigurationClass())
                .isSameAs(FakeConfiguration.class);
    }

    @Test
    public void canDetermineWrappedConfiguration() throws Exception {
        final PoserService service = new PoserService();
        assertThat(new WrapperService<FakeConfiguration>(service).getConfigurationClass())
                .isSameAs(FakeConfiguration.class);
    }

    @Test
    public void notNullServerCommand() {
        final PoserService service = new PoserService();
        ServerCommand<FakeConfiguration> serverCommand = service.getServerCommand();
        assertThat(serverCommand).isNotNull().isInstanceOf(ServerCommand.class);
    }

    @Test
    public void testDefaultCommands() {
        final PoserService service = new PoserService();
        List<Command> defaultCommands = service.getDefaultCommands();
        assertThat(defaultCommands).isNotNull().isNotEmpty();

        Set<Class> commandTypes = new HashSet<Class>();
        for(Command command : defaultCommands) {
            commandTypes.add(command.getClass());
        }

        assertThat(commandTypes).contains(ServerCommand.class, CheckCommand.class);
    }
}
