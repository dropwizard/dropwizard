package io.dropwizard.jersey.guava;

import com.sun.jersey.api.representation.Form;
import com.sun.jersey.test.framework.AppDescriptor;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.WebAppDescriptor;
import io.dropwizard.logging.LoggingFactory;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class OptionalFormParamInjectableProviderTest extends JerseyTest {
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
                .post(String.class))
                .isEqualTo("-1");
    }

    @Test
    public void injectsAPresentOptionalInsteadOfValue() throws Exception {
        Form form = new Form();
        form.add("id", "200");
        assertThat(resource().path("/optional-param/")
                .entity(form)
                .post(String.class))
                .isEqualTo("200");
    }
}
