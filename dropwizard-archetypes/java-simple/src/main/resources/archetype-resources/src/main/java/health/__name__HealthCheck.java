package ${package}.health;

import com.codahale.metrics.health.HealthCheck;

public class ${name}HealthCheck extends HealthCheck {

    @Override
    protected Result check() throws Exception {
        return Result.healthy();
    }
}
