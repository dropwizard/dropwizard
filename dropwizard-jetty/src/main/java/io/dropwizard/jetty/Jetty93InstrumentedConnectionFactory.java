package io.dropwizard.jetty;

import com.codahale.metrics.Timer;
import org.eclipse.jetty.io.Connection;
import org.eclipse.jetty.io.EndPoint;
import org.eclipse.jetty.server.ConnectionFactory;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.util.component.ContainerLifeCycle;

import javax.annotation.Nullable;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * A version {@link com.codahale.metrics.jetty9.InstrumentedConnectionFactory}, which supports Jetty 9.3 API.
 * NOTE: This class could be replaced, when <strong>dropwizard-metrics-jetty9</strong> will support Jetty 9.3.
 */
public class Jetty93InstrumentedConnectionFactory extends ContainerLifeCycle implements ConnectionFactory {

    private final ConnectionFactory connectionFactory;
    private final Timer timer;

    public Jetty93InstrumentedConnectionFactory(ConnectionFactory connectionFactory, Timer timer) {
        this.connectionFactory = connectionFactory;
        this.timer = timer;
        addBean(connectionFactory);
    }

    public ConnectionFactory getConnectionFactory() {
        return connectionFactory;
    }

    public Timer getTimer() {
        return timer;
    }

    @Override
    public String getProtocol() {
        return connectionFactory.getProtocol();
    }

    @Override
    public List<String> getProtocols() {
        return connectionFactory.getProtocols();
    }

    @Override
    public Connection newConnection(Connector connector, EndPoint endPoint) {
        final Connection connection = connectionFactory.newConnection(connector, endPoint);
        connection.addListener(new Connection.Listener() {

            @Nullable
            private Timer.Context context;

            @Override
            public void onOpened(Connection connection) {
                this.context = timer.time();
            }

            @Override
            public void onClosed(Connection connection) {
                requireNonNull(context).stop();
            }
        });
        return connection;
    }
}
