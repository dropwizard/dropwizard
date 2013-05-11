package com.codahale.dropwizard.testing.service;

import com.codahale.dropwizard.testing.Person;

public interface PeopleStore {
    Person fetchPerson(String name);
}
