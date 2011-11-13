package com.yammer.dropwizard.config;

import com.google.common.collect.ImmutableMap;
import org.eclipse.jetty.servlet.ServletHolder;

import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

public class ServletMapping {
    private final ServletHolder holder;
    private final ImmutableMap.Builder<String, ServletHolder> servlets;

    ServletMapping(ServletHolder holder,
                   ImmutableMap.Builder<String, ServletHolder> servlets) {
        this.holder = holder;
        this.servlets = servlets;
    }

    public ServletMapping setInitOrder(int order) {
        holder.setInitOrder(order);
        return this;
    }
    
    public ServletMapping setInitParam(String name, String value) {
        holder.setInitParameter(checkNotNull(name), checkNotNull(value));
        return this;
    }

    public ServletMapping addInitParams(Map<String, String> params) {
        for (Map.Entry<String, String> entry : checkNotNull(params).entrySet()) {
            setInitParam(entry.getKey(), entry.getValue());
        }
        return this;
    }

    public ServletMapping addUrlPattern(String urlPattern) {
        servlets.put(checkNotNull(urlPattern), holder);
        return this;
    }

    public ServletMapping addUrlPatterns(String urlPattern, String... urlPatterns) {
        addUrlPattern(checkNotNull(urlPattern));
        for (String pattern : checkNotNull(urlPatterns)) {
            addUrlPattern(checkNotNull(pattern));
        }
        return this;
    }
}
