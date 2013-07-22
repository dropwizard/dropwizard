package com.codahale.dropwizard.spdy;

import com.codahale.dropwizard.util.Duration;
import com.codahale.dropwizard.validation.MinDuration;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.eclipse.jetty.spdy.server.http.PushStrategy;
import org.eclipse.jetty.spdy.server.http.ReferrerPushStrategy;

import javax.validation.constraints.Min;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * A SPDY push strategy that auto-populates push metadata based on referrer URLs.
 * <p/>
 * <b>Configuration Parameters:</b>
 * <table>
 *     <tr>
 *         <td>Name</td>
 *         <td>Default</td>
 *         <td>Description</td>
 *     </tr>
 *     <tr>
 *         <td>{@code pushableOriginPatterns}</td>
 *         <td>(none)</td>
 *         <td>
 *             The list of origin patterns to which pushes are allowed. If not specified, all
 *             origins are allowed.
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>{@code maxAssociatedResources}</td>
 *         <td>32</td>
 *         <td>The maximum number of associated resources to push.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code referrerPushPeriod}</td>
 *         <td>5 seconds</td>
 *         <td>The amount of time after a request to consider following requests as secondary.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code pushableFilenamePatterns}</td>
 *         <td>
 *             {@code *.css}, {@code *.js}, {@code *.png}, {@code *.jpeg}, {@code *.jpg},
 *             {@code *.gif}, {@code *.ico}
 *         </td>
 *         <td>
 *             The list of regular expressions which determine which secondary requests are pushable
 *             resources.
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>{@code pushableContentTypes}</td>
 *         <td>
 *             {@code text/css}, {@code text/javascript}, {@code application/javascript},
 *             {@code application/x-javascript}, {@code image/png}, {@code image/x-png},
 *             {@code image/jpeg}, {@code image/gif}, {@code image/x-icon},
 *             {@code image/vnd.microsoft.icon}
 *         </td>
 *         <td>
 *             The list of MIME types of pushable resources.
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>{@code nonPushableUserAgentPatterns}</td>
 *         <td>(none)</td>
 *         <td>The list of user-agent patterns to which patterns are <b>not</b> allowed.</td>
 *     </tr>
 * </table>
 *
 * @see PushStrategyFactory
 * @see ReferrerPushStrategy
 */
@JsonTypeName("referrer")
public class ReferrerPushStrategyFactory implements PushStrategyFactory {
    private List<String> pushableOriginPatterns;

    @Min(1)
    private int maxAssociatedResources = 32;

    @MinDuration(value = 1, unit = TimeUnit.MILLISECONDS)
    private Duration referrerPushPeriod = Duration.seconds(5);

    private List<String> pushableFilenamePatterns;

    private List<String> pushableContentTypes;

    private List<String> nonPushableUserAgentPatterns;

    @JsonProperty
    public List<String> getPushableOriginPatterns() {
        return pushableOriginPatterns;
    }

    @JsonProperty
    public void setPushableOriginPatterns(List<String> origins) {
        this.pushableOriginPatterns = origins;
    }

    @JsonProperty
    public int getMaxAssociatedResources() {
        return maxAssociatedResources;
    }

    @JsonProperty
    public void setMaxAssociatedResources(int maxAssociatedResources) {
        this.maxAssociatedResources = maxAssociatedResources;
    }

    @JsonProperty
    public Duration getReferrerPushPeriod() {
        return referrerPushPeriod;
    }

    @JsonProperty
    public void setReferrerPushPeriod(Duration referrerPushPeriod) {
        this.referrerPushPeriod = referrerPushPeriod;
    }

    @JsonProperty
    public List<String> getPushableFilenamePatterns() {
        return pushableFilenamePatterns;
    }

    @JsonProperty
    public void setPushableFilenamePatterns(List<String> pushableFilenamePatterns) {
        this.pushableFilenamePatterns = pushableFilenamePatterns;
    }

    @JsonProperty
    public List<String> getPushableContentTypes() {
        return pushableContentTypes;
    }

    @JsonProperty
    public void setPushableContentTypes(List<String> pushableContentTypes) {
        this.pushableContentTypes = pushableContentTypes;
    }

    @JsonProperty
    public List<String> getNonPushableUserAgentPatterns() {
        return nonPushableUserAgentPatterns;
    }

    @JsonProperty
    public void setNonPushableUserAgentPatterns(List<String> nonPushableUserAgentPatterns) {
        this.nonPushableUserAgentPatterns = nonPushableUserAgentPatterns;
    }

    @Override
    public PushStrategy build() {
        final ReferrerPushStrategy strategy = new ReferrerPushStrategy();

        if (pushableOriginPatterns != null) {
            strategy.setAllowedPushOrigins(pushableOriginPatterns);
        }

        if (pushableContentTypes != null) {
            strategy.setPushContentTypes(pushableContentTypes);
        }

        if (pushableFilenamePatterns != null) {
            strategy.setPushRegexps(pushableFilenamePatterns);
        }

        if (nonPushableUserAgentPatterns != null) {
            strategy.setUserAgentBlacklist(nonPushableUserAgentPatterns);
        }

        strategy.setMaxAssociatedResources(maxAssociatedResources);
        strategy.setReferrerPushPeriod((int) referrerPushPeriod.toMilliseconds());

        return strategy;
    }
}
