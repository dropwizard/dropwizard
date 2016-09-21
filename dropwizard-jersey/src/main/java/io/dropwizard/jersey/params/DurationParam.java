/*
 * Copyright (c) 2016. Sense Labs, Inc. All Rights Reserved
 */

package io.dropwizard.jersey.params;

import io.dropwizard.util.Duration;

public class DurationParam extends AbstractParam<Duration> {

    public DurationParam(String input) {
        super(input);
    }

    public DurationParam(String input, String parameterName) {
        super(input, parameterName);
    }

    @Override
    protected Duration parse(String input) throws Exception {
        return Duration.parse(input);
    }

}
