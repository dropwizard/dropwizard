package io.dropwizard.client;

import org.glassfish.jersey.spi.ExecutorServiceProvider;

import java.util.concurrent.ExecutorService;

class DropwizardExecutorProvider implements ExecutorServiceProvider {

    private final ExecutorService threadPool;

    DropwizardExecutorProvider(ExecutorService threadPool) {
        this.threadPool = threadPool;
    }

    @Override
    public ExecutorService getExecutorService() {
        return this.threadPool;
    }

    @Override
    public void dispose(ExecutorService executorService) {
    }
}
