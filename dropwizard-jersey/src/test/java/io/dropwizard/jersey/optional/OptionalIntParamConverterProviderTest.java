package io.dropwizard.jersey.optional;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OptionalIntParamConverterProviderTest {
    @Test
    public void verifyInvalidDefaultValueFailsFast() {
        assertThatThrownBy(() -> new OptionalIntParamConverterProvider.OptionalIntParamConverter("invalid").fromString("invalid"))
            .isInstanceOf(NumberFormatException.class);
    }

    @Test
    public void verifyInvalidValueNoDefaultReturnsNotPresent() {
        assertThat(new OptionalIntParamConverterProvider.OptionalIntParamConverter().fromString("invalid")).isNotPresent();
    }
}
