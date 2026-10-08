package io.dropwizard.servlets.assets;

import io.dropwizard.util.Resources;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.http.ByteRange;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HexFormat;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.CRC32;

public class AssetServlet extends HttpServlet {
    private static final long serialVersionUID = 6393345594784987908L;

    // HTTP header names
    private static final String IF_MATCH = "If-Match";
    private static final String IF_UNMODIFIED_SINCE = "If-Unmodified-Since";
    private static final String IF_NONE_MATCH = "If-None-Match";
    private static final String IF_MODIFIED_SINCE = "If-Modified-Since";
    private static final String IF_RANGE = "If-Range";
    private static final String RANGE = "Range";
    private static final String ACCEPT_RANGES = "Accept-Ranges";
    private static final String CONTENT_RANGE = "Content-Range";
    private static final String ETAG = "ETag";
    private static final String LAST_MODIFIED = "Last-Modified";

    private static final String MULTIPART_CONTENT_TYPE_PREFIX = "multipart/byteranges; boundary=";

    private static class CachedAsset {
        private final byte[] resource;
        private final String eTag;
        private final long lastModifiedTime;

        private CachedAsset(byte[] resource, long lastModifiedTime) {
            this.resource = resource;
            this.eTag = '"' + hash(resource) + '"';
            this.lastModifiedTime = lastModifiedTime;
        }

