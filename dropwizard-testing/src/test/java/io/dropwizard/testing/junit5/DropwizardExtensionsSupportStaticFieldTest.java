package io.dropwizard.testing.junit5;

import io.dropwizard.testing.junit5.helper.CallbackVerifyingTestExtension;
import io.dropwizard.testing.junit5.helper.CallbackVerifyingTestExtension.DelayedAssertionsTest;
import io.dropwizard.testing.junit5.helper.CallbackVerifyingTestExtension.Invokable;
import io.dropwizard.testing.junit5.helper.CountingTestExtension;
import io.dropwizard.testing.junit5.helper.RecordingTestExtension;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

//
// Verifies DropwizardExtensionsSupport behavior for static DropwizardExtension fields.
//
// Static fields are class-scoped by nature - JUnit fires @BeforeAll/@AfterAll once per class, so before()/after()
// should each fire exactly once per class run regardless of @TestInstance lifecycle. That's why static-only
// coverage lives in its own file rather than being split across the PER_METHOD / PER_CLASS files.
//
// Coverage:
//   - Static field on a simple top-level class.
//   - Static field on an abstract parent, inherited by a concrete subclass (superclass walk).
//   - Static field declared inside a @Nested inner class.
//   - Multiple static fields: declaration order, @Order annotation, mixed.
//
// Uses the static-holder class-level delayed-assertion path (staticClassLevelDelayedAssertions()) to observe
// after() firing at afterAll time. The instance-based getClassLevelDelayedAssertions() path is unreachable under
// PER_METHOD (no persistent test instance exists at afterAll).

// Simple top-level class with one static DropwizardExtension field.
@ExtendWith(CallbackVerifyingTestExtension.class)
@ExtendWith(DropwizardExtensionsSupport.class)
class DropwizardExtensionsSupportStaticFieldSingleClassTest implements DelayedAssertionsTest {
    static final CountingTestExtension extension = new CountingTestExtension();

    private final List<Invokable> delayedAssertions = new ArrayList<>();

    @Override
    public List<Invokable> getDelayedAssertions() {
        return delayedAssertions;
    }

    @SuppressWarnings("unused")
    public static List<Invokable> staticClassLevelDelayedAssertions() {
        return List.of(() -> {
            // when
            //    (the whole test class has completed and DropwizardExtensionsSupport.afterAll has fired)
            // then
            //    before() and after() fired exactly once each for the class's lifetime.
            assertThat(extension.getBeforeInvocations())
                .isEqualTo(1);
            assertThat(extension.getAfterInvocations())
                .isEqualTo(1);
        });
    }

    @RepeatedTest(2)
    void staticBeforeFiresOnceAndAfterHoldsAtZero() {
        // when
        //    (this test is running and not yet completed - potentially on the 2nd repetition; the static field's
        //     counter persists across test-instance recreation)
        // then
        //    static before() fired exactly once at beforeAll and has NOT re-fired for this test method.
        //    after() has not fired yet - fires at afterAll.
        assertThat(extension.getBeforeInvocations())
            .isEqualTo(1);
        assertThat(extension.getAfterInvocations())
            .isEqualTo(0);

        getDelayedAssertions().add(() -> {
            // when
            //    (this test is completed)
            // then
            //    Static field's after() has not fired yet - only fires at afterAll.
            assertThat(extension.getBeforeInvocations())
                .isEqualTo(1);
            assertThat(extension.getAfterInvocations())
                .isEqualTo(0);
        });
    }
}

// Static field on an abstract parent class, inherited by a concrete subclass.
// Exercises findAnnotatedFields's superclass walk on the static branch.
abstract class DropwizardExtensionsSupportStaticFieldAbstractParent {
    static final CountingTestExtension extension = new CountingTestExtension();
}

