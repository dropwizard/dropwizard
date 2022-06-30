package io.dropwizard.jackson;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

class JacksonTest {
    @Test
    void objectMapperUsesGivenCustomJsonFactory() {
        JsonFactory factory = new JsonFactory();

        ObjectMapper mapper = Jackson.newObjectMapper(factory);

        assertThat(mapper.getFactory()).isSameAs(factory);
    }

    @Test
    void objectMapperCanHandleNullInsteadOfCustomJsonFactory() {
        ObjectMapper mapper = Jackson.newObjectMapper(null);

        assertThat(mapper.getFactory()).isNotNull();
    }

    @Test
    void objectMapperCanDeserializeJdk7Types() throws IOException {
        final LogMetadata metadata = Jackson.newObjectMapper()
            .readValue("{\"path\": \"/var/log/app/server.log\"}", LogMetadata.class);
        assertThat(metadata).isNotNull();
        assertThat(metadata.path).isEqualTo(Paths.get("/var/log/app/server.log"));
    }

    @Test
    void objectMapperSerializesNullValues() throws IOException {
        final ObjectMapper mapper = Jackson.newObjectMapper();
        final Issue1627 pojo = new Issue1627(null, null);
        final String json = "{\"string\":null,\"uuid\":null}";

        assertThat(mapper.writeValueAsString(pojo)).isEqualTo(json);
    }

    @Test
    void objectMapperIgnoresUnknownProperties() throws JsonProcessingException {
        assertThat(Jackson.newObjectMapper()
                .readValue("{\"unknown\": 4711, \"path\": \"/var/log/app/objectMapperIgnoresUnknownProperties.log\"}", LogMetadata.class)
                .path)
            .hasFileName("objectMapperIgnoresUnknownProperties.log");
    }

    @Test
    void objectMapperRegistersAfterburnerButNotBlackbird() {
        assertThat(Jackson.newObjectMapper().getRegisteredModuleIds())
                .contains("com.fasterxml.jackson.module.afterburner.AfterburnerModule")
                .doesNotContain("com.fasterxml.jackson.module.blackbird.BlackbirdModule");
    }

    static class LogMetadata {

        @Nullable
        public Path path;
    }

}
