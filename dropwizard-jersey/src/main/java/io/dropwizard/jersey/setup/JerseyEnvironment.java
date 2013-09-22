package io.dropwizard.jersey.setup;

import com.google.common.base.Function;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.core.spi.scanning.PackageNamesScanner;
import com.sun.jersey.spi.container.servlet.ServletContainer;
import io.dropwizard.jersey.DropwizardResourceConfig;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

public class JerseyEnvironment {
    private final JerseyContainerHolder holder;
    private final DropwizardResourceConfig config;

    public JerseyEnvironment(JerseyContainerHolder holder,
                             DropwizardResourceConfig config) {
        this.holder = holder;
        this.config = config;
    }

    public void disable() {
        holder.setContainer(null);
    }

    public void replace(Function<ResourceConfig, ServletContainer> replace) {
        holder.setContainer(replace.apply(config));
    }

    /**
     * Adds the given object as a Jersey singleton component.
     *
     * @param component a Jersey singleton component
     */
    public void register(Object component) {
        config.getSingletons().add(checkNotNull(component));
    }

    /**
     * Adds the given class as a Jersey component. <p/><b>N.B.:</b> This class must either have a
     * no-args constructor or use Jersey's built-in dependency injection.
     *
     * @param componentClass a Jersey component class
     */
    public void register(Class<?> componentClass) {
        config.getClasses().add(checkNotNull(componentClass));
    }

    /**
     * Adds array of package names which will be used to scan for components. Packages will be
     * scanned recursively, including all nested packages.
     *
     * @param packages array of package names
     */
    public void packages(String... packages) {
        config.init(new PackageNamesScanner(checkNotNull(packages)));
    }

    /**
     * Enables the Jersey feature with the given name.
     *
     * @param featureName the name of the feature to be enabled
     * @see com.sun.jersey.api.core.ResourceConfig
     */
    public void enable(String featureName) {
        config.getFeatures().put(checkNotNull(featureName), Boolean.TRUE);
    }

    /**
     * Disables the Jersey feature with the given name.
     *
     * @param featureName the name of the feature to be disabled
     * @see com.sun.jersey.api.core.ResourceConfig
     */
    public void disable(String featureName) {
        config.getFeatures().put(checkNotNull(featureName), Boolean.FALSE);
    }

    /**
     * Sets the given Jersey property.
     *
     * @param name  the name of the Jersey property
     * @param value the value of the Jersey property
     * @see com.sun.jersey.api.core.ResourceConfig
     */
    public void property(String name, @Nullable Object value) {
        config.getProperties().put(checkNotNull(name), value);
    }

    /**
     * Gets the given Jersey property.
     *
     * @param name the name of the Jersey property
     * @see com.sun.jersey.api.core.ResourceConfig
     */
    @SuppressWarnings("unchecked")
    public <T> T getProperty(String name) {
        return (T) config.getProperties().get(name);
    }

    public String getUrlPattern() {
        return config.getUrlPattern();
    }

    public void setUrlPattern(String urlPattern) {
        config.setUrlPattern(urlPattern);
    }

    public ResourceConfig getResourceConfig() {
        return config;
    }
}
