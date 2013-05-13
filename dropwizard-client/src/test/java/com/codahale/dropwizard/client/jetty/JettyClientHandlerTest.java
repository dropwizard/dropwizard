package com.codahale.dropwizard.client.jetty;

import com.google.common.base.Charsets;
import com.google.common.io.ByteStreams;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Enumeration;

import static org.fest.assertions.api.Assertions.assertThat;

public class JettyClientHandlerTest {
    private final HttpClient httpClient = new HttpClient();
    private final Server server = new Server(0);
    private final Client client = new Client(new JettyClientHandler(httpClient));

    private String root;

    @Before
    public void setUp() throws Exception {
        server.setHandler(new AbstractHandler() {
            @Override
            public void handle(String target,
                               Request baseRequest,
                               HttpServletRequest request,
                               HttpServletResponse response) throws IOException {
                response.addHeader("Example", "yay");
                try (OutputStream output = response.getOutputStream();
                     OutputStreamWriter outputWriter = new OutputStreamWriter(output, Charsets.UTF_8);
                     PrintWriter writer = new PrintWriter(outputWriter)) {
                    writer.print("method = " + request.getMethod());

                    final String[] requestedHeaders = request.getParameterValues("header");
                    if (requestedHeaders != null) {
                        final Enumeration<String> names = request.getHeaderNames();
                        while (names.hasMoreElements()) {
                            final String name = names.nextElement();
                            if (Arrays.binarySearch(requestedHeaders, name) >= 0) {
                                final Enumeration<String> values = request.getHeaders(name);
                                while (values.hasMoreElements()) {
                                    writer.print(", " + name + '=' + values.nextElement());
                                }
                            }
                        }
                    }

                    final byte[] entity = ByteStreams.toByteArray(request.getInputStream());
                    if (entity.length > 0) {
                        writer.print(", entity = ");
                        writer.flush();
                        output.write(entity);
                    }
                }
            }
        });

        httpClient.start();
        server.start();

        this.root = "http://127.0.0.1:" + ((ServerConnector) server.getConnectors()[0]).getLocalPort() + '/';
    }

    @After
    public void tearDown() throws Exception {
        server.stop();
        httpClient.stop();
    }

    @Test
    public void handlesGetMethods() throws Exception {
        assertThat(client.resource(root).get(String.class))
                .isEqualTo("method = GET");
    }

    @Test
    public void handlesPostMethods() throws Exception {
        assertThat(client.resource(root).post(String.class, "poops"))
                .isEqualTo("method = POST, entity = poops");
    }

    @Test
    public void copiesRequestHeaders() throws Exception {
        assertThat(client.resource(root)
                         .queryParam("header", "Example")
                         .header("Example", "whee")
                         .post(String.class, "poops"))
                .isEqualTo("method = POST, Example=whee, entity = poops");
    }

    @Test
    public void copiesResponseHeaders() throws Exception {
        final ClientResponse response = client.resource(root).get(ClientResponse.class);

        assertThat(response.getHeaders().get("Example"))
                .containsOnly("yay");

        response.getEntity(String.class);
    }
}
