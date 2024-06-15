package io.dropwizard.jersey;

import org.glassfish.jersey.internal.inject.Providers;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Demonstrates that DropwizardResourceConfig.register needs to include an extra condition for hk2 binder, but not for
 * jersey Binder as it will be picked up as a Provider
 */
class ProvidersBinderTest {

    @Test
    void demonstrateThatHk2BinderIsPickedUpAsProvider() {
        org.glassfish.hk2.utilities.Binder binder = new org.glassfish.hk2.utilities.binding.AbstractBinder() {
            @Override
            protected void configure() {

            }
        };
        assertThat(Providers.isProvider(binder.getClass())).isTrue();
    }

    @Test
    void demonstrateThatJerseyBinderIsPickedUpAsProvider() {
        org.glassfish.jersey.internal.inject.Binder binder = new org.glassfish.jersey.internal.inject.AbstractBinder() {
            @Override
            protected void configure() {

            }
        };
        assertThat(Providers.isProvider(binder.getClass())).isTrue();
    }
}
