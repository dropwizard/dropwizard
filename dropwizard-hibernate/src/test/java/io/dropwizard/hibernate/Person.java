package io.dropwizard.hibernate;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.jspecify.annotations.Nullable;

import java.time.ZonedDateTime;

@Entity
@Table(name = "people")
public class Person {
    @Id
    private String name = "";

    @Column
    private String email = "";

    @Column
    @Nullable
    private ZonedDateTime birthday;

    @JsonProperty
    @Nullable
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
    @Nullable
    public ZonedDateTime getBirthday() {
        return birthday;
    }

    @JsonProperty
    public void setBirthday(@Nullable ZonedDateTime birthday) {
        this.birthday = birthday;
    }
}
