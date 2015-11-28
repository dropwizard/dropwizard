package io.dropwizard.jdbi.timestamps;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestRule;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;

import java.util.Random;
import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test for handling translation between DateTime to SQL TIMESTAMP
 * in a different time zone
 */
public class JodaDateTimeSqlTimestampTest {

    private static final DateTimeFormatter ISO_FMT = ISODateTimeFormat.dateTimeNoMillis();

    private static TemporaryFolder temporaryFolder;
    private static DatabaseInTimeZone databaseInTimeZone;
    private static DateTimeZone dbTimeZone;
    private static DBIClient DBIClient;
    @ClassRule
    public static TestRule chain;

    static {
        boolean done = false;
        while (!done) {
            try {
                final TimeZone timeZone = getRandomTimeZone();
                dbTimeZone = DateTimeZone.forTimeZone(timeZone);
                temporaryFolder = new TemporaryFolder();
                databaseInTimeZone = new DatabaseInTimeZone(temporaryFolder, timeZone);
                DBIClient = new DBIClient(timeZone);
                chain = RuleChain.outerRule(temporaryFolder)
                        .around(databaseInTimeZone)
                        .around(DBIClient);
                done = true;
            } catch(IllegalArgumentException e) {
                if (!e.getMessage().contains("is not recognised")) {
                    throw e;
                }
            }
        }
    }

    private Handle handle;
    private FlightDao flightDao;


    private static TimeZone getRandomTimeZone() {
        String[] ids = TimeZone.getAvailableIDs();
        return TimeZone.getTimeZone(ids[new Random().nextInt(ids.length)]);
    }

    @Before
    public void setUp() throws Exception {
        handle = DBIClient.getDbi().open();
        handle.execute("CREATE TABLE flights (" +
                "  flight_id         VARCHAR(5)  PRIMARY KEY," +
                "  departure_airport VARCHAR(3)  NOT NULL," +
                "  arrival_airport   VARCHAR(3)  NOT NULL," +
                "  departure_time    TIMESTAMP   NOT NULL," +
                "  arrival_time      TIMESTAMP   NOT NULL" +
                ")");
        flightDao = handle.attach(FlightDao.class);
    }

    @After
    public void tearDown() throws Exception {
        handle.execute("DROP TABLE flights");
        handle.close();
    }

    @Test
    public void testInsertTimestamp() {
        final DateTime departureTime = ISO_FMT.parseDateTime("2015-04-01T06:00:00-05:00");
        final DateTime arrivalTime = ISO_FMT.parseDateTime("2015-04-01T21:00:00+02:00");
        final int result = flightDao.insert("C1671", "ORD", "DUS", departureTime, arrivalTime);
        assertThat(result).isGreaterThan(0);

        final Integer serverDepartureHour = (Integer) handle.select(
                "SELECT EXTRACT(HOUR FROM departure_time) departure_hour " +
                        "FROM flights WHERE flight_id=?", "C1671").get(0).get("departure_hour");
        final Integer serverArrivalHour = (Integer) handle.select(
                "SELECT EXTRACT(HOUR FROM arrival_time) arrival_hour " +
                        "FROM flights WHERE flight_id=?", "C1671").get(0).get("arrival_hour");

        assertThat(serverDepartureHour).isEqualTo(departureTime.withZone(dbTimeZone).getHourOfDay());
        assertThat(serverArrivalHour).isEqualTo(arrivalTime.withZone(dbTimeZone).getHourOfDay());
    }

    @Test
    public void testReadTimestamp() {
        int result = handle.insert(
                "INSERT INTO flights(flight_id, departure_airport, arrival_airport, departure_time, arrival_time) " +
                        "VALUES ('C1671','ORD','DUS','2015-04-01T06:00:00-05:00','2015-04-01T21:00:00+02:00')");
        assertThat(result).isGreaterThan(0);

        final DateTime departureTime = flightDao.getDepartureTime("C1671");
        final DateTime arrivalTime = flightDao.getArrivalTime("C1671");

        assertThat(departureTime).isEqualTo(ISO_FMT.parseDateTime("2015-04-01T06:00:00-05:00"));
        assertThat(arrivalTime).isEqualTo(ISO_FMT.parseDateTime("2015-04-01T21:00:00+02:00"));
    }

    public interface FlightDao {

        @SqlUpdate("INSERT INTO flights(flight_id, departure_airport, arrival_airport,departure_time, arrival_time) " +
                "VALUES (:flight_id, :departure_airport, :arrival_airport, :departure_time, :arrival_time)")
        int insert(@Bind("flight_id") String flightId, @Bind("departure_airport") String departureAirport,
                   @Bind("arrival_airport") String arrivalAirport,
                   @Bind("departure_time") DateTime departureTime, @Bind("arrival_time") DateTime arrivalTime);

        @SqlQuery("SELECT arrival_time FROM flights WHERE flight_id=:flight_id")
        DateTime getArrivalTime(@Bind("flight_id") String flightId);

        @SqlQuery("SELECT departure_time FROM flights WHERE flight_id=:flight_id")
        DateTime getDepartureTime(@Bind("flight_id") String flightId);
    }
}
