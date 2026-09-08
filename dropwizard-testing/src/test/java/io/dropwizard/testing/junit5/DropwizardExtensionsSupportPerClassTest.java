package io.dropwizard.testing.junit5;

import io.dropwizard.testing.junit5.helper.CallbackVerifyingTestExtension;
import io.dropwizard.testing.junit5.helper.CallbackVerifyingTestExtension.DelayedAssertionsTest;
import io.dropwizard.testing.junit5.helper.CallbackVerifyingTestExtension.Invokable;
import io.dropwizard.testing.junit5.helper.CountingTestExtension;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

//
// Verifies DropwizardExtensionsSupport behavior for @TestInstance(PER_CLASS) lifecycle
//

// Base case: test class with @ExtendWith(DropwizardExtensionsSupport.class) but ZERO DropwizardExtension fields.
// Should be a no-op - no NPE, no crash, test body runs normally.
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(DropwizardExtensionsSupport.class)
class DropwizardExtensionsSupportPerClassEmptyClassTest {
    @Test
    void testBodyRunsWithNoExtensionFields() {
        // when
        //    (this test is running)
        // then
        //    DropwizardExtensionSupport tolerated a class with no @DropwizardExtension fields.
        assertThat(true)
            .isTrue();
    }
}

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(CallbackVerifyingTestExtension.class)
@ExtendWith(DropwizardExtensionsSupport.class)
class DropwizardExtensionsSupportPerClassTest implements DelayedAssertionsTest {
    private final CountingTestExtension extension = new CountingTestExtension();

    private final List<Invokable> delayedAssertions = new ArrayList<>();

    @Override
    public List<Invokable> getDelayedAssertions() {
        return delayedAssertions;
    }

    @Override
    public List<Invokable> getClassLevelDelayedAssertions() {
        return List.of(() -> {
            // when
            //    (the whole test class has completed and DropwizardExtensionsSupport.afterAll has fired)
            // then
            //    after() fired exactly once for the class's PER_CLASS lifetime.
            assertThat(extension.getBeforeInvocations())
                .isEqualTo(1);
            assertThat(extension.getAfterInvocations())
                .isEqualTo(1);
        });
    }

    @RepeatedTest(2)
    void beforeFiresOnceAndAfterHoldsAtZero() {
        // when
        //    (this test is running and not yet completed - potentially on the 2nd repetition)
        // then
        //    before() fired exactly once at beforeAll and has NOT re-fired for this test method.
        //    after() has not fired yet.
        assertThat(extension.getBeforeInvocations())
            .isEqualTo(1);
        assertThat(extension.getAfterInvocations())
            .isEqualTo(0);

        getDelayedAssertions().add(() -> {
            // when
            //    (this test is completed)
            // then
            //    Under PER_CLASS, after() fires at afterAll, not afterEach.
            assertThat(extension.getBeforeInvocations())
                .isEqualTo(1);
            assertThat(extension.getAfterInvocations())
                .isEqualTo(0);
        });
    }
}

// Parent class exposes an extension via an abstract getter; PER_CLASS lifecycle applies to the child.
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(CallbackVerifyingTestExtension.class)
abstract class DropwizardExtensionsSupportPerClassChildHasExtension implements DelayedAssertionsTest {
    protected abstract CountingTestExtension getExtension();

    private final List<Invokable> delayedAssertions = new ArrayList<>();

    @Override
    public List<Invokable> getDelayedAssertions() {
        return delayedAssertions;
    }

    @Override
    public List<Invokable> getClassLevelDelayedAssertions() {
        return List.of(() -> {
            // when
            //    (the whole test class has completed and DropwizardExtensionsSupport.afterAll has fired)
            // then
            //    after() fired exactly once for the class's PER_CLASS lifetime.
            assertThat(getExtension().getBeforeInvocations())
                .isEqualTo(1);
            assertThat(getExtension().getAfterInvocations())
                .isEqualTo(1);
        });
    }

    @Test
    void parentEntryA() {
        // when
        //    (this test is running and not yet completed)
        // then
        //    before() fired exactly once at beforeAll and has NOT re-fired for this test method.
        //    after() has not fired yet.
        assertThat(getExtension().getBeforeInvocations())
            .isEqualTo(1);
        assertThat(getExtension().getAfterInvocations())
            .isEqualTo(0);

        getDelayedAssertions().add(() -> {
            // when
            //    (this test is completed)
            // then
            //    Under PER_CLASS, after() fires at afterAll, not afterEach.
            assertThat(getExtension().getBeforeInvocations())
                .isEqualTo(1);
            assertThat(getExtension().getAfterInvocations())
                .isEqualTo(0);
        });
    }

