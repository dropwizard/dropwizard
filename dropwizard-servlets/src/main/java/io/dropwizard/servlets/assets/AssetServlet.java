package io.dropwizard.servlets.assets;

import io.dropwizard.util.Resources;

import javax.annotation.Nullable;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.CRC32;

public class AssetServlet extends HttpServlet {
    private static final long serialVersionUID = 6393345594784987908L;

    // HTTP header names
    private static final String IF_MODIFIED_SINCE = "If-Modified-Since";
    private static final String IF_NONE_MATCH = "If-None-Match";
    private static final String IF_RANGE = "If-Range";
    private static final String RANGE = "Range";
    private static final String ACCEPT_RANGES = "Accept-Ranges";
    private static final String CONTENT_RANGE = "Content-Range";
    private static final String ETAG = "ETag";
    private static final String LAST_MODIFIED = "Last-Modified";

    private static class CachedAsset {
        private static final CRC32 CRC_32 = new CRC32();
        private final byte[] resource;
        private final String eTag;
        private final long lastModifiedTime;

        private CachedAsset(byte[] resource, long lastModifiedTime) {
            this.resource = resource;
            this.eTag = '"' + hash(resource) + '"';
            this.lastModifiedTime = lastModifiedTime;
        }

        private static String hash(byte[] resource) {
            CRC_32.reset();
            CRC_32.update(resource);
            return Long.toHexString(CRC_32.getValue());
        }

        public byte[] getResource() {
            return resource;
        }

        public String getETag() {
            return eTag;
        }

        public long getLastModifiedTime() {
            return lastModifiedTime;
        }
    }

    private static final String DEFAULT_MEDIA_TYPE = "text/html";

    private final String resourcePath;
    private final String uriPath;

    @Nullable
    private final String indexFile;

    @Nullable
    private final Charset defaultCharset;

    /**
     * Creates a new {@code AssetServlet} that serves static assets loaded from {@code resourceURL}
     * (typically a file: or jar: URL). The assets are served at URIs rooted at {@code uriPath}. For
     * example, given a {@code resourceURL} of {@code "file:/data/assets"} and a {@code uriPath} of
     * {@code "/js"}, an {@code AssetServlet} would serve the contents of {@code
     * /data/assets/example.js} in response to a request for {@code /js/example.js}. If a directory
     * is requested and {@code indexFile} is defined, then {@code AssetServlet} will attempt to
     * serve a file with that name in that directory. If a directory is requested and {@code
     * indexFile} is null, it will serve a 404.
     *
     * @param resourcePath   the base URL from which assets are loaded
     * @param uriPath        the URI path fragment in which all requests are rooted
     * @param indexFile      the filename to use when directories are requested, or null to serve no
     *                       indexes
     * @param defaultCharset the default character set
     */
    public AssetServlet(String resourcePath,
                        String uriPath,
                        @Nullable String indexFile,
                        @Nullable Charset defaultCharset) {
        final String trimmedPath = trimSlashes(resourcePath);
        this.resourcePath = trimmedPath.isEmpty() ? trimmedPath : trimmedPath + '/';
        final String trimmedUri = trimTrailingSlashes(uriPath);
        this.uriPath = trimmedUri.isEmpty() ? "/" : trimmedUri;
        this.indexFile = indexFile;
        this.defaultCharset = defaultCharset;
    }

