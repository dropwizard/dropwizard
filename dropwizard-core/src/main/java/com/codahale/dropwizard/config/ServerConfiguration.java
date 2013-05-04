package com.codahale.dropwizard.config;

import com.codahale.dropwizard.jetty.GzipHandlerFactory;
import com.codahale.dropwizard.jetty.RequestLogFactory;
import com.codahale.dropwizard.util.Duration;
import com.codahale.dropwizard.util.Size;
import com.codahale.dropwizard.util.SizeUnit;
import com.codahale.dropwizard.validation.MinDuration;
import com.codahale.dropwizard.validation.MinSize;
import com.codahale.dropwizard.validation.PortRange;
import com.codahale.dropwizard.validation.ValidationMethod;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Optional;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.concurrent.TimeUnit;

/**
 * An object representation of the {@code server} section of the YAML configuration file.
 */
public class ServerConfiguration {
    @Valid
    @NotNull
    private RequestLogFactory requestLog = new RequestLogFactory();

    @Valid
    @NotNull
    private GzipHandlerFactory gzip = new GzipHandlerFactory();

    @PortRange
    private int port = 8080;

    @PortRange
    private int adminPort = 8081;

    @Min(2)
    private int maxThreads = 1024;

    @Min(1)
    private int minThreads = 8;

    @Min(1)
    private int acceptorThreads = Math.max(1, Runtime.getRuntime().availableProcessors() / 2);

    @Min(1)
    private int selectorThreads = Runtime.getRuntime().availableProcessors();

    @Min(-1)
    private int acceptQueueSize = -1;

    private boolean reuseAddress = true;
    private Duration soLingerTime = null;
    private boolean useServerHeader = false;
    private boolean useDateHeader = true;
    private boolean useForwardedHeaders = true;
    private boolean useDirectBuffers = false;
    private String bindHost = null;
    private String adminUsername = null;
    private String adminPassword = null;

    @NotNull
    @MinSize(128)
    private Size headerCacheSize = Size.bytes(512);

    @NotNull
    @MinSize(value = 8, unit = SizeUnit.KILOBYTES)
    private Size outputBufferSize = Size.kilobytes(32);

    @NotNull
    @MinSize(value = 1, unit = SizeUnit.KILOBYTES)
    private Size maxRequestHeaderSize = Size.kilobytes(8);

    @NotNull
    @MinSize(value = 1, unit = SizeUnit.KILOBYTES)
    private Size maxResponseHeaderSize = Size.kilobytes(8);

    @NotNull
    @MinSize(value = 1, unit = SizeUnit.KILOBYTES)
    private Size inputBufferSize = Size.kilobytes(8);

    @NotNull
    @MinDuration(value = 1, unit = TimeUnit.MILLISECONDS)
    private Duration idleTimeout = Duration.seconds(30);

    @NotNull
    @MinSize(value = 1, unit = SizeUnit.BYTES)
    private Size minBufferPoolSize = Size.bytes(64);

    @NotNull
    @MinSize(value = 1, unit = SizeUnit.BYTES)
    private Size bufferPoolIncrement = Size.bytes(1024);

    @NotNull
    @MinSize(value = 1, unit = SizeUnit.BYTES)
    private Size maxBufferPoolSize = Size.kilobytes(64);

    @NotNull
    private Optional<Integer> maxQueuedRequests = Optional.absent();

    @JsonIgnore
    @ValidationMethod(message = "must have a smaller minThreads than maxThreads")
    public boolean isThreadPoolSizedCorrectly() {
        return minThreads <= maxThreads;
    }

    @JsonIgnore
    @ValidationMethod(message = "must have adminUsername if adminPassword is defined")
    public boolean isAdminUsernameDefined() {
        return (adminPassword == null) || (adminUsername != null);
    }

    @JsonProperty("requestLog")
    public RequestLogFactory getRequestLogFactory() {
        return requestLog;
    }

    @JsonProperty("requestLog")
    public void setRequestLogFactory(RequestLogFactory requestLog) {
        this.requestLog = requestLog;
    }

    @JsonProperty("gzip")
    public GzipHandlerFactory getGzipHandlerFactory() {
        return gzip;
    }

    @JsonProperty("gzip")
    public void setGzipHandlerFactory(GzipHandlerFactory gzip) {
        this.gzip = gzip;
    }

    @JsonProperty
    public int getPort() {
        return port;
    }

    @JsonProperty
    public void setPort(int port) {
        this.port = port;
    }

    @JsonProperty
    public int getAdminPort() {
        return adminPort;
    }

    @JsonProperty
    public void setAdminPort(int port) {
        this.adminPort = port;
    }

    @JsonProperty
    public int getMaxThreads() {
        return maxThreads;
    }

    @JsonProperty
    public void setMaxThreads(int count) {
        this.maxThreads = count;
    }

    @JsonProperty
    public int getMinThreads() {
        return minThreads;
    }

    @JsonProperty
    public void setMinThreads(int count) {
        this.minThreads = count;
    }

    @JsonProperty
    public int getAcceptorThreads() {
        return acceptorThreads;
    }

    @JsonProperty
    public void setAcceptorThreads(int count) {
        this.acceptorThreads = count;
    }

    @JsonProperty
    public int getAcceptQueueSize() {
        return acceptQueueSize;
    }

