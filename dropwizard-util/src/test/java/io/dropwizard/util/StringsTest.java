package io.dropwizard.util;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class StringsTest {

  @Rule public ExpectedException thrown = ExpectedException.none();

  @Test
  public void emptyToNull() {
    Assert.assertEquals("A",  Strings.emptyToNull("A"));
    Assert.assertNull(Strings.emptyToNull(""));
  }

  @Test
  public void isNullOrEmpty() {
    Assert.assertEquals(false, Strings.isNullOrEmpty("A"));
    Assert.assertEquals(true, Strings.isNullOrEmpty(""));
  }

  @Test
  public void nullToEmpty() {
    Assert.assertEquals("", Strings.nullToEmpty(""));
    Assert.assertEquals("", Strings.nullToEmpty(null));
  }

  @Test
  public void repeatIllegalArgumentException() {
    thrown.expect(IllegalArgumentException.class);
    Strings.repeat("", -2_147_483_647);
    // Method is not expected to return due to exception thrown
  }

  @Test
  public void repeatArrayIndexOutOfBoundsException() {
      thrown.expect(ArrayIndexOutOfBoundsException.class);
      Strings.repeat("00000000", 319_979_524);
      // Method is not expected to return due to exception thrown
  }

  @Test
  public void repeat() {
    Assert.assertEquals("", Strings.repeat("", 0));
    Assert.assertEquals("000000", Strings.repeat("0", 6));
  }
}
