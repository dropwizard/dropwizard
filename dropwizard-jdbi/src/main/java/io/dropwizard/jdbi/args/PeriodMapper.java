package io.dropwizard.jdbi.args;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultColumnMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Period;

/**
 * A {@link ResultColumnMapper} to map {@link Period} objects.
 */
public class PeriodMapper implements ResultColumnMapper<Period> {

    @Override
    public Period mapColumn(ResultSet r, int columnNumber, StatementContext ctx) throws SQLException {
        final String period = r.getString(columnNumber);
        if (period == null) {
            return null;
        }
        return Period.parse(period);
    }

    @Override
    public Period mapColumn(ResultSet r, String columnLabel, StatementContext ctx) throws SQLException {
        final String period = r.getString(columnLabel);
        if (period == null) {
            return null;
        }
        return Period.parse(period);
    }
}
