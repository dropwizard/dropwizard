package com.codahale.dropwizard.testing.tests.service;

import com.codahale.dropwizard.testing.tests.Person;

public interface PeopleStore {
    Person fetchPerson(String name);
}
