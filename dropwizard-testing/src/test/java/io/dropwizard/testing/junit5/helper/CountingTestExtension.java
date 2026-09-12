package io.dropwizard.testing.junit5.helper;

import io.dropwizard.testing.junit5.DropwizardExtension;

// Records before()/after() invocations. Assertions read the counters after DropwizardExtensionsSupport has driven
// its callbacks.
public class CountingTestExtension implements DropwizardExtension {
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
