package io.dropwizard.jersey;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jersey2.InstrumentedResourceMethodApplicationListener;
import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.TypeResolver;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import io.dropwizard.jersey.caching.CacheControlledResponseFeature;
import io.dropwizard.jersey.guava.OptionalMessageBodyWriter;
import io.dropwizard.jersey.guava.OptionalParamFeature;
import io.dropwizard.jersey.params.NonEmptyStringParamFeature;
import io.dropwizard.jersey.sessions.SessionFactoryProvider;
import io.dropwizard.jersey.validation.HibernateValidationFeature;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.server.model.Resource;
import org.glassfish.jersey.server.model.ResourceMethod;
import org.glassfish.jersey.server.monitoring.ApplicationEvent;
import org.glassfish.jersey.server.monitoring.ApplicationEventListener;
import org.glassfish.jersey.server.monitoring.RequestEvent;
import org.glassfish.jersey.server.monitoring.RequestEventListener;
import org.glassfish.jersey.server.validation.ValidationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

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
        register(OptionalMessageBodyWriter.class);
        register(OptionalParamFeature.class);
        register(NonEmptyStringParamFeature.class);
        register(new SessionFactoryProvider.Binder());
        register(HibernateValidationFeature.class);
        register(ValidationFeature.class);
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
        final Set<Class<?>> allClasses = Sets.newHashSet(getClasses());
        for (Object singleton : getSingletons()) {
            allClasses.add(singleton.getClass());
        }
        return allClasses;
    }

    private Set<String> canonicalNamesByAnnotation(final Class<? extends Annotation> annotation) {
        final Set<String> result = Sets.newHashSet();
        for (Class<?> clazz : getClasses()) {
            if (clazz.isAnnotationPresent(annotation)) {
                result.add(clazz.getCanonicalName());
            }
        }
        return result;
    }

    public String getEndpointsInfo() {
        final StringBuilder msg = new StringBuilder(1024);
        final List<EndpointLogLine> endpointLogLines = Lists.newArrayList();

        msg.append("The following paths were found for the configured resources:");
        msg.append(NEWLINE).append(NEWLINE);

        final Set<Class<?>> allResources = Sets.newHashSet();
        for (Class<?> clazz : allClasses()) {
            if (!clazz.isInterface() && Resource.from(clazz) != null) {
                allResources.add(clazz);
            }
        }

        for (Class<?> klass : allResources) {
            new EndpointLogger(urlPattern, klass).populate(endpointLogLines);
        }

        if (!endpointLogLines.isEmpty()) {
            Collections.sort(endpointLogLines, new EndpointComparator());
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

        public EndpointLogger(String urlPattern, Class<?> klass) {
            this.rootPath = urlPattern.endsWith("/*") ? urlPattern.substring(0, urlPattern.length() - 1) : urlPattern;
            this.klass = klass;
        }

        public void populate(List<EndpointLogLine> endpointLogLines) {
            populate(this.rootPath, klass, false, endpointLogLines);
        }

        private void populate(String basePath, Class<?> klass, boolean isLocator, List<EndpointLogLine> endpointLogLines) {
            populate(basePath, klass, isLocator, Resource.from(klass), endpointLogLines);
        }

        private void populate(String basePath, Class<?> klass, boolean isLocator, Resource resource, List<EndpointLogLine> endpointLogLines) {
            if (!isLocator) {
                basePath = normalizePath(basePath, resource.getPath());
            }

            for (ResourceMethod method : resource.getResourceMethods()) {
                populateList(endpointLogLines, method, new EndpointLogLine(method.getHttpMethod(), basePath, klass));
            }

            for (Resource childResource : resource.getChildResources()) {
                for (ResourceMethod method : childResource.getAllMethods()) {
                    if (method.getType() == ResourceMethod.JaxrsType.RESOURCE_METHOD) {
                        final String path = normalizePath(basePath, childResource.getPath());
                        populateList(endpointLogLines, method, new EndpointLogLine(method.getHttpMethod(), path, klass));
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

        private void populateList(List<EndpointLogLine> endpointLogLines, ResourceMethod method, EndpointLogLine candidateLogLine) {
            int indexInLogLineList = endpointLogLines.indexOf(candidateLogLine);
            if (indexInLogLineList >= 0) {
                candidateLogLine = endpointLogLines.get(indexInLogLineList);
            }
            candidateLogLine.consumedMediaTypes.addAll(method.getConsumedTypes());
            candidateLogLine.producedMediaTypes.addAll(method.getProducedTypes());
            if (indexInLogLineList >= 0) {
                endpointLogLines.set(indexInLogLineList, candidateLogLine);
            } else {
                endpointLogLines.add(candidateLogLine);
            }
        }

        private String normalizePath(String basePath, String path) {
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
        private final List<MediaType> consumedMediaTypes = Lists.newArrayList();
        private final List<MediaType> producedMediaTypes = Lists.newArrayList();

        public EndpointLogLine(String httpMethod, String basePath, Class<?> klass) {
            this.basePath = basePath;
            this.klass = klass;
            this.httpMethod = httpMethod;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder(String.format("    %-7s %s (%s)", httpMethod, basePath, klass.getCanonicalName()));
            if(!consumedMediaTypes.isEmpty()){
                builder.append(System.lineSeparator() + String.format("         Accepts  -  %s", Arrays.toString(consumedMediaTypes.toArray())));
            }
            if(!producedMediaTypes.isEmpty()){
                builder.append(System.lineSeparator() + String.format("         Produces -  %s", Arrays.toString(producedMediaTypes.toArray())));
            }
            return builder.toString();
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(httpMethod, basePath, klass);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            EndpointLogLine other = (EndpointLogLine) obj;
            if (basePath == null) {
                if (other.basePath != null)
                    return false;
            } else if (!basePath.equals(other.basePath))
                return false;
            if (httpMethod == null) {
                if (other.httpMethod != null)
                    return false;
            } else if (!httpMethod.equals(other.httpMethod))
                return false;
            if (klass == null) {
                if (other.klass != null)
                    return false;
            } else if (klass != other.klass)
                return false;
            return true;
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

        public ComponentLoggingListener(DropwizardResourceConfig config) {
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
