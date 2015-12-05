package io.dropwizard.jetty;

import com.google.common.io.ByteStreams;
import com.google.common.io.CharStreams;
import com.google.common.io.Resources;
import com.google.common.net.HttpHeaders;
import com.google.common.net.MediaType;
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
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.Inflater;

import static org.assertj.core.api.Assertions.assertThat;

public class BiDiGzipHandlerTest {

    private static final String PLAIN_TEXT_UTF_8 = MediaType.PLAIN_TEXT_UTF_8.toString().replace(" ", "");

    private final BiDiGzipHandler gzipHandler = new BiDiGzipHandler();

    private final ServletTester servletTester = new ServletTester();
    private final HttpTester.Request request = HttpTester.newRequest();

    @Before
    public void setUp() throws Exception {
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
        request.setURI("/banner");
        request.setHeader(HttpHeaders.ACCEPT_ENCODING, "gzip");

        HttpTester.Response response = HttpTester.parseResponse(servletTester.getResponses(request.generate()));
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.get(HttpHeader.CONTENT_ENCODING)).isEqualTo("gzip");
        assertThat(response.get(HttpHeader.VARY)).isEqualTo(HttpHeaders.ACCEPT_ENCODING);
        assertThat(response.get(HttpHeader.CONTENT_TYPE)).isEqualToIgnoringCase(PLAIN_TEXT_UTF_8);
        try (GZIPInputStream is = new GZIPInputStream(new ByteArrayInputStream(response.getContentBytes()))) {
            assertThat(ByteStreams.toByteArray(is)).isEqualTo(
                    Resources.toByteArray(Resources.getResource("assets/banner.txt")));
        }
    }

    @Test
    public void testDecompressRequest() throws Exception {
        request.setMethod("POST");
        request.setURI("/banner");
        request.setHeader(HttpHeaders.CONTENT_ENCODING, "gzip");
        request.setHeader(HttpHeaders.CONTENT_TYPE, PLAIN_TEXT_UTF_8);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (GZIPOutputStream gz = new GZIPOutputStream(baos)) {
            Resources.copy(Resources.getResource("assets/new-banner.txt"), gz);
        }
        request.setContent(baos.toByteArray());

        HttpTester.Response response = HttpTester.parseResponse(servletTester.getResponses(request.generate()));
        System.out.println(response.getStatus());
        System.out.println(response.getContent());

    }

    @Test
    public void testDecompressDeflateRequest() throws Exception {
        request.setMethod("POST");
        request.setURI("/banner");
        request.setHeader(HttpHeaders.CONTENT_ENCODING, "deflate");
        request.setHeader(HttpHeaders.CONTENT_TYPE, PLAIN_TEXT_UTF_8);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (DeflaterOutputStream deflate = new DeflaterOutputStream(baos)) {
            Resources.copy(Resources.getResource("assets/new-banner.txt"), deflate);
        }
        byte[] output = baos.toByteArray();
        request.setContent(output);

        // Decompress the bytes
        Inflater decompresser = new Inflater();
        decompresser.setInput(output);

        byte[] result = new byte[4096];
        int resultLength = decompresser.inflate(result);
        decompresser.end();

        // Decode the bytes into a String
        System.out.println(new String(result, 0, resultLength, "UTF-8"));

        HttpTester.Response response = HttpTester.parseResponse(servletTester.getResponses(request.generate()));
        System.out.println(response.getStatus());
        System.out.println(response.getContent());

    }

    public static class BannerServlet extends HttpServlet {

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            resp.setCharacterEncoding(StandardCharsets.UTF_8.toString());
            resp.setContentType(PLAIN_TEXT_UTF_8);
            Resources.asCharSource(Resources.getResource("assets/banner.txt"), StandardCharsets.UTF_8)
                    .copyTo(resp.getWriter());
        }

        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            assertThat(req.getHeader(HttpHeaders.CONTENT_TYPE)).isEqualToIgnoringCase(PLAIN_TEXT_UTF_8);
            assertThat(req.getHeader(HttpHeaders.CONTENT_ENCODING)).isNull();
            System.out.println(CharStreams.toString(req.getReader()));

            resp.setContentType(PLAIN_TEXT_UTF_8);
            resp.getWriter().write("Banner has been updated");
        }
    }
}
