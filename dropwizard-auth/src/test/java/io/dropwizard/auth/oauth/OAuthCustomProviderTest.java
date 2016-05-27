package io.dropwizard.auth.oauth;

import com.google.common.collect.ImmutableList;
import io.dropwizard.auth.AbstractAuthResourceConfig;
import io.dropwizard.auth.AuthBaseTest;
import io.dropwizard.auth.AuthFilter;
import io.dropwizard.auth.AuthResource;
import io.dropwizard.auth.util.AuthUtil;
import io.dropwizard.jersey.DropwizardResourceConfig;

public class OAuthCustomProviderTest extends AuthBaseTest<OAuthCustomProviderTest.OAuthTestResourceConfig> {
    public static class OAuthTestResourceConfig extends AbstractAuthResourceConfig {
        public OAuthTestResourceConfig() {
            register(AuthResource.class);
        }

        @Override protected AuthFilter getAuthFilter() {
            return new OAuthCredentialAuthFilter.Builder<>()
                .setAuthenticator(AuthUtil.getMultiplyUsersOAuthAuthenticator(ImmutableList.of(ADMIN_USER, ORDINARY_USER)))
                .setAuthorizer(AuthUtil.getTestAuthorizer(ADMIN_USER, ADMIN_ROLE))
              .setPrefix(CUSTOM_PREFIX)
              .buildAuthFilter();
        }
    }

    @Override
    protected DropwizardResourceConfig getDropwizardResourceConfig() {
        return new OAuthProviderTest.OAuthTestResourceConfig();
    }

    @Override
    protected Class<OAuthTestResourceConfig> getDropwizardResourceConfigClass() {
        return OAuthTestResourceConfig.class;
    }

    @Override
    protected String getPrefix() {
        return CUSTOM_PREFIX;
    }

    @Override
    protected String getOrdinaryGuyValidToken() {
        return ORDINARY_USER;
    }

    @Override
    protected String getGoodGuyValidToken() {
        return ADMIN_USER;
    }

    @Override
    protected String getBadGuyToken() {
        return BADGUY_USER;
    }
}
