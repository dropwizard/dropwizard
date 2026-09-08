package io.dropwizard.testing.junit5;

import io.dropwizard.testing.junit5.fixtures.DropwizardExtensionsSupportExceptionTestFixture.AfterThrowsSubTest;
import io.dropwizard.testing.junit5.fixtures.DropwizardExtensionsSupportExceptionTestFixture.BeforeThrowsSubTest;
import io.dropwizard.testing.junit5.fixtures.DropwizardExtensionsSupportExceptionTestFixture.MultiFieldFirstBeforeThrowsSubTest;
import io.dropwizard.testing.junit5.fixtures.DropwizardExtensionsSupportExceptionTestFixture.MultiFieldMiddleAfterThrowsSubTest;
import io.dropwizard.testing.junit5.fixtures.DropwizardExtensionsSupportExceptionTestFixture.PerClassInstanceAfterThrowsSubTest;
import io.dropwizard.testing.junit5.fixtures.DropwizardExtensionsSupportExceptionTestFixture.PerClassNestedTestThrowsSubTest;
import io.dropwizard.testing.junit5.fixtures.DropwizardExtensionsSupportExceptionTestFixture.PerClassTestMethodThrowsSubTest;
import io.dropwizard.testing.junit5.fixtures.DropwizardExtensionsSupportExceptionTestFixture.StaticAfterThrowsSubTest;
import io.dropwizard.testing.junit5.fixtures.DropwizardExtensionsSupportExceptionTestFixture.TestMethodThrowsAssertionErrorSubTest;
import io.dropwizard.testing.junit5.fixtures.DropwizardExtensionsSupportExceptionTestFixture.TestMethodThrowsSubTest;
import org.junit.jupiter.api.Test;
import org.junit.platform.testkit.engine.EngineExecutionResults;
import org.junit.platform.testkit.engine.EngineTestKit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

// Verifies DropwizardExtensionsSupport behavior for exception paths.
//
// These tests use JUnit's EngineTestKit to launch inner "fixture" classes and inspect the resulting execution events,
// since the failure conditions we want to observe would fail the enclosing test method if run normally.
// See: DropwizardExtensionsSupportExceptionTestFixture.
class DropwizardExtensionsSupportExceptionTest {
    // Configures an EngineTestKit builder to allow @Disabled tests to run. @Disabled prevents IDEs from trying to run
    // tests that need to be run only via EngineTestKit.
    private static EngineTestKit.Builder engine() {
        return EngineTestKit.engine("junit-jupiter")
            .configurationParameter(
                "junit.jupiter.conditions.deactivate",
                "org.junit.jupiter.engine.extension.DisabledCondition");
    }

    @Test
    void whenBeforeThrows_afterIsNotCalled_forSameExtension() {
        BeforeThrowsSubTest.log.clear();

        // when
        //    the fixture test is expected to fail (before method threw)
        final EngineExecutionResults results = engine()
            .selectors(selectClass(BeforeThrowsSubTest.class))
            .execute();

        // then
        results.testEvents()
            .assertStatistics(stats -> stats.failed(1).succeeded(0));
        //    JUnit's AfterEachCallback contract: fires only for extensions whose BeforeEachCallback completed.
        //    DropwizardExtensionSupport's beforeEach threw; afterEach does NOT run, so after() is NOT called on the
        //    extension. The test body also did not run.
        assertThat(BeforeThrowsSubTest.log)
            .containsExactly("before:A");
    }

    @Test
    void whenAfterThrows_testIsMarkedFailed() {
        AfterThrowsSubTest.log.clear();

        // when
        final EngineExecutionResults results = engine()
            .selectors(selectClass(AfterThrowsSubTest.class))
            .execute();

        // then
        results.testEvents()
            .assertStatistics(stats -> stats.failed(1));
        //    both before() and after() (and the test body) ran; after() threw
        assertThat(AfterThrowsSubTest.log)
            .containsExactly("before:A", "test-body", "after:A");
    }

    @Test
    void whenTestMethodThrows_afterStillFires() {
        TestMethodThrowsSubTest.log.clear();

        // when
        final EngineExecutionResults results = engine()
            .selectors(selectClass(TestMethodThrowsSubTest.class))
            .execute();

        // then
        results.testEvents()
            .assertStatistics(stats -> stats.failed(1));
        //    even though the test body threw, after() fired
        assertThat(TestMethodThrowsSubTest.log)
            .containsExactly("before:A", "test-body", "after:A");
    }

    @Test
    void perClass_whenTestMethodThrows_afterStillFiresAtAfterAll() {
        PerClassTestMethodThrowsSubTest.log.clear();

        // when
        final EngineExecutionResults results = engine()
            .selectors(selectClass(PerClassTestMethodThrowsSubTest.class))
            .execute();

        // then
        results.testEvents()
            .assertStatistics(stats -> stats.failed(1));
        //    before() at beforeAll, test body, after() at afterAll
        assertThat(PerClassTestMethodThrowsSubTest.log)
            .containsExactly("before:A", "test-body", "after:A");
    }

