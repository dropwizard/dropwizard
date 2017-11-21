package io.dropwizard.testing.app;

public interface PeopleStore {
    Person fetchPerson(String name);
}
