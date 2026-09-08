package io.dropwizard.testing.junit5;

import io.dropwizard.testing.junit5.helper.CallbackVerifyingTestExtension;
import io.dropwizard.testing.junit5.helper.CallbackVerifyingTestExtension.DelayedAssertionsTest;
import io.dropwizard.testing.junit5.helper.CallbackVerifyingTestExtension.Invokable;
import io.dropwizard.testing.junit5.helper.CountingTestExtension;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

// Verifies DropwizardExtensionsSupport behavior with @Nested classes nested 2+ levels deep.
@ExtendWith(CallbackVerifyingTestExtension.class)
@ExtendWith(DropwizardExtensionsSupport.class)
class DropwizardExtensionsSupportDeepNestingOuterExtensionTest implements DelayedAssertionsTest {
    private final CountingTestExtension outerExtension = new CountingTestExtension();
    private final List<Invokable> delayedAssertions = new ArrayList<>();

    @Override
    public List<Invokable> getDelayedAssertions() {
        return delayedAssertions;
    }

    @Test
    void topLevelTest() {
        // when
        //    (this test is running and not yet completed)
        // then
        //    verify outer's extension fires for its own test.
        assertThat(outerExtension.getBeforeInvocations())
            .isEqualTo(1);
        assertThat(outerExtension.getAfterInvocations())
            .isEqualTo(0);

        delayedAssertions.add(() -> {
            // when
            //    (this test is completed)
            // then
            assertThat(outerExtension.getBeforeInvocations())
                .isEqualTo(1);
            assertThat(outerExtension.getAfterInvocations())
                .isEqualTo(1);
        });
    }

    @Nested
    class Level1 implements DelayedAssertionsTest {
        @Override
        public List<Invokable> getDelayedAssertions() {
            return delayedAssertions;
        }

        @Test
        void level1Test() {
            // when
            //    (this test is running and not yet completed)
            // then
            //    enclosing walk reaches outer at depth 1.
            assertThat(outerExtension.getBeforeInvocations())
                .isEqualTo(1);
            assertThat(outerExtension.getAfterInvocations())
                .isEqualTo(0);

            delayedAssertions.add(() -> {
                // when
                //    (this test is completed)
                // then
                assertThat(outerExtension.getBeforeInvocations())
                    .isEqualTo(1);
                assertThat(outerExtension.getAfterInvocations())
                    .isEqualTo(1);
            });
        }

        @Nested
        class Level2 implements DelayedAssertionsTest {
            @Override
            public List<Invokable> getDelayedAssertions() {
                return delayedAssertions;
            }

            @Test
            void level2Test() {
                // when
                //    (this test is running and not yet completed)
                // then
                //    enclosing walk reaches outer at depth 2.
                assertThat(outerExtension.getBeforeInvocations())
                    .isEqualTo(1);
                assertThat(outerExtension.getAfterInvocations())
                    .isEqualTo(0);

                delayedAssertions.add(() -> {
                    // when
                    //    (this test is completed)
                    // then
                    assertThat(outerExtension.getBeforeInvocations())
                        .isEqualTo(1);
                    assertThat(outerExtension.getAfterInvocations())
                        .isEqualTo(1);
                });
            }

            @Nested
            class Level3 implements DelayedAssertionsTest {
                @Override
                public List<Invokable> getDelayedAssertions() {
                    return delayedAssertions;
                }

                @Test
                void level3Test() {
                    // when
                    //    (this test is running and not yet completed)
                    // then
                    //    enclosing walk reaches outer at depth 3.
                    assertThat(outerExtension.getBeforeInvocations())
                        .isEqualTo(1);
                    assertThat(outerExtension.getAfterInvocations())
                        .isEqualTo(0);

                    delayedAssertions.add(() -> {
                        // when
                        //    (this test is completed)
                        // then
                        assertThat(outerExtension.getBeforeInvocations())
                            .isEqualTo(1);
                        assertThat(outerExtension.getAfterInvocations())
                            .isEqualTo(1);
                    });
                }
            }
        }
    }
}

// Extensions declared at multiple depths, all must fire when innermost test runs.
@ExtendWith(CallbackVerifyingTestExtension.class)
@ExtendWith(DropwizardExtensionsSupport.class)
class DropwizardExtensionsSupportDeepNestingExtensionAtEachLevelTest implements DelayedAssertionsTest {
    private final CountingTestExtension outerExtension = new CountingTestExtension();
    private final List<Invokable> delayedAssertions = new ArrayList<>();

    @Override
    public List<Invokable> getDelayedAssertions() {
        return delayedAssertions;
    }

    @Nested
    class Level1 implements DelayedAssertionsTest {
        private final CountingTestExtension level1Extension = new CountingTestExtension();

        @Override
        public List<Invokable> getDelayedAssertions() {
            return delayedAssertions;
        }

        @Nested
        class Level2 implements DelayedAssertionsTest {
            private final CountingTestExtension level2Extension = new CountingTestExtension();

            @Override
            public List<Invokable> getDelayedAssertions() {
                return delayedAssertions;
            }

            @Test
            void allThreeLevelsFire() {
                // when
                //    (this test is running and not yet completed)
                // then
                assertThat(outerExtension.getBeforeInvocations())
                    .isEqualTo(1);
                assertThat(level1Extension.getBeforeInvocations())
                    .isEqualTo(1);
                assertThat(level2Extension.getBeforeInvocations())
                    .isEqualTo(1);
                assertThat(outerExtension.getAfterInvocations())
                    .isEqualTo(0);
                assertThat(level1Extension.getAfterInvocations())
                    .isEqualTo(0);
                assertThat(level2Extension.getAfterInvocations())
                    .isEqualTo(0);

                delayedAssertions.add(() -> {
                    // when
                    //    (this test is completed)
                    // then
                    assertThat(outerExtension.getBeforeInvocations())
                        .isEqualTo(1);
                    assertThat(level1Extension.getBeforeInvocations())
                        .isEqualTo(1);
                    assertThat(level2Extension.getBeforeInvocations())
                        .isEqualTo(1);
                    assertThat(outerExtension.getAfterInvocations())
                        .isEqualTo(1);
                    assertThat(level1Extension.getAfterInvocations())
                        .isEqualTo(1);
                    assertThat(level2Extension.getAfterInvocations())
                        .isEqualTo(1);
                });
            }
        }
    }
}
