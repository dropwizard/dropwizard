package io.dropwizard.metrics;

import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(Parameterized.class)
public class BaseReporterFactoryTest {


    private static final ImmutableSet<String> INCLUDES = ImmutableSet.of("inc", "both");
    private static final ImmutableSet<String> EXCLUDES = ImmutableSet.of("both", "exc");
    private static final ImmutableSet<String> EMPTY = ImmutableSet.of();


    @Parameterized.Parameters(name = "{index} {4} {2}={3}")
    public static List<Object[]> data() {

        return ImmutableList.of(
                /**
                 * case1: If include list is empty and exclude list is empty, everything should be
                 * included.
                 */
                new Object[]{EMPTY, EMPTY, "inc", true, "case1"},
                new Object[]{EMPTY, EMPTY, "both", true, "case1"},
                new Object[]{EMPTY, EMPTY, "exc", true, "case1"},
                new Object[]{EMPTY, EMPTY, "any", true, "case1"},

                /**
                 * case2: If include list is NOT empty and exclude list is empty, only the ones
                 * specified in the include list should be included.
                 */
                new Object[]{INCLUDES, EMPTY, "inc", true, "case2"},
                new Object[]{INCLUDES, EMPTY, "both", true, "case2"},
                new Object[]{INCLUDES, EMPTY, "exc", false, "case2"},
                new Object[]{INCLUDES, EMPTY, "any", false, "case2"},

                /**
                 * case3: If include list is empty and exclude list is NOT empty, everything should be
                 * included except the ones in the exclude list.
                 */
                new Object[]{EMPTY, EXCLUDES, "inc", true, "case3"},
                new Object[]{EMPTY, EXCLUDES, "both", false, "case3"},
                new Object[]{EMPTY, EXCLUDES, "exc", false, "case3"},
                new Object[]{EMPTY, EXCLUDES, "any", true, "case3"},

                /**
                 * case4: If include list is NOT empty and exclude list is NOT empty, everything
                 * should be included except the
                 * ones that are not in the include list AND are in the exclude list
                 */
                new Object[]{INCLUDES, EXCLUDES, "inc", true, "case4"},
                new Object[]{INCLUDES, EXCLUDES, "both", true, "case4"},
                new Object[]{INCLUDES, EXCLUDES, "exc", false, "case4"},
                new Object[]{INCLUDES, EXCLUDES, "any", true, "case4"}
        );
    }

    private final BaseReporterFactory factory = new BaseReporterFactory() {
        @Override
        public ScheduledReporter build(MetricRegistry registry) {
            throw new UnsupportedOperationException("not implemented");
        }
    };

    @Parameterized.Parameter(0)
    public ImmutableSet<String> includes;

    @Parameterized.Parameter(1)
    public ImmutableSet<String> excludes;


    @Parameterized.Parameter(2)
    public String name;

    @Parameterized.Parameter(3)
    public boolean expected;

    @Parameterized.Parameter(4)
    public String msg;

    private final Metric metric = mock(Metric.class);

    @Test
    public void matches() {
        factory.setIncludes(includes);
        factory.setExcludes(excludes);

        assertThat(factory.getFilter().matches(name, metric))
                .overridingErrorMessage(msg + ": expected 'matches(%s)=%s'", name, expected)
                .isEqualTo(expected);
    }

}