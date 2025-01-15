package io.dropwizard.client;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.httpclient5.HttpClientMetricNameStrategies;
import com.codahale.metrics.httpclient5.HttpClientMetricNameStrategy;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.core.setup.Environment;
import io.dropwizard.jersey.gzip.ConfiguredGZipEncoder;
import io.dropwizard.jersey.gzip.GZipDecoder;
import io.dropwizard.jersey.validation.Validators;
import io.dropwizard.lifecycle.setup.ExecutorServiceBuilder;
import io.dropwizard.lifecycle.setup.LifecycleEnvironment;
import jakarta.validation.Validator;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyReader;
import jakarta.ws.rs.ext.Provider;
import org.apache.hc.client5.http.DnsResolver;
import org.apache.hc.client5.http.HttpRequestRetryStrategy;
import org.apache.hc.client5.http.SystemDefaultDnsResolver;
import org.apache.hc.client5.http.auth.CredentialsStore;
import org.apache.hc.client5.http.entity.GzipCompressingEntity;
import org.apache.hc.client5.http.impl.DefaultHttpRequestRetryStrategy;
import org.apache.hc.client5.http.impl.auth.SystemDefaultCredentialsProvider;
import org.apache.hc.client5.http.impl.routing.SystemDefaultRoutePlanner;
import org.apache.hc.client5.http.routing.HttpRoutePlanner;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.client5.http.socket.PlainConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.config.Registry;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.http.io.entity.ByteArrayEntity;
import org.apache.hc.core5.util.TimeValue;
import org.jspecify.annotations.Nullable;
import org.glassfish.jersey.client.ClientRequest;
import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.client.rx.rxjava2.RxFlowableInvokerProvider;
import org.glassfish.jersey.client.spi.ConnectorProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JerseyClientBuilderTest {
    private final MetricRegistry metricRegistry = new MetricRegistry();
    private final JerseyClientBuilder builder = new JerseyClientBuilder(metricRegistry);
    private final LifecycleEnvironment lifecycleEnvironment = spy(new LifecycleEnvironment(metricRegistry));
    private final Environment environment = mock(Environment.class);
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final ObjectMapper objectMapper = mock(ObjectMapper.class);
    private final Validator validator = Validators.newValidator();
    private final HttpClientBuilder apacheHttpClientBuilder = mock(HttpClientBuilder.class);

    @BeforeEach
    void setUp() throws Exception {
        when(environment.lifecycle()).thenReturn(lifecycleEnvironment);
        when(environment.getObjectMapper()).thenReturn(objectMapper);
        when(environment.getValidator()).thenReturn(validator);
        builder.setApacheHttpClientBuilder(apacheHttpClientBuilder);
    }

    @AfterEach
    void tearDown() {
        executorService.shutdown();
    }

    @Test
    void throwsAnExceptionWithoutAnEnvironmentOrAThreadPoolAndObjectMapper() {
        assertThatIllegalStateException()
            .isThrownBy(() -> builder.build("test"))
            .withMessage("Must have either an environment or both an executor service and an object mapper");
    }

    @Test
    void throwsAnExceptionWithoutAnEnvironmentAndOnlyObjectMapper() {
        JerseyClientBuilder configuredBuilder = builder.using(objectMapper);
        assertThatIllegalStateException()
            .isThrownBy(() -> configuredBuilder.build("test"))
            .withMessage("Must have either an environment or both an executor service and an object mapper");
    }

    @Test
    void throwsAnExceptionWithoutAnEnvironmentAndOnlyAThreadPool() {
        JerseyClientBuilder configuredBuilder = builder.using(executorService);
        assertThatIllegalStateException()
            .isThrownBy(() -> configuredBuilder.build("test"))
            .withMessage("Must have either an environment or both an executor service and an object mapper");
    }

    @Test
    void includesJerseyProperties() {
        final Client client = builder.withProperty("poop", true)
                .using(executorService, objectMapper)
                .build("test");

        assertThat(client.getConfiguration().getProperty("poop")).isEqualTo(Boolean.TRUE);
    }

    @Test
    void includesJerseyProviderSingletons() {
        final FakeMessageBodyReader provider = new FakeMessageBodyReader();
        final Client client = builder.withProvider(provider)
                .using(executorService, objectMapper)
                .build("test");

        assertThat(client.getConfiguration().isRegistered(provider)).isTrue();
    }

    @Test
    void includesJerseyProviderClasses() {
        @SuppressWarnings("unused")
        final Client client = builder.withProvider(FakeMessageBodyReader.class)
                .using(executorService, objectMapper)
                .build("test");

        assertThat(client.getConfiguration().isRegistered(FakeMessageBodyReader.class)).isTrue();
    }

    @Test
    void createsAnRxEnabledClient() {
        final Client client =
            builder.using(executorService, objectMapper)
                .buildRx("test", RxFlowableInvokerProvider.class);

        for (Object o : client.getConfiguration().getInstances()) {
            if (o instanceof DropwizardExecutorProvider) {
                final DropwizardExecutorProvider provider = (DropwizardExecutorProvider) o;
                assertThat(provider.getExecutorService()).isSameAs(executorService);
            }
        }
    }

    @Test
    void usesTheGivenThreadPool() {
        final Client client = builder.using(executorService, objectMapper).build("test");
        for (Object o : client.getConfiguration().getInstances()) {
            if (o instanceof DropwizardExecutorProvider) {
                final DropwizardExecutorProvider provider = (DropwizardExecutorProvider) o;
                assertThat(provider.getExecutorService()).isSameAs(executorService);
            }
        }

    }

    @Test
    void usesTheGivenThreadPoolAndEnvironmentsObjectMapper() {
        final Client client = builder.using(environment).using(executorService).build("test");
        for (Object o : client.getConfiguration().getInstances()) {
            if (o instanceof DropwizardExecutorProvider) {
                final DropwizardExecutorProvider provider = (DropwizardExecutorProvider) o;
                assertThat(provider.getExecutorService()).isSameAs(executorService);
            }
        }

    }

    @Test
    void createsNewConnectorProvider() {
        final JerseyClient clientA = (JerseyClient) builder.using(executorService, objectMapper).build("testA");
        final JerseyClient clientB = (JerseyClient) builder.build("testB");
        assertThat(clientA.getConfiguration().getConnectorProvider())
            .isNotSameAs(clientB.getConfiguration().getConnectorProvider());
    }

    @Test
    void usesSameConnectorProvider()  {
        final JerseyClient clientA = (JerseyClient) builder.using(executorService, objectMapper)
            .using(mock(ConnectorProvider.class))
            .build("testA");
        final JerseyClient clientB = (JerseyClient) builder.build("testB");

        assertThat(clientA.getConfiguration().getConnectorProvider())
            .isSameAs(clientB.getConfiguration().getConnectorProvider());
    }

    @Test
    void addBidirectionalGzipSupportIfEnabled() {
        final JerseyClientConfiguration configuration = new JerseyClientConfiguration();
        configuration.setGzipEnabled(true);

        final Client client = builder.using(configuration)
                .using(executorService, objectMapper).build("test");
        assertThat(client.getConfiguration().getInstances())
                .anyMatch(element -> element instanceof GZipDecoder);
        assertThat(client.getConfiguration().getInstances())
                .anyMatch(element -> element instanceof ConfiguredGZipEncoder);
        verify(apacheHttpClientBuilder, never()).disableContentCompression(true);
    }

    @Test
    void disablesGzipSupportIfDisabled() {
        final JerseyClientConfiguration configuration = new JerseyClientConfiguration();
        configuration.setGzipEnabled(false);

        final Client client = builder.using(configuration)
                .using(executorService, objectMapper).build("test");

        assertThat(client.getConfiguration().getInstances())
                .noneMatch(element -> element instanceof GZipDecoder);
        assertThat(client.getConfiguration().getInstances())
                .noneMatch(element -> element instanceof ConfiguredGZipEncoder);
        verify(apacheHttpClientBuilder).disableContentCompression(true);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void usesAnExecutorServiceFromTheEnvironment() {
        final JerseyClientConfiguration configuration = new JerseyClientConfiguration();
        configuration.setMinThreads(7);
        configuration.setMaxThreads(532);
        configuration.setWorkQueueSize(16);

        final ExecutorServiceBuilder executorServiceBuilderMock = mock(ExecutorServiceBuilder.class);
        when(lifecycleEnvironment.executorService("jersey-client-test-%d")).thenReturn(executorServiceBuilderMock);

        when(executorServiceBuilderMock.minThreads(7)).thenReturn(executorServiceBuilderMock);
        when(executorServiceBuilderMock.maxThreads(532)).thenReturn(executorServiceBuilderMock);

        final ArgumentCaptor<ArrayBlockingQueue> arrayBlockingQueueCaptor =
                ArgumentCaptor.forClass(ArrayBlockingQueue.class);
        when(executorServiceBuilderMock.workQueue(arrayBlockingQueueCaptor.capture()))
                .thenReturn(executorServiceBuilderMock);
        when(executorServiceBuilderMock.build()).thenReturn(mock(ExecutorService.class));

        builder.using(configuration).using(environment).build("test");

        assertThat(arrayBlockingQueueCaptor.getValue().remainingCapacity()).isEqualTo(16);
    }

    @Test
    void usesACustomHttpClientMetricNameStrategy() {
        final HttpClientMetricNameStrategy customStrategy = HttpClientMetricNameStrategies.HOST_AND_METHOD;
        builder.using(customStrategy);
        verify(apacheHttpClientBuilder).using(customStrategy);
    }

    @Test
    void usesACustomHttpRequestRetryHandler() {
        final HttpRequestRetryStrategy customRetryHandler = new DefaultHttpRequestRetryStrategy(2, TimeValue.ofSeconds(1));
        builder.using(customRetryHandler);
        verify(apacheHttpClientBuilder).using(customRetryHandler);
    }

    @Test
    void usesACustomDnsResolver() {
        final DnsResolver customDnsResolver = new SystemDefaultDnsResolver();
        builder.using(customDnsResolver);
        verify(apacheHttpClientBuilder).using(customDnsResolver);
    }

    @Test
    void usesACustomHostnameVerifier() {
        final HostnameVerifier customHostnameVerifier = new NoopHostnameVerifier();
        builder.using(customHostnameVerifier);
        verify(apacheHttpClientBuilder).using(customHostnameVerifier);
    }

    @Test
    void usesACustomConnectionFactoryRegistry() throws Exception {
        final SSLContext ctx = SSLContext.getInstance("TLSv1.2");
        ctx.init(null, new TrustManager[]{
            new X509TrustManager() {

                @Override
                public void checkClientTrusted(X509Certificate[] xcs, String string) {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] xcs, String string) {
                }

                @Override
                @Nullable
                public X509Certificate @Nullable [] getAcceptedIssuers() {
                    return null;
                }
            }
        }, null);
        final Registry<ConnectionSocketFactory> customRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.getSocketFactory())
                .register("https", new SSLConnectionSocketFactory(ctx, new NoopHostnameVerifier()))
                .build();
        builder.using(customRegistry);
        verify(apacheHttpClientBuilder).using(customRegistry);
    }

    @Test
    void usesACustomEnvironmentName() {
        final String userAgent = "Dropwizard Jersey Client";
        builder.name(userAgent);
        verify(apacheHttpClientBuilder).name(userAgent);
    }

    @Test
    void usesACustomHttpRoutePlanner() {
        final HttpRoutePlanner customHttpRoutePlanner = new SystemDefaultRoutePlanner(new ProxySelector() {
            @Override
            public List<Proxy> select(URI uri) {
                return Collections.singletonList(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("192.168.53.12", 8080)));
            }

            @Override
            public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {

            }
        });
        builder.using(customHttpRoutePlanner);
        verify(apacheHttpClientBuilder).using(customHttpRoutePlanner);
    }

    @Test
    void usesACustomCredentialsProvider() {
        CredentialsStore customCredentialsProvider = new SystemDefaultCredentialsProvider();
        builder.using(customCredentialsProvider);
        verify(apacheHttpClientBuilder).using(customCredentialsProvider);
    }

    @Test
    void apacheConnectorCanOverridden() {
        assertThat(new JerseyClientBuilder(new MetricRegistry()) {
            @Override
            protected DropwizardApacheConnector createDropwizardApacheConnector(ConfiguredCloseableHttpClient configuredClient) {
                return new DropwizardApacheConnector(configuredClient.getClient(), configuredClient.getDefaultRequestConfig(),
                    true) {
                    @Override
                    protected HttpEntity getHttpEntity(ClientRequest jerseyRequest) {
                        return new GzipCompressingEntity(new ByteArrayEntity((byte[]) jerseyRequest.getEntity(), ContentType.DEFAULT_BINARY));
                    }
                };
            }
        }.using(environment).build("test")).isNotNull();
    }

    @Provider
    @Consumes(MediaType.APPLICATION_SVG_XML)
    public static class FakeMessageBodyReader implements MessageBodyReader<JerseyClientBuilderTest> {
        @Override
        public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return JerseyClientBuilderTest.class.isAssignableFrom(type);
        }

        @Override
        @Nullable
        public JerseyClientBuilderTest readFrom(Class<JerseyClientBuilderTest> type, Type genericType,
                                                Annotation[] annotations, MediaType mediaType,
                                                MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
            throws WebApplicationException {
            return null;
        }
    }
}
