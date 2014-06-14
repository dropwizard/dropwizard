package io.dropwizard.jetty.setup;

import io.dropwizard.jetty.MutableServletContextHandler;
import io.dropwizard.jetty.NonblockingServletHolder;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import javax.servlet.Servlet;
import javax.servlet.ServletRegistration;
import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;

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
     * @return a {@link javax.servlet.ServletRegistration.Dynamic} instance allowing for further
     *         configuration
     */
    public ServletRegistration.Dynamic addServlet(String name, Servlet servlet) {
        final ServletHolder holder = new NonblockingServletHolder(checkNotNull(servlet));
        holder.setName(name);
        handler.getServletHandler().addServlet(holder);

        ServletRegistration.Dynamic registration = holder.getRegistration();
        checkDuplicateRegistration(name, servlets, "servlet");

        return registration;
    }

    /**
     * Add a servlet class.
     *
     * @param name  the servlet's name
     * @param klass the servlet class
     * @return a {@link javax.servlet.ServletRegistration.Dynamic} instance allowing for further configuration
     */
    public ServletRegistration.Dynamic addServlet(String name, Class<? extends Servlet> klass) {
        final ServletHolder holder = new ServletHolder(checkNotNull(klass));
        holder.setName(name);
        handler.getServletHandler().addServlet(holder);

        ServletRegistration.Dynamic registration = holder.getRegistration();
        checkDuplicateRegistration(name, servlets, "servlet");

        return registration;
    }

    /**
     * Add a filter instance.
     *
     * @param name   the filter's name
     * @param filter the filter instance
     * @return a {@link javax.servlet.FilterRegistration.Dynamic} instance allowing for further
     *         configuration
     */
    public FilterRegistration.Dynamic addFilter(String name, Filter filter) {
        final FilterHolder holder = new FilterHolder(checkNotNull(filter));
        holder.setName(name);
        handler.getServletHandler().addFilter(holder);

        FilterRegistration.Dynamic registration = holder.getRegistration();
        checkDuplicateRegistration(name, filters, "filter");

        return registration;
    }

    /**
     * Add a filter class.
     *
     * @param name  the filter's name
     * @param klass the filter class
     * @return a {@link javax.servlet.FilterRegistration.Dynamic} instance allowing for further configuration
     */
    public FilterRegistration.Dynamic addFilter(String name, Class<? extends Filter> klass) {
        final FilterHolder holder = new FilterHolder(checkNotNull(klass));
        holder.setName(name);
        handler.getServletHandler().addFilter(holder);

        FilterRegistration.Dynamic registration = holder.getRegistration();
        checkDuplicateRegistration(name, filters, "filter");

        return registration;
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

    private void checkDuplicateRegistration(String name, Set<String> items, String type) {
        if(!items.add(name)) {
            LOGGER.warn("Overriding the existing {} registered with the name: {}", type, name);
        }
    }
}
