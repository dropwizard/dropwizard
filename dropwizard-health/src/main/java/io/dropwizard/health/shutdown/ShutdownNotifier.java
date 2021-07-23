package io.dropwizard.health.shutdown;

public interface ShutdownNotifier {
    void notifyShutdownStarted() throws Exception;
}
