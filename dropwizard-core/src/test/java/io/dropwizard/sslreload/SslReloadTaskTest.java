package io.dropwizard.sslreload;

import io.dropwizard.jetty.SslReload;
import org.junit.jupiter.api.Test;

import java.io.PrintWriter;
import java.util.Set;

import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.catchRuntimeException;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class SslReloadTaskTest {
    @Test
    void reloadingNothingSucceeds() {
        SslReloadTask reloadTask = new SslReloadTask();

        assertThatNoException().isThrownBy(() -> reloadTask.execute(emptyMap(), mock(PrintWriter.class)));

        reloadTask.setReloaders(Set.of());
        assertThatNoException().isThrownBy(() -> reloadTask.execute(emptyMap(), mock(PrintWriter.class)));
    }

    @Test
    void failingDryRunSkipsAll() throws Exception {
        SslReload failingDryRun = mock(SslReload.class);
        doThrow(new RuntimeException("Dry run failed")).when(failingDryRun).reloadDryRun();
        SslReload ok = mock(SslReload.class);

        SslReloadTask reloadTask = new SslReloadTask();
        reloadTask.setReloaders(Set.of(failingDryRun, ok));

        assertThat(catchRuntimeException(() -> reloadTask.execute(emptyMap(), mock(PrintWriter.class)))
            .getMessage())
            .isEqualTo("Dry run failed");

        verify(failingDryRun, never()).reload();
        verify(ok, never()).reload();
    }

    @Test
    void reloadsMultiple() throws Exception {
        SslReload ok1 = mock(SslReload.class);
        SslReload ok2 = mock(SslReload.class);

        SslReloadTask reloadTask = new SslReloadTask();
        reloadTask.setReloaders(Set.of(ok1, ok2));

        reloadTask.execute(emptyMap(), mock(PrintWriter.class));

        verify(ok1, times(1)).reload();
        verify(ok2, times(1)).reload();
    }
}