@ExtendWith(CallbackVerifyingTestExtension.class)
@ExtendWith(DropwizardExtensionsSupport.class)
class DropwizardExtensionsSupportStaticFieldInheritedTest
    extends DropwizardExtensionsSupportStaticFieldAbstractParent
    implements DelayedAssertionsTest {

    private final List<Invokable> delayedAssertions = new ArrayList<>();

    @Override
    public List<Invokable> getDelayedAssertions() {
        return delayedAssertions;
    }

    @SuppressWarnings("unused")
    public static List<Invokable> staticClassLevelDelayedAssertions() {
        return List.of(() -> {
            // then
            //    Static field inherited from abstract parent fired before()/after() exactly once.
            assertThat(extension.getBeforeInvocations())
                .isEqualTo(1);
            assertThat(extension.getAfterInvocations())
                .isEqualTo(1);
        });
    }

    @Test
    void inheritedStaticIsDiscovered() {
        // when
        //    (this test is running and not yet completed)
        // then
        //    findAnnotatedFields walked the superclass chain and found the abstract parent's static field.
        assertThat(extension.getBeforeInvocations())
            .isEqualTo(1);
        assertThat(extension.getAfterInvocations())
            .isEqualTo(0);

        getDelayedAssertions().add(() -> {
            // when
            //    (this test is completed)
            // then
            //    Static field's after() has not fired yet - only fires at afterAll.
            assertThat(extension.getBeforeInvocations())
                .isEqualTo(1);
            assertThat(extension.getAfterInvocations())
                .isEqualTo(0);
        });
    }
}

// Static field declared inside a @Nested inner class. JUnit fires @BeforeAll/@AfterAll once per class-level in the
// tree, so a static declared on the inner class should be driven when the inner runs.
@ExtendWith(CallbackVerifyingTestExtension.class)
@ExtendWith(DropwizardExtensionsSupport.class)
class DropwizardExtensionsSupportStaticFieldOnNestedClassTest {

    @Nested
    class InnerWithOwnStatic implements DelayedAssertionsTest {
        static final CountingTestExtension innerExtension = new CountingTestExtension();

        private final List<Invokable> delayedAssertions = new ArrayList<>();

        @Override
        public List<Invokable> getDelayedAssertions() {
            return delayedAssertions;
        }

        @SuppressWarnings("unused")
        public static List<Invokable> staticClassLevelDelayedAssertions() {
            return List.of(() -> {
                // then
                //    Static declared on this @Nested class fired before()/after() exactly once for the nested class's
                //    class-level lifetime.
                assertThat(innerExtension.getBeforeInvocations())
                    .isEqualTo(1);
                assertThat(innerExtension.getAfterInvocations())
                    .isEqualTo(1);
            });
        }

        @Test
        void innerStaticIsDriven() {
            // when
            //    (this test is running and not yet completed)
            // then
            //    Static declared inside the @Nested class was found and driven.
            assertThat(innerExtension.getBeforeInvocations())
                .isEqualTo(1);
            assertThat(innerExtension.getAfterInvocations())
                .isEqualTo(0);

            getDelayedAssertions().add(() -> {
                // when
                //    (this test is completed)
                // then
                assertThat(innerExtension.getBeforeInvocations())
                    .isEqualTo(1);
                assertThat(innerExtension.getAfterInvocations())
                    .isEqualTo(0);
            });
        }
    }
}

// Multiple static fields, no @Order - verifies declaration-order iteration.
@ExtendWith(CallbackVerifyingTestExtension.class)
@ExtendWith(DropwizardExtensionsSupport.class)
@SuppressWarnings("unused")
class DropwizardExtensionsSupportMultipleStaticFieldsDeclarationOrderTest implements DelayedAssertionsTest {
    static final List<String> log = new ArrayList<>();

    static final RecordingTestExtension first = new RecordingTestExtension("first", log);
    static final RecordingTestExtension second = new RecordingTestExtension("second", log);
    static final RecordingTestExtension third = new RecordingTestExtension("third", log);

