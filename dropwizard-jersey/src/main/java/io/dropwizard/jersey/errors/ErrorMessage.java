package io.dropwizard.jersey.errors;

public class ErrorMessage {
    private final int code;
    private final String message;
    private final String details;

    public ErrorMessage(String message) {
        this(500, message);
    }

    public ErrorMessage(int code, String message) {
        this(code, message, null);
    }

    public ErrorMessage(int code, String message, String details) {
        this.code = code;
        this.message = message;
        this.details = details;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getDetails() {
        return details;
    }
}
