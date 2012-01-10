package com.yammer.dropwizard.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.io.Buffer;
import org.eclipse.jetty.util.URIUtil;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.io.Resources;

public class AssetServlet extends HttpServlet {
    private static final long serialVersionUID = 6393345594784987908L;

    private static class AssetLoader extends CacheLoader<String, byte[]> {
        private final String sourcePath;
        private final String urlPath;

        private AssetLoader(String sourcePath, String urlPath) {
            this.sourcePath = sourcePath;
            this.urlPath = urlPath;
        }

        @Override
        public byte[] load(String key) throws Exception {
            final String path = URIUtil.canonicalPath(key);
            final String resource = path.substring(urlPath.length());
            String resourcePath = sourcePath + resource;
            if (path.equals(this.urlPath)) {
                resourcePath = sourcePath + "index.htm";
            }
            return Resources.toByteArray(Resources.getResource(resourcePath.substring(1)));
        }
    }

    private final transient LoadingCache<String, byte[]> cache;
    private final transient MimeTypes mimeTypes;

    public AssetServlet(String sourcePath, String urlPath, int maxCacheSize) {
        this.cache = buildCache(sourcePath, urlPath, maxCacheSize);
        this.mimeTypes = new MimeTypes();
    }

    private static LoadingCache<String, byte[]> buildCache(String sourcePath, String urlPath, int maxCacheSize) {
        return CacheBuilder.newBuilder().maximumSize(maxCacheSize).build(new AssetLoader(sourcePath, urlPath));
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            final byte[] resource = cache.getUnchecked(req.getRequestURI());
            Buffer mimeType = mimeTypes.getMimeByExtension(req.getRequestURI());
            // @coda : Caused NPE, when mimeType didn't exist.
            if (mimeType == null) {
                resp.setContentType("text/html");
            } else {
                resp.setContentType(mimeType.toString());
            }
            final ServletOutputStream output = resp.getOutputStream();
            try {
                output.write(resource);
            } finally {
                output.close();
            }
        } catch (RuntimeException ignored) {
            // @coda: should we log the error?
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
}
