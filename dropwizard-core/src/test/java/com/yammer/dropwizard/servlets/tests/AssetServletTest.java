package com.yammer.dropwizard.servlets.tests;

import com.google.common.cache.CacheBuilderSpec;
import com.google.common.io.Resources;
import com.google.common.net.HttpHeaders;
import com.yammer.dropwizard.servlets.AssetServlet;
import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.testing.HttpTester;
import org.eclipse.jetty.testing.ServletTester;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.*;

@SuppressWarnings({ "serial", "StaticNonFinalField", "StaticVariableMayNotBeInitialized" })
public class AssetServletTest {
    private static ServletTester servletTester;
    private static final String DUMMY_SERVLET = "/dummy_servlet/";
    private static final String NOINDEX_SERVLET = "/noindex_servlet/";
    private static final String RESOURCE_PATH = "assets";
    private static final CacheBuilderSpec CACHE_BUILDER_SPEC = CacheBuilderSpec.parse("maximumSize=100");

    private HttpTester request;
    private HttpTester response;

    // ServletTester expects to be able to instantiate the servlet with zero arguments

    public static class DummyAssetServlet extends AssetServlet {
        public DummyAssetServlet() {
            super(Resources.getResource(RESOURCE_PATH), CACHE_BUILDER_SPEC, DUMMY_SERVLET, "index.htm");
        }
    }

    public static class NoIndexAssetServlet extends AssetServlet {
        public NoIndexAssetServlet() {
            super(Resources.getResource(RESOURCE_PATH), CACHE_BUILDER_SPEC, DUMMY_SERVLET, null);
        }
    }

    @BeforeClass
    public static void setup() throws Exception {
        servletTester = new ServletTester();
        servletTester.addServlet(DummyAssetServlet.class, DUMMY_SERVLET + '*');
        servletTester.addServlet(NoIndexAssetServlet.class, NOINDEX_SERVLET + '*');
        servletTester.start();
    }

    @Before
    public void setupTester() throws Exception {
        request = new HttpTester();
        request.setMethod("GET");
        request.setURI(DUMMY_SERVLET + "example.txt");
        request.setVersion("HTTP/1.0");
        response = new HttpTester();
    }

    @Test
    public void servesFilesWithA200() throws Exception {
        response.parse(servletTester.getResponses(request.generate()));
        assertThat(response.getStatus())
                .isEqualTo(200);
        assertThat(response.getContent())
                .isEqualTo("HELLO THERE");
    }

    @Test
    public void throws404IfTheAssetIsMissing() throws Exception {
        request.setURI(DUMMY_SERVLET + "doesnotexist.txt");

        response.parse(servletTester.getResponses(request.generate()));
        assertThat(response.getStatus())
                .isEqualTo(404);
    }

    @Test
    public void consistentlyAssignsETags() throws Exception {
        response.parse(servletTester.getResponses(request.generate()));
        final String firstEtag = response.getHeader(HttpHeaders.ETAG);

        response.parse(servletTester.getResponses(request.generate()));
        final String secondEtag = response.getHeader(HttpHeaders.ETAG);

        assertThat(firstEtag)
                .isNotNull();
        assertThat(firstEtag)
                .isNotEmpty();
        assertThat(firstEtag)
                .isEqualTo(secondEtag);
    }

    @Test
    public void assignsDifferentETagsForDifferentFiles() throws Exception {
        response.parse(servletTester.getResponses(request.generate()));
        final String firstEtag = response.getHeader(HttpHeaders.ETAG);

        request.setURI(DUMMY_SERVLET + "foo.bar");
        response.parse(servletTester.getResponses(request.generate()));
        final String secondEtag = response.getHeader(HttpHeaders.ETAG);

        assertThat(firstEtag)
                .isNotEqualTo(secondEtag);
    }

    @Test
    public void supportsIfNoneMatchRequests() throws Exception {
        response.parse(servletTester.getResponses(request.generate()));
        final String correctEtag = response.getHeader(HttpHeaders.ETAG);

        request.setHeader(HttpHeaders.IF_NONE_MATCH, correctEtag);
        response.parse(servletTester.getResponses(request.generate()));
        final int statusWithMatchingEtag = response.getStatus();

        request.setHeader(HttpHeaders.IF_NONE_MATCH, correctEtag + "FOO");
        response.parse(servletTester.getResponses(request.generate()));
        final int statusWithNonMatchingEtag = response.getStatus();

        assertThat(statusWithMatchingEtag)
                .isEqualTo(304);
        assertThat(statusWithNonMatchingEtag)
                .isEqualTo(200);
    }

