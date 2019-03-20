package io.dropwizard.lifecycle;

import com.google.common.base.MoreObjects;

public class PortDescriptor {

    private String protocol;
    private int port;
    private String connectorType;

    public PortDescriptor() {
    }

    public PortDescriptor(String protocol, int port, String connectorType) {
        this.protocol = protocol;
        this.port = port;
        this.connectorType = connectorType;
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

    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("port", port)
            .add("protocol", protocol)
            .add("connectorType", connectorType)
            .toString();
    }
}
