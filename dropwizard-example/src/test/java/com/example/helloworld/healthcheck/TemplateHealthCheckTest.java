package com.example.helloworld.healthcheck;

import com.example.helloworld.core.Template;
import com.example.helloworld.health.TemplateHealthCheck;
import com.codahale.dropwizard.testing.HealthCheckTest;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class TemplateHealthCheckTest extends HealthCheckTest {
    @Override
    protected void setUpHealthChecks() throws Exception {
        addHealthCheck("template", new TemplateHealthCheck(new Template("Hello, %s!", "Stranger")));
    }

    @Test
    public void healthCheck() throws Exception {
        assertThat(getContent(), is("{\"deadlocks\":{\"healthy\":true},\"template\":{\"healthy\":true}}"));
    }
}
