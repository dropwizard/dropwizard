package com.codahale.dropwizard.setup;

import com.codahale.dropwizard.jersey.DropwizardResourceConfig;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Ordering;
import com.sun.jersey.core.reflection.AnnotatedMethod;
import com.sun.jersey.core.reflection.MethodList;
import com.sun.jersey.core.spi.scanning.PackageNamesScanner;
import com.sun.jersey.spi.container.servlet.ServletContainer;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.eclipse.jetty.util.component.LifeCycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import javax.ws.rs.ext.Provider;
import java.util.concurrent.atomic.AtomicReference;

import static com.google.common.base.Preconditions.checkNotNull;

public class JerseyEnvironment {
    private static final Logger LOGGER = LoggerFactory.getLogger(JerseyEnvironment.class);

    private final AtomicReference<ServletContainer> container;
    private final DropwizardResourceConfig config;
    private String urlPattern;

    public JerseyEnvironment(LifeCycle handler,
                             AtomicReference<ServletContainer> container,
                             DropwizardResourceConfig config) {
        this.container = container;
        this.config = config;
        this.urlPattern = "/*";
        handler.addLifeCycleListener(new AbstractLifeCycle.AbstractLifeCycleListener() {
            @Override
            public void lifeCycleStarting(LifeCycle event) {
                logResources();
                logProviders();
                logEndpoints();
            }
        });
    }

    public void disable() {
        container.set(null);
    }

    public void replace(Function<Application, ServletContainer> replace) {
        container.set(replace.apply(config));
    }

    /**
     * Adds the given object as a Jersey singleton resource.
     *
     * @param resource a Jersey singleton resource
     */
    public void addResource(Object resource) {
        config.getSingletons().add(checkNotNull(resource));
    }

    /**
     * Scans the packages and sub-packages of the given {@link Class} objects for resources and
     * providers.
     *
     * @param classes the classes whose packages to scan
     */
    public void scanPackagesForResourcesAndProviders(Class<?>... classes) {
        checkNotNull(classes);
        final String[] names = new String[classes.length];
        for (int i = 0; i < classes.length; i++) {
            names[i] = classes[i].getPackage().getName();
        }
        config.init(new PackageNamesScanner(names));
    }

    /**
     * Adds the given class as a Jersey resource. <p/><b>N.B.:</b> This class must either have a
     * no-args constructor or use Jersey's built-in dependency injection.
     *
     * @param klass a Jersey resource class
     */
    public void addResource(Class<?> klass) {
        config.getClasses().add(checkNotNull(klass));
    }

    /**
     * Adds the given object as a Jersey provider.
     *
     * @param provider a Jersey provider
     */
    public void addProvider(Object provider) {
        config.getSingletons().add(checkNotNull(provider));
    }

    /**
     * Adds the given class as a Jersey provider. <p/><b>N.B.:</b> This class must either have a
     * no-args constructor or use Jersey's built-in dependency injection.
     *
     * @param klass a Jersey provider class
     */
    public void addProvider(Class<?> klass) {
        config.getClasses().add(checkNotNull(klass));
    }

    /**
     * Enables the Jersey feature with the given name.
     *
     * @param name the name of the feature to be enabled
     * @see com.sun.jersey.api.core.ResourceConfig
     */
    public void enableJerseyFeature(String name) {
        config.getFeatures().put(checkNotNull(name), Boolean.TRUE);
    }

    /**
     * Disables the Jersey feature with the given name.
     *
     * @param name the name of the feature to be disabled
     * @see com.sun.jersey.api.core.ResourceConfig
     */
    public void disableJerseyFeature(String name) {
        config.getFeatures().put(checkNotNull(name), Boolean.FALSE);
    }

    /**
     * Sets the given Jersey property.
     *
     * @param name  the name of the Jersey property
     * @param value the value of the Jersey property
     * @see com.sun.jersey.api.core.ResourceConfig
     */
    public void setJerseyProperty(String name, @Nullable Object value) {
        config.getProperties().put(checkNotNull(name), value);
    }

    /**
     * Gets the given Jersey property.
     *
     * @param name the name of the Jersey property
     * @see com.sun.jersey.api.core.ResourceConfig
     */
    @SuppressWarnings("unchecked")
    public <T> T getJerseyProperty(String name) {
        return (T) config.getProperties().get(name);
    }

    public String getUrlPattern() {
        return urlPattern;
    }

    public void setUrlPattern(String urlPattern) {
        this.urlPattern = urlPattern;
    }

    private void logResources() {
        final ImmutableSet.Builder<String> builder = ImmutableSet.builder();

        for (Class<?> klass : config.getClasses()) {
            if (klass.isAnnotationPresent(Path.class)) {
                builder.add(klass.getCanonicalName());
            }
        }

        for (Object o : config.getSingletons()) {
            if (o.getClass().isAnnotationPresent(Path.class)) {
                builder.add(o.getClass().getCanonicalName());
            }
        }

        LOGGER.debug("resources = {}", builder.build());
    }

    private void logProviders() {
        final ImmutableSet.Builder<String> builder = ImmutableSet.builder();

        for (Class<?> klass : config.getClasses()) {
            if (klass.isAnnotationPresent(Provider.class)) {
                builder.add(klass.getCanonicalName());
            }
        }

        for (Object o : config.getSingletons()) {
            if (o.getClass().isAnnotationPresent(Provider.class)) {
                builder.add(o.getClass().getCanonicalName());
            }
        }

        LOGGER.debug("providers = {}", builder.build());
    }

    private void logEndpoints() {
        final StringBuilder stringBuilder = new StringBuilder(1024).append(
                "The following paths were found for the configured resources:\n\n");

        final ImmutableList.Builder<Class<?>> builder = ImmutableList.builder();
        for (Object o : config.getSingletons()) {
            if (o.getClass().isAnnotationPresent(Path.class)) {
                builder.add(o.getClass());
            }
        }
        for (Class<?> klass : config.getClasses()) {
            if (klass.isAnnotationPresent(Path.class)) {
                builder.add(klass);
            }
        }

        for (Class<?> klass : builder.build()) {
            final String path = klass.getAnnotation(Path.class).value();
            String rootPath = urlPattern;
            if (rootPath.endsWith("/*")) {
                rootPath = rootPath.substring(0,
                                              rootPath.length() - (path.startsWith("/") ? 2 : 1));
            }

            final ImmutableList.Builder<String> endpoints = ImmutableList.builder();
            for (AnnotatedMethod method : annotatedMethods(klass)) {
                final StringBuilder pathBuilder = new StringBuilder()
                        .append(rootPath)
                        .append(path);
                if (method.isAnnotationPresent(Path.class)) {
                    final String methodPath = method.getAnnotation(Path.class).value();
                    if (!methodPath.startsWith("/") && !path.endsWith("/")) {
                        pathBuilder.append('/');
                    }
                    pathBuilder.append(methodPath);
                }
                for (HttpMethod verb : method.getMetaMethodAnnotations(HttpMethod.class)) {
                    endpoints.add(String.format("    %-7s %s (%s)",
                                                verb.value(),
                                                pathBuilder.toString(),
                                                klass.getCanonicalName()));
                }
            }

            for (String line : Ordering.natural().sortedCopy(endpoints.build())) {
                stringBuilder.append(line).append('\n');
            }
        }

        LOGGER.info(stringBuilder.toString());
    }

    private MethodList annotatedMethods(Class<?> resource) {
        return new MethodList(resource, true).hasMetaAnnotation(HttpMethod.class);
    }
}
