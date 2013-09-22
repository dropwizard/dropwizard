package io.dropwizard.assets;

import com.google.common.io.Resources;
import io.dropwizard.jetty.setup.ServletEnvironment;
import io.dropwizard.servlets.assets.AssetServlet;
import io.dropwizard.servlets.assets.ResourceURL;
import io.dropwizard.setup.Environment;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import javax.servlet.ServletRegistration;
import java.net.URL;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class AssetsBundleTest {
    private final ServletEnvironment servletEnvironment = mock(ServletEnvironment.class);
    private final Environment environment = mock(Environment.class);

    private AssetServlet servlet;
    private String servletPath;

    @Before
    public void setUp() throws Exception {
        when(environment.servlets()).thenReturn(servletEnvironment);
    }

    @Test
    public void hasADefaultPath() throws Exception {
        runBundle(new AssetsBundle());

        assertThat(servletPath)
                .isEqualTo("/assets/*");

        assertThat(servlet.getIndexFile())
                .isEqualTo("index.htm");

        assertThat(servlet.getResourceURL())
                .isEqualTo(normalize("assets"));

        assertThat(servlet.getUriPath())
                .isEqualTo("/assets");
    }

    @Test
    public void canHaveCustomPaths() throws Exception {
        runBundle(new AssetsBundle("/json"));

        assertThat(servletPath)
                .isEqualTo("/json/*");

        assertThat(servlet.getIndexFile())
                .isEqualTo("index.htm");

        assertThat(servlet.getResourceURL())
                .isEqualTo(normalize("json"));

        assertThat(servlet.getUriPath())
                .isEqualTo("/json");
    }

    @Test
    public void canHaveDifferentUriAndResourcePaths() throws Exception {
        runBundle(new AssetsBundle("/json", "/what"));

        assertThat(servletPath)
                .isEqualTo("/what/*");

        assertThat(servlet.getIndexFile())
                .isEqualTo("index.htm");

        assertThat(servlet.getResourceURL())
                .isEqualTo(normalize("json"));

        assertThat(servlet.getUriPath())
                .isEqualTo("/what");
    }

    @Test
    public void canHaveDifferentUriAndResourcePathsAndIndexFilename() throws Exception {
        runBundle(new AssetsBundle("/json", "/what", "index.txt"));

        assertThat(servletPath)
                .isEqualTo("/what/*");

        assertThat(servlet.getIndexFile())
                .isEqualTo("index.txt");

        assertThat(servlet.getResourceURL())
                .isEqualTo(normalize("json"));

        assertThat(servlet.getUriPath())
                .isEqualTo("/what");
    }

    private URL normalize(String path) {
        return ResourceURL.appendTrailingSlash(Resources.getResource(path));
    }

    private void runBundle(AssetsBundle bundle) {
        final ServletRegistration.Dynamic registration = mock(ServletRegistration.Dynamic.class);
        when(servletEnvironment.addServlet(anyString(), any(AssetServlet.class))).thenReturn(registration);

        bundle.run(environment);

        final ArgumentCaptor<AssetServlet> servletCaptor = ArgumentCaptor.forClass(AssetServlet.class);
        final ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);

        verify(servletEnvironment).addServlet(eq("assets"), servletCaptor.capture());
        verify(registration).addMapping(pathCaptor.capture());

        this.servlet = servletCaptor.getValue();
        this.servletPath = pathCaptor.getValue();
    }
}
