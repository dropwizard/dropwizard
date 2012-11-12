package com.yammer.dropwizard.auth.basic.tests;

import com.yammer.dropwizard.auth.basic.BasicCredentials;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class BasicCredentialsTest {
    private final BasicCredentials credentials = new BasicCredentials("u", "p");

    @Test
    public void hasAUsername() throws Exception {
        assertThat(credentials.getUsername())
                .isEqualTo("u");
    }

    @Test
    public void hasAPassword() throws Exception {
        assertThat(credentials.getPassword())
                .isEqualTo("p");
    }

    @Test
    @SuppressWarnings({ "ObjectEqualsNull", "EqualsBetweenInconvertibleTypes", "LiteralAsArgToStringEquals" })
    public void hasAWorkingEqualsMethod() throws Exception {
        assertThat(credentials.equals(credentials))
                .isTrue();

        assertThat(credentials.equals(new BasicCredentials("u", "p")))
                .isTrue();

        assertThat(credentials.equals(null))
                .isFalse();

        assertThat(credentials.equals("string"))
                .isFalse();

        assertThat(credentials.equals(new BasicCredentials("u1", "p")))
                .isFalse();

        assertThat(credentials.equals(new BasicCredentials("u", "p1")))
                .isFalse();
    }

    @Test
    public void hasAWorkingHashCode() throws Exception {
        assertThat(credentials.hashCode())
                .isEqualTo(new BasicCredentials("u", "p").hashCode());

        assertThat(credentials.hashCode())
                .isNotEqualTo(new BasicCredentials("u1", "p").hashCode());

        assertThat(credentials.hashCode())
                .isNotEqualTo(new BasicCredentials("u", "p1").hashCode());
    }

    @Test
    public void isHumanReadable() throws Exception {
        assertThat(credentials.toString())
                .isEqualTo("BasicCredentials{username=u, password=**********}");
    }
}
