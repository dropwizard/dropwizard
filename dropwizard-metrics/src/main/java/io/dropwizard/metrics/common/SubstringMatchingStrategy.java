package io.dropwizard.metrics.common;

import java.util.Set;

class SubstringMatchingStrategy implements StringMatchingStrategy {
    @Override
    public boolean containsMatch(Set<String> matchExpressions, String metricName) {
        for (String matchExpression : matchExpressions) {
            if (metricName.contains(matchExpression)) {
                // just need to match on a single value - return as soon as we do
                return true;
            }
        }
        return false;
    }
}
