package com.yammer.dropwizard.hibernate.tests;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.joda.time.DateTime;

import javax.persistence.*;

@Entity
@Table(name = "people")
@SuppressWarnings("UnusedDeclaration")
public class Person {
    @Id
    @JsonProperty
    private String name;

    @Column
    @JsonProperty
    private String email;

    @Column
    private DateTime birthday;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public DateTime getBirthday() {
        return birthday;
    }

    public void setBirthday(DateTime birthday) {
        this.birthday = birthday;
    }
}
