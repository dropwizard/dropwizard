package io.dropwizard.client;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.httpclient.HttpClientMetricNameStrategies;
import com.codahale.metrics.httpclient.HttpClientMetricNameStrategy;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import io.dropwizard.jersey.gzip.ConfiguredGZipEncoder;
import io.dropwizard.jersey.gzip.GZipDecoder;
import io.dropwizard.jersey.jackson.JacksonMessageBodyProvider;
import io.dropwizard.jersey.validation.Validators;
import io.dropwizard.lifecycle.setup.ExecutorServiceBuilder;
import io.dropwizard.lifecycle.setup.LifecycleEnvironment;
import io.dropwizard.setup.Environment;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.DnsResolver;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.SystemDefaultCredentialsProvider;
import org.apache.http.impl.conn.SystemDefaultDnsResolver;
import org.apache.http.impl.conn.SystemDefaultRoutePlanner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
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
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;

public class JerseyClientBuilderTest {
    private final JerseyClientBuilder builder = new JerseyClientBuilder(new MetricRegistry());
    private final LifecycleEnvironment lifecycleEnvironment = spy(new LifecycleEnvironment());
    private final Environment environment = mock(Environment.class);
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final ObjectMapper objectMapper = mock(ObjectMapper.class);
    private final Validator validator = Validators.newValidator();
    private final HttpClientBuilder apacheHttpClientBuilder = mock(HttpClientBuilder.class);

    @Before
    public void setUp() throws Exception {
        when(environment.lifecycle()).thenReturn(lifecycleEnvironment);
        when(environment.getObjectMapper()).thenReturn(objectMapper);
        when(environment.getValidator()).thenReturn(validator);
        builder.setApacheHttpClientBuilder(apacheHttpClientBuilder);
    }

    @After
    public void tearDown() throws Exception {
        executorService.shutdown();
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
    public void throwsAnExceptionWithoutAnEnvironmentAndOnlyObjectMapper() throws Exception {
        try {
            builder.using(objectMapper).build("test");
            failBecauseExceptionWasNotThrown(IllegalStateException.class);
        } catch (IllegalStateException e) {
            assertThat(e.getMessage())
                    .isEqualTo("Must have either an environment or both an executor service and an object mapper");
        }
    }

    @Test
    public void throwsAnExceptionWithoutAnEnvironmentAndOnlyAThreadPool() throws Exception {
        try {
            builder.using(executorService).build("test");
            failBecauseExceptionWasNotThrown(IllegalStateException.class);
        } catch (IllegalStateException e) {
            assertThat(e.getMessage())
                    .isEqualTo("Must have either an environment or both an executor service and an object mapper");
        }
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
                assertThat(provider.getExecutorService()).isSameAs(executorService);
            }
        }

    }

    @Test
    public void usesTheGivenThreadPoolAndEnvironmentsObjectMapper() throws Exception {
        final Client client = builder.using(environment).using(executorService).build("test");
        for (Object o : client.getConfiguration().getInstances()) {
            if (o instanceof DropwizardExecutorProvider) {
                final DropwizardExecutorProvider provider = (DropwizardExecutorProvider) o;
                assertThat(provider.getExecutorService()).isSameAs(executorService);
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
        verify(apacheHttpClientBuilder, never()).disableContentCompression(true);
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
        verify(apacheHttpClientBuilder).disableContentCompression(true);
    }

    @Test
    public void usesAnObjectMapperFromTheEnvironment() throws Exception {
        final Client client = builder.using(environment).build("test");

        assertThat(client.getConfiguration().isRegistered(JacksonMessageBodyProvider.class)).isTrue();
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void usesAnExecutorServiceFromTheEnvironment() throws Exception {
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
    public void usesACustomHttpClientMetricNameStrategy() {
        final HttpClientMetricNameStrategy customStrategy = HttpClientMetricNameStrategies.HOST_AND_METHOD;
        builder.using(customStrategy);
        verify(apacheHttpClientBuilder).using(customStrategy);
    }

    @Test
    public void usesACustomHttpRequestRetryHandler() {
        final DefaultHttpRequestRetryHandler customRetryHandler = new DefaultHttpRequestRetryHandler(2, true);
        builder.using(customRetryHandler);
        verify(apacheHttpClientBuilder).using(customRetryHandler);
    }

    @Test
    public void usesACustomDnsResolver() {
        final DnsResolver customDnsResolver = new SystemDefaultDnsResolver();
        builder.using(customDnsResolver);
        verify(apacheHttpClientBuilder).using(customDnsResolver);
    }

    @Test
    public void usesACustomConnectionFactoryRegistry() throws Exception {
        final SSLContext ctx = SSLContext.getInstance(SSLConnectionSocketFactory.TLS);
        ctx.init(null, new TrustManager[]{new X509TrustManager() {

            @Override
            public void checkClientTrusted(X509Certificate[] xcs, String string) throws CertificateException {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] xcs, String string) throws CertificateException {
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        }}, null);
        final Registry<ConnectionSocketFactory> customRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.getSocketFactory())
                .register("https", new SSLConnectionSocketFactory(ctx, new NoopHostnameVerifier()))
                .build();
        builder.using(customRegistry);
        verify(apacheHttpClientBuilder).using(customRegistry);
    }

    @Test
    public void usesACustomEnvironmentName() {
        final String userAgent = "Dropwizard Jersey Client";
        builder.name(userAgent);
        verify(apacheHttpClientBuilder).name(userAgent);
    }

    @Test
    public void usesACustomHttpRoutePlanner() {
       final HttpRoutePlanner customHttpRoutePlanner = new SystemDefaultRoutePlanner(new ProxySelector() {
           @Override
           public List<Proxy> select(URI uri) {
               return ImmutableList.of(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("192.168.53.12", 8080)));
           }

           @Override
           public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {

           }
       });
        builder.using(customHttpRoutePlanner);
        verify(apacheHttpClientBuilder).using(customHttpRoutePlanner);
    }

    @Test
    public void usesACustomCredentialsProvider(){
        CredentialsProvider customCredentialsProvider = new SystemDefaultCredentialsProvider();
        builder.using(customCredentialsProvider);
        verify(apacheHttpClientBuilder).using(customCredentialsProvider);
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
