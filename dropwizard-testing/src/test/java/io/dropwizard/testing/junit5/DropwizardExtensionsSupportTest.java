package io.dropwizard.testing.junit5;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

// Utility interface for tests in this file.
@FunctionalInterface
interface Invokable {
    void invoke();
}

// Utility class for tests in this file.
// Allows us to count whether before() and after() are being correctly called by DropwizardExtensionsSupport.
class CountingExtension implements DropwizardExtension {
    private int beforeInvocations;
    private int afterInvocations;

    @Override
    public void before() throws Throwable {
        beforeInvocations++;
    }

    @Override
    public void after() throws Throwable {
        afterInvocations++;
    }

    public int getBeforeInvocations() {
        return beforeInvocations;
    }

    public int getAfterInvocations() {
        return afterInvocations;
    }
}

// Utility interface for tests in this file.
// Tests that need to assert things after invocation of DropwizardExtensionsSupport.afterEach() can implement this.
//    CallbackVerifyingExtension will then call this interface's method to then invoke any additional assertions.
interface DelayedAssertionsTest {
    List<Invokable> getDelayedAssertions();
}

// Utility class for tests in this file.
// Allows us to perform assertions *after* DropwizardExtensionsSupport.afterEach() is invoked.
class CallbackVerifyingExtension implements AfterEachCallback {
    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        DelayedAssertionsTest testInstance = (DelayedAssertionsTest) context.getTestInstance()
            .orElseThrow(() -> new AssertionError("Null context.testInstance"));
        testInstance.getDelayedAssertions().forEach(Invokable::invoke);
    }
}

// -----------------------
// The rest of the classes in this file set up various different test class hierarchies.
//   (Ensure DropwizardExtensionsSupport's reflection logic works regardless of how one's tests are structured.)
// Related issue: #4205
// -----------------------

abstract class ParentClass_ChildHasExtension implements DelayedAssertionsTest {
    protected abstract CountingExtension getExtension();

    @Test
    public void parentClassTestMethod() {
        // when, then
        getDelayedAssertions().add(() -> {
            assertThat(getExtension().getBeforeInvocations()).isEqualTo(1);
            assertThat(getExtension().getAfterInvocations()).isEqualTo(1);
        });
    }

    @Test
    public void overriddenTestMethod() {
        // when, then
        getDelayedAssertions().add(() -> {
            assertThat(getExtension().getBeforeInvocations()).isEqualTo(1);
            assertThat(getExtension().getAfterInvocations()).isEqualTo(1);
        });
    }

    @Nested
    public class NestedClass_OnlyInParent implements DelayedAssertionsTest {
        @Override
        public List<Invokable> getDelayedAssertions() {
            return ParentClass_ChildHasExtension.this.getDelayedAssertions();
        }

        // This specific test failed due to issue: #4205
        @Test
        public void onlyInParent() {
            // when, then
            getDelayedAssertions().add(() -> {
                assertThat(getExtension().getBeforeInvocations()).isEqualTo(1);
                assertThat(getExtension().getAfterInvocations()).isEqualTo(1);
            });
        }
    }
}

@ExtendWith(CallbackVerifyingExtension.class)
@ExtendWith(DropwizardExtensionsSupport.class)
abstract class ParentClass_ParentHasExtension implements DelayedAssertionsTest {
    protected final CountingExtension extension = new CountingExtension();
    protected final List<Invokable> delayedAssertions = new ArrayList<>();

    @Override
    public List<Invokable> getDelayedAssertions() {
        return delayedAssertions;
    }

    @Test
    public void parentClassTestMethod() {
        // when, then
        getDelayedAssertions().add(() -> {
            assertThat(extension.getBeforeInvocations()).isEqualTo(1);
            assertThat(extension.getAfterInvocations()).isEqualTo(1);
        });
    }

    @Test
    public void overriddenTestMethod() {
        // when, then
        getDelayedAssertions().add(() -> {
            assertThat(extension.getBeforeInvocations()).isEqualTo(1);
            assertThat(extension.getAfterInvocations()).isEqualTo(1);
        });
    }

    @Nested
    public class NestedClass_OnlyInParent implements DelayedAssertionsTest {
        @Override
        public List<Invokable> getDelayedAssertions() {
            return delayedAssertions;
        }

