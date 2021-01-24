package io.dropwizard.jetty;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class ZipExceptionHandlingInputStreamDelegateTest {

    private final InputStream delegate = Mockito.mock(InputStream.class);
    private final ZipExceptionHandlingInputStream in = new ZipExceptionHandlingInputStream(delegate, "gzip");

    @Test
    public void testReadBytes() throws Exception {
        byte[] buffer = new byte[64];

        doReturn(buffer.length)
            .when(delegate).read(Mockito.any(byte[].class), anyInt(), anyInt());

        assertEquals(buffer.length, in.read(buffer, 0, buffer.length));
        verify(delegate).read(buffer, 0, buffer.length);
        verifyNoMoreInteractions(delegate);
    }

    @Test
    public void testReadByte() throws Exception {
        doReturn(42).when(delegate).read();
        assertEquals(42, in.read());
        verify(delegate).read();
        verifyNoMoreInteractions(delegate);
    }

    @Test
    public void testSkip() throws Exception {
        doReturn(42L).when(delegate).skip(42L);
        assertEquals(42L, in.skip(42L));
        verify(delegate).skip(42L);
        verifyNoMoreInteractions(delegate);
    }

    @Test
    public void testAvailable() throws Exception {
        doReturn(42).when(delegate).available();
        assertEquals(42, in.available());
        verify(delegate).available();
        verifyNoMoreInteractions(delegate);
    }

    @Test
    public void testClose() throws Exception {
        in.close();
        verify(delegate).close();
        verifyNoMoreInteractions(delegate);
    }

    @Test
    public void testMark() {
        in.mark(42);
        verify(delegate).mark(42);
        verifyNoMoreInteractions(delegate);
    }

    @Test
    public void testMarkSupported() {
        doReturn(true).when(delegate).markSupported();
        assertTrue(in.markSupported());
        verify(delegate).markSupported();
        verifyNoMoreInteractions(delegate);
    }

    @Test
    public void testReset() throws Exception {
        doNothing().when(delegate).reset();
        in.reset();
        verify(delegate).reset();
        verifyNoMoreInteractions(delegate);
    }
}
