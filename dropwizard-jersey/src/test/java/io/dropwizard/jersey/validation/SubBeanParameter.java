package io.dropwizard.jersey.validation;

import org.hibernate.validator.constraints.NotEmpty;

import javax.ws.rs.QueryParam;

public class SubBeanParameter extends BeanParameter {
    @QueryParam("address")
    @NotEmpty
    private String address;

    public String getAddress() {
        return address;
    }
}
