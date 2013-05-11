package com.codahale.dropwizard.testing.app;

import com.codahale.dropwizard.testing.Person;

public interface PeopleStore {
    Person fetchPerson(String name);
}
