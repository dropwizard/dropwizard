
package io.dropwizard;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMultimap;
import io.dropwizard.servlets.tasks.Task;
import java.io.PrintWriter;

/**
 * used to serve current configuration in response to POSTS to host:adminport/tasks/config
 * @author JAshe
 */
public class ConfigTask extends Task {

    private Configuration config;
    
    /**
     * Create a config task
     * 
     * @param c 
     */
    public ConfigTask(Configuration c) {
        super("config");
        config = c;
    }
    
    @Override
    public void execute(ImmutableMultimap<String, String> im, PrintWriter writer) throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        String indented = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(config);

        writer.write(indented);

    }

}
