package com.yammer.flopwizard.example;

import com.yammer.metrics.core.HealthCheck;

public class DumbHealthCheck extends HealthCheck {
    @Override
    public Result check() throws Exception {
        return Result.healthy("YAY!");
    }

    @Override
    public String name() {
        return "dumb";
    }
}
