package com.codahale.dropwizard.jetty.setup;

import com.codahale.dropwizard.jetty.MutableServletContextHandler;
import com.codahale.dropwizard.jetty.NonblockingServletHolder;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletHolder;

import javax.servlet.Filter;
import javax.servlet.Servlet;
import java.util.Arrays;
import java.util.EventListener;

import static com.google.common.base.Preconditions.checkNotNull;

public class ServletEnvironment {
    private final MutableServletContextHandler handler;

    public ServletEnvironment(MutableServletContextHandler handler) {
        this.handler = handler;
    }

    /**
     * Add a servlet instance.
     *
     * @param servlet    the servlet instance
     * @param urlPattern the URL pattern for requests that should be handled by {@code servlet}
     * @return a {@link ServletBuilder} instance allowing for further
     *         configuration
     */
    public ServletBuilder addServlet(Servlet servlet,
                                     String urlPattern) {
        final ServletHolder holder = new NonblockingServletHolder(checkNotNull(servlet));
        final ServletBuilder builder = new ServletBuilder(holder, handler);
        builder.addUrlPattern(checkNotNull(urlPattern));
        return builder;
    }

    /**
     * Add a servlet class.
     *
     * @param klass      the servlet class
     * @param urlPattern the URL pattern for requests that should be handled by instances of {@code
     *                   klass}
     * @return a {@link ServletBuilder} instance allowing for further configuration
     */
    public ServletBuilder addServlet(Class<? extends Servlet> klass,
                                     String urlPattern) {
        final ServletHolder holder = new ServletHolder(checkNotNull(klass));
        final ServletBuilder builder = new ServletBuilder(holder, handler);
        builder.addUrlPattern(checkNotNull(urlPattern));
        return builder;
    }

    /**
     * Add a filter instance.
     *
     * @param filter     the filter instance
     * @param urlPattern the URL pattern for requests that should be handled by {@code filter}
     * @return a {@link FilterBuilder} instance allowing for further
     *         configuration
     */
    public FilterBuilder addFilter(Filter filter,
                                   String urlPattern) {
        final FilterHolder holder = new FilterHolder(checkNotNull(filter));
        final FilterBuilder builder = new FilterBuilder(holder, handler);
        builder.addUrlPattern(checkNotNull(urlPattern));
        return builder;
    }

    /**
     * Add a filter class.
     *
     * @param klass      the filter class
     * @param urlPattern the URL pattern for requests that should be handled by instances of {@code
     *                   klass}
     * @return a {@link FilterBuilder} instance allowing for further configuration
     */
    public FilterBuilder addFilter(Class<? extends Filter> klass,
                                   String urlPattern) {
        final FilterHolder holder = new FilterHolder(checkNotNull(klass));
        final FilterBuilder filterConfig = new FilterBuilder(holder, handler);
        filterConfig.addUrlPattern(checkNotNull(urlPattern));
        return filterConfig;
    }

    /**
     * Add one or more servlet event listeners.
     *
     * @param listeners one or more listener instances that implement {@link
     *                  javax.servlet.ServletContextListener}, {@link javax.servlet.ServletContextAttributeListener},
     *                  {@link javax.servlet.ServletRequestListener} or {@link
     *                  javax.servlet.ServletRequestAttributeListener}
     */
    public void addServletListeners(EventListener... listeners) {
        for (EventListener listener : listeners) {
            handler.addEventListener(listener);
        }
    }

    public void setProtectedTargets(String... targets) {
        handler.setProtectedTargets(Arrays.copyOf(targets, targets.length));
    }

    public void setResourceBase(String resourceBase) {
        handler.setResourceBase(resourceBase);
    }

    public void setInitParameter(String name, String value) {
        handler.setInitParameter(name, value);
    }

    public void setSessionHandler(SessionHandler sessionHandler) {
        handler.setSessionsEnabled(sessionHandler != null);
        handler.setSessionHandler(sessionHandler);
    }

    public void setSecurityHandler(SecurityHandler securityHandler) {
        handler.setSecurityEnabled(securityHandler != null);
        handler.setSecurityHandler(securityHandler);
    }

    public void addMimeMapping(String extension, String type) {
        handler.getMimeTypes().addMimeMapping(extension, type);
    }
}
