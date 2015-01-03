package io.dropwizard.jersey;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jersey2.InstrumentedResourceMethodApplicationListener;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import io.dropwizard.jersey.caching.CacheControlledResponseFeature;
import io.dropwizard.jersey.guava.OptionalMessageBodyWriter;
import io.dropwizard.jersey.guava.OptionalParameterInjectionBinder;
import io.dropwizard.jersey.sessions.SessionFactoryProvider;
import org.glassfish.jersey.message.GZipEncoder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.server.filter.EncodingFilter;
import org.glassfish.jersey.server.model.Resource;
import org.glassfish.jersey.server.model.ResourceMethod;
import org.glassfish.jersey.server.monitoring.ApplicationEvent;
import org.glassfish.jersey.server.monitoring.ApplicationEventListener;
import org.glassfish.jersey.server.monitoring.RequestEvent;
import org.glassfish.jersey.server.monitoring.RequestEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.ws.rs.Path;
import javax.ws.rs.ext.Provider;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class DropwizardResourceConfig extends ResourceConfig {
    private static final String NEWLINE = String.format("%n");
    private static final Logger LOGGER = LoggerFactory.getLogger(DropwizardResourceConfig.class);

    private String urlPattern;

    public DropwizardResourceConfig(MetricRegistry metricRegistry) {
        this(false, metricRegistry);
    }

    public DropwizardResourceConfig() {
        this(true, null);
    }

    @SuppressWarnings("unchecked")
    public DropwizardResourceConfig(boolean testOnly, MetricRegistry metricRegistry) {
        super();

        if (metricRegistry == null) {
            metricRegistry = new MetricRegistry();
        }

        urlPattern = "/*";

        property(ServerProperties.WADL_FEATURE_DISABLE, Boolean.TRUE);
        if (!testOnly) {
            // create a subclass to pin it to Throwable
            register(new ComponentLoggingListener(this));
        }
        register(new InstrumentedResourceMethodApplicationListener(metricRegistry));
        register(CacheControlledResponseFeature.class);
        register(OptionalMessageBodyWriter.class);
        register(new OptionalParameterInjectionBinder());
        register(new SessionFactoryProvider.Binder());
        EncodingFilter.enableFor(this, GZipEncoder.class);
    }

    public static DropwizardResourceConfig forTesting(MetricRegistry metricRegistry) {
        return new DropwizardResourceConfig(true, metricRegistry);
    }

    public void logComponents() {
        LOGGER.debug("resources = {}", canonicalNamesFilteredByAnnotation(Path.class));
        LOGGER.debug("providers = {}", canonicalNamesFilteredByAnnotation(Provider.class));
        LOGGER.info(logEndpoints());
    }

    public String getUrlPattern() {
        return urlPattern;
    }

    public void setUrlPattern(String urlPattern) {
        this.urlPattern = urlPattern;
    }

    /**
     * Combines types of getClasses() and getSingletons in one Set.
     *
     * @return all registered types
     */
    @VisibleForTesting
    Set<Class<?>> allClasses() {
        return FluentIterable.from(getSingletons()).transform(new Function<Object, Class<?>>() {
            @Override
            public Class<?> apply(Object input) {
                return input.getClass();
            }
        }).append(getClasses()).toSet();
    }

    private Iterable<String> canonicalNamesFilteredByAnnotation(final Class<? extends Annotation> annotation) {
        return FluentIterable.from(getClasses()).filter(new Predicate<Class<?>>() {
            @Override
            public boolean apply(Class<?> input) {
                return input.isAnnotationPresent(annotation);
            }
        }).transform(new Function<Class<?>, String>() {
            @Override
            public String apply(final Class<?> input) {
                return input.getCanonicalName();
            }
        });
    }

    @VisibleForTesting
    String logEndpoints() {
        final StringBuilder msg = new StringBuilder(1024);
        msg.append("The following paths were found for the configured resources:");
        msg.append(NEWLINE).append(NEWLINE);

        final Set<Class<?>> allResources = FluentIterable.from(allClasses()).filter(new Predicate<Class<?>>() {
            @Override
            public boolean apply(@Nullable Class<?> input) {
                return Resource.from(input) != null;
            }
        }).toSet();


        if (!allResources.isEmpty()) {
            for (Class<?> klass : allResources) {
                Joiner.on(NEWLINE).appendTo(msg, new EndpointLogger(urlPattern, klass).getEndpoints());
                msg.append(NEWLINE);
            }
        } else {
            msg.append("    NONE").append(NEWLINE);
        }

        return msg.toString();
    }


    /**
     * Takes care of recursively creating all registered endpoints and providing them as Collection of lines to log
     * on application start.
     */
    private static class EndpointLogger {

        private final String rootPath;
        private final List<String> endpoints = Lists.newArrayList();

        public EndpointLogger(String urlPattern, Class<?> klass) {
            this.rootPath = urlPattern.endsWith("/*") ? urlPattern.substring(0, urlPattern.length() - 1) : urlPattern;

            populateEndpoints(rootPath, klass, false);
        }

        private void populateEndpoints(String basePath, Class<?> klass, boolean isLocator) {
            populateEndpoints(basePath, klass, isLocator, Resource.from(klass));
        }

        private void populateEndpoints(String basePath, Class<?> klass, boolean isLocator, Resource resource) {
            if (!isLocator) {
                basePath = normalizePath(basePath, resource.getPath());
            }

            for (ResourceMethod method : resource.getResourceMethods()) {
                endpoints.add(formatEndpoint(method.getHttpMethod(), basePath, klass));
            }

            for (Resource childResource : resource.getChildResources()) {
                for (ResourceMethod method : childResource.getResourceMethods()) {
                    if (method.getType() == ResourceMethod.JaxrsType.RESOURCE_METHOD) {
                        final String path = normalizePath(basePath, childResource.getPath());
                        endpoints.add(formatEndpoint(method.getHttpMethod(), path, klass));
                    } else if (method.getType() == ResourceMethod.JaxrsType.SUB_RESOURCE_LOCATOR) {
                        final String path = normalizePath(basePath, childResource.getPath());
                        populateEndpoints(path, method.getInvocable().getRawResponseType(), true);
                    }
                }
            }
        }

        private String formatEndpoint(String method, String path, Class<?> klass) {
            return String.format("    %-7s %s (%s)", method, path, klass.getCanonicalName());
        }

        private String normalizePath(String basePath, String path) {
            if (basePath.endsWith("/")) {
                return path.startsWith("/") ? basePath + path.substring(1) : basePath + path;
            }
            return path.startsWith("/") ? basePath + path : basePath + "/" + path;
        }

        public Collection<String> getEndpoints() {
            return Ordering.natural().sortedCopy(endpoints);
        }
    }

    private static class ComponentLoggingListener implements ApplicationEventListener {
        final DropwizardResourceConfig config;

        public ComponentLoggingListener(DropwizardResourceConfig config) {
            this.config = config;
        }

        @Override
        public void onEvent(ApplicationEvent event) {
            if (event.getType() == ApplicationEvent.Type.INITIALIZATION_APP_FINISHED)
                this.config.logComponents();

        }

        @Override
        public RequestEventListener onRequest(RequestEvent requestEvent) {
            return null;
        }

    }
}
