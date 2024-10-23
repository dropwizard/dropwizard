package io.dropwizard.metrics.common;

import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class BaseReporterFactoryTest {
    private static final Set<String> INCLUDES = Set.of("inc", "both", "inc.+");
    private static final Set<String> EXCLUDES = Set.of("exc", "both", "exc.+");
    private static final Set<String> EMPTY = Collections.emptySet();

    static Stream<Arguments> data() {

        return Stream.of(
                /*
                 * case1: If include list is empty and exclude list is empty, everything should be
                 * included.
                 */
                Arguments.of(EMPTY, EMPTY, "inc", true, true, true, "case1"),
                Arguments.of(EMPTY, EMPTY, "both", true, true, true, "case1"),
                Arguments.of(EMPTY, EMPTY, "exc", true, true, true, "case1"),
                Arguments.of(EMPTY, EMPTY, "any", true, true, true, "case1"),
                Arguments.of(EMPTY, EMPTY, "incWithSuffix", true, true, true, "case1"),
                Arguments.of(EMPTY, EMPTY, "excWithSuffix", true, true, true, "case1"),
                Arguments.of(EMPTY, EMPTY, "prefiXincSuffix", true, true, true, "case1"),

                /*
                 * case2: If include list is NOT empty and exclude list is empty, only the ones
                 * specified in the include list should be included.
                 */
                Arguments.of(INCLUDES, EMPTY, "inc", true, true, true, "case2"),
                Arguments.of(INCLUDES, EMPTY, "both", true, true, true, "case2"),
                Arguments.of(INCLUDES, EMPTY, "exc", false, false, false, "case2"),
                Arguments.of(INCLUDES, EMPTY, "any", false, false, false, "case2"),
                Arguments.of(INCLUDES, EMPTY, "incWithSuffix", false, true, true, "case2"),
                Arguments.of(INCLUDES, EMPTY, "excWithSuffix", false, false, false, "case2"),
                Arguments.of(INCLUDES, EMPTY, "prefiXincSuffix", false, false, true, "case2"),

                /*
                 * case3: If include list is empty and exclude list is NOT empty, everything should be
                 * included except the ones in the exclude list.
                 */
                Arguments.of(EMPTY, EXCLUDES, "inc", true, true, true, "case3"),
                Arguments.of(EMPTY, EXCLUDES, "both", false, false, false, "case3"),
                Arguments.of(EMPTY, EXCLUDES, "exc", false, false, false, "case3"),
                Arguments.of(EMPTY, EXCLUDES, "any", true, true, true, "case3"),
                Arguments.of(EMPTY, EXCLUDES, "incWithSuffix", true, true, true, "case3"),
                Arguments.of(EMPTY, EXCLUDES, "excWithSuffix", true, false, false, "case3"),
                Arguments.of(EMPTY, EXCLUDES, "prefiXincSuffix", true, true, true, "case3"),

                /*
                 * case4: If include list is NOT empty and exclude list is NOT empty, only things not excluded
                 * and specifically included should show up. Excludes take precedence.
                 */
                Arguments.of(INCLUDES, EXCLUDES, "inc", true, true, true, "case4"),
                Arguments.of(INCLUDES, EXCLUDES, "both", false, false, false, "case4"),
                Arguments.of(INCLUDES, EXCLUDES, "exc", false, false, false, "case4"),
                Arguments.of(INCLUDES, EXCLUDES, "any", false, false, false, "case4"),
                Arguments.of(INCLUDES, EXCLUDES, "incWithSuffix", false, true, true, "case4"),
                Arguments.of(INCLUDES, EXCLUDES, "excWithSuffix", false, false, false, "case4"),
                Arguments.of(INCLUDES, EXCLUDES, "prefiXincSuffix", false, false, true, "case4")
        );
    }

    private final BaseReporterFactory factory = new BaseReporterFactory() {
        @Override
        public ScheduledReporter build(MetricRegistry registry) {
            throw new UnsupportedOperationException("not implemented");
        }
    };

    private final Metric metric = mock(Metric.class);

    @ParameterizedTest
    @MethodSource("data")
    void testDefaultMatching(Set<String> includes, Set<String> excludes, String name,
                                    boolean expectedDefaultResult, boolean expectedRegexResult,
                                    boolean expectedSubstringResult, String msg) {
        factory.setIncludes(includes);
        factory.setExcludes(excludes);

        factory.setUseRegexFilters(false);
        factory.setUseSubstringMatching(false);
        assertThat(factory.getFilter().matches(name, metric))
                .overridingErrorMessage(msg + ": expected 'matches(%s)=%s' for default matcher", name, expectedDefaultResult)
                .isEqualTo(expectedDefaultResult);
    }

    @ParameterizedTest
    @MethodSource("data")
    void testRegexMatching(Set<String> includes, Set<String> excludes, String name,
                                  boolean expectedDefaultResult, boolean expectedRegexResult,
                                  boolean expectedSubstringResult, String msg) {
        factory.setIncludes(includes);
        factory.setExcludes(excludes);

        factory.setUseRegexFilters(true);
        factory.setUseSubstringMatching(false);
        assertThat(factory.getFilter().matches(name, metric))
                .overridingErrorMessage(msg + ": expected 'matches(%s)=%s' for regex matcher", name, expectedRegexResult)
                .isEqualTo(expectedRegexResult);
    }

    @ParameterizedTest
    @MethodSource("data")
    void tesSubstringMatching(Set<String> includes, Set<String> excludes, String name,
                                     boolean expectedDefaultResult, boolean expectedRegexResult,
                                     boolean expectedSubstringResult, String msg) {
        factory.setIncludes(includes);
        factory.setExcludes(excludes);

        factory.setUseRegexFilters(false);
        factory.setUseSubstringMatching(true);
        assertThat(factory.getFilter().matches(name, metric))
            .overridingErrorMessage(msg + ": expected 'matches(%s)=%s' for substring matcher", name, expectedSubstringResult)
            .isEqualTo(expectedSubstringResult);
    }
}
