package com.yammer.dropwizard.servlets;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.hash.Hashing;
import com.google.common.io.Resources;
import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.io.Buffer;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class AssetServlet extends HttpServlet {
    private static final long serialVersionUID = 6393345594784987908L;

    private static class AssetLoader extends CacheLoader<String, CachedAsset> {
        private static final String INDEX_FILENAME = "index.htm"; // TODO: Make this configurable.

        private final String resourcePath;
        private final String uriPath;

        private AssetLoader(String resourcePath, String uriPath) {
            this.resourcePath = resourcePath;
            this.uriPath = uriPath;
        }

        @Override
        public CachedAsset load(String key) throws Exception {
            final String resource = key.substring(uriPath.length());
            String fullResourcePath = this.resourcePath + resource;
            if (key.equals(this.uriPath)) {
                fullResourcePath = resourcePath + INDEX_FILENAME;
            }

            URL resourceURL = Resources.getResource(fullResourcePath.substring(1));

            CachedAsset cachedAsset = new CachedAsset(Resources.toByteArray(resourceURL));

            return cachedAsset;
        }
    }

    private static class CachedAsset {

        private final byte[] resource;

        private final String etag;

        private final Date lastModified;

        public CachedAsset(byte[] resource) {
            this.resource = resource.clone();

            this.etag = Hashing.murmur3_128().hashBytes(resource).toString();

            // lazy and non-aggressive lastModified impl
            // zero out the millis since the date we get back from If-Modified-Since will not have them
            Calendar now = Calendar.getInstance();
            now.set(Calendar.MILLISECOND, 0);
            this.lastModified = now.getTime();
        }

        public byte[] getResource() {
            return resource.clone();
        }

        public String getEtag() {
            return etag;
        }

        public Date getLastModified() {
            return new Date(lastModified.getTime());
        }

    }

    private static final String DEFAULT_MIME_TYPE = "text/html";

    private final transient LoadingCache<String, CachedAsset> cache;
    private final transient MimeTypes mimeTypes;

    public AssetServlet(String resourcePath, int maxCacheSize, String uriPath) {
        this.cache = buildCache(resourcePath, maxCacheSize, uriPath);
        this.mimeTypes = new MimeTypes();
    }

    private static LoadingCache<String, CachedAsset> buildCache(String resourcePath, int maxCacheSize, String uriPath) {
        return CacheBuilder.newBuilder()
                           .maximumSize(maxCacheSize)
                           .build(new AssetLoader(resourcePath, uriPath));
    }


    @Override
    protected void doGet(HttpServletRequest req,
                         HttpServletResponse resp) throws ServletException, IOException {
        try {

            CachedAsset cachedAsset = cache.getUnchecked(req.getRequestURI());

            if ((req.getHeader("If-None-Match") != null) && (req.getHeader("If-None-Match").equals(cachedAsset.getEtag()))) {
                resp.sendError(HttpServletResponse.SC_NOT_MODIFIED);
                return;
            }

            // http://tools.ietf.org/html/rfc2822
            // Tue, 15 Nov 1994 12:45:26 GMT
            final SimpleDateFormat httpDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
            httpDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

            if (req.getHeader("If-Modified-Since") != null) {
                try {
                    final Date modifiedSince = httpDateFormat.parse(req.getHeader("If-Modified-Since"));
                    if (cachedAsset.getLastModified().getTime() <= modifiedSince.getTime()) {
                        resp.sendError(HttpServletResponse.SC_NOT_MODIFIED);
                        return;
                    }
                }
                catch (ParseException e) {
                    // ignored
                }
            }

            resp.setHeader("Last-Modified", httpDateFormat.format(cachedAsset.getLastModified()));
            resp.setHeader("ETag", cachedAsset.getEtag());

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
}