package io.dropwizard.core.validation;

import io.dropwizard.core.Application;
import io.dropwizard.core.Configuration;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import io.dropwizard.jersey.validation.MutableValidatorFactory;
import io.dropwizard.util.DataSize;
import io.dropwizard.util.DataSizeUnit;
import io.dropwizard.validation.MinDataSize;
import io.dropwizard.validation.MinDataSizeValidator;
import jakarta.validation.ConstraintValidatorFactory;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.Min;
import org.hibernate.validator.HibernateValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.AdditionalAnswers.delegatesTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class InjectValidatorFeatureTest {

    private final Application<Configuration> application = new Application<>() {
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
            delegatesTo(Validation.byProvider(HibernateValidator.class).configure().getDefaultConstraintValidatorFactory())
        );

        // Swap validator factory at runtime
        mutableFactory.setValidatorFactory(mockedFactory);

        // Run validation manually
        Validator validator = validatorFactory.getValidator();
        validator.validate(new DataSizeRecord(DataSize.bytes(100)));

        verify(mockedFactory).getInstance(MinDataSizeValidator.class);
    }

    static class Bean {

        @Min(10)
        final int value;

        Bean(int value) {
            this.value = value;
        }
    }

    private record DataSizeRecord(@MinDataSize(value = 30, unit = DataSizeUnit.BYTES) DataSize value) {

    }

}
