package io.dropwizard.health;

import java.util.EventListener;

public interface HealthStateListener extends EventListener, StateChangedCallback {
    void onHealthyCheck(String healthCheckName);
    void onUnhealthyCheck(String healthCheckName);
}
