package io.dropwizard.sslreload;

import io.dropwizard.jetty.SslReload;
import io.dropwizard.servlets.tasks.Task;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import java.io.PrintWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/** A task that will refresh all ssl factories with up to date certificate information */
public class SslReloadTask extends Task {
    private Collection<SslReload> reloader = Collections.emptySet();

    protected SslReloadTask() {
        super("reload-ssl");
    }

    @Override
    public void execute(Map<String, List<String>> parameters, PrintWriter output) throws Exception {
        // Iterate through all the reloaders first to ensure valid configuration
        for (SslReload reloader : getReloaders()) {
            reloader.reload(new SslContextFactory());
        }

        // Now we know that configuration is valid, reload for real
        for (SslReload reloader : getReloaders()) {
            reloader.reload();
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

