package io.dropwizard.jackson;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;

public class JacksonTest
{
    @Test
    public void objectMapperUsesGivenCustomJsonFactory() {
        JsonFactory factory = Mockito.mock(JsonFactory.class);

        ObjectMapper mapper = Jackson.newObjectMapper(factory);

        assertThat(mapper.getFactory()).isSameAs(factory);
    }

}
