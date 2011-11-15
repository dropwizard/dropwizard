package com.yammer.dropwizard.config;

import com.google.common.base.Optional;
import com.yammer.dropwizard.util.Duration;
import com.yammer.dropwizard.util.Size;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.List;

// TODO: 11/7/11 <coda> -- document HttpConfiguration
// TODO: 11/7/11 <coda> -- test HttpConfiguration

@SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal", "CanBeFinal"})
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

        private String minimumEntitySize = null;

        private String bufferSize = null;

        private List<String> excludedUserAgents = null;

        private List<String> compressedMimeTypes = null;

        public boolean isEnabled() {
            return enabled;
        }

        public Optional<Size> getMinimumEntitySize() {
            if (minimumEntitySize == null) {
                return Optional.absent();
            }
            return Optional.of(Size.parse(minimumEntitySize));
        }

        public Optional<Size> getBufferSize() {
            if (bufferSize == null) {
                return Optional.absent();
            }
            return Optional.of(Size.parse(bufferSize));
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

    public enum ConnectorType {
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
    @Pattern(regexp = Duration.VALID_DURATION)
    private String maxIdleTime = "1s";

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
    @Pattern(regexp = Size.VALID_SIZE)
    private String requestBufferSize = "32KiB";

    @NotNull
    @Pattern(regexp = Size.VALID_SIZE)
    private String requestHeaderBufferSize = "3KiB";

    @NotNull
    @Pattern(regexp = Size.VALID_SIZE)
    private String responseBufferSize = "32KiB";

    @NotNull
    @Pattern(regexp = Size.VALID_SIZE)
    private String responseHeaderBufferSize = "6KiB";

    private boolean reuseAddress = true;

    @Pattern(regexp = Duration.VALID_DURATION)
    private String soLingerTime = null;

    @Min(1)
    private int lowResourcesConnectionThreshold = 25000;

    @NotNull
    @Pattern(regexp = Duration.VALID_DURATION)
    private String lowResourcesMaxIdleTime = "5s";

    @NotNull
    @Pattern(regexp = Duration.VALID_DURATION)
    private String shutdownGracePeriod = "2s";

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
        if ("blocking".equalsIgnoreCase(connectorType)) {
            return ConnectorType.BLOCKING_CHANNEL;
        } else if ("legacy".equalsIgnoreCase(connectorType)) {
            return ConnectorType.SOCKET;
        } else if ("nonblocking".equalsIgnoreCase(connectorType)) {
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
        return Duration.parse(maxIdleTime);
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
        return Size.parse(requestBufferSize);
    }

    public Size getRequestHeaderBufferSize() {
        return Size.parse(requestHeaderBufferSize);
    }

    public Size getResponseBufferSize() {
        return Size.parse(responseBufferSize);
    }

    public Size getResponseHeaderBufferSize() {
        return Size.parse(responseHeaderBufferSize);
    }

    public boolean isReuseAddressEnabled() {
        return reuseAddress;
    }

    public Optional<Duration> getSoLingerTime() {
        if (soLingerTime == null) {
            return Optional.absent();
        }
        return Optional.of(Duration.parse(soLingerTime));
    }

    public int getLowResourcesConnectionThreshold() {
        return lowResourcesConnectionThreshold;
    }

    public Duration getLowResourcesMaxIdleTime() {
        return Duration.parse(lowResourcesMaxIdleTime);
    }

    public Duration getShutdownGracePeriod() {
        return Duration.parse(shutdownGracePeriod);
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
}
