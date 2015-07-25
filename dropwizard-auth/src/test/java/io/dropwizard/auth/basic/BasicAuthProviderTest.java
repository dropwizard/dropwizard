package io.dropwizard.auth.basic;

import com.google.common.collect.ImmutableList;
import io.dropwizard.auth.*;
import io.dropwizard.auth.util.AuthUtil;
import io.dropwizard.jersey.DropwizardResourceConfig;
import org.junit.Test;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.security.Principal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;

public class BasicAuthProviderTest extends AuthBaseTest<BasicAuthProviderTest.BasicAuthTestResourceConfig> {
    public static class BasicAuthTestResourceConfig extends AuthBaseResourceConfig{
        protected ContainerRequestFilter getAuthFilter() {
            BasicCredentialAuthFilter.Builder<Principal> builder = new BasicCredentialAuthFilter.Builder<>();
            builder.setAuthorizer(AuthUtil.getTestAuthorizer(ADMIN_USER, ADMIN_ROLE));
            builder.setAuthenticator(AuthUtil.getBasicAuthenticator(ImmutableList.of(ADMIN_USER, ORDINARY_USER)));
            builder.setUnauthorizedHandler(new UnauthorizedHandler() {
                @Override
                public Response buildResponse(String prefix, String realm) {
                    return Response.status(Response.Status.UNAUTHORIZED)
                            .header(HttpHeaders.WWW_AUTHENTICATE, String.format("%s realm=\"%s\"", prefix, realm))
                            .type(MediaType.TEXT_PLAIN_TYPE)
                            .entity("Credentials are required to access this resource.")
                            .build();
                }
            });
            return builder.buildAuthFilter();
        }
    }

    @Override
    protected DropwizardResourceConfig getDropwizardResourceConfig() {
        return new BasicAuthTestResourceConfig();
    }

    @Override
    protected Class<BasicAuthTestResourceConfig> getDropwizardResourceConfigClass() {
        return BasicAuthTestResourceConfig.class;
    }

    @Override
    protected String getPrefix() {
        return BASIC_PREFIX;
    }

    @Override
    protected String getOrdinaryGuyValidToken() {
        return ORDINARY_USER_ENCODED_TOKEN;
    }

    @Override
    protected String getGoodGuyValidToken() {
        return GOOD_USER_ENCODED_TOKEN;
    }

    @Override
    protected String getBadGuyToken() {
        return BAD_USER_ENCODED_TOKEN;
    }

    @Test
    public void respondsToNoAuthenticationWith401() throws Exception {
        try {
            target("/test/admin").request()
                    .get(String.class);
            failBecauseExceptionWasNotThrown(WebApplicationException.class);
        } catch (WebApplicationException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(401);
            assertThat(e.getResponse().getHeaders().get(HttpHeaders.WWW_AUTHENTICATE))
                    .containsOnly(getPrefix() + " realm=\"realm\"");

        }
    }
}
