package io.dropwizard.metrics;

import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.ScheduledReporter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableSet;
import io.dropwizard.util.Duration;
import io.dropwizard.validation.MinDuration;
import org.hibernate.validator.valuehandling.UnwrapValidatedValue;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * A base {@link ReporterFactory} for configuring metric reporters.
 * <p/>
 * Configures options common to all {@link ScheduledReporter}s.
 * <p/>
 * <b>Configuration Parameters:</b>
 * <table>
 *     <tr>
 *         <td>Name</td>
 *         <td>Default</td>
 *         <td>Description</td>
 *     </tr>
 *     <tr>
 *         <td>durationUnit</td>
 *         <td>milliseconds</td>
 *         <td>The unit to report durations as. Overrides per-metric duration units.</td>
 *     </tr>
 *     <tr>
 *         <td>rateUnit</td>
 *         <td>seconds</td>
 *         <td>The unit to report rates as. Overrides per-metric rate units.</td>
 *     </tr>
 *     <tr>
 *         <td>excludes</td>
 *         <td>No excluded metrics.</td>
 *         <td>Metrics to exclude from reports, by name. When defined, matching metrics will not be
 *         reported. See {@link #getFilter()}.</td>
 *     </tr>
 *     <tr>
 *         <td>includes</td>
 *         <td>All metrics included.</td>
 *         <td>Metrics to include in reports, by name. When defined, only these metrics will be
 *         reported. See {@link #getFilter()}.  Exclusion rules (excludes) take precedence,
 *         so if a name matches both <i>excludes</i> and <i>includes</i>, it is excluded.</td>
 *     </tr>
 *     <tr>
 *         <td>useRegexFilters</td>
 *         <td>false</td>
 *         <td>Indicates whether the values of the 'includes' and 'excludes' fields should be
 *         treated as regular expressions or not.</td>
 *     </tr>
 *     <tr>
 *         <td>frequency</td>
 *         <td>none</td>
 *         <td>The frequency to report metrics. Overrides the {@link
 *         MetricsFactory#getFrequency() default}.</td>
 *     </tr>
 * </table>
 */
public abstract class BaseReporterFactory implements ReporterFactory {

    private static final DefaultStringMatchingStrategy DEFAULT_STRING_MATCHING_STRATEGY =
            new DefaultStringMatchingStrategy();

    private static final RegexStringMatchingStrategy REGEX_STRING_MATCHING_STRATEGY =
            new RegexStringMatchingStrategy();

    @NotNull
    private TimeUnit durationUnit = TimeUnit.MILLISECONDS;

    @NotNull
    private TimeUnit rateUnit = TimeUnit.SECONDS;

    @NotNull
    private ImmutableSet<String> excludes = ImmutableSet.of();

    @NotNull
    private ImmutableSet<String> includes = ImmutableSet.of();

    @Valid
    @MinDuration(0)
    @UnwrapValidatedValue
    private Optional<Duration> frequency = Optional.empty();

    private boolean useRegexFilters = false;

    public TimeUnit getDurationUnit() {
        return durationUnit;
    }

    @JsonProperty
    public void setDurationUnit(TimeUnit durationUnit) {
        this.durationUnit = durationUnit;
    }

    @JsonProperty
    public TimeUnit getRateUnit() {
        return rateUnit;
    }

    @JsonProperty
    public void setRateUnit(final TimeUnit rateUnit) {
        this.rateUnit = rateUnit;
    }

    @JsonProperty
    public ImmutableSet<String> getIncludes() {
        return includes;
    }

    @JsonProperty
    public void setIncludes(ImmutableSet<String> includes) {
        this.includes = includes;
    }

    @JsonProperty
    public ImmutableSet<String> getExcludes() {
        return excludes;
    }

    @JsonProperty
    public void setExcludes(ImmutableSet<String> excludes) {
        this.excludes = excludes;
    }

    @JsonProperty
    public Optional<Duration> getFrequency() {
        return frequency;
    }

    @JsonProperty
    public void setFrequency(Optional<Duration> frequency) {
        this.frequency = frequency;
    }

    @JsonProperty
    public boolean getUseRegexFilters() {
        return useRegexFilters;
    }

    @JsonProperty
    public void setUseRegexFilters(boolean useRegexFilters) {
        this.useRegexFilters = useRegexFilters;
    }

    /**
     * Gets a {@link MetricFilter} that specifically includes and excludes configured metrics.
     * <p/>
     * Filtering works in 4 ways:
     * <dl>
     *     <dt><i>unfiltered</i></dt>
     *     <dd>All metrics are reported</dd>
     *     <dt><i>excludes</i>-only</dt>
     *     <dd>All metrics are reported, except those whose name is listed in <i>excludes</i>.</dd>
     *     <dt><i>includes</i>-only</dt>
     *     <dd>Only metrics whose name is listed in <i>includes</i> are reported.</dd>
     *     <dt>mixed (both <i>includes</i> and <i>excludes</i></dt>
     *     <dd>Only metrics whose name is listed in <i>includes</i> and
     *     <em>not</em> listed in <i>excludes</i> are reported;
     *     <i>excludes</i> takes precedence over <i>includes</i>.</dd>
     * </dl>
     *
     * @return the filter for selecting metrics based on the configured excludes/includes.
     * @see #getIncludes()
     * @see #getExcludes()
     */
    @JsonIgnore
    public MetricFilter getFilter() {
        final StringMatchingStrategy stringMatchingStrategy = getUseRegexFilters() ?
                REGEX_STRING_MATCHING_STRATEGY : DEFAULT_STRING_MATCHING_STRATEGY;

        return (name, metric) -> {
            // Include the metric if its name is not excluded and its name is included
            // Where, by default, with no includes setting, all names are included.
            return !stringMatchingStrategy.containsMatch(getExcludes(), name) &&
                    (getIncludes().isEmpty() || stringMatchingStrategy.containsMatch(getIncludes(), name));
        };
    }

    private interface StringMatchingStrategy {
        boolean containsMatch(ImmutableSet<String> matchExpressions, String metricName);
    }

    private static class DefaultStringMatchingStrategy implements StringMatchingStrategy {
        @Override
        public boolean containsMatch(ImmutableSet<String> matchExpressions, String metricName) {
            return matchExpressions.contains(metricName);
        }
    }

    private static class RegexStringMatchingStrategy implements StringMatchingStrategy {
        private final LoadingCache<String, Pattern> patternCache;

        private RegexStringMatchingStrategy() {
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
}
