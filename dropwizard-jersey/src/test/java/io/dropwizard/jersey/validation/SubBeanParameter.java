package io.dropwizard.jersey.validation;

import io.dropwizard.jersey.jackson.JacksonMessageBodyProviderTest;
import io.dropwizard.validation.ValidationMethod;
import jakarta.validation.constraints.NotEmpty;

import jakarta.ws.rs.QueryParam;
import java.util.Locale;

public class SubBeanParameter extends BeanParameter {
    @QueryParam("address")
    @NotEmpty
    private String address = "";


    @ValidationMethod(message = "address must not be uppercase",
        groups = JacksonMessageBodyProviderTest.Partial1.class)
    public boolean isAddressNotUppercase() {
        return address == null || address.isEmpty() || (!address.toUpperCase(Locale.US).equals(address));
    }

    public String getAddress() {
        return address;
    }
}
