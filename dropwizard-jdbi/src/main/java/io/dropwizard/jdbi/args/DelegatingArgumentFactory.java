package io.dropwizard.jdbi.args;

import com.google.common.collect.ImmutableList;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.Argument;
import org.skife.jdbi.v2.tweak.ArgumentFactory;

import java.util.Collection;
import java.util.List;

public class DelegatingArgumentFactory implements ArgumentFactory {
    private final List<ArgumentFactory> argumentFactories;

    public DelegatingArgumentFactory(Collection<ArgumentFactory> argumentFactories) {
        this.argumentFactories = ImmutableList.copyOf(argumentFactories);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean accepts(Class expectedType, Object value, StatementContext ctx) {
        for (ArgumentFactory argumentFactory : argumentFactories) {
            if (argumentFactory.accepts(expectedType, value, ctx)) {
                return true;
            }
        }

        return false;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Argument build(Class expectedType, Object value, StatementContext ctx) {
        for (ArgumentFactory argumentFactory : argumentFactories) {
            if (argumentFactory.accepts(expectedType, value, ctx)) {
                return argumentFactory.build(expectedType, value, ctx);
            }
        }

        throw new IllegalArgumentException("Type " + expectedType + " not supported.");
    }
}
