package io.dropwizard.health;

import java.util.EventListener;

/**
 * A listener contract for adding of {@link HealthStateListener}.
 */
interface HealthStateListenerListener extends EventListener {
     /**
     * Called when a new {@link HealthStateListener} is registered.
     *
     * @param healthStateListener the health state listener.
     */
    void onHealthStateListenerAdded(HealthStateListener healthStateListener);
}
