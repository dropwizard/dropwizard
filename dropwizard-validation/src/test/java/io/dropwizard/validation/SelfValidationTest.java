package io.dropwizard.validation;

import static org.assertj.core.api.Assertions.assertThat;

import javax.validation.Validator;

import org.junit.Test;

import io.dropwizard.validation.selfvalidating.SelfValidating;
import io.dropwizard.validation.selfvalidating.SelfValidation;
import io.dropwizard.validation.selfvalidating.ViolationCollector;

public class SelfValidationTest {
	
	private static final String FAILED = "failed";
	
	@SelfValidating
    public static class FailingExample {
		@SelfValidation
        public void validateFail(ViolationCollector col) {
			col.addViolation(FAILED);
        }
    }
	
	@SelfValidating
    public static class CorrectExample {
		@SuppressWarnings("unused")
		@SelfValidation
        public void validateCorrect(ViolationCollector col) {}
    }
	
	@SelfValidating
    public static class ComplexExample {
		@SelfValidation
        public void validateFail1(ViolationCollector col) {
			col.addViolation(FAILED+"1");
        }
		
		@SelfValidation
        public void validateFail2(ViolationCollector col) {
			col.addViolation(FAILED+"2");
        }
		
		@SelfValidation
        public void validateFail3(ViolationCollector col) {
			col.addViolation(FAILED+"3");
        }
		
		@SuppressWarnings("unused")
		@SelfValidation
		public void validateCorrect(ViolationCollector col) {}
    }

    private final Validator validator = BaseValidator.newValidator();

    @Test
    public void failingExample() throws Exception {
        assertThat(ConstraintViolations.format(validator.validate(new FailingExample())))
                .containsOnly(" "+FAILED);
    }
    
    @Test
    public void correctExample() throws Exception {
        assertThat(ConstraintViolations.format(validator.validate(new CorrectExample())))
        		.isEmpty();
    }
    
    @Test
    public void complexExample() throws Exception {
        assertThat(ConstraintViolations.format(validator.validate(new ComplexExample())))
                .containsOnly(
                		" "+FAILED+"1",
                		" "+FAILED+"2",
                		" "+FAILED+"3"
                );
    }
}
