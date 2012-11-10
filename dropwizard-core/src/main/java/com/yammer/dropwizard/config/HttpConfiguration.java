package com.yammer.dropwizard.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.yammer.dropwizard.util.Duration;
import com.yammer.dropwizard.util.Size;
import com.yammer.dropwizard.validation.ValidationMethod;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.Locale;
import java.util.Map;

/**
 * An object representation of the {@code http} section of the YAML configuration file.
 */
@SuppressWarnings("UnusedDeclaration")
public class HttpConfiguration {
    public enum ConnectorType {
        BLOCKING,
        LEGACY,
        LEGACY_SSL,
        NONBLOCKING,
        NONBLOCKING_SSL;

        @Override
        @JsonValue
        public String toString() {
            return super.toString().replace("_", "+").toLowerCase(Locale.ENGLISH);
        }

        @JsonCreator
        public static ConnectorType parse(String type) {
            return valueOf(type.toUpperCase(Locale.ENGLISH).replace('+', '_'));
        }
    }

    @Valid
    @NotNull
    @JsonProperty("requestLog")
    private RequestLogConfiguration requestLogConfiguration = new RequestLogConfiguration();

    @Valid
    @NotNull
    @JsonProperty("gzip")
    private GzipConfiguration gzipConfiguration = new GzipConfiguration();

    @Valid
    @JsonProperty("ssl")
    private SslConfiguration sslConfiguration = null;

    @NotNull
    @JsonProperty
    private ImmutableMap<String, String> contextParameters = ImmutableMap.of();

    @Min(1025)
    @Max(65535)
    @JsonProperty
    private int port = 8080;

    @Min(1025)
    @Max(65535)
    @JsonProperty
    private int adminPort = 8081;

    @Min(2)
    @Max(1000000)
    @JsonProperty
    private int maxThreads = 254;

    @Min(1)
    @Max(1000000)
    @JsonProperty
    private int minThreads = 8;

    @NotNull
    @JsonProperty
    private String rootPath = "/*";
    
    @NotNull
    @JsonProperty
    private ConnectorType connectorType = ConnectorType.BLOCKING;

    @NotNull
    @JsonProperty
    private Duration maxIdleTime = Duration.seconds(200);

    @Min(1)
    @Max(128)
    @JsonProperty
    private int acceptorThreads = 1;

    @Min(-Thread.NORM_PRIORITY)
    @Max(Thread.NORM_PRIORITY)
    @JsonProperty
    private int acceptorThreadPriorityOffset = 0;

    @Min(-1)
    @JsonProperty
    private int acceptQueueSize = -1;

    @Min(1)
    @JsonProperty
    private int maxBufferCount = 1024;

    @NotNull
    @JsonProperty
    private Size requestBufferSize = Size.kilobytes(16);

    @NotNull
    @JsonProperty
    private Size requestHeaderBufferSize = Size.kilobytes(6);

    @NotNull
    @JsonProperty
    private Size responseBufferSize = Size.kilobytes(32);

    @NotNull
    @JsonProperty
    private Size responseHeaderBufferSize = Size.kilobytes(6);

    @JsonProperty
    private boolean reuseAddress = true;

    @JsonProperty
    private Duration soLingerTime = null;

    @JsonProperty
    private int lowResourcesConnectionThreshold = 0;

    @NotNull
    @JsonProperty
    private Duration lowResourcesMaxIdleTime = Duration.seconds(0);

    @NotNull
    @JsonProperty
    private Duration shutdownGracePeriod = Duration.seconds(2);

    @JsonProperty
    private boolean useServerHeader = false;

    @JsonProperty
    private boolean useDateHeader = true;

    @JsonProperty
    private boolean useForwardedHeaders = true;

    @JsonProperty
    private boolean useDirectBuffers = true;

    @JsonProperty
    private String bindHost = null;

    @JsonProperty
    private String adminUsername = null;

    @JsonProperty
    private String adminPassword = null;

    @ValidationMethod(message = "must have an SSL configuration when using SSL connection")
    public boolean isSslConfigured() {
        final ConnectorType type = getConnectorType();
        return !((sslConfiguration == null) && ((type == ConnectorType.LEGACY_SSL) ||
                                   (type == ConnectorType.NONBLOCKING_SSL)));
    }

    @ValidationMethod(message = "must have a smaller minThreads than maxThreads")
    public boolean isThreadPoolSizedCorrectly() {
        return minThreads <= maxThreads;
    }
    
    @ValidationMethod(message = "must have adminUsername if adminPassword is defined")
    public boolean isAdminUsernameDefined() {
        return (adminPassword == null) || (adminUsername != null);
    }

    public RequestLogConfiguration getRequestLogConfiguration() {
        return requestLogConfiguration;
    }

    public void setRequestLogConfiguration(RequestLogConfiguration config) {
        this.requestLogConfiguration = config;
    }

    public GzipConfiguration getGzipConfiguration() {
        return gzipConfiguration;
    }

    public void setGzipConfiguration(GzipConfiguration config) {
        this.gzipConfiguration = config;
    }

    public SslConfiguration getSslConfiguration() {
        return sslConfiguration;
    }

