package com.yammer.dropwizard.setup;

import com.google.common.base.Function;
import com.sun.jersey.core.spi.scanning.PackageNamesScanner;
import com.sun.jersey.spi.container.servlet.ServletContainer;
import com.yammer.dropwizard.jersey.DropwizardResourceConfig;

import javax.annotation.Nullable;
import javax.ws.rs.core.Application;
import java.util.concurrent.atomic.AtomicReference;

import static com.google.common.base.Preconditions.checkNotNull;

public class JerseyEnvironment {
    private final AtomicReference<ServletContainer> container;
    private final DropwizardResourceConfig config;
    private String urlPattern;

    public JerseyEnvironment(AtomicReference<ServletContainer> container,
                             DropwizardResourceConfig config) {
        this.container = container;
        this.config = config;
        this.urlPattern = "/*";
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
}