    // Second test method - existence proves before() is not re-fired between test methods
    // (whichever runs first, the other still sees beforeInvocations == 1).
    @Test
    void parentEntryB() {
        // when
        //    (this test is running and not yet completed)
        // then
        //    same invariant as parentEntryA - before() fired exactly once at beforeAll.
        assertThat(getExtension().getBeforeInvocations())
            .isEqualTo(1);
        assertThat(getExtension().getAfterInvocations())
            .isEqualTo(0);

        getDelayedAssertions().add(() -> {
            // when
            //    (this test is completed)
            // then
            //    Under PER_CLASS, after() fires at afterAll, not afterEach.
            assertThat(getExtension().getBeforeInvocations())
                .isEqualTo(1);
            assertThat(getExtension().getAfterInvocations())
                .isEqualTo(0);
        });
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class NestedClassOnlyInParent implements DelayedAssertionsTest {
        @Override
        public List<Invokable> getDelayedAssertions() {
            return delayedAssertions;
        }

        @Test
        void nestedTestInParent() {
            // when
            //    (this test is running and not yet completed)
            // then
            assertThat(getExtension().getBeforeInvocations())
                .isEqualTo(1);
            assertThat(getExtension().getAfterInvocations())
                .isEqualTo(0);

            getDelayedAssertions().add(() -> {
                // when
                //    (this test is completed)
                // then
                //    Under PER_CLASS, after() fires at afterAll, not afterEach.
                assertThat(getExtension().getBeforeInvocations())
                    .isEqualTo(1);
                assertThat(getExtension().getAfterInvocations())
                    .isEqualTo(0);
            });
        }
    }
}

@ExtendWith(DropwizardExtensionsSupport.class)
class DropwizardExtensionsSupportPerClassChildHasExtensionInheritedTest
    extends DropwizardExtensionsSupportPerClassChildHasExtension {
    private final CountingTestExtension extension = new CountingTestExtension();

    @Override
    protected CountingTestExtension getExtension() {
        return extension;
    }

    @Test
    void childOwnTestMethod() {
        // when
        //    (this test is running and not yet completed)
        // then
        assertThat(getExtension().getBeforeInvocations())
            .isEqualTo(1);
        assertThat(getExtension().getAfterInvocations())
            .isEqualTo(0);

        getDelayedAssertions().add(() -> {
            // when
            //    (this test is completed)
            // then
            //    Under PER_CLASS, after() fires at afterAll, not afterEach.
            assertThat(getExtension().getBeforeInvocations())
                .isEqualTo(1);
            assertThat(getExtension().getAfterInvocations())
                .isEqualTo(0);
        });
    }
}

// Concrete class that has the extension AND contains two @Nested classes:
//   - a "standalone" nested class exercising the enclosing-instance walk
//   - an "inheriting" nested class that extends the abstract parent (mixing @Nested + inheritance)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(CallbackVerifyingTestExtension.class)
@ExtendWith(DropwizardExtensionsSupport.class)
class DropwizardExtensionsSupportPerClassChildHasExtensionNestedUseTest implements DelayedAssertionsTest {
    private final CountingTestExtension extension = new CountingTestExtension();
    private final List<Invokable> delayedAssertions = new ArrayList<>();

    @Override
    public List<Invokable> getDelayedAssertions() {
        return delayedAssertions;
    }

    @Override
    public List<Invokable> getClassLevelDelayedAssertions() {
        return List.of(() -> {
            // when
            //    (the whole test class has completed and DropwizardExtensionsSupport.afterAll has fired)
            // then
            //    after() fired exactly once for the class's PER_CLASS lifetime.
            assertThat(extension.getBeforeInvocations())
                .isEqualTo(1);
            assertThat(extension.getAfterInvocations())
                .isEqualTo(1);
        });
    }

