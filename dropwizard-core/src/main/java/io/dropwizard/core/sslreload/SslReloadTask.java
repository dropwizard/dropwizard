package io.dropwizard.core.sslreload;

import io.dropwizard.core.Configuration;
import io.dropwizard.core.ConfiguredBundle;
import io.dropwizard.jetty.MutableServletContextHandler;
import io.dropwizard.jetty.SslReload;
import io.dropwizard.servlets.tasks.Task;
import io.dropwizard.core.setup.Environment;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;

/** A task that will refresh all ssl factories with up to date certificate information */
public class SslReloadTask extends Task implements ConfiguredBundle<Configuration> {
    private Collection<SslReload> reloader = Collections.emptySet();

    public SslReloadTask() {
        super("reload-ssl");
    }

    public void configureReloaders(Environment environment) {
        final Set<SslReload> reloaders = new HashSet<>();
        reloaders.addAll(getReloaders(environment.getApplicationContext()));
        reloaders.addAll(getReloaders(environment.getAdminContext()));
        this.reloader = Collections.unmodifiableSet(reloaders); // Immutable set for safety
    }

    private Collection<SslReload> getReloaders(MutableServletContextHandler handler) {
        return handler.getServer().getBeans(SslReload.class);
    }

    @Override
    public void execute(Map<String, List<String>> parameters, PrintWriter output) throws Exception {
        // Iterate through all the reloaders first to ensure valid configuration
        for (SslReload sslReload : getReloaders()) {
            sslReload.reloadDryRun();
        }

        // Now we know that configuration is valid, reload for real
        for (SslReload sslReload : getReloaders()) {
            sslReload.reload();
        }

        output.write("Reloaded certificate configuration\n");
    }

    public Collection<SslReload> getReloaders() {
        return reloader;
    }

    public void setReloaders(Collection<SslReload> reloader) {
        this.reloader = reloader;
    }
}