    public void setSslConfiguration(SslConfiguration config) {
        this.sslConfiguration = config;
    }

    public ImmutableMap<String, String> getContextParameters() {
        return contextParameters;
    }

    public void setContextParameters(Map<String, String> contextParameters) {
        this.contextParameters = ImmutableMap.copyOf(contextParameters);
    }

    public ConnectorType getConnectorType() {
        return connectorType;
    }

    public void setConnectorType(ConnectorType type) {
        this.connectorType = type;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getAdminPort() {
        return adminPort;
    }

    public void setAdminPort(int port) {
        this.adminPort = port;
    }

    public int getMaxThreads() {
        return maxThreads;
    }

    public void setMaxThreads(int count) {
        this.maxThreads = count;
    }

    public int getMinThreads() {
        return minThreads;
    }

    public void setMinThreads(int count) {
        this.minThreads = count;
    }

    public Duration getMaxIdleTime() {
        return maxIdleTime;
    }

    public void setMaxIdleTime(Duration duration) {
        this.maxIdleTime = duration;
    }

    public int getAcceptorThreads() {
        return acceptorThreads;
    }

    public void setAcceptorThreads(int count) {
        this.acceptorThreads = count;
    }

    public int getAcceptorThreadPriorityOffset() {
        return acceptorThreadPriorityOffset;
    }

    public void setAcceptorThreadPriorityOffset(int priorityOffset) {
        this.acceptorThreadPriorityOffset = priorityOffset;
    }

    public int getAcceptQueueSize() {
        return acceptQueueSize;
    }

    public void setAcceptQueueSize(int size) {
        this.acceptQueueSize = size;
    }

    public int getMaxBufferCount() {
        return maxBufferCount;
    }

    public void setMaxBufferCount(int count) {
        this.maxBufferCount = count;
    }

    public Size getRequestBufferSize() {
        return requestBufferSize;
    }

    public void setRequestBufferSize(Size size) {
        this.requestBufferSize = size;
    }

    public Size getRequestHeaderBufferSize() {
        return requestHeaderBufferSize;
    }

    public void setRequestHeaderBufferSize(Size size) {
        this.requestHeaderBufferSize = size;
    }

    public Size getResponseBufferSize() {
        return responseBufferSize;
    }

    public void setResponseBufferSize(Size size) {
        this.responseBufferSize = size;
    }

    public Size getResponseHeaderBufferSize() {
        return responseHeaderBufferSize;
    }

    public void setResponseHeaderBufferSize(Size size) {
        this.responseHeaderBufferSize = size;
    }

    public boolean isReuseAddressEnabled() {
        return reuseAddress;
    }

    public void setReuseAddress(boolean reuseAddress) {
        this.reuseAddress = reuseAddress;
    }

    public Optional<Duration> getSoLingerTime() {
        return Optional.fromNullable(soLingerTime);
    }

    public void setSoLingerTime(Duration duration) {
        this.soLingerTime = duration;
    }

    public int getLowResourcesConnectionThreshold() {
        return lowResourcesConnectionThreshold;
    }

    public void setLowResourcesConnectionThreshold(int connectionCount) {
        this.lowResourcesConnectionThreshold = connectionCount;
    }

    public Duration getLowResourcesMaxIdleTime() {
        return lowResourcesMaxIdleTime;
    }

    public void setLowResourcesMaxIdleTime(Duration duration) {
        this.lowResourcesMaxIdleTime = duration;
    }

    public Duration getShutdownGracePeriod() {
        return shutdownGracePeriod;
    }

    public void setShutdownGracePeriod(Duration duration) {
        this.shutdownGracePeriod = duration;
    }

    public boolean useForwardedHeaders() {
        return useForwardedHeaders;
    }

    public void setUseForwardedHeaders(boolean useForwardedHeaders) {
        this.useForwardedHeaders = useForwardedHeaders;
    }

    public boolean useDirectBuffers() {
        return useDirectBuffers;
    }

    public void setUseDirectBuffers(boolean useDirectBuffers) {
        this.useDirectBuffers = useDirectBuffers;
    }

    public Optional<String> getBindHost() {
        return Optional.fromNullable(bindHost);
    }

    public void setBindHost(String host) {
        this.bindHost = host;
    }

    public boolean isDateHeaderEnabled() {
        return useDateHeader;
    }

    public void setUseDateHeader(boolean useDateHeader) {
        this.useDateHeader = useDateHeader;
    }

    public boolean isServerHeaderEnabled() {
        return useServerHeader;
    }

    public void setUseServerHeader(boolean useServerHeader) {
        this.useServerHeader = useServerHeader;
    }

    public String getRootPath() {
        return rootPath;
    }

    public void setRootPath(String path) {
        this.rootPath = path;
    }

    public Optional<String> getAdminUsername() {
        return Optional.fromNullable(adminUsername);
    }

    public void setAdminUsername(String username) {
        this.adminUsername = username;
    }

    public Optional<String> getAdminPassword() {
        return Optional.fromNullable(adminPassword);
    }

    public void setAdminPassword(String password) {
        this.adminPassword = password;
    }
}
