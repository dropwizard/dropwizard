package io.dropwizard.client;

import org.glassfish.jersey.client.ClientAsyncExecutor;
import org.glassfish.jersey.spi.ExecutorServiceProvider;

import java.util.concurrent.ExecutorService;

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
    }
}
