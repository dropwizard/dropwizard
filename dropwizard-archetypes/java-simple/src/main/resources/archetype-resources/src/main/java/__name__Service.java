package ${package};

import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.config.Environment;

public class ${name}Service extends Service<${name}Configuration> {

    public static void main(String[] args) throws Exception {
        new ${name}Service().run(args);
    }

    private ${name}Service() {
        super("${name}");
    }

    @Override
    protected void initialize(${name}Configuration configuration,
                              Environment environment) {
      // TODO: implement service
    }

}