    @JsonProperty
    public void setAcceptQueueSize(int size) {
        this.acceptQueueSize = size;
    }

    @JsonProperty("reuseAddress")
    public boolean isReuseAddressEnabled() {
        return reuseAddress;
    }

    @JsonProperty
    public void setReuseAddress(boolean reuseAddress) {
        this.reuseAddress = reuseAddress;
    }

    @JsonProperty
    public Optional<Duration> getSoLingerTime() {
        return Optional.fromNullable(soLingerTime);
    }

    @JsonProperty
    public void setSoLingerTime(Duration duration) {
        this.soLingerTime = duration;
    }

    @JsonProperty("useForwardedHeaders")
    public boolean useForwardedHeaders() {
        return useForwardedHeaders;
    }

    @JsonProperty
    public void setUseForwardedHeaders(boolean useForwardedHeaders) {
        this.useForwardedHeaders = useForwardedHeaders;
    }

    @JsonProperty("useDirectBuffers")
    public boolean useDirectBuffers() {
        return useDirectBuffers;
    }

    @JsonProperty
    public void setUseDirectBuffers(boolean useDirectBuffers) {
        this.useDirectBuffers = useDirectBuffers;
    }

    @JsonProperty
    public Optional<String> getBindHost() {
        return Optional.fromNullable(bindHost);
    }

    @JsonProperty
    public void setBindHost(String host) {
        this.bindHost = host;
    }

    @JsonProperty("useDateHeader")
    public boolean isDateHeaderEnabled() {
        return useDateHeader;
    }

    @JsonProperty
    public void setUseDateHeader(boolean useDateHeader) {
        this.useDateHeader = useDateHeader;
    }

    @JsonProperty("useServerHeader")
    public boolean isServerHeaderEnabled() {
        return useServerHeader;
    }

    @JsonProperty
    public void setUseServerHeader(boolean useServerHeader) {
        this.useServerHeader = useServerHeader;
    }

    @JsonProperty
    public Optional<String> getAdminUsername() {
        return Optional.fromNullable(adminUsername);
    }

    @JsonProperty
    public void setAdminUsername(String username) {
        this.adminUsername = username;
    }

    @JsonProperty
    public Optional<String> getAdminPassword() {
        return Optional.fromNullable(adminPassword);
    }

    @JsonProperty
    public void setAdminPassword(String password) {
        this.adminPassword = password;
    }

    @JsonProperty
    public Size getHeaderCacheSize() {
        return headerCacheSize;
    }

    @JsonProperty
    public void setHeaderCacheSize(Size headerCacheSize) {
        this.headerCacheSize = headerCacheSize;
    }

    @JsonProperty
    public Size getOutputBufferSize() {
        return outputBufferSize;
    }

    @JsonProperty
    public void setOutputBufferSize(Size outputBufferSize) {
        this.outputBufferSize = outputBufferSize;
    }

    @JsonProperty
    public Size getMaxRequestHeaderSize() {
        return maxRequestHeaderSize;
    }

    @JsonProperty
    public void setMaxRequestHeaderSize(Size maxRequestHeaderSize) {
        this.maxRequestHeaderSize = maxRequestHeaderSize;
    }

    @JsonProperty
    public Size getMaxResponseHeaderSize() {
        return maxResponseHeaderSize;
    }

    @JsonProperty
    public void setMaxResponseHeaderSize(Size maxResponseHeaderSize) {
        this.maxResponseHeaderSize = maxResponseHeaderSize;
    }

    @JsonProperty
    public Size getInputBufferSize() {
        return inputBufferSize;
    }

    @JsonProperty
    public void setInputBufferSize(Size inputBufferSize) {
        this.inputBufferSize = inputBufferSize;
    }

    @JsonProperty
    public int getSelectorThreads() {
        return selectorThreads;
    }

    @JsonProperty
    public void setSelectorThreads(int selectorThreads) {
        this.selectorThreads = selectorThreads;
    }

    @JsonProperty
    public Duration getIdleTimeout() {
        return idleTimeout;
    }

    @JsonProperty
    public void setIdleTimeout(Duration idleTimeout) {
        this.idleTimeout = idleTimeout;
    }

    @JsonProperty
    public Size getMinBufferPoolSize() {
        return minBufferPoolSize;
    }

    @JsonProperty
    public void setMinBufferPoolSize(Size minBufferPoolSize) {
        this.minBufferPoolSize = minBufferPoolSize;
    }

    @JsonProperty
    public Size getBufferPoolIncrement() {
        return bufferPoolIncrement;
    }

    @JsonProperty
    public void setBufferPoolIncrement(Size bufferPoolIncrement) {
        this.bufferPoolIncrement = bufferPoolIncrement;
    }

    @JsonProperty
    public Size getMaxBufferPoolSize() {
        return maxBufferPoolSize;
    }

    @JsonProperty
    public void setMaxBufferPoolSize(Size maxBufferPoolSize) {
        this.maxBufferPoolSize = maxBufferPoolSize;
    }

    @JsonProperty
    public Optional<Integer> getMaxQueuedRequests() {
        return maxQueuedRequests;
    }

    @JsonProperty
    public void setMaxQueuedRequests(Optional<Integer> maxQueuedRequests) {
        this.maxQueuedRequests = maxQueuedRequests;
    }
}
