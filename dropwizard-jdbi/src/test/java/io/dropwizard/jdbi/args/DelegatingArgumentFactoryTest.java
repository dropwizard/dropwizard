package io.dropwizard.jdbi.args;

import org.junit.Test;
import org.skife.jdbi.v2.tweak.ArgumentFactory;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

public class DelegatingArgumentFactoryTest {
    @Test
    public void emptyDelegatingArgumentFactoryDoesNotAcceptAnything() throws Exception {
        final ArgumentFactory<?> factory = new DelegatingArgumentFactory(Collections.<ArgumentFactory>emptySet());
        assertThat(factory.accepts(String.class, "Test", null)).isFalse();
    }

    @Test(expected = IllegalArgumentException.class)
    @SuppressWarnings("unchecked")
    public void emptyDelegatingArgumentFactoryThrowsIllegalArgumentExceptionOnBuild() throws Exception {
        final ArgumentFactory factory = new DelegatingArgumentFactory(Collections.<ArgumentFactory>emptySet());
        factory.build(String.class, "Test", null);
    }
}
