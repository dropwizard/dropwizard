package io.dropwizard.metrics.common;

import java.util.Set;

interface StringMatchingStrategy {
    boolean containsMatch(Set<String> matchExpressions, String metricName);
}
