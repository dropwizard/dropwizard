package io.dropwizard.hibernate;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.jspecify.annotations.Nullable;

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
