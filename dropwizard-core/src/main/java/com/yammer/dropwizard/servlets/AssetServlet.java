package com.yammer.dropwizard.servlets;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheBuilderSpec;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.hash.Hashing;
import com.google.common.io.Resources;
import com.google.common.net.HttpHeaders;
import com.google.common.net.MediaType;
import com.yammer.dropwizard.util.ResourceURL;

import javax.activation.FileTypeMap;
import javax.activation.MimetypesFileTypeMap;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;

public class AssetServlet extends HttpServlet {
    private static final long serialVersionUID = 6393345594784987908L;

    private static class AssetLoader extends CacheLoader<String, CachedAsset> {
        private final String resourcePath;
        private final String uriPath;
        private final String indexFilename;

        private AssetLoader(String resourcePath, String uriPath, String indexFilename) {
            this.resourcePath = resourcePath.substring( 1 ) + "/";
            this.uriPath = uriPath.endsWith("/") ? uriPath.substring(0, uriPath.length() - 1) : uriPath;
            this.indexFilename = indexFilename;
        }

        @Override
        public CachedAsset load(String key) throws Exception {
            Preconditions.checkArgument(key.startsWith(uriPath));
            
            final String requestedResourcePath = key.substring(uriPath.length() + 1);
            
            final String absoluteRequestedResourcePath = this.resourcePath  + requestedResourcePath;
            URL requestedResourceURL =Resources.getResource(absoluteRequestedResourcePath);

            if (ResourceURL.isDirectory(requestedResourceURL)) {
                if (indexFilename != null) {
                    requestedResourceURL =Resources.getResource(absoluteRequestedResourcePath + "/" + indexFilename);
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

    private static final MediaType DEFAULT_MEDIA_TYPE = MediaType.HTML_UTF_8;
    private static final String DEFAULT_INDEX_FILE = "index.htm";

    private final String resourcePath;
    private final CacheBuilderSpec cacheBuilderSpec;
    private final String uriPath;
    private final String indexFile;

    private final transient LoadingCache<String, CachedAsset> cache;
    private final transient FileTypeMap mimeTypes;
    private Charset defaultCharset = Charsets.UTF_8;

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
    public AssetServlet(String resourcePath,
                        CacheBuilderSpec cacheBuilderSpec,
                        String uriPath,
                        String indexFile) {
        this.resourcePath = resourcePath.substring(1);
        this.cacheBuilderSpec = cacheBuilderSpec;
        this.uriPath = uriPath;
        this.indexFile = indexFile;
        this.cache = CacheBuilder.from(cacheBuilderSpec)
                                 .build(new AssetLoader(resourcePath, uriPath, indexFile));
        this.mimeTypes = MimetypesFileTypeMap.getDefaultFileTypeMap();
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
        this(resourcePath, cacheBuilderSpec, uriPath, DEFAULT_INDEX_FILE);
    }

    public URL getResourceURL() {
        return Resources.getResource( resourcePath );
    }

    public CacheBuilderSpec getCacheBuilderSpec() {
        return cacheBuilderSpec;
    }

    public String getUriPath() {
        return uriPath;
    }
    
    public void setDefaultCharset(Charset defaultCharset)
    {
        this.defaultCharset = defaultCharset;
    }
    
    public Charset getDefaultCharset()
    {
        return this.defaultCharset;
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

            final String contentTypeOfFile = mimeTypes.getContentType(req
                .getRequestURI());
            MediaType mediaType = DEFAULT_MEDIA_TYPE;
            
            // FileTypeMap will map unknown types to octet-stream, ignore
            if (contentTypeOfFile != null && !"application/octet-stream".equals(contentTypeOfFile)) {
                try {
                    mediaType = MediaType.parse(contentTypeOfFile);
                    if (defaultCharset != null) {
                        mediaType = mediaType.withCharset(defaultCharset);
                    }
                }catch (IllegalArgumentException ignore) {}
            }
            
            resp.setContentType(mediaType.type() + "/" + mediaType.subtype());

            if (mediaType.is(MediaType.ANY_TEXT_TYPE) && mediaType.charset().isPresent()) {
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
        }
    }

    private boolean isCachedClientSide(HttpServletRequest req, CachedAsset cachedAsset) {
        return cachedAsset.getETag().equals(req.getHeader(HttpHeaders.IF_NONE_MATCH)) ||
                (req.getDateHeader(HttpHeaders.IF_MODIFIED_SINCE) >= cachedAsset.getLastModifiedTime());
    }
}
