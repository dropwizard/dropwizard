package io.dropwizard.jersey.optional;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OptionalLongParamConverterProviderTest {
    @Test
    public void verifyInvalidDefaultValueFailsFast() {
        assertThatThrownBy(() -> new OptionalLongParamConverterProvider.OptionalLongParamConverter("invalid").fromString("invalid"))
            .isInstanceOf(NumberFormatException.class);
    }

    @Test
    public void verifyInvalidValueNoDefaultReturnsNotPresent() {
        assertThat(new OptionalLongParamConverterProvider.OptionalLongParamConverter().fromString("invalid")).isNotPresent();
    }
}
