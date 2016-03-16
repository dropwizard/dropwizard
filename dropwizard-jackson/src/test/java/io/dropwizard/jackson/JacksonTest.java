package io.dropwizard.jackson;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

public class JacksonTest
{
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

     static class LogMetadata {

         public Path path;
     }

}
