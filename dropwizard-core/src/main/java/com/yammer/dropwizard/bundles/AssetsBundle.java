package com.yammer.dropwizard.bundles;

import com.google.common.cache.CacheBuilderSpec;
import com.yammer.dropwizard.Bundle;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.servlets.AssetServlet;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * A bundle for serving static asset files from the classpath.
 */
public class AssetsBundle extends Bundle {
    public static final String DEFAULT_PATH = "/assets";
    public static final CacheBuilderSpec DEFAULT_CACHE_SPEC = CacheBuilderSpec.parse("maximumSize=100");
    
    private final String resourcePath;
    private final String uriPath;
    private final CacheBuilderSpec cacheBuilderSpec;

    /**
     * Creates a new {@link AssetsBundle} which serves up static assets from
     * {@code src/main/resources/assets/*} as {@code /assets/*}.
     *
     * @see AssetsBundle#AssetsBundle(String, CacheBuilderSpec)
     */
    public AssetsBundle() {
        this(DEFAULT_PATH, DEFAULT_CACHE_SPEC);
    }

    /**
     * Creates a new {@link AssetsBundle} which will configure the service to serve the static files
     * located in {@code src/main/resources/${path}} as {@code /${path}}. For example, given a
     * {@code path} of {@code "/assets"}, {@code src/main/resources/assets/example.js} would be
     * served up from {@code /assets/example.js}.
     *
     * @param path    the classpath and URI root of the static asset files
     * @see AssetsBundle#AssetsBundle(String, CacheBuilderSpec)
     */
    public AssetsBundle(String path) {
        this(path, DEFAULT_CACHE_SPEC, path);
    }

    /**
     * Creates a new {@link AssetsBundle} which will configure the service to serve the static files
     * located in {@code src/main/resources/${resourcePath}} as {@code /${uriPath}}. For example, given a
     * {@code resourcePath} of {@code "/assets"} and a uriPath of {@code "/js"},
     * {@code src/main/resources/assets/example.js} would be served up from {@code /js/example.js}.
     *
     * @param resourcePath    the resource path (in the classpath) of the static asset files
     * @param uriPath    the uri path for the static asset files
     * @see AssetsBundle#AssetsBundle(String, CacheBuilderSpec)
     */
    public AssetsBundle(String resourcePath, String uriPath) {
        this(resourcePath, DEFAULT_CACHE_SPEC, uriPath);
    }

    /**
     * Creates a new {@link AssetsBundle} which will configure the service to serve the static files
     * located in {@code src/main/resources/${path}} as {@code /${path}}. For example, given a
     * {@code path} of {@code "/assets"}, {@code src/main/resources/assets/example.js} would be
     * served up from {@code /assets/example.js}.
     *
     * @param resourcePath        the resource path (in the classpath) of the static asset files
     * @param cacheBuilderSpec    the spec for the cache builder
     */
    public AssetsBundle(String resourcePath, CacheBuilderSpec cacheBuilderSpec) {
        this(resourcePath, cacheBuilderSpec, resourcePath);
    }

    /**
     * Creates a new {@link AssetsBundle} which will configure the service to serve the static files
     * located in {@code src/main/resources/${resourcePath}} as {@code /${uriPath}}. For example, given a
     * {@code resourcePath} of {@code "/assets"} and a uriPath of {@code "/js"},
     * {@code src/main/resources/assets/example.js} would be served up from {@code /js/example.js}.
     *
     * @param resourcePath        the resource path (in the classpath) of the static asset files
     * @param cacheBuilderSpec    the spec for the cache builder
     * @param uriPath             the uri path for the static asset files
     */
    public AssetsBundle(String resourcePath, CacheBuilderSpec cacheBuilderSpec, String uriPath) {
        checkArgument(resourcePath.startsWith("/"), "%s is not an absolute path", resourcePath);
        checkArgument(!"/".equals(resourcePath), "%s is the classpath root", resourcePath);
        this.resourcePath = resourcePath.endsWith("/") ? resourcePath : (resourcePath + '/');
        this.uriPath = uriPath.endsWith("/") ? uriPath : (uriPath + '/');
        this.cacheBuilderSpec = cacheBuilderSpec;
    }

    @Override
    public void run(Environment environment) {
        environment.addServlet(new AssetServlet(resourcePath, cacheBuilderSpec, uriPath), uriPath + '*');
    }
}
