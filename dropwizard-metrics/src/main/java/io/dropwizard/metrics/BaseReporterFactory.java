package io.dropwizard.metrics;

import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.ScheduledReporter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import io.dropwizard.util.Duration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
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
 *         <td>excludesRegex</td>
 *         <td>No excluded metrics.</td>
 *         <td>Regex pattern of metrics to exclude from reports.  When defined, matching metrics will not be
 *         reported. See {@link #getFilter()}. Augments excludes setting.  If a metric matches either a name
 *         listed in excludes or matches this regex it is excluded.</td>
 *     </tr>
 *     <tr>
 *         <td>includes</td>
 *         <td>All metrics included.</td>
 *         <td>Metrics to include in reports, by name. When defined, only these metrics will be
 *         reported. See {@link #getFilter()}.  Exclusion rules (excludes and excludesRegex) take precedence,
 *         so if a name matches both exclusion rules and inclusion rules, it is excluded.</td>
 *     </tr>
 *     <tr>
 *         <td>includesRegex</td>
 *         <td>All metrics included.</td>
 *         <td>Regex pattern of metrics to include in reports. When defined, only metrics with names matching
 *         this regex OR in the includes list will be reported. See {@link #getFilter()}.  Exclusion rules
 *         (excludes and excludesRegex) take precedence, so if a name matches both exclusion rules and inclusion rules,
 *         it is excluded.</td>
 *     </tr>
 *     <tr>
 *         <td>frequency</td>
 *         <td>1 second</td>
 *         <td>The frequency to report metrics. Overrides the {@link
 *         MetricsFactory#getFrequency() default}.</td>
 *     </tr>
 * </table>
 */
public abstract class BaseReporterFactory implements ReporterFactory {
    @NotNull
    private TimeUnit durationUnit = TimeUnit.MILLISECONDS;

    @NotNull
    private TimeUnit rateUnit = TimeUnit.SECONDS;

    @NotNull
    private ImmutableSet<String> excludes = ImmutableSet.of();

    @NotNull
    private ImmutableSet<String> includes = ImmutableSet.of();

    // Defines the excludesRegex
    private String excludesRegex = null;
    // Cache of the regex Pattern that implements the excludesRegex.
    private Pattern excludesPattern = null;

    // Defines the includesRegex
    private String includesRegex = null;
    // Cache of the regex Pattern that implements the includesRegex.
    private Pattern includesPattern = null;


    @NotNull
    @Valid
    private Optional<Duration> frequency = Optional.absent();

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

    /**
     * List of names to include in reports.
     * @return the configured list of names
     */
    @JsonProperty
    public ImmutableSet<String> getIncludes() {
        return includes;
    }

    @JsonProperty
    public void setIncludes(ImmutableSet<String> includes) {
        this.includes = includes;
    }

    /**
     * Augments {@link #includes} so that any name which matches this regex pattern is also included in reports.
     * @return rhe string representation of a regex pattern
     */
    @JsonProperty
    public String getIncludesRegex() { return includesRegex; }

    @JsonProperty
    public void setIncludesRegex(String includesRegex) {
        this.includesRegex = includesRegex;
        this.includesPattern = null;
    }

    /**
     * Accessor for the internal regex Pattern that does inclusion matching.
     * @return the includesPattern, creating it from the includesRegex String if needed; null if includesRegex is unset
     */
    private Pattern getIncludesPattern() {
        if (includesPattern == null && includesRegex != null) {
            includesPattern = Pattern.compile(includesRegex);
        }
        return includesPattern;
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
    public String getExcludesRegex() { return excludesRegex; }

    @JsonProperty
    public void setExcludesRegex(String excludesRegex) {
        this.excludesRegex = excludesRegex;
        // Reset the associated regex Pattern, causing it to be recreated on next use.
        this.excludesPattern = null;
    }

    private Pattern getExcludesPattern() {
        // create the excludes pattern if needed.
        if (excludesPattern == null && excludesRegex != null) {
            excludesPattern = Pattern.compile(excludesRegex);
        }
        return excludesPattern;
    }


    @JsonProperty
    public Optional<Duration> getFrequency() {
        return frequency;
    }

    @JsonProperty
    public void setFrequency(Optional<Duration> frequency) {
        this.frequency = frequency;
    }

    /**
     * Implement the exclusion rules.
     * <p>
     *     If a name matches either the {@link #excludes} list or the {@link #excludesRegex} it is excluded.
     * </p>
     * <p>
     *     By default - if neither {@link #excludes} nor {@link #excludesRegex} is defined, no metric is excluded.
     * </p>
     * @param name the name of the metric to check against the rules.
     * @return true if the name matches the exclusion rules, false otherwise.
     */
    private boolean metricNameIsExcluded(String name) {
        boolean exclude = getExcludes().contains(name);
        if (!exclude) {
            Pattern p = getExcludesPattern();
            if (p != null) {
                exclude = p.matcher(name).matches();
            }
        }
        return exclude;
    }

    /**
     * Implement the inclusion rules.
     * <p>
     *     If a name matches either the {@link #includes} list or the {@link #includesRegex} it is included.
     * </p>
     * <p>
     *     By default - if neither {@link #includes} nor {@link #includesRegex} is defined, all metrics are included.
     * </p>
     * @param name the name of the metric to check against the rules.
     * @return true if the name matches the inclusion rules, false otherwise.
     */
    private boolean metricNameIsIncluded(String name) {
        boolean include = getIncludes().contains(name);
        if (!include) {
            Pattern p = getIncludesPattern();
            if (p != null) {
                include = p.matcher(name).matches();
            } else {
                // We only do this step AFTER checking the regex, since this default setting of "include all"
                // only applies when there is no inclusion rule defined.
                include = getIncludes().isEmpty();  // no regex, empty includes list means: include all.
            }
        }
        return include;
    }

    /**
     * Gets a {@link MetricFilter} that specifically includes and excludes configured metrics.
     * <p/>
     * Filtering works in 4 ways:
     * <dl>
     *     <dt><i>unfiltered</i></dt>
     *     <dd>All metrics are reported</dd>
     *     <dt><i>excludes</i>-only or <i>excludesRegex</i></dt>
     *     <dd>All metrics are reported, except those whose name is listed in <i>excludes</i> or matching
     *     <i>excludesRegex</i>.</dd>
     *     <dt><i>includes</i>-only or <i>includesRegex</i></dt>
     *     <dd>Only metrics whose a name is listed in <i>includes</i> or matching <i>includesRegex</i> are reported.</dd>
     *     <dt>mixed (both <i>includes</i> (and/or includesRegex) and <i>excludes</i></dt>
     *     <dd>Only metrics whose a name is listed in <i>includes</i> or matching <i>includesRegex</i> and are
     *     <em>not</em> listed in <i>excludes</i> or matching <i>includesRegex</i>, are reported;
     *     exclusion rules (<i>excludes</i> and/or <i>excludesRegex</i>) take precedence.</dd>
     * </dl>
     *
     * @return the filter for selecting metrics based on the configured excludes/includes.
     * @see #getIncludes()
     * @see #getIncludesRegex()
     * @see #getExcludes()
     * @see #getExcludesRegex()
     */
    public MetricFilter getFilter() {
        return new MetricFilter() {
            @Override
            public boolean matches(final String name, final Metric metric) {
                return !metricNameIsExcluded(name) && metricNameIsIncluded(name);
            }
        };
    }

}
