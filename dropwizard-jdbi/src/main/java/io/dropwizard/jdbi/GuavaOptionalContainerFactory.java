package io.dropwizard.jdbi;

import com.google.common.base.Optional;
import org.skife.jdbi.v2.ContainerBuilder;
import org.skife.jdbi.v2.tweak.ContainerFactory;

public class GuavaOptionalContainerFactory implements ContainerFactory<Optional<?>> {

    @Override
    public boolean accepts(Class<?> type) {
        return Optional.class.isAssignableFrom(type);
    }

    @Override
    public ContainerBuilder<Optional<?>> newContainerBuilderFor(Class<?> type) {
        return new OptionalContainerBuilder();
    }

    private static class OptionalContainerBuilder implements ContainerBuilder<Optional<?>> {

        private Optional<?> optional = Optional.absent();

        @Override
        public ContainerBuilder<Optional<?>> add(Object it) {
            optional = Optional.fromNullable(it);
            return this;
        }

        @Override
        public Optional<?> build() {
            return optional;
        }
    }
}
