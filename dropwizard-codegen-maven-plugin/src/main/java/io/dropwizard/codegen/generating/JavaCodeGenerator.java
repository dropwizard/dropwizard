package io.dropwizard.codegen.generating;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import io.dropwizard.codegen.scanning.ScannedClass;
import io.dropwizard.codegen.scanning.ScannedMethod;
import no.bouvet.jsonclient.JsonClient;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.lang.model.element.Modifier;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.Response;

public class JavaCodeGenerator {

    public static void generate(List<ScannedClass> scannedClasses, String generatedCodeFolder, String generatedCodePackage) {
        for (ScannedClass scannedClass : scannedClasses) {

            TypeSpec serviceClass = TypeSpec.classBuilder(scannedClass.getName().replaceAll("Resource", "").concat("Service"))
                .addField(createJsonClientField())
                .addModifiers(Modifier.PUBLIC)
                .addMethods(createMethodSpecList(scannedClass.getScannedMethods()))
                .build();

            JavaFile javaFile = JavaFile.builder(generatedCodePackage, serviceClass).build();

            try {
                javaFile.writeTo(new File(generatedCodeFolder));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static FieldSpec createJsonClientField() {
        return FieldSpec.builder(JsonClient.class, "jsonClient")
                    .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                    .initializer("new $T()", JsonClient.class)
                    .build();
    }

    private static List<MethodSpec> createMethodSpecList(List<ScannedMethod> scannedMethods) {
        List<MethodSpec> methodSpecList = new ArrayList<>();
        for(ScannedMethod scannedMethod : scannedMethods) {
            MethodSpec methodSpec = createMethodSpec(scannedMethod);
            if(methodSpec != null) {
                methodSpecList.add(methodSpec);
            }
        }
        Collections.sort(methodSpecList, createMethodSpecComparator());
        return methodSpecList;
    }

    private static MethodSpec createMethodSpec(ScannedMethod scannedMethod) {
        if(HttpMethod.GET.equals(scannedMethod.getMethod())) {
            return MethodSpec.methodBuilder(scannedMethod.getName())
                .returns(scannedMethod.getClassToReturn())
                .addStatement("return jsonClient.http().get($S).object($T.class)", scannedMethod.getUrl(), scannedMethod.getClassToReturn())
                .addModifiers(Modifier.PUBLIC)
                .build();
        } else if(HttpMethod.POST.equals(scannedMethod.getMethod())) {
            return MethodSpec.methodBuilder(scannedMethod.getName())
                .addParameter(scannedMethod.getClassToPost(), "dataToPost")
                .returns(scannedMethod.getClassToReturn())
                .addStatement("return jsonClient.http().post($S, dataToPost).object($T.class)", scannedMethod.getUrl(), scannedMethod.getClassToReturn())
                .addModifiers(Modifier.PUBLIC)
                .build();
        } else if(HttpMethod.PUT.equals(scannedMethod.getMethod())) {
            return MethodSpec.methodBuilder(scannedMethod.getName())
                .addParameter(scannedMethod.getClassToPost(), "dataToPost")
                .returns(scannedMethod.getClassToReturn())
                .addStatement("return jsonClient.http().put($S, dataToPost).object($T.class)", scannedMethod.getUrl(), scannedMethod.getClassToReturn())
                .addModifiers(Modifier.PUBLIC)
                .build();
        } else if(HttpMethod.DELETE.equals(scannedMethod.getMethod())) {
            return MethodSpec.methodBuilder(scannedMethod.getName())
                .returns(scannedMethod.getClassToReturn())
                .addStatement("return jsonClient.http().delete($S).object($T.class)", scannedMethod.getUrl(), scannedMethod.getClassToReturn())
                .addModifiers(Modifier.PUBLIC)
                .build();
        } else {
            return null;
        }
    }

    private static Comparator<MethodSpec> createMethodSpecComparator() {
        return new Comparator<MethodSpec>() {
            @Override
            public int compare(MethodSpec o1, MethodSpec o2) {
                return o1.name.compareTo(o2.name);
            }
        };
    }

}
