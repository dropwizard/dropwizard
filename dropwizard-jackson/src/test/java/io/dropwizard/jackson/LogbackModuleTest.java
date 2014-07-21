package io.dropwizard.jackson;

import ch.qos.logback.classic.Level;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class LogbackModuleTest {
    private final ObjectMapper mapper = new ObjectMapper();

    @Before
    public void setUp() throws Exception {
        mapper.registerModule(new LogbackModule());
    }

    @Test
    public void mapsStringsToLevels() throws Exception {
        assertThat(mapper.readValue("\"info\"", Level.class))
                .isEqualTo(Level.INFO);
    }

    @Test
    public void mapsFalseToOff() throws Exception {
        assertThat(mapper.readValue("\"false\"", Level.class))
                .isEqualTo(Level.OFF);
    }

    @Test
    public void mapsTrueToAll() throws Exception {
        assertThat(mapper.readValue("\"true\"", Level.class))
                .isEqualTo(Level.ALL);
    }
}
