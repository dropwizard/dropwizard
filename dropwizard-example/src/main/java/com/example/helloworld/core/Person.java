package com.example.helloworld.core;

import javax.persistence.*;
import javax.validation.constraints.Min;
import java.util.Objects;

@Entity
@Table(name = "people")
@NamedQueries(
        {
                @NamedQuery(
                        name = "com.example.helloworld.core.Person.findAll",
                        query = "SELECT p FROM Person p"
                )
        })
public class Person {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "fullName", nullable = false)
    private String fullName;

    @Column(name = "jobTitle", nullable = false)
    private String jobTitle;

    @Column(name = "age")
    @Min(value = 0)
    private int age;

    public Person() {
    }

    public Person(String fullName, String jobTitle, int age) {
        this.fullName = fullName;
        this.jobTitle = jobTitle;
        this.age = age;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof Person)) {
            return false;
        }

        Person person = (Person) o;

        return id == person.id &&
                age == person.age &&
                Objects.equals(fullName, person.fullName) &&
                Objects.equals(jobTitle, person.jobTitle);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, fullName, jobTitle, age);
    }
}
