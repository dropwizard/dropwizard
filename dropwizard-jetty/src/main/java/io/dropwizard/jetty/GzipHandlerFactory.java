package io.dropwizard.jetty;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.util.DataSize;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.Nullable;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.handler.gzip.GzipHandler;
import org.eclipse.jetty.util.compression.CompressionPool;
import org.eclipse.jetty.util.compression.DeflaterPool;

import java.util.Set;
import java.util.zip.Deflater;

import static java.util.Objects.requireNonNull;

/**
 * Builds GZIP filters.
 *
 * <p/>
 * <b>Configuration Parameters:</b>
 * <table>
 *     <tr>
 *         <td>Name</td>
 *         <td>Default</td>
 *         <td>Description</td>
 *     </tr>
 *     <tr>
 *         <td>{@code enabled}</td>
 *         <td>true</td>
 *         <td>If true, all requests with `gzip` in the `Accept-Encoding` header will have their
 *             response entities compressed and requests with `gzip` in the `Content-Encoding`
 *             header will have their request entities decompressed.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code minimumEntitySize}</td>
 *         <td>256 bytes</td>
 *         <td>All response entities under this DataSize are not compressed.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code bufferSize}</td>
 *         <td>8KiB</td>
 *         <td>The DataSize of the buffer to use when compressing.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code compressedMimeTypes}</td>
 *         <td>(Jetty's default)</td>
 *         <td>List of MIME types to compress. The default is all types apart the
 *         commonly known image, video, audio and compressed types.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code excludedMimeTypes}</td>
 *         <td>(Jetty's default)</td>
 *         <td>List of MIME types not to compress. The default is a list of commonly known image, video, audio and
 *             compressed types.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code includedPaths}</td>
 *         <td>(Jetty's default)</td>
 *         <td>List of paths to consider for compression. The default is all paths.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code excludedPaths}</td>
 *         <td>(none)</td>
 *         <td>List of paths to exclude from compression. Performs a {@code String.startsWith(String)} comparison to
 *             check if the path matches. If it does match then there is no compression. To match subpaths use
 *             excludePathPatterns instead.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code includedMethods}</td>
 *         <td>(Jetty's default)</td>
 *         <td>The list list of HTTP methods to compress. The default is to compress
 *         only GET responses.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code deflateCompressionLevel}</td>
 *         <td>-1</td>
 *         <td>The compression level used for deflation(compression).</td>
 *     </tr>
 *     <tr>
 *         <td>{@code gzipCompatibleInflation}</td>
 *         <td>true</td>
 *         <td>This option is unused and deprecated as compressed requests without header info are unsupported</td>
 *     </tr>
 * </table>
 */
public class GzipHandlerFactory {

    private boolean enabled = true;

    @NotNull
    private DataSize minimumEntitySize = DataSize.bytes(256);

    @NotNull
    private DataSize bufferSize = DataSize.kibibytes(8);

    @Nullable
    private Set<String> compressedMimeTypes;

    @Nullable
    private Set<String> excludedMimeTypes;

    @Nullable
    private Set<String> includedMethods;

    @Nullable
    private Set<String> excludedPaths;

    @Nullable
    private Set<String> includedPaths;

    @Min(Deflater.DEFAULT_COMPRESSION)
    @Max(Deflater.BEST_COMPRESSION)
    private int deflateCompressionLevel = Deflater.DEFAULT_COMPRESSION;

    private boolean syncFlush = false;

    @JsonProperty
    public boolean isEnabled() {
        return enabled;
    }

    @JsonProperty
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @JsonProperty
    public DataSize getMinimumEntitySize() {
        return minimumEntitySize;
    }

    @JsonProperty
    public void setMinimumEntitySize(DataSize size) {
        this.minimumEntitySize = requireNonNull(size);
    }

    @JsonProperty
    public DataSize getBufferSize() {
        return bufferSize;
    }

    @JsonProperty
    public void setBufferSize(DataSize size) {
        this.bufferSize = requireNonNull(size);
    }

    @JsonProperty
    @Nullable
    public Set<String> getCompressedMimeTypes() {
        return compressedMimeTypes;
    }

    @JsonProperty
    public void setCompressedMimeTypes(Set<String> mimeTypes) {
        this.compressedMimeTypes = mimeTypes;
    }

    /**
     * @since 2.0
     */
    @JsonProperty
    @Nullable
    public Set<String> getExcludedMimeTypes() {
        return excludedMimeTypes;
    }

    /**
     * @since 2.0
     */
    @JsonProperty
    public void setExcludedMimeTypes(Set<String> mimeTypes) {
        this.excludedMimeTypes = mimeTypes;
    }

    @JsonProperty
    public int getDeflateCompressionLevel() {
        return deflateCompressionLevel;
    }

    @JsonProperty
    public void setDeflateCompressionLevel(int level) {
        this.deflateCompressionLevel = level;
    }

    @JsonProperty
    @Nullable
    public Set<String> getIncludedMethods() {
        return includedMethods;
    }

    @JsonProperty
    public void setIncludedMethods(Set<String> methods) {
        this.includedMethods = methods;
    }

    /**
     * @since 2.0
     */
    @JsonProperty
    @Nullable
    public Set<String> getExcludedPaths() {
        return excludedPaths;
    }

    /**
     * @since 2.0
     */
    @JsonProperty
    public void setExcludedPaths(Set<String> paths) {
        this.excludedPaths = paths;
    }

    /**
     * @since 2.0
     */
    @JsonProperty
    @Nullable
    public Set<String> getIncludedPaths() {
        return includedPaths;
    }

    /**
     * @since 2.0
     */
    @JsonProperty
    public void setIncludedPaths(Set<String> paths) {
        this.includedPaths = paths;
    }

    @JsonProperty
    public boolean isSyncFlush() {
        return syncFlush;
    }

    @JsonProperty
    public void setSyncFlush(boolean syncFlush) {
        this.syncFlush = syncFlush;
    }

    public GzipHandler build(@Nullable Handler handler) {
        final ZipExceptionHandlingGzipHandler gzipHandler = new ZipExceptionHandlingGzipHandler();
        gzipHandler.setHandler(handler);
        gzipHandler.setMinGzipSize((int) minimumEntitySize.toBytes());
        gzipHandler.setInflateBufferSize((int) bufferSize.toBytes());
        gzipHandler.setDeflaterPool(new DeflaterPool(CompressionPool.DEFAULT_CAPACITY, deflateCompressionLevel, true));
        gzipHandler.setSyncFlush(syncFlush);

        if (compressedMimeTypes != null) {
            gzipHandler.setIncludedMimeTypes(compressedMimeTypes.toArray(new String[0]));
        }

        if (excludedMimeTypes != null) {
            gzipHandler.setExcludedMimeTypes(excludedMimeTypes.toArray(new String[0]));
        }

        if (includedMethods != null) {
            gzipHandler.setIncludedMethods(includedMethods.toArray(new String[0]));
        }

        if (excludedPaths != null) {
            gzipHandler.setExcludedPaths(excludedPaths.toArray(new String[0]));
        }

        if (includedPaths != null) {
            gzipHandler.setIncludedPaths(includedPaths.toArray(new String[0]));
        }

        return gzipHandler;
    }
}
