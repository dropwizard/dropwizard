package io.dropwizard.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.CaffeineSpec;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CaffeineModuleTest {
    private final ObjectMapper mapper = new ObjectMapper().registerModule(new CaffeineModule());

    @Test
    public void canDeserializeCacheBuilderSpecs() throws Exception {
        assertThat(mapper.readValue("\"maximumSize=30\"", CaffeineSpec.class))
                .isEqualTo(CaffeineSpec.parse("maximumSize=30"));
    }

    @Test
    public void canSerializeCacheBuilderSpecs() throws Exception {
        assertThat(mapper.writeValueAsString(CaffeineSpec.parse("maximumSize=0"))).isEqualTo("\"maximumSize=0\"");
    }
}
