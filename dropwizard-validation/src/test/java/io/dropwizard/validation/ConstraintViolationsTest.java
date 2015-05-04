package io.dropwizard.validation;

import org.junit.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Valid;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.executable.ExecutableValidator;
import java.util.Set;

import static org.apache.commons.lang3.reflect.MethodUtils.getAccessibleMethod;
import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings({"UnusedDeclaration"})
public class ConstraintViolationsTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    private final ExecutableValidator execValidator = validator.forExecutables();

    @Max(3)
    public Integer theIntTest(@Min(2) Integer x) {
        return x + 1;
    }

    @Valid
    public ConstraintPerson thePersonTest(@Valid ConstraintPerson person) {
        person.setName(null);
        return person;
    }

    @Test
    public void invalidIntReturnIs500() {
        Set<ConstraintViolation<ConstraintViolationsTest>> violations =
                execValidator.validateReturnValue(
                        this,
                        getAccessibleMethod(getClass(), "theIntTest", Integer.class),
                        4 // the return value
                );

        assertThat(violations).hasSize(1);
        assertThat(ConstraintViolations.determineStatus(violations)).isEqualTo(500);
    }

    @Test
    public void invalidIntParameterIs400() {
        Set<ConstraintViolation<ConstraintViolationsTest>> violations =
                execValidator.validateParameters(
                        this,
                        getAccessibleMethod(getClass(), "theIntTest", Integer.class),
                        new Object[]{1} // the parameter value
                );

        assertThat(violations).hasSize(1);
        assertThat(ConstraintViolations.determineStatus(violations)).isEqualTo(400);
    }

    @Test
    public void invalidPersonReturnIs500() {
        Set<ConstraintViolation<ConstraintViolationsTest>> violations =
                execValidator.validateReturnValue(
                        this,
                        getAccessibleMethod(getClass(), "thePersonTest", ConstraintPerson.class),
                        new ConstraintPerson() // return value has null name
                );

        assertThat(violations).hasSize(1);
        assertThat(ConstraintViolations.determineStatus(violations)).isEqualTo(500);
    }

    @Test
    public void invalidPersonParamIs400() {
        Set<ConstraintViolation<ConstraintViolationsTest>> violations =
                execValidator.validateParameters(
                        this,
                        getAccessibleMethod(getClass(), "thePersonTest", ConstraintPerson.class),
                        new Object[]{new ConstraintPerson()} // the parameter value
                );

        assertThat(violations).hasSize(1);
        assertThat(ConstraintViolations.determineStatus(violations)).isEqualTo(400);
    }

    @Test
    public void invalidPersonRepresentationIs422() {
        Set<ConstraintViolation<ConstraintPerson>> violations =
                validator.validate(new ConstraintPerson());

        assertThat(violations).hasSize(1);
        assertThat(ConstraintViolations.determineStatus(violations)).isEqualTo(422);
    }
}
