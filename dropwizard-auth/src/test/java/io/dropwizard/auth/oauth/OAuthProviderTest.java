package io.dropwizard.auth.oauth;

import com.google.common.collect.ImmutableList;
import io.dropwizard.auth.*;
import io.dropwizard.auth.util.AuthUtil;
import io.dropwizard.jersey.DropwizardResourceConfig;

public class OAuthProviderTest extends AuthBaseTest<OAuthProviderTest.BasicAuthTestResourceConfig>{
    public static class BasicAuthTestResourceConfig extends AuthBaseResourceConfig{
        protected AuthFilter getAuthFilter() {
            return new OAuthCredentialAuthFilter.Builder<>()
                .setAuthenticator(AuthUtil.getMultiplyUsersOAuthAuthenticator(ImmutableList.of(ADMIN_USER, ORDINARY_USER)))
                .setAuthorizer(AuthUtil.getTestAuthorizer(ADMIN_USER, ADMIN_ROLE))
                .setPrefix(BEARER_PREFIX)
                .buildAuthFilter();
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
