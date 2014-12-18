package io.dropwizard.quartz;

import io.dropwizard.Configuration;

public interface QuartzConfiguration<T extends Configuration>
{
    QuartzFactory getQuartzFactory(T configuration);
}

