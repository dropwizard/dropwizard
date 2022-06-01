package io.dropwizard.jersey.params;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import org.junit.jupiter.api.Test;

class NonEmptyStringParamTest {
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
