package com.yammer.dropwizard.client.tests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.filter.GZIPContentEncodingFilter;
import com.sun.jersey.client.apache4.ApacheHttpClient4;
import com.yammer.dropwizard.client.JerseyClientBuilder;
import com.yammer.dropwizard.client.JerseyClientConfiguration;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.jersey.JacksonMessageBodyProvider;
import com.yammer.dropwizard.json.ObjectMapperFactory;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.MessageBodyReader;
import java.lang.annotation.Annotation;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JerseyClientBuilderTest {
    private static final Annotation[] NO_ANNOTATIONS = new Annotation[0];

    private final JerseyClientBuilder builder = new JerseyClientBuilder();
    private final Environment environment = mock(Environment.class);
    private final ExecutorService executorService = mock(ExecutorService.class);
    private final ObjectMapper objectMapper = mock(ObjectMapper.class);

    @Test
    public void throwsAnExceptionWithoutAnEnvironmentOrAThreadPoolAndObjectMapper() throws Exception {
        try {
            builder.build();
            failBecauseExceptionWasNotThrown(IllegalStateException.class);
        } catch (IllegalStateException e) {
            assertThat(e.getMessage())
                    .isEqualTo("Must have either an environment or both an executor service and an object mapper");
        }
    }

    @Test
    public void buildsAnApache4BasedClient() throws Exception {
        final Client client = builder.using(executorService, objectMapper).build();

        assertThat(client)
                .isInstanceOf(ApacheHttpClient4.class);
    }

    @Test
    public void usesTheObjectMapperForJson() throws Exception {
        final Client client = builder.using(executorService, objectMapper).build();

        final MessageBodyReader<Object> reader = client.getProviders()
                                                       .getMessageBodyReader(Object.class,
                                                                             null,
                                                                             NO_ANNOTATIONS,
                                                                             MediaType.APPLICATION_JSON_TYPE);

        assertThat(reader)
                .isInstanceOf(JacksonMessageBodyProvider.class);
        assertThat(((JacksonMessageBodyProvider) reader).getObjectMapper())
                .isEqualTo(objectMapper);
    }

    @Test
    public void usesTheGivenThreadPool() throws Exception {
        final ApacheHttpClient4 client = (ApacheHttpClient4) builder.using(executorService, objectMapper).build();

        assertThat(client.getExecutorService())
                .isEqualTo(executorService);
    }

    @Test
    public void addBidirectionalGzipSupportIfEnabled() throws Exception {
        final JerseyClientConfiguration configuration = new JerseyClientConfiguration();
        configuration.setGzipEnabled(true);

        final ApacheHttpClient4 client = (ApacheHttpClient4) builder.using(configuration)
                                                                    .using(executorService,
                                                                           objectMapper).build();
        assertThat(client.getHeadHandler())
                .isInstanceOf(GZIPContentEncodingFilter.class);
    }

    @Test
    public void disablesGzipSupportIfDisabled() throws Exception {
        final JerseyClientConfiguration configuration = new JerseyClientConfiguration();
        configuration.setGzipEnabled(false);

        final ApacheHttpClient4 client = (ApacheHttpClient4) builder.using(configuration)
                                                                    .using(executorService,
                                                                           objectMapper).build();

        assertThat(client.getHeadHandler())
                .isNotInstanceOf(GZIPContentEncodingFilter.class);
    }

    @Test
    public void usesAnObjectMapperFromTheEnvironment() throws Exception {
        final JerseyClientConfiguration configuration = new JerseyClientConfiguration();

        when(environment.managedExecutorService("jersey-client-%d",
                                                configuration.getMinThreads(),
                                                configuration.getMaxThreads(),
                                                60,
                                                TimeUnit.SECONDS)).thenReturn(executorService);
        final ObjectMapperFactory factory = mock(ObjectMapperFactory.class);
        when(factory.build()).thenReturn(objectMapper);

        when(environment.getObjectMapperFactory()).thenReturn(factory);

        final Client client = builder.using(environment).build();

        final MessageBodyReader<Object> reader = client.getProviders()
                                                       .getMessageBodyReader(Object.class,
                                                                             null,
                                                                             NO_ANNOTATIONS,
                                                                             MediaType.APPLICATION_JSON_TYPE);

        assertThat(reader)
                .isInstanceOf(JacksonMessageBodyProvider.class);
        assertThat(((JacksonMessageBodyProvider) reader).getObjectMapper())
                .isEqualTo(objectMapper);
    }

    @Test
    public void usesAnExecutorServiceFromTheEnvironment() throws Exception {
        final JerseyClientConfiguration configuration = new JerseyClientConfiguration();

        when(environment.managedExecutorService("jersey-client-%d",
                                                configuration.getMinThreads(),
                                                configuration.getMaxThreads(),
                                                60,
                                                TimeUnit.SECONDS)).thenReturn(executorService);
        final ObjectMapperFactory factory = mock(ObjectMapperFactory.class);
        when(factory.build()).thenReturn(objectMapper);

        when(environment.getObjectMapperFactory()).thenReturn(factory);

        final ApacheHttpClient4 client = (ApacheHttpClient4) builder.using(environment).build();

        assertThat(client.getExecutorService())
                .isEqualTo(executorService);
    }
}
