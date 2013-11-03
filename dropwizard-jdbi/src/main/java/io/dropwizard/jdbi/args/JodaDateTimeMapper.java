package io.dropwizard.jdbi.args;

import org.joda.time.DateTime;
import org.skife.jdbi.v2.util.TypedMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * A {@link TypedMapper} to map Joda {@link DateTime} objects.
 */
public class JodaDateTimeMapper extends TypedMapper<DateTime> {

    @Override
    protected DateTime extractByName(final ResultSet r, final String name) throws SQLException {
        final Timestamp timestamp = r.getTimestamp(name);
        if (timestamp == null) {
            return null;
        }
        return new DateTime(timestamp.getTime());
    }

    @Override
    protected DateTime extractByIndex(final ResultSet r, final int index) throws SQLException {
        final Timestamp timestamp = r.getTimestamp(index);
        if (timestamp == null) {
            return null;
        }
        return new DateTime(timestamp.getTime());
    }
}
