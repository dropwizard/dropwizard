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

@JsonTypeName("referrer")
public class ReferrerPushStrategyFactory implements PushStrategyFactory {
    private List<String> allowedPushOrigins;

    @Min(1)
    private int maxAssociatedResources = 32;

    @MinDuration(value = 1, unit = TimeUnit.MILLISECONDS)
    private Duration referrerPushPeriod = Duration.seconds(5);

    private List<String> pushRegexps;

    private List<String> pushContentTypes;

    private List<String> userAgentBlacklist;

    @JsonProperty
    public List<String> getAllowedPushOrigins() {
        return allowedPushOrigins;
    }

    @JsonProperty
    public void setAllowedPushOrigins(List<String> origins) {
        this.allowedPushOrigins = origins;
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
    public List<String> getPushRegexps() {
        return pushRegexps;
    }

    @JsonProperty
    public void setPushRegexps(List<String> pushRegexps) {
        this.pushRegexps = pushRegexps;
    }

    @JsonProperty
    public List<String> getPushContentTypes() {
        return pushContentTypes;
    }

    @JsonProperty
    public void setPushContentTypes(List<String> pushContentTypes) {
        this.pushContentTypes = pushContentTypes;
    }

    @JsonProperty
    public List<String> getUserAgentBlacklist() {
        return userAgentBlacklist;
    }

    @JsonProperty
    public void setUserAgentBlacklist(List<String> userAgentBlacklist) {
        this.userAgentBlacklist = userAgentBlacklist;
    }

    @Override
    public PushStrategy build() {
        final ReferrerPushStrategy strategy = new ReferrerPushStrategy();

        if (allowedPushOrigins != null) {
            strategy.setAllowedPushOrigins(allowedPushOrigins);
        }

        if (pushContentTypes != null) {
            strategy.setPushContentTypes(pushContentTypes);
        }

        if (pushRegexps != null) {
            strategy.setPushRegexps(pushRegexps);
        }

        if (userAgentBlacklist != null) {
            strategy.setUserAgentBlacklist(userAgentBlacklist);
        }

        strategy.setMaxAssociatedResources(maxAssociatedResources);
        strategy.setReferrerPushPeriod((int) referrerPushPeriod.toMilliseconds());

        return strategy;
    }
}
