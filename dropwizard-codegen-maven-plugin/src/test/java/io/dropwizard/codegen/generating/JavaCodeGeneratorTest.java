package io.dropwizard.codegen.generating;

import io.dropwizard.codegen.Something;
import io.dropwizard.codegen.scanning.ScannedClass;
import io.dropwizard.codegen.scanning.ScannedMethod;
import org.junit.Test;

import javax.ws.rs.HttpMethod;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.assertNotNull;

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
        scannedMethods.add(createScannedMethodsGet());
        scannedMethods.add(createScannedMethodsPost());
        scannedMethods.add(createScannedMethodsPut());
        scannedMethods.add(createScannedMethodsDelete());
        scannedMethods.add(createScannedMethodsHead());
        return scannedMethods;
    }

    private ScannedClass createScannedClass() {
        ScannedClass scannedClass = new ScannedClass();
        scannedClass.setName("MyResource");
        scannedClass.setScannedMethods(createScannedMethods());
        return scannedClass;
    }

    private ScannedMethod createScannedMethodsGet() {
        ScannedMethod scannedMethod = new ScannedMethod();
        scannedMethod.setName("getSomething");
        scannedMethod.setMethod(HttpMethod.GET);
        scannedMethod.setUrl("http://localhost:8080/something/get");
        scannedMethod.setClassToReturn(Something.class);
        return scannedMethod;
    }

    private ScannedMethod createScannedMethodsPost() {
        ScannedMethod scannedMethod = new ScannedMethod();
        scannedMethod.setName("addSomething");
        scannedMethod.setMethod(HttpMethod.POST);
        scannedMethod.setUrl("http://localhost:8080/something/add");
        scannedMethod.setClassToReturn(Something.class);
        scannedMethod.setClassToPost(Something.class);
        return scannedMethod;
    }

    private ScannedMethod createScannedMethodsPut() {
        ScannedMethod scannedMethod = new ScannedMethod();
        scannedMethod.setName("putSomething");
        scannedMethod.setMethod(HttpMethod.PUT);
        scannedMethod.setUrl("http://localhost:8080/something/put");
        scannedMethod.setClassToReturn(Something.class);
        scannedMethod.setClassToPost(Something.class);
        return scannedMethod;
    }

    private ScannedMethod createScannedMethodsDelete() {
        ScannedMethod scannedMethod = new ScannedMethod();
        scannedMethod.setName("deleteSomething");
        scannedMethod.setMethod(HttpMethod.DELETE);
        scannedMethod.setUrl("http://localhost:8080/something/delete");
        scannedMethod.setClassToReturn(Void.class);
        return scannedMethod;
    }

    private ScannedMethod createScannedMethodsHead() {
        ScannedMethod scannedMethod = new ScannedMethod();
        scannedMethod.setName("headSomething");
        scannedMethod.setMethod(HttpMethod.HEAD);
        scannedMethod.setUrl("http://localhost:8080/something/head");
        scannedMethod.setClassToReturn(Void.class);
        return scannedMethod;
    }
}
