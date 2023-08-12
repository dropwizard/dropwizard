package io.dropwizard.jetty;

import io.dropwizard.util.DataSize;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.ee10.servlet.ServletTester;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpTester;
import org.eclipse.jetty.server.handler.gzip.GzipHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static org.assertj.core.api.Assertions.assertThat;

class GzipHandlerTest {

    private static final String PLAIN_TEXT_UTF_8 = "text/plain;charset=UTF-8";

    private final GzipHandler gzipHandler;

    private final ServletTester servletTester = new ServletTester();
    private final HttpTester.Request request = HttpTester.newRequest();

    GzipHandlerTest() {
        final GzipHandlerFactory gzipHandlerFactory = new GzipHandlerFactory();
        gzipHandlerFactory.setMinimumEntitySize(DataSize.bytes(0L));
        gzipHandler = gzipHandlerFactory.build(null);
    }

    @BeforeEach
    void setUp() throws Exception {
        request.setHeader(HttpHeader.HOST.asString(), "localhost");
        request.setHeader("Connection", "close");
        request.setURI("/banner");

        gzipHandler.addIncludedMethods("POST");
        servletTester.addServlet(BannerServlet.class, "/banner");
        servletTester.getContext().insertHandler(gzipHandler);
        servletTester.start();
    }

    @AfterEach
    void tearDown() throws Exception {
        servletTester.stop();
    }

    @Test
    void testCompressResponse() throws Exception {
        request.setMethod("GET");
        request.setHeader(HttpHeader.ACCEPT_ENCODING.asString(), "gzip");

        HttpTester.Response response = HttpTester.parseResponse(servletTester.getResponses(request.generate()));
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.get(HttpHeader.CONTENT_ENCODING)).isEqualTo("gzip");
        assertThat(response.get(HttpHeader.VARY)).isEqualTo(HttpHeader.ACCEPT_ENCODING.asString());
        assertThat(response.get(HttpHeader.CONTENT_TYPE)).isEqualToIgnoringCase(PLAIN_TEXT_UTF_8);

        try (GZIPInputStream is = new GZIPInputStream(new ByteArrayInputStream(response.getContentBytes()))) {
            assertThat(is).hasSameContentAs(getClass().getResourceAsStream("/assets/banner.txt"));
        }
    }

    @Test
    void testBadRequestStatusOnInvalidGzipBytes() throws Exception {
        request.setMethod("POST");
        request.setHeader(HttpHeader.CONTENT_TYPE.asString(), PLAIN_TEXT_UTF_8);
        request.setHeader(HttpHeader.CONTENT_ENCODING.asString(), "gzip");
        request.setContent("Invalid gzip bytes");

        HttpTester.Response response = HttpTester.parseResponse(servletTester.getResponses(request.generate()));
        assertThat(response.getStatus()).isEqualTo(400);
    }

    @Test
    void testDecompressRequest() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (GZIPOutputStream gz = new GZIPOutputStream(baos)) {
            gz.write(getClass().getResourceAsStream("/assets/new-banner.txt").readAllBytes());
        }

        setRequestPostGzipPlainText(baos.toByteArray());

        HttpTester.Response response = HttpTester.parseResponse(servletTester.getResponses(request.generate()));
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getContent()).isEqualTo("Banner has been updated");

    }

    private void setRequestPostPlainText(byte[] content) {
        request.setMethod("POST");
        request.setHeader(HttpHeader.CONTENT_TYPE.asString(), PLAIN_TEXT_UTF_8);
        request.setContent(content);
    }

    private void setRequestPostGzipPlainText(byte[] content) {
        setRequestPostPlainText(content);
        request.setHeader(HttpHeader.CONTENT_ENCODING.asString(), "gzip");
    }

    public static class BannerServlet extends HttpServlet {

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            resp.setCharacterEncoding(StandardCharsets.UTF_8.toString());
            resp.setContentType(PLAIN_TEXT_UTF_8);
            resp.getWriter().write(new String(getClass().getResourceAsStream("/assets/banner.txt").readAllBytes(), StandardCharsets.UTF_8));
        }

        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            assertThat(req.getHeader(HttpHeader.CONTENT_TYPE.asString())).isEqualToIgnoringCase(PLAIN_TEXT_UTF_8);
            assertThat(req.getHeader(HttpHeader.CONTENT_ENCODING.asString())).isNull();
            assertThat(req.getHeader(HttpHeader.CONTENT_LENGTH.asString())).isNull();
            assertThat(req.getContentLength()).isEqualTo(-1);
            assertThat(req.getContentLengthLong()).isEqualTo(-1L);

            assertThat(req.getInputStream())
                .hasSameContentAs(getClass().getResourceAsStream("/assets/new-banner.txt"));

            resp.setContentType(PLAIN_TEXT_UTF_8);
            resp.getWriter().write("Banner has been updated");
        }
    }
}
