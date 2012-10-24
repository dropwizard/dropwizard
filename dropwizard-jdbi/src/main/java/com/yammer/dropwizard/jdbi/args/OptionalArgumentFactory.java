package com.yammer.dropwizard.jdbi.args;

import com.google.common.base.Optional;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.Argument;
import org.skife.jdbi.v2.tweak.ArgumentFactory;

public class OptionalArgumentFactory implements ArgumentFactory<Optional<Object>> {
    private final String jdbcDriver;

    public OptionalArgumentFactory(String jdbcDriver) {
        this.jdbcDriver = jdbcDriver;
    }

    @Override
    public boolean accepts(Class<?> expectedType, Object value, StatementContext ctx) {
        return value instanceof Optional;
    }

    @Override
    public Argument build(Class<?> expectedType, Optional<Object> value, StatementContext ctx) {
        return new OptionalArgument(value, jdbcDriver);
    }
}
