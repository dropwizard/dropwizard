package com.yammer.dropwizard.hibernate.tests;

import javax.persistence.*;

@Entity
@Table(name = "people")
@SuppressWarnings("UnusedDeclaration")
public class ExampleEntity {
    @Id
    private String name;

    @Column
    private String email;

    @Column
    private int age;

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public int getAge() {
        return age;
    }
}
