package com.yammer.dropwizard.config.tests;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Resources;
import com.yammer.dropwizard.config.ConfigurationFactory;
import com.yammer.dropwizard.config.HttpConfiguration;
import com.yammer.dropwizard.util.Duration;
import com.yammer.dropwizard.util.Size;
import com.yammer.dropwizard.validation.Validator;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.fest.assertions.api.Assertions.assertThat;

public class HttpConfigurationTest {
    private HttpConfiguration http;

    @Before
    public void setUp() throws Exception {
        this.http = ConfigurationFactory.forClass(HttpConfiguration.class,
                                                  new Validator())
                                        .build(new File(Resources.getResource("yaml/http.yml").toURI()));
    }

    @Test
    public void loadsGzipConfig() throws Exception {
        assertThat(http.getGzipConfiguration().isEnabled())
                .isFalse();
    }

    @Test
    public void loadsContextParams() throws Exception {
        assertThat(http.getContextParameters())
                .isEqualTo(ImmutableMap.of("param", "value"));
    }

    @Test
    public void hasAServicePort() throws Exception {
        assertThat(http.getPort())
                .isEqualTo(9080);
    }

    @Test
    public void hasAnAdminPort() throws Exception {
        assertThat(http.getAdminPort())
                .isEqualTo(9081);
    }

    @Test
    public void hasAMaximumNumberOfThreads() throws Exception {
        assertThat(http.getMaxThreads())
                .isEqualTo(101);
    }

    @Test
    public void hasAMinimumNumberOfThreads() throws Exception {
        assertThat(http.getMinThreads())
                .isEqualTo(89);
    }

    @Test
    public void hasARootPath() throws Exception {
        assertThat(http.getRootPath())
                .isEqualTo("/services/*");
    }

    @Test
    public void hasAConnectorType() throws Exception {
        assertThat(http.getConnectorType())
                .isEqualTo(HttpConfiguration.ConnectorType.LEGACY);
    }

    @Test
    public void hasAMaxIdleTime() throws Exception {
        assertThat(http.getMaxIdleTime())
                .isEqualTo(Duration.seconds(2));
    }

    @Test
    public void hasAnAcceptorThreadCount() throws Exception {
        assertThat(http.getAcceptorThreads())
                .isEqualTo(2);
    }

    @Test
    public void hasAnAcceptorThreadPriorityOffset() throws Exception {
        assertThat(http.getAcceptorThreadPriorityOffset())
                .isEqualTo(-3);
    }

    @Test
    public void hasAnAcceptQueueSize() throws Exception {
        assertThat(http.getAcceptQueueSize())
                .isEqualTo(100);
    }

    @Test
    public void hasAMaxBufferCount() throws Exception {
        assertThat(http.getMaxBufferCount())
                .isEqualTo(512);
    }

    @Test
    public void hasARequestBufferSize() throws Exception {
        assertThat(http.getRequestBufferSize())
                .isEqualTo(Size.kilobytes(16));
    }

    @Test
    public void hasARequestHeaderBufferSize() throws Exception {
        assertThat(http.getRequestHeaderBufferSize())
                .isEqualTo(Size.kilobytes(17));
    }

    @Test
    public void hasAResponseBufferSize() throws Exception {
        assertThat(http.getResponseBufferSize())
                .isEqualTo(Size.kilobytes(18));
    }

    @Test
    public void hasAResponseHeaderBufferSize() throws Exception {
        assertThat(http.getResponseHeaderBufferSize())
                .isEqualTo(Size.kilobytes(19));
    }

    @Test
    public void canReuseAddresses() throws Exception {
        assertThat(http.isReuseAddressEnabled())
                .isFalse();
    }

    @Test
    public void hasAnSoLingerTime() throws Exception {
        assertThat(http.getSoLingerTime())
                .isEqualTo(Optional.of(Duration.seconds(2)));
    }

    @Test
    public void hasALowResourcesConnectionThreshold() throws Exception {
        assertThat(http.getLowResourcesConnectionThreshold())
                .isEqualTo(1000);
    }

    @Test
    public void hasALowResourcesMaxIdleTime() throws Exception {
        assertThat(http.getLowResourcesMaxIdleTime())
                .isEqualTo(Duration.seconds(1));
    }

    @Test
    public void hasAShutdownGracePeriod() throws Exception {
        assertThat(http.getShutdownGracePeriod())
                .isEqualTo(Duration.seconds(5));
    }

    @Test
    public void canSendAServerHeader() throws Exception {
        assertThat(http.isServerHeaderEnabled())
                .isTrue();
    }

    @Test
    public void canSendADateHeader() throws Exception {
        assertThat(http.isDateHeaderEnabled())
                .isFalse();
    }

    @Test
    public void canForwardHeaders() throws Exception {
        assertThat(http.useForwardedHeaders())
                .isFalse();
    }

    @Test
    public void canUseDirectBuffers() throws Exception {
        assertThat(http.useDirectBuffers())
                .isFalse();
    }

    @Test
    public void hasABindHost() throws Exception {
        assertThat(http.getBindHost())
                .isEqualTo(Optional.of("localhost"));
    }

    @Test
    public void hasAnAdminUsername() throws Exception {
        assertThat(http.getAdminUsername())
                .isEqualTo(Optional.of("admin"));
    }

    @Test
    public void hasAnAdminPassword() throws Exception {
        assertThat(http.getAdminPassword())
                .isEqualTo(Optional.of("password"));
    }
}
