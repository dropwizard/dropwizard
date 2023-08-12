package io.dropwizard.jetty.setup;

import io.dropwizard.jetty.MutableServletContextHandler;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterRegistration;
import jakarta.servlet.Servlet;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.ServletRegistration;
import jakarta.servlet.http.HttpServletRequest;
import org.eclipse.jetty.ee10.servlet.DebugListener;
import org.eclipse.jetty.ee10.servlet.FilterHolder;
import org.eclipse.jetty.ee10.servlet.ServletHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.jetty.ee10.servlet.SessionHandler;
import org.eclipse.jetty.ee10.servlet.security.ConstraintSecurityHandler;
import org.eclipse.jetty.ee10.servlets.DoSFilter;
import org.eclipse.jetty.ee10.servlets.EventSource;
import org.eclipse.jetty.ee10.servlets.EventSourceServlet;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.util.resource.CombinedResource;
import org.eclipse.jetty.util.resource.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

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
        final Servlet servlet = new TestEventSourceServlet();
        final ServletRegistration.Dynamic builder = environment.addServlet("servlet", servlet);
        assertThat(builder).isNotNull();

        try {
            handler.start();
            servletHandler.start();

            final ServletHolder servletHolder = servletHandler.getServlet("servlet");
            assertThat(servletHolder.getServlet()).isEqualTo(servlet);
        } finally {
            servletHandler.stop();
            handler.stop();
        }
    }

    @Test
    void addsServletClasses() throws Exception {
        final Class<TestEventSourceServlet> servletClass = TestEventSourceServlet.class;
        final ServletRegistration.Dynamic builder = environment.addServlet("servlet", servletClass);
        assertThat(builder).isNotNull();

        try {
            handler.start();
            servletHandler.start();

            final ServletHolder servletHolder = servletHandler.getServlet("servlet");
            assertThat(servletHolder.getServlet()).isExactlyInstanceOf(servletClass);
        } finally {
            servletHandler.stop();
            handler.stop();
        }
    }

    @Test
    void addsFilterInstances() throws Exception {
        final Filter filter = new DoSFilter();

        final FilterRegistration.Dynamic builder = environment.addFilter("filter", filter);
        assertThat(builder).isNotNull();

        try {
            handler.start();
            servletHandler.start();

            final FilterHolder filterHolder = servletHandler.getFilter("filter");
            assertThat(filterHolder.getFilter()).isEqualTo(filter);
        } finally {
            servletHandler.stop();
            handler.stop();
        }
    }

    @Test
    void addsFilterClasses() throws Exception {
        final Class<DoSFilter> filterClass = DoSFilter.class;
        final FilterRegistration.Dynamic builder = environment.addFilter("filter", filterClass);
        assertThat(builder).isNotNull();

        try {
            handler.start();
            servletHandler.start();

            final FilterHolder filterHolder = servletHandler.getFilter("filter");
            assertThat(filterHolder.getFilter()).isExactlyInstanceOf(filterClass);
        } finally {
            servletHandler.stop();
            handler.stop();
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
        final Resource testResource = handler.newResource(tempDir.resolve("dir").toUri());
        environment.setBaseResource(testResource);

        assertThat(handler.getBaseResource()).isEqualTo(testResource);
    }

    @Test
    void setsBaseResourceList(@TempDir Path tempDir) throws Exception {
        Resource wooResource = handler.newResource(Files.createDirectory(tempDir.resolve("dir-1")).toUri());
        Resource fooResource = handler.newResource(Files.createDirectory(tempDir.resolve("dir-2")).toUri());

        final Resource[] testResources = new Resource[]{wooResource, fooResource};
        environment.setBaseResource(testResources);

        assertThat(handler.getBaseResource()).isExactlyInstanceOf(CombinedResource.class);
        assertThat(((CombinedResource) handler.getBaseResource()).getResources()).contains(wooResource, fooResource);
    }

    @Test
    void setsResourceBase() throws Exception {
        environment.setResourceBase("/woo");

        assertThat(handler.getBaseResource()).isEqualTo(handler.newResource("/woo"));
    }

    @Test
    void setsBaseResourceStringList(@TempDir Path tempDir) throws Exception {
        String wooResource = Files.createDirectory(tempDir.resolve("dir-1")).toString();
        String fooResource = Files.createDirectory(tempDir.resolve("dir-2")).toString();

        final String[] testResources = new String[]{wooResource, fooResource};
        environment.setBaseResource(testResources);

        assertThat(handler.getBaseResource()).isExactlyInstanceOf(CombinedResource.class);
        assertThat(((CombinedResource) handler.getBaseResource()).getResources())
            .contains(handler.newResource(wooResource), handler.newResource(fooResource));
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

    public static final class TestEventSourceServlet extends EventSourceServlet {
        @Override
        protected EventSource newEventSource(HttpServletRequest httpServletRequest) {
            return new EventSource() {
                @Override
                public void onOpen(Emitter emitter) throws IOException {
                }

                @Override
                public void onClose() {
                }
            };
        }
    }
}
