package io.dropwizard.setup;

import io.dropwizard.Configuration;
import io.dropwizard.HttpApplication;
import io.dropwizard.HttpConfiguration;
import io.dropwizard.jersey.validation.NonEmptyStringParamUnwrapper;
import io.dropwizard.jersey.validation.ParamValidatorUnwrapper;
import io.dropwizard.validation.valuehandling.OptionalValidatedValueUnwrapper;
import org.hibernate.validator.internal.engine.ValidatorFactoryImpl;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class HttpBootstrapTest {
    private final HttpApplication<HttpConfiguration> application = new HttpApplication<HttpConfiguration>() {
        @Override
        public void run(HttpConfiguration configuration, HttpEnvironment environment) throws Exception {
        }
    };
    private HttpBootstrap<HttpConfiguration> bootstrap;

    @Before
    public void setUp() {
        bootstrap = new HttpBootstrap<>(application);
    }

    @Test
    public void defaultsToDefaultValidatorFactory() throws Exception {
        assertThat(bootstrap.getValidatorFactory()).isInstanceOf(ValidatorFactoryImpl.class);

        ValidatorFactoryImpl validatorFactory = (ValidatorFactoryImpl)bootstrap.getValidatorFactory();
        assertThat(validatorFactory.getValidatedValueHandlers()).hasSize(3);
        assertThat(validatorFactory.getValidatedValueHandlers().get(0))
                .isInstanceOf(OptionalValidatedValueUnwrapper.class);

        // It's imperative that the NonEmptyString validator come before the general param validator
        // because a NonEmptyString is a param that wraps an optional and the Hibernate Validator
        // can't unwrap nested classes it knows how to unwrap.
        // https://hibernate.atlassian.net/browse/HV-904
        assertThat(validatorFactory.getValidatedValueHandlers().get(1))
                .isInstanceOf(NonEmptyStringParamUnwrapper.class);

        assertThat(validatorFactory.getValidatedValueHandlers().get(2))
                .isInstanceOf(ParamValidatorUnwrapper.class);
    }

}
