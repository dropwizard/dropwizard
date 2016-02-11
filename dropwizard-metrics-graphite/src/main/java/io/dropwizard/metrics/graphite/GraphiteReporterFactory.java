package io.dropwizard.metrics.graphite;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.codahale.metrics.graphite.Graphite;
import com.codahale.metrics.graphite.GraphiteReporter;
import com.codahale.metrics.graphite.GraphiteUDP;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.common.annotations.VisibleForTesting;
import io.dropwizard.metrics.BaseReporterFactory;
import io.dropwizard.validation.OneOf;
import org.hibernate.validator.constraints.NotEmpty;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotNull;

/**
 * A factory for {@link GraphiteReporter} instances.
 * <p/>
 * <b>Configuration Parameters:</b>
 * <table>
 *     <tr>
 *         <td>Name</td>
 *         <td>Default</td>
 *         <td>Description</td>
 *     </tr>
 *     <tr>
 *         <td>host</td>
 *         <td>localhost</td>
 *         <td>The hostname of the Graphite server to report to.</td>
 *     </tr>
 *     <tr>
 *         <td>port</td>
 *         <td>8080</td>
 *         <td>The port of the Graphite server to report to.</td>
 *     </tr>
 *     <tr>
 *         <td>prefix</td>
 *         <td><i>None</i></td>
 *         <td>The prefix for Metric key names to report to Graphite.</td>
 *     </tr>
 *     <tr>
 *         <td>transport</td>
 *         <td><i>tcp</i></td>
 *         <td>The transport used to report to Graphite. One of {@code tcp} or
 *         {@code udp}.</td>
 *     </tr>
 * </table>
 */
@JsonTypeName("graphite")
public class GraphiteReporterFactory extends BaseReporterFactory {
    @NotEmpty
    private String host = "localhost";

    @Range(min = 0, max = 49151)
    private int port = 8080;

    @NotNull
    private String prefix = "";

    @NotNull
    @OneOf(value = {"tcp", "udp"}, ignoreCase = true)
    private String transport = "tcp";

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
    public String getPrefix() {
        return prefix;
    }

    @JsonProperty
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    @JsonProperty
    public String getTransport() {
        return transport;
    }

    @JsonProperty
    public void setTransport(String transport) {
        this.transport = transport;
    }

    @Override
    public ScheduledReporter build(MetricRegistry registry) {
        GraphiteReporter.Builder builder = builder(registry);

        if ("udp".equalsIgnoreCase(transport)) {
            return builder.build(new GraphiteUDP(host, port));
        } else {
            return builder.build(new Graphite(host, port));
        }
    }

    @VisibleForTesting
    protected GraphiteReporter.Builder builder(MetricRegistry registry) {
        return GraphiteReporter.forRegistry(registry)
                .convertDurationsTo(getDurationUnit())
                .convertRatesTo(getRateUnit())
                .filter(getFilter())
                .prefixedWith(getPrefix());
    }
}
