package io.dropwizard.auth.oauth;

import com.google.common.collect.ImmutableList;

import org.junit.Test;

import io.dropwizard.auth.AbstractAuthResourceConfig;
import io.dropwizard.auth.AuthBaseTest;
import io.dropwizard.auth.AuthFilter;
import io.dropwizard.auth.AuthResource;
import io.dropwizard.auth.util.AuthUtil;
import io.dropwizard.jersey.DropwizardResourceConfig;

import static org.assertj.core.api.Assertions.assertThat;

public class OAuthProviderTest extends AuthBaseTest<OAuthProviderTest.OAuthTestResourceConfig>{
    public static class OAuthTestResourceConfig extends AbstractAuthResourceConfig {
        public OAuthTestResourceConfig() {
            register(AuthResource.class);
        }

        @Override protected AuthFilter getAuthFilter() {
            return new OAuthCredentialAuthFilter.Builder<>()
                .setAuthenticator(AuthUtil.getMultiplyUsersOAuthAuthenticator(ImmutableList.of(ADMIN_USER, ORDINARY_USER)))
                .setAuthorizer(AuthUtil.getTestAuthorizer(ADMIN_USER, ADMIN_ROLE))
                .setPrefix(BEARER_PREFIX)
                .buildAuthFilter();
        }
    }

    @Test
    public void checksQueryStringAccessTokenIfAuthorizationHeaderMissing() {
        assertThat(target("/test/profile")
            .queryParam(OAuthCredentialAuthFilter.OAUTH_ACCESS_TOKEN_PARAM, getOrdinaryGuyValidToken())
            .request()
            .get(String.class))
            .isEqualTo("'" + ORDINARY_USER + "' has user privileges");
    }

    @Override
    protected DropwizardResourceConfig getDropwizardResourceConfig() {
        return new OAuthTestResourceConfig();
    }

    @Override
    protected Class<OAuthTestResourceConfig> getDropwizardResourceConfigClass() {
        return OAuthTestResourceConfig.class;
    }

    @Override
    protected String getPrefix() {
        return BEARER_PREFIX;
    }

    @Override
    protected String getOrdinaryGuyValidToken() {
        return "ordinary-guy";
    }

    @Override
    protected String getGoodGuyValidToken() {
        return "good-guy";
    }

    @Override
    protected String getBadGuyToken() {
        return "bad-guy";
    }
}
