package io.dropwizard.http2;

import io.dropwizard.Configuration;
import io.dropwizard.logging.BootstrapLogging;
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
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class Http2CIntegrationTest {

    static {
        BootstrapLogging.bootstrap();
    }

    @Rule
    public DropwizardAppRule<Configuration> appRule = new DropwizardAppRule<>(
            FakeApplication.class, ResourceHelpers.resourceFilePath("test-http2c.yml"));

    private final HTTP2Client client = new HTTP2Client();

    @Before
    public void setUp() throws Exception {
        client.start();
    }

    @After
    public void tearDown() throws Exception {
        client.stop();
    }

    @Test
    public void testHttp2c() throws Exception {
        final String hostname = "127.0.0.1";
        final int port = appRule.getLocalPort();

        final FuturePromise<Session> sessionPromise = new FuturePromise<>();
        client.connect(new InetSocketAddress(hostname, port), new ServerSessionListener.Adapter(), sessionPromise);
        final Session session = sessionPromise.get(5, TimeUnit.SECONDS);

        final MetaData.Request request = new MetaData.Request("GET",
                new HttpURI("http://" + hostname + ":" + port + "/api/test"),
                HttpVersion.HTTP_2, new HttpFields());
        final ResponseListener listener = new ResponseListener();
        session.newStream(new HeadersFrame(request, null, true), new Promise.Adapter<>(), listener);
        assertThat(listener.getResponse()).isEqualTo(FakeApplication.HELLO_WORLD);
    }
}
