package io.dropwizard.forms;

import io.dropwizard.Configuration;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Environment;
import org.glassfish.jersey.media.multipart.MultiPartFeature;

/**
 * A {@link ConfiguredBundle}, which enables the processing of multi-part form data by your application.
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
