package com.yammer.dropwizard.util.tests;

import com.yammer.dropwizard.util.Duration;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.fest.assertions.api.Assertions.assertThat;

public class DurationTest {
    @Test
    public void convertsDays() throws Exception {
        assertThat(Duration.days(2).toDays())
                .isEqualTo(2);
        assertThat(Duration.days(2).toHours())
                .isEqualTo(48);
    }

    @Test
    public void convertsHours() throws Exception {
        assertThat(Duration.hours(2).toMinutes())
                .isEqualTo(120);
    }

    @Test
    public void convertsMinutes() throws Exception {
        assertThat(Duration.minutes(3).toSeconds())
                .isEqualTo(180);
    }

    @Test
    public void convertsSeconds() throws Exception {
        assertThat(Duration.seconds(2).toMilliseconds())
                .isEqualTo(2000);
    }

    @Test
    public void convertsMilliseconds() throws Exception {
        assertThat(Duration.milliseconds(2).toMicroseconds())
                .isEqualTo(2000);
    }

    @Test
    public void convertsMicroseconds() throws Exception {
        assertThat(Duration.microseconds(2).toNanoseconds())
                .isEqualTo(2000);
    }

    @Test
    public void convertsNanoseconds() throws Exception {
        assertThat(Duration.nanoseconds(2).toNanoseconds())
                .isEqualTo(2);
    }

    @Test
    public void parsesDays() throws Exception {
        assertThat(Duration.parse("1d"))
                .isEqualTo(Duration.days(1));

        assertThat(Duration.parse("1 day"))
                .isEqualTo(Duration.days(1));

        assertThat(Duration.parse("2 days"))
                .isEqualTo(Duration.days(2));
    }

    @Test
    public void parsesHours() throws Exception {
        assertThat(Duration.parse("1h"))
                .isEqualTo(Duration.hours(1));

        assertThat(Duration.parse("1 hour"))
                .isEqualTo(Duration.hours(1));

        assertThat(Duration.parse("2 hours"))
                .isEqualTo(Duration.hours(2));
    }

    @Test
    public void parsesMinutes() throws Exception {
        assertThat(Duration.parse("1m"))
                .isEqualTo(Duration.minutes(1));

        assertThat(Duration.parse("1 minute"))
                .isEqualTo(Duration.minutes(1));

        assertThat(Duration.parse("2 minutes"))
                .isEqualTo(Duration.minutes(2));
    }

    @Test
    public void parsesSeconds() throws Exception {
        assertThat(Duration.parse("1s"))
                .isEqualTo(Duration.seconds(1));

        assertThat(Duration.parse("1 second"))
                .isEqualTo(Duration.seconds(1));

        assertThat(Duration.parse("2 seconds"))
                .isEqualTo(Duration.seconds(2));
    }

    @Test
    public void parsesMilliseconds() throws Exception {
        assertThat(Duration.parse("1ms"))
                .isEqualTo(Duration.milliseconds(1));

        assertThat(Duration.parse("1 millisecond"))
                .isEqualTo(Duration.milliseconds(1));

        assertThat(Duration.parse("2 milliseconds"))
                .isEqualTo(Duration.milliseconds(2));
    }

    @Test
    public void parsesMicroseconds() throws Exception {
        assertThat(Duration.parse("1us"))
                .isEqualTo(Duration.microseconds(1));

        assertThat(Duration.parse("1 microsecond"))
                .isEqualTo(Duration.microseconds(1));

        assertThat(Duration.parse("2 microseconds"))
                .isEqualTo(Duration.microseconds(2));
    }

    @Test
    public void parsesNanoseconds() throws Exception {
        assertThat(Duration.parse("1ns"))
                .isEqualTo(Duration.nanoseconds(1));

        assertThat(Duration.parse("1 nanosecond"))
                .isEqualTo(Duration.nanoseconds(1));

        assertThat(Duration.parse("2 nanoseconds"))
                .isEqualTo(Duration.nanoseconds(2));
    }

    @Test
    public void isHumanReadable() throws Exception {
        assertThat(Duration.microseconds(1).toString())
                .isEqualTo("1 microsecond");

        assertThat(Duration.microseconds(3).toString())
                .isEqualTo("3 microseconds");
    }

    @Test
    public void hasAQuantity() throws Exception {
        assertThat(Duration.microseconds(12).getQuantity())
                .isEqualTo(12);
    }

    @Test
    public void hasAUnit() throws Exception {
        assertThat(Duration.microseconds(1).getUnit())
                .isEqualTo(TimeUnit.MICROSECONDS);
    }
}
