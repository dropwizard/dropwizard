package io.dropwizard.logging.common;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.OutputStreamAppender;
import ch.qos.logback.core.spi.DeferredProcessingAware;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.dropwizard.logging.common.socket.DropwizardUdpSocketAppender;
import io.dropwizard.validation.PortRange;
import jakarta.validation.constraints.NotEmpty;

/**
 * An {@link AppenderFactory} implementation which provides an appender that writes events to a UDP socket.
 * <p/>
 * <b>Configuration Parameters:</b>
 * <table>
 * <tr>
 * <td>Name</td>
 * <td>Default</td>
 * <td>Description</td>
 * </tr>
 * <tr>
 * <td>{@code host}</td>
 * <td>{@code localhost}</td>
 * <td>The hostname of the UDP server.</td>
 * </tr>
 * <tr>
 * <td>{@code port}</td>
 * <td>{@code 514}</td>
 * <td>The port on which the UDP server is listening.</td>
 * </tr>
 * </table>
 */
@JsonTypeName("udp")
public class UdpSocketAppenderFactory<E extends DeferredProcessingAware> extends AbstractOutputStreamAppenderFactory<E> {

    @NotEmpty
    private String host = "localhost";

    @PortRange
    private int port = 514;

    @JsonProperty
    public String getHost() {
        return host;
    }

    @JsonProperty
    public void setHost(String host) {
        this.host = host;
    }

    @JsonProperty
    public int getPort() {
        return port;
    }

    @JsonProperty
    public void setPort(int port) {
        this.port = port;
    }

    @Override
    protected OutputStreamAppender<E> appender(LoggerContext context) {
        final DropwizardUdpSocketAppender<E> appender = new DropwizardUdpSocketAppender<>(host, port);
        appender.setContext(context);
        appender.setName("udp-socket-appender");
        return appender;
    }
}
