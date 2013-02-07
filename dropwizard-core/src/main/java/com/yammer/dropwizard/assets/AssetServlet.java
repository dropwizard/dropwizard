package com.yammer.dropwizard.assets;

import com.google.common.base.CharMatcher;
import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilderSpec;
import com.google.common.hash.Hashing;
import com.google.common.io.Resources;
import com.google.common.net.HttpHeaders;
import com.google.common.net.MediaType;

import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.io.Buffer;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;

public class AssetServlet extends HttpServlet {
    private static final long serialVersionUID = 6393345594784987908L;

    private static class CachedAsset {
        private final byte[] resource;
        private final String eTag;
        private final long lastModifiedTime;

        private CachedAsset(byte[] resource, long lastModifiedTime) {
            this.resource = resource;
            this.eTag = Hashing.murmur3_128().hashBytes(resource).toString();
            this.lastModifiedTime = lastModifiedTime;
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

    private static final MediaType DEFAULT_MEDIA_TYPE = MediaType.HTML_UTF_8;

    private final String resourcePath;
    private final String uriPath;
    private final String indexFile;

    private final transient MimeTypes mimeTypes;
    
    private Charset defaultCharset = Charsets.UTF_8;

    /**
     * Creates a new {@code AssetServlet} that serves static assets loaded from {@code resourceURL} (typically a file:
     * or jar: URL). The assets are served at URIs rooted at {@code uriPath}. For example, given a {@code resourceURL}
     * of {@code "file:/data/assets"} and a {@code uriPath} of {@code "/js"}, an {@code AssetServlet} would serve the
     * contents of {@code /data/assets/example.js} in response to a request for {@code /js/example.js}. If a directory
     * is requested and {@code indexFile} is defined, then {@code AssetServlet} will attempt to serve a file with that
     * name in that directory. If a directory is requested and {@code indexFile} is null, it will serve a 404.
     *
     * @param resourcePath      the base URL from which assets are loaded
     * @param uriPath          the URI path fragment in which all requests are rooted
     * @param indexFile        the filename to use when directories are requested, or null to serve no indexes
     * @see CacheBuilderSpec
     */
    public AssetServlet(String resourcePath,
                        String uriPath,
                        String indexFile) {
        final String trimmedPath = CharMatcher.is('/').trimFrom(resourcePath);
	this.resourcePath = trimmedPath.isEmpty() ? trimmedPath : trimmedPath + "/";
        final String trimmedUri = CharMatcher.is('/').trimTrailingFrom(uriPath);
        this.uriPath = trimmedUri.length() == 0 ? "/" : trimmedUri;
        this.indexFile = indexFile;
        this.mimeTypes = new MimeTypes();
    }

    public URL getResourceURL() {
        return Resources.getResource(resourcePath);
    }

    public String getUriPath() {
        return uriPath;
    }
    
    public void setDefaultCharset(Charset defaultCharset) {
        this.defaultCharset = defaultCharset;
    }

    public Charset getDefaultCharset() {
        return this.defaultCharset;
    }

    public String getIndexFile() {
        return indexFile;
    }

    @Override
    protected void doGet(HttpServletRequest req,
                         HttpServletResponse resp) throws ServletException, IOException {
        try {
            final CachedAsset cachedAsset = loadAsset(req.getRequestURI());
            if (cachedAsset == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            if (isCachedClientSide(req, cachedAsset)) {
                resp.sendError(HttpServletResponse.SC_NOT_MODIFIED);
                return;
            }

            resp.setDateHeader(HttpHeaders.LAST_MODIFIED, cachedAsset.getLastModifiedTime());
            resp.setHeader(HttpHeaders.ETAG, cachedAsset.getETag());

            final Buffer mimeTypeOfExtension = mimeTypes.getMimeByExtension(req.getRequestURI());
            MediaType mediaType = DEFAULT_MEDIA_TYPE;
            
            if (mimeTypeOfExtension != null) {
                try {
                    mediaType = MediaType.parse(mimeTypeOfExtension.toString());
                    if (defaultCharset != null && mediaType.is(MediaType.ANY_TEXT_TYPE)) {
                        mediaType = mediaType.withCharset(defaultCharset);
                    }
                }
                catch (IllegalArgumentException ignore) {}
            }
            
            resp.setContentType(mediaType.type() + "/" + mediaType.subtype());

            if (mediaType.charset().isPresent()) {
                resp.setCharacterEncoding(mediaType.charset().get().toString());
            }

            final ServletOutputStream output = resp.getOutputStream();
            try {
                output.write(cachedAsset.getResource());
            } finally {
                output.close();
            }
        } catch (RuntimeException ignored) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } catch (URISyntaxException ignored) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private CachedAsset loadAsset(String key) throws URISyntaxException, IOException {
        Preconditions.checkArgument(key.startsWith(uriPath));
        final String requestedResourcePath = CharMatcher.is('/').trimFrom(key.substring(uriPath.length()));
        final String absoluteRequestedResourcePath = CharMatcher.is('/').trimFrom(
        		this.resourcePath + requestedResourcePath);
        
        URL requestedResourceURL = Resources.getResource(absoluteRequestedResourcePath);

        if (ResourceURL.isDirectory(requestedResourceURL)) {
            if (indexFile != null) {
                requestedResourceURL = Resources.getResource(absoluteRequestedResourcePath + '/' + indexFile);
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
        return new CachedAsset(Resources.toByteArray(requestedResourceURL), lastModified);
    }

    private boolean isCachedClientSide(HttpServletRequest req, CachedAsset cachedAsset) {
        return cachedAsset.getETag().equals(req.getHeader(HttpHeaders.IF_NONE_MATCH)) ||
                (req.getDateHeader(HttpHeaders.IF_MODIFIED_SINCE) >= cachedAsset.getLastModifiedTime());
    }
}
