package io.dropwizard.client;

import com.google.common.util.concurrent.ForwardingExecutorService;
import org.glassfish.jersey.spi.ExecutorServiceProvider;

import java.util.concurrent.ExecutorService;

/**
 * An {@link ExecutorServiceProvider} implementation for use within
 * Dropwizard.
 *
 * With {DropwizardExecutorProvider.DisposableExecutorService}, one
 * can signal that an {ExecutorService} is to be gracefully shut down
 * upon its disposal by the Jersey runtime. It is used as a means of
 * signaling to {@link DropwizardExecutorProvider} that the executor
 * is not shared.
 */
class DropwizardExecutorProvider implements ExecutorServiceProvider {
    /**
     * An {@link ExecutorService} decorator used as a marker by
     * {@link DropwizardExecutorProvider#dispose} to induce service
     * shutdown.
     */
    static class DisposableExecutorService extends ForwardingExecutorService {
        private final ExecutorService delegate;

        public DisposableExecutorService(ExecutorService delegate) {
            this.delegate = delegate;
        }

        @Override
        protected ExecutorService delegate() {
            return delegate;
        }
    }

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
        if (executorService instanceof DisposableExecutorService) {
            executorService.shutdown();
        }
    }
}
