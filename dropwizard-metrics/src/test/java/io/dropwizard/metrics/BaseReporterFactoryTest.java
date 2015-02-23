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


    private static final String INCLUDES_REGEX = "inc2|all|regx|excregx";
    private static final ImmutableSet<String> INCLUDES = ImmutableSet.of("inc", "both", "inc2", "all");
    private static final ImmutableSet<String> EXCLUDES = ImmutableSet.of("both", "exc", "excregx", "all");
    private static final ImmutableSet<String> EMPTY = ImmutableSet.of();


    @Parameterized.Parameters(name = "{index} {4} {2}={3}")
    public static List<Object[]> data() {

        return ImmutableList.of(
                /**
                 * case1: If include list is empty and exclude list is empty, everything should be
                 * included.
                 */
                new Object[]{EMPTY, null, EMPTY, "inc", true, "case1"},
                new Object[]{EMPTY, null, EMPTY, "both", true, "case1"},
                new Object[]{EMPTY, null, EMPTY, "exc", true, "case1"},
                new Object[]{EMPTY, null, EMPTY, "any", true, "case1"},
                new Object[]{EMPTY, null, EMPTY, "all", true, "case1"},
                new Object[]{EMPTY, null, EMPTY, "inc2", true, "case1"},
                new Object[]{EMPTY, null, EMPTY, "regx", true, "case1"},
                new Object[]{EMPTY, null, EMPTY, "excregx", true, "case1"},

                /**
                 * case2: If include list is NOT empty and exclude list is empty, only the ones
                 * specified in the include list should be included.
                 */
                new Object[]{INCLUDES, null, EMPTY, "inc", true, "case2"},
                new Object[]{INCLUDES, null, EMPTY, "both", true, "case2"},
                new Object[]{INCLUDES, null, EMPTY, "exc", false, "case2"},
                new Object[]{INCLUDES, null, EMPTY, "any", false, "case2"},
                new Object[]{INCLUDES, null, EMPTY, "all", true, "case2"},
                new Object[]{INCLUDES, null, EMPTY, "inc2", true, "case2"},
                new Object[]{INCLUDES, null, EMPTY, "regx", false, "case2"},
                new Object[]{INCLUDES, null, EMPTY, "excregx", false, "case2"},

                /**
                 * case3: If include list is empty and exclude list is NOT empty, everything should be
                 * included except the ones in the exclude list.
                 */
                new Object[]{EMPTY, null, EXCLUDES, "inc", true, "case3"},
                new Object[]{EMPTY, null, EXCLUDES, "both", false, "case3"},
                new Object[]{EMPTY, null, EXCLUDES, "exc", false, "case3"},
                new Object[]{EMPTY, null, EXCLUDES, "any", true, "case3"},
                new Object[]{EMPTY, null, EXCLUDES, "all", false, "case3"},
                new Object[]{EMPTY, null, EXCLUDES, "inc2", true, "case3"},
                new Object[]{EMPTY, null, EXCLUDES, "regx", true, "case3"},
                new Object[]{EMPTY, null, EXCLUDES, "excregx", false, "case3"},

                /**
                 * case4: If include list is NOT empty and exclude list is NOT empty, only things not excluded
                 * and specifically included should show up.
                 */
                new Object[]{INCLUDES, "", EXCLUDES, "inc", true, "case4"},
                new Object[]{INCLUDES, "", EXCLUDES, "both", false, "case4"},
                new Object[]{INCLUDES, "", EXCLUDES, "exc", false, "case4"},
                new Object[]{INCLUDES, "", EXCLUDES, "any", false, "case4"},
                new Object[]{INCLUDES, "", EXCLUDES, "all", false, "case4"},
                new Object[]{INCLUDES, "", EXCLUDES, "inc2", true, "case4"},
                new Object[]{INCLUDES, "", EXCLUDES, "regx", false, "case4"},
                new Object[]{INCLUDES, "", EXCLUDES, "excregx", false, "case4"},

                /**
                 * case5: Similar to case 2; if include list is empty and includesRegex is not null and exclude list
                 * is empty, then items matching includes_regex should be included.
                 */
                new Object[]{EMPTY, INCLUDES_REGEX, EMPTY, "inc", false, "case5"},
                new Object[]{EMPTY, INCLUDES_REGEX, EMPTY, "both", false, "case5"},
                new Object[]{EMPTY, INCLUDES_REGEX, EMPTY, "exc", false, "case5"},
                new Object[]{EMPTY, INCLUDES_REGEX, EMPTY, "any", false, "case5"},
                new Object[]{EMPTY, INCLUDES_REGEX, EMPTY, "all", true, "case5"},
                new Object[]{EMPTY, INCLUDES_REGEX, EMPTY, "inc2", true, "case5"},
                new Object[]{EMPTY, INCLUDES_REGEX, EMPTY, "regx", true, "case5"},
                new Object[]{EMPTY, INCLUDES_REGEX, EMPTY, "excregx", true, "case5"},

                /**
                 * case6: Also similar to case 2; if include list is NOT empty and includesRegex is not null and
                 * exclude list is empty, only the ones specified in the include list or matching the regex should
                 * be included.
                 */
                new Object[]{INCLUDES, INCLUDES_REGEX, EMPTY, "inc", true, "case6"},
                new Object[]{INCLUDES, INCLUDES_REGEX, EMPTY, "both", true, "case6"},
                new Object[]{INCLUDES, INCLUDES_REGEX, EMPTY, "exc", false, "case6"},
                new Object[]{INCLUDES, INCLUDES_REGEX, EMPTY, "any", false, "case6"},
                new Object[]{INCLUDES, INCLUDES_REGEX, EMPTY, "all", true, "case6"},
                new Object[]{INCLUDES, INCLUDES_REGEX, EMPTY, "inc2", true, "case6"},
                new Object[]{INCLUDES, INCLUDES_REGEX, EMPTY, "regx", true, "case6"},
                new Object[]{INCLUDES, INCLUDES_REGEX, EMPTY, "excregx", true, "case6"},

                /**
                 * case7: Similar to case 4; if include list is NOT empty and includesRegex is not null and
                 * exclude list is empty, only the ones specified in the include list or matching the regex should
                 * be included.
                 */
                new Object[]{EMPTY, INCLUDES_REGEX, EXCLUDES, "inc", false, "case7"},
                new Object[]{EMPTY, INCLUDES_REGEX, EXCLUDES, "both", false, "case7"},
                new Object[]{EMPTY, INCLUDES_REGEX, EXCLUDES, "exc", false, "case7"},
                new Object[]{EMPTY, INCLUDES_REGEX, EXCLUDES, "any", false, "case7"},
                new Object[]{EMPTY, INCLUDES_REGEX, EXCLUDES, "all", false, "case7"},
                new Object[]{EMPTY, INCLUDES_REGEX, EXCLUDES, "inc2", true, "case7"},
                new Object[]{EMPTY, INCLUDES_REGEX, EXCLUDES, "regx", true, "case7"},
                new Object[]{EMPTY, INCLUDES_REGEX, EXCLUDES, "excregx", false, "case7"},

                /**
                 * case8: Also similar to case 4; if include list is NOT empty and includesRegex is not null and
                 * exclude list is specified, only the ones not excluded and also specified in the include list or
                 * matching the regex should be included.
                 */
                new Object[]{INCLUDES, INCLUDES_REGEX, EXCLUDES, "inc", true, "case8"},
                new Object[]{INCLUDES, INCLUDES_REGEX, EXCLUDES, "both", false, "case8"},
                new Object[]{INCLUDES, INCLUDES_REGEX, EXCLUDES, "exc", false, "case8"},
                new Object[]{INCLUDES, INCLUDES_REGEX, EXCLUDES, "any", false, "case8"},
                new Object[]{INCLUDES, INCLUDES_REGEX, EXCLUDES, "all", false, "case8"},
                new Object[]{INCLUDES, INCLUDES_REGEX, EXCLUDES, "inc2", true, "case8"},
                new Object[]{INCLUDES, INCLUDES_REGEX, EXCLUDES, "regx", true, "case8"},
                new Object[]{INCLUDES, INCLUDES_REGEX, EXCLUDES, "excregx", false, "case8"}

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
    public String includesRegex;

    @Parameterized.Parameter(2)
    public ImmutableSet<String> excludes;


    @Parameterized.Parameter(3)
    public String name;

    @Parameterized.Parameter(4)
    public boolean expected;

    @Parameterized.Parameter(5)
    public String msg;

    private final Metric metric = mock(Metric.class);

    @Test
    public void matches() {
        factory.setIncludes(includes);
        factory.setExcludes(excludes);
        factory.setIncludesRegex(includesRegex);

        assertThat(factory.getFilter().matches(name, metric))
                .overridingErrorMessage(msg + ": expected 'matches(%s)=%s'", name, expected)
                .isEqualTo(expected);
    }

}