package io.dropwizard.lifecycle;

public class AutoCloseableManager implements Managed {

    private final AutoCloseable autoCloseable;

    public AutoCloseableManager(final AutoCloseable autoCloseable) {
        this.autoCloseable = autoCloseable;
    }

    @Override
    public void start() throws Exception {
        // OK BOSS
    }

    @Override
    public void stop() throws Exception {
        this.autoCloseable.close();
    }
}
