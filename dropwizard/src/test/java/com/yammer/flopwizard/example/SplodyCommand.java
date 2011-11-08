package com.yammer.flopwizard.example;

import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.cli.Command;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;

@SuppressWarnings("NullableProblems")
public class SplodyCommand extends Command {
    public SplodyCommand() {
        super("splody", "throws various errors");
    }

    @Override
    public Options getOptions() {
        final OptionGroup group = new OptionGroup();
        group.setRequired(true);
        group.addOption(new Option("r", "required", false, "a required option"));
        
        return new Options()
                .addOptionGroup(group)
                .addOption("e", "exception", false, "throw an exception");
    }

    @Override
    protected void run(Service service,
                       CommandLine params) throws Exception {
        if (params.hasOption('e')) {
            System.err.println("throwing an exception");
            throw new IllegalAccessError("AW GAWD");
        }
    }
}
