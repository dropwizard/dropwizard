package io.dropwizard.jetty.setup;

import io.dropwizard.jetty.MutableServletContextHandler;
import org.eclipse.jetty.continuation.ContinuationFilter;
import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import javax.servlet.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class ServletEnvironmentTest {
    private final ServletHandler servletHandler = mock(ServletHandler.class);
    private final MutableServletContextHandler handler = mock(MutableServletContextHandler.class);
    private final ServletEnvironment environment = new ServletEnvironment(handler);

    @Before
    public void setUp() throws Exception {
        when(handler.getServletHandler()).thenReturn(servletHandler);
    }

    @Test
    public void addsServletInstances() throws Exception {
        final Servlet servlet = mock(Servlet.class);

        final ServletRegistration.Dynamic builder = environment.addServlet("servlet", servlet);
        assertThat(builder)
                .isNotNull();

        final ArgumentCaptor<ServletHolder> holder = ArgumentCaptor.forClass(ServletHolder.class);
        verify(servletHandler).addServlet(holder.capture());

        assertThat(holder.getValue().getName())
                .isEqualTo("servlet");

        assertThat(holder.getValue().getServlet())
                .isEqualTo(servlet);
    }

    @Test
    public void addsServletClasses() throws Exception {
        final ServletRegistration.Dynamic builder = environment.addServlet("servlet", GenericServlet.class);
        assertThat(builder)
                .isNotNull();

        final ArgumentCaptor<ServletHolder> holder = ArgumentCaptor.forClass(ServletHolder.class);
        verify(servletHandler).addServlet(holder.capture());

        assertThat(holder.getValue().getName())
                .isEqualTo("servlet");

        // this is ugly, but comparing classes sucks with these type bounds
        assertThat(holder.getValue().getHeldClass().equals(GenericServlet.class))
                .isTrue();
    }

    @Test
    public void addsFilterInstances() throws Exception {
        final Filter filter = mock(Filter.class);

        final FilterRegistration.Dynamic builder = environment.addFilter("filter", filter);
        assertThat(builder)
                .isNotNull();

        final ArgumentCaptor<FilterHolder> holder = ArgumentCaptor.forClass(FilterHolder.class);
        verify(servletHandler).addFilter(holder.capture());

        assertThat(holder.getValue().getName())
                .isEqualTo("filter");

        assertThat(holder.getValue().getFilter())
                .isEqualTo(filter);
    }

    @Test
    public void addsFilterClasses() throws Exception {
        final FilterRegistration.Dynamic builder = environment.addFilter("filter", ContinuationFilter.class);
        assertThat(builder)
                .isNotNull();

        final ArgumentCaptor<FilterHolder> holder = ArgumentCaptor.forClass(FilterHolder.class);
        verify(servletHandler).addFilter(holder.capture());

        assertThat(holder.getValue().getName())
                .isEqualTo("filter");

        // this is ugly, but comparing classes sucks with these type bounds
        assertThat(holder.getValue().getHeldClass().equals(ContinuationFilter.class))
                .isTrue();
    }

    @Test
    public void addsServletListeners() throws Exception {
        final ServletContextListener listener = mock(ServletContextListener.class);
        environment.addServletListeners(listener);

        verify(handler).addEventListener(listener);
    }

    @Test
    public void addsProtectedTargets() throws Exception {
        environment.setProtectedTargets("/woo");

        verify(handler).setProtectedTargets(new String[]{"/woo"});
    }

    @Test
    public void setsResourceBase() throws Exception {
        environment.setResourceBase("/woo");

        verify(handler).setResourceBase("/woo");
    }

    @Test
    public void setsInitParams() throws Exception {
        environment.setInitParameter("a", "b");

        verify(handler).setInitParameter("a", "b");
    }

    @Test
    public void setsSessionHandlers() throws Exception {
        final SessionHandler sessionHandler = mock(SessionHandler.class);

        environment.setSessionHandler(sessionHandler);

        verify(handler).setSessionHandler(sessionHandler);
        verify(handler).setSessionsEnabled(true);
    }


    @Test
    public void setsSecurityHandlers() throws Exception {
        final SecurityHandler securityHandler = mock(SecurityHandler.class);

        environment.setSecurityHandler(securityHandler);

        verify(handler).setSecurityHandler(securityHandler);
        verify(handler).setSecurityEnabled(true);
    }

    @Test
    public void addsMimeMapping() {
        final MimeTypes mimeTypes = mock(MimeTypes.class);
        when(handler.getMimeTypes()).thenReturn(mimeTypes);

        environment.addMimeMapping("example/foo", "foo");

        verify(mimeTypes).addMimeMapping("example/foo", "foo");
    }
}
