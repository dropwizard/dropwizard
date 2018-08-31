package io.dropwizard.jetty;

import io.dropwizard.util.CharStreams;
import io.dropwizard.util.Resources;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpTester;
import org.eclipse.jetty.servlet.ServletTester;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static org.assertj.core.api.Assertions.assertThat;

public class BiDiGzipHandlerTest {

    private static final String PLAIN_TEXT_UTF_8 = "text/plain;charset=UTF-8";

    private final BiDiGzipHandler gzipHandler = new BiDiGzipHandler();

    private final ServletTester servletTester = new ServletTester();
    private final HttpTester.Request request = HttpTester.newRequest();

    @Before
    public void setUp() throws Exception {
        request.setHeader(HttpHeader.HOST.asString(), "localhost");
        request.setHeader("Connection", "close");
        request.setURI("/banner");

        gzipHandler.setExcludedAgentPatterns();
        gzipHandler.addIncludedMethods("POST");

        servletTester.addServlet(BannerServlet.class, "/banner");
        servletTester.getContext().setGzipHandler(gzipHandler);
        servletTester.start();
    }

    @After
    public void tearDown() throws Exception {
        servletTester.stop();
    }

    @Test
    public void testCompressResponse() throws Exception {
        request.setMethod("GET");
        request.setHeader(HttpHeader.ACCEPT_ENCODING.asString(), "gzip");

        HttpTester.Response response = HttpTester.parseResponse(servletTester.getResponses(request.generate()));
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.get(HttpHeader.CONTENT_ENCODING)).isEqualTo("gzip");
        assertThat(response.get(HttpHeader.VARY)).isEqualTo(HttpHeader.ACCEPT_ENCODING.asString());
        assertThat(response.get(HttpHeader.CONTENT_TYPE)).isEqualToIgnoringCase(PLAIN_TEXT_UTF_8);

        final byte[] expectedBytes = Resources.toByteArray(Resources.getResource("assets/banner.txt"));
        try (GZIPInputStream is = new GZIPInputStream(new ByteArrayInputStream(response.getContentBytes()));
             ByteArrayInputStream expected = new ByteArrayInputStream(expectedBytes)) {
            assertThat(is).hasSameContentAs(expected);
        }
    }

    @Test
    public void testDecompressRequest() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (GZIPOutputStream gz = new GZIPOutputStream(baos)) {
            Resources.copy(Resources.getResource("assets/new-banner.txt"), gz);
        }

        setRequestPostGzipPlainText(baos.toByteArray());

        HttpTester.Response response = HttpTester.parseResponse(servletTester.getResponses(request.generate()));
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getContent()).isEqualTo("Banner has been updated");

    }

    @Test
    public void testDecompressBadRequest() throws Exception {
        setRequestPostGzipPlainText("Non-gziped content".getBytes("UTF-8"));
        testBadRequest("Invalid gzip data in request");
    }

    @Test
    public void testDecompressDeflateRequestGzipIncompatible() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (DeflaterOutputStream deflate = new DeflaterOutputStream(baos)) {
            Resources.copy(Resources.getResource("assets/new-banner.txt"), deflate);
        }
        setRequestPostDeflatePlainText(baos.toByteArray());
        gzipHandler.setInflateNoWrap(false);

        HttpTester.Response response = HttpTester.parseResponse(servletTester.getResponses(request.generate()));
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getContent()).isEqualTo("Banner has been updated");
    }

    @Test
    public void testDecompressDeflateRequestGzipCompatible() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (DeflaterOutputStream deflate = new DeflaterOutputStream(baos,  new Deflater(-1, true))) {
            Resources.copy(Resources.getResource("assets/new-banner.txt"), deflate);
        }

        setRequestPostDeflatePlainText(baos.toByteArray());

        HttpTester.Response response = HttpTester.parseResponse(servletTester.getResponses(request.generate()));
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getContent()).isEqualTo("Banner has been updated");
    }

    @Test
    public void testDecompressDeflateBadRequest() throws Exception {
        setRequestPostDeflatePlainText("Non-deflate content".getBytes("UTF-8"));
        testBadRequest("Invalid deflate data in request");
    }

    private void testBadRequest(String responseContains) throws Exception {
        HttpTester.Response response = HttpTester.parseResponse(servletTester.getResponses(request.generate()));
        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getContent()).contains(responseContains);
    }

    private void setRequestPostPlainText(byte[] content) {
        request.setMethod("POST");
        request.setHeader(HttpHeader.CONTENT_TYPE.asString(), PLAIN_TEXT_UTF_8);
        request.setContent(content);
    }

    private void setRequestPostDeflatePlainText(byte[] content) {
        setRequestPostPlainText(content);
        request.setHeader(HttpHeader.CONTENT_ENCODING.asString(), "deflate");
    }

    private void setRequestPostGzipPlainText(byte[] content) {
        setRequestPostPlainText(content);
        request.setHeader(HttpHeader.CONTENT_ENCODING.asString(), "gzip");
    }

    @SuppressWarnings("serial")
    public static class BannerServlet extends HttpServlet {

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            resp.setCharacterEncoding(StandardCharsets.UTF_8.toString());
            resp.setContentType(PLAIN_TEXT_UTF_8);
            resp.getWriter().write(Resources.toString(Resources.getResource("assets/banner.txt"), StandardCharsets.UTF_8));
        }

        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            assertThat(req.getHeader(HttpHeader.CONTENT_TYPE.asString())).isEqualToIgnoringCase(PLAIN_TEXT_UTF_8);
            assertThat(req.getHeader(HttpHeader.CONTENT_ENCODING.asString())).isNull();
            assertThat(req.getHeader(HttpHeader.CONTENT_LENGTH.asString())).isNull();
            assertThat(req.getContentLength()).isEqualTo(-1);
            assertThat(req.getContentLengthLong()).isEqualTo(-1L);

            assertThat(CharStreams.toString(req.getReader())).isEqualTo(Resources.toString(
                Resources.getResource("assets/new-banner.txt"), StandardCharsets.UTF_8));

            resp.setContentType(PLAIN_TEXT_UTF_8);
            resp.getWriter().write("Banner has been updated");
        }
    }
}
