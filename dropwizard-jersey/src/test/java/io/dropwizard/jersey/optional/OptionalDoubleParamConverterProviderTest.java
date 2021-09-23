package io.dropwizard.jersey.optional;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class OptionalDoubleParamConverterProviderTest {
    @Test
    void verifyInvalidDefaultValueFailsFast() {
        OptionalDoubleParamConverterProvider.OptionalDoubleParamConverter converter =
            new OptionalDoubleParamConverterProvider.OptionalDoubleParamConverter("invalid");
        assertThatExceptionOfType(NumberFormatException.class)
            .isThrownBy(() -> converter.fromString("invalid"));
    }

    @Test
    void verifyInvalidValueNoDefaultReturnsNotPresent() {
        assertThat(new OptionalDoubleParamConverterProvider.OptionalDoubleParamConverter().fromString("invalid")).isNotPresent();
    }
}
