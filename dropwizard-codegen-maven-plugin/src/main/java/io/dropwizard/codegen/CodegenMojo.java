package io.dropwizard.codegen;

import io.dropwizard.codegen.generating.JavaCodeGenerator;
import io.dropwizard.codegen.scanning.ResourcesScanner;
import io.dropwizard.codegen.scanning.ScannedClass;
import io.dropwizard.codegen.scanning.ScannedMethod;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.util.List;

@Mojo( name = "codegen")
public class CodegenMojo extends AbstractMojo {

    @Parameter( defaultValue = "${project}", readonly = true )
    private MavenProject mavenProject;

    @Parameter(property = "packageToScan", required = true)
    private String packageToScan;

    @Parameter( property = "generatedCodeFolder", defaultValue = "generated-code" )
    private String generatedCodeFolder;

    @Parameter( property = "generatedCodePackage", defaultValue = "com.example.services" )
    private String generatedCodePackage;

    @Parameter(property = "rootHost", defaultValue = "http://localhost:8080/")
    private String rootHost;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("Generating code - " + generatedCodeFolder);
        getLog().info("Scan package - " + packageToScan);

        ResourcesScanner scanner = new ResourcesScanner(getLog(), mavenProject);

        List<ScannedClass> scannedClasses = scanner.scan(packageToScan, rootHost);
        for(ScannedClass scannedClass : scannedClasses) {
            getLog().info("Class that is scanned: " + scannedClass);
        }

        getLog().info("Generated code folder" + new File(generatedCodeFolder).getAbsolutePath());

        JavaCodeGenerator.generate(scannedClasses, generatedCodeFolder, generatedCodePackage);
    }

}