        private static String hash(byte[] resource) {
            final CRC32 crc32 = new CRC32();
            crc32.update(resource);
            return Long.toHexString(crc32.getValue());
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

    private final String defaultMediaType;

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
        this(resourcePath, uriPath, indexFile, DEFAULT_MEDIA_TYPE, defaultCharset);
    }

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
     * @param resourcePath     the base URL from which assets are loaded
     * @param uriPath          the URI path fragment in which all requests are rooted
     * @param indexFile        the filename to use when directories are requested, or null to serve no
     *                         indexes
     * @param defaultMediaType the default media type
     * @param defaultCharset   the default character set
     * @since 2.0
     */
    public AssetServlet(String resourcePath,
                        String uriPath,
                        @Nullable String indexFile,
                        @Nullable String defaultMediaType,
                        @Nullable Charset defaultCharset) {
        final String trimmedPath = trimSlashes(resourcePath);
        this.resourcePath = trimmedPath.isEmpty() ? trimmedPath : trimmedPath + '/';
        final String trimmedUri = trimTrailingSlashes(uriPath);
        this.uriPath = trimmedUri.isEmpty() ? "/" : trimmedUri;
        this.indexFile = indexFile;
        this.defaultMediaType = defaultMediaType == null ? DEFAULT_MEDIA_TYPE : defaultMediaType;
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

    /**
     * @since 2.0
     */
    public String getDefaultMediaType() {
        return defaultMediaType;
    }


    /**
     * @since 2.0
     */
    @Nullable
    public Charset getDefaultCharset() {
        return defaultCharset;
    }

    @Override
    protected void doGet(HttpServletRequest req,
                         HttpServletResponse resp) throws ServletException, IOException {
        final StringBuilder builder = new StringBuilder(req.getServletPath());
        if (req.getPathInfo() != null) {
            builder.append(req.getPathInfo());
        }
        final CachedAsset cachedAsset = loadAsset(builder.toString());
        if (cachedAsset == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // Representation metadata applies to every response, including 304 and 412 - see RFC 7232 4.1 and 4.2.
        resp.setHeader(ETAG, cachedAsset.getETag());
        resp.setDateHeader(LAST_MODIFIED, cachedAsset.getLastModifiedTime());
        resp.setHeader(ACCEPT_RANGES, "bytes");

        // RFC 7232 section 6 precondition evaluation order.
        if (req.getHeader(IF_MATCH) != null) {
            if (!ifMatchMatches(req.getHeader(IF_MATCH), cachedAsset.getETag())) {
                resp.setStatus(HttpServletResponse.SC_PRECONDITION_FAILED);
                return;
            }
        } else if (req.getHeader(IF_UNMODIFIED_SINCE) != null) {  // If-Unmodified-Since only eval'd If-Match absent
            final long ifUnmodifiedSince = parseDateHeader(req, IF_UNMODIFIED_SINCE);
            if (ifUnmodifiedSince != -1 && cachedAsset.getLastModifiedTime() > ifUnmodifiedSince) {
                resp.setStatus(HttpServletResponse.SC_PRECONDITION_FAILED);
                return;
            }
        }

        if (req.getHeader(IF_NONE_MATCH) != null) {
            if (ifNoneMatchMatches(req.getHeader(IF_NONE_MATCH), cachedAsset.getETag())) {
                resp.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                return;
            }
        } else if (req.getHeader(IF_MODIFIED_SINCE) != null) {  // If-Modified-Since only eval'd If-None-Match absent
            final long ifModifiedSince = parseDateHeader(req, IF_MODIFIED_SINCE);
            if (ifModifiedSince != -1 && cachedAsset.getLastModifiedTime() <= ifModifiedSince) {
                resp.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                return;
            }
        }

        final String requestUri = req.getRequestURI();
        final String mediaType = Optional.ofNullable(
            req.getServletContext().getMimeType(
                indexFile != null && requestUri.endsWith("/")  // If indexFile configured (and directory requested),
                    ? requestUri + indexFile                   //   then use MIME type of the index file.
                    : requestUri))
            .orElse(defaultMediaType);

        final List<String> rangeHeaders = Collections.list(
            Optional.ofNullable(req.getHeaders(RANGE))
                .orElse(Collections.emptyEnumeration()));
        final long resourceLength = cachedAsset.getResource().length;
        List<ByteRange> parsedRanges = null;

        if (!rangeHeaders.isEmpty()) {
            if (req.getHeader(IF_RANGE) == null || ifRangeMatches(req, cachedAsset)) {
                parsedRanges = ByteRange.parse(rangeHeaders, resourceLength);

                if (parsedRanges.isEmpty()) {
                    resp.setHeader(CONTENT_RANGE, ByteRange.toNonSatisfiableHeaderValue(resourceLength));
                    resp.setStatus(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                    return;
                }

                resp.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
            }
        }

        final String boundary;
        if (parsedRanges != null && parsedRanges.size() > 1) {
            // Top-level content type is multipart/byteranges; each part carries the asset's own media type in its
            // own Content-Type header.
            boundary = generateBoundary();
            resp.setContentType(MULTIPART_CONTENT_TYPE_PREFIX + boundary);
        } else {
            boundary = null;
            resp.setContentType(mediaType);
            if (defaultCharset != null) {
                resp.setCharacterEncoding(defaultCharset.toString());
            }
            if (parsedRanges != null) {
                // at most only one Range header exists (multiple are handled in above 'if' block)
                resp.setHeader(CONTENT_RANGE, parsedRanges.get(0).toHeaderValue(resourceLength));
            }
        }

        try (ServletOutputStream output = resp.getOutputStream()) {
            if (parsedRanges == null) {
                output.write(cachedAsset.getResource());
            } else if (parsedRanges.size() == 1) {
                final ByteRange singleRange = parsedRanges.get(0);
                output.write(cachedAsset.getResource(), (int) singleRange.first(), (int) singleRange.getLength());
            } else {
                writeMultipartBody(output, cachedAsset.getResource(), parsedRanges, mediaType,
                        Objects.requireNonNull(boundary), resourceLength);
            }
        }
    }

    /**
     * Loads and caches the asset at the given key.
     *
     * @return the cached asset, or {@code null} if the resource is missing — the caller translates this into a 404
     * @throws IOException if the resource exists but cannot be read (translated to a 500 by the servlet container)
     */
    @Nullable
    private CachedAsset loadAsset(String key) throws IOException {
        if (!key.startsWith(uriPath)) {
            return null;
        }

        final String requestedResourcePath = trimSlashes(key.substring(uriPath.length()));
        final String absoluteRequestedResourcePath = trimSlashes(this.resourcePath + requestedResourcePath);

        URL requestedResourceURL;
        try {
            requestedResourceURL = getResourceURL(absoluteRequestedResourcePath);
            if (ResourceURL.isDirectory(requestedResourceURL)) {
                if (indexFile == null) {
                    // directory requested but no index file defined
                    return null;
                }
                requestedResourceURL = getResourceURL(absoluteRequestedResourcePath + '/' + indexFile);
            }
        } catch (IllegalArgumentException | URISyntaxException | ResourceNotFoundException notFound) {
            // Resource missing or path malformed - the caller translates null into a 404.
            return null;
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

    protected URL getResourceURL(String absoluteRequestedResourcePath) {
        return Resources.getResource(absoluteRequestedResourcePath);
    }

    protected byte[] readResource(URL requestedResourceURL) throws IOException {
        try (InputStream inputStream = requestedResourceURL.openStream()) {
            return inputStream.readAllBytes();
        }
    }

    /**
     * Evaluates an {@code If-Match} header value against our ETag per RFC 7232 3.1. The header may be the wildcard
     * {@code *} (matches any existing representation), or a comma-separated list of entity-tags, each compared with
     * <em>strong</em> comparison (2.3.2). Per RFC, the {@code W/} (weak match) prefix disqualifies an If-Match match.
     */
    private static boolean ifMatchMatches(String ifMatch, String ourETag) {
        if (ifMatch.trim().equals("*")) {
            return true;
        }
        for (String entry : ifMatch.split(",")) {
            final String candidate = entry.trim();
            // Weak tags on If-Match are nonsensical; either side having a W/ prefix disqualifies a match.
            if (!candidate.startsWith("W/") && ourETag.equals(candidate)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Evaluates an {@code If-None-Match} header value against our ETag per RFC 7232 3.2. The header may be the
     * wildcard {@code *} (always a match for an existing resource), or a comma-separated list of entity-tags, each
     * compared with weak comparison (2.3.2) - i.e. the optional {@code W/} prefix on either side is ignored.
     * <p>
     * This method returns {@code true} when a tag matches (meaning the caller should 304), whereas the RFC phrases the
     * precondition as "true if none match, send 304 when false."
     */
    private static boolean ifNoneMatchMatches(String ifNoneMatch, String ourETag) {
        if (ifNoneMatch.trim().equals("*")) {
            return true;
        }
        final String ourOpaque = stripWeakPrefix(ourETag);
        for (String entry : ifNoneMatch.split(",")) {
            if (ourOpaque.equals(stripWeakPrefix(entry.trim()))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Strips an optional {@code W/} prefix, exposing the opaque-tag for weak comparison per RFC 7232 2.3.2.
     */
    private static String stripWeakPrefix(String eTag) {
        return eTag.startsWith("W/") ? eTag.substring(2) : eTag;
    }

    /**
     * Evaluates an {@code If-Range} precondition per RFC 7233 3.2. The header's value may be either a strong
     * entity-tag or an HTTP-date; weak entity-tags ({@code W/"..."}) never match. A match means the client's cached
     * copy is current, so the server may send a 206 partial response; a non-match means the server should fall back to
     * a full 200 response.
     */
    private boolean ifRangeMatches(HttpServletRequest req, CachedAsset cachedAsset) {
        final String ifRange = req.getHeader(IF_RANGE);
        // Strong ETags start with a quote; weak ETags start with W/ and must never match.
        if (ifRange.startsWith("\"")) {
            return cachedAsset.getETag().equals(ifRange);
        }
        // Otherwise interpret as HTTP-date, matched only against an exact Last-Modified.
        final long ifRangeDate = parseDateHeader(req, IF_RANGE);
        return ifRangeDate != -1 && ifRangeDate == cachedAsset.getLastModifiedTime();
    }

    /**
     * Parses a date-valued request header, treating any unparseable value as "not present" (returns {@code -1}).
     * Servlet containers throw {@link IllegalArgumentException} when {@code getDateHeader} encounters a malformed
     * value; per the RFC, we need to ignore such unparseable headers rather than rejecting the request.
     */
    private static long parseDateHeader(HttpServletRequest req, String header) {
        try {
            return req.getDateHeader(header);
        } catch (IllegalArgumentException e) {
            return -1;
        }
    }

    /**
     * Generates a {@code multipart/byteranges} boundary token: 16 hex characters drawn from {@link ThreadLocalRandom}.
     * Not cryptographically strong, but the content body separator is a loose guarantee. This aligns with Jetty
     * behavior.
     */
    private static String generateBoundary() {
        return HexFormat.of().toHexDigits(ThreadLocalRandom.current().nextLong());
    }

    /**
     * Writes an RFC 7233 4.1 {@code multipart/byteranges} body for the given ranges. Each part has its own
     * {@code Content-Type} (echoing the asset's media type) and {@code Content-Range} header, followed by the raw byte
     * slice and closing boundary.
     */
    private static void writeMultipartBody(ServletOutputStream output,
                                           byte[] asset,
                                           List<ByteRange> ranges,
                                           String mediaType,
                                           String boundary,
                                           long totalLength) throws IOException {
        for (ByteRange range : ranges) {
            final String partHeader = "\r\n--" + boundary + "\r\n"
                    + "Content-Type: " + mediaType + "\r\n"
                    + "Content-Range: " + range.toHeaderValue(totalLength) + "\r\n"
                    + "\r\n";
            output.write(partHeader.getBytes(StandardCharsets.US_ASCII));
            output.write(asset, (int) range.first(), (int) range.getLength());
        }
        output.write(("\r\n--" + boundary + "--\r\n").getBytes(StandardCharsets.US_ASCII));
    }
}
