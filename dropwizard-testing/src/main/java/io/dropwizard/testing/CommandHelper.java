package io.dropwizard.testing;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.cli.Command;
import io.dropwizard.setup.Bootstrap;
import net.sourceforge.argparse4j.inf.Namespace;

import static com.google.common.base.Throwables.propagate;

abstract class CommandHelper<C extends Configuration> {
    protected final Class<? extends Application<C>> applicationClass;
    protected final String configPath;
    protected final ConfigOverride[] configOverrides;

    protected Application<C> application;
    protected Bootstrap<C> bootstrap;
    protected Namespace namespace;

    public CommandHelper(Class<? extends Application<C>> applicationClass,
                         String configPath,
                         ConfigOverride... configOverrides) {
        this.applicationClass = applicationClass;
        this.configPath = configPath;
        this.configOverrides = configOverrides;
    }

    protected void applyConfigOverrides() {
        for (ConfigOverride configOverride: configOverrides) {
            configOverride.addToSystemProperties();
        }
    }

    protected void resetConfigOverrides() {
        for (ConfigOverride configOverride : configOverrides) {
            configOverride.removeFromSystemProperties();
        }
    }

    public Application<C> newApplication() {
        try {
            return applicationClass.newInstance();
        } catch (Exception e) {
            throw propagate(e);
        }
    }

    @SuppressWarnings("unchecked")
    public <A extends Application<C>> A getApplication() {
        return (A) application;
    }

    protected Bootstrap<C> newBootStrap(Application<C> app) {
        return new Bootstrap<C>(app);
    }

    protected Namespace newNamespace() {
        ImmutableMap.Builder<String, Object> file = ImmutableMap.builder();
        if (!Strings.isNullOrEmpty(configPath)) {
            file.put("file", configPath);
        }
        return new Namespace(file.build());
    }

    protected void initialize() {
        application = newApplication();
        bootstrap = newBootStrap(application);
        namespace = newNamespace();

        application.initialize(bootstrap);
    }
}
