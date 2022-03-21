package io.dropwizard.metrics.common;

import java.util.Set;

class DefaultStringMatchingStrategy implements StringMatchingStrategy {
    @Override
    public boolean containsMatch(Set<String> matchExpressions, String metricName) {
        return matchExpressions.contains(metricName);
    }
}
