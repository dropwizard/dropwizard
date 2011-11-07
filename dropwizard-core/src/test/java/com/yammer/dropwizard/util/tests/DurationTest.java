package com.yammer.dropwizard.util.tests;

import com.yammer.dropwizard.util.Duration;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class DurationTest {
    @Test
    public void convertsDays() throws Exception {
        assertThat(Duration.days(2).toDays(),
                   is(2L));
        assertThat(Duration.days(2).toHours(),
                   is(48L));
    }

    @Test
    public void convertsHours() throws Exception {
        assertThat(Duration.hours(2).toMinutes(),
                   is(120L));
    }

    @Test
    public void convertsMinutes() throws Exception {
        assertThat(Duration.minutes(3).toSeconds(),
                   is(180L));
    }

    @Test
    public void convertsSeconds() throws Exception {
        assertThat(Duration.seconds(2).toMilliseconds(),
                   is(2000L));
    }

    @Test
    public void convertsMilliseconds() throws Exception {
        assertThat(Duration.milliseconds(2).toMicroseconds(),
                   is(2000L));
    }

    @Test
    public void convertsMicroseconds() throws Exception {
        assertThat(Duration.microseconds(2).toNanoseconds(),
                   is(2000L));
    }

    @Test
    public void convertsNanoseconds() throws Exception {
        assertThat(Duration.nanoseconds(2).toNanoseconds(),
                   is(2L));
    }

    @Test
    public void parsesDays() throws Exception {
        assertThat(new Duration("1d"),
                   is(Duration.days(1)));

        assertThat(new Duration("1 day"),
                   is(Duration.days(1)));

        assertThat(new Duration("2 days"),
                   is(Duration.days(2)));
    }

    @Test
    public void parsesHours() throws Exception {
        assertThat(new Duration("1h"),
                   is(Duration.hours(1)));

        assertThat(new Duration("1 hour"),
                   is(Duration.hours(1)));

        assertThat(new Duration("2 hours"),
                   is(Duration.hours(2)));
    }

    @Test
    public void parsesMinutes() throws Exception {
        assertThat(new Duration("1m"),
                   is(Duration.minutes(1)));

        assertThat(new Duration("1 minute"),
                   is(Duration.minutes(1)));

        assertThat(new Duration("2 minutes"),
                   is(Duration.minutes(2)));
    }

    @Test
    public void parsesSeconds() throws Exception {
        assertThat(new Duration("1s"),
                   is(Duration.seconds(1)));

        assertThat(new Duration("1 second"),
                   is(Duration.seconds(1)));

        assertThat(new Duration("2 seconds"),
                   is(Duration.seconds(2)));
    }

    @Test
    public void parsesMilliseconds() throws Exception {
        assertThat(new Duration("1ms"),
                   is(Duration.milliseconds(1)));

        assertThat(new Duration("1 millisecond"),
                   is(Duration.milliseconds(1)));

        assertThat(new Duration("2 milliseconds"),
                   is(Duration.milliseconds(2)));
    }

    @Test
    public void parsesMicroseconds() throws Exception {
        assertThat(new Duration("1us"),
                   is(Duration.microseconds(1)));

        assertThat(new Duration("1 microsecond"),
                   is(Duration.microseconds(1)));

        assertThat(new Duration("2 microseconds"),
                   is(Duration.microseconds(2)));
    }

    @Test
    public void parsesNanoseconds() throws Exception {
        assertThat(new Duration("1ns"),
                   is(Duration.nanoseconds(1)));

        assertThat(new Duration("1 nanosecond"),
                   is(Duration.nanoseconds(1)));

        assertThat(new Duration("2 nanoseconds"),
                   is(Duration.nanoseconds(2)));
    }

    @Test
    public void isHumanReadable() throws Exception {
        assertThat(Duration.microseconds(1).toString(),
                   is("1 microsecond"));

        assertThat(Duration.microseconds(3).toString(),
                   is("3 microseconds"));
    }
}
