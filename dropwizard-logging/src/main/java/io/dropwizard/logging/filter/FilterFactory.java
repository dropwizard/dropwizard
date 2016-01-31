package io.dropwizard.logging.filter;

import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.DeferredProcessingAware;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.dropwizard.jackson.Discoverable;

/**
 * A service provider interface for creating Logback {@link Filter} instances.
 * <p/>
 * To create your own, just:
 * <ol>
 * <li>Create a class which implements {@link FilterFactory}.</li>
 * <li>Annotate it with {@code @JsonTypeName} and give it a unique type name.</li>
 * <li>add a {@code META-INF/services/io.dropwizard.logging.filter.FilterFactory} file with your
 * implementation's full class name to the class path.</li>
 * </ol>
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
public interface FilterFactory<E extends DeferredProcessingAware> extends Discoverable {

    Filter<E> build();
}
