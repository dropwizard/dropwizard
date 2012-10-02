package com.yammer.dropwizard.cli;

import com.yammer.dropwizard.AbstractService;
import com.yammer.dropwizard.util.JarLocation;
import org.apache.commons.cli.HelpFormatter;

import java.io.PrintWriter;

@SuppressWarnings("UseOfSystemOutOrSystemErr")
public class UsagePrinter {
    private UsagePrinter() {
        // singleton
    }

    public static void printRootHelp(AbstractService<?> service) {
        System.err.printf("java -jar %s <command> [arg1 arg2]\n\n", new JarLocation(service.getClass()));
        System.err.println("Commands");
        System.err.println("========\n");

        for (Command command : service.getCommands()) {
            printCommandHelp(command, service.getClass());
        }
    }

    public static void printCommandHelp(Command cmd, Class<?> klass) {
        printCommandHelp(cmd, klass, null);
    }

    public static void printCommandHelp(Command cmd, Class<?> klass, String errorMessage) {
        if (errorMessage != null) {
            System.err.println(errorMessage);
            System.err.println();
        }

        System.err.println(formatTitle(cmd));
        final HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.setLongOptPrefix(" --");
        final PrintWriter pw = new PrintWriter(System.err);
        helpFormatter.printHelp(pw,
                                HelpFormatter.DEFAULT_WIDTH,
                                String.format("java -jar %s", cmd.getUsage(klass)),
                                null,
                                cmd.getOptionsWithHelp(),
                                HelpFormatter.DEFAULT_LEFT_PAD,
                                HelpFormatter.DEFAULT_DESC_PAD,
                                null,
                                false);
        pw.flush();
        System.err.println("\n");
    }

    private static String formatTitle(Command cmd) {
        final String title = cmd.getName() + ": " + cmd.getDescription();
        return title + '\n' + getBanner(title.length());
    }

    private static String getBanner(int length) {
        final StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            builder.append('-');
        }
        return builder.toString();
    }
}
