package com.yammer.dropwizard.tasks;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;

import java.io.PrintWriter;

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
    @SuppressWarnings("CallToSystemGC")
    public void execute(ImmutableMultimap<String, String> parameters, PrintWriter output) {
        final int count = parseRuns(parameters);
        for (int i = 1; i < count; i++) {
            output.println("Running GC...");
            output.flush();
            System.gc();
        }

        output.println("Done!");
    }

    private static int parseRuns(ImmutableMultimap<String, String> parameters) {
        final ImmutableList<String> runs = parameters.get("runs").asList();
        if (runs.isEmpty()) {
            return 1;
        } else {
            try {
                return Integer.parseInt(runs.get(0));
            } catch (NumberFormatException ignored) {
                return 1;
            }
        }
    }
}
