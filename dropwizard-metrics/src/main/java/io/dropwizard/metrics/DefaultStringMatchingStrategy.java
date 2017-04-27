package io.dropwizard.metrics;

import com.google.common.collect.ImmutableSet;

class DefaultStringMatchingStrategy implements StringMatchingStrategy {
    @Override
    public boolean containsMatch(ImmutableSet<String> matchExpressions, String metricName) {
        return matchExpressions.contains(metricName);
    }
}
