package io.dropwizard.jetty;

import org.eclipse.jetty.http.BadMessageException;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mockito;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.zip.ZipException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
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
@RunWith(Enclosed.class)
public class ZipExceptionHandlingInputStreamTest {

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

    @RunWith(Parameterized.class)
    public static class ExceptionTest {

        @Parameterized.Parameters(name = "{0}")
        public static Collection<Object[]> parameters() {
            return Arrays.asList(new Object[][] {
                {new EOFException(), BadMessageException.class},
                {new ZipException(), BadMessageException.class},
                {new IOException(), IOException.class},
            });
        }

        private final InputStream delegate = Mockito.mock(InputStream.class);
        private final ZipExceptionHandlingInputStream in = new ZipExceptionHandlingInputStream(delegate, "gzip");

        private final Exception t;
        private final Class<? extends Exception> expected;

        public ExceptionTest(Exception t, Class<? extends Exception> expected) {
            this.t = t;
            this.expected = expected;
        }

        @Test
        public void testReadBytes() throws Exception {
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
        public void testReadByte() throws Exception {
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
        public void testSkip() throws Exception {
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
        public void testAvailable() throws Exception {
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
        public void testClose() throws Exception {
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
        public void testReset() throws Exception {
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
