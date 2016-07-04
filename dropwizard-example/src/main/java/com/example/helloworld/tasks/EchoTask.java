package com.example.helloworld.tasks;

import com.google.common.collect.ImmutableMultimap;
import io.dropwizard.servlets.tasks.PostBodyTask;

import java.io.PrintWriter;

public class EchoTask extends PostBodyTask {
    public EchoTask() {
        super("echo");
    }

    @Override
    public void execute(ImmutableMultimap<String, String> parameters, String body, PrintWriter output) throws Exception {
        output.print(body);
        output.flush();
    }
}
