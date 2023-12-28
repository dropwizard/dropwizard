package io.dropwizard.auth.basic;

import com.google.common.testing.EqualsTester;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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
    void hasAWorkingEqualsMethod() {
        new EqualsTester()
            .addEqualityGroup(credentials, new BasicCredentials("u", "p"))
            .addEqualityGroup(new BasicCredentials("u1", "p"))
            .addEqualityGroup(new BasicCredentials("u", "p1"))
            .testEquals();
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
