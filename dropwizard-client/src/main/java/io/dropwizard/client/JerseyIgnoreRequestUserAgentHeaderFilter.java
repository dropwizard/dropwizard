package io.dropwizard.client;

import java.io.IOException;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.ext.Provider;

import org.glassfish.jersey.client.ClientRequest;

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
