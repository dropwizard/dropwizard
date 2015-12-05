package io.dropwizard.jdbi.args;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultColumnMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.OptionalDouble;

/**
 * A {@link ResultColumnMapper} to map {@link OptionalDouble} objects.
 */
public class OptionalDoubleMapper implements ResultColumnMapper<OptionalDouble> {
    @Override
    public OptionalDouble mapColumn(ResultSet r, int columnNumber, StatementContext ctx) throws SQLException {
        final double value = r.getDouble(columnNumber);
        return r.wasNull() ? OptionalDouble.empty() : OptionalDouble.of(value);
    }

    @Override
    public OptionalDouble mapColumn(ResultSet r, String columnLabel, StatementContext ctx) throws SQLException {
        final double value = r.getDouble(columnLabel);
        return r.wasNull() ? OptionalDouble.empty() : OptionalDouble.of(value);
    }
}
