package io.dropwizard.metrics;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;
import java.util.Locale;

/**
 * A base {@link ReporterFactory} for configuring metric reporters with formatting options.
 * <p/>
 * Configures formatting options common to some {@link com.codahale.metrics.ScheduledReporter}s.
 * <p/>
 * <b>Configuration Parameters:</b>
 * <table>
 *     <tr>
 *         <td>Name</td>
 *         <td>Default</td>
 *         <td>Description</td>
 *     </tr>
 *     <tr>
 *         <td>locale</td>
 *         <td>System default {@link Locale}.</td>
 *         <td>The {@link Locale} for formatting numbers, dates and times.</td>
 *     </tr>
 *     <tr>
 *         <td colspan="3">See {@link BaseReporterFactory} for more options.</td>
 *     </tr>
 * </table>
 */
public abstract class BaseFormattedReporterFactory extends BaseReporterFactory {
    @NotNull
    private Locale locale = Locale.getDefault();

    @JsonProperty
    public Locale getLocale() {
        return locale;
    }

    @JsonProperty
    public void setLocale(Locale locale) {
        this.locale = locale;
    }
}
