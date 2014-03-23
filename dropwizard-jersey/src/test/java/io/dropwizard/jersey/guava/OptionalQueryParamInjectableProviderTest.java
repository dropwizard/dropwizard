package io.dropwizard.jersey.guava;

import com.sun.jersey.test.framework.AppDescriptor;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.WebAppDescriptor;
import io.dropwizard.logging.LoggingFactory;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class OptionalQueryParamInjectableProviderTest extends JerseyTest {
    static {
        LoggingFactory.bootstrap();
    }

    @Override
    protected AppDescriptor configure() {
        return new WebAppDescriptor.Builder("io.dropwizard.jersey.guava").build();
    }

    @Test
    public void injectsAnAbsentOptionalInsteadOfNull() throws Exception {
        assertThat(resource().path("/optional-param/")
                             .get(String.class))
                .isEqualTo("-1");
    }

    @Test
    public void injectsAPresentOptionalInsteadOfValue() throws Exception {
        assertThat(resource().path("/optional-param/")
                             .queryParam("id", "200")
                             .get(String.class))
                .isEqualTo("200");
    }
}
