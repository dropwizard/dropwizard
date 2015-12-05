package io.dropwizard.jdbi.args;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultColumnMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.OptionalInt;

/**
 * A {@link ResultColumnMapper} to map {@link OptionalInt} objects.
 */
public class OptionalIntMapper implements ResultColumnMapper<OptionalInt> {
    @Override
    public OptionalInt mapColumn(ResultSet r, int columnNumber, StatementContext ctx) throws SQLException {
        final int value = r.getInt(columnNumber);
        return r.wasNull() ? OptionalInt.empty() : OptionalInt.of(value);
    }

    @Override
    public OptionalInt mapColumn(ResultSet r, String columnLabel, StatementContext ctx) throws SQLException {
        final int value = r.getInt(columnLabel);
        return r.wasNull() ? OptionalInt.empty() : OptionalInt.of(value);
    }
}
