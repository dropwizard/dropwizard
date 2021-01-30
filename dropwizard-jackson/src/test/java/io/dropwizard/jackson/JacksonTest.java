package io.dropwizard.jackson;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnJre;
import org.junit.jupiter.api.condition.JRE;
import org.mockito.Mockito;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;

public class JacksonTest {
    @Test
    public void objectMapperUsesGivenCustomJsonFactory() {
        JsonFactory factory = Mockito.mock(JsonFactory.class);

        ObjectMapper mapper = Jackson.newObjectMapper(factory);

        assertThat(mapper.getFactory()).isSameAs(factory);
    }

    @Test
    public void objectMapperCanHandleNullInsteadOfCustomJsonFactory() {
        ObjectMapper mapper = Jackson.newObjectMapper(null);

        assertThat(mapper.getFactory()).isNotNull();
    }

    @Test
    public void objectMapperCanDeserializeJdk7Types() throws IOException {
        final LogMetadata metadata = Jackson.newObjectMapper()
            .readValue("{\"path\": \"/var/log/app/server.log\"}", LogMetadata.class);
        assertThat(metadata).isNotNull();
        assertThat(metadata.path).isEqualTo(Paths.get("/var/log/app/server.log"));
    }

    @Test
    public void objectMapperSerializesNullValues() throws IOException {
        final ObjectMapper mapper = Jackson.newObjectMapper();
        final Issue1627 pojo = new Issue1627(null, null);
        final String json = "{\"string\":null,\"uuid\":null}";

        assertThat(mapper.writeValueAsString(pojo)).isEqualTo(json);
    }

    @Test
    public void objectMapperIgnoresUnknownProperties() {
        assertThatCode(() ->
            Jackson.newObjectMapper()
                .readValue("{\"unknown\": 4711, \"path\": \"/var/log/app/server.log\"}", LogMetadata.class)
        ).doesNotThrowAnyException();
    }

    @Test
    void blackbirdIsEnabledOnJdk11() {
        System.setProperty("java.specification.version", "11");

        assertThat(Jackson.newObjectMapper().getRegisteredModuleIds())
            .extracting(Object::toString)
            .contains("com.fasterxml.jackson.module.blackbird.BlackbirdModule");
    }

    @Test
    void blackbirdIsEnabledOnJdk15() {
        System.setProperty("java.specification.version", "15");

        assertThat(Jackson.newObjectMapper().getRegisteredModuleIds())
            .extracting(Object::toString)
            .contains("com.fasterxml.jackson.module.blackbird.BlackbirdModule");
    }

    @Test
    void blackbirdIsNotEnabledOnJdk8() {
        System.setProperty("java.specification.version", "1.8.0_222");

        assertThat(Jackson.newObjectMapper().getRegisteredModuleIds())
            .extracting(Object::toString)
            .isNotEmpty()
            .doesNotContain("com.fasterxml.jackson.module.blackbird.BlackbirdModule");
    }

    static class LogMetadata {

        @Nullable
        public Path path;
    }

}
