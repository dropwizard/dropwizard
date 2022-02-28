package io.dropwizard.logging.json.layout;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExceptionFormatTest {

    @Test
    void testDefaults() {
        ExceptionFormat ef = new ExceptionFormat();
        assertThat(ef.isRootFirst()).isTrue();
        assertThat(ef.getDepth()).isEqualTo("full");
        assertThat(ef.getEvaluators()).isEmpty();
    }

    @Test
    void testSetDepth() {
        ExceptionFormat ef = new ExceptionFormat();
        assertThat(ef.getDepth()).isEqualTo("full");

        ef.setDepth("short");
        assertThat(ef.getDepth()).isEqualTo("short");

        // Verify depth can be set to a number as well
        ef.setDepth("25");
        assertThat(ef.getDepth()).isEqualTo("25");
    }
}
