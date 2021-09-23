package io.dropwizard.jersey.optional;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class OptionalIntParamConverterProviderTest {
    @Test
    void verifyInvalidDefaultValueFailsFast() {
        OptionalIntParamConverterProvider.OptionalIntParamConverter converter =
            new OptionalIntParamConverterProvider.OptionalIntParamConverter("invalid");
        assertThatExceptionOfType(NumberFormatException.class)
            .isThrownBy(() -> converter.fromString("invalid"));
    }

    @Test
    void verifyInvalidValueNoDefaultReturnsNotPresent() {
        assertThat(new OptionalIntParamConverterProvider.OptionalIntParamConverter().fromString("invalid")).isNotPresent();
    }
}
