package io.dropwizard.servlets.assets;

import com.google.common.net.HttpHeaders;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpTester;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.servlet.ServletTester;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

public class AssetServletTest {
    private static final String DUMMY_SERVLET = "/dummy_servlet/";
    private static final String NOINDEX_SERVLET = "/noindex_servlet/";
    private static final String NOCHARSET_SERVLET = "/nocharset_servlet/";
    private static final String ROOT_SERVLET = "/";
    private static final String RESOURCE_PATH = "/assets";

    // ServletTester expects to be able to instantiate the servlet with zero arguments

    public static class DummyAssetServlet extends AssetServlet {
        private static final long serialVersionUID = -1L;

        public DummyAssetServlet() {
            super(RESOURCE_PATH, DUMMY_SERVLET, "index.htm", StandardCharsets.UTF_8);
        }
    }

    public static class NoIndexAssetServlet extends AssetServlet {
        private static final long serialVersionUID = -1L;

        public NoIndexAssetServlet() {
            super(RESOURCE_PATH, DUMMY_SERVLET, null, StandardCharsets.UTF_8);
        }
    }

    public static class RootAssetServlet extends AssetServlet {
        private static final long serialVersionUID = 1L;

        public RootAssetServlet() {
            super("/", ROOT_SERVLET, null, StandardCharsets.UTF_8);
        }
    }

    public static class NoCharsetAssetServlet extends AssetServlet {
        private static final long serialVersionUID = 1L;

        public NoCharsetAssetServlet() {
            super(RESOURCE_PATH, NOCHARSET_SERVLET, null, null);
        }
    }

    private static final ServletTester servletTester = new ServletTester();
    private final HttpTester.Request request = HttpTester.newRequest();
    private HttpTester.Response response;

    @BeforeClass
    public static void startServletTester() throws Exception {
        servletTester.addServlet(DummyAssetServlet.class, DUMMY_SERVLET + '*');
        servletTester.addServlet(NoIndexAssetServlet.class, NOINDEX_SERVLET + '*');
        servletTester.addServlet(NoCharsetAssetServlet.class, NOCHARSET_SERVLET + '*');
        servletTester.addServlet(RootAssetServlet.class, ROOT_SERVLET + '*');
        servletTester.start();

        servletTester.getContext().getMimeTypes().addMimeMapping("mp4", "video/mp4");
        servletTester.getContext().getMimeTypes().addMimeMapping("m4a", "audio/mp4");
    }

    @AfterClass
    public static void stopServletTester() throws Exception {
        servletTester.stop();
    }

    @Before
    public void setUp() throws Exception {
        request.setMethod("GET");
        request.setURI(DUMMY_SERVLET + "example.txt");
        request.setVersion(HttpVersion.HTTP_1_0);
    }

    @Test
    public void servesFilesMappedToRoot() throws Exception {
        request.setURI(ROOT_SERVLET + "assets/example.txt");
        response = HttpTester.parseResponse(servletTester.getResponses(request.generate()));
        assertThat(response.getStatus())
                .isEqualTo(200);
        assertThat(response.getContent())
                .isEqualTo("HELLO THERE");
    }

    @Test
    public void servesCharset() throws Exception {
        request.setURI(DUMMY_SERVLET + "example.txt");
        response = HttpTester.parseResponse(servletTester.getResponses(request.generate()));
        assertThat(response.getStatus())
                .isEqualTo(200);
        assertThat(MimeTypes.CACHE.get(response.get(HttpHeader.CONTENT_TYPE)))
                .isEqualTo(MimeTypes.Type.TEXT_PLAIN_UTF_8);

        request.setURI(NOCHARSET_SERVLET + "example.txt");
        response = HttpTester.parseResponse(servletTester.getResponses(request.generate()));
        assertThat(response.getStatus())
                .isEqualTo(200);
        assertThat(response.get(HttpHeader.CONTENT_TYPE))
                .isEqualTo(MimeTypes.Type.TEXT_PLAIN.toString());
    }

