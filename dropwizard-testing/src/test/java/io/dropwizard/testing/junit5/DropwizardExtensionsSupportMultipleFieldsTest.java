package io.dropwizard.testing.junit5;

import io.dropwizard.testing.junit5.helper.CallbackVerifyingTestExtension;
import io.dropwizard.testing.junit5.helper.CallbackVerifyingTestExtension.DelayedAssertionsTest;
import io.dropwizard.testing.junit5.helper.CallbackVerifyingTestExtension.Invokable;
import io.dropwizard.testing.junit5.helper.RecordingTestExtension;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

// Verifies DropwizardExtensionsSupport behavior when a test class declares multiple DropwizardExtension fields
@ExtendWith(CallbackVerifyingTestExtension.class)
@ExtendWith(DropwizardExtensionsSupport.class)
@SuppressWarnings("unused")
class DropwizardExtensionsSupportMultipleFieldsAllInvokedTest implements DelayedAssertionsTest {
    private final List<String> log = new ArrayList<>();
    private final List<Invokable> delayedAssertions = new ArrayList<>();

    private final RecordingTestExtension first = new RecordingTestExtension("A", log);
    private final RecordingTestExtension second = new RecordingTestExtension("B", log);

    @Override
    public List<Invokable> getDelayedAssertions() {
        return delayedAssertions;
    }

    @Test
    void bothFieldsFire() {
        // when
        //    (this test is running and not yet completed)
        // then
        //    both before() invocations must happen
        assertThat(log)
            .containsExactlyInAnyOrder("before:A", "before:B");

        delayedAssertions.add(() -> {
            // when
            //    (this test is completed)
            // then
            //    both after() invocations must happen
            assertThat(log)
                .containsExactlyInAnyOrder("before:A", "before:B", "after:A", "after:B");
        });
    }
}

// Verifies fields are invoked in declaration order by default (no @Order annotations)
@ExtendWith(CallbackVerifyingTestExtension.class)
@ExtendWith(DropwizardExtensionsSupport.class)
@SuppressWarnings("unused")
class DropwizardExtensionsSupportMultipleFieldsDeclarationOrderTest implements DelayedAssertionsTest {
    private final List<String> log = new ArrayList<>();
    private final List<Invokable> delayedAssertions = new ArrayList<>();

    private final RecordingTestExtension first = new RecordingTestExtension("first", log);
    private final RecordingTestExtension second = new RecordingTestExtension("second", log);
    private final RecordingTestExtension third = new RecordingTestExtension("third", log);

    @Override
    public List<Invokable> getDelayedAssertions() {
        return delayedAssertions;
    }

    @Test
    void beforeInDeclarationOrder_afterInReverse() {
        // when
        //    (this test is running and not yet completed)
        // then
        //    before() calls happen in declaration order.
        assertThat(log)
            .containsExactly("before:first", "before:second", "before:third");

        delayedAssertions.add(() -> {
            // when
            //    (this test is completed)
            // then
            //    after() calls happen in reverse declaration order for setup/teardown symmetry.
            assertThat(log)
                .containsExactly(
                    "before:first", "before:second", "before:third",
                    "after:third", "after:second", "after:first");
        });
    }
}

// Verifies @Order annotation on fields controls invocation order.
@ExtendWith(CallbackVerifyingTestExtension.class)
@ExtendWith(DropwizardExtensionsSupport.class)
@SuppressWarnings("unused")
class DropwizardExtensionsSupportMultipleFieldsOrderAnnotationTest implements DelayedAssertionsTest {
    private final List<String> log = new ArrayList<>();
    private final List<Invokable> delayedAssertions = new ArrayList<>();

    // Fields declared in non-@Order order; @Order values should override declaration order.
    @Order(3)
    private final RecordingTestExtension third = new RecordingTestExtension("third", log);

    @Order(1)
    private final RecordingTestExtension first = new RecordingTestExtension("first", log);

    @Order(2)
    private final RecordingTestExtension second = new RecordingTestExtension("second", log);

    @Override
    public List<Invokable> getDelayedAssertions() {
        return delayedAssertions;
    }

    @Test
    void orderAnnotationHonored() {
        // when
        //    (this test is running and not yet completed)
        // then
        assertThat(log)
            .containsExactly("before:first", "before:second", "before:third");

        delayedAssertions.add(() -> {
            // when
            //    (this test is completed)
            // then
            assertThat(log)
                .containsExactly(
                    "before:first", "before:second", "before:third",
                    "after:third", "after:second", "after:first");
        });
    }
}

// Verifies mixed @Order-annotated and un-annotated fields work sensibly.
// Convention (matching JUnit's @Order semantics): un-annotated fields get default order
// Integer.MAX_VALUE / 2 (middle), so they sort between explicitly low and explicitly high orders.
@ExtendWith(CallbackVerifyingTestExtension.class)
@ExtendWith(DropwizardExtensionsSupport.class)
@SuppressWarnings("unused")
class DropwizardExtensionsSupportMultipleFieldsMixedOrderTest implements DelayedAssertionsTest {
    private final List<String> log = new ArrayList<>();
    private final List<Invokable> delayedAssertions = new ArrayList<>();

    @Order(1)
    private final RecordingTestExtension explicitFirst = new RecordingTestExtension("explicit-first", log);

    // No @Order - falls to default position.
    private final RecordingTestExtension middle = new RecordingTestExtension("middle", log);

    @Order(Integer.MAX_VALUE)
    private final RecordingTestExtension explicitLast = new RecordingTestExtension("explicit-last", log);

    @Override
    public List<Invokable> getDelayedAssertions() {
        return delayedAssertions;
    }

    @Test
    void mixedOrderAnnotationsHandled() {
        // when
        //    (this test is running and not yet completed)
        // then
        assertThat(log)
            .containsExactly("before:explicit-first", "before:middle", "before:explicit-last");

        delayedAssertions.add(() -> {
            // when
            //    (this test is completed)
            // then
            assertThat(log)
                .containsExactly(
                    "before:explicit-first", "before:middle", "before:explicit-last",
                    "after:explicit-last", "after:middle", "after:explicit-first");
        });
    }
}
