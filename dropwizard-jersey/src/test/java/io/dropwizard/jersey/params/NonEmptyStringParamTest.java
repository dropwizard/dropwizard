package io.dropwizard.jersey.params;

import org.junit.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class NonEmptyStringParamTest {
    @Test
    public void aBlankStringIsAnAbsentString() throws Exception {
        final NonEmptyStringParam param = new NonEmptyStringParam("");
        assertThat(param.get()).isEqualTo(Optional.empty());
    }

    @Test
    public void aNullStringIsAnAbsentString() throws Exception {
        final NonEmptyStringParam param = new NonEmptyStringParam(null);
        assertThat(param.get()).isEqualTo(Optional.empty());
    }

    @Test
    public void aStringWithContentIsItself() throws Exception {
        final NonEmptyStringParam param = new NonEmptyStringParam("hello");
        assertThat(param.get()).isEqualTo(Optional.of("hello"));
    }
}
