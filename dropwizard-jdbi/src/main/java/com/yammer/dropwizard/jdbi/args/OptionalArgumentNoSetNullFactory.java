package com.yammer.dropwizard.jdbi.args;

import com.google.common.base.Optional;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.Argument;

public class OptionalArgumentNoSetNullFactory extends OptionalArgumentFactory {
    @Override
    public Argument build(Class<?> expectedType, Optional<Object> value, StatementContext ctx) {
        return new OptionalArgumentNoSetNull(value);
    }
}
