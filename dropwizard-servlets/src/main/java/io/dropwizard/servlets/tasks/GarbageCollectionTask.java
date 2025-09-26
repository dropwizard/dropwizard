package io.dropwizard.servlets.tasks;

import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

/**
 * Performs a full JVM garbage collection (probably).
 */
public class GarbageCollectionTask extends Task {
    private final Runnable invokeGc;

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
        this(runtime::gc);
    }

    // visible for testing
    GarbageCollectionTask(Runnable invokeGc) {
        super("gc");
        this.invokeGc = invokeGc;
    }

    @Override
    @SuppressWarnings("CallToSystemGC")
    public void execute(Map<String, List<String>> parameters, PrintWriter output) {
        final int count = parseRuns(parameters);
        for (int i = 0; i < count; i++) {
            output.println("Running GC...");
            output.flush();
            invokeGc.run();
        }

        output.println("Done!");
    }

    private static int parseRuns(Map<String, List<String>> parameters) {
        final List<String> runs = parameters.get("runs");
        if (runs == null || runs.isEmpty()) {
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
