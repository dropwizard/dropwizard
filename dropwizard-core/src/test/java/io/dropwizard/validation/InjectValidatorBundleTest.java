package io.dropwizard.validation;

import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.jersey.validation.MutableValidatorFactory;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.hibernate.validator.internal.constraintvalidators.bv.MinValidatorForNumber;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorFactoryImpl;
import org.junit.Before;
import org.junit.Test;

import javax.validation.ConstraintValidatorFactory;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.constraints.Min;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.AdditionalAnswers.delegatesTo;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class InjectValidatorBundleTest {

    private final Application<Configuration> application = new Application<Configuration>() {
        @Override
        public void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(new InjectValidatorBundle());
        }

        @Override
        public void run(Configuration configuration, Environment environment) { }
    };

    private ValidatorFactory validatorFactory;

    @Before
    public void setUp() throws Exception {
        Bootstrap<Configuration> bootstrap = new Bootstrap<>(application);
        application.initialize(bootstrap);

        validatorFactory = bootstrap.getValidatorFactory();
    }

    @Test
    public void shouldReplaceValidatorFactory() {
        ConstraintValidatorFactory factory = validatorFactory.getConstraintValidatorFactory();

        assertThat(factory).isInstanceOf(MutableValidatorFactory.class);
    }

    @Test
    public void shouldValidateNormally() {
        Validator validator = validatorFactory.getValidator();

        // Run validation manually
        Set<ConstraintViolation<Bean>> constraintViolations = validator.validate(new Bean(1));


        assertThat(constraintViolations.size()).isEqualTo(1);

        Optional<String> message = constraintViolations.stream()
            .findFirst()
            .map(ConstraintViolation::getMessage);

        assertThat(message).hasValue("must be greater than or equal to 10");
    }

    @Test
    public void shouldInvokeUpdatedFactory() {
        MutableValidatorFactory mutableFactory = (MutableValidatorFactory) validatorFactory
            .getConstraintValidatorFactory();

        ConstraintValidatorFactory mockedFactory = mock(
            ConstraintValidatorFactory.class,
            delegatesTo(new ConstraintValidatorFactoryImpl())
        );

        // Swap validator factory at runtime
        mutableFactory.setValidatorFactory(mockedFactory);

        // Run validation manually
        Validator validator = validatorFactory.getValidator();
        validator.validate(new Bean(1));

        verify(mockedFactory).getInstance(eq(MinValidatorForNumber.class));
    }

    static class Bean {

        @Min(10)
        final int value;

        Bean(int value) {
            this.value = value;
        }
    }
}
