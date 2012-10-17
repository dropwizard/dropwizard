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
import java.net.URL;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class AssetsBundleTest {

    private final Environment environment = mock(Environment.class);

    @Test
    public void allDefaultsFromBundle() throws MalformedURLException {
        final AssetsBundle bundle = new CapturingAssetsBundle();
        bundle.run(environment);

        assertServletInEnvironment(AssetsBundle.DEFAULT_PATH + '/',
                AssetsBundle.DEFAULT_PATH + '/',
                AssetsBundle.DEFAULT_CACHE_SPEC,
                AssetsBundle.DEFAULT_INDEX_FILE);
    }

    @Test
    public void customPathChangesResourcePathAndUriPath() {
        final String path = "/json";

        final AssetsBundle bundle = new CapturingAssetsBundle(path);
        bundle.run(environment);

        assertServletInEnvironment(path + '/', path + '/',
                AssetsBundle.DEFAULT_CACHE_SPEC, AssetsBundle.DEFAULT_INDEX_FILE);
    }

    @Test
    public void customResourcePathAndUriPath() {
        final String resourcePath = "/json/";
        final String uriPath = "/js";

        final AssetsBundle bundle = new CapturingAssetsBundle(resourcePath, uriPath);
        bundle.run(environment);

        assertServletInEnvironment(resourcePath, uriPath + '/',
                AssetsBundle.DEFAULT_CACHE_SPEC, AssetsBundle.DEFAULT_INDEX_FILE);
    }

    @Test
    public void customPathAndCacheBuilderSpec() {
        final String path = "/json/";
        final CacheBuilderSpec spec = CacheBuilderSpec.disableCaching();

        final AssetsBundle bundle = new CapturingAssetsBundle(path, spec);
        bundle.run(environment);

        assertServletInEnvironment(path, path, spec, AssetsBundle.DEFAULT_INDEX_FILE);
    }

    @Test
    public void customResourcePathUriPathAndCacheBuilderSpec() {
        final String resourcePath = "/json";
        final String uriPath = "/js/";
        final CacheBuilderSpec spec = CacheBuilderSpec.disableCaching();

        final AssetsBundle bundle = new CapturingAssetsBundle(resourcePath, spec, uriPath);
        bundle.run(environment);

        assertServletInEnvironment(resourcePath + '/', uriPath, spec, AssetsBundle.DEFAULT_INDEX_FILE);
    }

    @Test
    public void customResourcePathUriPathCacheBuilderSpecAndIndexFile() {
        final String resourcePath = "/json/";
        final String uriPath = "/js/";
        final CacheBuilderSpec spec = CacheBuilderSpec.disableCaching();
        final String indexFile = "my-index-file.html";

        final AssetsBundle bundle = new CapturingAssetsBundle(resourcePath, spec, uriPath, indexFile);
        bundle.run(environment);

        assertServletInEnvironment(resourcePath, uriPath, spec, indexFile);
    }

    private void assertServletInEnvironment(String resourcePath, String uriPath, CacheBuilderSpec spec, String indexFile) {
        final ArgumentCaptor<Servlet> captureServlet = ArgumentCaptor.forClass(Servlet.class);
        final ArgumentCaptor<String> captureUriPath = ArgumentCaptor.forClass(String.class);
        verify(environment, times(1)).addServlet(captureServlet.capture(), captureUriPath.capture());

        assertThat(captureServlet.getValue()).isInstanceOf(CapturingAssetServlet.class);

        final CapturingAssetServlet servlet = (CapturingAssetServlet) captureServlet.getValue();
        assertThat(servlet.resourceURL).isEqualTo(Resources.getResource(resourcePath.substring(1)));
        assertThat(servlet.cacheBuilderSpec).isSameAs(spec);
        assertThat(servlet.uriPath).isEqualTo(uriPath);
        assertThat(servlet.indexFile).isEqualTo(indexFile);

        assertThat(captureUriPath.getValue()).isEqualTo(uriPath + "*");
    }

    private static class CapturingAssetServlet extends AssetServlet {
        public final URL resourceURL;
        public final CacheBuilderSpec cacheBuilderSpec;
        public final String uriPath;
        public final String indexFile;

        public CapturingAssetServlet(URL resourceURL,
                                     CacheBuilderSpec cacheBuilderSpec,
                                     String uriPath,
                                     String indexFile) {
            super(resourceURL, cacheBuilderSpec, uriPath, indexFile);

            this.resourceURL = resourceURL;
            this.cacheBuilderSpec = cacheBuilderSpec;
            this.uriPath = uriPath;
            this.indexFile = indexFile;
        }
    }

    private static class CapturingAssetsBundle extends AssetsBundle {
        private CapturingAssetsBundle() {
        }

        private CapturingAssetsBundle(String path) {
            super(path);
        }

        private CapturingAssetsBundle(String resourcePath, String uriPath) {
            super(resourcePath, uriPath);
        }

        private CapturingAssetsBundle(String resourcePath, CacheBuilderSpec cacheBuilderSpec) {
            super(resourcePath, cacheBuilderSpec);
        }

        private CapturingAssetsBundle(String resourcePath, CacheBuilderSpec cacheBuilderSpec, String uriPath) {
            super(resourcePath, cacheBuilderSpec, uriPath);
        }

        private CapturingAssetsBundle(String resourcePath, CacheBuilderSpec cacheBuilderSpec, String uriPath, String indexFile) {
            super(resourcePath, cacheBuilderSpec, uriPath, indexFile);
        }

        @Override
        protected AssetServlet createServlet() {
            final URL resourceURL = Resources.getResource(resourcePath.substring(1));

            return new CapturingAssetServlet(resourceURL, cacheBuilderSpec, uriPath, indexFile);
        }
    }
}
