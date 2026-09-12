package io.dropwizard.testing.junit5.fixtures;

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.testing.junit5.helper.RecordingTestExtension;
import io.dropwizard.testing.junit5.helper.ThrowingTestExtension;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.List;

// Verifies DropwizardExtensionsSupport behavior for exception paths.
//
// Coverage:
//   #4 - exception during before(): does the corresponding after() still fire? Do sibling fields
//        still get their after() called?
//   #4 - exception during after(): do other fields' after() calls still happen? Is the test still
//        recorded as passed or failed?
//   #5 - @Test method throws: does after() still fire?
//   #6 - PER_CLASS + @Test throws: does after() still fire at afterAll?

// Tests in this file are run indirectly via other tests using EngineTestKit.
// Marked @Disabled at each inner static SubTest class to prevent IDE/Surefire from executing lifecycle
// callbacks (beforeAll/afterAll fire regardless of method-level @Disabled). The outer-class @Disabled
// documents intent but does not propagate to static nested classes for JUnit discovery purposes.
@Disabled("test-kit fixture: executed indirectly via EngineTestKit")
@SuppressWarnings("unused")
public class DropwizardExtensionsSupportExceptionTestFixture {
    @Disabled("test-kit fixture: executed indirectly via EngineTestKit")
    @ExtendWith(DropwizardExtensionsSupport.class)
    public static class BeforeThrowsSubTest {
        public static final List<String> log = new ArrayList<>();

        private final ThrowingTestExtension ext = new ThrowingTestExtension(
            "A",
            log,
            true,    // throwOnBefore
            false);  // throwOnAfter

        @Test
        void test() {
            log.add("test-body");
        }
    }

    @Disabled("test-kit fixture: executed indirectly via EngineTestKit")
    @ExtendWith(DropwizardExtensionsSupport.class)
    public static class AfterThrowsSubTest {
        public static final List<String> log = new ArrayList<>();

        private final ThrowingTestExtension ext = new ThrowingTestExtension(
            "A",
            log,
            false,  // throwOnBefore
            true);  // throwOnAfter

        @Test
        void test() {
            log.add("test-body");
        }
    }

    @Disabled("test-kit fixture: executed indirectly via EngineTestKit")
    @ExtendWith(DropwizardExtensionsSupport.class)
    public static class TestMethodThrowsSubTest {
        public static final List<String> log = new ArrayList<>();

        private final RecordingTestExtension ext = new RecordingTestExtension("A", log);

        @Test
        void test() {
            log.add("test-body");
            throw new RuntimeException("test-boom");
        }
    }

    @Disabled("test-kit fixture: executed indirectly via EngineTestKit")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @ExtendWith(DropwizardExtensionsSupport.class)
    public static class PerClassTestMethodThrowsSubTest {
        public static final List<String> log = new ArrayList<>();

        private final RecordingTestExtension ext = new RecordingTestExtension("A", log);

        @Test
        void test() {
            log.add("test-body");
            throw new RuntimeException("test-boom");
        }
    }

    @Disabled("test-kit fixture: executed indirectly via EngineTestKit")
    @ExtendWith(DropwizardExtensionsSupport.class)
    public static class MultiFieldFirstBeforeThrowsSubTest {
        public static final List<String> log = new ArrayList<>();

        // Declared first - before() throws.
        private final ThrowingTestExtension first = new ThrowingTestExtension(
            "first",
            log,
            true,    // throwOnBefore
            false);  // throwOnAfter

        // Declared second - should not have its before() invoked if the first's before() throws
        // (fail-fast semantics), and correspondingly should not have its after() invoked.
        private final RecordingTestExtension second = new RecordingTestExtension("second", log);

        @Test
        void test() {
            log.add("test-body");
        }
    }

    @Disabled("test-kit fixture: executed indirectly via EngineTestKit")
    @ExtendWith(DropwizardExtensionsSupport.class)
    public static class MultiFieldMiddleAfterThrowsSubTest {
        public static final List<String> log = new ArrayList<>();

        private final RecordingTestExtension first = new RecordingTestExtension("first", log);
        private final ThrowingTestExtension middle = new ThrowingTestExtension(
            "middle",
            log,
            false,  // throwOnBefore
            true);  // throwOnAfter
        private final RecordingTestExtension last = new RecordingTestExtension("last", log);

        @Test
        void test() {
            log.add("test-body");
        }
    }

    // Test method throws AssertionError (an Error, not a RuntimeException). Exercises the code path where a test
    // fails via failed assertion. Distinguished from RuntimeException coverage because AssertionError extends Error,
    // and if DropwizardExtensionSupport's cleanup wrapper catches only Exception, this would leak.
    @Disabled("test-kit fixture: executed indirectly via EngineTestKit")
    @ExtendWith(DropwizardExtensionsSupport.class)
    public static class TestMethodThrowsAssertionErrorSubTest {
        public static final List<String> log = new ArrayList<>();

        private final RecordingTestExtension ext = new RecordingTestExtension("A", log);

        @Test
        void test() {
            log.add("test-body");
            throw new AssertionError("simulated assertion failure");
        }
    }

    // Outer test class is PER_CLASS with a DropwizardExtension. Nested inner @Nested test class throws when its
    // @Test runs. Verifies the outer's after() still fires at outer's afterAll despite the nested test failure.
    @Disabled("test-kit fixture: executed indirectly via EngineTestKit")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @ExtendWith(DropwizardExtensionsSupport.class)
    public static class PerClassNestedTestThrowsSubTest {
        public static final List<String> log = new ArrayList<>();

        private final RecordingTestExtension outerExt = new RecordingTestExtension("outer", log);

        @Nested
        public class Inner {
            @Test
            void innerTestThatThrows() {
                log.add("inner-test-body");
                throw new RuntimeException("simulated inner failure");
            }
        }
    }

    // PER_CLASS with a non-static DropwizardExtension whose after() throws at afterAll. Exercises the
    // non-static branch of invokeAfterAll - verifies the thrown exception surfaces to the test framework.
    @Disabled("test-kit fixture: executed indirectly via EngineTestKit")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @ExtendWith(DropwizardExtensionsSupport.class)
    public static class PerClassInstanceAfterThrowsSubTest {
        public static final List<String> log = new ArrayList<>();

        private final ThrowingTestExtension ext = new ThrowingTestExtension(
            "A",
            log,
            false,  // throwOnBefore
            true);  // throwOnAfter

        @Test
        void test() {
            log.add("test-body");
        }
    }

    // Static DropwizardExtension whose after() throws at afterAll. Exercises the static branch of
    // invokeAfterAll - verifies the thrown exception surfaces to the test framework.
    @Disabled("test-kit fixture: executed indirectly via EngineTestKit")
    @ExtendWith(DropwizardExtensionsSupport.class)
    public static class StaticAfterThrowsSubTest {
        public static final List<String> log = new ArrayList<>();

        private static final ThrowingTestExtension ext = new ThrowingTestExtension(
            "A",
            log,
            false,  // throwOnBefore
            true);  // throwOnAfter

        @Test
        void test() {
            log.add("test-body");
        }
    }
}