    private final List<Invokable> delayedAssertions = new ArrayList<>();

    @Override
    public List<Invokable> getDelayedAssertions() {
        return delayedAssertions;
    }

    public static List<Invokable> staticClassLevelDelayedAssertions() {
        return List.of(() -> {
            // then
            //    All three fields fired before() in declaration order at beforeAll,
            //    then all three fired after() in REVERSE declaration order at afterAll (setup/teardown symmetry).
            assertThat(log)
                .containsExactly(
                    "before:first", "before:second", "before:third",
                    "after:third", "after:second", "after:first");
        });
    }

    @Test
    void allThreeFieldsFiredBeforeInDeclarationOrder() {
        // when
        //    (this test is running and not yet completed - potentially on the 2nd repetition, but the log is
        //     class-scoped and captures beforeAll ordering, not per-test firings)
        // then
        //    before() calls happened in declaration order.
        assertThat(log)
            .containsExactly("before:first", "before:second", "before:third");
    }
}

// Multiple static fields with @Order annotation - verifies @Order overrides declaration order.
@ExtendWith(CallbackVerifyingTestExtension.class)
@ExtendWith(DropwizardExtensionsSupport.class)
@SuppressWarnings("unused")
class DropwizardExtensionsSupportMultipleStaticFieldsOrderAnnotationTest implements DelayedAssertionsTest {
    static final List<String> log = new ArrayList<>();

    // Fields declared in non-@Order order; @Order values should override declaration order.
    @Order(3)
    static final RecordingTestExtension third = new RecordingTestExtension("third", log);

    @Order(1)
    static final RecordingTestExtension first = new RecordingTestExtension("first", log);

    @Order(2)
    static final RecordingTestExtension second = new RecordingTestExtension("second", log);

    private final List<Invokable> delayedAssertions = new ArrayList<>();

    @Override
    public List<Invokable> getDelayedAssertions() {
        return delayedAssertions;
    }

    public static List<Invokable> staticClassLevelDelayedAssertions() {
        return List.of(() ->
            assertThat(log)
                .containsExactly(
                    "before:first", "before:second", "before:third",
                    "after:third", "after:second", "after:first"));
    }

    @Test
    void orderAnnotationHonored() {
        // then
        //    before() calls fired in @Order sequence, not declaration order.
        assertThat(log)
            .containsExactly("before:first", "before:second", "before:third");
    }
}

// Mixed @Order-annotated and un-annotated static fields.
// Un-annotated fields get default order Integer.MAX_VALUE / 2 (middle), so they sort between explicitly low and
// explicitly high orders.
@ExtendWith(CallbackVerifyingTestExtension.class)
@ExtendWith(DropwizardExtensionsSupport.class)
@SuppressWarnings("unused")
class DropwizardExtensionsSupportMultipleStaticFieldsMixedOrderTest implements DelayedAssertionsTest {
    static final List<String> log = new ArrayList<>();

    @Order(1)
    static final RecordingTestExtension explicitFirst = new RecordingTestExtension("explicit-first", log);

    // No @Order - falls to default position.
    static final RecordingTestExtension middle = new RecordingTestExtension("middle", log);

    @Order(Integer.MAX_VALUE)
    static final RecordingTestExtension explicitLast = new RecordingTestExtension("explicit-last", log);

    private final List<Invokable> delayedAssertions = new ArrayList<>();

    @Override
    public List<Invokable> getDelayedAssertions() {
        return delayedAssertions;
    }

    public static List<Invokable> staticClassLevelDelayedAssertions() {
        return List.of(() ->
            assertThat(log)
                .containsExactly(
                    "before:explicit-first", "before:middle", "before:explicit-last",
                    "after:explicit-last", "after:middle", "after:explicit-first"));
    }

    @Test
    void mixedOrderAnnotationsHandled() {
        // then
        assertThat(log)
            .containsExactly("before:explicit-first", "before:middle", "before:explicit-last");
    }
}
