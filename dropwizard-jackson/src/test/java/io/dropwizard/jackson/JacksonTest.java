package io.dropwizard.jackson;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;

class JacksonTest {
    @Test
    void objectMapperUsesGivenCustomJsonFactory() {
        JsonFactory factory = Mockito.mock(JsonFactory.class);

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
    void objectMapperIgnoresUnknownProperties() {
        assertThatCode(() ->
            Jackson.newObjectMapper()
                .readValue("{\"unknown\": 4711, \"path\": \"/var/log/app/server.log\"}", LogMetadata.class)
        ).doesNotThrowAnyException();
    }

    static class LogMetadata {

        @Nullable
        public Path path;
    }

}
