package io.dropwizard.jetty;

import org.eclipse.jetty.util.ssl.SslContextFactory;

import java.util.function.Consumer;

/** Provides the ability to modify an existing SSL factory with new configuration options. */
public class SslReload {
    private final SslContextFactory factory;
    private final Consumer<SslContextFactory> configurer;

    public SslReload(SslContextFactory factory, Consumer<SslContextFactory> configurer) {
        this.factory = factory;
        this.configurer = configurer;
    }

    public void reload() throws Exception {
        getFactory().reload(configurer);
    }

    /**
     * Reload a given {@link SslContextFactory}'s configuration
     *
     * @deprecated Use #reload() or #reloadDryRun() instead.
     */
    @Deprecated
    public void reload(SslContextFactory factory) throws Exception {
        factory.reload(configurer);
    }

    /**
     * Perform a mock configuration reload
     *
     * @since 2.1.0
     * @throws Exception if the reload failed, e.g. due to invalid configuration
     */
    public void reloadDryRun() throws Exception {
        new SslContextFactory.Server().reload(configurer);
    }

    public SslContextFactory getFactory() {
        return factory;
    }
}
