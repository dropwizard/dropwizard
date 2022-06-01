package io.dropwizard.client;

import java.util.concurrent.ExecutorService;
import org.glassfish.jersey.client.ClientAsyncExecutor;
import org.glassfish.jersey.spi.ExecutorServiceProvider;

/**
 * An {@link ExecutorServiceProvider} implementation for use within
 * Dropwizard.
 */
@ClientAsyncExecutor
class DropwizardExecutorProvider implements ExecutorServiceProvider {

    private final ExecutorService executor;

    DropwizardExecutorProvider(ExecutorService executor) {
        this.executor = executor;
    }

    @Override
    public ExecutorService getExecutorService() {
        return this.executor;
    }

    @Override
    public void dispose(ExecutorService executorService) {
        /*
        Jersey makes copies of clients, including the executor
        This means we cannot shut down the ExecutorService here as it may be in use elsewhere
        https://github.com/dropwizard/dropwizard/issues/2218
        */
    }
}
