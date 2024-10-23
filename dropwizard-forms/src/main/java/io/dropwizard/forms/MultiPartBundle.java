package io.dropwizard.forms;

import io.dropwizard.core.Configuration;
import io.dropwizard.core.ConfiguredBundle;
import io.dropwizard.core.setup.Environment;
import org.glassfish.jersey.media.multipart.MultiPartFeature;

/**
 * A {@link ConfiguredBundle}, which enables the processing of multipart form data by your application.
 *
 * @see org.glassfish.jersey.media.multipart.MultiPartFeature
 * @see <a href="https://jersey.java.net/documentation/latest/media.html#multipart">Jersey Multipart</a>
 */
public class MultiPartBundle implements ConfiguredBundle<Configuration> {
    @Override
    public void run(Configuration configuration, Environment environment) {
        environment.jersey().register(MultiPartFeature.class);
    }
}
