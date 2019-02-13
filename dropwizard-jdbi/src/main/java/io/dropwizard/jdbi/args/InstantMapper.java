package io.dropwizard.jdbi.args;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultColumnMapper;

import javax.annotation.Nullable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Optional;
import java.util.TimeZone;

/**
 * A {@link ResultColumnMapper} to map {@link Instant} objects.
 */
public class InstantMapper implements ResultColumnMapper<Instant> {
    /**
     * <p>{@link Calendar} for representing a database time zone.<p>
     * If a field is not represented in a database as
     * {@code TIMESTAMP WITH TIME ZONE}, we need to set its time zone
     * explicitly. Otherwise it will not be correctly represented in
     * a time zone different from the time zone of the database.
     */
    private final Optional<Calendar> calendar;

    public InstantMapper() {
        this(Optional.empty());
    }

    public InstantMapper(final Optional<TimeZone> tz) {
        this.calendar = tz.map(GregorianCalendar::new);
    }

    @Override
    @Nullable
    public Instant mapColumn(ResultSet r, int columnNumber, StatementContext ctx) throws SQLException {
        final Optional<Calendar> instance = cloneCalendar();
        final Timestamp timestamp = instance.isPresent() ?
                r.getTimestamp(columnNumber, instance.get()) :
                r.getTimestamp(columnNumber);
        return timestamp == null ? null : timestamp.toInstant();
    }

    @Override
    @Nullable
    public Instant mapColumn(ResultSet r, String columnLabel, StatementContext ctx) throws SQLException {
        final Optional<Calendar> instance = cloneCalendar();
        final Timestamp timestamp = instance.isPresent() ?
                r.getTimestamp(columnLabel, instance.get()) :
                r.getTimestamp(columnLabel);
        return timestamp == null ? null : timestamp.toInstant();
    }

    private Optional<Calendar> cloneCalendar() {
        return calendar.map(Calendar::clone).map(x -> (Calendar) x);
    }
}
