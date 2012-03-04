package com.yammer.dropwizard.auth;

import com.google.common.base.Optional;

public interface Authenticator<C, P> {
    Optional<P> authenticate(C credentials) throws AuthenticationException;
}
