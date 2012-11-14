package com.yammer.dropwizard.config;

import com.google.common.collect.ImmutableMap;
import org.eclipse.jetty.servlet.ServletHolder;

import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * The configuration for a {@link javax.servlet.Servlet}.
 */
public class ServletBuilder {
    private final ServletHolder holder;
    private final ImmutableMap.Builder<String, ServletHolder> mappings;

    /**
     * Creates a new ServletBuilder.
     *
     * @param holder   the {@link ServletHolder} containing the {@link javax.servlet.Servlet}
     * @param mappings the mappings of URL patterns to {@link javax.servlet.Servlet}s
     */
    public ServletBuilder(ServletHolder holder,
                          ImmutableMap.Builder<String, ServletHolder> mappings) {
        this.holder = holder;
        this.mappings = mappings;
    }

    /**
     * Sets the servlet's name.
     *
     * @param name the name of the servlet
     * @return {@code this}
     */
    public ServletBuilder setName(String name) {
        checkArgument(!isNullOrEmpty(name), "name must be non-empty");
        holder.setName(name);
        return this;
    }

    /**
     * Sets the servlet's initialization order.
     *
     * @param order the initialization order
     * @return {@code this}
     */
    public ServletBuilder setInitOrder(int order) {
        holder.setInitOrder(order);
        return this;
    }

    /**
     * Sets the given servlet initialization parameter.
     *
     * @param name  the name of the initialization parameter
     * @param value the value of the parameter
     * @return {@code this}
     */
    public ServletBuilder setInitParam(String name, String value) {
        holder.setInitParameter(checkNotNull(name), checkNotNull(value));
        return this;
    }

    /**
     * Sets the given servlet initialization parameters.
     *
     * @param params the initialization parameters
     * @return {@code this}
     */
    public ServletBuilder addInitParams(Map<String, String> params) {
        for (Map.Entry<String, String> entry : checkNotNull(params).entrySet()) {
            setInitParam(entry.getKey(), entry.getValue());
        }
        return this;
    }

    /**
     * Adds the given URL pattern as a servlet mapping.
     *
     * @param urlPattern the URL pattern
     * @return {@code this}
     */
    public ServletBuilder addUrlPattern(String urlPattern) {
        try {
            mappings.put(checkNotNull(urlPattern), holder);
        } catch (IllegalArgumentException ignored) {
                throw new IllegalArgumentException("Can't map this servlet to " + urlPattern +
                                                             ", another servlet is already mapped to that.");
        }
        return this;
    }

    /**
     * Adds the given URL patterns as a servlet mappings.
     *
     * @param urlPattern  the URL pattern
     * @param urlPatterns additional URL patterns
     * @return {@code this}
     */
    public ServletBuilder addUrlPatterns(String urlPattern, String... urlPatterns) {
        addUrlPattern(checkNotNull(urlPattern));
        for (String pattern : checkNotNull(urlPatterns)) {
            addUrlPattern(checkNotNull(pattern));
        }
        return this;
    }
}
