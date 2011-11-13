package com.yammer.dropwizard.config;

import com.google.common.collect.ImmutableMap;
import org.eclipse.jetty.servlet.FilterHolder;

import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

public class FilterMapping {
    private final FilterHolder holder;
    private final ImmutableMap.Builder<String, FilterHolder> filters;

    FilterMapping(FilterHolder holder,
                  ImmutableMap.Builder<String, FilterHolder> filters) {
        this.holder = holder;
        this.filters = filters;
    }

    public FilterMapping setInitParam(String name, String value) {
        holder.setInitParameter(checkNotNull(name), checkNotNull(value));
        return this;
    }

    public FilterMapping addInitParams(Map<String, String> params) {
        for (Map.Entry<String, String> entry : checkNotNull(params).entrySet()) {
            setInitParam(entry.getKey(), entry.getValue());
        }
        return this;
    }

    public FilterMapping addUrlPattern(String urlPattern) {
        filters.put(checkNotNull(urlPattern), holder);
        return this;
    }

    public FilterMapping addUrlPatterns(String urlPattern, String... urlPatterns) {
        addUrlPattern(checkNotNull(urlPattern));
        for (String pattern : checkNotNull(urlPatterns)) {
            addUrlPattern(checkNotNull(pattern));
        }
        return this;
    }
}
