package io.dropwizard.metrics.ganglia;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.codahale.metrics.ganglia.GangliaReporter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import info.ganglia.gmetric4j.gmetric.GMetric;
import io.dropwizard.metrics.BaseReporterFactory;
import io.dropwizard.util.Duration;
import io.dropwizard.validation.MinDuration;
import org.hibernate.validator.constraints.NotEmpty;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

/**
 * A factory for {@link GangliaReporter} instances.
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
 *         <td>The hostname (or group) of the Ganglia server(s) to report to.</td>
 *     </tr>
 *     <tr>
 *         <td>port</td>
 *         <td>8649</td>
 *         <td>The port of the Ganglia server(s) to report to.</td>
 *     </tr>
 *     <tr>
 *         <td>mode</td>
 *         <td>unicast</td>
 *         <td>The UDP addressing mode to announce the metrics with. One of {@code unicast} or
 *         {@code multicast}.</td>
 *     </tr>
 *     <tr>
 *         <td>ttl</td>
 *         <td>1</td>
 *         <td>The time-to-live of the UDP packets for the announced metrics.</td>
 *     </tr>
 *     <tr>
 *         <td>uuid</td>
 *         <td><i>None</i></td>
 *         <td>The UUID to tag announced metrics with.</td>
 *     </tr>
 *     <tr>
 *         <td>spoof</td>
 *         <td><i>None</i></td>
 *         <td>The hostname and port to use instead of this nodes for the announced metrics. In the
 *         format {@code hostname:port}.</td>
 *     </tr>
 *     <tr>
 *         <td>tmax</td>
 *         <td>60</td>
 *         <td>The tmax value to annouce metrics with.</td>
 *     </tr>
 *     <tr>
 *         <td>dmax</td>
 *         <td>0</td>
 *         <td>The dmax value to announce metrics with.</td>
 *     </tr>
 * </table>
 */
@JsonTypeName("ganglia")
public class GangliaReporterFactory extends BaseReporterFactory {
    @NotNull
    @MinDuration(0)
    private Duration tmax = Duration.seconds(1);

    @NotNull
    @MinDuration(0)
    private Duration dmax = Duration.seconds(0);

    @NotEmpty
    private String host = "localhost";

    @Range(min = 1, max = 49151)
    private int port = 8649;

    @NotNull
    private GMetric.UDPAddressingMode mode = GMetric.UDPAddressingMode.UNICAST;

    @Range(min = 0, max = 255)
    private int ttl = 1;

    private String prefix;
    private UUID uuid;
    private String spoof;

    @JsonProperty
    public Duration getTmax() {
        return tmax;
    }

    @JsonProperty
    public void setTmax(Duration tmax) {
        this.tmax = tmax;
    }

    @JsonProperty
    public Duration getDmax() {
        return dmax;
    }

    @JsonProperty
    public void setDmax(Duration dmax) {
        this.dmax = dmax;
    }

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
    public GMetric.UDPAddressingMode getMode() {
        return mode;
    }

    @JsonProperty
    public void setMode(GMetric.UDPAddressingMode mode) {
        this.mode = mode;
    }

    @JsonProperty
    public int getTtl() {
        return ttl;
    }

    @JsonProperty
    public void setTtl(int ttl) {
        this.ttl = ttl;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    @JsonProperty
    public Optional<UUID> getUuid() {
        return Optional.ofNullable(uuid);
    }

    @JsonProperty
    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    @JsonProperty
    public Optional<String> getSpoof() {
        return Optional.ofNullable(spoof);
    }

    @JsonProperty
    public void setSpoof(String spoof) {
        this.spoof = spoof;
    }

    @Override
    public ScheduledReporter build(MetricRegistry registry) {
        try {
            final GMetric ganglia = new GMetric(host,
                                          port,
                                          mode,
                                          ttl,
                                          uuid != null || spoof != null,
                                          uuid,
                                          spoof);

            return GangliaReporter.forRegistry(registry)
                                  .convertDurationsTo(getDurationUnit())
                                  .convertRatesTo(getRateUnit())
                                  .filter(getFilter())
                                  .prefixedWith(getPrefix())
                                  .withDMax((int) dmax.toSeconds())
                                  .withTMax((int) tmax.toSeconds())
                                  .build(ganglia);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
