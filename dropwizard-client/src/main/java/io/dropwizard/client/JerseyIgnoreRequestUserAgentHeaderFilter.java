package io.dropwizard.client;

import org.glassfish.jersey.client.ClientRequest;

import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;

/**
 * Prevents Jersey from modification Request's User-Agent header with default value,
 * to escape the value conflict with Dropwizard
 */
@Provider
public class JerseyIgnoreRequestUserAgentHeaderFilter implements ClientRequestFilter {
    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {
        ((ClientRequest) requestContext).ignoreUserAgent(true);
    }
}
