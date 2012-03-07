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

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class HttpConfigurationTest {
    private HttpConfiguration http;

    @Before
    public void setUp() throws Exception {
        this.http = ConfigurationFactory.forClass(HttpConfiguration.class,
                                                  new Validator()).build(new File(Resources.getResource("yaml/http.yml").getFile()));
    }

    @Test
    public void loadsGzipConfig() throws Exception {
        assertThat(http.getGzipConfiguration().isEnabled(),
                   is(false));
    }

    @Test
    public void loadsRequestLogConfig() throws Exception {
        assertThat(http.getRequestLogConfiguration().isEnabled(),
                   is(true));
    }

    @Test
    public void loadsContextParams() throws Exception {
        assertThat(http.getContextParameters(), 
                   is(ImmutableMap.of("param", "value")));
    }
    
    @Test
    public void hasAServicePort() throws Exception {
        assertThat(http.getPort(),
                   is(9080));
    }

    @Test
    public void hasAnAdminPort() throws Exception {
        assertThat(http.getAdminPort(),
                   is(9081));
    }

    @Test
    public void canEnableAdmin() throws Exception {
        assertThat(http.isAdminEnabled(),
                   is(false));
    }

    @Test
    public void hasAMaximumNumberOfThreads() throws Exception {
        assertThat(http.getMaxThreads(),
                   is(101));
    }

    @Test
    public void hasAMinimumNumberOfThreads() throws Exception {
        assertThat(http.getMinThreads(),
                   is(89));
    }

    @Test
    public void hasARootPath() throws Exception {
        assertThat(http.getRootPath(),
                   is("/services/*"));
    }

    @Test
    public void hasAConnectorType() throws Exception {
        assertThat(http.getConnectorType(),
                   is(HttpConfiguration.ConnectorType.SOCKET));
    }

    @Test
    public void hasAMaxIdleTime() throws Exception {
        assertThat(http.getMaxIdleTime(),
                   is(Duration.seconds(2)));
    }

    @Test
    public void hasAnAcceptorThreadCount() throws Exception {
        assertThat(http.getAcceptorThreadCount(),
                   is(2));
    }

    @Test
    public void hasAnAcceptorThreadPriorityOffset() throws Exception {
        assertThat(http.getAcceptorThreadPriorityOffset(),
                   is(-3));
    }

    @Test
    public void hasAnAcceptQueueSize() throws Exception {
        assertThat(http.getAcceptQueueSize(),
                   is(100));
    }

    @Test
    public void hasAMaxBufferCount() throws Exception {
        assertThat(http.getMaxBufferCount(),
                   is(512));
    }

    @Test
    public void hasARequestBufferSize() throws Exception {
        assertThat(http.getRequestBufferSize(),
                   is(Size.kilobytes(16)));
    }

    @Test
    public void hasARequestHeaderBufferSize() throws Exception {
        assertThat(http.getRequestHeaderBufferSize(),
                   is(Size.kilobytes(17)));
    }

    @Test
    public void hasAResponseBufferSize() throws Exception {
        assertThat(http.getResponseBufferSize(),
                   is(Size.kilobytes(18)));
    }

    @Test
    public void hasAResponseHeaderBufferSize() throws Exception {
        assertThat(http.getResponseHeaderBufferSize(),
                   is(Size.kilobytes(19)));
    }

    @Test
    public void canReuseAddresses() throws Exception {
        assertThat(http.isReuseAddressEnabled(),
                   is(false));
    }

    @Test
    public void hasAnSoLingerTime() throws Exception {
        assertThat(http.getSoLingerTime(),
                   is(Optional.of(Duration.seconds(2))));
    }

    @Test
    public void hasALowResourcesConnectionThreshold() throws Exception {
        assertThat(http.getLowResourcesConnectionThreshold(),
                   is(1000));
    }

    @Test
    public void hasALowResourcesMaxIdleTime() throws Exception {
        assertThat(http.getLowResourcesMaxIdleTime(),
                   is(Duration.seconds(1)));
    }

    @Test
    public void hasAShutdownGracePeriod() throws Exception {
        assertThat(http.getShutdownGracePeriod(),
                   is(Duration.seconds(5)));
    }

    @Test
    public void canSendAServerHeader() throws Exception {
        assertThat(http.isServerHeaderEnabled(),
                   is(true));
    }

    @Test
    public void canSendADateHeader() throws Exception {
        assertThat(http.isDateHeaderEnabled(),
                   is(false));
    }

    @Test
    public void canForwardHeaders() throws Exception {
        assertThat(http.useForwardedHeaders(),
                   is(false));
    }

    @Test
    public void canUseDirectBuffers() throws Exception {
        assertThat(http.useDirectBuffers(),
                   is(false));
    }

    @Test
    public void hasABindHost() throws Exception {
        assertThat(http.getBindHost(),
                   is(Optional.of("localhost")));
    }
}
