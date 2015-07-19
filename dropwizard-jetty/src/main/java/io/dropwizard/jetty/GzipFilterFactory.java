package io.dropwizard.jetty;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Sets;
import io.dropwizard.util.Size;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.zip.Deflater;

import static com.google.common.base.Preconditions.checkNotNull;

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
 *         <td>{@code excludedUserAgents}</td>
 *         <td>(none)</td>
 *         <td>The set of user agents to exclude from compression. </td>
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
 *         <td>{@code gzipCompatibleDeflation}</td>
 *         <td>true</td>
 *         <td>If true, then ZLIB deflation(compression) will be performed in the GZIP-compatible mode.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code gzipCompatibleInflation}</td>
 *         <td>true</td>
 *         <td>If true, then ZLIB inflation(decompression) will be performed in the GZIP-compatible mode.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code vary}</td>
 *         <td>Accept-Encoding</td>
 *         <td>Value of the `Vary` header sent with responses that could be compressed.</td>
 *     </tr>
 * </table>
 */
public class GzipFilterFactory {
    private boolean enabled = true;

    @NotNull
    private Size minimumEntitySize = Size.bytes(256);

    @NotNull
    private Size bufferSize = Size.kilobytes(8);

    private Set<String> excludedUserAgents = Sets.newHashSet();
    private Set<Pattern> excludedUserAgentPatterns = Sets.newHashSet();
    private Set<String> compressedMimeTypes = Sets.newHashSet();
    private Set<String> includedMethods = Sets.newHashSet();
    private boolean gzipCompatibleDeflation = true;
    private boolean gzipCompatibleInflation = true;
    private String vary = "Accept-Encoding";

    @Min(Deflater.DEFAULT_COMPRESSION)
    @Max(Deflater.BEST_COMPRESSION)
    private int deflateCompressionLevel = Deflater.DEFAULT_COMPRESSION;

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
        this.minimumEntitySize = checkNotNull(size);
    }

    @JsonProperty
    public Size getBufferSize() {
        return bufferSize;
    }

    @JsonProperty
    public void setBufferSize(Size size) {
        this.bufferSize = checkNotNull(size);
    }

    @JsonProperty
    public Set<String> getExcludedUserAgents() {
        return excludedUserAgents;
    }

    @JsonProperty
    public void setExcludedUserAgents(Set<String> userAgents) {
        this.excludedUserAgents = userAgents;
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
    public boolean isGzipCompatibleDeflation() {
        return gzipCompatibleDeflation;
    }

    @JsonProperty
    public void setGzipCompatibleDeflation(boolean compatible) {
        this.gzipCompatibleDeflation = compatible;
    }

    @JsonProperty
    public boolean isGzipCompatibleInflation() {
        return gzipCompatibleInflation;
    }

    @JsonProperty
    public void setGzipCompatibleInflation(boolean gzipCompatibleInflation) {
        this.gzipCompatibleInflation = gzipCompatibleInflation;
    }

    @JsonProperty
    public Set<Pattern> getExcludedUserAgentPatterns() {
        return excludedUserAgentPatterns;
    }

    @JsonProperty
    public void setExcludedUserAgentPatterns(Set<Pattern> patterns) {
        this.excludedUserAgentPatterns = patterns;
    }

    @JsonProperty
    public Set<String> getIncludedMethods() {
        return includedMethods;
    }

    @JsonProperty
    public void setIncludedMethods(Set<String> methods) {
        this.includedMethods = methods;
    }

    @JsonProperty
    public String getVary() {
        return vary;
    }

    @JsonProperty
    public void setVary(String vary) {
        this.vary = vary;
    }

    public BiDiGzipFilter build() {
        final BiDiGzipFilter filter = new BiDiGzipFilter();
        filter.setMinGzipSize((int) minimumEntitySize.toBytes());

        filter.setBufferSize((int) bufferSize.toBytes());

        filter.setDeflateCompressionLevel(deflateCompressionLevel);

        if (excludedUserAgents != null) {
            filter.setExcludedAgents(excludedUserAgents);
        }

        if (compressedMimeTypes != null) {
            filter.setMimeTypes(compressedMimeTypes);
        }

        if (includedMethods != null) {
            filter.setMethods(includedMethods);
        }

        if (excludedUserAgentPatterns != null) {
            filter.setExcludedAgentPatterns(excludedUserAgentPatterns);
        }

        if (vary != null) {
            filter.setVary(vary);
        }

        filter.setDeflateNoWrap(gzipCompatibleDeflation);
        filter.setInflateNoWrap(gzipCompatibleInflation);

        return filter;
    }
}