    @Test
    void regularTestMethod() {
        // when
        //    (this test is running and not yet completed)
        // then
        assertThat(extension.getBeforeInvocations())
            .isEqualTo(1);
        assertThat(extension.getAfterInvocations())
            .isEqualTo(0);

        getDelayedAssertions().add(() -> {
            // when
            //    (this test is completed)
            // then
            //    Under PER_CLASS, after() fires at afterAll, not afterEach.
            assertThat(extension.getBeforeInvocations())
                .isEqualTo(1);
            assertThat(extension.getAfterInvocations())
                .isEqualTo(0);
        });
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class NestedClassStandalone implements DelayedAssertionsTest {
        @Override
        public List<Invokable> getDelayedAssertions() {
            return delayedAssertions;
        }

        @Test
        void nestedClassMethod() {
            // when
            //    (this test is running and not yet completed)
            // then
            //    Enclosing walk reaches outer, finds extension, drives it exactly once for outer's PER_CLASS lifetime.
            assertThat(extension.getBeforeInvocations())
                .isEqualTo(1);
            assertThat(extension.getAfterInvocations())
                .isEqualTo(0);

            getDelayedAssertions().add(() -> {
                // when
                //    (this test is completed)
                // then
                //    Under PER_CLASS, after() fires at afterAll, not afterEach.
                assertThat(extension.getBeforeInvocations())
                    .isEqualTo(1);
                assertThat(extension.getAfterInvocations())
                    .isEqualTo(0);
            });
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class NestedClassInheriting extends DropwizardExtensionsSupportPerClassChildHasExtension {
        @Override
        public List<Invokable> getDelayedAssertions() {
            return delayedAssertions;
        }

        @Override
        public List<Invokable> getClassLevelDelayedAssertions() {
            // Outer owns the extension's PER_CLASS lifetime. Outer's afterAll (not inner's) is when after() fires,
            // so the class-level assertion at inner afterAll would see after == 0. Skip; outer's own class-level
            // assertion covers the invariant.
            return Collections.emptyList();
        }

        @Override
        protected CountingTestExtension getExtension() {
            return extension;
        }

        @Test
        void childClassTestMethod() {
            // when
            //    (this test is running and not yet completed)
            // then
            assertThat(extension.getBeforeInvocations())
                .isEqualTo(1);
            assertThat(extension.getAfterInvocations())
                .isEqualTo(0);

            getDelayedAssertions().add(() -> {
                // when
                //    (this test is completed)
                // then
                //    Under PER_CLASS, after() fires at afterAll, not afterEach.
                assertThat(extension.getBeforeInvocations())
                    .isEqualTo(1);
                assertThat(extension.getAfterInvocations())
                    .isEqualTo(0);
            });
        }

        // Overrides the parent's @Test to prove that child-side body runs (not the parent's) while the
        // extension lifecycle still fires exactly once for the outer's PER_CLASS lifetime.
        @Test
        @Override
        void parentEntryA() {
            // when
            //    (this test is running and not yet completed)
            // then
            assertThat(extension.getBeforeInvocations())
                .isEqualTo(1);
            assertThat(extension.getAfterInvocations())
                .isEqualTo(0);

            getDelayedAssertions().add(() -> {
                // when
                //    (this test is completed)
                // then
                assertThat(extension.getBeforeInvocations())
                    .isEqualTo(1);
                assertThat(extension.getAfterInvocations())
                    .isEqualTo(0);
            });
        }
    }
}

// Parent class holds the extension; PER_CLASS applies. Verifies nested tests do not re-fire before().
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(CallbackVerifyingTestExtension.class)
@ExtendWith(DropwizardExtensionsSupport.class)
abstract class DropwizardExtensionsSupportPerClassParentHasExtension implements DelayedAssertionsTest {
    protected final CountingTestExtension extension = new CountingTestExtension();

    private final List<Invokable> delayedAssertions = new ArrayList<>();

    @Override
    public List<Invokable> getDelayedAssertions() {
        return delayedAssertions;
    }

    @Override
    public List<Invokable> getClassLevelDelayedAssertions() {
        return List.of(() -> {
            // when
            //    (the whole test class has completed and DropwizardExtensionsSupport.afterAll has fired)
            // then
            //    after() fired exactly once for the class's PER_CLASS lifetime.
            assertThat(extension.getBeforeInvocations())
                .isEqualTo(1);
            assertThat(extension.getAfterInvocations())
                .isEqualTo(1);
        });
    }

    @Test
    void parentClassTestMethod() {
        // when
        //    (this test is running and not yet completed)
        // then
        assertThat(extension.getBeforeInvocations())
            .isEqualTo(1);
        assertThat(extension.getAfterInvocations())
            .isEqualTo(0);

        getDelayedAssertions().add(() -> {
            // when
            //    (this test is completed)
            // then
            //    Under PER_CLASS, after() fires at afterAll, not afterEach.
            assertThat(extension.getBeforeInvocations())
                .isEqualTo(1);
            assertThat(extension.getAfterInvocations())
                .isEqualTo(0);
        });
    }

    @Test
    void overriddenTestMethod() {
        // when
        //    (this test is running and not yet completed)
        // then
        assertThat(extension.getBeforeInvocations())
            .isEqualTo(1);
        assertThat(extension.getAfterInvocations())
            .isEqualTo(0);

        getDelayedAssertions().add(() -> {
            // when
            //    (this test is completed)
            // then
            //    Under PER_CLASS, after() fires at afterAll, not afterEach.
            assertThat(extension.getBeforeInvocations())
                .isEqualTo(1);
            assertThat(extension.getAfterInvocations())
                .isEqualTo(0);
        });
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class NestedClassOnlyInParent implements DelayedAssertionsTest {
        @Override
        public List<Invokable> getDelayedAssertions() {
            return delayedAssertions;
        }

        @Test
        void onlyInParent() {
            // when
            //    (this test is running and not yet completed)
            // then
            //    Outer's before() fired once for PER_CLASS lifetime; nested test doesn't re-fire it.
            assertThat(extension.getBeforeInvocations())
                .isEqualTo(1);
            assertThat(extension.getAfterInvocations())
                .isEqualTo(0);

            getDelayedAssertions().add(() -> {
                // when
                //    (this test is completed)
                // then
                assertThat(extension.getBeforeInvocations())
                    .isEqualTo(1);
                assertThat(extension.getAfterInvocations())
                    .isEqualTo(0);
            });
        }
    }
}

class DropwizardExtensionsSupportPerClassParentHasExtensionNoOpWrapperTest {
    // Wraps ParentHasExtension for JUnit discovery (abstract class isn't run directly).
    @Nested
    class NestedForInheritance extends DropwizardExtensionsSupportPerClassParentHasExtension {
        @Test
        void childClassTestMethod() {
            // when
            //    (this test is running and not yet completed)
            // then
            assertThat(extension.getBeforeInvocations())
                .isEqualTo(1);
            assertThat(extension.getAfterInvocations())
                .isEqualTo(0);

            getDelayedAssertions().add(() -> {
                // when
                //    (this test is completed)
                // then
                assertThat(extension.getBeforeInvocations())
                    .isEqualTo(1);
                assertThat(extension.getAfterInvocations())
                    .isEqualTo(0);
            });
        }

        // Overrides the parent's @Test method - verifies that the child body runs (not the parent's)
        // and that DropwizardExtensionsSupport still drives before()/after() correctly on the shared
        // extension field inherited from the parent.
        @Test
        @Override
        void overriddenTestMethod() {
            // when
            //    (this test is running and not yet completed)
            // then
            assertThat(extension.getBeforeInvocations())
                .isEqualTo(1);
            assertThat(extension.getAfterInvocations())
                .isEqualTo(0);

            getDelayedAssertions().add(() -> {
                // when
                //    (this test is completed)
                // then
                assertThat(extension.getBeforeInvocations())
                    .isEqualTo(1);
                assertThat(extension.getAfterInvocations())
                    .isEqualTo(0);
            });
        }
    }
}

// Mixed lifecycle: outer PER_CLASS, inner PER_METHOD.
// Outer's non-static field is scoped to the outer's class lifetime (once).
// Inner's non-static field is scoped per method.
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(CallbackVerifyingTestExtension.class)
@ExtendWith(DropwizardExtensionsSupport.class)
class DropwizardExtensionsSupportPerClassMixedLifecycleTest implements DelayedAssertionsTest {
    private final CountingTestExtension outerExtension = new CountingTestExtension();
    private final List<Invokable> delayedAssertions = new ArrayList<>();

    @Override
    public List<Invokable> getDelayedAssertions() {
        return delayedAssertions;
    }

    @Override
    public List<Invokable> getClassLevelDelayedAssertions() {
        return List.of(() -> {
            // when
            //    (the whole outer test class has completed and DropwizardExtensionsSupport.afterAll has fired)
            // then
            //    outer's after() fired exactly once at outer afterAll.
            assertThat(outerExtension.getBeforeInvocations())
                .isEqualTo(1);
            assertThat(outerExtension.getAfterInvocations())
                .isEqualTo(1);
        });
    }

    @Test
    void outerTest() {
        // when
        //    (this test is running and not yet completed)
        // then
        assertThat(outerExtension.getBeforeInvocations())
            .isEqualTo(1);
        assertThat(outerExtension.getAfterInvocations())
            .isEqualTo(0);

        getDelayedAssertions().add(() -> {
            // when
            //    (this test is completed)
            // then
            //    Outer is PER_CLASS - after() fires at afterAll, not afterEach.
            assertThat(outerExtension.getBeforeInvocations())
                .isEqualTo(1);
            assertThat(outerExtension.getAfterInvocations())
                .isEqualTo(0);
        });
    }

    @Nested
    class NestedPerMethodInner implements DelayedAssertionsTest {
        private final CountingTestExtension innerExtension = new CountingTestExtension();

        @Override
        public List<Invokable> getDelayedAssertions() {
            return delayedAssertions;
        }

        @Test
        void innerTest() {
            // when
            //    (this test is running and not yet completed)
            // then
            //   Outer's before() fired once (at outer beforeAll, class-level scope).
            //   Inner's before() also fired once (at inner beforeEach, per-method scope).
            assertThat(outerExtension.getBeforeInvocations())
                .isEqualTo(1);
            assertThat(innerExtension.getBeforeInvocations())
                .isEqualTo(1);
            assertThat(innerExtension.getAfterInvocations())
                .isEqualTo(0);
            assertThat(outerExtension.getAfterInvocations())
                .isEqualTo(0);

            delayedAssertions.add(() -> {
                // when
                //    (this test is completed)
                // then
                //    inner is PER_METHOD (after fires now); outer is PER_CLASS (after fires at afterAll).
                assertThat(innerExtension.getAfterInvocations())
                    .isEqualTo(1);
                assertThat(outerExtension.getAfterInvocations())
                    .isEqualTo(0);
            });
        }
    }
}

// Static field alongside a non-static instance field under PER_CLASS. Both are class-scoped under PER_CLASS,
// so before() should fire exactly once on each at beforeAll, after() exactly once on each at afterAll.
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(CallbackVerifyingTestExtension.class)
@ExtendWith(DropwizardExtensionsSupport.class)
class DropwizardExtensionsSupportPerClassStaticAndInstanceFieldTest implements DelayedAssertionsTest {
    static final CountingTestExtension staticExtension = new CountingTestExtension();
    private final CountingTestExtension instanceExtension = new CountingTestExtension();

    private final List<Invokable> delayedAssertions = new ArrayList<>();

    @Override
    public List<Invokable> getDelayedAssertions() {
        return delayedAssertions;
    }

    @Override
    public List<Invokable> getClassLevelDelayedAssertions() {
        return List.of(() -> {
            // when
            //    (the whole test class has completed and DropwizardExtensionsSupport.afterAll has fired)
            // then
            //    Both static and instance extensions fired after() exactly once at afterAll.
            assertThat(staticExtension.getBeforeInvocations())
                .isEqualTo(1);
            assertThat(staticExtension.getAfterInvocations())
                .isEqualTo(1);
            assertThat(instanceExtension.getBeforeInvocations())
                .isEqualTo(1);
            assertThat(instanceExtension.getAfterInvocations())
                .isEqualTo(1);
        });
    }

    @RepeatedTest(2)
    void bothFieldsFireBeforeOnceAndHoldAfterAtZero() {
        // when
        //    (this test is running and not yet completed - potentially on the 2nd repetition)
        // then
        //    both fields' before() fired exactly once at beforeAll and have NOT re-fired for this test method.
        //    after() has not fired yet on either.
        assertThat(staticExtension.getBeforeInvocations())
            .isEqualTo(1);
        assertThat(staticExtension.getAfterInvocations())
            .isEqualTo(0);
        assertThat(instanceExtension.getBeforeInvocations())
            .isEqualTo(1);
        assertThat(instanceExtension.getAfterInvocations())
            .isEqualTo(0);

        getDelayedAssertions().add(() -> {
            // when
            //    (this test is completed)
            // then
            //    Under PER_CLASS, after() fires at afterAll, not afterEach.
            assertThat(staticExtension.getBeforeInvocations())
                .isEqualTo(1);
            assertThat(staticExtension.getAfterInvocations())
                .isEqualTo(0);
            assertThat(instanceExtension.getBeforeInvocations())
                .isEqualTo(1);
            assertThat(instanceExtension.getAfterInvocations())
                .isEqualTo(0);
        });
    }
}
