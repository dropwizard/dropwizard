package io.dropwizard.health;

@FunctionalInterface
public interface StateChangedCallback {
    void onStateChanged(String healthCheckName, boolean healthy);
}
