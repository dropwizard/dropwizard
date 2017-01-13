package io.dropwizard.auth.basic;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class BasicCredentialsTest {
    private final BasicCredentials credentials = new BasicCredentials("u", "p");

    @Test
    public void hasAUsername() throws Exception {
        assertThat(credentials.getUsername()).isEqualTo("u");
    }

    @Test
    public void hasAPassword() throws Exception {
        assertThat(credentials.getPassword()).isEqualTo("p");
    }

    @Test
    @SuppressWarnings({ "ObjectEqualsNull", "EqualsBetweenInconvertibleTypes", "LiteralAsArgToStringEquals" })
    public void hasAWorkingEqualsMethod() throws Exception {
        assertThat(credentials).isEqualTo(credentials);
        assertThat(credentials).isEqualTo(new BasicCredentials("u", "p"));
        assertThat(credentials).isNotEqualTo(null);
        assertThat(credentials).isNotEqualTo("string");
        assertThat(credentials).isNotEqualTo(new BasicCredentials("u1", "p"));
        assertThat(credentials).isNotEqualTo(new BasicCredentials("u", "p1"));
    }

    @Test
    public void hasAWorkingHashCode() throws Exception {
        assertThat(credentials.hashCode()).isEqualTo(new BasicCredentials("u", "p").hashCode());
        assertThat(credentials.hashCode()).isNotEqualTo(new BasicCredentials("u1", "p").hashCode());
        assertThat(credentials.hashCode()).isNotEqualTo(new BasicCredentials("u", "p1").hashCode());
    }

    @Test
    public void isHumanReadable() throws Exception {
        assertThat(credentials.toString()).isEqualTo("BasicCredentials{username=u, password=**********}");
    }
}