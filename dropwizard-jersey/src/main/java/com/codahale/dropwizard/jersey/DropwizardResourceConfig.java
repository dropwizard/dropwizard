package com.codahale.dropwizard.jersey;

import com.codahale.dropwizard.jersey.caching.CacheControlledResourceMethodDispatchAdapter;
import com.codahale.dropwizard.jersey.errors.LoggingExceptionMapper;
import com.codahale.dropwizard.jersey.guava.OptionalQueryParamInjectableProvider;
import com.codahale.dropwizard.jersey.guava.OptionalResourceMethodDispatchAdapter;
import com.codahale.dropwizard.jersey.jackson.JsonProcessingExceptionMapper;
import com.codahale.dropwizard.jersey.validation.ConstraintViolationExceptionMapper;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jersey.InstrumentedResourceMethodDispatchAdapter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.sun.jersey.api.core.ScanningResourceConfig;
import com.sun.jersey.api.model.AbstractResource;
import com.sun.jersey.api.model.AbstractResourceMethod;
import com.sun.jersey.api.model.AbstractSubResourceLocator;
import com.sun.jersey.api.model.AbstractSubResourceMethod;
import com.sun.jersey.server.impl.modelapi.annotation.IntrospectionModeller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Path;
import javax.ws.rs.ext.Provider;
import java.util.List;

public class DropwizardResourceConfig extends ScanningResourceConfig {
    private static final String NEWLINE = String.format("%n");
    private static final Logger LOGGER = LoggerFactory.getLogger(DropwizardResourceConfig.class);
    private String urlPattern;

    public static DropwizardResourceConfig forTesting(MetricRegistry metricRegistry) {
        return new DropwizardResourceConfig(true, metricRegistry);
    }

    public DropwizardResourceConfig(MetricRegistry metricRegistry) {
        this(false, metricRegistry);
    }

    private DropwizardResourceConfig(boolean testOnly, MetricRegistry metricRegistry) {
        super();
        urlPattern = "/*";
        getFeatures().put(FEATURE_DISABLE_WADL, Boolean.TRUE);
        if (!testOnly) {
            // create a subclass to pin it to Throwable
            getSingletons().add(new LoggingExceptionMapper<Throwable>() {});
            getSingletons().add(new ConstraintViolationExceptionMapper());
            getSingletons().add(new JsonProcessingExceptionMapper());
        }
        getSingletons().add(new InstrumentedResourceMethodDispatchAdapter(metricRegistry));
        getClasses().add(CacheControlledResourceMethodDispatchAdapter.class);
        getClasses().add(OptionalResourceMethodDispatchAdapter.class);
        getClasses().add(OptionalQueryParamInjectableProvider.class);
    }

    @Override
    public void validate() {
        super.validate();

        logResources();
        logProviders();
        logEndpoints();
    }

    private void logResources() {
        final ImmutableSet.Builder<String> builder = ImmutableSet.builder();

        for (Class<?> klass : getClasses()) {
            if (klass.isAnnotationPresent(Path.class)) {
                builder.add(klass.getCanonicalName());
            }
        }

        for (Object o : getSingletons()) {
            if (o.getClass().isAnnotationPresent(Path.class)) {
                builder.add(o.getClass().getCanonicalName());
            }
        }

        LOGGER.debug("resources = {}", builder.build());
    }

    private void logProviders() {
        final ImmutableSet.Builder<String> builder = ImmutableSet.builder();

        for (Class<?> klass : getClasses()) {
            if (klass.isAnnotationPresent(Provider.class)) {
                builder.add(klass.getCanonicalName());
            }
        }

        for (Object o : getSingletons()) {
            if (o.getClass().isAnnotationPresent(Provider.class)) {
                builder.add(o.getClass().getCanonicalName());
            }
        }

        LOGGER.debug("providers = {}", builder.build());
    }

    private void logEndpoints() {
        final StringBuilder msg = new StringBuilder(1024);
        msg.append("The following paths were found for the configured resources:");
        msg.append(NEWLINE).append(NEWLINE);

        final ImmutableList.Builder<Class<?>> builder = ImmutableList.builder();
        for (Object o : getSingletons()) {
            if (o.getClass().isAnnotationPresent(Path.class)) {
                builder.add(o.getClass());
            }
        }
        for (Class<?> klass : getClasses()) {
            if (klass.isAnnotationPresent(Path.class)) {
                builder.add(klass);
            }
        }

        String rootPath = urlPattern;
        if (rootPath.endsWith("/*")) {
            rootPath = rootPath.substring(0, rootPath.length() - 1);
        }

        for (Class<?> klass : builder.build()) {
            List<String> endpoints = Lists.newArrayList();
            populateEndpoints(endpoints, rootPath, klass, false);

            for (String line : Ordering.natural().sortedCopy(endpoints)) {
                msg.append(line).append(NEWLINE);
            }
        }

        LOGGER.info(msg.toString());
    }

    private void populateEndpoints(List<String> endpoints, String basePath, Class<?> klass,
                                   boolean isLocator) {
        AbstractResource resource = IntrospectionModeller.createResource(klass);

        if (!isLocator) {
            basePath = normalizePath(basePath, resource.getPath().getValue());
        }

        for (AbstractResourceMethod method : resource.getResourceMethods()) {
            endpoints.add(formatEndpoint(method.getHttpMethod(), basePath, klass));
        }

        for (AbstractSubResourceMethod method : resource.getSubResourceMethods()) {
            String path = normalizePath(basePath, method.getPath().getValue());
            endpoints.add(formatEndpoint(method.getHttpMethod(), path, klass));
        }

        for (AbstractSubResourceLocator locator : resource.getSubResourceLocators()) {
            String path = normalizePath(basePath, locator.getPath().getValue());
            populateEndpoints(endpoints, path, locator.getMethod().getReturnType(), true);
        }
    }

    private String formatEndpoint(String method, String path, Class<?> klass) {
        return String.format("    %-7s %s (%s)", method, path, klass.getCanonicalName());
    }

    private String normalizePath(String basePath, String path) {
        final String result;
        if (basePath.endsWith("/")) {
            if (path.startsWith("/")) {
                result = basePath + path.substring(1);
            } else {
                result = basePath + path;
            }
        } else {
            if (path.startsWith("/")) {
                result = basePath + path;
            } else {
                result = basePath + "/" + path;
            }
        }
        return result;
    }

    public String getUrlPattern() {
        return urlPattern;
    }

    public void setUrlPattern(String urlPattern) {
        this.urlPattern = urlPattern;
    }
}
