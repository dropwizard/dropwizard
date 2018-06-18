package io.dropwizard.servlets.assets;

import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpTester;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.servlet.ServletTester;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.annotation.Nullable;
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

    private static final ServletTester SERVLET_TESTER = new ServletTester();
    private final HttpTester.Request request = HttpTester.newRequest();
    @Nullable
    private HttpTester.Response response;

    @BeforeClass
    public static void startServletTester() throws Exception {
        SERVLET_TESTER.addServlet(DummyAssetServlet.class, DUMMY_SERVLET + '*');
        SERVLET_TESTER.addServlet(NoIndexAssetServlet.class, NOINDEX_SERVLET + '*');
        SERVLET_TESTER.addServlet(NoCharsetAssetServlet.class, NOCHARSET_SERVLET + '*');
        SERVLET_TESTER.addServlet(RootAssetServlet.class, ROOT_SERVLET + '*');
        SERVLET_TESTER.start();

        SERVLET_TESTER.getContext().getMimeTypes().addMimeMapping("mp4", "video/mp4");
        SERVLET_TESTER.getContext().getMimeTypes().addMimeMapping("m4a", "audio/mp4");
    }

    @AfterClass
    public static void stopServletTester() throws Exception {
        SERVLET_TESTER.stop();
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
        response = HttpTester.parseResponse(SERVLET_TESTER.getResponses(request.generate()));
        assertThat(response.getStatus())
                .isEqualTo(200);
        assertThat(response.getContent())
                .isEqualTo("HELLO THERE");
    }

    @Test
    public void servesCharset() throws Exception {
        request.setURI(DUMMY_SERVLET + "example.txt");
        response = HttpTester.parseResponse(SERVLET_TESTER.getResponses(request.generate()));
        assertThat(response.getStatus())
                .isEqualTo(200);
        assertThat(MimeTypes.CACHE.get(response.get(HttpHeader.CONTENT_TYPE)))
                .isEqualTo(MimeTypes.Type.TEXT_PLAIN_UTF_8);

        request.setURI(NOCHARSET_SERVLET + "example.txt");
        response = HttpTester.parseResponse(SERVLET_TESTER.getResponses(request.generate()));
        assertThat(response.getStatus())
                .isEqualTo(200);
        assertThat(response.get(HttpHeader.CONTENT_TYPE))
                .isEqualTo(MimeTypes.Type.TEXT_PLAIN.toString());
    }

    @Test
    public void servesFilesFromRootsWithSameName() throws Exception {
        request.setURI(DUMMY_SERVLET + "example2.txt");
        response = HttpTester.parseResponse(SERVLET_TESTER.getResponses(request.generate()));
        assertThat(response.getStatus())
                .isEqualTo(200);
        assertThat(response.getContent())
                .isEqualTo("HELLO THERE 2");
    }

    @Test
    public void servesFilesWithA200() throws Exception {
        response = HttpTester.parseResponse(SERVLET_TESTER.getResponses(request.generate()));
        assertThat(response.getStatus())
                .isEqualTo(200);
        assertThat(response.getContent())
                .isEqualTo("HELLO THERE");
    }

    @Test
    public void throws404IfTheAssetIsMissing() throws Exception {
        request.setURI(DUMMY_SERVLET + "doesnotexist.txt");

        response = HttpTester.parseResponse(SERVLET_TESTER.getResponses(request.generate()));
        assertThat(response.getStatus())
                .isEqualTo(404);
    }

    @Test
    public void consistentlyAssignsETags() throws Exception {
        response = HttpTester.parseResponse(SERVLET_TESTER.getResponses(request.generate()));
        final String firstEtag = response.get(HttpHeader.ETAG);

        response = HttpTester.parseResponse(SERVLET_TESTER.getResponses(request.generate()));
        final String secondEtag = response.get(HttpHeader.ETAG);

        assertThat(firstEtag)
                .isEqualTo("\"e7bd7e8e\"")
                .isEqualTo(secondEtag);
    }

    @Test
    public void assignsDifferentETagsForDifferentFiles() throws Exception {
        response = HttpTester.parseResponse(SERVLET_TESTER.getResponses(request.generate()));
        final String firstEtag = response.get(HttpHeader.ETAG);

        request.setURI(DUMMY_SERVLET + "foo.bar");
        response = HttpTester.parseResponse(SERVLET_TESTER.getResponses(request.generate()));
        final String secondEtag = response.get(HttpHeader.ETAG);

        assertThat(firstEtag)
                .isEqualTo("\"e7bd7e8e\"");
        assertThat(secondEtag)
                .isEqualTo("\"2684fb5a\"");
    }

    @Test
    public void supportsIfNoneMatchRequests() throws Exception {
        response = HttpTester.parseResponse(SERVLET_TESTER.getResponses(request.generate()));
        final String correctEtag = response.get(HttpHeader.ETAG);

        request.setHeader(HttpHeader.IF_NONE_MATCH.asString(), correctEtag);
        response = HttpTester.parseResponse(SERVLET_TESTER.getResponses(request.generate()));
        final int statusWithMatchingEtag = response.getStatus();

        request.setHeader(HttpHeader.IF_NONE_MATCH.asString(), correctEtag + "FOO");
        response = HttpTester.parseResponse(SERVLET_TESTER.getResponses(request.generate()));
        final int statusWithNonMatchingEtag = response.getStatus();

        assertThat(statusWithMatchingEtag)
                .isEqualTo(304);
        assertThat(statusWithNonMatchingEtag)
                .isEqualTo(200);
    }

    @Test
    public void consistentlyAssignsLastModifiedTimes() throws Exception {
        response = HttpTester.parseResponse(SERVLET_TESTER.getResponses(request.generate()));
        final long firstLastModifiedTime = response.getDateField(HttpHeader.LAST_MODIFIED.asString());

        response = HttpTester.parseResponse(SERVLET_TESTER.getResponses(request.generate()));
        final long secondLastModifiedTime = response.getDateField(HttpHeader.LAST_MODIFIED.asString());

        assertThat(firstLastModifiedTime)
                .isEqualTo(secondLastModifiedTime);
    }

    @Test
    public void supportsByteRangeForMedia() throws Exception {
        request.setURI(ROOT_SERVLET + "assets/foo.mp4");
        response = HttpTester.parseResponse(SERVLET_TESTER.getResponses(request
                .generate()));
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.get(HttpHeader.ACCEPT_RANGES)).isEqualTo("bytes");

        request.setURI(ROOT_SERVLET + "assets/foo.m4a");
        response = HttpTester.parseResponse(SERVLET_TESTER.getResponses(request
                .generate()));
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.get(HttpHeader.ACCEPT_RANGES)).isEqualTo("bytes");
    }

    @Test
    public void supportsFullByteRange() throws Exception {
        request.setURI(ROOT_SERVLET + "assets/example.txt");
        request.setHeader(HttpHeader.RANGE.asString(), "bytes=0-");
        response = HttpTester.parseResponse(SERVLET_TESTER.getResponses(request
                .generate()));
        assertThat(response.getStatus()).isEqualTo(206);
        assertThat(response.getContent()).isEqualTo("HELLO THERE");
        assertThat(response.get(HttpHeader.ACCEPT_RANGES)).isEqualTo("bytes");
        assertThat(response.get(HttpHeader.CONTENT_RANGE)).isEqualTo(
                "bytes 0-10/11");
    }

    @Test
    public void supportsCentralByteRange() throws Exception {
        request.setURI(ROOT_SERVLET + "assets/example.txt");
        request.setHeader(HttpHeader.RANGE.asString(), "bytes=4-8");
        response = HttpTester.parseResponse(SERVLET_TESTER.getResponses(request
                .generate()));
        assertThat(response.getStatus()).isEqualTo(206);
        assertThat(response.getContent()).isEqualTo("O THE");
        assertThat(response.get(HttpHeader.ACCEPT_RANGES)).isEqualTo("bytes");
        assertThat(response.get(HttpHeader.CONTENT_RANGE)).isEqualTo(
                "bytes 4-8/11");
        assertThat(response.get(HttpHeader.CONTENT_LENGTH)).isEqualTo("5");
    }

    @Test
    public void supportsFinalByteRange() throws Exception {
        request.setURI(ROOT_SERVLET + "assets/example.txt");
        request.setHeader(HttpHeader.RANGE.asString(), "bytes=10-10");
        response = HttpTester.parseResponse(SERVLET_TESTER.getResponses(request
                .generate()));
        assertThat(response.getStatus()).isEqualTo(206);
        assertThat(response.getContent()).isEqualTo("E");
        assertThat(response.get(HttpHeader.ACCEPT_RANGES)).isEqualTo("bytes");
        assertThat(response.get(HttpHeader.CONTENT_RANGE)).isEqualTo(
                "bytes 10-10/11");
        assertThat(response.get(HttpHeader.CONTENT_LENGTH)).isEqualTo("1");

        request.setHeader(HttpHeader.RANGE.asString(), "bytes=-1");
        response = HttpTester.parseResponse(SERVLET_TESTER.getResponses(request
                .generate()));
        assertThat(response.getStatus()).isEqualTo(206);
        assertThat(response.getContent()).isEqualTo("E");
        assertThat(response.get(HttpHeader.ACCEPT_RANGES)).isEqualTo("bytes");
        assertThat(response.get(HttpHeader.CONTENT_RANGE)).isEqualTo(
                "bytes 10-10/11");
        assertThat(response.get(HttpHeader.CONTENT_LENGTH)).isEqualTo("1");
    }

    @Test
    public void rejectsInvalidByteRanges() throws Exception {
        request.setURI(ROOT_SERVLET + "assets/example.txt");
        request.setHeader(HttpHeader.RANGE.asString(), "bytes=test");
        response = HttpTester.parseResponse(SERVLET_TESTER.getResponses(request
                .generate()));
        assertThat(response.getStatus()).isEqualTo(416);

        request.setHeader(HttpHeader.RANGE.asString(), "bytes=");
        response = HttpTester.parseResponse(SERVLET_TESTER.getResponses(request
                .generate()));
        assertThat(response.getStatus()).isEqualTo(416);

        request.setHeader(HttpHeader.RANGE.asString(), "bytes=1-infinity");
        response = HttpTester.parseResponse(SERVLET_TESTER.getResponses(request
                .generate()));
        assertThat(response.getStatus()).isEqualTo(416);

        request.setHeader(HttpHeader.RANGE.asString(), "test");
        response = HttpTester.parseResponse(SERVLET_TESTER.getResponses(request
                .generate()));
        assertThat(response.getStatus()).isEqualTo(416);
    }

    @Test
    public void supportsMultipleByteRanges() throws Exception {
        request.setURI(ROOT_SERVLET + "assets/example.txt");
        request.setHeader(HttpHeader.RANGE.asString(), "bytes=0-0,-1");
        response = HttpTester.parseResponse(SERVLET_TESTER.getResponses(request
                .generate()));
        assertThat(response.getStatus()).isEqualTo(206);
        assertThat(response.getContent()).isEqualTo("HE");
        assertThat(response.get(HttpHeader.ACCEPT_RANGES)).isEqualTo("bytes");
        assertThat(response.get(HttpHeader.CONTENT_RANGE)).isEqualTo(
                "bytes 0-0,10-10/11");
        assertThat(response.get(HttpHeader.CONTENT_LENGTH)).isEqualTo("2");

        request.setHeader(HttpHeader.RANGE.asString(), "bytes=5-6,7-10");
        response = HttpTester.parseResponse(SERVLET_TESTER.getResponses(request
                .generate()));
        assertThat(response.getStatus()).isEqualTo(206);
        assertThat(response.getContent()).isEqualTo(" THERE");
        assertThat(response.get(HttpHeader.ACCEPT_RANGES)).isEqualTo("bytes");
        assertThat(response.get(HttpHeader.CONTENT_RANGE)).isEqualTo(
                "bytes 5-6,7-10/11");
        assertThat(response.get(HttpHeader.CONTENT_LENGTH)).isEqualTo("6");
    }

    @Test
    public void supportsIfRangeMatchRequests() throws Exception {
        response = HttpTester.parseResponse(SERVLET_TESTER.getResponses(request
                .generate()));
        final String correctEtag = response.get(HttpHeader.ETAG);

        request.setHeader(HttpHeader.RANGE.asString(), "bytes=10-10");

        request.setHeader(HttpHeader.IF_RANGE.asString(), correctEtag);
        response = HttpTester.parseResponse(SERVLET_TESTER.getResponses(request
                .generate()));
        final int statusWithMatchingEtag = response.getStatus();

        request.setHeader(HttpHeader.IF_RANGE.asString(), correctEtag + "FOO");
        response = HttpTester.parseResponse(SERVLET_TESTER.getResponses(request
                .generate()));
        final int statusWithNonMatchingEtag = response.getStatus();

        assertThat(statusWithMatchingEtag).isEqualTo(206);
        assertThat(statusWithNonMatchingEtag).isEqualTo(200);
    }

    @Test
    public void supportsIfModifiedSinceRequests() throws Exception {
        response = HttpTester.parseResponse(SERVLET_TESTER.getResponses(request.generate()));
        final long lastModifiedTime = response.getDateField(HttpHeader.LAST_MODIFIED.asString());

        request.putDateField(HttpHeader.IF_MODIFIED_SINCE, lastModifiedTime);
        response = HttpTester.parseResponse(SERVLET_TESTER.getResponses(request.generate()));
        final int statusWithMatchingLastModifiedTime = response.getStatus();

        request.putDateField(HttpHeader.IF_MODIFIED_SINCE,
                          lastModifiedTime - 100);
        response = HttpTester.parseResponse(SERVLET_TESTER.getResponses(request.generate()));
        final int statusWithStaleLastModifiedTime = response.getStatus();

        request.putDateField(HttpHeader.IF_MODIFIED_SINCE,
                          lastModifiedTime + 100);
        response = HttpTester.parseResponse(SERVLET_TESTER.getResponses(request.generate()));
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
        response = HttpTester.parseResponse(SERVLET_TESTER.getResponses(request.generate()));
        assertThat(response.getStatus())
                .isEqualTo(200);
        assertThat(MimeTypes.CACHE.get(response.get(HttpHeader.CONTENT_TYPE)))
                .isEqualTo(MimeTypes.Type.TEXT_PLAIN_UTF_8);
    }

    @Test
    public void defaultsToHtml() throws Exception {
        request.setURI(DUMMY_SERVLET + "foo.bar");
        response = HttpTester.parseResponse(SERVLET_TESTER.getResponses(request.generate()));
        assertThat(response.getStatus())
                .isEqualTo(200);
        assertThat(MimeTypes.CACHE.get(response.get(HttpHeader.CONTENT_TYPE)))
                .isEqualTo(MimeTypes.Type.TEXT_HTML_UTF_8);
    }

    @Test
    public void servesIndexFilesByDefault() throws Exception {
        // Root directory listing:
        request.setURI(DUMMY_SERVLET);
        response = HttpTester.parseResponse(SERVLET_TESTER.getResponses(request.generate()));
        assertThat(response.getStatus())
                .isEqualTo(200);
        assertThat(response.getContent())
                .contains("/assets Index File");

        // Subdirectory listing:
        request.setURI(DUMMY_SERVLET + "some_directory");
        response = HttpTester.parseResponse(SERVLET_TESTER.getResponses(request.generate()));
        assertThat(response.getStatus())
                .isEqualTo(200);
        assertThat(response.getContent())
                .contains("/assets/some_directory Index File");

        // Subdirectory listing with slash:
        request.setURI(DUMMY_SERVLET + "some_directory/");
        response = HttpTester.parseResponse(SERVLET_TESTER.getResponses(request.generate()));
        assertThat(response.getStatus())
                .isEqualTo(200);
        assertThat(response.getContent())
                .contains("/assets/some_directory Index File");
    }

    @Test
    public void throwsA404IfNoIndexFileIsDefined() throws Exception {
        // Root directory listing:
        request.setURI(NOINDEX_SERVLET + '/');
        response = HttpTester.parseResponse(SERVLET_TESTER.getResponses(request.generate()));
        assertThat(response.getStatus())
                .isEqualTo(404);

        // Subdirectory listing:
        request.setURI(NOINDEX_SERVLET + "some_directory");
        response = HttpTester.parseResponse(SERVLET_TESTER.getResponses(request.generate()));
        assertThat(response.getStatus())
                .isEqualTo(404);

        // Subdirectory listing with slash:
        request.setURI(NOINDEX_SERVLET + "some_directory/");
        response = HttpTester.parseResponse(SERVLET_TESTER.getResponses(request.generate()));
        assertThat(response.getStatus())
                .isEqualTo(404);
    }

    @Test
    public void doesNotAllowOverridingUrls() throws Exception {
        request.setURI(DUMMY_SERVLET + "file:/etc/passwd");
        response = HttpTester.parseResponse(SERVLET_TESTER.getResponses(request.generate()));
        assertThat(response.getStatus())
                .isEqualTo(404);
    }

    @Test
    public void doesNotAllowOverridingPaths() throws Exception {
        request.setURI(DUMMY_SERVLET + "/etc/passwd");
        response = HttpTester.parseResponse(SERVLET_TESTER.getResponses(request.generate()));
        assertThat(response.getStatus())
                .isEqualTo(404);
    }

    @Test
    public void allowsEncodedAssetNames() throws Exception {
        request.setURI(DUMMY_SERVLET + "encoded%20example.txt");
        response = HttpTester.parseResponse(SERVLET_TESTER.getResponses(request.generate()));
        assertThat(response.getStatus())
                .isEqualTo(200);
    }
}
