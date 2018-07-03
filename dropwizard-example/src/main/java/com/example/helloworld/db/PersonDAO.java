package com.example.helloworld.db;

import com.example.helloworld.core.Person;
import io.dropwizard.hibernate.AbstractDAO;
import io.dropwizard.hibernate.ClusteredSessionFactory;

import java.util.List;
import java.util.Optional;

public class PersonDAO extends AbstractDAO<Person> {
    public PersonDAO(ClusteredSessionFactory clusteredSessionFactory) {
        super(clusteredSessionFactory);
    }

    public Optional<Person> findById(Long id) {
        return Optional.ofNullable(get(id));
    }

    public Person create(Person person) {
        return persist(person);
    }

    public List<Person> findAll() {
        return list(namedQuery("com.example.helloworld.core.Person.findAll"));
    }
}
