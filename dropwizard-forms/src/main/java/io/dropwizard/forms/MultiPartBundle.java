package io.dropwizard.forms;

import io.dropwizard.AbstractHttpBundle;
import io.dropwizard.Bundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.HttpEnvironment;
import org.glassfish.jersey.media.multipart.MultiPartFeature;

/**
 * A {@link Bundle}, which enables the processing of multi-part form data by your application.
 *
 * @see org.glassfish.jersey.media.multipart.MultiPartFeature
 * @see <a href="https://jersey.java.net/documentation/latest/media.html#multipart">Jersey Multipart</a>
 */
public class MultiPartBundle extends AbstractHttpBundle {
    @Override
    public void initialize(Bootstrap<?> bootstrap) {
    }

    @Override
    public void run(HttpEnvironment environment) {
        environment.jersey().register(MultiPartFeature.class);
    }
}
