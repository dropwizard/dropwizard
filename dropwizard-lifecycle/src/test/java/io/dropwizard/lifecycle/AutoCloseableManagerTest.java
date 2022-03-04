package io.dropwizard.lifecycle;

import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class AutoCloseableManagerTest {

    private final AutoCloseable managed = mock(AutoCloseable.class);
    private final AutoCloseableManager closeableManager = new AutoCloseableManager(this.managed);

    @Test
    void startsAndStops() throws Exception {
        this.closeableManager.start();
        this.closeableManager.stop();

        verify(this.managed).close();
    }

}
