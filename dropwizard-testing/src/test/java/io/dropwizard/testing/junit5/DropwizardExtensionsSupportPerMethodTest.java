package io.dropwizard.testing.junit5;

import io.dropwizard.testing.junit5.helper.CallbackVerifyingTestExtension;
import io.dropwizard.testing.junit5.helper.CallbackVerifyingTestExtension.DelayedAssertionsTest;
import io.dropwizard.testing.junit5.helper.CallbackVerifyingTestExtension.Invokable;
import io.dropwizard.testing.junit5.helper.CountingTestExtension;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

//
// Verifies DropwizardExtensionsSupport behavior for @TestInstance(PER_METHOD) lifecycle
//

// Base case: test class with @ExtendWith(DropwizardExtensionsSupport.class) but ZERO DropwizardExtension fields.
// Should be a no-op - no NPE, no crash, test body runs normally.
@ExtendWith(DropwizardExtensionsSupport.class)
class DropwizardExtensionsSupportPerMethodEmptyClassTest {
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

@ExtendWith(CallbackVerifyingTestExtension.class)
@ExtendWith(DropwizardExtensionsSupport.class)
class DropwizardExtensionsSupportPerMethodTest implements DelayedAssertionsTest {
    private final CountingTestExtension extension = new CountingTestExtension();

    private final List<Invokable> delayedAssertions = new ArrayList<>();

    @Override
    public List<Invokable> getDelayedAssertions() {
        return delayedAssertions;
    }

    @RepeatedTest(2)
    void beforeFiresOnceAndAfterFiresOncePerMethod() {
        // when
        //    (this test is running and not yet completed - potentially on the 2nd repetition,
        //     which under PER_METHOD gets a fresh test instance and fresh extension)
        // then
        //    before() fired exactly once at beforeEach for this test method.
        //    after() has not fired yet.
        assertThat(extension.getBeforeInvocations())
            .isEqualTo(1);
        assertThat(extension.getAfterInvocations())
            .isEqualTo(0);

        getDelayedAssertions().add(() -> {
            // when
            //    (this test is completed)
            // then
            //    Under PER_METHOD, after() fires at afterEach.
            assertThat(extension.getBeforeInvocations())
                .isEqualTo(1);
            assertThat(extension.getAfterInvocations())
                .isEqualTo(1);
        });
    }
}

@ExtendWith(CallbackVerifyingTestExtension.class)
abstract class DropwizardExtensionsSupportPerMethodChildHasExtension implements DelayedAssertionsTest {
    protected abstract CountingTestExtension getExtension();

    private final List<Invokable> delayedAssertions = new ArrayList<>();

    @Override
    public List<Invokable> getDelayedAssertions() {
        return delayedAssertions;
    }

    @Test
    void parentEntryA() {
        // when
        //    (this test is running and not yet completed)
        // then
        //    before() fired exactly once at beforeEach for this test method.
        //    after() has not fired yet.
        assertThat(getExtension().getBeforeInvocations())
            .isEqualTo(1);
        assertThat(getExtension().getAfterInvocations())
            .isEqualTo(0);

        getDelayedAssertions().add(() -> {
            // when
            //    (this test is completed)
            // then
            assertThat(getExtension().getBeforeInvocations())
                .isEqualTo(1);
            assertThat(getExtension().getAfterInvocations())
                .isEqualTo(1);
        });
    }

    // Second test method - existence proves before() fires exactly once per test method under PER_METHOD
    // (whichever runs first, the other still sees beforeInvocations == 1 on entry).
    @Test
    void parentEntryB() {
        // when
        //    (this test is running and not yet completed)
        // then
        //    same invariant as parentEntryA under PER_METHOD - a fresh test instance and extension per method.
        assertThat(getExtension().getBeforeInvocations())
            .isEqualTo(1);
        assertThat(getExtension().getAfterInvocations())
            .isEqualTo(0);

        getDelayedAssertions().add(() -> {
            // when
            //    (this test is completed)
            // then
            assertThat(getExtension().getBeforeInvocations())
                .isEqualTo(1);
            assertThat(getExtension().getAfterInvocations())
                .isEqualTo(1);
        });
    }

    @Nested
    class NestedClassOnlyInParent implements DelayedAssertionsTest {
        @Override
        public List<Invokable> getDelayedAssertions() {
            return delayedAssertions;
        }

        // This specific test failed due to issue: #4205
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
                assertThat(getExtension().getBeforeInvocations())
                    .isEqualTo(1);
                assertThat(getExtension().getAfterInvocations())
                    .isEqualTo(1);
            });
        }
    }
}

@ExtendWith(DropwizardExtensionsSupport.class)
class DropwizardExtensionsSupportPerMethodChildHasExtensionInheritedTest
    extends DropwizardExtensionsSupportPerMethodChildHasExtension {
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
            assertThat(getExtension().getBeforeInvocations())
                .isEqualTo(1);
            assertThat(getExtension().getAfterInvocations())
                .isEqualTo(1);
        });
    }
}

@ExtendWith(CallbackVerifyingTestExtension.class)
@ExtendWith(DropwizardExtensionsSupport.class)
class DropwizardExtensionsSupportPerMethodChildHasExtensionNestedUseTest implements DelayedAssertionsTest {
    private final CountingTestExtension extension = new CountingTestExtension();
    private final List<Invokable> delayedAssertions = new ArrayList<>();

    @Override
    public List<Invokable> getDelayedAssertions() {
        return delayedAssertions;
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
            assertThat(extension.getBeforeInvocations())
                .isEqualTo(1);
            assertThat(extension.getAfterInvocations())
                .isEqualTo(1);
        });
    }

    @Nested
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
                    .isEqualTo(1);
            });
        }
    }

    @Nested
    class NestedClassInheriting extends DropwizardExtensionsSupportPerMethodChildHasExtension {
        @Override
        public List<Invokable> getDelayedAssertions() {
            return delayedAssertions;
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
                assertThat(extension.getBeforeInvocations())
                    .isEqualTo(1);
                assertThat(extension.getAfterInvocations())
                    .isEqualTo(1);
            });
        }

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
                    .isEqualTo(1);
            });
        }
    }
}

@ExtendWith(CallbackVerifyingTestExtension.class)
@ExtendWith(DropwizardExtensionsSupport.class)
abstract class DropwizardExtensionsSupportPerMethodParentHasExtension implements DelayedAssertionsTest {
    protected final CountingTestExtension extension = new CountingTestExtension();

    private final List<Invokable> delayedAssertions = new ArrayList<>();

    @Override
    public List<Invokable> getDelayedAssertions() {
        return delayedAssertions;
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
            assertThat(extension.getBeforeInvocations())
                .isEqualTo(1);
            assertThat(extension.getAfterInvocations())
                .isEqualTo(1);
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
            assertThat(extension.getBeforeInvocations())
                .isEqualTo(1);
            assertThat(extension.getAfterInvocations())
                .isEqualTo(1);
        });
    }

    @Nested
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
                    .isEqualTo(1);
            });
        }
    }
}

class DropwizardExtensionsSupportPerMethodParentHasExtensionNoOpWrapperTest {
    @Nested
    class NestedForInheritance extends DropwizardExtensionsSupportPerMethodParentHasExtension {
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
                    .isEqualTo(1);
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
                    .isEqualTo(1);
            });
        }
    }
}
