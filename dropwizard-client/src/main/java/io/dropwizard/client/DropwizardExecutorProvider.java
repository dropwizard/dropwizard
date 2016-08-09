package io.dropwizard.client;

import com.google.common.util.concurrent.ForwardingExecutorService;
import io.dropwizard.util.Duration;
import org.glassfish.jersey.client.ClientAsyncExecutor;
import org.glassfish.jersey.spi.ExecutorServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
@ClientAsyncExecutor
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

    private static final Logger LOGGER = LoggerFactory.getLogger(DropwizardExecutorProvider.class);

    private final ExecutorService executor;
    private final Duration shutdownGracePeriod;

    DropwizardExecutorProvider(ExecutorService executor, Duration shutdownGracePeriod) {
        this.executor = executor;
        this.shutdownGracePeriod = shutdownGracePeriod;
    }

    @Override
    public ExecutorService getExecutorService() {
        return this.executor;
    }

    @Override
    public void dispose(ExecutorService executorService) {
        if (executorService instanceof DisposableExecutorService) {
            executorService.shutdown();

            try {
                executorService.awaitTermination(
                    shutdownGracePeriod.getQuantity(), shutdownGracePeriod.getUnit());
            } catch (InterruptedException err) {
                LOGGER.warn("Interrupted while waiting for ExecutorService shutdown", err);
            }
        }
    }
}
