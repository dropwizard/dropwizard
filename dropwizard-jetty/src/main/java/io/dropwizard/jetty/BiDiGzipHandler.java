package io.dropwizard.jetty;

import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.gzip.GzipHandler;

import javax.servlet.ReadListener;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Enumeration;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;
import java.util.zip.GZIPInputStream;

/**
 * An extension of {@link GzipHandler} which decompresses gzip- and deflate-encoded request
 * entities.
 */
public class BiDiGzipHandler extends GzipHandler {

    private final ThreadLocal<Inflater> localInflater = new ThreadLocal<>();

    /**
     * Size of the buffer for decompressing requests
     */
    private int inputBufferSize = 8192;
   
    /**
     * Whether inflating (decompressing) of deflate-encoded requests
     * should be performed in the GZIP-compatible mode
     */
    private boolean inflateNoWrap = true;

    public boolean isInflateNoWrap() {
        return inflateNoWrap;
    }

    public void setInflateNoWrap(boolean inflateNoWrap) {
        this.inflateNoWrap = inflateNoWrap;
    }

    public BiDiGzipHandler() {
    }

    public void setInputBufferSize(int inputBufferSize) {
        this.inputBufferSize = inputBufferSize;
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        final String encoding = request.getHeader(HttpHeader.CONTENT_ENCODING.asString());
        if (GZIP.equalsIgnoreCase(encoding)) {
            super.handle(target, baseRequest, wrapGzippedRequest(removeContentEncodingHeader(request)), response);
        } else if (DEFLATE.equalsIgnoreCase(encoding)) {
            super.handle(target, baseRequest, wrapDeflatedRequest(removeContentEncodingHeader(request)), response);
        } else {
            super.handle(target, baseRequest, request, response);
        }
    }

    private Inflater buildInflater() {
        final Inflater inflater = localInflater.get();
        if (inflater != null) {
            // The request could fail in the middle of decompressing, so potentially we can get
            // a broken inflater in the thread local storage. That's why we need to clear the storage.
            localInflater.set(null);

            // Reuse the inflater from the thread local storage
            inflater.reset();
            return inflater;
        } else {
            return new Inflater(inflateNoWrap);
        }
    }

    private WrappedServletRequest wrapDeflatedRequest(HttpServletRequest request) throws IOException {
        final Inflater inflater = buildInflater();
        final InflaterInputStream input = new InflaterInputStream(request.getInputStream(), inflater, inputBufferSize) {
            @Override
            public void close() throws IOException {
                super.close();
                localInflater.set(inflater);
            }
        };
        return new WrappedServletRequest(request, input);
    }

    private WrappedServletRequest wrapGzippedRequest(HttpServletRequest request) throws IOException {
        return new WrappedServletRequest(request, new GZIPInputStream(request.getInputStream(), inputBufferSize));
    }

    private HttpServletRequest removeContentEncodingHeader(final HttpServletRequest request) {
        return new RemoveHttpHeaderWrapper(request, HttpHeader.CONTENT_ENCODING.asString());
    }

    private static class WrappedServletRequest extends HttpServletRequestWrapper {
        private final ServletInputStream input;
        private final BufferedReader reader;

        private WrappedServletRequest(HttpServletRequest request,
                                      InputStream inputStream) throws IOException {
            super(request);
            this.input = new WrappedServletInputStream(inputStream);
            this.reader = new BufferedReader(new InputStreamReader(input, getCharset()));
        }

        private Charset getCharset() {
            final String encoding = getCharacterEncoding();
            if (encoding == null || !Charset.isSupported(encoding)) {
                return StandardCharsets.ISO_8859_1;
            }
            return Charset.forName(encoding);
        }

        @Override
        public ServletInputStream getInputStream() throws IOException {
            return input;
        }

        @Override
        public BufferedReader getReader() throws IOException {
            return reader;
        }
    }

    private static class WrappedServletInputStream extends ServletInputStream {
        private final InputStream input;

        private WrappedServletInputStream(InputStream input) {
            this.input = input;
        }

        @Override
        public void close() throws IOException {
            input.close();
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            return input.read(b, off, len);
        }

        @Override
        public int available() throws IOException {
            return input.available();
        }

        @Override
        public void mark(int readlimit) {
            input.mark(readlimit);
        }

        @Override
        public boolean markSupported() {
            return input.markSupported();
        }

        @Override
        public int read() throws IOException {
            return input.read();
        }

        @Override
        public void reset() throws IOException {
            input.reset();
        }

        @Override
        public long skip(long n) throws IOException {
            return input.skip(n);
        }

        @Override
        public int read(byte[] b) throws IOException {
            return input.read(b);
        }

        @Override
        public boolean isFinished() {
            try {
                return input.available() == 0;
            } catch (IOException ignored) {
            }
            return true;
        }

        @Override
        public boolean isReady() {
            try {
                return input.available() > 0;
            } catch (IOException ignored) {
            }
            return false;
        }

        @Override
        public void setReadListener(ReadListener readListener) {
            throw new UnsupportedOperationException();
        }
    }

    private static class RemoveHttpHeaderWrapper extends HttpServletRequestWrapper {
        private final String headerName;

        RemoveHttpHeaderWrapper(final HttpServletRequest request, final String headerName) {
            super(request);
            this.headerName = headerName;
        }

        /**
         * The default behavior of this method is to return
         * getIntHeader(String name) on the wrapped request object.
         *
         * @param name a <code>String</code> specifying the name of a request header
         */
        @Override
        public int getIntHeader(final String name) {
            if (headerName.equalsIgnoreCase(name)) {
                return -1;
            } else {
                return super.getIntHeader(name);
            }
        }

        /**
         * The default behavior of this method is to return getHeaders(String name)
         * on the wrapped request object.
         *
         * @param name a <code>String</code> specifying the name of a request header
         */
        @Override
        public Enumeration<String> getHeaders(final String name) {
            if (headerName.equalsIgnoreCase(name)) {
                return Collections.emptyEnumeration();
            } else {
                return super.getHeaders(name);
            }
        }

        /**
         * The default behavior of this method is to return getHeader(String name)
         * on the wrapped request object.
         *
         * @param name a <code>String</code> specifying the name of a request header
         */
        @Override
        public String getHeader(final String name) {
            if (headerName.equalsIgnoreCase(name)) {
                return null;
            } else {
                return super.getHeader(name);
            }
        }

        /**
         * The default behavior of this method is to return getDateHeader(String name)
         * on the wrapped request object.
         *
         * @param name a <code>String</code> specifying the name of a request header
         */
        @Override
        public long getDateHeader(final String name) {
            if (headerName.equalsIgnoreCase(name)) {
                return -1L;
            } else {
                return super.getDateHeader(name);
            }
        }
    }
}
