package ${package};

import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Environment;

public class ${name}Service extends Service<${name}Configuration> {

    public static void main(final String[] args) throws Exception {
        new ${name}Service().run(args);
    }

    @Override
    public String getName() {
        return "${name}";
    }

    @Override
    public void initialize(final Bootstrap<${name}Configuration> bootstrap) {
        // TODO: service initialization
    }

    @Override
    public void run(final ${name}Configuration configuration,
                    final Environment environment) {
        // TODO: implement service
    }

}
