package io.dropwizard.codegen;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.junit.Test;

public class CodegenMojoTest {

    @Test
    public void testCodegenMojo() {
        CodegenMojo codegenMojo = new CodegenMojo();
        try {
            codegenMojo.execute();
        } catch (Exception e) {
        }
    }
}
