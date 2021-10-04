package io.dropwizard.jackson;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Person {
    private final String name;
    private final String surname;

    public Person(@JsonProperty("name") String name, @JsonProperty("surname") String surname) {
        this.name = name;
        this.surname = surname;
    }

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }
}
