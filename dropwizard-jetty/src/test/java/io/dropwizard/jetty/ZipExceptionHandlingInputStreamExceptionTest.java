package io.dropwizard.jetty;

import org.eclipse.jetty.http.BadMessageException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Stream;
import java.util.zip.ZipException;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

class ZipExceptionHandlingInputStreamExceptionTest {

    public static Stream<Arguments> parameters() {
        return Stream.of(
            Arguments.of(new EOFException(), BadMessageException.class),
            Arguments.of(new ZipException(), BadMessageException.class),
            Arguments.of(new IOException(), IOException.class)
        );
    }

    private final InputStream delegate = Mockito.mock(InputStream.class);
    private final ZipExceptionHandlingInputStream in = new ZipExceptionHandlingInputStream(delegate, "gzip");

    @ParameterizedTest
    @MethodSource("parameters")
    void testReadBytes(Exception t, Class<? extends Exception> expected) throws Exception {
        doThrow(t).when(delegate).read(Mockito.any(byte[].class), anyInt(), anyInt());
        byte[] buffer = new byte[20];
        assertThatExceptionOfType(expected).isThrownBy(() -> in.read(buffer, 4, 16));
        verify(delegate).read(same(buffer), eq(4), eq(16));
        verifyNoMoreInteractions(delegate);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    void testReadByte(Exception t, Class<? extends Exception> expected) throws Exception {
        doThrow(t).when(delegate).read();
        assertThatExceptionOfType(expected).isThrownBy(() -> in.read());
        verify(delegate).read();
        verifyNoMoreInteractions(delegate);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    void testSkip(Exception t, Class<? extends Exception> expected) throws Exception {
        doThrow(t).when(delegate).skip(anyLong());
        assertThatExceptionOfType(expected).isThrownBy(() -> in.skip(42L));
        verify(delegate).skip(42L);
        verifyNoMoreInteractions(delegate);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    void testAvailable(Exception t, Class<? extends Exception> expected) throws Exception {
        doThrow(t).when(delegate).available();
        assertThatExceptionOfType(expected).isThrownBy(() -> in.available());
        verify(delegate).available();
        verifyNoMoreInteractions(delegate);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    void testClose(Exception t, Class<? extends Exception> expected) throws Exception {
        doThrow(t).when(delegate).close();
        assertThatExceptionOfType(expected).isThrownBy(() -> in.close());
        verify(delegate).close();
        verifyNoMoreInteractions(delegate);
    }

    @ParameterizedTest
    @MethodSource("parameters")
    void testReset(Exception t, Class<? extends Exception> expected) throws Exception {
        doThrow(t).when(delegate).reset();
        assertThatExceptionOfType(expected).isThrownBy(() -> in.reset());
        verify(delegate).reset();
        verifyNoMoreInteractions(delegate);
    }
}
