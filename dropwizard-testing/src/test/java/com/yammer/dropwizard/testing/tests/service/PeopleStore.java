package com.yammer.dropwizard.testing.tests.service;

import com.yammer.dropwizard.testing.tests.Person;

public interface PeopleStore {
    Person fetchPerson(String name);
}
