package com.yammer.dropwizard.config;

import com.google.common.base.Optional;
import com.yammer.dropwizard.util.Duration;
import com.yammer.dropwizard.util.Size;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.List;

// TODO: 11/7/11 <coda> -- document HttpConfiguration
// TODO: 11/7/11 <coda> -- test HttpConfiguration

@SuppressWarnings("FieldCanBeLocal")
public class HttpConfiguration {
    public static class RequestLogConfiguration {
        private boolean enabled = true;

        @NotNull
        private String filenamePattern = "./logs/yyyy_mm_dd.log";
        
        @Min(1)
        @Max(50)
        private int retainedFileCount = 5;

        public boolean isEnabled() {
            return enabled;
        }

        public String getFilenamePattern() {
            return filenamePattern;
        }

        public int getRetainedFileCount() {
            return retainedFileCount;
        }
    }

    public static class GzipConfiguration {
        private boolean enabled = true;

        private Size minimumEntitySize = null;

        private Size bufferSize = null;

        private List<String> excludedUserAgents = null;

        private List<String> compressedMimeTypes = null;

        public boolean isEnabled() {
            return enabled;
        }

        public Optional<Size> getMinimumEntitySize() {
            return Optional.fromNullable(minimumEntitySize);
        }

        public Optional<Size> getBufferSize() {
            return Optional.fromNullable(bufferSize);
        }

        public Optional<List<String>> getExcludedUserAgents() {
            return Optional.fromNullable(excludedUserAgents);
        }

        public Optional<List<String>> getCompressedMimeTypes() {
            return Optional.fromNullable(compressedMimeTypes);
        }
    }

    @NotNull
    private RequestLogConfiguration requestLog = new RequestLogConfiguration();

    @NotNull
    private GzipConfiguration gzip = new GzipConfiguration();

    public static enum ConnectorType {
        SOCKET,
        BLOCKING_CHANNEL,
        SELECT_CHANNEL
    }

    @Min(1025)
    @Max(65535)
    private int port = 8080;

    @Min(1025)
    @Max(65535)
    private int adminPort = 8081;

    @Min(10)
    @Max(20000)
    private int maxThreads = 100;

    @Min(10)
    @Max(20000)
    private int minThreads = 10;

    @NotNull
    @Pattern(regexp = "(blocking|nonblocking|legacy)",
             flags = {Pattern.Flag.CASE_INSENSITIVE})
    private String connectorType = "blocking";

    @NotNull
    @Valid
    private Duration maxIdleTime = Duration.seconds(1);

    @Min(1)
    @Max(128)
    private int acceptorThreadCount = Runtime.getRuntime().availableProcessors();

    @Min(-Thread.NORM_PRIORITY)
    @Max(Thread.NORM_PRIORITY)
    private int acceptorThreadPriorityOffset = 0;

    @Min(-1)
    private int acceptQueueSize = -1;

    @Min(1)
    private int maxBufferCount = 1024;

    @NotNull
    @Valid
    private Size requestBufferSize = Size.kilobytes(32);

    @NotNull
    @Valid
    private Size requestHeaderBufferSize = Size.kilobytes(6);

    @NotNull
    @Valid
    private Size responseBufferSize = Size.kilobytes(32);

    @NotNull
    @Valid
    private Size responseHeaderBufferSize = Size.kilobytes(6);

    private boolean reuseAddress = true;

    @Valid
    private Duration soLingerTime = null;

    @Min(1)
    private int lowResourcesConnectionThreshold = 25000;

    @NotNull
    @Valid
    private Duration lowResourcesMaxIdleTime = Duration.seconds(5);

    @NotNull
    @Valid
    private Duration shutdownGracePeriod = Duration.seconds(2);

    private boolean useServerHeader = false;

    private boolean useDateHeader = true;

    private boolean useForwardedHeaders = true;

    private boolean useDirectBuffers = true;

    private String bindHost = null;

    public RequestLogConfiguration getRequestLogConfiguration() {
        return requestLog;
    }

    public GzipConfiguration getGzipConfiguration() {
        return gzip;
    }

    public ConnectorType getConnectorType() {
        if (connectorType.equalsIgnoreCase("blocking")) {
            return ConnectorType.BLOCKING_CHANNEL;
        } else if (connectorType.equalsIgnoreCase("legacy")) {
            return ConnectorType.SOCKET;
        } else if (connectorType.equalsIgnoreCase("nonblocking")) {
            return ConnectorType.SELECT_CHANNEL;
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

    public boolean enableReuseAddress() {
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

    public boolean enableDateHeader() {
        return useDateHeader;
    }

    public boolean enableServerHeader() {
        return useServerHeader;
    }
}
