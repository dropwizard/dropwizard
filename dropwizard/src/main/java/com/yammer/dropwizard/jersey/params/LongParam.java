package com.yammer.dropwizard.jersey.params;

// TODO: 11/14/11 <coda> -- document LongParam
// TODO: 11/14/11 <coda> -- test LongParam

public class LongParam extends AbstractParam<Long> {
    public LongParam(String input) {
        super(input);
    }

    @Override
    protected String errorMessage(String input, Exception e) {
        return '"' + input + "\" is not a number.";
    }

    @Override
    protected Long parse(String input) {
        return Long.valueOf(input);
    }
}
