package com.yammer.dropwizard.auth;

import java.io.PrintWriter;
import com.google.common.collect.ImmutableMultimap;
import com.yammer.dropwizard.tasks.Task;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Invalidates an authentication cache.
 */
public class AuthCacheInvalidationTask<C> extends Task {

    protected final CachingAuthenticator<C, ?> authenticator;

    /**
     * Creates a new AuthCacheInvalidationTask with the given {@link CachingAuthenticator} instance.
     *
     * @param authenticator a {@link CachingAuthenticator} instance
     */
    public AuthCacheInvalidationTask(CachingAuthenticator<C, ?> authenticator) {
        this("authCacheInvalidation", authenticator);
    }

    /**
     * Creates a new AuthCacheInvalidationTask with the given {@link CachingAuthenticator} instance.
     *
     * @param name the task's name
     * @param authenticator a {@link CachingAuthenticator} instance
     */
    public AuthCacheInvalidationTask(String name, CachingAuthenticator<C, ?> authenticator) {
        super(name);
        this.authenticator = checkNotNull(authenticator);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(ImmutableMultimap<String, String> parameters, PrintWriter output) {
        output.printf("Invalidating approximately %d cached principals...", this.authenticator.size());
        output.flush();
        this.authenticator.invalidateAll();
        output.println("Done!");
    }
}
