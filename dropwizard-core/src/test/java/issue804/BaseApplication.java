package issue804;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;

public abstract class BaseApplication<R extends BaseConfiguration> extends Application<R> {

    @Override
    public void initialize(Bootstrap<R> bootstrap) {
        bootstrap.addBundle(new MigrationsBundleClone() {});
    }
}
