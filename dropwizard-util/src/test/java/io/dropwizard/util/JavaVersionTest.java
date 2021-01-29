package io.dropwizard.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnJre;
import org.junit.jupiter.api.condition.JRE;

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

    @Test
    @EnabledOnJre({JRE.JAVA_11, JRE.JAVA_15})
    void isJava11_or_higher_returns_true_on_Java_11_15() {
        assertThat(JavaVersion.isJava11OrHigher()).isTrue();
    }

    @Test
    @EnabledOnJre(JRE.JAVA_8)
    void isJava11_or_higher_returns_false_on_Java_8() {
        assertThat(JavaVersion.isJava11OrHigher()).isTrue();
    }
}
