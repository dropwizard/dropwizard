package io.dropwizard.jdbi.args;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultColumnMapper;

import javax.annotation.Nullable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Optional;
import java.util.TimeZone;

/**
 * A {@link ResultColumnMapper} to map {@link ZonedDateTime} objects.
 */
public class ZonedDateTimeMapper implements ResultColumnMapper<ZonedDateTime> {

    /**
     * <p>{@link Calendar} for representing a database time zone.<p>
     * If a field is not represented in a database as
     * {@code TIMESTAMP WITH TIME ZONE}, we need to set its time zone
     * explicitly. Otherwise it will not be correctly represented in
     * a time zone different from the time zone of the database.
     */
    private Optional<Calendar> calendar;

    public ZonedDateTimeMapper() {
        calendar = Optional.empty();
    }

    public ZonedDateTimeMapper(Optional<TimeZone> timeZone) {
        calendar = timeZone.map(GregorianCalendar::new);
    }

    /**
     * Make a clone of a calendar.
     * <p>Despite the fact that {@link Calendar} is used only for
     * representing a time zone, some JDBC drivers actually use it
     * for time calculations,</p>
     * <p>Also {@link Calendar} is not immutable, which makes it
     * thread-unsafe. Therefore we need to make a copy to avoid
     * state mutation problems.</p>
     *
     * @return a clone of calendar, representing a database time zone
     */
    private Optional<Calendar> cloneCalendar() {
        return calendar.map(Calendar::clone).map(x -> (Calendar)x);
    }

    @Override
    @Nullable
    public ZonedDateTime mapColumn(ResultSet r, int columnNumber, StatementContext ctx) throws SQLException {
        final Optional<Calendar> instance = cloneCalendar();
        final Timestamp timestamp = instance.isPresent() ? r.getTimestamp(columnNumber, instance.get()) :
            r.getTimestamp(columnNumber);
        return convertToZonedDateTime(timestamp);
    }

    @Override
    @Nullable
    public ZonedDateTime mapColumn(ResultSet r, String columnLabel, StatementContext ctx) throws SQLException {
        final Optional<Calendar> instance = cloneCalendar();
        final Timestamp timestamp = instance.isPresent() ? r.getTimestamp(columnLabel, instance.get()) :
            r.getTimestamp(columnLabel);
        return convertToZonedDateTime(timestamp);
    }

    @Nullable
    private ZonedDateTime convertToZonedDateTime(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }
        final Optional<ZoneId> zoneId = calendar.flatMap(c -> Optional.of(c.getTimeZone().toZoneId()));
        return ZonedDateTime.ofInstant(
            Instant.ofEpochSecond(timestamp.getTime() / 1000, timestamp.getNanos()),
            zoneId.orElse(ZoneId.systemDefault()));
    }
}
