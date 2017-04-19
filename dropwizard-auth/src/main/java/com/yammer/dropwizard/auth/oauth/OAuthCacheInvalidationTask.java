package com.yammer.dropwizard.auth.oauth;

import java.io.PrintWriter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.yammer.dropwizard.auth.AuthCacheInvalidationTask;
import com.yammer.dropwizard.auth.CachingAuthenticator;

/**
 * Invalidates an OAuth authentication cache.
 */
public class OAuthCacheInvalidationTask extends AuthCacheInvalidationTask<String> {

    /**
     * Creates a new OAuthCacheInvalidationTask with the given {@link CachingAuthenticator} instance.
     *
     * @param authenticator a {@link CachingAuthenticator} instance
     */
    public OAuthCacheInvalidationTask(CachingAuthenticator<String, ?> authenticator) {
        super("oauthCacheInvalidation", authenticator);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(ImmutableMultimap<String, String> parameters, PrintWriter output) {
        final ImmutableList<String> credentials = parameters.get("credentials").asList();
        if (credentials.isEmpty()) {
            super.execute(parameters, output);
        } else {
            output.printf("Invalidating approximately %d cached principals...", credentials.size());
            output.flush();
            super.authenticator.invalidateAll(credentials);
            output.println("Done!");
        }
    }
}
