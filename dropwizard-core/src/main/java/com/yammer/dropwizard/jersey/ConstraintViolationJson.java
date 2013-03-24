package com.yammer.dropwizard.jersey;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ConstraintViolationJson {

    @JsonProperty private String messageTemplate;
    @JsonProperty private String message;
    @JsonProperty private String invalidValue;

    public ConstraintViolationJson(String messageTemplate, String message, String invalidValue) {
        this.messageTemplate = messageTemplate;
        this.message = message;
        this.invalidValue = invalidValue;
    }

    public String getMessageTemplate() { return messageTemplate; }
    public String getMessage() { return message; }
    public String getInvalidValue() { return invalidValue; }
}
