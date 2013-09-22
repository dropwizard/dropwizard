package io.dropwizard.jersey.sessions;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.test.framework.AppDescriptor;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.WebAppDescriptor;
import io.dropwizard.logging.LoggingFactory;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;

public class HttpSessionProviderTest extends JerseyTest {
    static {
        LoggingFactory.bootstrap();
    }

    @Override
    protected AppDescriptor configure() {
        return new WebAppDescriptor.Builder("io.dropwizard.jersey.sessions").build();
    }

    @Test
    public void passesInHttpSessions() throws Exception {
        final ClientResponse firstResponse = resource().path("/session/")
                                                       .type(MediaType.TEXT_PLAIN)
                                                       .post(ClientResponse.class, "Mr. Peeps");
        final List<NewCookie> cookies = firstResponse.getCookies();
        firstResponse.close();

        final WebResource.Builder builder = resource().path("/session/")
                                                      .accept(MediaType.TEXT_PLAIN);

        for (NewCookie cookie : cookies) {
            builder.cookie(cookie);
        }

        final String secondResponse = builder.get(String.class);
        assertThat(secondResponse)
                .isEqualTo("Mr. Peeps");
    }
}
