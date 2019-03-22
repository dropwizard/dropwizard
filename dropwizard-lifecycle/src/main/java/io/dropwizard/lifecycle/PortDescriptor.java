package io.dropwizard.lifecycle;

import com.google.common.base.MoreObjects;

public class PortDescriptor {

    private static final String UNKNOWN = "UNKNOWN";
    private static final String DEFAULT_HOST = "0.0.0.0";

    private String host;

    private String protocol;

    private int port;

    private String connectorType;

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

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getConnectorType() {
        return connectorType;
    }

    public void setConnectorType(String connectorType) {
        this.connectorType = connectorType;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
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
