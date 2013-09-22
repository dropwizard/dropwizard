package io.dropwizard.testing.app;

import io.dropwizard.testing.Person;

public interface PeopleStore {
    Person fetchPerson(String name);
}
