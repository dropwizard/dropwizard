package io.dropwizard.lifecycle;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Test;

public class AutoCloseableManagerTest {

    private final AutoCloseable managed = mock(AutoCloseable.class);
    private final AutoCloseableManager closeableManager = new AutoCloseableManager(this.managed);

    @Test
    public void startsAndStops() throws Exception {
        this.closeableManager.start();
        this.closeableManager.stop();

        verify(this.managed).close();
    }

}
