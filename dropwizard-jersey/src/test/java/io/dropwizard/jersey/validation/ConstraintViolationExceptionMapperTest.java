package io.dropwizard.jersey.validation;

import io.dropwizard.jersey.DropwizardResourceConfig;
import io.dropwizard.logging.LoggingFactory;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;

import com.codahale.metrics.MetricRegistry;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.CharBuffer;
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
        ResourceConfig rc = DropwizardResourceConfig.forTesting(new MetricRegistry());
        rc = rc.packages("io.dropwizard.jersey.validation");
        return rc;
    }

    @Test
    public void returnsAnErrorMessage() throws Exception {
        assumeThat(Locale.getDefault().getLanguage(), is("en"));

        Response response = target("/valid/")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(new String("{}"),
                        MediaType.APPLICATION_JSON));
        assertThat(response.getStatus()).isEqualTo(422);
        
        ByteArrayInputStream entity = (ByteArrayInputStream) response.getEntity();
        InputStreamReader reader = new InputStreamReader(entity);
        CharBuffer chars = CharBuffer.allocate(1024);
        String responseStr = "";
        while (reader.read(chars) != -1)
        {
            chars.limit(chars.position());
            chars.rewind();
            responseStr += chars.toString();
            chars.clear();
        }
        assertThat(responseStr)
                    .isEqualTo("{\"errors\":[\"name may not be empty (was null)\"]}");
    }
}
