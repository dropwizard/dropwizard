package io.dropwizard.lifecycle.setup;

import java.util.concurrent.atomic.AtomicLong;
public class ExecutorBuilderBase {
    protected static final AtomicLong COUNT = new AtomicLong(0);
}
