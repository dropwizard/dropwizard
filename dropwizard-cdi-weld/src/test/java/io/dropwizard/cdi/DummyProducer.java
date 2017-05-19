package io.dropwizard.cdi;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Named;
import java.io.Serializable;
import java.util.UUID;

@ApplicationScoped
public class DummyProducer implements Serializable {
    static final String DUMMY = UUID.randomUUID().toString();

    @Produces
    @Named("dummy")
    private String someMethod() {
        return DUMMY;
    }
}
