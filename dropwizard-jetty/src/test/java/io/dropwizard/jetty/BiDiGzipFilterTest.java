package io.dropwizard.jetty;

import com.google.common.base.Charsets;
import com.google.common.io.ByteStreams;
import com.google.common.io.Resources;
import com.google.common.net.HttpHeaders;
import com.google.common.net.MediaType;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpTester;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletTester;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.DispatcherType;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.EnumSet;
import java.util.zip.*;

import static org.assertj.core.api.Assertions.assertThat;

public class BiDiGzipFilterTest {

    private static final String PLAIN_TEXT_UTF_8 = MediaType.PLAIN_TEXT_UTF_8.toString().replace(" ", "");

    private final BiDiGzipFilter gzipFilter = new BiDiGzipFilter();
    private final ServletTester servletTester = new ServletTester();

    @Before
    public void setUp() throws Exception {
        gzipFilter.setVary("Accept-Encoding");
        gzipFilter.setDeflateNoWrap(false);
        gzipFilter.setInflateNoWrap(false);

        servletTester.addServlet(BannerServlet.class, "/banner");
        servletTester.addFilter(new FilterHolder(gzipFilter), "/*", EnumSet.allOf(DispatcherType.class));
        servletTester.start();
    }

    @After
    public void tearDown() throws Exception {
        servletTester.stop();
    }

    @Test
    public void testCompressResponseWithGzip() throws Exception {
        final ByteBuffer request = getRequest("gzip");
        final HttpTester.Response response = run(request);

        assertSuccessfulGetResponse(response, "gzip");
        assertThat(decompress(response.getContentBytes(), "gzip")).isEqualTo(getResourceAsByteArray("assets/banner.txt"));
    }

    @Test
    public void testCompressResponseWithDeflate() throws Exception {
        final ByteBuffer request = getRequest("deflate");
        final HttpTester.Response response = run(request);

        assertSuccessfulGetResponse(response, "deflate");
        assertThat(decompress(response.getContentBytes(), "deflate")).isEqualTo(getResourceAsByteArray("assets/banner.txt"));
    }

    @Test
    public void testCompressResponseWithDeflateNoWrap() throws Exception {
        gzipFilter.setDeflateNoWrap(true);
        final ByteBuffer request = getRequest("deflate");
        final HttpTester.Response response = run(request);

        assertSuccessfulGetResponse(response, "deflate");
        assertThat(decompress(response.getContentBytes(), "deflateNoWrap")).isEqualTo(getResourceAsByteArray("assets/banner.txt"));
    }

    @Test
    public void testDecompressGzipRequest() throws Exception {
        final ByteBuffer request = postRequest(compress("assets/new-banner.txt", "gzip"), "gzip");
        final HttpTester.Response response = run(request);
        assertSuccessfulPostResponse(response);
    }

    @Test
    public void testDecompressDeflateRequest() throws Exception {
        final ByteBuffer request = postRequest(compress("assets/new-banner.txt", "deflate"), "deflate");
        final HttpTester.Response response = run(request);
        assertSuccessfulPostResponse(response);
    }

    @Test
    public void testDecompressDeflateRequestWithInflateNoWrap() throws Exception {
        gzipFilter.setInflateNoWrap(true);
        final ByteBuffer request = postRequest(compress("assets/new-banner.txt", "deflateNoWrap"), "deflate");

        final HttpTester.Response response = run(request);
        assertSuccessfulPostResponse(response);
    }

    private static ByteBuffer getRequest(String encoding) {
        final HttpTester.Request request = HttpTester.newRequest();
        request.setMethod("GET");
        request.setURI("/banner");
        request.setHeader(HttpHeaders.ACCEPT_ENCODING, encoding);
        return request.generate();
    }

    private static ByteBuffer postRequest(byte[] content, String encoding) {
        final HttpTester.Request request = HttpTester.newRequest();
        request.setMethod("POST");
        request.setURI("/banner");
        request.setHeader(HttpHeaders.CONTENT_ENCODING, encoding);
        request.setHeader(HttpHeaders.CONTENT_TYPE, PLAIN_TEXT_UTF_8);
        request.setContent(content);
        return request.generate();
    }

    private HttpTester.Response run(ByteBuffer request) throws Exception {
        return HttpTester.parseResponse(servletTester.getResponses(request));
    }

    private static void assertSuccessfulGetResponse(HttpTester.Response response, String encoding) {
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.get(HttpHeader.CONTENT_ENCODING)).isEqualTo(encoding);
        assertThat(response.get(HttpHeader.VARY)).isEqualTo(HttpHeaders.ACCEPT_ENCODING);
        assertThat(response.get(HttpHeader.CONTENT_TYPE)).isEqualToIgnoringCase(PLAIN_TEXT_UTF_8);
    }

    private static void assertSuccessfulPostResponse(HttpTester.Response response) {
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getContent()).isEqualTo("Banner has been updated");
    }

    private static byte[] getResourceAsByteArray(String resourceName) throws IOException {
        return Resources.toByteArray(Resources.getResource(resourceName));
    }

    private static byte[] compress(String resourceName, String encoding) throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (OutputStream os = createCompressionStream(baos, encoding)) {
            Resources.copy(Resources.getResource(resourceName), os);
        }
        return baos.toByteArray();
    }

    private static byte[] decompress(byte[] compressedData, String encoding) throws IOException {
        try (InputStream is = createDecompressionStream(new ByteArrayInputStream(compressedData), encoding)) {
            return ByteStreams.toByteArray(is);
        }
    }

    private static OutputStream createCompressionStream(ByteArrayOutputStream baos, String encoding)
            throws IOException {
        switch (encoding) {
            case "gzip":
                return new GZIPOutputStream(baos);
            case "deflate":
                return new DeflaterOutputStream(baos);
            case "deflateNoWrap":
                return new DeflaterOutputStream(baos, new Deflater(Deflater.DEFAULT_COMPRESSION, true));
            default:
                throw new IllegalArgumentException("Wrong encoding:" + encoding);
        }
    }

    private static InputStream createDecompressionStream(ByteArrayInputStream bais, String encoding)
            throws IOException {
        switch (encoding) {
            case "gzip":
                return new GZIPInputStream(bais);
            case "deflate":
                return new InflaterInputStream(bais);
            case "deflateNoWrap":
                return new InflaterInputStream(bais, new Inflater(true));
            default:
                throw new IllegalArgumentException("Wrong encoding:" + encoding);
        }
    }

    public static class BannerServlet extends HttpServlet {

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            resp.setCharacterEncoding(Charsets.UTF_8.toString());
            resp.setContentType(PLAIN_TEXT_UTF_8);
            Resources.asCharSource(Resources.getResource("assets/banner.txt"), Charsets.UTF_8).copyTo(resp.getWriter());
        }

        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            assertThat(req.getHeader(HttpHeaders.CONTENT_TYPE)).isEqualToIgnoringCase(PLAIN_TEXT_UTF_8);
            assertThat(req.getHeader(HttpHeaders.CONTENT_ENCODING)).isNull();
            assertThat(ByteStreams.toByteArray(req.getInputStream()))
                    .isEqualTo(getResourceAsByteArray("assets/new-banner.txt"));

            resp.setContentType(PLAIN_TEXT_UTF_8);
            resp.getWriter().write("Banner has been updated");
        }
    }
}