        @Test
        public void onlyInParent() {
            // when, then
            getDelayedAssertions().add(() -> {
                assertThat(extension.getBeforeInvocations()).isEqualTo(1);
                assertThat(extension.getAfterInvocations()).isEqualTo(1);
            });
        }
    }
}

@ExtendWith(CallbackVerifyingExtension.class)
@ExtendWith(DropwizardExtensionsSupport.class)
class DropwizardExtensionsSupport_ChildHasExtension_NestedUseTest implements DelayedAssertionsTest {
    private final CountingExtension extension = new CountingExtension();
    private final List<Invokable> delayedAssertions = new ArrayList<>();

    @Override
    public List<Invokable> getDelayedAssertions() {
        return delayedAssertions;
    }

    @Test
    public void regularTestMethod() {
        // when, then
        delayedAssertions.add(() -> {
            assertThat(extension.getBeforeInvocations()).isEqualTo(1);
            assertThat(extension.getAfterInvocations()).isEqualTo(1);
        });
    }

    @Nested
    public class NestedClass_Standalone implements DelayedAssertionsTest {
        @Override
        public List<Invokable> getDelayedAssertions() {
            return delayedAssertions;
        }

        @Test
        public void nestedClassMethod() {
            // when, then
            delayedAssertions.add(() -> {
                assertThat(extension.getBeforeInvocations()).isEqualTo(1);
                assertThat(extension.getAfterInvocations()).isEqualTo(1);
            });
        }
    }

    @Nested
    public class NestedClass_Inheriting extends ParentClass_ChildHasExtension {
        @Override
        public List<Invokable> getDelayedAssertions() {
            return delayedAssertions;
        }

        @Override
        protected CountingExtension getExtension() {
            return extension;
        }

        @Test
        public void childClassTestMethod() {
            // when, then
            delayedAssertions.add(() -> {
                assertThat(extension.getBeforeInvocations()).isEqualTo(1);
                assertThat(extension.getAfterInvocations()).isEqualTo(1);
            });
        }

        @Test
        @Override
        public void parentClassTestMethod() {
            // when, then
            delayedAssertions.add(() -> {
                assertThat(extension.getBeforeInvocations()).isEqualTo(1);
                assertThat(extension.getAfterInvocations()).isEqualTo(1);
            });
        }
    }
}

@ExtendWith(CallbackVerifyingExtension.class)
@ExtendWith(DropwizardExtensionsSupport.class)
class DropwizardExtensionsSupport_ChildHasExtension_OuterUseTest extends ParentClass_ChildHasExtension {
    private final CountingExtension extension = new CountingExtension();
    private final List<Invokable> delayedAssertions = new ArrayList<>();

    @Override
    protected CountingExtension getExtension() {
        return extension;
    }

    @Override
    public List<Invokable> getDelayedAssertions() {
        return delayedAssertions;
    }

    @Test
    public void regularTestMethod() {
        // when, then
        delayedAssertions.add(() -> {
            assertThat(extension.getBeforeInvocations()).isEqualTo(1);
            assertThat(extension.getAfterInvocations()).isEqualTo(1);
        });
    }

    @Nested
    public class NestedClass_Standalone implements DelayedAssertionsTest {
        @Override
        public List<Invokable> getDelayedAssertions() {
            return delayedAssertions;
        }

        @Test
        public void nestedClassMethod() {
            // when, then
            delayedAssertions.add(() -> {
                assertThat(extension.getBeforeInvocations()).isEqualTo(1);
                assertThat(extension.getAfterInvocations()).isEqualTo(1);
            });
        }
    }
}

class DropwizardExtensionsSupport_ParentHasExtensionTest {
    @Nested
    public class NestedClass_Inheriting extends ParentClass_ParentHasExtension {
        @Test
        public void childClassTestMethod() {
            // when, then
            delayedAssertions.add(() -> {
                assertThat(extension.getBeforeInvocations()).isEqualTo(1);
                assertThat(extension.getAfterInvocations()).isEqualTo(1);
            });
        }

        @Test
        @Override
        public void parentClassTestMethod() {
            // when, then
            delayedAssertions.add(() -> {
                assertThat(extension.getBeforeInvocations()).isEqualTo(1);
                assertThat(extension.getAfterInvocations()).isEqualTo(1);
            });
        }
    }
}
