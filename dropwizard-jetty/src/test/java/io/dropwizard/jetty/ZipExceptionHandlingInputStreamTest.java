package io.dropwizard.jetty;

import java.io.EOFException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.zip.ZipException;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import org.eclipse.jetty.http.BadMessageException;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mockito;

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
                {new EOFException()}, {new ZipException()}
            });
        }

        private final InputStream delegate = Mockito.mock(InputStream.class);
        private final ZipExceptionHandlingInputStream in = new ZipExceptionHandlingInputStream(delegate, "gzip");

        private final Exception t;

        public ExceptionTest(Exception t) {
            this.t = t;
        }

        @Test(expected = BadMessageException.class)
        public void testReadBytes() throws Exception {
            doThrow(t).when(delegate).read(Mockito.any(byte[].class), anyInt(), anyInt());
            in.read(new byte[32], 0, 32);
        }

        @Test(expected = BadMessageException.class)
        public void testReadByte() throws Exception {
            doThrow(t).when(delegate).read();
            in.read();
        }

        @Test(expected = BadMessageException.class)
        public void testSkip() throws Exception {
            doThrow(t).when(delegate).skip(42L);
            in.skip(42L);
        }

        @Test(expected = BadMessageException.class)
        public void testAvailable() throws Exception {
            doThrow(t).when(delegate).available();
            in.available();
        }

        @Test(expected = BadMessageException.class)
        public void testClose() throws Exception {
            doThrow(t).when(delegate).close();
            in.close();
        }

        @Test(expected = BadMessageException.class)
        public void testReset() throws Exception {
            doThrow(t).when(delegate).reset();
            in.reset();
        }
    }
}
