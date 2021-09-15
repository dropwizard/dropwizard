package io.dropwizard.jersey.optional;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class OptionalLongParamConverterProviderTest {
    @Test
    void verifyInvalidDefaultValueFailsFast() {
        OptionalLongParamConverterProvider.OptionalLongParamConverter converter =
            new OptionalLongParamConverterProvider.OptionalLongParamConverter("invalid");
        assertThatExceptionOfType(NumberFormatException.class)
            .isThrownBy(() -> converter.fromString("invalid"));
    }

    @Test
    void verifyInvalidValueNoDefaultReturnsNotPresent() {
        assertThat(new OptionalLongParamConverterProvider.OptionalLongParamConverter().fromString("invalid")).isNotPresent();
    }
}
