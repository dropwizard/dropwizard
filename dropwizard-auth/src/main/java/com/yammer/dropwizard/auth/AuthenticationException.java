package com.yammer.dropwizard.auth;

/**
 * An exception thrown to indicate that an {@link Authenticator} is <b>unable</b> to check the
 * validity of the given credentials.
 *
 * <p><b>DO NOT USE THIS TO INDICATE THAT THE CREDENTIALS ARE INVALID.</b></p>
 */
public class AuthenticationException extends Exception {
    private static final long serialVersionUID = -5053567474138953905L;

    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }

    public AuthenticationException(Throwable cause) {
        super(cause);
    }
}
