package io.dropwizard.servlets.tasks;

import com.google.common.collect.ImmutableMultimap;
import org.junit.Test;

import java.io.PrintWriter;

public class PostBodyTaskTest {
    private final PostBodyTask task = new PostBodyTask("test") {
        @Override
        public void execute(ImmutableMultimap<String, String> parameters, String body, PrintWriter output) throws Exception {

        }
    };

    @Test(expected = UnsupportedOperationException.class)
    public void throwsExceptionWhenCallingExecuteWithoutThePostBody() throws Exception {
        task.execute(new ImmutableMultimap.Builder<String, String>().build(), new PrintWriter(System.out));
    }
}
