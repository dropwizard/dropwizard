package io.dropwizard.servlets.assets;

public class ResourceNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 7084957514695533766L;

    public ResourceNotFoundException(Throwable cause) {
        super(cause);
    }
}
