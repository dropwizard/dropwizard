package io.dropwizard.jetty;

import org.eclipse.jetty.http.BadMessageException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Stream;
import java.util.zip.ZipException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@SuppressWarnings("ALL")
public class ZipExceptionHandlingInputStreamTest {

    @Nested
    public static class DelegateTest {

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

    @Nested
    public static class ExceptionTest {

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
        public void testReadBytes(Exception t, Class<? extends Exception> expected) throws Exception {
            doThrow(t).when(delegate).read(Mockito.any(byte[].class), anyInt(), anyInt());
            byte[] buffer = new byte[20];
            try {
                in.read(buffer, 4, 16);
                fail();
            } catch (Exception e) {
                assertThat(e).isInstanceOf(expected);
                verify(delegate).read(same(buffer), eq(4), eq(16));
                verifyNoMoreInteractions(delegate);
            }
        }

        @Test
        @MethodSource("parameters")
        public void testReadByte(Exception t, Class<? extends Exception> expected) throws Exception {
            doThrow(t).when(delegate).read();
            try {
                in.read();
                fail();
            } catch (Exception e) {
                assertThat(e).isInstanceOf(expected);
                verify(delegate).read();
                verifyNoMoreInteractions(delegate);
            }
        }

        @Test
        @MethodSource("parameters")
        public void testSkip(Exception t, Class<? extends Exception> expected) throws Exception {
            doThrow(t).when(delegate).skip(anyLong());
            try {
                in.skip(42L);
                fail();
            } catch (Exception e) {
                assertThat(e).isInstanceOf(expected);
                verify(delegate).skip(42L);
                verifyNoMoreInteractions(delegate);
            }
        }

        @Test
        @MethodSource("parameters")
        public void testAvailable(Exception t, Class<? extends Exception> expected) throws Exception {
            doThrow(t).when(delegate).available();
            try {
                in.available();
                fail();
            } catch (Exception e) {
                assertThat(e).isInstanceOf(expected);
                verify(delegate).available();
                verifyNoMoreInteractions(delegate);
            }
        }

        @Test
        @MethodSource("parameters")
        public void testClose(Exception t, Class<? extends Exception> expected) throws Exception {
            doThrow(t).when(delegate).close();
            try {
                in.close();
                fail();
            } catch (Exception e) {
                assertThat(e).isInstanceOf(expected);
                verify(delegate).close();
                verifyNoMoreInteractions(delegate);
            }
        }

        @Test
        @MethodSource("parameters")
        public void testReset(Exception t, Class<? extends Exception> expected) throws Exception {
            doThrow(t).when(delegate).reset();
            try {
                in.reset();
                fail();
            } catch (Exception e) {
                assertThat(e).isInstanceOf(expected);
                verify(delegate).reset();
                verifyNoMoreInteractions(delegate);
            }
        }
    }
}
