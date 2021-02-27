package io.dropwizard.jersey.optional;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class OptionalDoubleParamConverterProviderTest {
    @Test
    void verifyInvalidDefaultValueFailsFast() {
        assertThatExceptionOfType(NumberFormatException.class)
            .isThrownBy(() -> new OptionalDoubleParamConverterProvider.OptionalDoubleParamConverter("invalid").fromString("invalid"));
    }

    @Test
    void verifyInvalidValueNoDefaultReturnsNotPresent() {
        assertThat(new OptionalDoubleParamConverterProvider.OptionalDoubleParamConverter().fromString("invalid")).isNotPresent();
    }
}
