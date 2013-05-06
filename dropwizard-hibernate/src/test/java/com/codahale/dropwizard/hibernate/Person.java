package com.codahale.dropwizard.hibernate;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.joda.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "people")
public class Person {
    @Id
    private String name;

    @Column
    private String email;

    @Column
    private LocalDateTime birthday;

    @JsonProperty
    public String getName() {
        return name;
    }

    @JsonProperty
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty
    public String getEmail() {
        return email;
    }

    @JsonProperty
    public void setEmail(String email) {
        this.email = email;
    }

    @JsonProperty
    public LocalDateTime getBirthday() {
        return birthday;
    }

    @JsonProperty
    public void setBirthday(LocalDateTime birthday) {
        this.birthday = birthday;
    }
}
