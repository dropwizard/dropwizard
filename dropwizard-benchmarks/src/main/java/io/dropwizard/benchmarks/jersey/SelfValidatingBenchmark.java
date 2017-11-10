package io.dropwizard.benchmarks.jersey;

import io.dropwizard.jersey.validation.Validators;
import io.dropwizard.logging.BootstrapLogging;
import io.dropwizard.validation.ValidationMethod;
import io.dropwizard.validation.selfvalidating.SelfValidating;
import io.dropwizard.validation.selfvalidating.SelfValidation;
import io.dropwizard.validation.selfvalidating.ViolationCollector;
import org.glassfish.jersey.server.model.Invocable;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import javax.validation.Validator;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
public class SelfValidatingBenchmark {

    static {
        BootstrapLogging.bootstrap();
    }

    public static class ValidationMethodUser {
        @ValidationMethod
        public boolean isValid1(String param1) {
            return true;
        }

        @ValidationMethod
        public boolean isValid2(String param1, int param2) {
            return true;
        }

        @ValidationMethod(message = "invalid1")
        public boolean isInvalid1(String param1) {
            return false;
        }

        @ValidationMethod(message = "invalid2")
        public boolean isInvalid2(String param1, int param2) {
            return false;
        }
    }

    @SelfValidating
    public static class SelfValidatingMethodUser {
        @SelfValidation
        public void validateValid1(ViolationCollector collector) {
        }

        @SelfValidation
        public void validateValid2(ViolationCollector collector) {
        }

        @SelfValidation
        public void validateInvalid1(ViolationCollector collector) {
            collector.addViolation("invalid1");
        }

        @SelfValidation
        public void validateInvalid2(ViolationCollector collector) {
            collector.addViolation("invalid2");
        }
    }

    private ValidationMethodUser validationMethodUser;
    private SelfValidatingMethodUser selfValidatingMethodUser;
    private Validator validator;

    final Invocable invocable = Invocable.create(request -> null);

    @Setup
    public void prepare() {
        validator = Validators.newValidator();
        validationMethodUser = new ValidationMethodUser();
        selfValidatingMethodUser = new SelfValidatingMethodUser();
    }

    @Benchmark
    public void validationMethod() {
        validator.validate(validationMethodUser);
    }

    @Benchmark
    public void selfValidating() {
        validator.validate(selfValidatingMethodUser);
    }

    public static void main(String[] args) throws Exception {
        new Runner(new OptionsBuilder()
            .include(SelfValidatingBenchmark.class.getSimpleName())
            .forks(1)
            .warmupIterations(5)
            .measurementIterations(8)
            .build())
            .run();
    }
}
