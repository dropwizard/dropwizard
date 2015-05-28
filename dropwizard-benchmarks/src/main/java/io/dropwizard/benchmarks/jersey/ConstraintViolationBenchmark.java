package io.dropwizard.benchmarks.jersey;

import io.dropwizard.jersey.validation.ConstraintMessage;
import org.hibernate.validator.constraints.NotEmpty;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import javax.validation.ConstraintViolation;
import javax.validation.Valid;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.executable.ExecutableValidator;
import javax.ws.rs.HeaderParam;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.apache.commons.lang3.reflect.MethodUtils.getAccessibleMethod;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
public class ConstraintViolationBenchmark {

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

    @Setup
    public void prepare() {
        final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        final ExecutableValidator execValidator = validator.forExecutables();

        Set<ConstraintViolation<ConstraintViolationBenchmark.Resource>> paramViolations =
            execValidator.validateParameters(
                new Resource(),
                getAccessibleMethod(ConstraintViolationBenchmark.Resource.class, "paramFunc", String.class),
                new Object[]{""} // the parameter value
            );
        paramViolation = paramViolations.iterator().next();

        Set<ConstraintViolation<ConstraintViolationBenchmark.Resource>> objViolations =
            execValidator.validateParameters(
                new Resource(),
                getAccessibleMethod(ConstraintViolationBenchmark.Resource.class, "objectFunc", Foo.class),
                new Object[]{new Foo()} // the parameter value
            );
        objViolation = objViolations.iterator().next();
    }

    @Benchmark
    public String paramViolation() {
        return ConstraintMessage.getMessage(paramViolation);
    }

    @Benchmark
    public String objViolation() {
        return ConstraintMessage.getMessage(objViolation);
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
