package io.dropwizard.jetty.setup;

import io.dropwizard.jetty.MutableServletContextHandler;
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
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import javax.servlet.Servlet;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRegistration;

import static org.assertj.core.api.Assertions.assertThat;

public class ServletEnvironmentTest {

    private MutableServletContextHandler handler;
    private ServletHandler servletHandler;
    private ServletEnvironment environment;

    @Rule
    public TemporaryFolder tempDir = new TemporaryFolder();

    @Before
    public void setUp() {
        handler = new MutableServletContextHandler();
        servletHandler = handler.getServletHandler();
        environment = new ServletEnvironment(handler);
    }

    @Test
    public void addsServletInstances() throws Exception {
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
    public void addsServletClasses() throws Exception {
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
    public void addsFilterInstances() throws Exception {
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
    public void addsFilterClasses() throws Exception {
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
    public void addsServletListeners() {
        final ServletContextListener listener = new DebugListener();
        environment.addServletListeners(listener);

        assertThat(handler.getEventListeners()).contains(listener);
    }

    @Test
    public void addsProtectedTargets() {
        environment.setProtectedTargets("/woo");

        assertThat(handler.getProtectedTargets()).contains("/woo");
    }

    @Test
    public void setsBaseResource() throws Exception {
        final Resource testResource = Resource.newResource(tempDir.newFolder());
        environment.setBaseResource(testResource);

        assertThat(handler.getBaseResource()).isEqualTo(testResource);
    }

    @Test
    public void setsBaseResourceList() throws Exception {
        Resource wooResource = Resource.newResource(tempDir.newFolder());
        Resource fooResource = Resource.newResource(tempDir.newFolder());

        final Resource[] testResources = new Resource[]{wooResource, fooResource};
        environment.setBaseResource(testResources);

        assertThat(handler.getBaseResource()).isExactlyInstanceOf(ResourceCollection.class);
        assertThat(((ResourceCollection) handler.getBaseResource()).getResources()).contains(wooResource, fooResource);
    }

    @Test
    public void setsResourceBase() throws Exception {
        environment.setResourceBase("/woo");

        assertThat(handler.getResourceBase()).isEqualTo(handler.newResource("/woo").toString());
    }

    @Test
    public void setsBaseResourceStringList() throws Exception {
        String wooResource = tempDir.newFolder().getAbsolutePath();
        String fooResource = tempDir.newFolder().getAbsolutePath();

        final String[] testResources = new String[]{wooResource, fooResource};
        environment.setBaseResource(testResources);

        assertThat(handler.getBaseResource()).isExactlyInstanceOf(ResourceCollection.class);
        assertThat(((ResourceCollection) handler.getBaseResource()).getResources())
            .contains(Resource.newResource(wooResource), Resource.newResource(fooResource));
    }

    @Test
    public void setsInitParams() {
        environment.setInitParameter("a", "b");

        assertThat(handler.getInitParameter("a")).isEqualTo("b");
    }

    @Test
    public void setsSessionHandlers() {
        final SessionHandler sessionHandler = new SessionHandler();
        environment.setSessionHandler(sessionHandler);

        assertThat(handler.getSessionHandler()).isEqualTo(sessionHandler);
        assertThat(handler.isSessionsEnabled()).isTrue();
    }


    @Test
    public void setsSecurityHandlers() {
        final SecurityHandler securityHandler = new ConstraintSecurityHandler();
        environment.setSecurityHandler(securityHandler);

        assertThat(handler.getSecurityHandler()).isEqualTo(securityHandler);
        assertThat(handler.isSecurityEnabled()).isTrue();
    }

    @Test
    public void addsMimeMapping() {
        environment.addMimeMapping("foo", "example/foo");

        assertThat(handler.getMimeTypes().getMimeMap()).containsEntry("foo", "example/foo");
    }
}
