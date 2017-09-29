package io.dropwizard.metrics;

import com.google.common.collect.ImmutableSet;

class SubstringMatchingStrategy implements StringMatchingStrategy {
    @Override
    public boolean containsMatch(ImmutableSet<String> matchExpressions, String metricName) {
        return matchExpressions.stream().anyMatch(matchExpression -> metricName.contains(matchExpression));
    }
}
