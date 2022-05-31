package io.dropwizard.documentation;

public class PersonDAO {
    public Person find(String id) {
        return new Person("Edmond Dantès");
    }
}