package io.dropwizard.hibernate;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Nullable;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "dogs")
public class Dog {
    @Id
    @Nullable
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner")
    @Nullable
    private Person owner;

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
    @Nullable
    public Person getOwner() {
        return owner;
    }

    @JsonProperty
    public void setOwner(Person owner) {
        this.owner = owner;
    }
}
