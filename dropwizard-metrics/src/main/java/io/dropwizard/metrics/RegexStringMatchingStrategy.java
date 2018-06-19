package io.dropwizard.metrics;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;

import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

class RegexStringMatchingStrategy implements StringMatchingStrategy {
    private final LoadingCache<String, Pattern> patternCache;

    RegexStringMatchingStrategy() {
        patternCache = Caffeine.newBuilder()
            .expireAfterWrite(1, TimeUnit.HOURS)
            .build(Pattern::compile);
    }

    @Override
    public boolean containsMatch(Set<String> matchExpressions, String metricName) {
        for (String regexExpression : matchExpressions) {
            final Pattern pattern = patternCache.get(regexExpression);
            if (pattern != null && pattern.matcher(metricName).matches()) {
                // just need to match on a single value - return as soon as we do
                return true;
            }
        }
        return false;
    }
}
