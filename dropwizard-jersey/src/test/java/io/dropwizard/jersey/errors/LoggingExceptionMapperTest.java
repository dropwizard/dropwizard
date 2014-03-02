package io.dropwizard.jersey.errors;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.failBecauseExceptionWasNotThrown;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.CharBuffer;

import io.dropwizard.jersey.DropwizardResourceConfig;
import io.dropwizard.logging.LoggingFactory;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;

import com.codahale.metrics.MetricRegistry;

public class LoggingExceptionMapperTest extends JerseyTest {
    static {
        LoggingFactory.bootstrap();
    }

    @Override
    protected Application configure() {
        ResourceConfig rc = DropwizardResourceConfig.forTesting(new MetricRegistry());
        rc = rc.register(new DefaultLoggingExceptionMapper());
        rc = rc.register(new DefaultJacksonMessageBodyProvider());
        rc = rc.register(ExceptionResource.class);
        return rc;
    }

    @Test
    public void returnsAnErrorMessage() throws Exception {
        try {
            target("/exception/").request(MediaType.APPLICATION_JSON).get(String.class);
            failBecauseExceptionWasNotThrown(WebApplicationException.class);
        } catch (WebApplicationException e) {
            Response response = e.getResponse();
            assertThat(response.getStatus())
                    .isEqualTo(500);

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
                    .startsWith("{\"message\":\"There was an error processing your request. It has been logged (ID ");
        }
    }
}
