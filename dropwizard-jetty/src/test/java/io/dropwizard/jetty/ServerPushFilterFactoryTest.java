package io.dropwizard.jetty;

import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.util.Duration;
import io.dropwizard.util.Resources;
import io.dropwizard.validation.BaseValidator;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlets.PushCacheFilter;
import org.junit.Test;

import javax.servlet.DispatcherType;
import java.io.File;
import java.util.Arrays;
import java.util.EnumSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class ServerPushFilterFactoryTest {

    @Test
    public void testLoadConfiguration() throws Exception {
        final ServerPushFilterFactory serverPush = new YamlConfigurationFactory<>(
                ServerPushFilterFactory.class, BaseValidator.newValidator(),
                Jackson.newObjectMapper(), "dw-server-push")
                .build(new File(Resources.getResource("yaml/server-push.yml").toURI()));
        assertThat(serverPush.isEnabled()).isTrue();
        assertThat(serverPush.getAssociatePeriod()).isEqualTo(Duration.seconds(5));
        assertThat(serverPush.getMaxAssociations()).isEqualTo(8);
        assertThat(serverPush.getRefererHosts()).contains("dropwizard.io", "dropwizard.github.io");
        assertThat(serverPush.getRefererPorts()).contains(8444, 8445);
    }

    @Test
    public void testDefaultConfiguration() {
        final ServerPushFilterFactory serverPush = new ServerPushFilterFactory();
        assertThat(serverPush.isEnabled()).isFalse();
        assertThat(serverPush.getAssociatePeriod()).isEqualTo(Duration.seconds(4));
        assertThat(serverPush.getMaxAssociations()).isEqualTo(16);
        assertThat(serverPush.getRefererHosts()).isNull();
        assertThat(serverPush.getRefererPorts()).isNull();
    }

    @Test
    public void testDontAddFilterByDefault() {
        final ServerPushFilterFactory serverPush = new ServerPushFilterFactory();

        ServletContextHandler servletContextHandler = mock(ServletContextHandler.class);
        serverPush.addFilter(servletContextHandler);
        verify(servletContextHandler, never())
                .addFilter(PushCacheFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));
    }

    @Test
    public void testAddFilter() {
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
    public void testRefererHostsAndPortsAreNotSet() {
        final ServerPushFilterFactory serverPush = new ServerPushFilterFactory();
        serverPush.setEnabled(true);

        ServletContextHandler servletContextHandler = mock(ServletContextHandler.class);

        serverPush.addFilter(servletContextHandler);

        verify(servletContextHandler, never()).setInitParameter(eq("hosts"), anyString());
        verify(servletContextHandler, never()).setInitParameter(eq("ports"), anyString());
        verify(servletContextHandler).addFilter(PushCacheFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));
    }
}