    @Test
    public void servesFilesFromRootsWithSameName() throws Exception {
        request.setURI(DUMMY_SERVLET + "example2.txt");
        response = HttpTester.parseResponse(servletTester.getResponses(request.generate()));
        assertThat(response.getStatus())
                .isEqualTo(200);
        assertThat(response.getContent())
                .isEqualTo("HELLO THERE 2");
    }

    @Test
    public void servesFilesWithA200() throws Exception {
        response = HttpTester.parseResponse(servletTester.getResponses(request.generate()));
        assertThat(response.getStatus())
                .isEqualTo(200);
        assertThat(response.getContent())
                .isEqualTo("HELLO THERE");
    }

    @Test
    public void throws404IfTheAssetIsMissing() throws Exception {
        request.setURI(DUMMY_SERVLET + "doesnotexist.txt");

        response = HttpTester.parseResponse(servletTester.getResponses(request.generate()));
        assertThat(response.getStatus())
                .isEqualTo(404);
    }

    @Test
    public void consistentlyAssignsETags() throws Exception {
        response = HttpTester.parseResponse(servletTester.getResponses(request.generate()));
        final String firstEtag = response.get(HttpHeaders.ETAG);

        response = HttpTester.parseResponse(servletTester.getResponses(request.generate()));
        final String secondEtag = response.get(HttpHeaders.ETAG);

        assertThat(firstEtag)
                .isEqualTo("\"174a6dd7325e64c609eab14ab1d30b86\"")
                .isEqualTo(secondEtag);
    }

    @Test
    public void assignsDifferentETagsForDifferentFiles() throws Exception {
        response = HttpTester.parseResponse(servletTester.getResponses(request.generate()));
        final String firstEtag = response.get(HttpHeaders.ETAG);

        request.setURI(DUMMY_SERVLET + "foo.bar");
        response = HttpTester.parseResponse(servletTester.getResponses(request.generate()));
        final String secondEtag = response.get(HttpHeaders.ETAG);

        assertThat(firstEtag)
                .isEqualTo("\"174a6dd7325e64c609eab14ab1d30b86\"");
        assertThat(secondEtag)
                .isEqualTo("\"378521448e0a3893a209edcc686d91ce\"");
    }

    @Test
    public void supportsIfNoneMatchRequests() throws Exception {
        response = HttpTester.parseResponse(servletTester.getResponses(request.generate()));
        final String correctEtag = response.get(HttpHeaders.ETAG);

        request.setHeader(HttpHeaders.IF_NONE_MATCH, correctEtag);
        response = HttpTester.parseResponse(servletTester.getResponses(request.generate()));
        final int statusWithMatchingEtag = response.getStatus();

        request.setHeader(HttpHeaders.IF_NONE_MATCH, correctEtag + "FOO");
        response = HttpTester.parseResponse(servletTester.getResponses(request.generate()));
        final int statusWithNonMatchingEtag = response.getStatus();

        assertThat(statusWithMatchingEtag)
                .isEqualTo(304);
        assertThat(statusWithNonMatchingEtag)
                .isEqualTo(200);
    }

    @Test
    public void consistentlyAssignsLastModifiedTimes() throws Exception {
        response = HttpTester.parseResponse(servletTester.getResponses(request.generate()));
        final long firstLastModifiedTime = response.getDateField(HttpHeaders.LAST_MODIFIED);

        response = HttpTester.parseResponse(servletTester.getResponses(request.generate()));
        final long secondLastModifiedTime = response.getDateField(HttpHeaders.LAST_MODIFIED);

        assertThat(firstLastModifiedTime)
                .isEqualTo(secondLastModifiedTime);
    }

