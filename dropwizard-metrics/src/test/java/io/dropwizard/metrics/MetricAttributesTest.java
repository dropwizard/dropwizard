package io.dropwizard.metrics;

import com.codahale.metrics.MetricAttribute;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.EnumSet;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class MetricAttributesTest {

    private static final EnumSet<MetricAttribute> ALL = EnumSet.allOf(MetricAttribute.class);
    private static final EnumSet<MetricAttribute> NONE = EnumSet.noneOf(MetricAttribute.class);

    public static Stream<Arguments> data() {
        return Stream.of(
                Arguments.of(NONE, NONE, ALL),
                Arguments.of(ALL, NONE, NONE),
                Arguments.of(ALL, EnumSet.of(MetricAttribute.STDDEV, MetricAttribute.M15_RATE), EnumSet.of(MetricAttribute.STDDEV, MetricAttribute.M15_RATE)),
                Arguments.of(EnumSet.of(MetricAttribute.STDDEV, MetricAttribute.M15_RATE), NONE, EnumSet.complementOf(EnumSet.of(MetricAttribute.STDDEV, MetricAttribute.M15_RATE))),
                Arguments.of(EnumSet.of(MetricAttribute.STDDEV, MetricAttribute.M15_RATE, MetricAttribute.P95), EnumSet.of(MetricAttribute.P95), EnumSet.complementOf(EnumSet.of(MetricAttribute.STDDEV, MetricAttribute.M15_RATE)))
        );
    }

    private final BaseReporterFactory factory = new BaseReporterFactory() {
        @Override
        public ScheduledReporter build(MetricRegistry registry) {
            throw new UnsupportedOperationException("not implemented");
        }
    };

    @ParameterizedTest
    @MethodSource("data")
    public void testGetDisabledAttributes(EnumSet<MetricAttribute> includes, EnumSet<MetricAttribute> excludes,
                                          EnumSet<MetricAttribute> expectedResult) {
        factory.setIncludesAttributes(includes);
        factory.setExcludesAttributes(excludes);
        assertThat(factory.getDisabledAttributes()).isEqualTo(expectedResult);
    }

}
