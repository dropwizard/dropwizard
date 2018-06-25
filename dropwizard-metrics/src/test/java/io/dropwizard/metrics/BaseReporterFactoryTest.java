package io.dropwizard.metrics;

import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import io.dropwizard.util.Sets;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(Parameterized.class)
public class BaseReporterFactoryTest {
    private static final Set<String> INCLUDES = Sets.of("inc", "both", "inc.+");
    private static final Set<String> EXCLUDES = Sets.of("exc", "both", "exc.+");
    private static final Set<String> EMPTY = Collections.emptySet();

    @Parameterized.Parameters(name = "{index} {6} {2}")
    public static List<Object[]> data() {

        return Arrays.asList(
                /*
                 * case1: If include list is empty and exclude list is empty, everything should be
                 * included.
                 */
                new Object[]{EMPTY, EMPTY, "inc", true, true, true, "case1"},
                new Object[]{EMPTY, EMPTY, "both", true, true, true, "case1"},
                new Object[]{EMPTY, EMPTY, "exc", true, true, true, "case1"},
                new Object[]{EMPTY, EMPTY, "any", true, true, true, "case1"},
                new Object[]{EMPTY, EMPTY, "incWithSuffix", true, true, true, "case1"},
                new Object[]{EMPTY, EMPTY, "excWithSuffix", true, true, true, "case1"},
                new Object[]{EMPTY, EMPTY, "prefiXincSuffix", true, true, true, "case1"},

                /*
                 * case2: If include list is NOT empty and exclude list is empty, only the ones
                 * specified in the include list should be included.
                 */
                new Object[]{INCLUDES, EMPTY, "inc", true, true, true, "case2"},
                new Object[]{INCLUDES, EMPTY, "both", true, true, true, "case2"},
                new Object[]{INCLUDES, EMPTY, "exc", false, false, false, "case2"},
                new Object[]{INCLUDES, EMPTY, "any", false, false, false, "case2"},
                new Object[]{INCLUDES, EMPTY, "incWithSuffix", false, true, true, "case2"},
                new Object[]{INCLUDES, EMPTY, "excWithSuffix", false, false, false, "case2"},
                new Object[]{INCLUDES, EMPTY, "prefiXincSuffix", false, false, true, "case2"},

                /*
                 * case3: If include list is empty and exclude list is NOT empty, everything should be
                 * included except the ones in the exclude list.
                 */
                new Object[]{EMPTY, EXCLUDES, "inc", true, true, true, "case3"},
                new Object[]{EMPTY, EXCLUDES, "both", false, false, false, "case3"},
                new Object[]{EMPTY, EXCLUDES, "exc", false, false, false, "case3"},
                new Object[]{EMPTY, EXCLUDES, "any", true, true, true, "case3"},
                new Object[]{EMPTY, EXCLUDES, "incWithSuffix", true, true, true, "case3"},
                new Object[]{EMPTY, EXCLUDES, "excWithSuffix", true, false, false, "case3"},
                new Object[]{EMPTY, EXCLUDES, "prefiXincSuffix", true, true, true, "case3"},

                /*
                 * case4: If include list is NOT empty and exclude list is NOT empty, only things not excluded
                 * and specifically included should show up. Excludes takes precedence.
                 */
                new Object[]{INCLUDES, EXCLUDES, "inc", true, true, true, "case4"},
                new Object[]{INCLUDES, EXCLUDES, "both", false, false, false, "case4"},
                new Object[]{INCLUDES, EXCLUDES, "exc", false, false, false, "case4"},
                new Object[]{INCLUDES, EXCLUDES, "any", false, false, false, "case4"},
                new Object[]{INCLUDES, EXCLUDES, "incWithSuffix", false, true, true, "case4"},
                new Object[]{INCLUDES, EXCLUDES, "excWithSuffix", false, false, false, "case4"},
                new Object[]{INCLUDES, EXCLUDES, "prefiXincSuffix", false, false, true, "case4"}
        );
    }

    private final BaseReporterFactory factory = new BaseReporterFactory() {
        @Override
        public ScheduledReporter build(MetricRegistry registry) {
            throw new UnsupportedOperationException("not implemented");
        }
    };

    @Parameterized.Parameter
    public Set<String> includes = Collections.emptySet();

    @Parameterized.Parameter(1)
    public Set<String> excludes = Collections.emptySet();

    @Parameterized.Parameter(2)
    public String name = "";

    @Parameterized.Parameter(3)
    public boolean expectedDefaultResult;

    @Parameterized.Parameter(4)
    public boolean expectedRegexResult;

    @Parameterized.Parameter(5)
    public boolean expectedSubstringResult;

    @Parameterized.Parameter(6)
    public String msg = "";

    private final Metric metric = mock(Metric.class);

    @Test
    public void testDefaultMatching() {
        factory.setIncludes(includes);
        factory.setExcludes(excludes);

        factory.setUseRegexFilters(false);
        factory.setUseSubstringMatching(false);
        assertThat(factory.getFilter().matches(name, metric))
                .overridingErrorMessage(msg + ": expected 'matches(%s)=%s' for default matcher", name, expectedDefaultResult)
                .isEqualTo(expectedDefaultResult);
    }

    @Test
    public void testRegexMatching() {
        factory.setIncludes(includes);
        factory.setExcludes(excludes);

        factory.setUseRegexFilters(true);
        factory.setUseSubstringMatching(false);
        assertThat(factory.getFilter().matches(name, metric))
                .overridingErrorMessage(msg + ": expected 'matches(%s)=%s' for regex matcher", name, expectedRegexResult)
                .isEqualTo(expectedRegexResult);
    }

    @Test
    public void tesSubstringMatching() {
        factory.setIncludes(includes);
        factory.setExcludes(excludes);

        factory.setUseRegexFilters(false);
        factory.setUseSubstringMatching(true);
        assertThat(factory.getFilter().matches(name, metric))
            .overridingErrorMessage(msg + ": expected 'matches(%s)=%s' for substring matcher", name, expectedSubstringResult)
            .isEqualTo(expectedSubstringResult);
    }
}
