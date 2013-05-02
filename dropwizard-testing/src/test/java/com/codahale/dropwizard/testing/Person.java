package com.codahale.dropwizard.testing;

import com.google.common.base.Objects;

public class Person {
    private String name;
    private String email;

    public Person() { /* Jackson deserialization */ }

    public Person(String name, String email) {
        this.name = name;
        this.email = email;
    }

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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) { return true; }
        if ((obj == null) || (getClass() != obj.getClass())) { return false; }

        final Person person = (Person) obj;
        return !((email != null) ? !email.equals(person.email) : (person.email != null)) &&
                !((name != null) ? !name.equals(person.name) : (person.name != null));
    }

    @Override
    public int hashCode() {
        int result = (name != null) ? name.hashCode() : 0;
        result = (31 * result) + ((email != null) ? email.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).add("name", name).add("email", email).toString();
    }
}
