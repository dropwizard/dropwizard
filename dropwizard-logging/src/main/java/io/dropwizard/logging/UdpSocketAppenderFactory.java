package io.dropwizard.logging;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.encoder.LayoutWrappingEncoder;
import ch.qos.logback.core.spi.DeferredProcessingAware;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.dropwizard.logging.async.AsyncAppenderFactory;
import io.dropwizard.logging.filter.LevelFilterFactory;
import io.dropwizard.logging.layout.LayoutFactory;
import io.dropwizard.logging.socket.DropwizardUdpSocketAppender;
import io.dropwizard.validation.PortRange;
import org.hibernate.validator.constraints.NotEmpty;

/**
 * An {@link AppenderFactory} implementation which provides an appender that writes events to an UDP socket.
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
public class UdpSocketAppenderFactory<E extends DeferredProcessingAware> extends AbstractAppenderFactory<E> {

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
    public Appender<E> build(LoggerContext context, String applicationName, LayoutFactory<E> layoutFactory,
                             LevelFilterFactory<E> levelFilterFactory, AsyncAppenderFactory<E> asyncAppenderFactory) {
        final DropwizardUdpSocketAppender<E> appender = new DropwizardUdpSocketAppender<>(host, port);
        appender.setContext(context);
        appender.setName("udp-socket-appender");

        final LayoutWrappingEncoder<E> layoutEncoder = new LayoutWrappingEncoder<>();
        layoutEncoder.setLayout(buildLayout(context, layoutFactory));
        appender.setEncoder(layoutEncoder);

        appender.addFilter(levelFilterFactory.build(threshold));
        getFilterFactories().forEach(f -> appender.addFilter(f.build()));
        appender.start();
        return wrapAsync(appender, asyncAppenderFactory);
    }
}
