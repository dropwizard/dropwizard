package com.codahale.dropwizard.metrics.reporters;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.codahale.metrics.ganglia.GangliaReporter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.common.base.Optional;
import info.ganglia.gmetric4j.gmetric.GMetric;
import org.hibernate.validator.constraints.NotEmpty;
import org.hibernate.validator.constraints.Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.IOException;
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
 *         <td>The dmax value to annouce metrics with.</td>
 *     </tr>
 * </table>
 */
@JsonTypeName("ganglia")
public class GangliaReporterFactory extends BaseReporterFactory {

    @Min(0)
    private int tmax = 60;

    @Min(0)
    private int dmax = 0;

    @NotEmpty
    private String host = "localhost";

    @Range(min = 1, max = 49151)
    private int port = 8649;

    @NotNull
    private GMetric.UDPAddressingMode mode = GMetric.UDPAddressingMode.UNICAST;

    @Range(min = 0, max = 255)
    private int ttl = 1;

    private Optional<UUID> uuid;

    private Optional<String> spoof;

    @JsonProperty
    public int getTMax() {
        return tmax;
    }

    @JsonProperty
    public void setMax(int tMax) {
        this.tmax = tMax;
    }

    @JsonProperty
    public int getDMax() {
        return dmax;
    }

    @JsonProperty
    public void setDMax(int dMax) {
        this.dmax = dMax;
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

    @JsonProperty
    public Optional<UUID> getUuid() {
        return uuid;
    }

    @JsonProperty
    public void setUuid(Optional<UUID> uuid) {
        this.uuid = uuid;
    }

    @JsonProperty
    public Optional<String> getSpoof() {
        return spoof;
    }

    @JsonProperty
    public void setSpoof(Optional<String> spoof) {
        this.spoof = spoof;
    }

    @Override
    public ScheduledReporter build(MetricRegistry registry) {
        try {
            GMetric ganglia = new GMetric(
                    getHost(),
                    getPort(),
                    getMode(),
                    getTtl(),
                    getUuid().isPresent() || getSpoof().isPresent(),
                    getUuid().orNull(),
                    getSpoof().orNull());

            return GangliaReporter
                    .forRegistry(registry)
                    .convertDurationsTo(getDurationUnit())
                    .convertRatesTo(getRateUnit())
                    .filter(getFilter())
                    .withDMax(getDMax())
                    .withTMax(getTMax())
                    .build(ganglia);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
