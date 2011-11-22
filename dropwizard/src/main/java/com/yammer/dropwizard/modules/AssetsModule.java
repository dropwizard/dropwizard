package com.yammer.dropwizard.modules;

import com.yammer.dropwizard.Module;
import com.yammer.dropwizard.config.Environment;
import org.eclipse.jetty.servlet.DefaultServlet;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * A module for serving static asset files from the classpath.
 */
public class AssetsModule implements Module {
    private final String path;

    /**
     * Creates a new {@link AssetsModule} which serves up static assets from
     * {@code src/main/resources/assets/*} as {@code /assets/*}.
     *
     * @see AssetsModule#AssetsModule(String)
     */
    public AssetsModule() {
        this("/assets");
    }

    /**
     * Creates a new {@link AssetsModule} which will configure the service to serve the static files
     * located in {@code src/main/resources/${path}} as {@code /${path}}. For example, given a
     * {@code path} of {@code "/assets"}, {@code src/main/resources/assets/example.js} would be
     * served up from {@code /assets/example.js}.
     *
     * @param path    the classpath and URI root of the static asset files
     */
    public AssetsModule(String path) {
        checkArgument(path.startsWith("/"), "%s is not an absolute path", path);
        checkArgument(!"/".equals(path), "%s is the classpath root");
        this.path = path.endsWith("/") ? path : (path + '/');
    }

    @Override
    public void initialize(Environment environment) {
        environment.addServlet(DefaultServlet.class, path + '*')
                   .setInitParam("dirAllowed", "false")
                   .setInitParam("pathInfoOnly", "true")
                   .setInitParam("relativeResourceBase", path);
    }
}
