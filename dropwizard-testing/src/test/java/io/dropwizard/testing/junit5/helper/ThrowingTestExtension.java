package io.dropwizard.testing.junit5.helper;

import io.dropwizard.testing.junit5.DropwizardExtension;

import java.util.List;

// Configurable extension for exception-path tests. Throws from before() and/or after() as configured.
public class ThrowingTestExtension implements DropwizardExtension {
    private final String label;
    private final List<String> log;
    private final boolean throwOnBefore;
    private final boolean throwOnAfter;

    public ThrowingTestExtension(String label, List<String> sharedLog, boolean throwOnBefore, boolean throwOnAfter) {
        this.label = label;
        this.log = sharedLog;
        this.throwOnBefore = throwOnBefore;
        this.throwOnAfter = throwOnAfter;
    }

    @Override
    public void before() {
        log.add("before:" + label);
        if (throwOnBefore) {
            throw new RuntimeException("before-boom:" + label);
        }
    }

    @Override
    public void after() {
        log.add("after:" + label);
        if (throwOnAfter) {
            throw new RuntimeException("after-boom:" + label);
        }
    }
}
