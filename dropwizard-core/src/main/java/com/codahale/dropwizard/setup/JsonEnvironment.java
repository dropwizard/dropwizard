package com.codahale.dropwizard.setup;

import com.codahale.dropwizard.json.ObjectMapperFactory;

public class JsonEnvironment extends ObjectMapperFactory {
    public JsonEnvironment(ObjectMapperFactory factory) {
        super(factory);
    }
}
