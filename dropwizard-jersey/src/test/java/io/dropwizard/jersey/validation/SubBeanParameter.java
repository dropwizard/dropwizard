package io.dropwizard.jersey.validation;

import io.dropwizard.jersey.jackson.JacksonMessageBodyProviderTest;
import io.dropwizard.validation.ValidationMethod;
import org.assertj.core.util.Strings;
import org.hibernate.validator.constraints.NotEmpty;

import javax.ws.rs.QueryParam;

public class SubBeanParameter extends BeanParameter {
    @QueryParam("address")
    @NotEmpty
    private String address;


    @ValidationMethod(message="address must not be uppercase",
        groups = JacksonMessageBodyProviderTest.Partial1.class)
    public boolean isAddressNotUppercase() {
        return Strings.isNullOrEmpty(address) || (!address.toUpperCase().equals(address));
    }

    public String getAddress() {
        return address;
    }
}
