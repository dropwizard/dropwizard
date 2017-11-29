package io.dropwizard.jetty;

import org.eclipse.jetty.http.BadMessageException;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipException;

/**
 * This InputStream is used to decorate a GZIPInputStream or InflaterInputStream, intercept decompression
 * exceptions, and rethrow them as BadMessageExceptions
 */
class ZipExceptionHandlingInputStream extends InputStream {

    private final InputStream delegate;
    private final String format;

    ZipExceptionHandlingInputStream(InputStream in, String format) {
        this.delegate = in;
        this.format = format;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        try {
            return delegate.read(b, off, len);
        } catch (IOException e) {
            throw handleException(format, e);
        }
    }

    @Override
    public int read() throws IOException {
        try {
            return delegate.read();
        } catch (IOException e) {
            throw handleException(format, e);
        }
    }

    @Override
    public long skip(long n) throws IOException {
        try {
            return delegate.skip(n);
        } catch (IOException e) {
            throw handleException(format, e);
        }
    }

    @Override
    public int available() throws IOException {
        try {
            return delegate.available();
        } catch (IOException e) {
            throw handleException(format, e);
        }
    }

    @Override
    public void close() throws IOException {
        try {
            delegate.close();
        } catch (IOException e) {
            throw handleException(format, e);
        }
    }

    @Override
    public synchronized void mark(int readlimit) {
        delegate.mark(readlimit);
    }

    @Override
    public boolean markSupported() {
        return delegate.markSupported();
    }

    @Override
    synchronized public void reset() throws IOException {
        try {
            delegate.reset();
        } catch (IOException e) {
            throw handleException(format, e);
        }
    }

    static BadMessageException handleException(String format, IOException e) throws IOException {
        if (e instanceof ZipException) {
            return buildBadDataException(format, e);
        } else if (e instanceof EOFException) {
            return buildPrematureEofException(format, e);
        } else {
            throw e;
        }
    }

    private static BadMessageException buildBadDataException(String format, Throwable cause) {
        return new BadMessageException(400, "Invalid " + format + " data in request", cause);
    }

    private static BadMessageException buildPrematureEofException(String format, Throwable cause) {
        return new BadMessageException(400, "Premature end of " + format + " data", cause);
    }
}
