package io.dropwizard.jdbi.args;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import org.joda.time.DateTime;
import org.skife.jdbi.v2.util.TypedMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * A {@link TypedMapper} to map Joda {@link DateTime} objects.
 */
public class JodaDateTimeMapper extends TypedMapper<DateTime> {

    /**
     * <p>{@link Calendar} for representing a database time zone.<p>
     * If a field is not represented in a database as
     * {@code TIMESTAMP WITH TIME ZONE}, we need to set its time zone
     * explicitly. Otherwise it will not be correctly represented in
     * a time zone different from the time zone of the database.
     */
    private Optional<Calendar> calendar;

    public JodaDateTimeMapper() {
        calendar = Optional.absent();
    }

    public JodaDateTimeMapper(Optional<TimeZone> timeZone) {
        calendar = timeZone.transform(new Function<TimeZone, Calendar>() {
            @Override
            public Calendar apply(TimeZone tz) {
                return new GregorianCalendar(tz);
            }
        });
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
    private Calendar cloneCalendar() {
        return (Calendar) calendar.get().clone();
    }

    @Override
    protected DateTime extractByName(final ResultSet r, final String name) throws SQLException {
        final Timestamp timestamp = calendar.isPresent() ? r.getTimestamp(name, cloneCalendar()) :
                r.getTimestamp(name);
        if (timestamp == null) {
            return null;
        }
        return new DateTime(timestamp.getTime());
    }

    @Override
    protected DateTime extractByIndex(final ResultSet r, final int index) throws SQLException {
        final Timestamp timestamp = calendar.isPresent() ? r.getTimestamp(index, cloneCalendar()) :
                r.getTimestamp(index);
        if (timestamp == null) {
            return null;
        }
        return new DateTime(timestamp.getTime());
    }
}
