package io.dropwizard.request.logging.filter;

import ch.qos.logback.access.common.spi.IAccessEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.dropwizard.logging.common.filter.FilterFactory;
import jakarta.validation.constraints.NotNull;

import java.util.Collections;
import java.util.Set;

/**
 * @since 2.0
 */
@JsonTypeName("uri")
public class UriFilterFactory implements FilterFactory<IAccessEvent> {
    @NotNull
    private Set<String> uris = Collections.emptySet();

    @JsonProperty
    public Set<String> getUris() {
        return uris;
    }

    @JsonProperty
    public void setUris(final Set<String> uris) {
        this.uris = uris;
    }

    @Override
    public Filter<IAccessEvent> build() {
        return new Filter<>() {
            @Override
            public FilterReply decide(final IAccessEvent event) {
                if (uris.contains(event.getRequestURI())) {
                    return FilterReply.DENY;
                }

                return FilterReply.NEUTRAL;
            }
        };
    }
}
