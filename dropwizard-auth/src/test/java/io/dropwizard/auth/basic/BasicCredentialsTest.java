package io.dropwizard.auth.basic;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class BasicCredentialsTest {
    private final BasicCredentials credentials = new BasicCredentials("u", "p");

    @Test
    public void hasAUsername() {
        assertThat(credentials.getUsername()).isEqualTo("u");
    }

    @Test
    public void hasAPassword() {
        assertThat(credentials.getPassword()).isEqualTo("p");
    }

    @Test
    @SuppressWarnings({ "ObjectEqualsNull", "LiteralAsArgToStringEquals" })
    public void hasAWorkingEqualsMethod() {
        assertThat(credentials)
            .isEqualTo(credentials)
            .isEqualTo(new BasicCredentials("u", "p"))
            .isNotEqualTo(null)
            .isNotEqualTo("string")
            .isNotEqualTo(new BasicCredentials("u1", "p"))
            .isNotEqualTo(new BasicCredentials("u", "p1"));
    }

    @Test
    public void hasAWorkingHashCode() {
        assertThat(credentials.hashCode())
            .hasSameHashCodeAs(new BasicCredentials("u", "p"))
            .isNotEqualTo(new BasicCredentials("u1", "p").hashCode())
            .isNotEqualTo(new BasicCredentials("u", "p1").hashCode());
    }

    @Test
    public void isHumanReadable() {
        assertThat(credentials).hasToString("BasicCredentials{username=u, password=**********}");
    }
}
