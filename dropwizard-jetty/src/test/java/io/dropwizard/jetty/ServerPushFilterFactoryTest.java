package io.dropwizard.jetty;

import io.dropwizard.configuration.ResourceConfigurationSourceProvider;
import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.util.Duration;
import io.dropwizard.validation.BaseValidator;
import jakarta.servlet.DispatcherType;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlets.PushCacheFilter;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.EnumSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class ServerPushFilterFactoryTest {

    @Test
    void testLoadConfiguration() throws Exception {
        final ServerPushFilterFactory serverPush = new YamlConfigurationFactory<>(
                ServerPushFilterFactory.class, BaseValidator.newValidator(),
                Jackson.newObjectMapper(), "dw-server-push")
                .build(new ResourceConfigurationSourceProvider(), "yaml/server-push.yml");
        assertThat(serverPush.isEnabled()).isTrue();
        assertThat(serverPush.getAssociatePeriod()).isEqualTo(Duration.seconds(5));
        assertThat(serverPush.getMaxAssociations()).isEqualTo(8);
        assertThat(serverPush.getRefererHosts()).contains("dropwizard.io", "dropwizard.github.io");
        assertThat(serverPush.getRefererPorts()).contains(8444, 8445);
    }

    @Test
    void testDefaultConfiguration() {
        final ServerPushFilterFactory serverPush = new ServerPushFilterFactory();
        assertThat(serverPush.isEnabled()).isFalse();
        assertThat(serverPush.getAssociatePeriod()).isEqualTo(Duration.seconds(4));
        assertThat(serverPush.getMaxAssociations()).isEqualTo(16);
        assertThat(serverPush.getRefererHosts()).isNull();
        assertThat(serverPush.getRefererPorts()).isNull();
    }

    @Test
    void testDontAddFilterByDefault() {
        final ServerPushFilterFactory serverPush = new ServerPushFilterFactory();

        ServletContextHandler servletContextHandler = mock(ServletContextHandler.class);
        serverPush.addFilter(servletContextHandler);
        verify(servletContextHandler, never())
                .addFilter(PushCacheFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));
    }

    @Test
    void testAddFilter() {
        final ServerPushFilterFactory serverPush = new ServerPushFilterFactory();
        serverPush.setRefererHosts(Arrays.asList("dropwizard.io", "dropwizard.github.io"));
        serverPush.setRefererPorts(Arrays.asList(8444, 8445));
        serverPush.setEnabled(true);

        ServletContextHandler servletContextHandler = mock(ServletContextHandler.class);

        serverPush.addFilter(servletContextHandler);

        verify(servletContextHandler).setInitParameter("associatePeriod", "4000");
        verify(servletContextHandler).setInitParameter("maxAssociations", "16");
        verify(servletContextHandler).setInitParameter("hosts", "dropwizard.io,dropwizard.github.io");
        verify(servletContextHandler).setInitParameter("ports", "8444,8445");
        verify(servletContextHandler).addFilter(PushCacheFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));
    }

    @Test
    void testRefererHostsAndPortsAreNotSet() {
        final ServerPushFilterFactory serverPush = new ServerPushFilterFactory();
        serverPush.setEnabled(true);

        ServletContextHandler servletContextHandler = mock(ServletContextHandler.class);

        serverPush.addFilter(servletContextHandler);

        verify(servletContextHandler, never()).setInitParameter(eq("hosts"), anyString());
        verify(servletContextHandler, never()).setInitParameter(eq("ports"), anyString());
        verify(servletContextHandler).addFilter(PushCacheFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));
    }
}
