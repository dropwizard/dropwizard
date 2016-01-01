package io.dropwizard.auth.basic;

import com.google.common.collect.ImmutableList;
import io.dropwizard.auth.*;
import io.dropwizard.auth.util.AuthUtil;
import io.dropwizard.jersey.DropwizardResourceConfig;
import javax.ws.rs.container.ContainerRequestFilter;
import java.security.Principal;

public class BasicCustomAuthProviderTest extends AuthBaseTest<BasicCustomAuthProviderTest.BasicAuthTestResourceConfig> {

    public static class BasicAuthTestResourceConfig extends AbstractAuthResourceConfig {
        public BasicAuthTestResourceConfig() {
            register(AuthResource.class);
        }

        @Override protected ContainerRequestFilter getAuthFilter() {
            BasicCredentialAuthFilter.Builder<Principal> builder  = new BasicCredentialAuthFilter.Builder<>();
            builder.setAuthorizer(AuthUtil.getTestAuthorizer(ADMIN_USER, ADMIN_ROLE));
            builder.setAuthenticator(AuthUtil.getBasicAuthenticator(ImmutableList.of(ADMIN_USER, ORDINARY_USER)));
            builder.setPrefix(CUSTOM_PREFIX);
            return builder.buildAuthFilter();
        }
    }

    @Override
    protected DropwizardResourceConfig getDropwizardResourceConfig() {
        return new BasicAuthTestResourceConfig();
    }

    @Override
    protected Class<BasicCustomAuthProviderTest.BasicAuthTestResourceConfig> getDropwizardResourceConfigClass() {
        return BasicAuthTestResourceConfig.class;
    }

    @Override
    protected String getPrefix() {
        return CUSTOM_PREFIX;
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
