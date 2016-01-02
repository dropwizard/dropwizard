package io.dropwizard.auth.principal;

import io.dropwizard.auth.PrincipalImpl;

/**
 * An empty principal.
 */
public class NullPrincipal extends PrincipalImpl {
    public NullPrincipal() {
        super("null");
    }
}
