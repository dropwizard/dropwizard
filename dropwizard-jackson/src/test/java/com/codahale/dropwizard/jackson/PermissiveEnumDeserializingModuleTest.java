package com.codahale.dropwizard.jackson;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import java.sql.ClientInfoStatus;
import java.util.concurrent.TimeUnit;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Tests {@link PermissiveEnumDeserializingModule}.
 */
public class PermissiveEnumDeserializingModuleTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Before
    public void setUp() throws Exception {
        mapper.registerModule(new PermissiveEnumDeserializingModule());
    }

    @Test
    public void mapsUpperCaseEnums() throws Exception {
        assertThat(mapper.readValue("\"SECONDS\"", TimeUnit.class))
                .isEqualTo(TimeUnit.SECONDS);
    }

    @Test
    public void mapsLowerCaseEnums() throws Exception {
        assertThat(mapper.readValue("\"milliseconds\"", TimeUnit.class))
                .isEqualTo(TimeUnit.MILLISECONDS);
    }

    @Test
    public void mapsPaddedEnums() throws Exception {
        assertThat(mapper.readValue("\"   MINUTES \"", TimeUnit.class))
                .isEqualTo(TimeUnit.MINUTES);
    }

    @Test
    public void mapsSpacedEnums() throws Exception {
        assertThat(mapper.readValue("\"   MILLI SECONDS \"", TimeUnit.class))
                .isEqualTo(TimeUnit.MILLISECONDS);
    }

    @Test
    public void mapsDashedEnums() throws Exception {
        assertThat(mapper.readValue("\"REASON-UNKNOWN\"", ClientInfoStatus.class))
                .isEqualTo(ClientInfoStatus.REASON_UNKNOWN);
    }

    @Test(expected = JsonMappingException.class)
    public void failsOnIncorrectValue() throws Exception {
        mapper.readValue("\"wrong\"", TimeUnit.class);
    }
}
