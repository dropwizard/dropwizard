package io.dropwizard.jersey.validation;

import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.test.framework.AppDescriptor;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.WebAppDescriptor;
import io.dropwizard.logging.LoggingFactory;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assume.assumeThat;

public class ConstraintViolationExceptionMapperTest extends JerseyTest {
    static {
        LoggingFactory.bootstrap();
    }

    @Override
    protected AppDescriptor configure() {
        return new WebAppDescriptor.Builder("io.dropwizard.jersey.validation").build();
    }

    @Test
    public void returnsAnErrorMessage() throws Exception {
        assumeThat(Locale.getDefault().getLanguage(), is("en"));

        try {
            resource().path("/valid/").type(MediaType.APPLICATION_JSON).post("{}");
            failBecauseExceptionWasNotThrown(UniformInterfaceException.class);
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus())
                    .isEqualTo(422);

            assertThat(e.getResponse().getEntity(String.class))
                    .isEqualTo("{\"errors\":[\"name may not be empty (was null)\"]}");
        }
    }
}
