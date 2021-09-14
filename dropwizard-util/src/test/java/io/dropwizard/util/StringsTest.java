package io.dropwizard.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class StringsTest {

  @Test
  void emptyToNull() {
      assertThat(Strings.emptyToNull("A"))
          .isEqualTo("A");
      assertThat(Strings.emptyToNull("")).isNull();
      assertThat(Strings.emptyToNull(null)).isNull();
  }

  @Test
  void isNullOrEmpty() {
      assertThat(Strings.isNullOrEmpty("A")).isFalse();
      assertThat(Strings.isNullOrEmpty("")).isTrue();
      assertThat(Strings.isNullOrEmpty(null)).isTrue();
  }

  @Test
  void nullToEmpty() {
      assertThat(Strings.nullToEmpty("")).isEmpty();
      assertThat(Strings.nullToEmpty(null)).isEmpty();
      assertThat(Strings.nullToEmpty("foo")).isEqualTo("foo");
  }

  @Test
  void repeatExceptions() {
    assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> Strings.repeat("", -2_147_483_647))
        .withMessage("invalid count: -2147483647" );
    assertThatExceptionOfType(ArrayIndexOutOfBoundsException.class).isThrownBy(() -> Strings.repeat("00000000", 319_979_524))
        .withMessage("Required array size too large: 2559836192");
  }


  @Test
  void repeat() {
      assertThat(Strings.repeat("", 0)).isEmpty();
      assertThat(Strings.repeat("0", 6)).isEqualTo("000000");
  }
}
