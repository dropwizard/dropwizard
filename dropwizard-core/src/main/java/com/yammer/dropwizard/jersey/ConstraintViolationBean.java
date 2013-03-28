package com.yammer.dropwizard.jersey;

import javax.validation.ConstraintViolation;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ConstraintViolationBean {

    @XmlElement private String messageTemplate;
    @XmlElement private String message;
    @XmlElement private String invalidValue;

    public ConstraintViolationBean(String messageTemplate, String message, String invalidValue) {
        this.messageTemplate = messageTemplate;
        this.message = message;
        this.invalidValue = invalidValue;
    }

    public ConstraintViolationBean(ConstraintViolation violation) {
        this.messageTemplate = violation.getMessageTemplate();
        this.message = violation.getMessage();
        try {
            this.invalidValue = violation.getInvalidValue().toString();
        } catch (Exception e) {
            this.invalidValue = "Error converting invalid value to String: "+e;
        }
    }

    public String getMessageTemplate() { return messageTemplate; }
    public String getMessage() { return message; }
    public String getInvalidValue() { return invalidValue; }

}
