package io.dropwizard.metrics;

import com.codahale.metrics.MetricAttribute;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Parameterized.class)
public class MetricAttributesTest {

    private static final EnumSet<MetricAttribute> ALL = EnumSet.allOf(MetricAttribute.class);
    private static final EnumSet<MetricAttribute> NONE = EnumSet.noneOf(MetricAttribute.class);

    @Parameters(name = "{index} !({0}-{1})={2}")
    public static List<Object[]> data() {
        return Arrays.asList(
                new Object[]{NONE, NONE, ALL},
                new Object[]{ALL, NONE, NONE},
                new Object[]{ALL, EnumSet.of(MetricAttribute.STDDEV, MetricAttribute.M15_RATE), EnumSet.of(MetricAttribute.STDDEV, MetricAttribute.M15_RATE)},
                new Object[]{EnumSet.of(MetricAttribute.STDDEV, MetricAttribute.M15_RATE), NONE, EnumSet.complementOf(EnumSet.of(MetricAttribute.STDDEV, MetricAttribute.M15_RATE))},
                new Object[]{EnumSet.of(MetricAttribute.STDDEV, MetricAttribute.M15_RATE, MetricAttribute.P95), EnumSet.of(MetricAttribute.P95), EnumSet.complementOf(EnumSet.of(MetricAttribute.STDDEV, MetricAttribute.M15_RATE))}
        );
    }

    private final BaseReporterFactory factory = new BaseReporterFactory() {
        @Override
        public ScheduledReporter build(MetricRegistry registry) {
            throw new UnsupportedOperationException("not implemented");
        }
    };

    @Parameter
    public EnumSet<MetricAttribute> includes = EnumSet.noneOf(MetricAttribute.class);

    @Parameter(1)
    public EnumSet<MetricAttribute> excludes = EnumSet.noneOf(MetricAttribute.class);

    @Parameter(2)
    public EnumSet<MetricAttribute> expectedResult = EnumSet.noneOf(MetricAttribute.class);

    @Test
    public void testGetDisabledAttributes() {
        factory.setIncludesAttributes(includes);
        factory.setExcludesAttributes(excludes);
        assertThat(factory.getDisabledAttributes()).isEqualTo(expectedResult);
    }

}
