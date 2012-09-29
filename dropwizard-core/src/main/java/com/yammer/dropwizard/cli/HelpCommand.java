package com.yammer.dropwizard.cli;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.yammer.dropwizard.AbstractService;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

@Parameters(commandNames = "help",
            commandDescription = "Show usage information")
public class HelpCommand implements Command {
    @Parameter(description = "[command1 ... command2]")
    @SuppressWarnings({ "FieldMayBeFinal", "MismatchedQueryAndUpdateOfCollection" })
    private List<String> commands = new ArrayList<String>(1);

    private final JCommander commander;
    private final PrintStream output;

    public HelpCommand(JCommander commander, PrintStream output) {
        this.commander = commander;
        this.output = output;
    }

    @Override
    public void run(AbstractService<?> service) throws Exception {
        final StringBuilder builder = new StringBuilder();
        if (commands.isEmpty()) {
            commander.usage(builder);
        } else {
            for (String command : commands) {
                commander.usage(command, builder);
            }
        }
        output.print(builder.toString());
    }
}
