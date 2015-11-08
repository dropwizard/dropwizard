package io.dropwizard.websockets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.io.Resources;
import java.io.IOException;
import java.net.URI;
import javax.websocket.Session;
import junit.framework.Assert;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.glassfish.tyrus.ext.client.java8.SessionBuilder;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class DropWizardWebsocketsTest {
    @BeforeClass
    public static void setUpClass() throws InterruptedException, IOException {
        Thread t = new Thread(GeneralUtils.rethrow(() -> new MyApp().run(new String[]{"server", Resources.getResource("server.yml").getPath()})));
        t.setDaemon(true);
        t.start();
        waitUrlAvailable(HEALTHCHECK);
    }
    private CloseableHttpClient client;
    private ObjectMapper om;

    @Before
    public void setUp() throws Exception {
        this.client = HttpClients.custom().setDefaultRequestConfig(RequestConfig.custom()
                .setSocketTimeout(5000).setConnectTimeout(5000).setConnectionRequestTimeout(5000)
                .build()).build();
        this.om = new ObjectMapper();
    }

    @After
    public void tearDown() throws Exception {
        client.close();
    }

    @Test
    public void testGet() throws IOException, InterruptedException, Exception {
        final int NUM = 10;
        for (int i = 0; i < NUM; i++)
            assertTrue(client.execute(new HttpGet(String.format("http://%s:%d/api?name=foo", LOCALHOST, PORT)), BASIC_RESPONSE_HANDLER).contains("foo"));
        ObjectNode json = om.readValue(client.execute(new HttpGet(METRICS_URL), BASIC_RESPONSE_HANDLER), ObjectNode.class);
        Assert.assertEquals(NUM,
                json.path("meters").path(MyApp.MyResource.class.getName() + ".get").path("count").asInt());

    }

    @Test
    public void testAnnotatedWebsocket() throws Exception {
        testWsMetrics(MyApp.AnnotatedEchoServer.class, "annotated-ws");
    }

    @Test
    public void testExtendsWebsocket() throws Exception {
        testWsMetrics(MyApp.EchoServer.class, "extends-ws");
    }

    private void testWsMetrics(final Class<?> klass, final String path) throws Exception {
        try (Session ws = new SessionBuilder()
                .uri(new URI(String.format("ws://%s:%d/%s", LOCALHOST, PORT, path)))
                .connect()) {
            for (int i = 0; i < 3; i++) {
                ws.getAsyncRemote().sendText("hello");
            }
            ObjectNode json = om.readValue(client.execute(new HttpGet(METRICS_URL), BASIC_RESPONSE_HANDLER), ObjectNode.class);
            // One open connection
            Assert.assertEquals(1,
                    json.path("counters").path(klass.getName() + ".openConnections").path("count").asInt());
        }
        ObjectNode json = om.readValue(client.execute(new HttpGet(METRICS_URL), BASIC_RESPONSE_HANDLER), ObjectNode.class);

        // Number of sessions that were opened
        Assert.assertEquals(1,
                json.path("timers").path(klass.getName()).path("count").asInt());

        // Length of session should be 5ms
        Assert.assertEquals(0.05, json.path("timers").path(klass.getName()).path("max").asDouble(), 1);

        // No Open connections
        Assert.assertEquals(0,
                json.path("counters").path(klass.getName() + ".openConnections").path("count").asInt());

        // Three text messages
        Assert.assertEquals(3,
                json.path("meters").path(klass.getName() + ".OnMessage").path("count").asInt());
    }

    public static void waitUrlAvailable(final String url) throws InterruptedException, IOException {
        for (int i = 0; i < 50; i++) {
            Thread.sleep(100);
            try {
                if (HttpClients.createDefault().execute(new HttpGet(url)).getStatusLine().getStatusCode() > -100)
                    break;
            } catch (HttpHostConnectException ex) {
            }
        }
    }

    private static final int ADMIN_PORT = 48081;
    private static final int PORT = 48080;
    private static final String LOCALHOST = "127.0.0.1";
    private static final String METRICS_URL = String.format("http://%s:%d/metrics", LOCALHOST, ADMIN_PORT);
    private static final BasicResponseHandler BASIC_RESPONSE_HANDLER = new BasicResponseHandler();
    private static final String HEALTHCHECK = String.format("http://%s:%d/healthcheck", LOCALHOST, ADMIN_PORT);

}
