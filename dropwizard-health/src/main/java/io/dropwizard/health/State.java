package io.dropwizard.health;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

class State {
    private static final Logger LOGGER = LoggerFactory.getLogger(State.class);

    private final String name;
    private final int successAttempts;
    private final int failureAttempts;
    private final HealthStateListener healthStateListener;
    private final AtomicInteger counter = new AtomicInteger(0);
    private final AtomicBoolean healthy;

    /**
     * Creates instance that is used to track state of a health check.
     *
     * @param name                    name of health check
     * @param failureAttempts         the threshold of consecutive failed attempts needed to mark a dependency as unhealthy
     *                                (from a healthy state)
     * @param successAttempts         the threshold of consecutive successful attempts needed to mark a dependency as healthy
     *                                (from an unhealthy state)
     * @param initialState            initial state ({@code true} indicates to start in a healthy state and
     *                                {@code false} indicates to start in an unhealthy state)
     * @param healthStateListener    {@link HealthStateListener} that is called when state changes
     *                                (e.g. healthy to unhealthy or unhealthy to healthy)
     */
    State(final String name, final int failureAttempts, final int successAttempts, final boolean initialState,
          final HealthStateListener healthStateListener) {
        this.name = name;
        this.failureAttempts = failureAttempts;
        this.successAttempts = successAttempts;
        this.healthy = new AtomicBoolean(initialState);
        this.healthStateListener = healthStateListener;
    }

    void success() {
        if (healthy.get()) {
            // already healthy, do nothing
            return;
        }
        LOGGER.trace("health check received a successful result: name={} current={}", name, healthy);
        healthStateListener.onHealthyCheck(this.name);
        handleEvent(successAttempts, true);
    }

    void failure() {
        if (!healthy.get()) {
            // already unhealthy, do nothing
            return;
        }
        LOGGER.trace("health check received a failed result: name={} current={}", name, healthy);
        healthStateListener.onUnhealthyCheck(this.name);
        handleEvent(failureAttempts, false);
    }

    private void handleEvent(final int numAttempts, final boolean result) {
        final int newCount = counter.incrementAndGet();
        LOGGER.debug("health check state update: name={} result={} count={}/{}", name, result, newCount, numAttempts);
        if (newCount >= numAttempts) {
            final boolean newState = !healthy.get();
            healthy.set(newState);
            resetCount();
            healthStateListener.onStateChanged(this.name, newState);
        }
    }

    private void resetCount() {
        counter.set(0);
    }

    AtomicBoolean getHealthy() {
        return healthy;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof State state)) return false;
        return successAttempts == state.successAttempts &&
                failureAttempts == state.failureAttempts &&
                Objects.equals(name, state.name) &&
                Objects.equals(healthStateListener, state.healthStateListener) &&
                Objects.equals(counter, state.counter) &&
                Objects.equals(healthy, state.healthy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, successAttempts, failureAttempts, healthStateListener, counter, healthy);
    }

    @Override
    public String toString() {
        return "State{" +
                "name='" + name + '\'' +
                ", successAttempts=" + successAttempts +
                ", failureAttempts=" + failureAttempts +
                ", healthStateListener=" + healthStateListener +
                ", counter=" + counter +
                ", healthy=" + healthy +
                '}';
    }
}
