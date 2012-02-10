package com.yammer.dropwizard.cli;

import com.yammer.dropwizard.AbstractService;
import com.yammer.dropwizard.util.JarLocation;
import org.apache.commons.cli.HelpFormatter;

@SuppressWarnings("UseOfSystemOutOrSystemErr")
public class UsagePrinter {
    private UsagePrinter() {
        // singleton
    }

    public static void printRootHelp(AbstractService<?> service) {
        System.out.printf("java -jar %s <command> [arg1 arg2]\n\n", new JarLocation(service.getClass()));
        System.out.println("Commands");
        System.out.println("========\n");

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
            System.out.println();
        }

        System.out.println(formatTitle(cmd));
        final HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.setLongOptPrefix(" --");
        helpFormatter.printHelp(String.format("java -jar %s", cmd.getUsage(klass)),
                                cmd.getOptionsWithHelp());
        System.out.println("\n");
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
