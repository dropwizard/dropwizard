package com.codahale.dropwizard;

import java.util.concurrent.Executor;

import com.codahale.dropwizard.setup.Environment;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.common.util.concurrent.AbstractIdleService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.Service;

@JsonTypeName("nop")
public class NopServerFactory implements ServerFactory
{
    public Service build(Environment environment)
    {
        return new AbstractIdleService() {
            @Override
            protected Executor executor()
            {
                return MoreExecutors.sameThreadExecutor();
            }

            @Override
            protected void startUp() throws Exception
            {
            }

            @Override
            protected void shutDown() throws Exception
            {
            }
        };
    }
}