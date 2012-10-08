package com.yammer.dropwizard.jdbi;

import com.google.common.collect.ImmutableList;
import org.skife.jdbi.v2.ContainerBuilder;
import org.skife.jdbi.v2.tweak.ContainerFactory;

public class ImmutableListContainerFactory implements ContainerFactory<ImmutableList<?>> {
    @Override
    public boolean accepts(Class<?> type) {
        return ImmutableList.class.isAssignableFrom(type);
    }

    @Override
    public ContainerBuilder<ImmutableList<?>> newContainerBuilderFor(Class<?> type) {
        return new ImmutableListContainerBuilder();
    }

    private static class ImmutableListContainerBuilder implements ContainerBuilder<ImmutableList<?>> {
        private final ImmutableList.Builder<Object> builder = ImmutableList.builder();

        @Override
        public ContainerBuilder<ImmutableList<?>> add(Object it) {
            builder.add(it);
            return this;
        }

        @Override
        public ImmutableList<?> build() {
            return builder.build();
        }
    }
}
