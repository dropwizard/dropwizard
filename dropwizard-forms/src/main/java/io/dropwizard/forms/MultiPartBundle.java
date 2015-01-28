package io.dropwizard.forms;

import io.dropwizard.Configuration;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.glassfish.jersey.media.multipart.MultiPartFeature;

/**
 * A {@link Bundle}, which enables the processing of multi-part form data by your application.
 *
 * @param <T>    the required configuration interface
 * 
 * @see org.glassfish.jersey.media.multipart.MultiPartFeature
 * @see <a href="https://jersey.java.net/documentation/latest/media.html#multipart">Jersey Multipart</a>
 */
public class MultiPartBundle<T extends Configuration> implements ConfiguredBundle<T> {
    @Override
    public void initialize(Bootstrap<?> bootstrap) {
    }

    @Override
    public final void run(T configuration, Environment environment) throws Exception {
        environment.jersey().register(new MultiPartFeature());
    }
}
