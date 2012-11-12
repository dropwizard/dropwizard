package com.yammer.dropwizard.bundles.tests;

import com.google.common.cache.CacheBuilderSpec;
import com.google.common.io.Resources;
import com.yammer.dropwizard.bundles.AssetsBundle;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.servlets.AssetServlet;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import javax.servlet.Servlet;
import java.net.MalformedURLException;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SuppressWarnings("JUnitTestMethodWithNoAssertions")
public class AssetsBundleTest {

    private final Environment environment = mock(Environment.class);

    @Test
    public void allDefaultsFromBundle() throws MalformedURLException {
        final AssetsBundle bundle = new AssetsBundle();
        bundle.run(environment);

        assertServletInEnvironment(AssetsBundle.DEFAULT_PATH + '/',
                AssetsBundle.DEFAULT_PATH + '/',
                AssetsBundle.DEFAULT_CACHE_SPEC,
                AssetsBundle.DEFAULT_INDEX_FILE);
    }

    @Test
    public void customPathChangesResourcePathAndUriPath() {
        final String path = "/json";

        final AssetsBundle bundle = new AssetsBundle(path);
        bundle.run(environment);

        assertServletInEnvironment(path + '/', path + '/',
                AssetsBundle.DEFAULT_CACHE_SPEC, AssetsBundle.DEFAULT_INDEX_FILE);
    }

    @Test
    public void customResourcePathAndUriPath() {
        final String resourcePath = "/json/";
        final String uriPath = "/js";

        final AssetsBundle bundle = new AssetsBundle(resourcePath, uriPath);
        bundle.run(environment);

        assertServletInEnvironment(resourcePath, uriPath + '/',
                AssetsBundle.DEFAULT_CACHE_SPEC, AssetsBundle.DEFAULT_INDEX_FILE);
    }

    @Test
    public void customPathAndCacheBuilderSpec() {
        final String path = "/json/";
        final CacheBuilderSpec spec = CacheBuilderSpec.disableCaching();

        final AssetsBundle bundle = new AssetsBundle(path, spec);
        bundle.run(environment);

        assertServletInEnvironment(path, path, spec, AssetsBundle.DEFAULT_INDEX_FILE);
    }

    @Test
    public void customResourcePathUriPathAndCacheBuilderSpec() {
        final String resourcePath = "/json";
        final String uriPath = "/js/";
        final CacheBuilderSpec spec = CacheBuilderSpec.disableCaching();

        final AssetsBundle bundle = new AssetsBundle(resourcePath, spec, uriPath);
        bundle.run(environment);

        assertServletInEnvironment(resourcePath + '/', uriPath, spec, AssetsBundle.DEFAULT_INDEX_FILE);
    }

    @Test
    public void customResourcePathUriPathCacheBuilderSpecAndIndexFile() {
        final String resourcePath = "/json/";
        final String uriPath = "/js/";
        final CacheBuilderSpec spec = CacheBuilderSpec.disableCaching();
        final String indexFile = "my-index-file.html";

        final AssetsBundle bundle = new AssetsBundle(resourcePath, spec, uriPath, indexFile);
        bundle.run(environment);

        assertServletInEnvironment(resourcePath, uriPath, spec, indexFile);
    }

    private void assertServletInEnvironment(String resourcePath, String uriPath, CacheBuilderSpec spec, String indexFile) {
        final ArgumentCaptor<Servlet> captureServlet = ArgumentCaptor.forClass(Servlet.class);
        final ArgumentCaptor<String> captureUriPath = ArgumentCaptor.forClass(String.class);
        verify(environment, times(1)).addServlet(captureServlet.capture(), captureUriPath.capture());

        assertThat(captureServlet.getValue()).isInstanceOf(AssetServlet.class);

        final AssetServlet servlet = (AssetServlet) captureServlet.getValue();
        assertThat(servlet.getResourceURL()).isEqualTo(Resources.getResource(resourcePath.substring(1)));
        assertThat(servlet.getCacheBuilderSpec()).isSameAs(spec);
        assertThat(servlet.getUriPath()).isEqualTo(uriPath);
        assertThat(servlet.getIndexFile()).isEqualTo(indexFile);

        assertThat(captureUriPath.getValue()).isEqualTo(uriPath + '*');
    }
}
