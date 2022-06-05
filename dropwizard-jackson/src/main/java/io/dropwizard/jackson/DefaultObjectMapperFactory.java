package io.dropwizard.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.blackbird.BlackbirdModule;

/**
 * Factory for creating instances of the default {@link ObjectMapper} instances for Dropwizard including the
 * <a href="https://github.com/FasterXML/jackson-modules-base/tree/jackson-modules-base-2.13.3/blackbird#readme">Jackson Blackbird module</a>.
 *
 * @see BaseObjectMapperFactory
 * @since 2.1.1
 */
public class DefaultObjectMapperFactory extends BaseObjectMapperFactory {
    @Override
    protected ObjectMapper configure(ObjectMapper mapper) {
        return super.configure(mapper).registerModule(new BlackbirdModule());
    }
}
