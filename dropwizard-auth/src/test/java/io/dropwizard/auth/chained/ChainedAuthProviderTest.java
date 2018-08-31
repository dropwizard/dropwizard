package io.dropwizard.auth.chained;

import io.dropwizard.auth.AuthBaseTest;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.AuthFilter;
import io.dropwizard.auth.AuthResource;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.auth.Authorizer;
import io.dropwizard.auth.basic.BasicCredentialAuthFilter;
import io.dropwizard.auth.basic.BasicCredentials;
import io.dropwizard.auth.oauth.OAuthCredentialAuthFilter;
import io.dropwizard.auth.util.AuthUtil;
import io.dropwizard.jersey.DropwizardResourceConfig;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.glassfish.jersey.test.TestProperties;
import org.junit.Test;

import javax.ws.rs.core.HttpHeaders;
import java.security.Principal;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ChainedAuthProviderTest extends AuthBaseTest<ChainedAuthProviderTest.ChainedAuthTestResourceConfig> {
    private static final String BEARER_USER = "A12B3C4D";
    public static class ChainedAuthTestResourceConfig extends DropwizardResourceConfig {

        public ChainedAuthTestResourceConfig() {
            super();

            final Authorizer<Principal> authorizer = AuthUtil.getTestAuthorizer(ADMIN_USER, ADMIN_ROLE);
            final AuthFilter<BasicCredentials, Principal> basicAuthFilter = new BasicCredentialAuthFilter.Builder<>()
                .setAuthenticator(AuthUtil.getBasicAuthenticator(Arrays.asList(ADMIN_USER, ORDINARY_USER)))
                .setAuthorizer(authorizer)
                .buildAuthFilter();

            final AuthFilter<String, Principal> oAuthFilter = new OAuthCredentialAuthFilter.Builder<>()
                .setAuthenticator(AuthUtil.getSingleUserOAuthAuthenticator(BEARER_USER, ADMIN_USER))
                .setPrefix(BEARER_PREFIX)
                .setAuthorizer(authorizer)
                .buildAuthFilter();

            property(TestProperties.CONTAINER_PORT, "0");
            register(new AuthValueFactoryProvider.Binder<>(Principal.class));
            register(new AuthDynamicFeature(new ChainedAuthFilter<>(buildHandlerList(basicAuthFilter, oAuthFilter))));
            register(RolesAllowedDynamicFeature.class);
            register(AuthResource.class);
        }

        @SuppressWarnings("rawtypes")
        public List<AuthFilter> buildHandlerList(AuthFilter<BasicCredentials, Principal> basicAuthFilter,
                                                 AuthFilter<String, Principal> oAuthFilter) {
            return Arrays.asList(basicAuthFilter, oAuthFilter);
        }
    }

    @Test
    public void transformsBearerCredentialsToPrincipals() throws Exception {
        assertThat(target("/test/admin").request()
            .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + " " + BEARER_USER)
            .get(String.class))
            .isEqualTo("'" + ADMIN_USER + "' has admin privileges");
    }

    @Override
    protected DropwizardResourceConfig getDropwizardResourceConfig() {
        return new ChainedAuthTestResourceConfig();
    }

    @Override
    protected Class<ChainedAuthTestResourceConfig> getDropwizardResourceConfigClass() {
        return ChainedAuthTestResourceConfig.class;
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
}
