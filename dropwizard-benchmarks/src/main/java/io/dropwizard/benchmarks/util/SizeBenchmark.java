package io.dropwizard.benchmarks.util;

import io.dropwizard.util.Size;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
public class SizeBenchmark {

    /**
     * Don't trust the IDE, it's advisedly non-final to avoid constant folding
     */
    private String size = "256KiB";

    @Benchmark
    public Size parseSize() {
        return Size.parse(size);
    }

    public static void main(String[] args) throws Exception {
        new Runner(new OptionsBuilder()
                .include(SizeBenchmark.class.getSimpleName())
                .forks(1)
                .warmupIterations(5)
                .measurementIterations(5)
                .build())
                .run();
    }
}
