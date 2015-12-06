package io.dropwizard.jetty;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Iterables;
import io.dropwizard.util.Size;
import org.eclipse.jetty.server.Handler;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
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
 *         <td>If true, all requests with `gzip` or `deflate` in the `Accept-Encoding` header will have their
 *             response entities compressed and requests with `gzip` or `deflate` in the `Content-Encoding`
 *             header will have their request entities decompressed.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code minimumEntitySize}</td>
 *         <td>256 bytes</td>
 *         <td>All response entities under this size are not compressed.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code bufferSize}</td>
 *         <td>8KiB</td>
 *         <td>The size of the buffer to use when compressing.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code excludedUserAgentPatterns}</td>
 *         <td>(none)</td>
 *         <td>The set of user agent patterns to exclude from compression. </td>
 *     </tr>
 *     <tr>
 *         <td>{@code compressedMimeTypes}</td>
 *         <td>(Jetty's default)</td>
 *         <td>The list of mime types to compress. The default is all types apart the
 *         commonly known image, video, audio and compressed types.</td>
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
 *         <td>The compression level used for ZLIB deflation(compression).</td>
 *     </tr>
 *     <tr>
 *         <td>{@code gzipCompatibleInflation}</td>
 *         <td>true</td>
 *         <td>If true, then ZLIB inflation(decompression) will be performed in the GZIP-compatible mode.</td>
 *     </tr>
 * </table>
 */
public class GzipHandlerFactory {

    private boolean enabled = true;

    @NotNull
    private Size minimumEntitySize = Size.bytes(256);

    @NotNull
    private Size bufferSize = Size.kilobytes(8);

    // By default compress responses for all user-agents
    private Set<String> excludedUserAgentPatterns = new HashSet<>();
    private Set<String> compressedMimeTypes;
    private Set<String> includedMethods;

    @Min(Deflater.DEFAULT_COMPRESSION)
    @Max(Deflater.BEST_COMPRESSION)
    private int deflateCompressionLevel = Deflater.DEFAULT_COMPRESSION;

    private boolean gzipCompatibleInflation = true;

    @JsonProperty
    public boolean isEnabled() {
        return enabled;
    }

    @JsonProperty
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @JsonProperty
    public Size getMinimumEntitySize() {
        return minimumEntitySize;
    }

    @JsonProperty
    public void setMinimumEntitySize(Size size) {
        this.minimumEntitySize = requireNonNull(size);
    }

    @JsonProperty
    public Size getBufferSize() {
        return bufferSize;
    }

    @JsonProperty
    public void setBufferSize(Size size) {
        this.bufferSize = requireNonNull(size);
    }

    @JsonProperty
    public Set<String> getCompressedMimeTypes() {
        return compressedMimeTypes;
    }

    @JsonProperty
    public void setCompressedMimeTypes(Set<String> mimeTypes) {
        this.compressedMimeTypes = mimeTypes;
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
    public boolean isGzipCompatibleInflation() {
        return gzipCompatibleInflation;
    }

    @JsonProperty
    public void setGzipCompatibleInflation(boolean gzipCompatibleInflation) {
        this.gzipCompatibleInflation = gzipCompatibleInflation;
    }

    public Set<String> getExcludedUserAgentPatterns() {
        return excludedUserAgentPatterns;
    }

    public void setExcludedUserAgentPatterns(Set<String> excludedUserAgentPatterns) {
        this.excludedUserAgentPatterns = excludedUserAgentPatterns;
    }

    @JsonProperty
    public Set<String> getIncludedMethods() {
        return includedMethods;
    }

    @JsonProperty
    public void setIncludedMethods(Set<String> methods) {
        this.includedMethods = methods;
    }

    public BiDiGzipHandler build(Handler handler) {
        final BiDiGzipHandler gzipHandler = new BiDiGzipHandler();
        gzipHandler.setHandler(handler);
        gzipHandler.setMinGzipSize((int) minimumEntitySize.toBytes());
        gzipHandler.setInputBufferSize((int) bufferSize.toBytes());
        gzipHandler.setCompressionLevel(deflateCompressionLevel);

        if (compressedMimeTypes != null) {
            gzipHandler.setIncludedMimeTypes(Iterables.toArray(compressedMimeTypes, String.class));
        }

        if (includedMethods != null) {
            gzipHandler.setIncludedMethods(Iterables.toArray(includedMethods, String.class));
        }

        gzipHandler.setExcludedAgentPatterns(Iterables.toArray(excludedUserAgentPatterns, String.class));
        gzipHandler.setInflateNoWrap(gzipCompatibleInflation);

        return gzipHandler;
    }
}
