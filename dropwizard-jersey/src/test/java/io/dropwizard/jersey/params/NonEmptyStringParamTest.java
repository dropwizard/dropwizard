package io.dropwizard.jersey.params;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class NonEmptyStringParamTest {
    @Test
    void aBlankStringIsAnAbsentString() {
        final NonEmptyStringParam param = new NonEmptyStringParam("");
        assertThat(param.get()).isNotPresent();
    }

    @Test
    void aNullStringIsAnAbsentString() {
        final NonEmptyStringParam param = new NonEmptyStringParam(null);
        assertThat(param.get()).isNotPresent();
    }

    @Test
    void aStringWithContentIsItself() {
        final NonEmptyStringParam param = new NonEmptyStringParam("hello");
        assertThat(param.get()).isEqualTo(Optional.of("hello"));
    }
}
