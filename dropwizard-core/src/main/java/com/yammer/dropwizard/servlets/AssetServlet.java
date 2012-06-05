package com.yammer.dropwizard.servlets;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheBuilderSpec;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.hash.Hashing;
import com.google.common.io.Resources;
import com.google.common.net.HttpHeaders;
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
        private final String resourcePath;
        private final String uriPath;
        private final String indexFilename;

        private AssetLoader(String resourcePath, String uriPath, String indexFilename) {
            this.resourcePath = resourcePath;
            this.uriPath = uriPath;
            this.indexFilename = indexFilename;
        }

        @Override
        public CachedAsset load(String key) throws Exception {
            final String resource = key.substring(uriPath.length());
            String fullResourcePath = this.resourcePath + resource;
            if (key.equals(this.uriPath)) {
                fullResourcePath = resourcePath + indexFilename;
            }
            final URL resourceURL = Resources.getResource(fullResourcePath.substring(1));
            return new CachedAsset(Resources.toByteArray(resourceURL));
        }
    }

    private static class CachedAsset {
        private final byte[] resource;
        private final String eTag;
        private final long lastModifiedTime;

        private CachedAsset(byte[] resource) {
            this.resource = resource;
            this.eTag = Hashing.murmur3_128().hashBytes(resource).toString();
            this.lastModifiedTime = roundedTimestamp();
        }

        private long roundedTimestamp() {
            // zero out the millis since the date we get back from If-Modified-Since will not have them
            return (System.currentTimeMillis() / 1000) * 1000;
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

    private final transient LoadingCache<String, CachedAsset> cache;
    private final transient MimeTypes mimeTypes;

    public AssetServlet(String resourcePath, CacheBuilderSpec cacheBuilderSpec, String uriPath) {
        // TODO: 3/20/12 <coda> -- make the default filename here configurable
        this.cache = CacheBuilder.from(cacheBuilderSpec)
                                 .build(new AssetLoader(resourcePath, uriPath, "index.htm"));
        this.mimeTypes = new MimeTypes();
    }

    @Override
    protected void doGet(HttpServletRequest req,
                         HttpServletResponse resp) throws ServletException, IOException {
        try {

            final CachedAsset cachedAsset = cache.getUnchecked(req.getRequestURI());

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
