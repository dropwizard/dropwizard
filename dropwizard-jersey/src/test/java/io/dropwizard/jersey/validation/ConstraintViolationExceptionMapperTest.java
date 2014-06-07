package io.dropwizard.jersey.validation;

import com.codahale.metrics.MetricRegistry;
import io.dropwizard.jersey.DropwizardResourceConfig;
import io.dropwizard.logging.LoggingFactory;
import java.util.Locale;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import static org.assertj.core.api.Assertions.assertThat;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assume.assumeThat;
import org.junit.Test;

public class ConstraintViolationExceptionMapperTest extends JerseyTest {
    static {
        LoggingFactory.bootstrap();
    }

    @Override
    protected Application configure() {
        ResourceConfig rc = DropwizardResourceConfig.forTesting(new MetricRegistry());
        rc = rc.packages("io.dropwizard.jersey.validation");
        return rc;
    }

    @Test
    public void returnsAnErrorMessage() throws Exception {
        assumeThat(Locale.getDefault().getLanguage(), is("en"));

        final Response response = target("/valid/")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(new String("{}"),
                        MediaType.APPLICATION_JSON));
        assertThat(response.getStatus()).isEqualTo(422);
        
        final String responseStr = response.readEntity(String.class);
        
        assertThat(responseStr)
                    .isEqualTo("{\"errors\":[\"name may not be empty (was null)\"]}");
    }
}
