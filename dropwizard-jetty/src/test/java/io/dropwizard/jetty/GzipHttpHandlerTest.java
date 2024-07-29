package io.dropwizard.jetty;

import io.dropwizard.util.DataSize;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpTester;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.LocalConnector;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.gzip.GzipHandler;
import org.eclipse.jetty.util.Callback;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.OutputStream;

import static org.assertj.core.api.Assertions.assertThat;

class GzipHttpHandlerTest {

    private static final String PLAIN_TEXT_UTF_8 = "text/plain;charset=UTF-8";

    private final GzipHandler gzipHandler;

    private final HttpTester.Request request = HttpTester.newRequest();
    private final Server server;
    private final LocalConnector connector;

    GzipHttpHandlerTest() {
        final GzipHandlerFactory gzipHandlerFactory = new GzipHandlerFactory();
        gzipHandlerFactory.setMinimumEntitySize(DataSize.bytes(0L));
        gzipHandler = gzipHandlerFactory.build(null);
        this.server = new Server();
        this.connector = new LocalConnector(server);
        server.addConnector(connector);
    }

    @BeforeEach
    void setUp() throws Exception {
        request.setHeader(HttpHeader.HOST.asString(), "localhost");
        request.setHeader("Connection", "close");
        request.setURI("/banner");

        gzipHandler.addIncludedMethods("POST");
        gzipHandler.setHandler(new EchoHandler());
        server.setHandler(gzipHandler);

        server.start();
    }

    @AfterEach
    void tearDown() throws Exception {
        server.stop();
    }

    @Test
    void testBadRequestStatusOnInvalidGzipBytes() throws Exception {
        request.setMethod("POST");
        request.setHeader(HttpHeader.CONTENT_TYPE.asString(), PLAIN_TEXT_UTF_8);
        request.setHeader(HttpHeader.CONTENT_ENCODING.asString(), "gzip");
        request.setContent("Invalid gzip bytes");

        HttpTester.Response response = HttpTester.parseResponse(connector.getResponse(request.generate()));
        assertThat(response.getStatus()).isEqualTo(400);
    }

    public static class EchoHandler extends Handler.Abstract {

        @Override
        public boolean handle(Request request, Response response, Callback callback) throws Exception {
            try (
                InputStream inputStream = Request.asInputStream(request);
                OutputStream outputStream = Response.asBufferedOutputStream(request, response)
            ) {
                inputStream.transferTo(outputStream);
            }

            callback.succeeded();
            return true;
        }
    }
}
