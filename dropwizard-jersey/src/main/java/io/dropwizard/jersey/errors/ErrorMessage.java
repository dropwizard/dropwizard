package io.dropwizard.jersey.errors;

public class ErrorMessage {
    private final Integer code;
    private final String message;

    public ErrorMessage(String message) {
        this(500, message);
    }
    
    public ErrorMessage(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
