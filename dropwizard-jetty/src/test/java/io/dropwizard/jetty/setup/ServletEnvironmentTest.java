package io.dropwizard.jetty.setup;

import static org.assertj.core.api.Assertions.assertThat;

import io.dropwizard.jetty.MutableServletContextHandler;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import javax.servlet.Servlet;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRegistration;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.server.DebugListener;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlets.ConcatServlet;
import org.eclipse.jetty.servlets.WelcomeFilter;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.resource.ResourceCollection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ServletEnvironmentTest {

    private MutableServletContextHandler handler;
    private ServletHandler servletHandler;
    private ServletEnvironment environment;

    @BeforeEach
    void setUp() {
        handler = new MutableServletContextHandler();
        servletHandler = handler.getServletHandler();
        environment = new ServletEnvironment(handler);
    }

    @Test
    void addsServletInstances() throws Exception {
        final Servlet servlet = new ConcatServlet();
        final ServletRegistration.Dynamic builder = environment.addServlet("servlet", servlet);
        assertThat(builder).isNotNull();

        try {
            servletHandler.start();

            final ServletHolder servletHolder = servletHandler.getServlet("servlet");
            assertThat(servletHolder.getServlet()).isEqualTo(servlet);
        } finally {
            servletHandler.stop();
        }
    }

    @Test
    void addsServletClasses() throws Exception {
        final Class<ConcatServlet> servletClass = ConcatServlet.class;
        final ServletRegistration.Dynamic builder = environment.addServlet("servlet", servletClass);
        assertThat(builder).isNotNull();

        try {
            servletHandler.start();

            final ServletHolder servletHolder = servletHandler.getServlet("servlet");
            assertThat(servletHolder.getServlet()).isExactlyInstanceOf(servletClass);
        } finally {
            servletHandler.stop();
        }
    }

    @Test
    void addsFilterInstances() throws Exception {
        final Filter filter = new WelcomeFilter();

        final FilterRegistration.Dynamic builder = environment.addFilter("filter", filter);
        assertThat(builder).isNotNull();

        try {
            servletHandler.start();

            final FilterHolder filterHolder = servletHandler.getFilter("filter");
            assertThat(filterHolder.getFilter()).isEqualTo(filter);
        } finally {
            servletHandler.stop();
        }
    }

    @Test
    void addsFilterClasses() throws Exception {
        final Class<WelcomeFilter> filterClass = WelcomeFilter.class;
        final FilterRegistration.Dynamic builder = environment.addFilter("filter", filterClass);
        assertThat(builder).isNotNull();

        try {
            servletHandler.start();

            final FilterHolder filterHolder = servletHandler.getFilter("filter");
            assertThat(filterHolder.getFilter()).isExactlyInstanceOf(filterClass);
        } finally {
            servletHandler.stop();
        }
    }

    @Test
    void addsServletListeners() {
        final ServletContextListener listener = new DebugListener();
        environment.addServletListeners(listener);

        assertThat(handler.getEventListeners()).contains(listener);
    }

    @Test
    void addsProtectedTargets() {
        environment.setProtectedTargets("/woo");

        assertThat(handler.getProtectedTargets()).contains("/woo");
    }

    @Test
    void setsBaseResource(@TempDir Path tempDir) throws Exception {
        final Resource testResource =
                Resource.newResource(tempDir.resolve("dir").toUri());
        environment.setBaseResource(testResource);

        assertThat(handler.getBaseResource()).isEqualTo(testResource);
    }

    @Test
    void setsBaseResourceList(@TempDir Path tempDir) throws Exception {
        Resource wooResource = Resource.newResource(Files.createDirectory(tempDir.resolve("dir-1")));
        Resource fooResource = Resource.newResource(Files.createDirectory(tempDir.resolve("dir-2")));

        final Resource[] testResources = new Resource[] {wooResource, fooResource};
        environment.setBaseResource(testResources);

        assertThat(handler.getBaseResource()).isExactlyInstanceOf(ResourceCollection.class);
        assertThat(((ResourceCollection) handler.getBaseResource()).getResources())
                .contains(wooResource, fooResource);
    }

    @Test
    void setsResourceBase() throws Exception {
        environment.setResourceBase("/woo");

        assertThat(handler.getResourceBase())
                .isEqualTo(handler.newResource("/woo").toString());
    }

    @Test
    void setsBaseResourceStringList(@TempDir Path tempDir) throws Exception {
        String wooResource = Files.createDirectory(tempDir.resolve("dir-1")).toString();
        String fooResource = Files.createDirectory(tempDir.resolve("dir-2")).toString();

        final String[] testResources = new String[] {wooResource, fooResource};
        environment.setBaseResource(testResources);

        assertThat(handler.getBaseResource()).isExactlyInstanceOf(ResourceCollection.class);
        assertThat(((ResourceCollection) handler.getBaseResource()).getResources())
                .contains(Resource.newResource(wooResource), Resource.newResource(fooResource));
    }

    @Test
    void setsInitParams() {
        environment.setInitParameter("a", "b");

        assertThat(handler.getInitParameter("a")).isEqualTo("b");
    }

    @Test
    void setsSessionHandlers() {
        final SessionHandler sessionHandler = new SessionHandler();
        environment.setSessionHandler(sessionHandler);

        assertThat(handler.getSessionHandler()).isEqualTo(sessionHandler);
        assertThat(handler.isSessionsEnabled()).isTrue();
    }

    @Test
    void setsSecurityHandlers() {
        final SecurityHandler securityHandler = new ConstraintSecurityHandler();
        environment.setSecurityHandler(securityHandler);

        assertThat(handler.getSecurityHandler()).isEqualTo(securityHandler);
        assertThat(handler.isSecurityEnabled()).isTrue();
    }

    @Test
    void addsMimeMapping() {
        environment.addMimeMapping("foo", "example/foo");

        assertThat(handler.getMimeTypes().getMimeMap()).containsEntry("foo", "example/foo");
    }
}
