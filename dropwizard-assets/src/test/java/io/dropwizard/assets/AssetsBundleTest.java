package io.dropwizard.assets;

import io.dropwizard.Configuration;
import io.dropwizard.jetty.setup.ServletEnvironment;
import io.dropwizard.servlets.assets.AssetServlet;
import io.dropwizard.servlets.assets.ResourceURL;
import io.dropwizard.setup.Environment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import jakarta.servlet.ServletRegistration;
import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AssetsBundleTest {
    private final ServletEnvironment servletEnvironment = mock(ServletEnvironment.class);
    private final Environment environment = mock(Environment.class);

    private AssetServlet servlet = new AssetServlet("/", "/", null, null, null);
    private String servletPath = "";

    @BeforeEach
    void setUp() {
        when(environment.servlets()).thenReturn(servletEnvironment);
    }

    @Test
    void hasADefaultPath() {
        runBundle(new AssetsBundle());

        assertThat(servletPath)
                .isEqualTo("/assets/*");

        assertThat(servlet.getIndexFile())
                .isEqualTo("index.htm");

        assertThat(servlet.getResourceURL())
                .isEqualTo(normalize("/assets"));

        assertThat(servlet.getUriPath())
                .isEqualTo("/assets");
    }

    @Test
    void canHaveCustomPaths() {
        runBundle(new AssetsBundle("/json"));

        assertThat(servletPath)
                .isEqualTo("/json/*");

        assertThat(servlet.getIndexFile())
                .isEqualTo("index.htm");

        assertThat(servlet.getResourceURL())
                .isEqualTo(normalize("/json"));

        assertThat(servlet.getUriPath())
                .isEqualTo("/json");
    }

    @Test
    void canHaveDifferentUriAndResourcePaths() {
        runBundle(new AssetsBundle("/json", "/what"));

        assertThat(servletPath)
                .isEqualTo("/what/*");

        assertThat(servlet.getIndexFile())
                .isEqualTo("index.htm");

        assertThat(servlet.getResourceURL())
                .isEqualTo(normalize("/json"));

        assertThat(servlet.getUriPath())
                .isEqualTo("/what");
    }

    @Test
    void canSupportDifferentAssetsBundleName() {
        runBundle(new AssetsBundle("/json", "/what/new", "index.txt", "customAsset1"), "customAsset1");

        assertThat(servletPath)
                .isEqualTo("/what/new/*");

        assertThat(servlet.getIndexFile())
                .isEqualTo("index.txt");

        assertThat(servlet.getResourceURL())
                .isEqualTo(normalize("/json"));

        assertThat(servlet.getUriPath())
                .isEqualTo("/what/new");

        runBundle(new AssetsBundle("/json", "/what/old", "index.txt", "customAsset2"), "customAsset2");
        assertThat(servletPath)
                .isEqualTo("/what/old/*");

        assertThat(servlet.getIndexFile())
                .isEqualTo("index.txt");

        assertThat(servlet.getResourceURL())
                .isEqualTo(normalize("/json"));

        assertThat(servlet.getUriPath())
                .isEqualTo("/what/old");
    }

    @Test
    void canHaveDifferentUriAndResourcePathsAndIndexFilename() {
        runBundle(new AssetsBundle("/json", "/what", "index.txt"));

        assertThat(servletPath)
                .isEqualTo("/what/*");

        assertThat(servlet.getIndexFile())
                .isEqualTo("index.txt");

        assertThat(servlet.getResourceURL())
                .isEqualTo(normalize("/json"));

        assertThat(servlet.getUriPath())
                .isEqualTo("/what");
    }

    @Test
    void canHaveDifferentDefaultMediaType() {
        runBundle(new AssetsBundle("/assets", "/assets", "index.html", "assets", "text/plain"));

        assertThat(servletPath).isEqualTo("/assets/*");
        assertThat(servlet.getIndexFile()).isEqualTo("index.html");
        assertThat(servlet.getResourceURL()).isEqualTo(normalize("/assets"));
        assertThat(servlet.getUriPath()).isEqualTo("/assets");
        assertThat(servlet.getDefaultMediaType()).isEqualTo("text/plain");
    }

    private URL normalize(String path) {
        return ResourceURL.appendTrailingSlash(getClass().getResource(path));
    }

    private void runBundle(AssetsBundle bundle) {
        runBundle(bundle, "assets");
    }

    private void runBundle(AssetsBundle bundle, String assetName) {
        final ServletRegistration.Dynamic registration = mock(ServletRegistration.Dynamic.class);
        when(servletEnvironment.addServlet(anyString(), any(AssetServlet.class))).thenReturn(registration);

        bundle.run(new Configuration(), environment);

        final ArgumentCaptor<AssetServlet> servletCaptor = ArgumentCaptor.forClass(AssetServlet.class);
        final ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);

        verify(servletEnvironment).addServlet(eq(assetName), servletCaptor.capture());
        verify(registration).addMapping(pathCaptor.capture());

        this.servlet = servletCaptor.getValue();
        this.servletPath = pathCaptor.getValue();
    }
}
