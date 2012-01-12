package com.yammer.dropwizard.bundles;

import com.yammer.dropwizard.Bundle;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.servlets.AssetServlet;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * A bundle for serving static asset files from the classpath.
 */
public class AssetsBundle implements Bundle {
    public static final String DEFAULT_PATH = "/assets";
    public static final int DEFAULT_MAX_CACHE_SIZE = 100;

    private final String sourcePath;
    private final String urlPath;
    private final int maxCacheSize;

    /**
     * Creates a new {@link AssetsBundle} which serves up static assets from
     * {@code src/main/resources/assets/*} as {@code /assets/*}.
     * 
     * @see AssetsBundle#AssetsBundle(String, int)
     */
    public AssetsBundle() {
        this(DEFAULT_PATH, DEFAULT_MAX_CACHE_SIZE, DEFAULT_PATH);
    }

    /**
     * Creates a new {@link AssetsBundle} which will configure the service to
     * serve the static files located in {@code src/main/resources/$ path}} as
     * {@code /$ path}}. For example, given a {@code path} of {@code "/assets"}
     * , {@code src/main/resources/assets/example.js} would be served up from
     * {@code /assets/example.js}.
     * 
     * @param path
     *            the classpath and URI root of the static asset files
     * @see AssetsBundle#AssetsBundle(String, int)
     */
    public AssetsBundle(String path) {
        this(path, DEFAULT_MAX_CACHE_SIZE, path);
    }

    /**
     * Creates a new {@link AssetsBundle} which will configure the service to
     * serve the static files located in {@code src/main/resources/$ path}} as
     * {@code /$ urlPath}}. For example, given a {@code path} of
     * {@code "/assets"} and {@code urlPath of /ui},
     * {@code src/main/resources/assets/example.js} would be served up from
     * {@code /ui/example.js}.
     * 
     * @param path
     *            the classpath and URI root of the static asset files
     * @param maxCacheSize
     *            the maximum number of resources to cache
     * @param urlPath
     *            the url pattern to use for assets in this bundle
     */
    public AssetsBundle(String path, int maxCacheSize, String urlPath) {
        checkArgument(path.startsWith("/"), "%s is not an absolute path", path);
        checkArgument(!"/".equals(path), "%s is the classpath root");
        this.sourcePath = path.endsWith("/") ? path : (path + '/');
        this.urlPath = urlPath.endsWith("/") ? urlPath : (urlPath + '/');
        this.maxCacheSize = maxCacheSize;
    }

    /**
     * Creates a new {@link AssetsBundle} which will configure the service to
     * serve the static files located in {@code src/main/resources/$ path}} as
     * {@code /$ urlPath}}. For example, given a {@code path} of
     * {@code "/assets"} and {@code urlPath of /ui},
     * {@code src/main/resources/assets/example.js} would be served up from
     * {@code /ui/example.js}.
     * 
     * @param path
     *            the classpath and URI root of the static asset files
     * @param urlPath
     *            the url pattern to use for assets in this bundle
     */
    public AssetsBundle(String path, String urlPath) {
        this(path, DEFAULT_MAX_CACHE_SIZE, urlPath);
    }

    @Override
    public void initialize(Environment environment) {
        environment.addServlet(new AssetServlet(sourcePath, urlPath, maxCacheSize), urlPath + '*');
    }
}
