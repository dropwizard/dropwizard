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
        // Same username AND password → same hashCode
        assertThat(credentials.hashCode())
            .hasSameHashCodeAs(new BasicCredentials("u", "p"));

        // Different username → different hashCode (username drives the hash)
        assertThat(credentials.hashCode())
            .isNotEqualTo(new BasicCredentials("u1", "p").hashCode());

        // Different password with the same username → same hashCode by design.
        // Password is excluded from hashCode() to force all same-username lookups
        // through the constant-time equals() path (see BasicCredentials#hashCode javadoc).
        assertThat(credentials.hashCode())
            .isEqualTo(new BasicCredentials("u", "p1").hashCode());
    }

    /**
     * Verifies that the password is excluded from {@code hashCode()} so that
     * same-username credentials always fall into the same {@link java.util.HashMap}
     * bucket, guaranteeing that the constant-time {@code equals()} path is reached
     * for every authentication attempt in {@link io.dropwizard.auth.CachingAuthenticator}.
     */
    @Test
    void hashCodeExcludesPassword() {
        final BasicCredentials sameUserDiffPass1 = new BasicCredentials("alice", "correcthorsebattery");
        final BasicCredentials sameUserDiffPass2 = new BasicCredentials("alice", "wrongpassword");
        final BasicCredentials differentUser    = new BasicCredentials("bob",   "correcthorsebattery");

        assertThat(sameUserDiffPass1.hashCode())
            .as("credentials with the same username must hash to the same bucket regardless of password")
            .isEqualTo(sameUserDiffPass2.hashCode());

        assertThat(sameUserDiffPass1.hashCode())
            .as("credentials with a different username must not hash to the same bucket")
            .isNotEqualTo(differentUser.hashCode());
    }

    @Test
    void isHumanReadable() {
        assertThat(credentials).hasToString("BasicCredentials{username=u, password=**********}");
    }
}
