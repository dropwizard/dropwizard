package io.dropwizard.logging;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.OutputStreamAppender;
import ch.qos.logback.core.spi.DeferredProcessingAware;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.dropwizard.logging.socket.DropwizardSocketAppender;
import io.dropwizard.util.Duration;
import io.dropwizard.util.Size;
import io.dropwizard.validation.MinSize;
import io.dropwizard.validation.PortRange;
import org.hibernate.validator.constraints.NotEmpty;

import javax.net.SocketFactory;
import javax.validation.constraints.NotNull;

/**
 * An {@link AppenderFactory} implementation which provides an appender that writes events to a TCP socket.
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
 * <td>The hostname of the TCP server.</td>
 * </tr>
 * <tr>
 * <td>{@code port}</td>
 * <td>{@code 4560}</td>
 * <td>The port on which the TCP server is listening.</td>
 * </tr>
 * <tr>
 * <td>{@code connectionTimeout}</td>
 * <td>{@code 500 ms}</td>
 * <td>The timeout to connect to the TCP server.</td>
 * </tr>
 * <tr>
 * <td>{@code immediateFlush}</td>
 * <td>{@code true}</td>
 * <td>If set to true, log events will be immediately send to the server. Immediate flushing is safer, but it
 * degrades logging throughput.</td>
 * </tr>
 * <tr>
 * <td>{@code sendBufferSize}</td>
 * <td>8KB</td>
 * <td>The buffer size of the underlying SocketAppender. Takes into effect if immediateFlush is disabled.</td>
 * </tr>
 * </table>
 */
@JsonTypeName("tcp")
public class TcpSocketAppenderFactory<E extends DeferredProcessingAware> extends AbstractOutputStreamAppenderFactory<E> {

    @NotEmpty
    private String host = "localhost";

    @PortRange
    private int port = 4560;

    @NotNull
    private Duration connectionTimeout = Duration.milliseconds(500);

    private boolean immediateFlush = true;

    @MinSize(1)
    private Size sendBufferSize = Size.kilobytes(8);

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

    @JsonProperty
    public Duration getConnectionTimeout() {
        return connectionTimeout;
    }

    @JsonProperty
    public void setConnectionTimeout(Duration connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    @JsonProperty
    public boolean isImmediateFlush() {
        return immediateFlush;
    }

    @JsonProperty
    public void setImmediateFlush(boolean immediateFlush) {
        this.immediateFlush = immediateFlush;
    }

    @JsonProperty
    public Size getSendBufferSize() {
        return sendBufferSize;
    }

    @JsonProperty
    public void setSendBufferSize(Size sendBufferSize) {
        this.sendBufferSize = sendBufferSize;
    }

    @Override
    protected OutputStreamAppender<E> appender(LoggerContext context) {
        final OutputStreamAppender<E> appender = new DropwizardSocketAppender<>(host, port,
            (int) connectionTimeout.toMilliseconds(), (int) sendBufferSize.toBytes(), socketFactory());
        appender.setContext(context);
        appender.setName("tcp-socket-appender");
        appender.setImmediateFlush(immediateFlush);
        return appender;
    }

    protected SocketFactory socketFactory() {
        return SocketFactory.getDefault();
    }

}
