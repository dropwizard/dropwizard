package io.dropwizard.codegen.scanning;

import org.apache.maven.plugin.MojoExecutionException;
import org.junit.Test;

import java.util.List;

public class ResourcesScannerTest {

    @Test
    public void testScan() throws MojoExecutionException {
        ResourcesScanner scanner = new ResourcesScanner(null, null);

        List<ScannedClass> scannedClasses = scanner.scan("io.dropwizard", "http://localhost:8080");
        for(ScannedClass scannedClass : scannedClasses) {
            System.out.println(scannedClass.toString());
        }
    }
}
