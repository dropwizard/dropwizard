package io.dropwizard.servlets.tasks;

import com.google.common.collect.ImmutableMultimap;
import org.junit.Test;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class PostBodyTaskTest {
    private final PostBodyTask task = new PostBodyTask("test") {
        @Override
        public void execute(ImmutableMultimap<String, String> parameters, String body, PrintWriter output) throws Exception {

        }
    };

    @SuppressWarnings("deprecation")
    @Test
    public void throwsExceptionWhenCallingExecuteWithoutThePostBody() throws Exception {
        assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(() ->
            task.execute(new ImmutableMultimap.Builder<String, String>().build(),
                new PrintWriter(new OutputStreamWriter(System.out, UTF_8))));
    }
}
