package io.dropwizard.jetty;

import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipException;

import org.eclipse.jetty.http.BadMessageException;

/**
 * This InputStream is used to decorate a GZIPInputStream or InflaterInputStream, intercept decompression
 * exceptions, and rethrow them as BadMessageExceptions
 */
class ZipExceptionHandlingInputStream extends FilterInputStream {

    private final String format;

    ZipExceptionHandlingInputStream(InputStream in, String format) {
        super(in);
        this.format = format;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        try {
            return super.read(b, off, len);
        } catch (IOException e) {
            throw handleException(format, e);
        }
    }

    @Override
    public int read() throws IOException {
        try {
            return super.read();
        } catch (IOException e) {
            throw handleException(format, e);
        }
    }

    @Override
    public long skip(long n) throws IOException {
        try {
            return super.skip(n);
        } catch (IOException e) {
            throw handleException(format, e);
        }
    }

    @Override
    public int available() throws IOException {
        try {
            return super.available();
        } catch (IOException e) {
            throw handleException(format, e);
        }
    }

    @Override
    public void close() throws IOException {
        try {
            super.close();
        } catch (IOException e) {
            throw handleException(format, e);
        }
    }

    @Override
    synchronized public void reset() throws IOException {
        try {
            super.reset();
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
