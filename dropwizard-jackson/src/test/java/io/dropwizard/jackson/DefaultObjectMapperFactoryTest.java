package io.dropwizard.jackson;

import com.fasterxml.jackson.module.blackbird.BlackbirdModule;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultObjectMapperFactoryTest {
    private final ObjectMapperFactory objectMapperFactory = new DefaultObjectMapperFactory();

    @Test
    void objectMapperFactoryRegistersBlackbird() {
        assertThat(objectMapperFactory.newObjectMapper().getRegisteredModuleIds())
                .contains(BlackbirdModule.class.getCanonicalName());
    }
}
