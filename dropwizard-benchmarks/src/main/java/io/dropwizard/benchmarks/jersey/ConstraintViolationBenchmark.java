package io.dropwizard.benchmarks.jersey;

import io.dropwizard.jersey.validation.ConstraintMessage;
import io.dropwizard.jersey.validation.Validators;
import io.dropwizard.logging.BootstrapLogging;
import org.glassfish.jersey.process.Inflector;
import org.glassfish.jersey.server.model.Invocable;
import org.hibernate.validator.constraints.NotEmpty;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import javax.validation.ConstraintViolation;
import javax.validation.Valid;
import javax.validation.Validator;
import javax.validation.executable.ExecutableValidator;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.core.Request;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.apache.commons.lang3.reflect.MethodUtils.getAccessibleMethod;

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
    public void prepare() {
        final Validator validator = Validators.newValidator();
        final ExecutableValidator execValidator = validator.forExecutables();

        final Set<ConstraintViolation<ConstraintViolationBenchmark.Resource>> paramViolations =
            execValidator.validateParameters(
                new Resource(),
                getAccessibleMethod(ConstraintViolationBenchmark.Resource.class, "paramFunc", String.class),
                new Object[]{""} // the parameter value
            );
        paramViolation = paramViolations.iterator().next();

        final Set<ConstraintViolation<ConstraintViolationBenchmark.Resource>> objViolations =
            execValidator.validateParameters(
                new Resource(),
                getAccessibleMethod(ConstraintViolationBenchmark.Resource.class, "objectFunc", Foo.class),
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
