package io.dropwizard.testing.app;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Application;
import io.dropwizard.jersey.PATCH;
import io.dropwizard.servlets.tasks.PostBodyTask;
import io.dropwizard.servlets.tasks.Task;
import io.dropwizard.setup.Environment;

import javax.annotation.Nullable;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DropwizardTestApplication extends Application<TestConfiguration> {
    @Override
    public void run(TestConfiguration configuration, Environment environment) throws Exception {
        environment.jersey().register(new TestResource(configuration.getMessage()));
        environment.admin().addTask(new HelloTask());
        environment.admin().addTask(new EchoTask());
    }

    @Path("/")
    public static class TestResource {

        private final String message;

        public TestResource(String message) {
            this.message = message;
        }

        @Path("test")
        @GET
        public String test() {
            return message;
        }

        @Path("message")
        @GET
        @Produces(MediaType.APPLICATION_JSON)
        public MessageView messageView() {
            return new MessageView(message);
        }

        @Path("echoPatch")
        @PATCH
        public String echoPatch(String patchMessage) {
            return patchMessage;
        }
    }

    public static class HelloTask extends Task {

        public HelloTask() {
            super("hello");
        }

        @Override
        public void execute(Map<String, List<String>> parameters, PrintWriter output) throws Exception {
            List<String> names = parameters.getOrDefault("name", Collections.emptyList());
            String name = !names.isEmpty() ? names.get(0) : "Anonymous";
            output.print("Hello has been said to " + name);
            output.flush();
        }
    }

    public static class EchoTask extends PostBodyTask {

        public EchoTask() {
            super("echo");
        }

        @Override
        public void execute(Map<String, List<String>> parameters, String body, PrintWriter output) throws Exception {
            output.print(body);
            output.flush();
        }
    }

    public static class MessageView {

        @Nullable
        private String message;

        @JsonCreator
        public MessageView(@JsonProperty("message") @Nullable String message) {
            this.message = message;
        }

        @JsonProperty
        public Optional<String> getMessage() {
            return Optional.ofNullable(message);
        }
    }
}
