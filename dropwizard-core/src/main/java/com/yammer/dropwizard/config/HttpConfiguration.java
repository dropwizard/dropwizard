package com.yammer.dropwizard.config;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.yammer.dropwizard.util.Duration;
import com.yammer.dropwizard.util.Size;
import com.yammer.dropwizard.validation.ValidationMethod;
import org.codehaus.jackson.annotate.JsonProperty;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

// TODO: 11/7/11 <coda> -- document HttpConfiguration

@SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal", "CanBeFinal"})
public class HttpConfiguration {
    @Valid
    @NotNull
    @JsonProperty
    protected RequestLogConfiguration requestLog = new RequestLogConfiguration();

    @Valid
    @NotNull
    @JsonProperty
    protected GzipConfiguration gzip = new GzipConfiguration();

    @Valid
    @JsonProperty
    protected SslConfiguration ssl = null;

    @NotNull
    @JsonProperty
    protected ImmutableMap<String, String> contextParameters = ImmutableMap.of();
    
    public enum ConnectorType {
        SOCKET,
        BLOCKING_CHANNEL,
        SELECT_CHANNEL,
        SOCKET_SSL,
        SELECT_CHANNEL_SSL
    }

    @Min(1025)
    @Max(65535)
    @JsonProperty
    protected int port = 8080;

    @Min(1025)
    @Max(65535)
    @JsonProperty
    protected int adminPort = 8081;

    @Min(2)
    @Max(1000000)
    @JsonProperty
    protected int maxThreads = 254;

    @Min(1)
    @Max(1000000)
    @JsonProperty
    protected int minThreads = 8;

    @NotNull
    @JsonProperty
    protected String rootPath = "/*";
    
    @NotNull
    @Pattern(regexp = "^(blocking|nonblocking|nonblocking\\+ssl|legacy|legacy\\+ssl)$",
             flags = {Pattern.Flag.CASE_INSENSITIVE})
    @JsonProperty
    protected String connectorType = "blocking";

    @NotNull
    @JsonProperty
    protected Duration maxIdleTime = Duration.seconds(200);

    @Min(1)
    @Max(128)
    @JsonProperty
    protected int acceptorThreadCount = 1;

    @Min(-Thread.NORM_PRIORITY)
    @Max(Thread.NORM_PRIORITY)
    @JsonProperty
    protected int acceptorThreadPriorityOffset = 0;

    @Min(-1)
    @JsonProperty
    protected int acceptQueueSize = -1;

    @Min(1)
    @JsonProperty
    protected int maxBufferCount = 1024;

    @NotNull
    @JsonProperty
    protected Size requestBufferSize = Size.kilobytes(16);

    @NotNull
    @JsonProperty
    protected Size requestHeaderBufferSize = Size.kilobytes(6);

    @NotNull
    @JsonProperty
    protected Size responseBufferSize = Size.kilobytes(32);

    @NotNull
    @JsonProperty
    protected Size responseHeaderBufferSize = Size.kilobytes(6);

    @JsonProperty
    protected boolean reuseAddress = true;

    @JsonProperty
    protected Duration soLingerTime = null;

    @JsonProperty
    protected int lowResourcesConnectionThreshold = 0;

    @NotNull
    @JsonProperty
    protected Duration lowResourcesMaxIdleTime = Duration.seconds(0);

    @NotNull
    @JsonProperty
    protected Duration shutdownGracePeriod = Duration.seconds(2);

    @JsonProperty
    protected boolean useServerHeader = false;

    @JsonProperty
    protected boolean useDateHeader = true;

    @JsonProperty
    protected boolean useForwardedHeaders = true;

    @JsonProperty
    protected boolean useDirectBuffers = true;

    @JsonProperty
    protected String bindHost = null;

    @JsonProperty
    protected String adminUsername = null;

    @JsonProperty
    protected String adminPassword = null;

    @ValidationMethod(message = "must have an SSL configuration when using SSL connection")
    public boolean isSslConfigured() {
        final ConnectorType type = getConnectorType();
        return !((ssl == null) && ((type == ConnectorType.SOCKET_SSL) ||
                                   (type == ConnectorType.SELECT_CHANNEL_SSL)));
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
        return requestLog;
    }

    public GzipConfiguration getGzipConfiguration() {
        return gzip;
    }

    public SslConfiguration getSslConfiguration() {
        return ssl;
    }

    public ImmutableMap<String, String> getContextParameters() {
        return contextParameters;
    }
    
    public ConnectorType getConnectorType() {
        if ("blocking".equalsIgnoreCase(connectorType)) {
            return ConnectorType.BLOCKING_CHANNEL;
        } else if ("legacy".equalsIgnoreCase(connectorType)) {
            return ConnectorType.SOCKET;
        } else if ("legacy+ssl".equalsIgnoreCase(connectorType)) {
            return ConnectorType.SOCKET_SSL;
        } else if ("nonblocking".equalsIgnoreCase(connectorType)) {
            return ConnectorType.SELECT_CHANNEL;
        } else if ("nonblocking+ssl".equalsIgnoreCase(connectorType)) {
            return ConnectorType.SELECT_CHANNEL_SSL;
        } else {
            throw new IllegalStateException("Invalid connector type: " + connectorType);
        }
    }

    public int getPort() {
        return port;
    }

    public int getAdminPort() {
        return adminPort;
    }

    public int getMaxThreads() {
        return maxThreads;
    }

    public int getMinThreads() {
        return minThreads;
    }

    public Duration getMaxIdleTime() {
        return maxIdleTime;
    }

    public int getAcceptorThreadCount() {
        return acceptorThreadCount;
    }

    public int getAcceptorThreadPriorityOffset() {
        return acceptorThreadPriorityOffset;
    }

    public int getAcceptQueueSize() {
        return acceptQueueSize;
    }

    public int getMaxBufferCount() {
        return maxBufferCount;
    }

    public Size getRequestBufferSize() {
        return requestBufferSize;
    }

    public Size getRequestHeaderBufferSize() {
        return requestHeaderBufferSize;
    }

    public Size getResponseBufferSize() {
        return responseBufferSize;
    }

    public Size getResponseHeaderBufferSize() {
        return responseHeaderBufferSize;
    }

    public boolean isReuseAddressEnabled() {
        return reuseAddress;
    }

    public Optional<Duration> getSoLingerTime() {
        return Optional.fromNullable(soLingerTime);
    }

    public int getLowResourcesConnectionThreshold() {
        return lowResourcesConnectionThreshold;
    }

    public Duration getLowResourcesMaxIdleTime() {
        return lowResourcesMaxIdleTime;
    }

    public Duration getShutdownGracePeriod() {
        return shutdownGracePeriod;
    }

    public boolean useForwardedHeaders() {
        return useForwardedHeaders;
    }

    public boolean useDirectBuffers() {
        return useDirectBuffers;
    }

    public Optional<String> getBindHost() {
        return Optional.fromNullable(bindHost);
    }

    public boolean isDateHeaderEnabled() {
        return useDateHeader;
    }

    public boolean isServerHeaderEnabled() {
        return useServerHeader;
    }

    public String getRootPath() {
        return rootPath;
    }

    public Optional<String> getAdminUsername() {
        return Optional.fromNullable(adminUsername);
    }

    public Optional<String> getAdminPassword() {
        return Optional.fromNullable(adminPassword);
    }
}
