package io.dropwizard.jersey.optional;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OptionalDoubleParamConverterProviderTest {
    @Test
    public void verifyInvalidDefaultValueFailsFast() {
        assertThatThrownBy(() -> new OptionalDoubleParamConverterProvider.OptionalDoubleParamConverter("invalid").fromString("invalid"))
            .isInstanceOf(NumberFormatException.class);
    }

    @Test
    public void verifyInvalidValueNoDefaultReturnsNotPresent() {
        assertThat(new OptionalDoubleParamConverterProvider.OptionalDoubleParamConverter().fromString("invalid")).isNotPresent();
    }
}
