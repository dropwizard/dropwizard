package io.dropwizard.health;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StateTest {
    private static final String NAME = "test";

    private final AtomicBoolean didStateChange = new AtomicBoolean(false);
    private final HealthStateListener listener = new HealthStateListener() {
        @Override
        public void onHealthyCheck(String healthCheckName) {}

        @Override
        public void onUnhealthyCheck(String healthCheckName) {}

        @Override
        public void onStateChanged(String healthCheckName, boolean healthy) {
            didStateChange.set(true);
        }
    };

    @Mock
    private HealthStateListener listenerMock;

    @BeforeEach
    void setUp() {
        didStateChange.set(false);
    }

    @Test
    void singleFailureShouldNotChangeStateIfThresholdNotExceeded() {
        final State state = new State(NAME, 2, 1, true, listener);
        state.failure();

        assertThat(didStateChange).isFalse();
        assertThat(state.getHealthy()).isTrue();
    }

    @Test
    void singleFailureShouldChangeStateIfThresholdExceeded() {
        final State state = new State(NAME, 1, 1, true, listener);
        assertThat(state.getHealthy()).isTrue();

        state.failure();

        assertThat(didStateChange).isTrue();
        assertThat(state.getHealthy()).isFalse();
    }

    @Test
    void singleSuccessShouldNotChangeStateIfThresholdNotExceeded() {
        final State state = new State(NAME, 1, 2, false, listener);
        assertThat(state.getHealthy()).isFalse();

        state.success();

        assertThat(didStateChange).isFalse();
        assertThat(state.getHealthy()).isFalse();
    }

    @Test
    void singleSuccessShouldChangeStateIfThresholdExceeded() {
        final State state = new State(NAME, 1, 1, false, listener);
        assertThat(state.getHealthy()).isFalse();

        state.success();

        assertThat(didStateChange).isTrue();
        assertThat(state.getHealthy()).isTrue();
    }

    @Test
    void failureFollowedByRecoveryShouldAllowAStateChangeToUnhealthyAfterAnotherFailureOccurs() {
        final State state = new State(NAME, 1, 1, true, listener);

        state.failure();

        assertThat(didStateChange).isTrue();
        assertThat(state.getHealthy()).isFalse();

        didStateChange.set(false);

        state.success();

        assertThat(didStateChange).isTrue();
        assertThat(state.getHealthy()).isTrue();

        didStateChange.set(false);

        state.failure();

        assertThat(didStateChange).isTrue();
        assertThat(state.getHealthy()).isFalse();
    }

    @Test
    void successFollowedByFailureShouldAllowAStateChangeToHealthyAfterAnotherSuccessOccurs() {
        final State state = new State(NAME, 1, 1, false, listener);
        assertThat(state.getHealthy()).isFalse();

        state.success();

        assertThat(didStateChange).isTrue();
        assertThat(state.getHealthy()).isTrue();

        didStateChange.set(false);

        state.failure();

        assertThat(didStateChange).isTrue();
        assertThat(state.getHealthy()).isFalse();

        didStateChange.set(false);

        state.success();

        assertThat(didStateChange).isTrue();
        assertThat(state.getHealthy()).isTrue();
    }

    @Test
    void dependencyFailingThenRecoveringTriggersStateChangeEventsCorrectly() {
        // given
        final State state = new State(NAME, 3, 2, true, listenerMock);

        // when / then
        state.success(); // start success
        state.failure(); // first failure
        state.failure();
        state.failure(); // should trigger callback transitioning to unhealthy
        verify(listenerMock).onStateChanged(NAME, false);

        state.success(); // dependency recovering and starts returning healthy
        state.success(); // should trigger callback transitioning to healthy
        verify(listenerMock).onStateChanged(NAME, true);
    }
}
