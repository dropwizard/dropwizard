package io.dropwizard.health;

public interface ShutdownNotifier {
    void notifyShutdownStarted() throws Exception;
}
