package io.dropwizard.benchmarks.jersey;

import io.dropwizard.jersey.validation.ConstraintMessage;
import io.dropwizard.jersey.validation.Validators;
import io.dropwizard.logging.common.BootstrapLogging;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.executable.ExecutableValidator;
import jakarta.ws.rs.HeaderParam;
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

import java.util.Set;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
public class ConstraintViolationBenchmark {

    static {
        BootstrapLogging.bootstrap();
    }

    public static class Resource {
        public String paramFunc(@HeaderParam("cheese") @NotEmpty String secretSauce) {
            return secretSauce;
        }

        public String objectFunc(@Valid Foo foo) {
            return foo.toString();
        }
    }

    public static class Foo {
        @NotEmpty
        private String bar;
    }

    private ConstraintViolation<ConstraintViolationBenchmark.Resource> paramViolation;
    private ConstraintViolation<ConstraintViolationBenchmark.Resource> objViolation;

    final Invocable invocable = Invocable.create(request -> null);

    @Setup
    public void prepare() throws NoSuchMethodException {
        final Validator validator = Validators.newValidator();
        final ExecutableValidator execValidator = validator.forExecutables();

        final Set<ConstraintViolation<ConstraintViolationBenchmark.Resource>> paramViolations =
            execValidator.validateParameters(
                new Resource(),
                ConstraintViolationBenchmark.Resource.class.getMethod("paramFunc", String.class),
                new Object[]{""} // the parameter value
            );
        paramViolation = paramViolations.iterator().next();

        final Set<ConstraintViolation<ConstraintViolationBenchmark.Resource>> objViolations =
            execValidator.validateParameters(
                new Resource(),
                ConstraintViolationBenchmark.Resource.class.getMethod("objectFunc", Foo.class),
                new Object[]{new Foo()} // the parameter value
            );
        objViolation = objViolations.iterator().next();
    }

    @Benchmark
    public String paramViolation() {
        return ConstraintMessage.getMessage(paramViolation, invocable);
    }

    @Benchmark
    public String objViolation() {
        return ConstraintMessage.getMessage(objViolation, invocable);
    }

    public static void main(String[] args) throws Exception {
        new Runner(new OptionsBuilder()
                .include(ConstraintViolationBenchmark.class.getSimpleName())
                .forks(1)
                .warmupIterations(5)
                .measurementIterations(5)
                .build())
                .run();
    }
}
