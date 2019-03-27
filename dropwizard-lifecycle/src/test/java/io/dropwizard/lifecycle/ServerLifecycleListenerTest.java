package io.dropwizard.lifecycle;

import org.assertj.core.api.Assertions;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ServerLifecycleListenerTest {
    private static final String APPLICATION = "application";
    private static final String ADMIN = "admin";
    private static final String HOST_1 = "192.168.2.5";
    private static final String HOST_2 = "192.68.4.77";
    private static final String HOST_3 = "192.168.67.20";

    @Test
    void getLocalPort() {
        int localPort = 5673;
        int adminPort = 12345;
        Server server = configureAndGetSingleConnectorServer(localPort, adminPort);
        ServerLifecycleListener listener = (server1) -> {
        };
        int retrievedLocalPort = listener.getLocalPort(server);
        Assertions.assertThat(retrievedLocalPort).isEqualTo(localPort);
    }

    @Test
    void getAdminPort() {
        int localPort = 5673;
        int adminPort = 12345;
        Server server = configureAndGetSingleConnectorServer(localPort, adminPort);
        ServerLifecycleListener listener = (server1) -> {
        };
        int retrievedAdminPort = listener.getAdminPort(server);
        Assertions.assertThat(retrievedAdminPort).isEqualTo(adminPort);
    }

    private Server configureAndGetSingleConnectorServer(int applicationPort, int adminPort) {
        Server server = mock(Server.class);
        ServerConnector applicationConnector = mock(ServerConnector.class);
        ServerConnector adminConnector = mock(ServerConnector.class);
        Connector[] connectors = {applicationConnector, adminConnector};
        when(server.getConnectors()).thenReturn(connectors);
        configuredServerConnector(applicationConnector, applicationPort, Arrays.asList("ssl", "http/1.1", "http/2"), APPLICATION, HOST_1);
        configuredServerConnector(adminConnector, adminPort, Arrays.asList("tls", "http/2"), ADMIN, HOST_2);
        return server;
    }

    @Test
    void getPortDescriptorList() {
        Server server = configureMultiProtocolServer();
        ServerLifecycleListener listener = (server1) -> {
        };
        List<PortDescriptor> portDescriptorList = listener.getPortDescriptorList(server);
        PortDescriptor[] portDescriptors = buildCompletePortDescriptorsArray();
        Assertions.assertThat(portDescriptorList).usingElementComparator((o1, o2) -> {
            if (Objects.equals(o1.getConnectorType(), o2.getConnectorType()) &&
                Objects.equals(o1.getProtocol(), o2.getProtocol()) &&
                Objects.equals(o1.getPort(), o2.getPort()) &&
                Objects.equals(o1.getHost(), o2.getHost())) {
                return 0;
            } else {
                return -1;
            }
        }).contains(portDescriptors);
    }

    private PortDescriptor[] buildCompletePortDescriptorsArray() {
        return Stream.of(getPortDescriptors(5673, ADMIN, new String[]{"ssl", "http/1.1", "http/2"}, HOST_1),
            getPortDescriptors(12345, APPLICATION, new String[]{"tls", "http/2"}, HOST_2),
            getPortDescriptors(1234, APPLICATION, new String[]{"http/1.1", "http/2", "websocket"}, HOST_3))
            .flatMap(Arrays::stream)
            .toArray(PortDescriptor[]::new);
    }

    private PortDescriptor[] getPortDescriptors(int port, String type, String[] protocols, String host) {
        return Arrays.stream(protocols)
            .map(protocol -> getPortDescriptor(protocol, port, type, host))
            .toArray(PortDescriptor[]::new);
    }

    private PortDescriptor getPortDescriptor(String protocol, int port, String type, String host) {
        return new PortDescriptor(protocol, port, type, host);
    }

    private Server configureMultiProtocolServer() {
        Server server = mock(Server.class);
        ServerConnector connectorMock1 = mock(ServerConnector.class);
        ServerConnector connectorMock2 = mock(ServerConnector.class);
        ServerConnector connectorMock3 = mock(ServerConnector.class);
        Connector[] connectors = {connectorMock1, connectorMock2, connectorMock3};
        when(server.getConnectors()).thenReturn(connectors);
        configuredServerConnector(connectorMock1, 5673, Arrays.asList("ssl", "http/1.1", "http/2"), ADMIN, HOST_1);
        configuredServerConnector(connectorMock2, 12345, Arrays.asList("tls", "http/2"), APPLICATION, HOST_2);
        configuredServerConnector(connectorMock3, 1234, Arrays.asList("http/1.1", "http/2", "websocket"), APPLICATION, "192.168.67.20");
        return server;
    }

    private void configuredServerConnector(ServerConnector connectorMock1, int localPort, List<String> protocols, String portType, String host) {
        when(connectorMock1.getLocalPort()).thenReturn(localPort);
        when(connectorMock1.getProtocols()).thenReturn(protocols);
        when(connectorMock1.getName()).thenReturn(portType);
        when(connectorMock1.getHost()).thenReturn(host);
    }
}
