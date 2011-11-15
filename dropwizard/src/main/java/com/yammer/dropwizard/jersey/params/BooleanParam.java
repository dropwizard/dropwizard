package com.yammer.dropwizard.jersey.params;

// TODO: 11/14/11 <coda> -- document BooleanParam
// TODO: 11/14/11 <coda> -- test BooleanParam

public class BooleanParam extends AbstractParam<Boolean> {
    public BooleanParam(String input) {
        super(input);
    }

    @Override
    protected String errorMessage(String input, Exception e) {
        return '"' + input + "\" must be \"true\" or \"false\"";
    }

    @Override
    protected Boolean parse(String input) {
        return Boolean.valueOf(input);
    }
}
