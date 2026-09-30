package io.dropwizard.core.server;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

/**
 * Configuration options for Content Security Policy (CSP).
 */
public class CspConfiguration {

    @Nullable
    private String policy;

    @Nullable
    private String reportOnlyPolicy;

    @JsonProperty
    public Optional<String> getPolicy() {
        return Optional.ofNullable(policy);
    }

    @JsonProperty
    public void setPolicy(String policy) {
        this.policy = policy;
    }

    @JsonProperty
    public Optional<String> getReportOnlyPolicy() {
        return Optional.ofNullable(reportOnlyPolicy);
    }

    @JsonProperty
    public void setReportOnlyPolicy(String reportOnlyPolicy) {
        this.reportOnlyPolicy = reportOnlyPolicy;
    }
}
