package io.dropwizard.jdbi;

import com.google.common.collect.ImmutableSet;
import org.skife.jdbi.v2.ContainerBuilder;
import org.skife.jdbi.v2.tweak.ContainerFactory;

public class ImmutableSetContainerFactory implements ContainerFactory<ImmutableSet<?>> {
    @Override
    public boolean accepts(Class<?> type) {
        return ImmutableSet.class.isAssignableFrom(type);
    }

    @Override
    public ContainerBuilder<ImmutableSet<?>> newContainerBuilderFor(Class<?> type) {
        return new ImmutableSetContainerBuilder();
    }


    private static class ImmutableSetContainerBuilder implements ContainerBuilder<ImmutableSet<?>> {
        private final ImmutableSet.Builder<Object> builder = ImmutableSet.builder();

        @Override
        public ContainerBuilder<ImmutableSet<?>> add(Object it) {
            builder.add(it);
            return this;
        }

        @Override
        public ImmutableSet<?> build() {
            return builder.build();
        }
    }
}
