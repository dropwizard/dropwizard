package io.dropwizard.validation;

import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.jersey.validation.MutableValidatorFactory;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.MinValidatorForInteger;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorFactoryImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintValidatorFactory;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.Min;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.AdditionalAnswers.delegatesTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class InjectValidatorFeatureTest {

    private final Application<Configuration> application = new Application<Configuration>() {
        @Override
        public void initialize(Bootstrap<Configuration> bootstrap) { }

        @Override
        public void run(Configuration configuration, Environment environment) { }
    };

    private ValidatorFactory validatorFactory;

    @BeforeEach
    void setUp() {
        Bootstrap<Configuration> bootstrap = new Bootstrap<>(application);
        application.initialize(bootstrap);

        validatorFactory = bootstrap.getValidatorFactory();
    }

    @Test
    void shouldReplaceValidatorFactory() {
        ConstraintValidatorFactory factory = validatorFactory.getConstraintValidatorFactory();

        assertThat(factory).isInstanceOf(MutableValidatorFactory.class);
    }

    @Test
    void shouldValidateNormally() {
        Validator validator = validatorFactory.getValidator();

        // Run validation manually
        Set<ConstraintViolation<Bean>> constraintViolations = validator.validate(new Bean(1));

        assertThat(constraintViolations)
            .singleElement()
            .extracting(ConstraintViolation::getMessage)
            .isEqualTo("must be greater than or equal to 10");
    }

    @Test
    void shouldInvokeUpdatedFactory() {
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

        verify(mockedFactory).getInstance(MinValidatorForInteger.class);
    }

    static class Bean {

        @Min(10)
        final int value;

        Bean(int value) {
            this.value = value;
        }
    }
}
