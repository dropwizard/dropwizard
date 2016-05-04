package io.dropwizard.codegen.generating;

import io.dropwizard.codegen.Something;
import io.dropwizard.codegen.scanning.ScannedClass;
import io.dropwizard.codegen.scanning.ScannedMethod;
import org.junit.Test;

import javax.ws.rs.HttpMethod;
import java.util.ArrayList;
import java.util.List;

public class JavaCodeGeneratorTest {

    @Test
    public void testGenerateCode() {
        String generatedCodeFolder = "src/test/java";
        String generatedCodePackage = "com.example.helloworld";
        List<ScannedClass> scannedClasses = createScannedClasses();
        JavaCodeGenerator.generate(scannedClasses, generatedCodeFolder, generatedCodePackage);
    }

    private List<ScannedClass> createScannedClasses() {
        List<ScannedClass> scannedClasses = new ArrayList<>();
        scannedClasses.add(createScannedClass());
        return scannedClasses;
    }

    private List<ScannedMethod> createScannedMethods() {
        List<ScannedMethod> scannedMethods = new ArrayList<>();
        scannedMethods.add(createScannedMethods1());
        scannedMethods.add(createScannedMethods2());
        return scannedMethods;
    }

    private ScannedClass createScannedClass() {
        ScannedClass scannedClass = new ScannedClass();
        scannedClass.setName("MyResource");
        scannedClass.setScannedMethods(createScannedMethods());
        return scannedClass;
    }

    private ScannedMethod createScannedMethods1() {
        ScannedMethod scannedMethod = new ScannedMethod();
        scannedMethod.setName("getSomething");
        scannedMethod.setMethod(HttpMethod.GET);
        scannedMethod.setUrl("http://localhost:8080/something/get");
        scannedMethod.setClassToReturn(Something.class);
        return scannedMethod;
    }

    private ScannedMethod createScannedMethods2() {
        ScannedMethod scannedMethod = new ScannedMethod();
        scannedMethod.setName("addSomething");
        scannedMethod.setMethod(HttpMethod.POST);
        scannedMethod.setUrl("http://localhost:8080/something/add");
        scannedMethod.setClassToReturn(Something.class);
        scannedMethod.setClassToPost(Something.class);
        return scannedMethod;
    }
}
