package io.dropwizard.jetty.setup;

import io.dropwizard.jetty.MutableServletContextHandler;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterRegistration;
import jakarta.servlet.Servlet;
import jakarta.servlet.ServletRegistration;
import org.eclipse.jetty.ee10.servlet.FilterHolder;
import org.eclipse.jetty.ee10.servlet.ServletHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.jetty.ee10.servlet.SessionHandler;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.resource.ResourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.EventListener;
import java.util.HashSet;
import java.util.Set;

import static java.util.Objects.requireNonNull;

public class ServletEnvironment {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServletEnvironment.class);

    private final MutableServletContextHandler handler;

    private final Set<String> servlets = new HashSet<>();
    private final Set<String> filters = new HashSet<>();

    public ServletEnvironment(MutableServletContextHandler handler) {
        this.handler = handler;
    }

    /**
     * Add a servlet instance.
     *
     * @param name    the servlet's name
     * @param servlet the servlet instance
     * @return a {@link jakarta.servlet.ServletRegistration.Dynamic} instance allowing for further
     *         configuration
     */
    public ServletRegistration.Dynamic addServlet(String name, Servlet servlet) {
        final ServletHolder holder = new ServletHolder(name, servlet);
        final ServletHandler servletHandler = handler.getServletHandler();
        servletHandler.addServlet(holder);

        final ServletRegistration.Dynamic registration = holder.getRegistration();
        checkDuplicateRegistration(name, servlets, "servlet");

        return registration;
    }

    /**
     * Add a servlet class.
     *
     * @param name  the servlet's name
     * @param klass the servlet class
     * @return a {@link jakarta.servlet.ServletRegistration.Dynamic} instance allowing for further configuration
     */
    public ServletRegistration.Dynamic addServlet(String name, Class<? extends Servlet> klass) {
        final ServletHolder holder = new ServletHolder(name, klass);
        final ServletHandler servletHandler = handler.getServletHandler();
        servletHandler.addServlet(holder);

        final ServletRegistration.Dynamic registration = holder.getRegistration();
        checkDuplicateRegistration(name, servlets, "servlet");

        return registration;
    }

    /**
     * Add a filter instance.
     *
     * @param name   the filter's name
     * @param filter the filter instance
     * @return a {@link jakarta.servlet.FilterRegistration.Dynamic} instance allowing for further
     *         configuration
     */
    public FilterRegistration.Dynamic addFilter(String name, Filter filter) {
        return addFilter(name, new FilterHolder(requireNonNull(filter)));
    }

    /**
     * Add a filter class.
     *
     * @param name  the filter's name
     * @param klass the filter class
     * @return a {@link jakarta.servlet.FilterRegistration.Dynamic} instance allowing for further configuration
     */
    public FilterRegistration.Dynamic addFilter(String name, Class<? extends Filter> klass) {
        return addFilter(name, new FilterHolder(requireNonNull(klass)));
    }

    private FilterRegistration.Dynamic addFilter(String name, FilterHolder holder) {
        holder.setName(name);
        handler.getServletHandler().addFilter(holder);

        final FilterRegistration.Dynamic registration = holder.getRegistration();
        checkDuplicateRegistration(name, filters, "filter");

        return registration;
    }

    /**
     * Add one or more servlet event listeners.
     *
     * @param listeners one or more listener instances that implement {@link
     *                  jakarta.servlet.ServletContextListener}, {@link jakarta.servlet.ServletContextAttributeListener},
     *                  {@link jakarta.servlet.ServletRequestListener} or {@link
     *                  jakarta.servlet.ServletRequestAttributeListener}
     */
    public void addServletListeners(EventListener... listeners) {
        for (EventListener listener : listeners) {
            handler.addEventListener(listener);
        }
    }

    /**
     * Set protected targets.
     *
     * @param targets Array of URL prefix. Each prefix is in the form /path and
     *                will match either /path exactly or /path/anything
     */
    public void setProtectedTargets(String... targets) {
        handler.setProtectedTargets(Arrays.copyOf(targets, targets.length));
    }

    /**
     * Sets the base resource for this context.
     *
     * @param baseResource The resource to be used as the base for all static content of this context.
     */
    public void setBaseResource(Resource baseResource) {
        handler.setBaseResource(baseResource);
    }

    /**
     * Sets the base resources for this context.
     *
     * @param baseResources The list of resources to be used as the base for all static
     *                      content of this context.
     */
    public void setBaseResource(Resource... baseResources) {
        handler.setBaseResource(ResourceFactory.combine(baseResources));
    }

    /**
     * Sets the base resources for this context.
     *
     * @param resources A list of strings representing the base resources to serve static
     *                  content for the context. Any string accepted by Resource.newResource(String)
     *                  may be passed and the call is equivalent to {@link #setBaseResource(Resource...)}}
     */
    public void setBaseResource(String... resources) {
        handler.setBaseResource(
            ResourceFactory.combine(
                Arrays.stream(resources).map(handler::newResource).toList()
            )
        );
    }

    /**
     * Sets the base resource for this context.
     * @param resourceBase A string representing the base resource for the context. Any
     *                     string accepted by Resource.newResource(String) may be passed
     *                     and the call is equivalent to {@link #setBaseResource(Resource)}}
     */
    public void setResourceBase(String resourceBase) {
        handler.setBaseResource(handler.newResource(resourceBase));
    }

    /**
     * Set an initialization parameter.
     *
     * @param name  Parameter name
     * @param value Parameter value
     */
    public void setInitParameter(String name, String value) {
        handler.setInitParameter(name, value);
    }

    /**
     * Set the session handler.
     *
     * @param sessionHandler The sessionHandler to set.
     */
    public void setSessionHandler(SessionHandler sessionHandler) {
        handler.setSessionsEnabled(sessionHandler != null);
        handler.setSessionHandler(sessionHandler);
    }

    /**
     * Set the security handler.
     *
     * @param securityHandler The securityHandler to set.
     */
    public void setSecurityHandler(SecurityHandler securityHandler) {
        handler.setSecurityEnabled(securityHandler != null);
        handler.setSecurityHandler(securityHandler);
    }

    /**
     * Set a mime mapping.
     *
     * @param extension Extension
     * @param type      Mime type
     */
    public void addMimeMapping(String extension, String type) {
        handler.getMimeTypes().addMimeMapping(extension, type);
    }

    private void checkDuplicateRegistration(String name, Set<String> items, String type) {
        if (!items.add(name)) {
            LOGGER.warn("Overriding the existing {} registered with the name: {}", type, name);
        }
    }
}
