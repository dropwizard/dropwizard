package io.dropwizard.lifecycle;

import com.google.common.base.MoreObjects;

public final class PortDescriptor {

    private static final String UNKNOWN = "UNKNOWN";
    private static final String DEFAULT_HOST = "0.0.0.0";

    private final String host;

    private final String protocol;

    private final int port;

    private final String connectorType;

    public PortDescriptor() {
        this(UNKNOWN, 0, UNKNOWN, DEFAULT_HOST);
    }

    public PortDescriptor(String protocol, int port, String connectorType, String host) {
        this.protocol = protocol;
        this.port = port;
        this.connectorType = connectorType;
        this.host = host;
    }

    public String getProtocol() {
        return protocol;
    }

    public int getPort() {
        return port;
    }

    public String getConnectorType() {
        return connectorType;
    }

    public String getHost() {
        return host;
    }

    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("port", port)
            .add("protocol", protocol)
            .add("connectorType", connectorType)
            .add("host", host)
            .toString();
    }
}
