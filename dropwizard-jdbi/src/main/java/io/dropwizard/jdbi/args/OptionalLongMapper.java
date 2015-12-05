package io.dropwizard.jdbi.args;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultColumnMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.OptionalLong;

/**
 * A {@link ResultColumnMapper} to map {@link OptionalLong} objects.
 */
public class OptionalLongMapper implements ResultColumnMapper<OptionalLong> {
    @Override
    public OptionalLong mapColumn(ResultSet r, int columnNumber, StatementContext ctx) throws SQLException {
        final long value = r.getLong(columnNumber);
        return r.wasNull() ? OptionalLong.empty() : OptionalLong.of(value);
    }

    @Override
    public OptionalLong mapColumn(ResultSet r, String columnLabel, StatementContext ctx) throws SQLException {
        final long value = r.getLong(columnLabel);
        return r.wasNull() ? OptionalLong.empty() : OptionalLong.of(value);
    }
}
