package io.dropwizard.auth.basic;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class BasicCredentialsTest {
    private final BasicCredentials credentials = new BasicCredentials("u", "p");

    @Test
    void hasAUsername() {
        assertThat(credentials.getUsername()).isEqualTo("u");
    }

    @Test
    void hasAPassword() {
        assertThat(credentials.getPassword()).isEqualTo("p");
    }

    @Test
    @SuppressWarnings({"ObjectEqualsNull", "LiteralAsArgToStringEquals"})
    void hasAWorkingEqualsMethod() {
        assertThat(credentials)
                .isEqualTo(credentials)
                .isEqualTo(new BasicCredentials("u", "p"))
                .isNotEqualTo(null)
                .isNotEqualTo("string")
                .isNotEqualTo(new BasicCredentials("u1", "p"))
                .isNotEqualTo(new BasicCredentials("u", "p1"));
    }

    @Test
    void hasAWorkingHashCode() {
        assertThat(credentials.hashCode())
                .hasSameHashCodeAs(new BasicCredentials("u", "p"))
                .isNotEqualTo(new BasicCredentials("u1", "p").hashCode())
                .isNotEqualTo(new BasicCredentials("u", "p1").hashCode());
    }

    @Test
    void isHumanReadable() {
        assertThat(credentials).hasToString("BasicCredentials{username=u, password=**********}");
    }
}
