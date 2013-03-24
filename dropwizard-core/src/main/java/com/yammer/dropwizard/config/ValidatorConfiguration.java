package com.yammer.dropwizard.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.yammer.dropwizard.jersey.InvalidEntityExceptionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.ext.ExceptionMapper;

public class ValidatorConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(ValidatorConfiguration.class);

    @JsonProperty
    private String invalidEntityExceptionMapperClass = null;

    @JsonIgnore
    public ExceptionMapper getInvalidEntityExceptionMapper() {
        if (invalidEntityExceptionMapperClass == null) {
            return new InvalidEntityExceptionMapper();
        } else {
            try {
                return (ExceptionMapper) Class.forName(invalidEntityExceptionMapperClass).newInstance();
            } catch (Exception e) {
                throw new IllegalArgumentException("Cannot create instance of invalidEntityExceptionMapper="+invalidEntityExceptionMapperClass+": "+e, e);
            }
        }
    }

}
