package io.dropwizard.client;

import java.util.concurrent.ExecutorService;

import org.glassfish.jersey.spi.RequestExecutorProvider;

class DropwizardExecutorProvider implements RequestExecutorProvider
{
    private ExecutorService threadPool;
    
    public DropwizardExecutorProvider (ExecutorService threadPool)
    {
        this.threadPool = threadPool;
    }

    @Override
    public ExecutorService getRequestingExecutor() {
        return this.threadPool;
    }

    @Override
    public void releaseRequestingExecutor(ExecutorService executor) {
        
    }
    
}