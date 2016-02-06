package io.dropwizard.jersey;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jersey2.InstrumentedResourceMethodApplicationListener;
import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.TypeResolver;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ComparisonChain;
import io.dropwizard.jersey.caching.CacheControlledResponseFeature;
import io.dropwizard.jersey.params.NonEmptyStringParamFeature;
import io.dropwizard.jersey.sessions.SessionFactoryProvider;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.server.model.Resource;
import org.glassfish.jersey.server.model.ResourceMethod;
import org.glassfish.jersey.server.monitoring.ApplicationEvent;
import org.glassfish.jersey.server.monitoring.ApplicationEventListener;
import org.glassfish.jersey.server.monitoring.RequestEvent;
import org.glassfish.jersey.server.monitoring.RequestEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Path;
import javax.ws.rs.ext.Provider;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

public class DropwizardResourceConfig extends ResourceConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(DropwizardResourceConfig.class);
    private static final String NEWLINE = String.format("%n");
    private static final TypeResolver TYPE_RESOLVER = new TypeResolver();

    private String urlPattern = "/*";

    public DropwizardResourceConfig(MetricRegistry metricRegistry) {
        this(false, metricRegistry);
    }

    public DropwizardResourceConfig() {
        this(true, null);
    }

    public DropwizardResourceConfig(boolean testOnly, MetricRegistry metricRegistry) {
        super();

        if (metricRegistry == null) {
            metricRegistry = new MetricRegistry();
        }

        property(ServerProperties.WADL_FEATURE_DISABLE, Boolean.TRUE);
        if (!testOnly) {
            // create a subclass to pin it to Throwable
            register(new ComponentLoggingListener(this));
        }

        register(new InstrumentedResourceMethodApplicationListener(metricRegistry));
        register(CacheControlledResponseFeature.class);
        register(io.dropwizard.jersey.guava.OptionalMessageBodyWriter.class);
        register(io.dropwizard.jersey.guava.OptionalParamFeature.class);
        register(io.dropwizard.jersey.optional.OptionalMessageBodyWriter.class);
        register(io.dropwizard.jersey.optional.OptionalDoubleMessageBodyWriter.class);
        register(io.dropwizard.jersey.optional.OptionalIntMessageBodyWriter.class);
        register(io.dropwizard.jersey.optional.OptionalLongMessageBodyWriter.class);
        register(io.dropwizard.jersey.optional.OptionalParamFeature.class);
        register(NonEmptyStringParamFeature.class);
        register(new SessionFactoryProvider.Binder());
    }

    public static DropwizardResourceConfig forTesting(MetricRegistry metricRegistry) {
        return new DropwizardResourceConfig(true, metricRegistry);
    }

    public void logComponents() {
        LOGGER.debug("resources = {}", canonicalNamesByAnnotation(Path.class));
        LOGGER.debug("providers = {}", canonicalNamesByAnnotation(Provider.class));
        LOGGER.info(getEndpointsInfo());
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
        final Set<Class<?>> allClasses = new HashSet<>(getClasses());
        for (Object singleton : getSingletons()) {
            allClasses.add(singleton.getClass());
        }
        return allClasses;
    }

    private Set<String> canonicalNamesByAnnotation(final Class<? extends Annotation> annotation) {
        final Set<String> result = new HashSet<>();
        for (Class<?> clazz : getClasses()) {
            if (clazz.isAnnotationPresent(annotation)) {
                result.add(clazz.getCanonicalName());
            }
        }
        return result;
    }

    public String getEndpointsInfo() {
        final StringBuilder msg = new StringBuilder(1024);
        final Set<EndpointLogLine> endpointLogLines = new TreeSet<>(new EndpointComparator());

        msg.append("The following paths were found for the configured resources:");
        msg.append(NEWLINE).append(NEWLINE);

        final Set<Class<?>> allResources = new HashSet<>();
        for (Class<?> clazz : allClasses()) {
            if (!clazz.isInterface() && Resource.from(clazz) != null) {
                allResources.add(clazz);
            }
        }

        for (Class<?> klass : allResources) {
            new EndpointLogger(urlPattern, klass).populate(endpointLogLines);
        }

        if (!endpointLogLines.isEmpty()) {
            for (EndpointLogLine line : endpointLogLines) {
                msg.append(line).append(NEWLINE);
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
        private final Class<?> klass;

        EndpointLogger(String urlPattern, Class<?> klass) {
            this.rootPath = urlPattern.endsWith("/*") ? urlPattern.substring(0, urlPattern.length() - 1) : urlPattern;
            this.klass = klass;
        }

        public void populate(Set<EndpointLogLine> endpointLogLines) {
            populate(this.rootPath, klass, false, endpointLogLines);
        }

        private void populate(String basePath, Class<?> klass, boolean isLocator,
                              Set<EndpointLogLine> endpointLogLines) {
            populate(basePath, klass, isLocator, Resource.from(klass), endpointLogLines);
        }

        private void populate(String basePath, Class<?> klass, boolean isLocator, Resource resource,
                              Set<EndpointLogLine> endpointLogLines) {
            if (!isLocator) {
                basePath = normalizePath(basePath, resource.getPath());
            }

            for (ResourceMethod method : resource.getResourceMethods()) {
                endpointLogLines.add(new EndpointLogLine(method.getHttpMethod(), basePath, klass));
            }

            for (Resource childResource : resource.getChildResources()) {
                for (ResourceMethod method : childResource.getAllMethods()) {
                    if (method.getType() == ResourceMethod.JaxrsType.RESOURCE_METHOD) {
                        final String path = normalizePath(basePath, childResource.getPath());
                        endpointLogLines.add(new EndpointLogLine(method.getHttpMethod(), path, klass));
                    } else if (method.getType() == ResourceMethod.JaxrsType.SUB_RESOURCE_LOCATOR) {
                        final String path = normalizePath(basePath, childResource.getPath());
                        final ResolvedType responseType = TYPE_RESOLVER
                                .resolve(method.getInvocable().getResponseType());
                        final Class<?> erasedType = !responseType.getTypeBindings().isEmpty() ?
                                responseType.getTypeBindings().getBoundType(0).getErasedType() :
                                responseType.getErasedType();
                        populate(path, erasedType, true, endpointLogLines);
                    }
                }
            }
        }

        private static String normalizePath(String basePath, String path) {
            if (path == null) {
                return basePath;
            }
            if (basePath.endsWith("/")) {
                return path.startsWith("/") ? basePath + path.substring(1) : basePath + path;
            }
            return path.startsWith("/") ? basePath + path : basePath + "/" + path;
        }
    }

    private static class EndpointLogLine {
        private final String httpMethod;
        private final String basePath;
        private final Class<?> klass;

        EndpointLogLine(String httpMethod, String basePath, Class<?> klass) {
            this.basePath = basePath;
            this.klass = klass;
            this.httpMethod = httpMethod;
        }

        @Override
        public String toString() {
            return String.format("    %-7s %s (%s)", httpMethod, basePath, klass.getCanonicalName());
        }
    }

    private static class EndpointComparator implements Comparator<EndpointLogLine>, Serializable {
        private static final long serialVersionUID = 1L;

        @Override
        public int compare(EndpointLogLine endpointA, EndpointLogLine endpointB) {
            return ComparisonChain.start()
                .compare(endpointA.basePath, endpointB.basePath)
                .compare(endpointA.httpMethod, endpointB.httpMethod)
                .result();
        }
    }

    private static class ComponentLoggingListener implements ApplicationEventListener {
        private final DropwizardResourceConfig config;

        ComponentLoggingListener(DropwizardResourceConfig config) {
            this.config = config;
        }

        @Override
        public void onEvent(ApplicationEvent event) {
            if (event.getType() == ApplicationEvent.Type.INITIALIZATION_APP_FINISHED) {
                config.logComponents();
            }
        }

        @Override
        public RequestEventListener onRequest(RequestEvent requestEvent) {
            return null;
        }
    }
}
