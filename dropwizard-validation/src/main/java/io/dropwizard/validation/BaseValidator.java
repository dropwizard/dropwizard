package io.dropwizard.validation;

import javax.validation.Validation;
import javax.validation.Validator;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;

import io.dropwizard.validation.valuehandling.GuavaOptionalValidatedValueExtractor;
import io.dropwizard.validation.valuehandling.OptionalDoubleValidatedValueExtractor;
import io.dropwizard.validation.valuehandling.OptionalIntValidatedValueExtractor;
import io.dropwizard.validation.valuehandling.OptionalLongValidatedValueExtractor;

public class BaseValidator {
	private BaseValidator() {
		/* singleton */ }

	/**
	 * Creates a new {@link Validator} based on {@link #newConfiguration()}
	 */
	public static Validator newValidator() {
		return newConfiguration().buildValidatorFactory().getValidator();
	}

	/**
	 * Creates a new {@link HibernateValidatorConfiguration} with the base custom
	 * {@link org.hibernate.validator.spi.valuehandling.ValidatedValueUnwrapper}
	 * registered.
	 */
	public static HibernateValidatorConfiguration newConfiguration() {

		return Validation.byProvider(HibernateValidator.class).configure()
				.addValueExtractor(new GuavaOptionalValidatedValueExtractor())
				.addValueExtractor(new OptionalDoubleValidatedValueExtractor())
				.addValueExtractor(new OptionalIntValidatedValueExtractor())
				.addValueExtractor(new OptionalLongValidatedValueExtractor());

	}

}
