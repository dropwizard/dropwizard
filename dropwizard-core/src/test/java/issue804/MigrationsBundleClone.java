package issue804;

import io.dropwizard.Bundle;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.util.Generics;
import org.junit.Assert;

/**
 * I just cloned MigrationsBundle. I couldn't find any other way to get the configuration class...
 */
public abstract class MigrationsBundleClone<T extends Configuration> implements Bundle {

    @Override
    public final void initialize(Bootstrap<?> bootstrap) {
        Assert.assertEquals(SampleApplication.class, bootstrap.getApplication().getClass());
        final Class<T> klass = Generics.getTypeParameter(bootstrap.getApplication().getClass(), Configuration.class);
        Assert.assertEquals(SampleConfiguration.class, klass);
    }

    @Override
    public final void run(Environment environment) {
        // nothing doing
    }
}

