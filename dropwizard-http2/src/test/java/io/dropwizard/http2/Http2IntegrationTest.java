package io.dropwizard.http2;

import com.google.common.base.Optional;
import io.dropwizard.Configuration;
import io.dropwizard.logging.BootstrapLogging;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpURI;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.http.MetaData;
import org.eclipse.jetty.http2.api.Session;
import org.eclipse.jetty.http2.api.server.ServerSessionListener;
import org.eclipse.jetty.http2.client.HTTP2Client;
import org.eclipse.jetty.http2.frames.HeadersFrame;
import org.eclipse.jetty.util.FuturePromise;
import org.eclipse.jetty.util.Promise;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class Http2IntegrationTest {

    static {
        BootstrapLogging.bootstrap();
    }

    @Rule
    public final DropwizardAppRule<Configuration> appRule = new DropwizardAppRule<>(
            FakeApplication.class, ResourceHelpers.resourceFilePath("test-http2.yml"),
            Optional.of("tls_http2"),
            ConfigOverride.config("tls_http2", "server.connector.keyStorePath",
                    ResourceHelpers.resourceFilePath("stores/http2_server.jks"))
    );

    private final HTTP2Client client = new HTTP2Client();
    private final SslContextFactory sslContextFactory = new SslContextFactory();

    @Before
    public void setUp() throws Exception {
        sslContextFactory.setTrustStorePath(ResourceHelpers.resourceFilePath("stores/http2_client.jts"));
        sslContextFactory.setTrustStorePassword("http2_client");
        sslContextFactory.setIncludeCipherSuites("TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256");
        client.addBean(sslContextFactory);
        client.start();
    }

    @After
    public void tearDown() throws Exception {
        client.stop();
    }

    @Test
    public void testHttp2() throws Exception {
        final String hostname = "127.0.0.1";
        final int port = appRule.getLocalPort();

        final FuturePromise<Session> sessionPromise = new FuturePromise<>();
        client.connect(sslContextFactory, new InetSocketAddress(hostname, port),
                new ServerSessionListener.Adapter(), sessionPromise);
        final Session session = sessionPromise.get(5, TimeUnit.SECONDS);

        final MetaData.Request request = new MetaData.Request("GET",
                new HttpURI("https://" + hostname + ":" + port + "/api/test"),
                HttpVersion.HTTP_2, new HttpFields());

        final ResponseListener listener = new ResponseListener();
        session.newStream(new HeadersFrame(request, null, true), new Promise.Adapter<>(), listener);
        assertThat(listener.getResponse()).isEqualTo(FakeApplication.HELLO_WORLD);
    }
}
