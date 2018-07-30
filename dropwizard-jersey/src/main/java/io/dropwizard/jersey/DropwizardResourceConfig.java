package io.dropwizard.jersey;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jersey2.InstrumentedResourceMethodApplicationListener;
import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.TypeResolver;
import io.dropwizard.jersey.caching.CacheControlledResponseFeature;
import io.dropwizard.jersey.params.AbstractParamConverterProvider;
import io.dropwizard.jersey.sessions.SessionFactoryProvider;
import io.dropwizard.jersey.validation.FuzzyEnumParamConverterProvider;
import io.dropwizard.util.Strings;
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

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class DropwizardResourceConfig extends ResourceConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(DropwizardResourceConfig.class);
    private static final String NEWLINE = String.format("%n");
    private static final TypeResolver TYPE_RESOLVER = new TypeResolver();

    private static final Pattern PATH_DIRTY_SLASHES = Pattern.compile("\\s*/\\s*/+\\s*");

    private String urlPattern = "/*";
    private String contextPath = "/";
    private final ComponentLoggingListener loggingListener = new ComponentLoggingListener(this);

    public DropwizardResourceConfig() {
        this(null);
    }

    public DropwizardResourceConfig(@Nullable MetricRegistry metricRegistry) {
        super();

        if (metricRegistry == null) {
            metricRegistry = new MetricRegistry();
        }

        property(ServerProperties.WADL_FEATURE_DISABLE, Boolean.TRUE);
        register(loggingListener);

        register(new InstrumentedResourceMethodApplicationListener(metricRegistry));
        register(CacheControlledResponseFeature.class);
        register(io.dropwizard.jersey.guava.OptionalMessageBodyWriter.class);
        register(new io.dropwizard.jersey.guava.OptionalParamBinder());
        register(io.dropwizard.jersey.optional.OptionalMessageBodyWriter.class);
        register(io.dropwizard.jersey.optional.OptionalDoubleMessageBodyWriter.class);
        register(io.dropwizard.jersey.optional.OptionalIntMessageBodyWriter.class);
        register(io.dropwizard.jersey.optional.OptionalLongMessageBodyWriter.class);
        register(new io.dropwizard.jersey.optional.OptionalParamBinder());
        register(AbstractParamConverterProvider.class);
        register(new FuzzyEnumParamConverterProvider());
        register(new SessionFactoryProvider.Binder());
    }

    /**
     * Build a {@link DropwizardResourceConfig} which makes Jersey Test run on a random port,
     * also see {@code org.glassfish.jersey.test.TestProperties#CONTAINER_PORT}.
     */
    public static DropwizardResourceConfig forTesting() {
        return forTesting(null);
    }

    /**
     * Build a {@link DropwizardResourceConfig} which makes Jersey Test run on a random port,
     * also see {@code org.glassfish.jersey.test.TestProperties#CONTAINER_PORT}.
     */
    public static DropwizardResourceConfig forTesting(@Nullable MetricRegistry metricRegistry) {
        final DropwizardResourceConfig config = new DropwizardResourceConfig(metricRegistry);
        // See org.glassfish.jersey.test.TestProperties#CONTAINER_PORT
        config.property("jersey.config.test.container.port", "0");
        return config;
    }

    public String getUrlPattern() {
        return urlPattern;
    }

    public void setUrlPattern(String urlPattern) {
        this.urlPattern = urlPattern;
    }

    public String getContextPath() {
        return contextPath;
    }

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    public String getEndpointsInfo() {
        return loggingListener.getEndpointsInfo();
    }

    /**
     * Combines types of getClasses() and getSingletons in one Set.
     *
     * @return all registered types
     */
    Set<Class<?>> allClasses() {
        final Set<Class<?>> allClasses = new HashSet<>(getClasses());
        for (Object singleton : getSingletons()) {
            allClasses.add(singleton.getClass());
        }
        return allClasses;
    }

    static String cleanUpPath(String path) {
        return PATH_DIRTY_SLASHES.matcher(path).replaceAll("/").trim();
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
            final String method = httpMethod == null ? "UNKNOWN" : httpMethod;
            return String.format("    %-7s %s (%s)", method, basePath, klass.getCanonicalName());
        }
    }

    private static class EndpointComparator implements Comparator<EndpointLogLine>, Serializable {
        private static final long serialVersionUID = 1L;

        @Override
        public int compare(EndpointLogLine endpointA, EndpointLogLine endpointB) {
            return Comparator.<EndpointLogLine, String>comparing(endpoint -> endpoint.basePath)
                    .thenComparing(endpoint -> endpoint.httpMethod, Comparator.nullsLast(Comparator.naturalOrder()))
                    .compare(endpointA, endpointB);
        }
    }

    private static class ComponentLoggingListener implements ApplicationEventListener {
        private final DropwizardResourceConfig config;
        private List<Resource> resources = Collections.emptyList();
        private Set<Class<?>> providers = Collections.emptySet();

        public ComponentLoggingListener(DropwizardResourceConfig config) {
            this.config = config;
        }

        @Override
        public void onEvent(ApplicationEvent event) {
            if (event.getType() == ApplicationEvent.Type.INITIALIZATION_APP_FINISHED) {
                resources = event.getResourceModel().getResources();
                providers = event.getProviders();

                final String resourceClasses = resources.stream()
                        .map(x -> x.getClass().getCanonicalName())
                        .collect(Collectors.joining(", "));

                final String providerClasses = providers.stream()
                        .map(Class::getCanonicalName)
                        .collect(Collectors.joining(", "));

                LOGGER.debug("resources = {}", resourceClasses);
                LOGGER.debug("providers = {}", providerClasses);
                LOGGER.info(getEndpointsInfo());
            }
        }

        private List<EndpointLogLine> logMethodLines(Resource resource, String contextPath) {
            final List<EndpointLogLine> methodLines = new ArrayList<>();
            for (ResourceMethod method : resource.getAllMethods()) {
                if ("OPTIONS".equalsIgnoreCase(method.getHttpMethod())) {
                    continue;
                }

                final String path = cleanUpPath(contextPath + Strings.nullToEmpty(resource.getPath()));
                final Class<?> handler = method.getInvocable().getHandler().getHandlerClass();
                switch (method.getType()) {
                    case RESOURCE_METHOD:
                        methodLines.add(new EndpointLogLine(method.getHttpMethod(), path, handler));
                        break;
                    case SUB_RESOURCE_LOCATOR:
                        final ResolvedType responseType = TYPE_RESOLVER
                                .resolve(method.getInvocable().getResponseType());
                        final Class<?> erasedType = !responseType.getTypeBindings().isEmpty() ?
                                responseType.getTypeBindings().getBoundType(0).getErasedType() :
                                responseType.getErasedType();

                        final Resource res = Resource.from(erasedType);
                        if (res == null) {
                            methodLines.add(new EndpointLogLine(method.getHttpMethod(), path, handler));
                        } else {
                            methodLines.addAll(logResourceLines(res, path));
                        }

                        break;
                    default:
                        break;
                }
            }

            return methodLines;
        }

        private List<EndpointLogLine> logResourceLines(Resource resource, String contextPath) {
            final List<EndpointLogLine> resourceLines = new ArrayList<>();
            for (Resource child : resource.getChildResources()) {
                resourceLines.addAll(logResourceLines(child, cleanUpPath(contextPath + Strings.nullToEmpty(resource.getPath()))));
            }

            resourceLines.addAll(logMethodLines(resource, contextPath));

            return resourceLines;
        }

        String getEndpointsInfo() {
            final StringBuilder msg = new StringBuilder(1024);
            final Set<EndpointLogLine> endpointLogLines = new TreeSet<>(new EndpointComparator());
            final String contextPath = config.getContextPath();
            final String normalizedContextPath = contextPath.isEmpty() || contextPath.equals("/") ? "" :
                    contextPath.startsWith("/") ? contextPath : "/" + contextPath;
            final String pattern = config.getUrlPattern().endsWith("/*") ?
                    config.getUrlPattern().substring(0, config.getUrlPattern().length() - 1) :
                    config.getUrlPattern();

            final String path = cleanUpPath(normalizedContextPath + pattern);

            msg.append("The following paths were found for the configured resources:");
            msg.append(NEWLINE).append(NEWLINE);

            for (Resource resource : resources) {
                endpointLogLines.addAll(logResourceLines(resource, path));
            }

            final List<EndpointLogLine> providerLines = providers.stream()
                    .map(Resource::from)
                    .filter(Objects::nonNull)
                    .flatMap(res -> logResourceLines(res, path).stream())
                    .collect(Collectors.toList());

            endpointLogLines.addAll(providerLines);

            if (!endpointLogLines.isEmpty()) {
                for (EndpointLogLine line : endpointLogLines) {
                    msg.append(line).append(NEWLINE);
                }
            } else {
                msg.append("    NONE").append(NEWLINE);
            }

            return msg.toString();
        }

        @Override
        @Nullable
        public RequestEventListener onRequest(RequestEvent requestEvent) {
            return null;
        }
    }
}
