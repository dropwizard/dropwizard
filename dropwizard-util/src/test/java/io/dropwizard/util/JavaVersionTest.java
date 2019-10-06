package io.dropwizard.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JavaVersionTest {
    @Test
    void isJava8_returns_false_if_specVersion_cannot_be_read() {
        System.clearProperty("java.specification.version");
        assertThat(JavaVersion.isJava8()).isFalse();
    }

    @Test
    void isJava8_returns_true_if_specVersion_is_Java_8() {
        System.setProperty("java.specification.version", "1.8.0_222");
        assertThat(JavaVersion.isJava8()).isTrue();
    }

    @Test
    void isJava8_returns_true_if_specVersion_is_Java_11() {
        System.setProperty("java.specification.version", "11");
        assertThat(JavaVersion.isJava8()).isFalse();
    }
}