    private static String trimSlashes(String s) {
        final Matcher matcher = Pattern.compile("^/*(.*?)/*$").matcher(s);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return s;
        }
    }

    private static String trimTrailingSlashes(String s) {
        final Matcher matcher = Pattern.compile("(.*?)/*$").matcher(s);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return s;
        }
    }

    public URL getResourceURL() {
        return Resources.getResource(resourcePath);
    }

    public String getUriPath() {
        return uriPath;
    }

    @Nullable
    public String getIndexFile() {
        return indexFile;
    }

    @Override
    protected void doGet(HttpServletRequest req,
                         HttpServletResponse resp) throws ServletException, IOException {
        try {
            final StringBuilder builder = new StringBuilder(req.getServletPath());
            if (req.getPathInfo() != null) {
                builder.append(req.getPathInfo());
            }
            final CachedAsset cachedAsset = loadAsset(builder.toString());
            if (cachedAsset == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            if (isCachedClientSide(req, cachedAsset)) {
                resp.sendError(HttpServletResponse.SC_NOT_MODIFIED);
                return;
            }

            final String rangeHeader = req.getHeader(RANGE);

            final int resourceLength = cachedAsset.getResource().length;
            List<ByteRange> ranges = Collections.emptyList();

            boolean usingRanges = false;
            // Support for HTTP Byte Ranges
            // http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html
            if (rangeHeader != null) {

                final String ifRange = req.getHeader(IF_RANGE);

                if (ifRange == null || cachedAsset.getETag().equals(ifRange)) {

                    try {
                        ranges = parseRangeHeader(rangeHeader, resourceLength);
                    } catch (NumberFormatException e) {
                        resp.sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                        return;
                    }

                    if (ranges.isEmpty()) {
                        resp.sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                        return;
                    }

                    resp.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
                    usingRanges = true;

                    final String byteRanges = ranges.stream()
                            .map(ByteRange::toString)
                            .collect(Collectors.joining(","));
                    resp.addHeader(CONTENT_RANGE, "bytes " + byteRanges + "/" + resourceLength);
                }
            }

            resp.setDateHeader(LAST_MODIFIED, cachedAsset.getLastModifiedTime());
            resp.setHeader(ETAG, cachedAsset.getETag());

            final String mediaType = Optional.ofNullable(req.getServletContext().getMimeType(req.getRequestURI()))
                .orElse(DEFAULT_MEDIA_TYPE);
            if (mediaType.startsWith("video") || mediaType.startsWith("audio") || usingRanges) {
                resp.addHeader(ACCEPT_RANGES, "bytes");
            }

            resp.setContentType(mediaType);
            if (defaultCharset != null) {
                resp.setCharacterEncoding(defaultCharset.toString());
            }

            try (ServletOutputStream output = resp.getOutputStream()) {
                if (usingRanges) {
                    for (ByteRange range : ranges) {
                        output.write(cachedAsset.getResource(), range.getStart(),
                                range.getEnd() - range.getStart() + 1);
                    }
                } else {
                    output.write(cachedAsset.getResource());
                }
            }
        } catch (RuntimeException | URISyntaxException ignored) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Nullable
    private CachedAsset loadAsset(String key) throws URISyntaxException, IOException {
        if (!key.startsWith(uriPath)) {
            throw new IllegalArgumentException("Cache key must start with " + uriPath);
        }

        final String requestedResourcePath = trimSlashes(key.substring(uriPath.length()));
        final String absoluteRequestedResourcePath = trimSlashes(this.resourcePath + requestedResourcePath);

        URL requestedResourceURL = getResourceUrl(absoluteRequestedResourcePath);
        if (ResourceURL.isDirectory(requestedResourceURL)) {
            if (indexFile != null) {
                requestedResourceURL = getResourceUrl(absoluteRequestedResourcePath + '/' + indexFile);
            } else {
                // directory requested but no index file defined
                return null;
            }
        }

        long lastModified = ResourceURL.getLastModified(requestedResourceURL);
        if (lastModified < 1) {
            // Something went wrong trying to get the last modified time: just use the current time
            lastModified = System.currentTimeMillis();
        }

        // zero out the millis since the date we get back from If-Modified-Since will not have them
        lastModified = (lastModified / 1000) * 1000;
        return new CachedAsset(readResource(requestedResourceURL), lastModified);
    }

    protected URL getResourceUrl(String absoluteRequestedResourcePath) {
        return Resources.getResource(absoluteRequestedResourcePath);
    }

    protected byte[] readResource(URL requestedResourceURL) throws IOException {
        return Resources.toByteArray(requestedResourceURL);
    }

    private boolean isCachedClientSide(HttpServletRequest req, CachedAsset cachedAsset) {
        return cachedAsset.getETag().equals(req.getHeader(IF_NONE_MATCH)) ||
                (req.getDateHeader(IF_MODIFIED_SINCE) >= cachedAsset.getLastModifiedTime());
    }

    /**
     * Parses a given Range header for one or more byte ranges.
     *
     * @param rangeHeader    Range header to parse
     * @param resourceLength Length of the resource in bytes
     * @return List of parsed ranges
     */
    private List<ByteRange> parseRangeHeader(final String rangeHeader, final int resourceLength) {
        final List<ByteRange> byteRanges;
        if (rangeHeader.contains("=")) {
            final String[] parts = rangeHeader.split("=", -1);
            if (parts.length > 1) {
                byteRanges = Arrays.stream(parts[1].split(",", -1))
                        .map(String::trim)
                        .map(s -> ByteRange.parse(s, resourceLength))
                        .collect(Collectors.toList());
            } else {
                byteRanges = Collections.emptyList();
            }
        } else {
            byteRanges = Collections.emptyList();
        }
        return byteRanges;
    }
}
