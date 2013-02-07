package com.yammer.dropwizard.config;

import com.google.common.collect.ImmutableMultimap;
import org.eclipse.jetty.servlet.FilterHolder;

import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * The configuration for a servlet {@link javax.servlet.Filter}.
 */
public class FilterBuilder {
    private final FilterHolder holder;
    private final ImmutableMultimap.Builder<String, FilterHolder> mappings;

    /**
     * Creates a new FilterBuilder.
     *
     * @param holder   the {@link FilterHolder} containing the {@link javax.servlet.Filter}
     * @param mappings the mappings of URL patterns to {@link javax.servlet.Filter}s
     */
    public FilterBuilder(FilterHolder holder,
                         ImmutableMultimap.Builder<String, FilterHolder> mappings) {
        this.holder = holder;
        this.mappings = mappings;
    }

    /**
     * Sets the filter's name.
     *
     * @param name the name of the filter
     * @return {@code this}
     */
    public FilterBuilder setName(String name) {
        checkArgument(!isNullOrEmpty(name), "name must be non-empty");
        holder.setName(name);
        return this;
    }

    /**
     * Sets the given filter initialization parameter.
     *
     * @param name  the name of the initialization parameter
     * @param value the value of the parameter
     * @return {@code this}
     */
    public FilterBuilder setInitParam(String name, String value) {
        holder.setInitParameter(checkNotNull(name), checkNotNull(value));
        return this;
    }

    /**
     * Sets the given filter initialization parameters.
     *
     * @param params the initialization parameters
     * @return {@code this}
     */
    public FilterBuilder addInitParams(Map<String, String> params) {
        for (Map.Entry<String, String> entry : checkNotNull(params).entrySet()) {
            setInitParam(entry.getKey(), entry.getValue());
        }
        return this;
    }

    /**
     * Adds the given URL pattern as a filter mapping.
     *
     * @param urlPattern the URL pattern
     * @return {@code this}
     */
    public FilterBuilder addUrlPattern(String urlPattern) {
        mappings.put(checkNotNull(urlPattern), holder);
        return this;
    }

    /**
     * Adds the given URL patterns as a filter mappings.
     *
     * @param urlPattern  the URL pattern
     * @param urlPatterns additional URL patterns
     * @return {@code this}
     */
    public FilterBuilder addUrlPatterns(String urlPattern, String... urlPatterns) {
        addUrlPattern(checkNotNull(urlPattern));
        for (String pattern : checkNotNull(urlPatterns)) {
            addUrlPattern(checkNotNull(pattern));
        }
        return this;
    }
}
