package com.yammer.dropwizard.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.io.Buffer;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.io.Resources;

public class AssetServlet extends HttpServlet {
    private static final long serialVersionUID = 6393345594784987908L;

    private static class AssetLoader extends CacheLoader<String, byte[]> {
        private final String resourcePath;
        private final String uriPath;
        private final static String INDEX_FILENAME = "index.htm"; // TODO: Make this configurable.
        
        private AssetLoader(String resourcePath, String uriPath) {
            this.resourcePath = resourcePath;
            this.uriPath = uriPath;
        }

        @Override
        public byte[] load(String key) throws Exception {
            final String resource = key.substring(uriPath.length());
            String fullResourcePath = this.resourcePath + resource;
            if (key.equals(this.uriPath)) {
                fullResourcePath = resourcePath + INDEX_FILENAME;
            }
            return Resources.toByteArray(Resources.getResource(fullResourcePath.substring(1)));
        }
    }
    
    private final transient LoadingCache<String, byte[]> cache;
    private final transient MimeTypes mimeTypes;
    private final static String DEFAULT_MIME_TYPE = "text/html";

    public AssetServlet(String resourcePath, int maxCacheSize, String uriPath) {
        this.cache = buildCache(resourcePath, maxCacheSize, uriPath);
        this.mimeTypes = new MimeTypes();
    }

    private static LoadingCache<String, byte[]> buildCache(String resourecePath, int maxCacheSize, String uriPath) {
        return CacheBuilder.newBuilder()
                           .maximumSize(maxCacheSize)
                           .build(new AssetLoader(resourecePath, uriPath));
    }

    @Override
    protected void doGet(HttpServletRequest req,
                         HttpServletResponse resp) throws ServletException, IOException {
        try {
            final byte[] resource = cache.getUnchecked(req.getRequestURI());
            Buffer mimeType = mimeTypes.getMimeByExtension(req.getRequestURI());
            if (mimeType == null) {
                resp.setContentType(DEFAULT_MIME_TYPE);
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
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
}
