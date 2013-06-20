package com.codahale.dropwizard.jetty.setup;

import com.codahale.dropwizard.jetty.MutableServletContextHandler;
import com.codahale.dropwizard.jetty.NonblockingServletHolder;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletHolder;

import javax.servlet.*;
import java.util.Arrays;
import java.util.EnumSet;
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
     * @return a {@link javax.servlet.ServletRegistration.Dynamic} instance allowing for further
     *         configuration
     */
    public ServletRegistration.Dynamic addServlet(Servlet servlet,
                                                  String urlPattern) {
        final ServletHolder holder = new NonblockingServletHolder(checkNotNull(servlet));
        handler.addServlet(holder, checkNotNull(urlPattern));
        return holder.getRegistration();
    }

    /**
     * Add a servlet class.
     *
     * @param klass      the servlet class
     * @param urlPattern the URL pattern for requests that should be handled by instances of {@code
     *                   klass}
     * @return a {@link javax.servlet.ServletRegistration.Dynamic} instance allowing for further configuration
     */
    public ServletRegistration.Dynamic addServlet(Class<? extends Servlet> klass,
                                                  String urlPattern) {
        final ServletHolder holder = new ServletHolder(checkNotNull(klass));
        handler.addServlet(holder, checkNotNull(urlPattern));
        return holder.getRegistration();
    }

    /**
     * Add a filter instance.
     *
     * @param filter          the filter instance
     * @param urlPattern      the URL pattern for requests that should be handled by {@code filter}
     * @param dispatcherTypes the set of dispatcher types
     * @return a {@link FilterRegistration.Dynamic} instance allowing for further
     *         configuration
     */
    public FilterRegistration.Dynamic addFilter(Filter filter,
                                                String urlPattern,
                                                EnumSet<DispatcherType> dispatcherTypes) {
        final FilterHolder holder = new FilterHolder(checkNotNull(filter));
        handler.addFilter(holder, checkNotNull(urlPattern), dispatcherTypes);
        return holder.getRegistration();
    }

    /**
     * Add a filter instance.
     *
     * @param filter     the filter instance
     * @param urlPattern the URL pattern for requests that should be handled by {@code filter}
     * @return a {@link FilterRegistration.Dynamic} instance allowing for further
     *         configuration
     */
    public FilterRegistration.Dynamic addFilter(Filter filter, String urlPattern) {
        return addFilter(filter, urlPattern, EnumSet.of(DispatcherType.REQUEST));
    }

    /**
     * Add a filter class.
     *
     * @param klass           the filter class
     * @param urlPattern      the URL pattern for requests that should be handled by instances of
     *                        {@code klass}
     * @param dispatcherTypes the set of dispatcher types
     * @return a {@link FilterRegistration.Dynamic} instance allowing for further configuration
     */
    public FilterRegistration.Dynamic addFilter(Class<? extends Filter> klass,
                                                String urlPattern,
                                                EnumSet<DispatcherType> dispatcherTypes) {
        final FilterHolder holder = new FilterHolder(checkNotNull(klass));
        handler.addFilter(holder, checkNotNull(urlPattern), dispatcherTypes);
        return holder.getRegistration();
    }

    /**
     * Add a filter class.
     *
     * @param klass      the filter class
     * @param urlPattern the URL pattern for requests that should be handled by instances of
     *                   {@code klass}
     * @return a {@link FilterRegistration.Dynamic} instance allowing for further configuration
     */
    public FilterRegistration.Dynamic addFilter(Class<? extends Filter> klass, String urlPattern) {
        return addFilter(klass, urlPattern, EnumSet.of(DispatcherType.REQUEST));
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
