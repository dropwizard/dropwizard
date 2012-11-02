package com.yammer.dropwizard.servlets;

import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheBuilderSpec;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.hash.Hashing;
import com.google.common.io.Resources;
import com.google.common.net.HttpHeaders;
import com.yammer.dropwizard.util.ResourceURL;
import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.io.Buffer;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;

public class AssetServlet extends HttpServlet {
    private static final long serialVersionUID = 6393345594784987908L;

    private static class AssetLoader extends CacheLoader<String, CachedAsset> {
        private final URL resourceURL;
        private final String uriPath;
        private final String indexFilename;

        private AssetLoader(URL resourceURL, String uriPath, String indexFilename) {
            this.resourceURL = ResourceURL.appendTrailingSlash(resourceURL);
            this.uriPath = uriPath.endsWith("/") ? uriPath.substring(0, uriPath.length() - 1) : uriPath;
            this.indexFilename = indexFilename;
        }

        @Override
        public CachedAsset load(String key) throws Exception {
            Preconditions.checkArgument(key.startsWith(uriPath));
            final String requestedResourcePath = key.substring(uriPath.length() + 1);
            URL requestedResourceURL = ResourceURL.resolveRelativeURL(this.resourceURL, requestedResourcePath);

            if (ResourceURL.isDirectory(requestedResourceURL)) {
                if (indexFilename != null) {
                    requestedResourceURL = ResourceURL.resolveRelativeURL(
                            ResourceURL.appendTrailingSlash(requestedResourceURL), indexFilename);
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
    }

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

    private static final String DEFAULT_MIME_TYPE = "text/html";
    private static final String DEFAULT_INDEX_FILE = "index.htm";

    private final URL resourceURL;
    private final CacheBuilderSpec cacheBuilderSpec;
    private final String uriPath;
    private final String indexFile;

    private final transient LoadingCache<String, CachedAsset> cache;
    private final transient MimeTypes mimeTypes;


    /**
     * Creates a new {@code AssetServlet} that serves static assets loaded from {@code resourceURL} (typically a file:
     * or jar: URL). The assets are served at URIs rooted at {@code uriPath}. For example, given a {@code resourceURL}
     * of {@code "file:/data/assets"} and a {@code uriPath} of {@code "/js"}, an {@code AssetServlet} would serve the
     * contents of {@code /data/assets/example.js} in response to a request for {@code /js/example.js}. If a directory
     * is requested and {@code indexFile} is defined, then {@code AssetServlet} will attempt to serve a file with that
     * name in that directory. If a directory is requested and {@code indexFile} is null, it will serve a 404.
     *
     * @param resourceURL      the base URL from which assets are loaded
     * @param cacheBuilderSpec specification for the underlying cache
     * @param uriPath          the URI path fragment in which all requests are rooted
     * @param indexFile        the filename to use when directories are requested, or null to serve no indexes
     * @see CacheBuilderSpec
     */
    public AssetServlet(URL resourceURL,
                        CacheBuilderSpec cacheBuilderSpec,
                        String uriPath,
                        String indexFile) {
        this.resourceURL = resourceURL;
        this.cacheBuilderSpec = cacheBuilderSpec;
        this.uriPath = uriPath;
        this.indexFile = indexFile;
        this.cache = CacheBuilder.from(cacheBuilderSpec)
                                 .build(new AssetLoader(resourceURL, uriPath, indexFile));
        this.mimeTypes = new MimeTypes();
    }

    /**
     * Creates a new {@code AssetServlet}. This is provided for backwards-compatibility; see
     * {@link AssetServlet#AssetServlet(URL, CacheBuilderSpec, String, String)} for details.
     *
     * @param resourcePath     the path of the directory in which assets are stored, starting with '/'
     * @param cacheBuilderSpec specification for the underlying cache
     * @param uriPath          the URI path fragment in which all requests are rooted
     */
    @SuppressWarnings("UnusedDeclaration")
    public AssetServlet(String resourcePath, CacheBuilderSpec cacheBuilderSpec, String uriPath) {
        this(Resources.getResource(resourcePath.substring(1)), cacheBuilderSpec, uriPath, DEFAULT_INDEX_FILE);
    }

    public URL getResourceURL() {
        return resourceURL;
    }

    public CacheBuilderSpec getCacheBuilderSpec() {
        return cacheBuilderSpec;
    }

    public String getUriPath() {
        return uriPath;
    }

    public String getIndexFile() {
        return indexFile;
    }

    @Override
    protected void doGet(HttpServletRequest req,
                         HttpServletResponse resp) throws ServletException, IOException {
        try {
            final CachedAsset cachedAsset = cache.getUnchecked(req.getRequestURI());
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

            final Buffer mimeType = mimeTypes.getMimeByExtension(req.getRequestURI());
            if (mimeType == null) {
                resp.setContentType(DEFAULT_MIME_TYPE);
            } else {
                resp.setContentType(mimeType.toString());
            }

            final ServletOutputStream output = resp.getOutputStream();
            try {
                output.write(cachedAsset.getResource());
            } finally {
                output.close();
            }
        } catch (RuntimeException ignored) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private boolean isCachedClientSide(HttpServletRequest req, CachedAsset cachedAsset) {
        return cachedAsset.getETag().equals(req.getHeader(HttpHeaders.IF_NONE_MATCH)) ||
                (req.getDateHeader(HttpHeaders.IF_MODIFIED_SINCE) >= cachedAsset.getLastModifiedTime());
    }
}
