package io.dropwizard.metrics;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableSet;

import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

class RegexStringMatchingStrategy implements StringMatchingStrategy {
    private final LoadingCache<String, Pattern> patternCache;

    RegexStringMatchingStrategy() {
        patternCache = CacheBuilder.newBuilder()
            .expireAfterWrite(1, TimeUnit.HOURS)
            .build(new CacheLoader<String, Pattern>() {
                @Override
                public Pattern load(String regex) throws Exception {
                    return Pattern.compile(regex);
                }
            });
    }

    @Override
    public boolean containsMatch(ImmutableSet<String> matchExpressions, String metricName) {
        for (String regexExpression : matchExpressions) {
            if (patternCache.getUnchecked(regexExpression).matcher(metricName).matches()) {
                // just need to match on a single value - return as soon as we do
                return true;
            }
        }
        return false;
    }
}
