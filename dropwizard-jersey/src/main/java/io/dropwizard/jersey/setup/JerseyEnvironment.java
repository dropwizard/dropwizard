package io.dropwizard.jersey.setup;

import com.google.common.base.Function;
import io.dropwizard.jersey.DropwizardResourceConfig;
import org.glassfish.jersey.server.ResourceConfig;

import javax.annotation.Nullable;
import javax.servlet.Servlet;

import static java.util.Objects.requireNonNull;

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

    public void replace(Function<ResourceConfig, Servlet> replace) {
        holder.setContainer(replace.apply(config));
    }

    /**
     * Adds the given object as a Jersey singleton component.
     *
     * @param component a Jersey singleton component
     */
    public void register(Object component) {
        config.register(requireNonNull(component));
    }

    /**
     * Adds the given class as a Jersey component. <p/><b>N.B.:</b> This class must either have a
     * no-args constructor or use Jersey's built-in dependency injection.
     *
     * @param componentClass a Jersey component class
     */
    public void register(Class<?> componentClass) {
        config.register(requireNonNull(componentClass));
    }

    /**
     * Adds array of package names which will be used to scan for components. Packages will be
     * scanned recursively, including all nested packages.
     *
     * @param packages array of package names
     */
    public void packages(String... packages) {
        config.packages(requireNonNull(packages));
    }

    /**
     * Enables the Jersey feature with the given name.
     *
     * @param featureName the name of the feature to be enabled
     * @see org.glassfish.jersey.server.ResourceConfig
     */
    public void enable(String featureName) {
        config.property(requireNonNull(featureName), Boolean.TRUE);
    }

    /**
     * Disables the Jersey feature with the given name.
     *
     * @param featureName the name of the feature to be disabled
     * @see org.glassfish.jersey.server.ResourceConfig
     */
    public void disable(String featureName) {
        config.property(requireNonNull(featureName), Boolean.FALSE);
    }

    /**
     * Sets the given Jersey property.
     *
     * @param name  the name of the Jersey property
     * @param value the value of the Jersey property
     * @see org.glassfish.jersey.server.ResourceConfig
     */
    public void property(String name, @Nullable Object value) {
        config.property(requireNonNull(name), value);
    }

    /**
     * Gets the given Jersey property.
     *
     * @param name the name of the Jersey property
     * @see org.glassfish.jersey.server.ResourceConfig
     */
    @SuppressWarnings("unchecked")
    public <T> T getProperty(String name) {
        return (T) config.getProperties().get(name);
    }

    public String getUrlPattern() {
        return config.getUrlPattern();
    }

    public void setUrlPattern(String urlPattern) {
        String normalizedUrlPattern = urlPattern;
        if (!normalizedUrlPattern.endsWith("*") && !normalizedUrlPattern.endsWith("/")) {
            normalizedUrlPattern += "/";
        }
        if (!normalizedUrlPattern.endsWith("*")) {
            normalizedUrlPattern+= "*";
        }
        config.setUrlPattern(normalizedUrlPattern);
    }

    public DropwizardResourceConfig getResourceConfig() {
        return config;
    }
}
