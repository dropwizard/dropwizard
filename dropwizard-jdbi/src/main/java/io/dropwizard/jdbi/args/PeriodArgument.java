package io.dropwizard.jdbi.args;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.Argument;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.time.Period;

/**
 * An {@link Argument} for {@link Period} objects.
 */
public class PeriodArgument implements Argument {
    private final Period period;

    protected PeriodArgument(final Period period) {
        this.period = period;
    }

    @Override
    public void apply(int position, PreparedStatement statement, StatementContext ctx) throws SQLException {
        if (period != null) {
            statement.setString(position, period.toString());
        } else {
            statement.setNull(position, Types.VARCHAR);
        }
    }
}
