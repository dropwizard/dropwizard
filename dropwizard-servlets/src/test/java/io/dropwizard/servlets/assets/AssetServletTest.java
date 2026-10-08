package io.dropwizard.servlets.assets;

import org.eclipse.jetty.ee10.servlet.ServletTester;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpTester;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.http.MultiPart;
import org.eclipse.jetty.http.MultiPartByteRanges;
import org.eclipse.jetty.io.content.ByteBufferContentSource;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;

public class AssetServletTest {
    private static final String DUMMY_SERVLET = "/dummy_servlet/";
    private static final String NOINDEX_SERVLET = "/noindex_servlet/";
    private static final String NOCHARSET_SERVLET = "/nocharset_servlet/";
    private static final String NOMEDIATYPE_SERVLET = "/nomediatype_servlet/";
    private static final String MEDIATYPE_SERVLET = "/mediatype_servlet/";
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

    public static class NoDefaultMediaTypeAssetServlet extends AssetServlet {
        private static final long serialVersionUID = 1L;

        public NoDefaultMediaTypeAssetServlet() {
            super(RESOURCE_PATH, NOMEDIATYPE_SERVLET, null, null, StandardCharsets.UTF_8);
        }
    }

    public static class DefaultMediaTypeAssetServlet extends AssetServlet {
        private static final long serialVersionUID = 1L;

        public DefaultMediaTypeAssetServlet() {
            super(RESOURCE_PATH, MEDIATYPE_SERVLET, null, "text/plain", StandardCharsets.UTF_8);
        }
    }

    private static final ServletTester SERVLET_TESTER = new ServletTester();
    private final HttpTester.Request request = HttpTester.newRequest();
    private HttpTester.@Nullable Response response;

    @BeforeAll
    public static void startServletTester() throws Exception {
        SERVLET_TESTER.addServlet(DummyAssetServlet.class, DUMMY_SERVLET + '*');
        SERVLET_TESTER.addServlet(NoIndexAssetServlet.class, NOINDEX_SERVLET + '*');
        SERVLET_TESTER.addServlet(NoCharsetAssetServlet.class, NOCHARSET_SERVLET + '*');
        SERVLET_TESTER.addServlet(NoDefaultMediaTypeAssetServlet.class, NOMEDIATYPE_SERVLET + '*');
        SERVLET_TESTER.addServlet(DefaultMediaTypeAssetServlet.class, MEDIATYPE_SERVLET + '*');
        SERVLET_TESTER.addServlet(RootAssetServlet.class, ROOT_SERVLET + '*');
        SERVLET_TESTER.start();

        SERVLET_TESTER.getContext().getMimeTypes().addMimeMapping("mp4", "video/mp4");
        SERVLET_TESTER.getContext().getMimeTypes().addMimeMapping("m4a", "audio/mp4");
    }

    @AfterAll
    public static void stopServletTester() throws Exception {
        SERVLET_TESTER.stop();
    }

    @BeforeEach
    void setUp() throws Exception {
        request.setMethod("GET");
        request.setURI(DUMMY_SERVLET + "example.txt");
        request.setVersion(HttpVersion.HTTP_1_0);
    }

    @Test
    void servesFilesMappedToRoot() throws Exception {
        request.setURI(ROOT_SERVLET + "assets/example.txt");
        response = HttpTester.parseResponse(SERVLET_TESTER.getResponses(request.generate()));
        assertThat(response.getStatus())
                .isEqualTo(200);
        assertThat(response.getContent())
                .isEqualTo("HELLO THERE");
    }

