package io.dropwizard.client;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Iterables;
import io.dropwizard.jersey.gzip.ConfiguredGZipEncoder;
import io.dropwizard.jersey.gzip.GZipDecoder;
import io.dropwizard.jersey.jackson.JacksonMessageBodyProvider;
import io.dropwizard.lifecycle.setup.LifecycleEnvironment;
import io.dropwizard.setup.Environment;
import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.junit.Before;
import org.junit.Test;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class JerseyClientBuilderTest {
    private final JerseyClientBuilder builder = new JerseyClientBuilder(new MetricRegistry());
    private final LifecycleEnvironment lifecycleEnvironment = spy(new LifecycleEnvironment());
    private final Environment environment = mock(Environment.class);
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final ObjectMapper objectMapper = mock(ObjectMapper.class);
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Before
    public void setUp() throws Exception {
        when(environment.lifecycle()).thenReturn(lifecycleEnvironment);
        when(environment.getObjectMapper()).thenReturn(objectMapper);
        when(environment.getValidator()).thenReturn(validator);
    }

    @Test
    public void throwsAnExceptionWithoutAnEnvironmentOrAThreadPoolAndObjectMapper() throws Exception {
        try {
            builder.build("test");
            failBecauseExceptionWasNotThrown(IllegalStateException.class);
        } catch (IllegalStateException e) {
            assertThat(e.getMessage())
                    .isEqualTo("Must have either an environment or both an executor service and an object mapper");
        }
    }

    @Test
    public void buildsAnApache4BasedClient() throws Exception {
        final Client client = builder.using(executorService, objectMapper).build("test");
        final ClientConfig jerseyConfig = (ClientConfig) client.getConfiguration();

        assertThat(jerseyConfig.getConnectorProvider()).isInstanceOf(ApacheConnectorProvider.class);
    }

    @Test
    public void includesJerseyProperties() throws Exception {
        final Client client = builder.withProperty("poop", true)
                .using(executorService, objectMapper)
                .build("test");

        assertThat(client.getConfiguration().getProperty("poop")).isEqualTo(Boolean.TRUE);
    }

    @Test
    public void includesJerseyProviderSingletons() throws Exception {
        final FakeMessageBodyReader provider = new FakeMessageBodyReader();
        final Client client = builder.withProvider(provider)
                .using(executorService, objectMapper)
                .build("test");

        assertThat(client.getConfiguration().isRegistered(provider)).isTrue();
    }

    @Test
    public void includesJerseyProviderClasses() throws Exception {
        @SuppressWarnings("unused")
        final Client client = builder.withProvider(FakeMessageBodyReader.class)
                .using(executorService, objectMapper)
                .build("test");

        assertThat(client.getConfiguration().isRegistered(FakeMessageBodyReader.class)).isTrue();
    }

    @Test
    public void usesTheObjectMapperForJson() throws Exception {
        final Client client = builder.using(executorService, objectMapper).build("test");
        assertThat(client.getConfiguration().isRegistered(JacksonMessageBodyProvider.class)).isTrue();
    }

    @Test
    public void usesTheGivenThreadPool() throws Exception {
        final Client client = builder.using(executorService, objectMapper).build("test");

        for (Object o : client.getConfiguration().getInstances()) {
            if (o instanceof DropwizardExecutorProvider) {
                final DropwizardExecutorProvider provider = (DropwizardExecutorProvider) o;
                assertThat(provider.getRequestingExecutor()).isSameAs(executorService);
            }
        }

    }

    @Test
    public void addBidirectionalGzipSupportIfEnabled() throws Exception {
        final JerseyClientConfiguration configuration = new JerseyClientConfiguration();
        configuration.setGzipEnabled(true);

        final Client client = builder.using(configuration)
                .using(executorService, objectMapper).build("test");
        assertThat(Iterables.filter(client.getConfiguration().getInstances(), GZipDecoder.class)
                .iterator().hasNext()).isTrue();
        assertThat(Iterables.filter(client.getConfiguration().getInstances(), ConfiguredGZipEncoder.class)
                .iterator().hasNext()).isTrue();
    }

    @Test
    public void disablesGzipSupportIfDisabled() throws Exception {
        final JerseyClientConfiguration configuration = new JerseyClientConfiguration();
        configuration.setGzipEnabled(false);

        final Client client = builder.using(configuration)
                .using(executorService, objectMapper).build("test");

        assertThat(Iterables.filter(client.getConfiguration().getInstances(), GZipDecoder.class)
                .iterator().hasNext()).isFalse();
        assertThat(Iterables.filter(client.getConfiguration().getInstances(), ConfiguredGZipEncoder.class)
                .iterator().hasNext()).isFalse();
    }

    @Test
    public void usesAnObjectMapperFromTheEnvironment() throws Exception {
        final Client client = builder.using(environment).build("test");

        assertThat(client.getConfiguration().isRegistered(JacksonMessageBodyProvider.class)).isTrue();
    }

    @Test
    public void usesAnExecutorServiceFromTheEnvironment() throws Exception {
        final JerseyClientConfiguration configuration = new JerseyClientConfiguration();
        configuration.setMinThreads(7);
        configuration.setMaxThreads(532);

        builder.using(configuration).using(environment).build("test");

        verify(lifecycleEnvironment).executorService("jersey-client-test-%d");
    }

    @Provider
    @Consumes(MediaType.APPLICATION_SVG_XML)
    public static class FakeMessageBodyReader implements MessageBodyReader<JerseyClientBuilderTest> {
        @Override
        public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return JerseyClientBuilderTest.class.isAssignableFrom(type);
        }

        @Override
        public JerseyClientBuilderTest readFrom(Class<JerseyClientBuilderTest> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException, WebApplicationException {
            return null;
        }
    }
}
