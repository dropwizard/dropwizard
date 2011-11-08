package com.yammer.dropwizard.jersey.params;

public class IntParam extends AbstractParam<Integer> {
    public IntParam(String input) {
        super(input);
    }

    @Override
    protected String errorMessage(String input, Exception e) {
        return "\"" + input + "\" is not a number.";
    }

    @Override
    protected Integer parse(String input) {
        return Integer.valueOf(input);
    }
}
