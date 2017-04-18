package io.dropwizard.codegen.scanning;

import java.util.ArrayList;
import java.util.List;

public class ScannedClass {

    private String name;
    private List<ScannedMethod> scannedMethods = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ScannedMethod> getScannedMethods() {
        return scannedMethods;
    }

    public void setScannedMethods(List<ScannedMethod> scannedMethods) {
        this.scannedMethods = scannedMethods;
    }

    @Override
    public String toString() {
        return "ScannedClass{" +
            "name='" + name + '\'' +
            ", scannedMethods=" + scannedMethods +
            '}';
    }
}
