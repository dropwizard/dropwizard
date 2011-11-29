package com.yammer.dropwizard.db.args;

import com.google.common.base.Optional;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.Argument;
import org.skife.jdbi.v2.tweak.ArgumentFactory;

public class OptionalArgumentFactory implements ArgumentFactory<Optional<Object>> {
    @Override
    public boolean accepts(Class<? super Optional<Object>> expectedType,
                           Object value,
                           StatementContext ctx) {
        return value instanceof Optional;
    }

    @Override
    public Argument build(Class<? super Optional<Object>> expectedType,
                          Optional<Object> value,
                          StatementContext ctx) {
        return new OptionalArgument(value);
    }
}
