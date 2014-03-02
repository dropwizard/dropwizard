package io.dropwizard.jersey.sessions;

import static org.fest.assertions.api.Assertions.assertThat;
import io.dropwizard.jersey.DropwizardResourceConfig;
import io.dropwizard.jersey.testing.JerseyServletTest;
import io.dropwizard.logging.LoggingFactory;

import java.util.Collections;
import java.util.Map;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;

import org.glassfish.jersey.server.ResourceConfig;
import org.junit.Test;

import com.codahale.metrics.MetricRegistry;

public class HttpSessionProviderTest extends JerseyServletTest {
    static {
        LoggingFactory.bootstrap();
    }
    
    public HttpSessionProviderTest()
    {
        super("io.dropwizard.jersey.DropwizardResourceConfig",
                Collections.singletonList("io.dropwizard.jersey.sessions.SessionResource"));
    }

    @Override
    protected Application configure() {
        ResourceConfig rc = DropwizardResourceConfig.forTesting(new MetricRegistry());
        rc = rc.register(SessionResource.class);
        return rc;
    }
    
    @Test
    public void passesInHttpSessions() throws Exception {
        Response firstResponse = target("/session/")
                .request(MediaType.TEXT_PLAIN)
                .post(Entity.entity(new String("Mr. Peeps"), MediaType.TEXT_PLAIN));

        final Map<String,NewCookie> cookies = firstResponse.getCookies();
        firstResponse.close();

        Invocation.Builder builder =
                target("/session/")
                .request()
                .accept(MediaType.TEXT_PLAIN);

        for (NewCookie cookie : cookies.values()) {
            builder.cookie(cookie);
        }

        final String secondResponse = builder.get(String.class);
        assertThat(secondResponse)
        .isEqualTo("Mr. Peeps");
    }
}