    @Test
    void multiField_whenFirstBeforeThrows_subsequentBeforesAndAllAftersSkipped() {
        MultiFieldFirstBeforeThrowsSubTest.log.clear();

        // when
        final EngineExecutionResults results = engine()
            .selectors(selectClass(MultiFieldFirstBeforeThrowsSubTest.class))
            .execute();

        // then
        results.testEvents()
            .assertStatistics(stats -> stats.failed(1));
        //    First field's before() threw, so DropwizardExtensionSupport's beforeEach threw. Per JUnit contract,
        //    afterEach then does NOT run, so no after() calls fire on any field. Subsequent before() calls are also
        //    not made.
        //    Result: only the first field's before() shows in the log.
        assertThat(MultiFieldFirstBeforeThrowsSubTest.log)
            .containsExactly("before:first");
    }

    @Test
    void multiField_whenMiddleAfterThrows_otherAftersStillFire() {
        MultiFieldMiddleAfterThrowsSubTest.log.clear();

        // when
        final EngineExecutionResults results = engine()
            .selectors(selectClass(MultiFieldMiddleAfterThrowsSubTest.class))
            .execute();

        // then
        results.testEvents()
            .assertStatistics(stats -> stats.failed(1));
        //    if one field's after() throws, other fields' after() calls still happen.
        assertThat(MultiFieldMiddleAfterThrowsSubTest.log)
            .containsExactly(
                "before:first", "before:middle", "before:last",
                "test-body",
                "after:last", "after:middle", "after:first");
    }

    @Test
    void whenTestMethodThrowsAssertionError_afterStillFires() {
        TestMethodThrowsAssertionErrorSubTest.log.clear();

        // when
        //    a test that fails via AssertionError (the common assertion-failure path). Distinguished from
        //    RuntimeException coverage: AssertionError extends Error, and a cleanup wrapper catching only Exception
        //    would leak. This asserts DropwizardExtensionSupport's cleanup catches Throwable / AssertionError.
        final EngineExecutionResults results = engine()
            .selectors(selectClass(TestMethodThrowsAssertionErrorSubTest.class))
            .execute();

        // then
        results.testEvents()
            .assertStatistics(stats -> stats.failed(1));
        //    before(), test body (which threw AssertionError), and after() all ran.
        assertThat(TestMethodThrowsAssertionErrorSubTest.log)
            .containsExactly("before:A", "test-body", "after:A");
    }

    @Test
    void perClass_whenNestedTestThrows_outerAfterStillFiresAtOuterAfterAll() {
        PerClassNestedTestThrowsSubTest.log.clear();

        // when
        //    an outer PER_CLASS class with a DropwizardExtension. A nested @Nested test throws. Verifies the outer's
        //    after() still fires at outer's afterAll despite the nested-test failure.
        final EngineExecutionResults results = engine()
            .selectors(selectClass(PerClassNestedTestThrowsSubTest.class))
            .execute();

        // then
        results.testEvents()
            .assertStatistics(stats -> stats.failed(1));
        //    outer's before() at outer beforeAll, nested test body ran and threw, outer's after() at outer afterAll.
        assertThat(PerClassNestedTestThrowsSubTest.log)
            .containsExactly("before:outer", "inner-test-body", "after:outer");
    }

    @Test
    void perClass_whenInstanceFieldAfterThrowsAtAfterAll_failureSurfaces() {
        PerClassInstanceAfterThrowsSubTest.log.clear();

        // when
        //    PER_CLASS with a non-static DropwizardExtension whose after() throws at afterAll.
        final EngineExecutionResults results = engine()
            .selectors(selectClass(PerClassInstanceAfterThrowsSubTest.class))
            .execute();

        // then
        //    The test body itself completes normally (afterAll runs after the test), so the test event is
        //    successful. The afterAll failure surfaces as a container failure.
        results.testEvents()
            .assertStatistics(stats -> stats.succeeded(1).failed(0));
        results.containerEvents()
            .assertStatistics(stats -> stats.failed(1));
        //    before() fired at beforeAll, test body ran, after() fired at afterAll (and threw).
        assertThat(PerClassInstanceAfterThrowsSubTest.log)
            .containsExactly("before:A", "test-body", "after:A");
    }

    @Test
    void staticField_whenAfterThrowsAtAfterAll_failureSurfaces() {
        StaticAfterThrowsSubTest.log.clear();

        // when
        //    Static DropwizardExtension whose after() throws at afterAll.
        final EngineExecutionResults results = engine()
            .selectors(selectClass(StaticAfterThrowsSubTest.class))
            .execute();

        // then
        //    Test body itself passes; afterAll failure surfaces as a container failure.
        results.testEvents()
            .assertStatistics(stats -> stats.succeeded(1).failed(0));
        results.containerEvents()
            .assertStatistics(stats -> stats.failed(1));
        //    Static before() at beforeAll, test body, static after() at afterAll (and threw).
        assertThat(StaticAfterThrowsSubTest.log)
            .containsExactly("before:A", "test-body", "after:A");
    }
}
