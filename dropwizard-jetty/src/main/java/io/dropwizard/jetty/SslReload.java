package io.dropwizard.jetty;

import org.eclipse.jetty.util.ssl.SslContextFactory;

import java.util.function.Consumer;

/** Provides the ability to modify an existing ssl factory with new configuration options. */
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

    public void reload(SslContextFactory factory) throws Exception {
        factory.reload(configurer);
    }

    public SslContextFactory getFactory() {
        return factory;
    }
}
