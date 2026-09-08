package io.dropwizard.testing.junit5.helper;

import io.dropwizard.testing.junit5.DropwizardExtension;

import java.util.List;

// Extension that appends a labeled event to a shared log on before() and after(). Used to verify multi-field ordering:
// given multiple RecordingExtension fields with distinct labels, the shared log captures the order in which
// before()/after() calls happen across all of them.
public class RecordingTestExtension implements DropwizardExtension {
    private final String label;
    private final List<String> log;

    public RecordingTestExtension(String label, List<String> sharedLog) {
        this.label = label;
        this.log = sharedLog;
    }

    @Override
    public void before() {
        log.add("before:" + label);
    }

    @Override
    public void after() {
        log.add("after:" + label);
    }

    public String getLabel() {
        return label;
    }
}
