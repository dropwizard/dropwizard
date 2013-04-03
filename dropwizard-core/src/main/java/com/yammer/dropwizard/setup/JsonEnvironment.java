package com.yammer.dropwizard.setup;

import com.yammer.dropwizard.json.ObjectMapperFactory;

public class JsonEnvironment extends ObjectMapperFactory {
    public JsonEnvironment(ObjectMapperFactory factory) {
        super(factory);
    }
}
