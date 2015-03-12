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


    private static final ImmutableSet<String> INCLUDES = ImmutableSet.of("inc", "both", "inc.+");
    private static final ImmutableSet<String> EXCLUDES = ImmutableSet.of("both", "exc", "exc.+");
    private static final ImmutableSet<String> EMPTY = ImmutableSet.of();


    @Parameterized.Parameters(name = "{index} {4} {2}={3}")
    public static List<Object[]> data() {

        return ImmutableList.of(
                /**
                 * case1: If include list is empty and exclude list is empty, everything should be
                 * included.
                 */
                new Object[]{EMPTY, EMPTY, "inc", true, true, "case1"},
                new Object[]{EMPTY, EMPTY, "both", true, true, "case1"},
                new Object[]{EMPTY, EMPTY, "exc", true, true, "case1"},
                new Object[]{EMPTY, EMPTY, "any", true, true, "case1"},
                new Object[]{EMPTY, EMPTY, "incWithSuffix", true, true, "case1"},
                new Object[]{EMPTY, EMPTY, "excWithSuffix", true, true, "case1"},

                /**
                 * case2: If include list is NOT empty and exclude list is empty, only the ones
                 * specified in the include list should be included.
                 */
                new Object[]{INCLUDES, EMPTY, "inc", true, true, "case2"},
                new Object[]{INCLUDES, EMPTY, "both", true, true, "case2"},
                new Object[]{INCLUDES, EMPTY, "exc", false, false, "case2"},
                new Object[]{INCLUDES, EMPTY, "any", false, false, "case2"},
                new Object[]{INCLUDES, EMPTY, "incWithSuffix", false, true, "case2"},
                new Object[]{INCLUDES, EMPTY, "excWithSuffix", false, false, "case2"},

                /**
                 * case3: If include list is empty and exclude list is NOT empty, everything should be
                 * included except the ones in the exclude list.
                 */
                new Object[]{EMPTY, EXCLUDES, "inc", true, true, "case3"},
                new Object[]{EMPTY, EXCLUDES, "both", false, false, "case3"},
                new Object[]{EMPTY, EXCLUDES, "exc", false, false, "case3"},
                new Object[]{EMPTY, EXCLUDES, "any", true, true, "case3"},
                new Object[]{EMPTY, EXCLUDES, "incWithSuffix", true, true, "case3"},
                new Object[]{EMPTY, EXCLUDES, "excWithSuffix", true, false, "case3"},

                /**
                 * case4: If include list is NOT empty and exclude list is NOT empty, only things not excluded
                 * and specifically included should show up. Excludes takes precedence.
                 */
                new Object[]{INCLUDES, EXCLUDES, "inc", true, true, "case4"},
                new Object[]{INCLUDES, EXCLUDES, "both", false, false, "case4"},
                new Object[]{INCLUDES, EXCLUDES, "exc", false, false, "case4"},
                new Object[]{INCLUDES, EXCLUDES, "any", false, false, "case4"},
                new Object[]{INCLUDES, EXCLUDES, "incWithSuffix", false, true, "case4"},
                new Object[]{INCLUDES, EXCLUDES, "excWithSuffix", false, false, "case4"}
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
    public boolean expectedDefaultResult;

    @Parameterized.Parameter(4)
    public boolean expectedRegexResult;

    @Parameterized.Parameter(5)
    public String msg;

    private final Metric metric = mock(Metric.class);

    @Test
    public void testDefaultMatching() {
        factory.setIncludes(includes);
        factory.setExcludes(excludes);

        factory.setUseRegexFilters(false);
        assertThat(factory.getFilter().matches(name, metric))
                .overridingErrorMessage(msg + ": expected 'matches(%s)=%s' for default matcher", name, expectedDefaultResult)
                .isEqualTo(expectedDefaultResult);
    }

    @Test
    public void testRegexMatching() {
        factory.setIncludes(includes);
        factory.setExcludes(excludes);

        factory.setUseRegexFilters(true);
        assertThat(factory.getFilter().matches(name, metric))
                .overridingErrorMessage(msg + ": expected 'matches(%s)=%s' for regex matcher", name, expectedRegexResult)
                .isEqualTo(expectedRegexResult);
    }

}