    @Test
    public void consistentlyAssignsLastModifiedTimes() throws Exception {
        response.parse(servletTester.getResponses(request.generate()));
        final long firstLastModifiedTime = response.getDateHeader(HttpHeaders.LAST_MODIFIED);

        response.parse(servletTester.getResponses(request.generate()));
        final long secondLastModifiedTime = response.getDateHeader(HttpHeaders.LAST_MODIFIED);

        assertThat(firstLastModifiedTime)
                .isEqualTo(secondLastModifiedTime);
    }

    @Test
    public void supportsIfModifiedSinceRequests() throws Exception {
        response.parse(servletTester.getResponses(request.generate()));
        final long lastModifiedTime = response.getDateHeader(HttpHeaders.LAST_MODIFIED);

        request.setDateHeader(HttpHeaders.IF_MODIFIED_SINCE, lastModifiedTime);
        response.parse(servletTester.getResponses(request.generate()));
        final int statusWithMatchingLastModifiedTime = response.getStatus();

        request.setDateHeader(HttpHeaders.IF_MODIFIED_SINCE, lastModifiedTime - 100);
        response.parse(servletTester.getResponses(request.generate()));
        final int statusWithStaleLastModifiedTime = response.getStatus();

        request.setDateHeader(HttpHeaders.IF_MODIFIED_SINCE, lastModifiedTime + 100);
        response.parse(servletTester.getResponses(request.generate()));
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
        response.parse(servletTester.getResponses(request.generate()));
        assertThat(response.getStatus())
                .isEqualTo(200);
        assertThat(response.getContentType())
                .isEqualTo(MimeTypes.TEXT_PLAIN);
    }

    @Test
    public void defaultsToHtml() throws Exception {
        request.setURI(DUMMY_SERVLET + "foo.bar");
        response.parse(servletTester.getResponses(request.generate()));
        assertThat(response.getStatus())
                .isEqualTo(200);
        assertThat(response.getContentType())
                .isEqualTo(MimeTypes.TEXT_HTML);
    }

    @Test
    public void servesIndexFilesByDefault() throws Exception {
        // Root directory listing:
        request.setURI(DUMMY_SERVLET);
        response.parse(servletTester.getResponses(request.generate()));
        assertThat(response.getStatus())
                .isEqualTo(200);
        assertThat(response.getContent())
                .contains("/assets Index File");

        // Subdirectory listing:
        request.setURI(DUMMY_SERVLET + "some_directory");
        response.parse(servletTester.getResponses(request.generate()));
        assertThat(response.getStatus())
                .isEqualTo(200);
        assertThat(response.getContent())
                .contains("/assets/some_directory Index File");

        // Subdirectory listing with slash:
        request.setURI(DUMMY_SERVLET + "some_directory/");
        response.parse(servletTester.getResponses(request.generate()));
        assertThat(response.getStatus())
                .isEqualTo(200);
        assertThat(response.getContent())
                .contains("/assets/some_directory Index File");
    }

    @Test
    public void throwsA404IfNoIndexFileIsDefined() throws Exception {
        // Root directory listing:
        request.setURI(NOINDEX_SERVLET + '/');
        response.parse(servletTester.getResponses(request.generate()));
        assertThat(response.getStatus())
                .isEqualTo(404);

        // Subdirectory listing:
        request.setURI(NOINDEX_SERVLET + "some_directory");
        response.parse(servletTester.getResponses(request.generate()));
        assertThat(response.getStatus())
                .isEqualTo(404);

        // Subdirectory listing with slash:
        request.setURI(NOINDEX_SERVLET + "some_directory/");
        response.parse(servletTester.getResponses(request.generate()));
        assertThat(response.getStatus())
                .isEqualTo(404);
    }

    @Test
    public void doesNotAllowOverridingUrls() throws Exception {
        request.setURI(DUMMY_SERVLET + "file:/etc/passwd");
        response.parse(servletTester.getResponses(request.generate()));
        assertThat(response.getStatus())
                .isEqualTo(404);
    }

    @Test
    public void doesNotAllowOverridingPaths() throws Exception {
        request.setURI(DUMMY_SERVLET + "/etc/passwd");
        response.parse(servletTester.getResponses(request.generate()));
        assertThat(response.getStatus())
                .isEqualTo(404);
    }
}