    @Test
    void servesCharset() throws Exception {
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
    void servesFilesFromRootsWithSameName() throws Exception {
        request.setURI(DUMMY_SERVLET + "example2.txt");
        response = HttpTester.parseResponse(SERVLET_TESTER.getResponses(request.generate()));
        assertThat(response.getStatus())
                .isEqualTo(200);
        assertThat(response.getContent())
                .isEqualTo("HELLO THERE 2");
    }

    @Test
    void cacheIfModifiedSinceOverwrittenByIfNoneMatch() throws Exception{
        request.setHeader(HttpHeader.IF_MODIFIED_SINCE.toString(), "Sat, 05 Nov 1955 22:57:05 GMT");
        request.setURI(DUMMY_SERVLET + "index.htm");
        response = HttpTester.parseResponse(SERVLET_TESTER.getResponses(request.generate()));

        assertThat(response.getStatus())
            .isEqualTo(200);

        String eTag = response.get(HttpHeader.ETAG);

        // If-None-Match should override If-Modified-Since
        request.setHeader(HttpHeader.IF_NONE_MATCH.toString(), eTag);
        response = HttpTester.parseResponse(SERVLET_TESTER.getResponses(request.generate()));
        assertThat(response.getStatus())
            .isEqualTo(304);

    }

    @Test
    void servesFilesWithA200() throws Exception {
        response = HttpTester.parseResponse(SERVLET_TESTER.getResponses(request.generate()));
        assertThat(response.getStatus())
                .isEqualTo(200);
        assertThat(response.getContent())
                .isEqualTo("HELLO THERE");
    }

    @Test
    void throws404IfTheAssetIsMissing() throws Exception {
        request.setURI(DUMMY_SERVLET + "doesnotexist.txt");

        response = HttpTester.parseResponse(SERVLET_TESTER.getResponses(request.generate()));
        assertThat(response.getStatus())
                .isEqualTo(404);
    }

    @Test
    void consistentlyAssignsETags() throws Exception {
        response = HttpTester.parseResponse(SERVLET_TESTER.getResponses(request.generate()));
        final String firstEtag = response.get(HttpHeader.ETAG);

        final int numRequests = 1000;
        final List<Future<String>> futures = new ArrayList<>(numRequests);
        final CountDownLatch latch = new CountDownLatch(1);
        for (int i = 0; i < numRequests; i++) {
            futures.add(ForkJoinPool.commonPool().submit(() -> {
                final ByteBuffer req = request.generate();
                latch.await(); // Attempt to start multiple requests at the same time
                final HttpTester.Response resp = HttpTester.parseResponse(SERVLET_TESTER.getResponses(req));
                return resp.get(HttpHeader.ETAG);
            }));
        }
        latch.countDown();
        final Set<String> eTags = new HashSet<>();
        for (Future<String> future : futures) {
            eTags.add(future.get());
        }
        assertThat(eTags)
                .describedAs("eTag generation should be consistent with concurrent requests")
                .hasSize(1);
        assertThat(firstEtag)
                .isEqualTo("\"e7bd7e8e\"")
                .isEqualTo(eTags.iterator().next());
    }

    @Test
    void assignsDifferentETagsForDifferentFiles() throws Exception {
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
    void supportsIfNoneMatchRequests() throws Exception {
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
    void consistentlyAssignsLastModifiedTimes() throws Exception {
        response = HttpTester.parseResponse(SERVLET_TESTER.getResponses(request.generate()));
        final long firstLastModifiedTime = response.getDateField(HttpHeader.LAST_MODIFIED.asString());

        response = HttpTester.parseResponse(SERVLET_TESTER.getResponses(request.generate()));
        final long secondLastModifiedTime = response.getDateField(HttpHeader.LAST_MODIFIED.asString());

        assertThat(firstLastModifiedTime)
                .isEqualTo(secondLastModifiedTime);
    }

    @Test
    void supportsByteRangeForMedia() throws Exception {
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
    void supportsFullByteRange() throws Exception {
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
    void supportsCentralByteRange() throws Exception {
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
    void supportsFinalByteRange() throws Exception {
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

    @ParameterizedTest
    @ValueSource(strings = {
        "bytes=test",
        "bytes=",
        "bytes=1-infinity",
        "test",
        "bytes=20-",
        "bytes=20-30",
        "bytes=8-3",
        "bytes= -",
        "bytes=10000-20000",
        "bytes=10000-",
        "bytes=10500",
    })
    void rejectsInvalidByteRanges(String rangeValue) throws Exception {
        // given
        request.setURI(ROOT_SERVLET + "assets/example.txt");

        // when
        request.setHeader(HttpHeader.RANGE.asString(), rangeValue);
        response = HttpTester.parseResponse(SERVLET_TESTER.getResponses(request.generate()));

        // then
        assertThat(response.getStatus())
            .isEqualTo(416);
    }

    @Test
    void supportsMultipleByteRanges() throws Exception {
        // given
        request.setURI(ROOT_SERVLET + "assets/example.txt");

        // when
        request.setHeader(HttpHeader.RANGE.asString(), "bytes=0-0,-1");
        response = HttpTester.parseResponse(SERVLET_TESTER.getResponses(request.generate()));

        // then
        //    top-level HTTP response assertions
        assertThat(response.getStatus())
            .isEqualTo(206);
        assertThat(response.get(HttpHeader.ACCEPT_RANGES))
            .isEqualTo("bytes");
        final String contentType = response.get(HttpHeader.CONTENT_TYPE);
        assertThat(contentType)
            .startsWith("multipart/byteranges; boundary=");
        //    multipart response's number of parts == number ranges
        final MultiPartByteRanges.Parts parts = parseMultipartResponses(response);
        assertThat(parts.size())
            .isEqualTo(2);
        //    part 1 assertions
        final MultiPart.Part first = parts.get(0);
        assertThat(first.getHeaders().get(HttpHeader.CONTENT_TYPE))
            .isEqualTo("text/plain");
        assertThat(first.getHeaders().get(HttpHeader.CONTENT_RANGE))
            .isEqualTo("bytes 0-0/11");
        assertThat(first.getContentAsString(StandardCharsets.UTF_8))
            .isEqualTo("H");
        //    part 2 assertions
        final MultiPart.Part second = parts.get(1);
        assertThat(second.getHeaders().get(HttpHeader.CONTENT_TYPE))
            .isEqualTo("text/plain");
        assertThat(second.getHeaders().get(HttpHeader.CONTENT_RANGE))
            .isEqualTo("bytes 10-10/11");
        assertThat(second.getContentAsString(StandardCharsets.UTF_8))
            .isEqualTo("E");
    }

    // uses Jetty's parser to parse multipart responses
    private static MultiPartByteRanges.Parts parseMultipartResponses(HttpTester.Response resp) throws Exception {
        final String boundary = MultiPart.extractBoundary(resp.get(HttpHeader.CONTENT_TYPE));
        final MultiPartByteRanges.Parser parser = new MultiPartByteRanges.Parser(boundary);
        return parser.parse(new ByteBufferContentSource(resp.getContentByteBuffer())).get();
    }

    @Test
    void coalescesOverlappingByteRanges() throws Exception {
        // given
        request.setURI(ROOT_SERVLET + "assets/example.txt");

        // when
        //    Overlapping ranges 5-6 and 7-10 are adjacent (not overlapping), but when overlapping the parser coalesces
        //    them. "bytes=5-6,7-10" are adjacent and would be delivered as two parts; "bytes=5-8,7-10" overlaps and
        //    should coalesce into a single range 5-10.
        request.setHeader(HttpHeader.RANGE.asString(), "bytes=5-8,7-10");
        response = HttpTester.parseResponse(SERVLET_TESTER.getResponses(request.generate()));

        // then
        assertThat(response.getStatus())
            .isEqualTo(206);
        assertThat(response.get(HttpHeader.CONTENT_TYPE))
            .startsWith("text/plain");
        assertThat(response.get(HttpHeader.CONTENT_RANGE))
            .isEqualTo("bytes 5-10/11");
        assertThat(response.getContent()).isEqualTo(" THERE");
    }

    @Test
    void supportsIfRangeMatchRequests() throws Exception {
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
    void supportsIfModifiedSinceRequests() throws Exception {
        response = HttpTester.parseResponse(SERVLET_TESTER.getResponses(request.generate()));
        final long lastModifiedTime = response.getDateField(HttpHeader.LAST_MODIFIED.asString());

        request.putDate(HttpHeader.IF_MODIFIED_SINCE, lastModifiedTime);
        response = HttpTester.parseResponse(SERVLET_TESTER.getResponses(request.generate()));
        final int statusWithMatchingLastModifiedTime = response.getStatus();

        request.putDate(HttpHeader.IF_MODIFIED_SINCE,
                lastModifiedTime - 100);
        response = HttpTester.parseResponse(SERVLET_TESTER.getResponses(request.generate()));
        final int statusWithStaleLastModifiedTime = response.getStatus();

        request.putDate(HttpHeader.IF_MODIFIED_SINCE,
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
    void guessesMimeTypes() throws Exception {
        response = HttpTester.parseResponse(SERVLET_TESTER.getResponses(request.generate()));
        assertThat(response.getStatus())
                .isEqualTo(200);
        assertThat(MimeTypes.CACHE.get(response.get(HttpHeader.CONTENT_TYPE)))
                .isEqualTo(MimeTypes.Type.TEXT_PLAIN_UTF_8);
    }

    @Test
    void defaultsToHtml() throws Exception {
        request.setURI(DUMMY_SERVLET + "foo.bar");
        response = HttpTester.parseResponse(SERVLET_TESTER.getResponses(request.generate()));
        assertThat(response.getStatus())
                .isEqualTo(200);
        assertThat(MimeTypes.CACHE.get(response.get(HttpHeader.CONTENT_TYPE)))
                .isEqualTo(MimeTypes.Type.TEXT_HTML_UTF_8);
    }

    @Test
    void defaultsToHtmlIfNotOverridden() throws Exception {
        request.setURI(NOMEDIATYPE_SERVLET + "foo.bar");
        response = HttpTester.parseResponse(SERVLET_TESTER.getResponses(request.generate()));
        assertThat(response.getStatus())
                .isEqualTo(200);
        assertThat(MimeTypes.CACHE.get(response.get(HttpHeader.CONTENT_TYPE)))
                .isEqualTo(MimeTypes.Type.TEXT_HTML_UTF_8);
    }

    @Test
    void servesWithDefaultMediaType() throws Exception {
        request.setURI(MEDIATYPE_SERVLET + "foo.bar");
        response = HttpTester.parseResponse(SERVLET_TESTER.getResponses(request.generate()));
        assertThat(response.getStatus())
                .isEqualTo(200);
        assertThat(MimeTypes.CACHE.get(response.get(HttpHeader.CONTENT_TYPE)))
                .isEqualTo(MimeTypes.Type.TEXT_PLAIN_UTF_8);
    }

    @Test
    void servesIndexFilesByDefault() throws Exception {
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
    void throwsA404IfNoIndexFileIsDefined() throws Exception {
        // Root directory listing:
        request.setURI(NOINDEX_SERVLET + '/');
        response = HttpTester.parseResponse(SERVLET_TESTER.getResponses(request.generate()));
        assertThat(response.getStatus())
                .isEqualTo(400);

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
    void doesNotAllowOverridingUrls() throws Exception {
        request.setURI(DUMMY_SERVLET + "file:/etc/passwd");
        response = HttpTester.parseResponse(SERVLET_TESTER.getResponses(request.generate()));
        assertThat(response.getStatus())
                .isEqualTo(404);
    }

    @Test
    void doesNotAllowOverridingPaths() throws Exception {
        request.setURI(DUMMY_SERVLET + "/etc/passwd");
        response = HttpTester.parseResponse(SERVLET_TESTER.getResponses(request.generate()));
        assertThat(response.getStatus())
                .isEqualTo(400);
    }

    @Test
    void allowsEncodedAssetNames() throws Exception {
        request.setURI(DUMMY_SERVLET + "encoded%20example.txt");
        response = HttpTester.parseResponse(SERVLET_TESTER.getResponses(request.generate()));
        assertThat(response.getStatus())
                .isEqualTo(200);
    }

    @Test
    void acceptRangesHeaderIsPresentOnEveryResponse() throws Exception {
        // given
        //    a plain request for a non-media asset
        request.setURI(DUMMY_SERVLET + "example.txt");

        // when
        response = HttpTester.parseResponse(SERVLET_TESTER.getResponses(request.generate()));

        // then
        //    Accept-Ranges header present
        //    (Previously `Accept-Ranges: bytes` was only set for video/audio or active range responses.)
        assertThat(response.getStatus())
                .isEqualTo(200);
        assertThat(response.get(HttpHeader.ACCEPT_RANGES))
                .isEqualTo("bytes");
    }

    @Test
    void unsatisfiableRangeIncludesContentRangeHeader() throws Exception {
        // given
        request.setURI(ROOT_SERVLET + "assets/example.txt");

        // when
        //    a range request past the end of the resource
        request.setHeader(HttpHeader.RANGE.asString(), "bytes=100-200");
        response = HttpTester.parseResponse(SERVLET_TESTER.getResponses(request.generate()));

        // then
        //    Content-Range header present
        //    (Previously 416 responses omitted Content-Range, hiding the actual resource length from the client.)
        assertThat(response.getStatus())
                .isEqualTo(416);
        assertThat(response.get(HttpHeader.CONTENT_RANGE))
                .isEqualTo("bytes */11");
    }

    @Test
    void notModifiedResponseCarriesEntityHeaders() throws Exception {
        // given
        final String etag = fetchETag();

        // when
        request.setHeader(HttpHeader.IF_NONE_MATCH.asString(), etag);
        response = HttpTester.parseResponse(SERVLET_TESTER.getResponses(request.generate()));

        // then
        //    Various headers present
        //    (Previously 304 was sent via sendError and carried no ETag/Last-Modified.)
        assertThat(response.getStatus())
                .isEqualTo(304);
        assertThat(response.get(HttpHeader.ETAG))
                .isEqualTo(etag);
        assertThat(response.get(HttpHeader.LAST_MODIFIED))
                .isNotNull();
        assertThat(response.get(HttpHeader.ACCEPT_RANGES))
                .isEqualTo("bytes");
    }

    @Test
    void ifNoneMatchWildcardReturns304() throws Exception {
        // when
        request.setHeader(HttpHeader.IF_NONE_MATCH.asString(), "*");
        response = HttpTester.parseResponse(SERVLET_TESTER.getResponses(request.generate()));

        // then
        assertThat(response.getStatus())
                .isEqualTo(304);
    }

    @Test
    void ifNoneMatchListMatchesAnyEntry() throws Exception {
        // given
        final String etag = fetchETag();

        // when
        request.setHeader(HttpHeader.IF_NONE_MATCH.asString(), "\"nope\", " + etag + ", \"also-nope\"");
        response = HttpTester.parseResponse(SERVLET_TESTER.getResponses(request.generate()));

        // then
        assertThat(response.getStatus())
                .isEqualTo(304);
    }

    @Test
    void ifNoneMatchAcceptsWeakFormOfOurETag() throws Exception {
        // given
        final String etag = fetchETag();

        // when
        //    A client echoing our strong ETag back prefixed with W/
        //    (Our server emits strong ETags, but weak comparison per RFC 7232 2.3.2 says W/"X" matches "X".)
        request.setHeader(HttpHeader.IF_NONE_MATCH.asString(), "W/" + etag);
        response = HttpTester.parseResponse(SERVLET_TESTER.getResponses(request.generate()));

        // then
        assertThat(response.getStatus())
                .isEqualTo(304);
    }

    @Test
    void ifMatchMatchingTagReturns200() throws Exception {
        // given
        final String etag = fetchETag();

        // when
        request.setHeader(HttpHeader.IF_MATCH.asString(), etag);
        response = HttpTester.parseResponse(SERVLET_TESTER.getResponses(request.generate()));

        // then
        assertThat(response.getStatus())
                .isEqualTo(200);
        assertThat(response.getContent())
                .isEqualTo("HELLO THERE");
    }

    @Test
    void ifMatchNonMatchingTagReturns412() throws Exception {
        // when
        request.setHeader(HttpHeader.IF_MATCH.asString(), "\"bogus-tag\"");
        response = HttpTester.parseResponse(SERVLET_TESTER.getResponses(request.generate()));

        // then
        //    412 should still carry entity metadata so a client can retry sensibly.
        assertThat(response.getStatus())
                .isEqualTo(412);
        assertThat(response.get(HttpHeader.ETAG))
                .isNotNull();
        assertThat(response.get(HttpHeader.LAST_MODIFIED))
                .isNotNull();
    }

    @Test
    void ifMatchWildcardReturns200ForExistingResource() throws Exception {
        // when
        request.setHeader(HttpHeader.IF_MATCH.asString(), "*");
        response = HttpTester.parseResponse(SERVLET_TESTER.getResponses(request.generate()));

        // then
        assertThat(response.getStatus())
                .isEqualTo(200);
    }

    @Test
    void ifMatchRejectsWeakFormOfOurETag() throws Exception {
        // given
        final String etag = fetchETag();

        // when
        //    a client echoing our strong ETag back prefixed with W/
        //    (Strong comparison per RFC 7232 3.1 disqualifies W/-prefixed tags on either side.)
        request.setHeader(HttpHeader.IF_MATCH.asString(), "W/" + etag);
        response = HttpTester.parseResponse(SERVLET_TESTER.getResponses(request.generate()));

        // then
        assertThat(response.getStatus())
                .isEqualTo(412);
    }

    @Test
    void ifUnmodifiedSinceInFutureReturns200() throws Exception {
        // given
        response = HttpTester.parseResponse(SERVLET_TESTER.getResponses(request.generate()));
        final long lastModified = response.getDateField(HttpHeader.LAST_MODIFIED.asString());
        request.putDate(HttpHeader.IF_UNMODIFIED_SINCE, lastModified + 10_000);

        // when
        response = HttpTester.parseResponse(SERVLET_TESTER.getResponses(request.generate()));

        // then
        assertThat(response.getStatus())
                .isEqualTo(200);
    }

    @Test
    void ifUnmodifiedSinceInPastReturns412() throws Exception {
        // given
        response = HttpTester.parseResponse(SERVLET_TESTER.getResponses(request.generate()));
        final long lastModified = response.getDateField(HttpHeader.LAST_MODIFIED.asString());
        request.putDate(HttpHeader.IF_UNMODIFIED_SINCE, lastModified - 10_000);

        // when
        response = HttpTester.parseResponse(SERVLET_TESTER.getResponses(request.generate()));

        // then
        assertThat(response.getStatus())
                .isEqualTo(412);
    }

    @Test
    void ifMatchTakesPrecedenceOverIfUnmodifiedSince() throws Exception {
        // given
        final String etag = fetchETag();
        response = HttpTester.parseResponse(SERVLET_TESTER.getResponses(request.generate()));
        final long lastModified = response.getDateField(HttpHeader.LAST_MODIFIED.asString());

        // when
        //    both headers present; If-Match matches, If-Unmodified-Since would otherwise 412
        //    (RFC 7232 3.4: If-Unmodified-Since is ignored when If-Match is present.)
        request.setHeader(HttpHeader.IF_MATCH.asString(), etag);
        request.putDate(HttpHeader.IF_UNMODIFIED_SINCE, lastModified - 10_000);
        response = HttpTester.parseResponse(SERVLET_TESTER.getResponses(request.generate()));

        // then
        assertThat(response.getStatus())
                .isEqualTo(200);
    }

    @Test
    void ifMatchFailsAheadOfIfNoneMatchSuccess() throws Exception {
        // given
        final String etag = fetchETag();

        // when
        //    If-Match fails; If-None-Match would succeed (304) on its own
        //    (Precondition ordering per RFC 7232 6: If-Match is evaluated first, so a 412 wins over 304.)
        request.setHeader(HttpHeader.IF_MATCH.asString(), "\"not-our-tag\"");
        request.setHeader(HttpHeader.IF_NONE_MATCH.asString(), etag);
        response = HttpTester.parseResponse(SERVLET_TESTER.getResponses(request.generate()));

        // then
        assertThat(response.getStatus())
                .isEqualTo(412);
    }

    @Test
    void ifRangeHttpDateMatchingLastModifiedServesRange() throws Exception {
        // given
        response = HttpTester.parseResponse(SERVLET_TESTER.getResponses(request.generate()));
        final long lastModified = response.getDateField(HttpHeader.LAST_MODIFIED.asString());

        // when
        request.setHeader(HttpHeader.RANGE.asString(), "bytes=0-4");
        request.putDate(HttpHeader.IF_RANGE, lastModified);
        response = HttpTester.parseResponse(SERVLET_TESTER.getResponses(request.generate()));

        // then
        assertThat(response.getStatus())
                .isEqualTo(206);
    }

    @Test
    void ifRangeHttpDateMismatchServesFullResource() throws Exception {
        // given: a stale cached copy whose Last-Modified predates ours
        response = HttpTester.parseResponse(SERVLET_TESTER.getResponses(request.generate()));
        final long lastModified = response.getDateField(HttpHeader.LAST_MODIFIED.asString());

        // when
        request.setHeader(HttpHeader.RANGE.asString(), "bytes=0-4");
        request.putDate(HttpHeader.IF_RANGE, lastModified - 10_000);
        response = HttpTester.parseResponse(SERVLET_TESTER.getResponses(request.generate()));

        // then
        assertThat(response.getStatus())
                .isEqualTo(200);
    }

    @Test
    void ifRangeRejectsWeakETag() throws Exception {
        // given
        final String etag = fetchETag();

        // when
        request.setHeader(HttpHeader.RANGE.asString(), "bytes=0-4");
        request.setHeader(HttpHeader.IF_RANGE.asString(), "W/" + etag);
        response = HttpTester.parseResponse(SERVLET_TESTER.getResponses(request.generate()));

        // then
        //    Strong comparison per RFC 7233 3.2: W/-prefixed tags never match; fall through to a 200 full response.
        assertThat(response.getStatus())
                .isEqualTo(200);
        assertThat(response.getContent())
                .isEqualTo("HELLO THERE");
    }

    @Test
    void malformedIfModifiedSinceServesFullResource() throws Exception {
        // when
        request.setHeader(HttpHeader.IF_MODIFIED_SINCE.asString(), "not-a-valid-date");
        response = HttpTester.parseResponse(SERVLET_TESTER.getResponses(request.generate()));

        // then
        //    HTTP 200 result.
        //    (Previously this triggered 404 via a broad `catch (RuntimeException ...)`
        assertThat(response.getStatus())
                .isEqualTo(200);
        assertThat(response.getContent())
                .isEqualTo("HELLO THERE");
    }

    // Issues the current {@code request} and returns the ETag from the response. Convenience method.
    private String fetchETag() throws Exception {
        final HttpTester.Response r = HttpTester.parseResponse(SERVLET_TESTER.getResponses(request.generate()));
        assertThat(r.getStatus()).isEqualTo(200);
        return r.get(HttpHeader.ETAG);
    }
}
