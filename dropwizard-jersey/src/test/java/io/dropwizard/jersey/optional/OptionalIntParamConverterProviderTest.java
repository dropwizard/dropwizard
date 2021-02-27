package io.dropwizard.jersey.optional;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class OptionalIntParamConverterProviderTest {
    @Test
    void verifyInvalidDefaultValueFailsFast() {
        assertThatExceptionOfType(NumberFormatException.class)
            .isThrownBy(() -> new OptionalIntParamConverterProvider.OptionalIntParamConverter("invalid").fromString("invalid"));
    }

    @Test
    void verifyInvalidValueNoDefaultReturnsNotPresent() {
        assertThat(new OptionalIntParamConverterProvider.OptionalIntParamConverter().fromString("invalid")).isNotPresent();
    }
}
