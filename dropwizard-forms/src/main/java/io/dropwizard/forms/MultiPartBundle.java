package io.dropwizard.forms;

import io.dropwizard.Bundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.glassfish.jersey.media.multipart.MultiPartFeature;

/**
 * A {@link Bundle}, which enables the processing of multi-part form data by your application.
 *
 * @see org.glassfish.jersey.media.multipart.MultiPartFeature
 * @see <a href="https://jersey.java.net/documentation/latest/media.html#multipart">Jersey Multipart</a>
 */
public class MultiPartBundle implements Bundle {
    @Override
    public void initialize(Bootstrap<?> bootstrap) {
    }

    @Override
    public void run(Environment environment) {
        environment.jersey().register(MultiPartFeature.class);
    }
}
