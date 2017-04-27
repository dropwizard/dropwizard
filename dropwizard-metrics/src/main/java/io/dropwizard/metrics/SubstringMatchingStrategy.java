package io.dropwizard.metrics;

import com.google.common.collect.ImmutableSet;

class SubstringMatchingStrategy implements StringMatchingStrategy {
    @Override
    public boolean containsMatch(ImmutableSet<String> matchExpressions, String metricName) {
        for (String matchExpression : matchExpressions) {
            if (metricName.contains(matchExpression)) {
                // just need to match on a single value - return as soon as we do
                return true;
            }
        }
        return false;
    }
}
