package io.dropwizard.jackson;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

class BaseObjectMapperFactoryTest {
    private final ObjectMapperFactory objectMapperFactory = new BaseObjectMapperFactory() {
    };

    @Test
    void objectMapperUsesGivenCustomJsonFactory() {
        JsonFactory factory = Mockito.mock(JsonFactory.class);

        ObjectMapper mapper = objectMapperFactory.newObjectMapper(factory);

        assertThat(mapper.getFactory()).isSameAs(factory);
    }

    @Test
    void objectMapperCanHandleNullInsteadOfCustomJsonFactory() {
        ObjectMapper mapper = objectMapperFactory.newObjectMapper(null);

        assertThat(mapper.getFactory()).isNotNull();
    }

    @Test
    void objectMapperCanDeserializeJdk7Types() throws IOException {
        final LogMetadata metadata = new DefaultObjectMapperFactory().newObjectMapper()
                .readValue("{\"path\": \"/var/log/app/server.log\"}", LogMetadata.class);
        assertThat(metadata).isNotNull();
        assertThat(metadata.path).isEqualTo(Paths.get("/var/log/app/server.log"));
    }

    @Test
    void objectMapperSerializesNullValues() throws IOException {
        final ObjectMapper mapper = objectMapperFactory.newObjectMapper();
        final Issue1627 pojo = new Issue1627(null, null);
        final String json = "{\"string\":null,\"uuid\":null}";

        assertThat(mapper.writeValueAsString(pojo)).isEqualTo(json);
    }

    @Test
    void objectMapperIgnoresUnknownProperties() throws JsonProcessingException {
        assertThat(objectMapperFactory.newObjectMapper()
                .readValue("{\"unknown\": 4711, \"path\": \"/var/log/app/objectMapperIgnoresUnknownProperties.log\"}", LogMetadata.class)
                .path)
                .hasFileName("objectMapperIgnoresUnknownProperties.log");
    }

    static class LogMetadata {
        @Nullable
        public Path path;
    }
}
