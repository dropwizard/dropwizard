package com.yammer.dropwizard.tasks;

import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

// TODO: 10/12/11 <coda> -- write tests for GarbageCollectionTask
// TODO: 10/12/11 <coda> -- write docs for GarbageCollectionTask

/**
 * Performs a full JVM garbage collection (probably).
 */
public class GarbageCollectionTask extends Task {
    public GarbageCollectionTask() {
        super("gc");
    }

    @Override
    public void execute(Map<String, List<String>> parameters, PrintWriter output) {
        final int count = parseRuns(parameters);

        for (int i = 1; i < count; i++) {
            output.println("Running GC...");
            output.flush();
            System.gc();
        }

        output.println("Done!");
    }

    private int parseRuns(Map<String, List<String>> parameters) {
        final List<String> runs = parameters.get("runs");
        if (runs.isEmpty()) {
            return 1;
        } else {
            try {
                return Integer.parseInt(runs.get(0));
            } catch (NumberFormatException e) {
                return 1;
            }
        }
    }
}
