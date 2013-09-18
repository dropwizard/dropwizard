package io.dropwizard.servlets.tasks;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;

import java.io.PrintWriter;

/**
 * Performs a full JVM garbage collection (probably).
 */
public class GarbageCollectionTask extends Task {
    private final Runtime runtime;

    /**
     * Creates a new GarbageCollectionTask.
     */
    public GarbageCollectionTask() {
        this(Runtime.getRuntime());
    }

    /**
     * Creates a new GarbageCollectionTask with the given {@link Runtime} instance.
     * <p/>
     * <b>Use {@link GarbageCollectionTask#GarbageCollectionTask()} instead.</b>
     *
     * @param runtime a {@link Runtime} instance
     */
    public GarbageCollectionTask(Runtime runtime) {
        super("gc");
        this.runtime = runtime;
    }

    @Override
    @SuppressWarnings("CallToSystemGC")
    public void execute(ImmutableMultimap<String, String> parameters, PrintWriter output) {
        final int count = parseRuns(parameters);
        for (int i = 0; i < count; i++) {
            output.println("Running GC...");
            output.flush();
            runtime.gc();
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
