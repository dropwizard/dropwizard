package io.dropwizard.lifecycle;

/**
 * An implementation of the Managed Interface for {@link AutoCloseable} instances. Adding an
 * {@code AutoCloseableManager} instance to the application's environment ties that object’s lifecycle to
 * that of the application’s HTTP server. After the server has stopped (and after its graceful
 * shutdown period) the {@link #stop()} method is called, which will trigger the call to {@link
 * AutoCloseable#close()}
 *
 * <p>Usage :</p>
 * <pre>
 * {@code
 * AutoCloseable client = ...;
 * AutoCloseableManager clientManager = new AutoCloseableManager(client);
 * environment.lifecycle().manage(clientManager);
 * }
 * </pre>
 */
public class AutoCloseableManager implements Managed {

    private final AutoCloseable autoCloseable;

    /**
     * @param autoCloseable instance to close when the HTTP server stops.
     */
    public AutoCloseableManager(final AutoCloseable autoCloseable) {
        this.autoCloseable = autoCloseable;
    }

    /**
     * The start operation does nothing (i.e. it's a no-op).
     */
    @Override
    public void start() throws Exception {
    }

    /**
     * Calls {@link AutoCloseable#close()} on the closable provided in
     * {@link AutoCloseableManager#AutoCloseableManager(AutoCloseable)}.
     *
     * @throws Exception propagates {@link AutoCloseable#close()} exception
     */
    @Override
    public void stop() throws Exception {
        this.autoCloseable.close();
    }
}
