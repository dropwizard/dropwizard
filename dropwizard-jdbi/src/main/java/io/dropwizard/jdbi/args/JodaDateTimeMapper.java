package io.dropwizard.jdbi.args;

import org.joda.time.DateTime;
import org.skife.jdbi.v2.util.TypedMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * A {@link TypedMapper} to map Joda {@link DateTime} objects.
 */
public class JodaDateTimeMapper extends TypedMapper<DateTime> {

    @Override
    protected DateTime extractByName(final ResultSet r, final String name) throws SQLException {
        return new DateTime(r.getTimestamp(name).getTime());
    }

    @Override
    protected DateTime extractByIndex(final ResultSet r, final int index) throws SQLException {
        return new DateTime(r.getTimestamp(index).getTime());
    }
}
