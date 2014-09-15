package io.dropwizard.jersey.validation;

import com.codahale.metrics.MetricRegistry;
import io.dropwizard.jersey.DropwizardResourceConfig;
import io.dropwizard.logging.LoggingFactory;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assume.assumeThat;

public class ConstraintViolationExceptionMapperTest extends JerseyTest {
    static {
        LoggingFactory.bootstrap();
    }

    @Override
    protected Application configure() {
        return DropwizardResourceConfig.forTesting(new MetricRegistry())
                .packages("io.dropwizard.jersey.validation");
    }

    @Test
    public void returnsAnErrorMessage() throws Exception {
        assumeThat(Locale.getDefault().getLanguage(), is("en"));

        final Response response = target("/valid/").request(MediaType.APPLICATION_JSON)
                .post(Entity.entity("{}", MediaType.APPLICATION_JSON));
        assertThat(response.getStatus()).isEqualTo(422);
        assertThat(response.readEntity(String.class)).isEqualTo("{\"errors\":[\"name may not be empty (was null)\"]}");
    }
}
