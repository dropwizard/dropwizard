package io.dropwizard.util;

import org.joda.time.DateTime;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class DateTimeOffsetTest {
    @Test
    public void parseYears() throws Exception {
        assertThat(DateTimeOffset.parse("1yr"))
            .isEqualTo(DateTimeOffset.years(1));

        assertThat(DateTimeOffset.parse("2yrs"))
            .isEqualTo(DateTimeOffset.years(2));

        assertThat(DateTimeOffset.parse("1 year"))
            .isEqualTo(DateTimeOffset.years(1));

        assertThat(DateTimeOffset.parse("2 years"))
            .isEqualTo(DateTimeOffset.years(2));

        assertThat(DateTimeOffset.parse("+1yr"))
            .isEqualTo(DateTimeOffset.years(1));

        assertThat(DateTimeOffset.parse("+2yrs"))
            .isEqualTo(DateTimeOffset.years(2));

        assertThat(DateTimeOffset.parse("+1 year"))
            .isEqualTo(DateTimeOffset.years(1));

        assertThat(DateTimeOffset.parse("+2 years"))
            .isEqualTo(DateTimeOffset.years(2));

        assertThat(DateTimeOffset.parse("-1yr"))
            .isEqualTo(DateTimeOffset.years(-1));

        assertThat(DateTimeOffset.parse("-2yrs"))
            .isEqualTo(DateTimeOffset.years(-2));

        assertThat(DateTimeOffset.parse("-1 year"))
            .isEqualTo(DateTimeOffset.years(-1));

        assertThat(DateTimeOffset.parse("-2 years"))
            .isEqualTo(DateTimeOffset.years(-2));
    }

    @Test
    public void parsesMonths() throws Exception {
        assertThat(DateTimeOffset.parse("1mo"))
            .isEqualTo(DateTimeOffset.months(1));

        assertThat(DateTimeOffset.parse("2mos"))
            .isEqualTo(DateTimeOffset.months(2));

        assertThat(DateTimeOffset.parse("1 month"))
            .isEqualTo(DateTimeOffset.months(1));

        assertThat(DateTimeOffset.parse("2 months"))
            .isEqualTo(DateTimeOffset.months(2));

        assertThat(DateTimeOffset.parse("+1mo"))
            .isEqualTo(DateTimeOffset.months(1));

        assertThat(DateTimeOffset.parse("+2mos"))
            .isEqualTo(DateTimeOffset.months(2));

        assertThat(DateTimeOffset.parse("+1 month"))
            .isEqualTo(DateTimeOffset.months(1));

        assertThat(DateTimeOffset.parse("+2 months"))
            .isEqualTo(DateTimeOffset.months(2));

        assertThat(DateTimeOffset.parse("-1mo"))
            .isEqualTo(DateTimeOffset.months(-1));

        assertThat(DateTimeOffset.parse("-2mos"))
            .isEqualTo(DateTimeOffset.months(-2));

        assertThat(DateTimeOffset.parse("-1 month"))
            .isEqualTo(DateTimeOffset.months(-1));

        assertThat(DateTimeOffset.parse("-2 months"))
            .isEqualTo(DateTimeOffset.months(-2));
    }

    @Test
    public void parsesWeeks() throws Exception {
        assertThat(DateTimeOffset.parse("1w"))
            .isEqualTo(DateTimeOffset.weeks(1));

        assertThat(DateTimeOffset.parse("1wk"))
            .isEqualTo(DateTimeOffset.weeks(1));

        assertThat(DateTimeOffset.parse("2wks"))
            .isEqualTo(DateTimeOffset.weeks(2));

        assertThat(DateTimeOffset.parse("1 week"))
            .isEqualTo(DateTimeOffset.weeks(1));

        assertThat(DateTimeOffset.parse("2 weeks"))
            .isEqualTo(DateTimeOffset.weeks(2));

        assertThat(DateTimeOffset.parse("+1w"))
            .isEqualTo(DateTimeOffset.weeks(1));

        assertThat(DateTimeOffset.parse("+1wk"))
            .isEqualTo(DateTimeOffset.weeks(1));

        assertThat(DateTimeOffset.parse("+2wks"))
            .isEqualTo(DateTimeOffset.weeks(2));

        assertThat(DateTimeOffset.parse("+1 week"))
            .isEqualTo(DateTimeOffset.weeks(1));

        assertThat(DateTimeOffset.parse("+2 weeks"))
            .isEqualTo(DateTimeOffset.weeks(2));

        assertThat(DateTimeOffset.parse("-1w"))
            .isEqualTo(DateTimeOffset.weeks(-1));

        assertThat(DateTimeOffset.parse("-1wk"))
            .isEqualTo(DateTimeOffset.weeks(-1));

        assertThat(DateTimeOffset.parse("-2wks"))
            .isEqualTo(DateTimeOffset.weeks(-2));

        assertThat(DateTimeOffset.parse("-1 week"))
            .isEqualTo(DateTimeOffset.weeks(-1));

        assertThat(DateTimeOffset.parse("-2 weeks"))
            .isEqualTo(DateTimeOffset.weeks(-2));
    }
    @Test
    public void parsesDays() throws Exception {
        assertThat(DateTimeOffset.parse("1d"))
            .isEqualTo(DateTimeOffset.days(1));

        assertThat(DateTimeOffset.parse("1 day"))
            .isEqualTo(DateTimeOffset.days(1));

        assertThat(DateTimeOffset.parse("2 days"))
            .isEqualTo(DateTimeOffset.days(2));

        assertThat(DateTimeOffset.parse("+1d"))
            .isEqualTo(DateTimeOffset.days(1));

        assertThat(DateTimeOffset.parse("+1 day"))
            .isEqualTo(DateTimeOffset.days(1));

        assertThat(DateTimeOffset.parse("+2 days"))
            .isEqualTo(DateTimeOffset.days(2));

        assertThat(DateTimeOffset.parse("-1d"))
            .isEqualTo(DateTimeOffset.days(-1));

        assertThat(DateTimeOffset.parse("-1 day"))
            .isEqualTo(DateTimeOffset.days(-1));

        assertThat(DateTimeOffset.parse("-2 days"))
            .isEqualTo(DateTimeOffset.days(-2));
    }

    @Test
    public void parsesHours() throws Exception {
        assertThat(DateTimeOffset.parse("1h"))
            .isEqualTo(DateTimeOffset.hours(1));

        assertThat(DateTimeOffset.parse("1hr"))
            .isEqualTo(DateTimeOffset.hours(1));

        assertThat(DateTimeOffset.parse("2hrs"))
            .isEqualTo(DateTimeOffset.hours(2));

        assertThat(DateTimeOffset.parse("1 hour"))
            .isEqualTo(DateTimeOffset.hours(1));

        assertThat(DateTimeOffset.parse("2 hours"))
            .isEqualTo(DateTimeOffset.hours(2));

        assertThat(DateTimeOffset.parse("+1h"))
            .isEqualTo(DateTimeOffset.hours(1));

        assertThat(DateTimeOffset.parse("+1hr"))
            .isEqualTo(DateTimeOffset.hours(1));

        assertThat(DateTimeOffset.parse("+2hrs"))
            .isEqualTo(DateTimeOffset.hours(2));

        assertThat(DateTimeOffset.parse("+1 hour"))
            .isEqualTo(DateTimeOffset.hours(1));

        assertThat(DateTimeOffset.parse("+2 hours"))
            .isEqualTo(DateTimeOffset.hours(2));

        assertThat(DateTimeOffset.parse("-1h"))
            .isEqualTo(DateTimeOffset.hours(-1));

        assertThat(DateTimeOffset.parse("-1hr"))
            .isEqualTo(DateTimeOffset.hours(-1));

        assertThat(DateTimeOffset.parse("-2hrs"))
            .isEqualTo(DateTimeOffset.hours(-2));

        assertThat(DateTimeOffset.parse("-1 hour"))
            .isEqualTo(DateTimeOffset.hours(-1));

        assertThat(DateTimeOffset.parse("-2 hours"))
            .isEqualTo(DateTimeOffset.hours(-2));
    }

    @Test
    public void parsesMinutes() throws Exception {
        assertThat(DateTimeOffset.parse("1m"))
            .isEqualTo(DateTimeOffset.minutes(1));

        assertThat(DateTimeOffset.parse("1min"))
            .isEqualTo(DateTimeOffset.minutes(1));

        assertThat(DateTimeOffset.parse("2mins"))
            .isEqualTo(DateTimeOffset.minutes(2));

        assertThat(DateTimeOffset.parse("1 minute"))
            .isEqualTo(DateTimeOffset.minutes(1));

        assertThat(DateTimeOffset.parse("2 minutes"))
            .isEqualTo(DateTimeOffset.minutes(2));

        assertThat(DateTimeOffset.parse("+1m"))
            .isEqualTo(DateTimeOffset.minutes(1));

        assertThat(DateTimeOffset.parse("+1min"))
            .isEqualTo(DateTimeOffset.minutes(1));

        assertThat(DateTimeOffset.parse("+2mins"))
            .isEqualTo(DateTimeOffset.minutes(2));

        assertThat(DateTimeOffset.parse("+1 minute"))
            .isEqualTo(DateTimeOffset.minutes(1));

        assertThat(DateTimeOffset.parse("+2 minutes"))
            .isEqualTo(DateTimeOffset.minutes(2));

        assertThat(DateTimeOffset.parse("-1m"))
            .isEqualTo(DateTimeOffset.minutes(-1));

        assertThat(DateTimeOffset.parse("-1min"))
            .isEqualTo(DateTimeOffset.minutes(-1));

        assertThat(DateTimeOffset.parse("-2mins"))
            .isEqualTo(DateTimeOffset.minutes(-2));

        assertThat(DateTimeOffset.parse("-1 minute"))
            .isEqualTo(DateTimeOffset.minutes(-1));

        assertThat(DateTimeOffset.parse("-2 minutes"))
            .isEqualTo(DateTimeOffset.minutes(-2));
    }

    @Test
    public void parsesSeconds() throws Exception {
        assertThat(DateTimeOffset.parse("1s"))
            .isEqualTo(DateTimeOffset.seconds(1));

        assertThat(DateTimeOffset.parse("1sec"))
            .isEqualTo(DateTimeOffset.seconds(1));

        assertThat(DateTimeOffset.parse("2secs"))
            .isEqualTo(DateTimeOffset.seconds(2));

        assertThat(DateTimeOffset.parse("1 second"))
            .isEqualTo(DateTimeOffset.seconds(1));

        assertThat(DateTimeOffset.parse("2 seconds"))
            .isEqualTo(DateTimeOffset.seconds(2));

        assertThat(DateTimeOffset.parse("+1s"))
            .isEqualTo(DateTimeOffset.seconds(1));

        assertThat(DateTimeOffset.parse("+1sec"))
            .isEqualTo(DateTimeOffset.seconds(1));

        assertThat(DateTimeOffset.parse("+2secs"))
            .isEqualTo(DateTimeOffset.seconds(2));

        assertThat(DateTimeOffset.parse("+1 second"))
            .isEqualTo(DateTimeOffset.seconds(1));

        assertThat(DateTimeOffset.parse("+2 seconds"))
            .isEqualTo(DateTimeOffset.seconds(2));

        assertThat(DateTimeOffset.parse("-1s"))
            .isEqualTo(DateTimeOffset.seconds(-1));

        assertThat(DateTimeOffset.parse("-1sec"))
            .isEqualTo(DateTimeOffset.seconds(-1));

        assertThat(DateTimeOffset.parse("-2secs"))
            .isEqualTo(DateTimeOffset.seconds(-2));

        assertThat(DateTimeOffset.parse("-1 second"))
            .isEqualTo(DateTimeOffset.seconds(-1));

        assertThat(DateTimeOffset.parse("-2 seconds"))
            .isEqualTo(DateTimeOffset.seconds(-2));
    }

    @Test
    public void parsesMilliseconds() throws Exception {
        assertThat(DateTimeOffset.parse("1ms"))
            .isEqualTo(DateTimeOffset.milliseconds(1));

        assertThat(DateTimeOffset.parse("1msec"))
            .isEqualTo(DateTimeOffset.milliseconds(1));

        assertThat(DateTimeOffset.parse("2msecs"))
            .isEqualTo(DateTimeOffset.milliseconds(2));

        assertThat(DateTimeOffset.parse("1 millisecond"))
            .isEqualTo(DateTimeOffset.milliseconds(1));

        assertThat(DateTimeOffset.parse("2 milliseconds"))
            .isEqualTo(DateTimeOffset.milliseconds(2));

        assertThat(DateTimeOffset.parse("+1ms"))
            .isEqualTo(DateTimeOffset.milliseconds(1));

        assertThat(DateTimeOffset.parse("+1msec"))
            .isEqualTo(DateTimeOffset.milliseconds(1));

        assertThat(DateTimeOffset.parse("+2msecs"))
            .isEqualTo(DateTimeOffset.milliseconds(2));

        assertThat(DateTimeOffset.parse("+1 millisecond"))
            .isEqualTo(DateTimeOffset.milliseconds(1));

        assertThat(DateTimeOffset.parse("+2 milliseconds"))
            .isEqualTo(DateTimeOffset.milliseconds(2));

        assertThat(DateTimeOffset.parse("-1ms"))
            .isEqualTo(DateTimeOffset.milliseconds(-1));

        assertThat(DateTimeOffset.parse("-1msec"))
            .isEqualTo(DateTimeOffset.milliseconds(-1));

        assertThat(DateTimeOffset.parse("-2msecs"))
            .isEqualTo(DateTimeOffset.milliseconds(-2));

        assertThat(DateTimeOffset.parse("-1 millisecond"))
            .isEqualTo(DateTimeOffset.milliseconds(-1));

        assertThat(DateTimeOffset.parse("-2 milliseconds"))
            .isEqualTo(DateTimeOffset.milliseconds(-2));
    }

    @Test
    public void isHumanReadable() throws Exception {
        assertThat(DateTimeOffset.milliseconds(1).toString())
            .isEqualTo("1 millisecond");

        assertThat(DateTimeOffset.milliseconds(3).toString())
            .isEqualTo("3 milliseconds");
    }

    @Test
    public void hasAQuantity() throws Exception {
        assertThat(DateTimeOffset.milliseconds(12).getQuantity())
            .isEqualTo(12);
    }

    @Test
    public void hasAUnit() throws Exception {
        assertThat(DateTimeOffset.milliseconds(1).getUnit())
            .isEqualTo(OffsetUnit.MILLISECONDS);
    }

    @Test
    public void applyYears() {
        assertThat(DateTimeOffset.years(1).apply(DateTime.parse("2000-1-1TZ")))
            .isEqualTo(DateTime.parse("2001-1-1TZ"));
        assertThat(DateTimeOffset.years(-1).apply(DateTime.parse("2000-1-1TZ")))
            .isEqualTo(DateTime.parse("1999-1-1TZ"));
        assertThat(DateTimeOffset.years(0).apply(DateTime.parse("2000-1-1TZ")))
            .isEqualTo(DateTime.parse("2000-1-1TZ"));
    }

    @Test
    public void applyMonths() {
        assertThat(DateTimeOffset.months(1).apply(DateTime.parse("2000-1-1TZ")))
            .isEqualTo(DateTime.parse("2000-2-1TZ"));
        assertThat(DateTimeOffset.months(-1).apply(DateTime.parse("2000-1-1TZ")))
            .isEqualTo(DateTime.parse("1999-12-1TZ"));
        assertThat(DateTimeOffset.months(0).apply(DateTime.parse("2000-1-1TZ")))
            .isEqualTo(DateTime.parse("2000-1-1TZ"));
    }

    @Test
    public void applyWeeks() {
        assertThat(DateTimeOffset.weeks(1).apply(DateTime.parse("2000-1-10TZ")))
            .isEqualTo(DateTime.parse("2000-1-17TZ"));
        assertThat(DateTimeOffset.weeks(-1).apply(DateTime.parse("2000-1-10TZ")))
            .isEqualTo(DateTime.parse("2000-1-3TZ"));
        assertThat(DateTimeOffset.months(0).apply(DateTime.parse("2000-1-10TZ")))
            .isEqualTo(DateTime.parse("2000-1-10TZ"));
    }

    @Test
    public void applyDays() {
        assertThat(DateTimeOffset.days(1).apply(DateTime.parse("2000-1-10TZ")))
            .isEqualTo(DateTime.parse("2000-1-11TZ"));
        assertThat(DateTimeOffset.days(-1).apply(DateTime.parse("2000-1-10TZ")))
            .isEqualTo(DateTime.parse("2000-1-9TZ"));
        assertThat(DateTimeOffset.days(0).apply(DateTime.parse("2000-1-10TZ")))
            .isEqualTo(DateTime.parse("2000-1-10TZ"));
    }

    @Test
    public void applyHours() {
        assertThat(DateTimeOffset.hours(1).apply(DateTime.parse("2000-1-10T05:00:00Z")))
            .isEqualTo(DateTime.parse("2000-1-10T06:00:00Z"));
        assertThat(DateTimeOffset.hours(-1).apply(DateTime.parse("2000-1-10T05:00:00Z")))
            .isEqualTo(DateTime.parse("2000-1-10T04:00:00Z"));
        assertThat(DateTimeOffset.hours(0).apply(DateTime.parse("2000-1-10T05:00:00Z")))
            .isEqualTo(DateTime.parse("2000-1-10T05:00:00Z"));
    }

    @Test
    public void applyMinutes() {
        assertThat(DateTimeOffset.minutes(1).apply(DateTime.parse("2000-1-10T05:30:00Z")))
            .isEqualTo(DateTime.parse("2000-1-10T05:31:00Z"));
        assertThat(DateTimeOffset.minutes(-1).apply(DateTime.parse("2000-1-10T05:30:00Z")))
            .isEqualTo(DateTime.parse("2000-1-10T05:29:00Z"));
        assertThat(DateTimeOffset.minutes(0).apply(DateTime.parse("2000-1-10T05:30:00Z")))
            .isEqualTo(DateTime.parse("2000-1-10T05:30:00Z"));
    }

    @Test
    public void applySeconds() {
        assertThat(DateTimeOffset.seconds(1).apply(DateTime.parse("2000-1-10T05:30:30Z")))
            .isEqualTo(DateTime.parse("2000-1-10T05:30:31Z"));
        assertThat(DateTimeOffset.seconds(-1).apply(DateTime.parse("2000-1-10T05:30:30Z")))
            .isEqualTo(DateTime.parse("2000-1-10T05:30:29Z"));
        assertThat(DateTimeOffset.seconds(0).apply(DateTime.parse("2000-1-10T05:30:30Z")))
            .isEqualTo(DateTime.parse("2000-1-10T05:30:30Z"));
    }

    @Test
    public void applyMilliseconds() {
        assertThat(DateTimeOffset.milliseconds(100).apply(DateTime.parse("2000-1-10T05:30:30.500Z")))
            .isEqualTo(DateTime.parse("2000-1-10T05:30:30.600Z"));
        assertThat(DateTimeOffset.milliseconds(-100).apply(DateTime.parse("2000-1-10T05:30:30.500Z")))
            .isEqualTo(DateTime.parse("2000-1-10T05:30:30.400Z"));
        assertThat(DateTimeOffset.milliseconds(0).apply(DateTime.parse("2000-1-10T05:30:30.500Z")))
            .isEqualTo(DateTime.parse("2000-1-10T05:30:30.500Z"));
    }
}