    @Test
    public void supportsByteRangeForMedia() throws Exception {
        request.setURI(ROOT_SERVLET + "assets/foo.mp4");
        response = HttpTester.parseResponse(servletTester.getResponses(request
                .generate()));
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.get(HttpHeaders.ACCEPT_RANGES)).isEqualTo("bytes");

        request.setURI(ROOT_SERVLET + "assets/foo.m4a");
        response = HttpTester.parseResponse(servletTester.getResponses(request
                .generate()));
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.get(HttpHeaders.ACCEPT_RANGES)).isEqualTo("bytes");
    }

    @Test
    public void supportsFullByteRange() throws Exception {
        request.setURI(ROOT_SERVLET + "assets/example.txt");
        request.setHeader(HttpHeaders.RANGE, "bytes=0-");
        response = HttpTester.parseResponse(servletTester.getResponses(request
                .generate()));
        assertThat(response.getStatus()).isEqualTo(206);
        assertThat(response.getContent()).isEqualTo("HELLO THERE");
        assertThat(response.get(HttpHeaders.ACCEPT_RANGES)).isEqualTo("bytes");
        assertThat(response.get(HttpHeaders.CONTENT_RANGE)).isEqualTo(
                "bytes 0-10/11");
    }

    @Test
    public void supportsCentralByteRange() throws Exception {
        request.setURI(ROOT_SERVLET + "assets/example.txt");
        request.setHeader(HttpHeaders.RANGE, "bytes=4-8");
        response = HttpTester.parseResponse(servletTester.getResponses(request
                .generate()));
        assertThat(response.getStatus()).isEqualTo(206);
        assertThat(response.getContent()).isEqualTo("O THE");
        assertThat(response.get(HttpHeaders.ACCEPT_RANGES)).isEqualTo("bytes");
        assertThat(response.get(HttpHeaders.CONTENT_RANGE)).isEqualTo(
                "bytes 4-8/11");
        assertThat(response.get(HttpHeaders.CONTENT_LENGTH)).isEqualTo("5");
    }

    @Test
    public void supportsFinalByteRange() throws Exception {
        request.setURI(ROOT_SERVLET + "assets/example.txt");
        request.setHeader(HttpHeaders.RANGE, "bytes=10-10");
        response = HttpTester.parseResponse(servletTester.getResponses(request
                .generate()));
        assertThat(response.getStatus()).isEqualTo(206);
        assertThat(response.getContent()).isEqualTo("E");
        assertThat(response.get(HttpHeaders.ACCEPT_RANGES)).isEqualTo("bytes");
        assertThat(response.get(HttpHeaders.CONTENT_RANGE)).isEqualTo(
                "bytes 10-10/11");
        assertThat(response.get(HttpHeaders.CONTENT_LENGTH)).isEqualTo("1");

        request.setHeader(HttpHeaders.RANGE, "bytes=-1");
        response = HttpTester.parseResponse(servletTester.getResponses(request
                .generate()));
        assertThat(response.getStatus()).isEqualTo(206);
        assertThat(response.getContent()).isEqualTo("E");
        assertThat(response.get(HttpHeaders.ACCEPT_RANGES)).isEqualTo("bytes");
        assertThat(response.get(HttpHeaders.CONTENT_RANGE)).isEqualTo(
                "bytes 10-10/11");
        assertThat(response.get(HttpHeaders.CONTENT_LENGTH)).isEqualTo("1");
    }

    @Test
    public void rejectsInvalidByteRanges() throws Exception {
        request.setURI(ROOT_SERVLET + "assets/example.txt");
        request.setHeader(HttpHeaders.RANGE, "bytes=test");
        response = HttpTester.parseResponse(servletTester.getResponses(request
                .generate()));
        assertThat(response.getStatus()).isEqualTo(416);

        request.setHeader(HttpHeaders.RANGE, "bytes=");
        response = HttpTester.parseResponse(servletTester.getResponses(request
                .generate()));
        assertThat(response.getStatus()).isEqualTo(416);

        request.setHeader(HttpHeaders.RANGE, "bytes=1-infinity");
        response = HttpTester.parseResponse(servletTester.getResponses(request
                .generate()));
        assertThat(response.getStatus()).isEqualTo(416);

        request.setHeader(HttpHeaders.RANGE, "test");
        response = HttpTester.parseResponse(servletTester.getResponses(request
                .generate()));
        assertThat(response.getStatus()).isEqualTo(416);
    }

    @Test
    public void supportsMultipleByteRanges() throws Exception {
        request.setURI(ROOT_SERVLET + "assets/example.txt");
        request.setHeader(HttpHeaders.RANGE, "bytes=0-0,-1");
        response = HttpTester.parseResponse(servletTester.getResponses(request
                .generate()));
        assertThat(response.getStatus()).isEqualTo(206);
        assertThat(response.getContent()).isEqualTo("HE");
        assertThat(response.get(HttpHeaders.ACCEPT_RANGES)).isEqualTo("bytes");
        assertThat(response.get(HttpHeaders.CONTENT_RANGE)).isEqualTo(
                "bytes 0-0,10-10/11");
        assertThat(response.get(HttpHeaders.CONTENT_LENGTH)).isEqualTo("2");

        request.setHeader(HttpHeaders.RANGE, "bytes=5-6,7-10");
        response = HttpTester.parseResponse(servletTester.getResponses(request
                .generate()));
        assertThat(response.getStatus()).isEqualTo(206);
        assertThat(response.getContent()).isEqualTo(" THERE");
        assertThat(response.get(HttpHeaders.ACCEPT_RANGES)).isEqualTo("bytes");
        assertThat(response.get(HttpHeaders.CONTENT_RANGE)).isEqualTo(
                "bytes 5-6,7-10/11");
        assertThat(response.get(HttpHeaders.CONTENT_LENGTH)).isEqualTo("6");
    }

    @Test
    public void supportsIfRangeMatchRequests() throws Exception {
        response = HttpTester.parseResponse(servletTester.getResponses(request
                .generate()));
        final String correctEtag = response.get(HttpHeaders.ETAG);

        request.setHeader(HttpHeaders.RANGE, "bytes=10-10");

        request.setHeader(HttpHeaders.IF_RANGE, correctEtag);
        response = HttpTester.parseResponse(servletTester.getResponses(request
                .generate()));
        final int statusWithMatchingEtag = response.getStatus();

        request.setHeader(HttpHeaders.IF_RANGE, correctEtag + "FOO");
        response = HttpTester.parseResponse(servletTester.getResponses(request
                .generate()));
        final int statusWithNonMatchingEtag = response.getStatus();

        assertThat(statusWithMatchingEtag).isEqualTo(206);
        assertThat(statusWithNonMatchingEtag).isEqualTo(200);
    }

    @Test
    public void supportsIfModifiedSinceRequests() throws Exception {
        response = HttpTester.parseResponse(servletTester.getResponses(request.generate()));
        final long lastModifiedTime = response.getDateField(HttpHeaders.LAST_MODIFIED);

        request.putDateField(HttpHeaders.IF_MODIFIED_SINCE, lastModifiedTime);
        response = HttpTester.parseResponse(servletTester.getResponses(request.generate()));
        final int statusWithMatchingLastModifiedTime = response.getStatus();

        request.putDateField(HttpHeaders.IF_MODIFIED_SINCE,
                          lastModifiedTime - 100);
        response = HttpTester.parseResponse(servletTester.getResponses(request.generate()));
        final int statusWithStaleLastModifiedTime = response.getStatus();

        request.putDateField(HttpHeaders.IF_MODIFIED_SINCE,
                          lastModifiedTime + 100);
        response = HttpTester.parseResponse(servletTester.getResponses(request.generate()));
        final int statusWithRecentLastModifiedTime = response.getStatus();

        assertThat(statusWithMatchingLastModifiedTime)
                .isEqualTo(304);
        assertThat(statusWithStaleLastModifiedTime)
                .isEqualTo(200);
        assertThat(statusWithRecentLastModifiedTime)
                .isEqualTo(304);
    }

    @Test
    public void guessesMimeTypes() throws Exception {
        response = HttpTester.parseResponse(servletTester.getResponses(request.generate()));
        assertThat(response.getStatus())
                .isEqualTo(200);
        assertThat(MimeTypes.CACHE.get(response.get(HttpHeader.CONTENT_TYPE)))
                .isEqualTo(MimeTypes.Type.TEXT_PLAIN_UTF_8);
    }

    @Test
    public void defaultsToHtml() throws Exception {
        request.setURI(DUMMY_SERVLET + "foo.bar");
        response = HttpTester.parseResponse(servletTester.getResponses(request.generate()));
        assertThat(response.getStatus())
                .isEqualTo(200);
        assertThat(MimeTypes.CACHE.get(response.get(HttpHeader.CONTENT_TYPE)))
                .isEqualTo(MimeTypes.Type.TEXT_HTML_UTF_8);
    }

    @Test
    public void servesIndexFilesByDefault() throws Exception {
        // Root directory listing:
        request.setURI(DUMMY_SERVLET);
        response = HttpTester.parseResponse(servletTester.getResponses(request.generate()));
        assertThat(response.getStatus())
                .isEqualTo(200);
        assertThat(response.getContent())
                .contains("/assets Index File");

        // Subdirectory listing:
        request.setURI(DUMMY_SERVLET + "some_directory");
        response = HttpTester.parseResponse(servletTester.getResponses(request.generate()));
        assertThat(response.getStatus())
                .isEqualTo(200);
        assertThat(response.getContent())
                .contains("/assets/some_directory Index File");

        // Subdirectory listing with slash:
        request.setURI(DUMMY_SERVLET + "some_directory/");
        response = HttpTester.parseResponse(servletTester.getResponses(request.generate()));
        assertThat(response.getStatus())
                .isEqualTo(200);
        assertThat(response.getContent())
                .contains("/assets/some_directory Index File");
    }

    @Test
    public void throwsA404IfNoIndexFileIsDefined() throws Exception {
        // Root directory listing:
        request.setURI(NOINDEX_SERVLET + '/');
        response = HttpTester.parseResponse(servletTester.getResponses(request.generate()));
        assertThat(response.getStatus())
                .isEqualTo(404);

        // Subdirectory listing:
        request.setURI(NOINDEX_SERVLET + "some_directory");
        response = HttpTester.parseResponse(servletTester.getResponses(request.generate()));
        assertThat(response.getStatus())
                .isEqualTo(404);

        // Subdirectory listing with slash:
        request.setURI(NOINDEX_SERVLET + "some_directory/");
        response = HttpTester.parseResponse(servletTester.getResponses(request.generate()));
        assertThat(response.getStatus())
                .isEqualTo(404);
    }

    @Test
    public void doesNotAllowOverridingUrls() throws Exception {
        request.setURI(DUMMY_SERVLET + "file:/etc/passwd");
        response = HttpTester.parseResponse(servletTester.getResponses(request.generate()));
        assertThat(response.getStatus())
                .isEqualTo(404);
    }

    @Test
    public void doesNotAllowOverridingPaths() throws Exception {
        request.setURI(DUMMY_SERVLET + "/etc/passwd");
        response = HttpTester.parseResponse(servletTester.getResponses(request.generate()));
        assertThat(response.getStatus())
                .isEqualTo(404);
    }

    @Test
    public void allowsEncodedAssetNames() throws Exception {
        request.setURI(DUMMY_SERVLET + "encoded%20example.txt");
        response = HttpTester.parseResponse(servletTester.getResponses(request.generate()));
        assertThat(response.getStatus())
                .isEqualTo(200);
    }
}
