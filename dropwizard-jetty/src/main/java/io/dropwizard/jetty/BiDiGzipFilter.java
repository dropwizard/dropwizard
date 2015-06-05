package io.dropwizard.jetty;

import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.servlets.IncludableGzipFilter;

import javax.servlet.FilterChain;
import javax.servlet.ReadListener;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.zip.Deflater;
import java.util.zip.DeflaterInputStream;
import java.util.zip.GZIPInputStream;

/**
 * An extension of {@link IncludableGzipFilter} which decompresses gzip- and deflate-encoded request
 * entities.
 */
public class BiDiGzipFilter extends IncludableGzipFilter {
    private final ThreadLocal<Deflater> localDeflater = new ThreadLocal<>();

    public Set<String> getMimeTypes() {
        return _mimeTypes;
    }

    public void setMimeTypes(Set<String> mimeTypes) {
        _mimeTypes.clear();
        _mimeTypes.addAll(mimeTypes);
    }

    public int getBufferSize() {
        return _bufferSize;
    }

    public void setBufferSize(int bufferSize) {
        this._bufferSize = bufferSize;
    }

    public int getMinGzipSize() {
        return _minGzipSize;
    }

    public void setMinGzipSize(int minGzipSize) {
        this._minGzipSize = minGzipSize;
    }

    public int getDeflateCompressionLevel() {
        return _deflateCompressionLevel;
    }

    public void setDeflateCompressionLevel(int level) {
        this._deflateCompressionLevel = level;
    }

    public boolean isDeflateNoWrap() {
        return _deflateNoWrap;
    }

    public void setDeflateNoWrap(boolean noWrap) {
        this._deflateNoWrap = noWrap;
    }

    public Set<String> getMethods() {
        return _methods;
    }

    public void setMethods(Set<String> methods) {
        this._methods.clear();
        this._methods.addAll(methods);
    }

    public Set<String> getExcludedAgents() {
        return _excludedAgents;
    }

    public void setExcludedAgents(Set<String> userAgents) {
        this._excludedAgents = userAgents;
    }

    public Set<Pattern> getExcludedAgentPatterns() {
        return _excludedAgentPatterns;
    }

    public void setExcludedAgentPatterns(Set<Pattern> userAgentPatterns) {
        this._excludedAgentPatterns = userAgentPatterns;
    }

    public Set<String> getExcludedPaths() {
        return _excludedPaths;
    }

    public void setExcludedPaths(Set<String> paths) {
        this._excludedPaths = paths;
    }

    public Set<Pattern> getExcludedPathPatterns() {
        return _excludedPathPatterns;
    }

    public void setExcludedPathPatterns(Set<Pattern> patterns) {
        this._excludedPathPatterns = patterns;
    }

    public String getVary() {
        return _vary;
    }

    public void setVary(String vary) {
        this._vary = vary;
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        final HttpServletRequest request = (HttpServletRequest) req;
        final String encoding = request.getHeader(HttpHeader.CONTENT_ENCODING.asString());
        if (GZIP.equalsIgnoreCase(encoding)) {
            super.doFilter(wrapGzippedRequest(removeContentEncodingHeader(request)), res, chain);
        } else if (DEFLATE.equalsIgnoreCase(encoding)) {
            super.doFilter(wrapDeflatedRequest(removeContentEncodingHeader(request)), res, chain);
        } else {
            super.doFilter(req, res, chain);
        }
    }

    private Deflater buildDeflater() {
        final Deflater deflater = localDeflater.get();
        if (deflater != null) {
            return deflater;
        }
        return new Deflater(_deflateCompressionLevel, _deflateNoWrap);
    }

    private ServletRequest wrapDeflatedRequest(HttpServletRequest request) throws IOException {
        final Deflater deflater = buildDeflater();
        final DeflaterInputStream input = new DeflaterInputStream(request.getInputStream(), deflater, _bufferSize) {
            @Override
            public void close() throws IOException {
                deflater.reset();
                localDeflater.set(deflater);
                super.close();
            }
        };
        return new WrappedServletRequest(request, input);
    }

    private ServletRequest wrapGzippedRequest(HttpServletRequest request) throws IOException {
        return new WrappedServletRequest(request, new GZIPInputStream(request.getInputStream(), _bufferSize));
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

        public RemoveHttpHeaderWrapper(final HttpServletRequest request, final String headerName) {
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
