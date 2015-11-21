package io.dropwizard.jetty;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Joiner;
import io.dropwizard.util.Duration;
import io.dropwizard.validation.MinDuration;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlets.PushCacheFilter;

import javax.annotation.Nullable;
import javax.servlet.DispatcherType;
import javax.validation.constraints.Min;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * A factory for building HTTP/2 {@link PushCacheFilter},
 * <p/>
 * <b>Configuration Parameters:</b>
 * <table>
 *     <tr>
 *         <td>Name</td>
 *         <td>Default</td>
 *         <td>Description</td>
 *     </tr>
 *     <tr>
 *         <td>{@code enabled}</td>
 *         <td>false</td>
 *         <td>
 *             If true, the filter will organize resources as primary resources (those referenced by the
 *             <i>Referer</i> header) and secondary resources (those that have the <i>Referer</i> header).
 *             Secondary resources that have been requested within a time window from the request of the
 *             primary resource will be associated with the it. The next time a client will
 *             request the primary resource, the server will send to the client the secondary resources
 *             along with the primary in a single response.
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>{@code associatePeriod}</td>
 *         <td>4 seconds</td>
 *         <td>
 *             The time window within which a request for a secondary resource will be associated to a
 *             primary resource.
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>{@code maxAssociations}</td>
 *         <td>16</td>
 *         <td>
 *             The maximum number of secondary resources that may be associated to a primary resource.
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>{@code refererHosts}</td>
 *         <td>All hosts</td>
 *         <td>
 *             The list of referrer hosts for which the server push technology is supported.
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>{@code refererPorts}</td>
 *         <td>All ports</td>
 *         <td>
 *             The list of referrer ports for which the server push technology is supported.
 *         </td>
 *     </tr>
 * </table>
 */
public class ServerPushFilterFactory {

    private static final Joiner COMMA_JOINER = Joiner.on(",");

    private boolean enabled = false;

    @MinDuration(value = 1, unit = TimeUnit.MILLISECONDS)
    private Duration associatePeriod = Duration.seconds(4);

    @Min(1)
    private int maxAssociations = 16;

    @Nullable
    private List<String> refererHosts;

    @Nullable
    private List<Integer> refererPorts;

    @JsonProperty
    public boolean isEnabled() {
        return enabled;
    }

    @JsonProperty
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @JsonProperty
    public Duration getAssociatePeriod() {
        return associatePeriod;
    }

    @JsonProperty
    public void setAssociatePeriod(Duration associatePeriod) {
        this.associatePeriod = associatePeriod;
    }

    @JsonProperty
    public int getMaxAssociations() {
        return maxAssociations;
    }

    @JsonProperty
    public void setMaxAssociations(int maxAssociations) {
        this.maxAssociations = maxAssociations;
    }

    @Nullable
    @JsonProperty
    public List<String> getRefererHosts() {
        return refererHosts;
    }

    @JsonProperty
    public void setRefererHosts(@Nullable List<String> refererHosts) {
        this.refererHosts = refererHosts;
    }

    @Nullable
    @JsonProperty
    public List<Integer> getRefererPorts() {
        return refererPorts;
    }

    @JsonProperty
    public void setRefererPorts(@Nullable List<Integer> refererPorts) {
        this.refererPorts = refererPorts;
    }

    public void addFilter(ServletContextHandler handler) {
        if (!enabled) {
            return;
        }

        handler.setInitParameter("associatePeriod", String.valueOf(associatePeriod.toMilliseconds()));
        handler.setInitParameter("maxAssociations", String.valueOf(maxAssociations));
        if (refererHosts != null) {
            handler.setInitParameter("hosts", COMMA_JOINER.join(refererHosts));
        }
        if (refererPorts != null) {
            handler.setInitParameter("ports", COMMA_JOINER.join(refererPorts));
        }
        handler.addFilter(PushCacheFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));
    }
}
