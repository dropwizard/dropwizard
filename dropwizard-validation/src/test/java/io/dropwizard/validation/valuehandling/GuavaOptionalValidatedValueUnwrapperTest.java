package io.dropwizard.validation.valuehandling;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.HibernateValidator;
//import org.hibernate.validator.valuehandling.UnwrapValidatedValue;
import org.junit.Test;
//import org.junit.jupiter.api.Test;

import com.google.common.base.Optional;

public class GuavaOptionalValidatedValueUnwrapperTest {

	public static class Example {

		@Min(3)
		// @UnwrapValidatedValue
		public Optional<Integer> three = Optional.absent();

		@NotNull
		// @UnwrapValidatedValue
		public Optional<Integer> notNull = Optional.of(123);
	}

	private final Validator validator = Validation.byProvider(HibernateValidator.class).configure()
			.addValueExtractor(new GuavaOptionalValidatedValueExtractor()).buildValidatorFactory().getValidator();

	@Test
	public void succeedsWhenAbsent() {
		Example example = new Example();
		Set<ConstraintViolation<Example>> violations = validator.validate(example);
		assertThat(violations).isEmpty();
	}

	@Test
	public void failsWhenFailingConstraint() {
		Example example = new Example();
		example.three = Optional.of(2);
		Set<ConstraintViolation<Example>> violations = validator.validate(example);
		assertThat(violations).hasSize(1);
	}

	@Test
	public void succeedsWhenConstraintsMet() {
		Example example = new Example();
		example.three = Optional.of(10);
		Set<ConstraintViolation<Example>> violations = validator.validate(example);
		assertThat(violations).isEmpty();
	}

	@Test
	public void notNullFailsWhenAbsent() {
		Example example = new Example();
		example.notNull = Optional.absent();
		Set<ConstraintViolation<Example>> violations = validator.validate(example);
		assertThat(violations).hasSize(1);
	}
}
